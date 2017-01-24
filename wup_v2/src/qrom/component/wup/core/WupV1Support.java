package qrom.component.wup.core;

import qrom.component.log.QRomLog;
import qrom.component.wup.apiv2.LogTicketProxy;
import qrom.component.wup.apiv2.LogTicketProxy.LogTicketProxyCallback;
import qrom.component.wup.apiv2.WupException;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.RunEnv;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.guid.GuidProxy;
import TRom.log.LogReportStubAndroid.GetTicketResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * 前一个wup版本中的需要用的api
 * 
 * @author wileywang
 *
 */
public class WupV1Support extends BroadcastReceiver {
	private static final String TAG = WupV1Support.class.getSimpleName();
	
	/** 小工具查询guid 广播*/
    public static final String ACTION_WUP_TOOL_GET_GUID = ".get.tencent.qrom.tools.wup.guid";
    /** 小工具接收guid 广播*/
    public static final String ACTION_WUP_TOOL_SHOW_GUID = "show.tencent.qrom.tools.wup.guid";
    
    /** log sdk发送获取ticket 广播*/
    public final static String ACTION_WUP_LOGSDK_GETTICKET_INFO = ".qrom.intent.action.wup.logsdk.getLogTicket";
    /** log sdk 发送ticket相关信息 */
    public final static String ACTION_WUP_LOGSDK_REPORT_LOG_INFO = ".qrom.intent.action.REPORT_LOG_INFO";
    
    /** wup sdk -- 数据更新广播*/
    public static final String ACTION_WUP_SDK_BASEDATA_UPDATED = "qrom.component.wup.sdk.baseData.updated";
    /** wup sdk -- 向rom sys请求数据 */
    public static final String ACTION_WUP_SDK_ROMSYS_REQDATAS = "qrom.component.wup.sdk.romsys.ReqDatas";
    /** wup sdk -- rom sys响应数据 */
    public static final String ACTION_WUP_SDK_ROMSYS_RSPDATAS = "qrom.component.wup.sdk.romsys.RspDatas";
    /** wup sdk -- rom sys iplist数据 */
    public static final String ACTION_WUP_SDK_ROMSYS_IPDATAS = ".qrom.component.wup.sdk.romsys.RspDatas.iplist";
	
	public static final String ACTION_WUP_SDK_PARM_FLG_TYPE = "req_type";
	public static final String ACTION_WUP_SDK_PARM_FLG_PKG = "pkg_name";
	public static final String ACTION_WUP_SDK_PARM_FLG_DATAS = "datas";
	public static final String ACTION_WUP_SDK_PARM_FLG_EXTRA = "extra";
	
	//  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ log sdk发送数据的key ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    
    public static final String LOGSDK_PARAM_KEY_APP_PKGNAME = "app_pkgName";
    public static final String LOGSDK_PARAM_KEY_REPORT_RESID = "report_resId";
    public static final String LOGSDK_PARAM_KEY_TICKET_TIMEOUT = "ticket_timeout";
    public static final String LOGSDK_PARAM_KEY_REPORT_PID = "report_pid";
    public static final String LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA = "report_extra_data";
    public static final String LOGSDK_PARAM_KEY_APP_TICKET = "app_ticket";
    public static final String LOGSDK_PARAM_KEY_APP_TICKET_RSPCODE = "log_ticket_rspcode";
    public static final String LOGSDK_PARAM_KEY_ENV_FLG = "env_flg";
    //  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ log sdk发送数据的key ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	
	private static WupV1Support sInstance;
	public static WupV1Support get() {
		if (sInstance == null) {
			synchronized(WupV1Support.class) {
				if (sInstance == null) {
					sInstance = new WupV1Support();
				}
			}
		}
		
		return sInstance;
	}
	
	public void onContextAttached(Context applicationContext) {
		IntentFilter filter = new IntentFilter();
		
		filter.addAction(applicationContext.getPackageName() + ACTION_WUP_TOOL_GET_GUID);
		filter.addAction(applicationContext.getPackageName() + ACTION_WUP_LOGSDK_GETTICKET_INFO);
		
		applicationContext.registerReceiver(this, filter);
	}
	
	private WupV1Support() {
	}
	
	public void sendBroadcastForWupBaseDataUpdate(int reqType) {
        // 发送通知数据改变
        Intent intent = new Intent(ACTION_WUP_SDK_BASEDATA_UPDATED);
        intent.putExtra(ACTION_WUP_SDK_PARM_FLG_TYPE, reqType);
        ContextHolder.getApplicationContextForSure().sendBroadcast(intent);
        QRomLog.v(TAG, "sendBroadcastForWupBaseDataUpdate-> ACTION_WUP_SDK_BASEDATA_UPDATED reqType = " + reqType);
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		QRomLog.d(TAG, "onReceive -> action = " + action);
		
		final String packageName = context.getApplicationContext().getPackageName();
		String toolGetGuidAction = packageName + ACTION_WUP_TOOL_GET_GUID;
		if (toolGetGuidAction.equals(action)) {
			
			Intent rspIntent = new Intent(ACTION_WUP_TOOL_SHOW_GUID);
            rspIntent.putExtra("app_pkgName", packageName);
            rspIntent.putExtra("app_guid", GuidProxy.get().getGuidBytes());
            rspIntent.putExtra("app_test", (RunEnv.get().getEnvType() == RunEnvType.Gamma));
            rspIntent.putExtra("app_qua", BaseInfoManager.get().getQua());
            rspIntent.putExtra("app_lc", BaseInfoManager.get().getLC());
            rspIntent.putExtra("app_romId", 0);
            context.getApplicationContext().sendBroadcast(rspIntent);
            
		} else {
			String logSdkAction = packageName + ACTION_WUP_LOGSDK_GETTICKET_INFO;
			if (logSdkAction.equals(action)) {
				
				 final String reqAppPkg = intent.getStringExtra(LOGSDK_PARAM_KEY_APP_PKGNAME);
                 // 请求超时时间
                 int reqTimeoutMs = intent.getIntExtra(LOGSDK_PARAM_KEY_TICKET_TIMEOUT, 30000);
                 // 请求的id
                 final int reportResId = intent.getIntExtra(LOGSDK_PARAM_KEY_REPORT_RESID, -99);
                 final int reoprtPid =  intent.getIntExtra(LOGSDK_PARAM_KEY_REPORT_PID, -99);                        
                 final Bundle extraData = intent.getBundleExtra(LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA);
                 
                 try {
					new LogTicketProxy().asyncGetTicket(new LogTicketProxyCallback() {
							@Override
							public void onTicketCallback(RunEnvType envType, GetTicketResult result) {
								String appName = reqAppPkg;
								if (appName == null) {
									appName = packageName;
								}
								String ticket = "";
								int retCode = result.getErrorCode();
								if (result.getRsp() != null) {
									retCode = result.getRet();
									if (result.getRsp().getSTicket() != null) {
										ticket = result.getRsp().getSTicket();
									}
								}
								int envFlg = 0;
								if (envType != RunEnvType.IDC) {
									envFlg = 1;
								}

								String action = appName + ACTION_WUP_LOGSDK_REPORT_LOG_INFO;
								Intent intent = new Intent(action);
								intent.putExtra(LOGSDK_PARAM_KEY_REPORT_RESID, reportResId);
								intent.putExtra(LOGSDK_PARAM_KEY_REPORT_PID, reoprtPid);
								intent.putExtra(LOGSDK_PARAM_KEY_APP_TICKET, ticket);
								intent.putExtra(LOGSDK_PARAM_KEY_APP_TICKET_RSPCODE, retCode);
								
								// 请求环境
								intent.putExtra(LOGSDK_PARAM_KEY_ENV_FLG, envFlg);
								// 附加数据
								if (extraData != null) {
									intent.putExtra(LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA, extraData);
								}

								// app相关信息
								intent.putExtra("app_guid", GuidProxy.get().getGuidBytes());
								intent.putExtra("app_qua", BaseInfoManager.get().getQua());
								intent.putExtra("app_romId", 0);
								intent.putExtra("app_lc", BaseInfoManager.get().getLC());
								intent.putExtra("app_pkgName", packageName);

								ContextHolder.getApplicationContextForSure().sendBroadcast(intent);
							}

						}, reqTimeoutMs, null);
				} catch (WupException e) {
					QRomLog.e(TAG, e.getMessage(), e);
				}

			}
		}
	}
	
}
