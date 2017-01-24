package qrom.component.wup.runInfo;

import qrom.component.wup.QRomComponentWupManager;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;

import TRom.log.GetTicketRsp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.qq.jce.wup.UniPacket;

/**
 * wup sdk 内部发送获取wup请求使用的对象
 *   -- 除sdk内部逻辑使用外，其他地方勿使用
 * @author sukeyli
 *
 */
public class QRomSdkInnerWupMananger extends QRomComponentWupManager {

    /**yyy 远程请求模块 -- notify service */
    public static final int MODEL_TYPE_WUP = QWupSdkConstants.REMOTE_MODEL_TYPE_WUP;
    public final int MODEL_TYPE_LOG_SDK = -2000;
    public final int OPER_LOG_GET_TICKET = -2001;
    
	public QRomSdkInnerWupMananger() {
	    TAG = "QRomSdkInnerWupMananger";
	}
	
	public int sendWupRequest(int operType, UniPacket reqPacket) {
		return requestWupNoRetry(MODEL_TYPE_WUP, operType, reqPacket);
	}
	
	public int sendWupRequest(int operType, UniPacket reqPacket, QRomWupReqExtraData reqExtraData) {
	    return requestWupNoRetry(MODEL_TYPE_WUP, operType, reqPacket, reqExtraData, 0);
	}
	
	public int sendLogSdkGetTicketRequest(String reqApp, int resId, int timeout, int reoprtPid, Bundle extra) {
	    UniPacket reqPacket = WupAppProtocolBuilder.creatLogSdkGetTicketReqData(getRomBaseInfo());
	    QRomWupReqExtraData reqExtraData = new QRomWupReqExtraData();
	    reqExtraData.extraStr = reqApp;
	    reqExtraData.addWupReqExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_RESID, resId);
	    reqExtraData.addWupReqExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_PID, reoprtPid);	    
	    reqExtraData.addWupReqExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA, extra);
	    int envFlg = 0;
	    if (QRomWupImplEngine.getInstance().getWupRunTimeManager().isWupRunTest()) {  // 测试环境
	        envFlg = 1;
	    }
	    reqExtraData.addWupReqExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_ENV_FLG, envFlg);
	    
	    // 日志ticket获取指定包名为“report”，不使用当前app的包名
	    return requestAsymEncryptWup(MODEL_TYPE_LOG_SDK, OPER_LOG_GET_TICKET, reqPacket, reqExtraData, timeout, "report");	    
	}

	/**
	 * 处理log sdk 发送的请求sdk
	 * @param operType
	 * @param response
	 * @param wupReqExtraData
	 */
	private void onProcessRspForLogSdk(int reqId, int operType, byte[] response, QRomWupReqExtraData wupReqExtraData) {
	    Context context = QRomWupImplEngine.getInstance().getContext();
	    if (context == null) {
	        QWupLog.w(TAG, "onProcessRspForLogSdk -> context null");
	        return;
	    }
	    if (operType == OPER_LOG_GET_TICKET) {  // 获取ticket
            GetTicketRsp getTicketRsp = new GetTicketRsp();
            getTicketRsp = (GetTicketRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(
                    response, "rsp", getTicketRsp);
            String ticket = null;
            String appName = null;
            Bundle bundle = null;
            
            int resFlg = -100;
            int resCode = -100;
            int reportPid = -100;
            int envFlg = -1;
            if (getTicketRsp != null) {
                ticket = getTicketRsp.sTicket;    
                resCode = QRomWupDataBuilder.getuniPacketResultV3(response);
            }
            
            if (wupReqExtraData != null) {
                
                appName = wupReqExtraData.extraStr;
                Integer resId = (Integer) wupReqExtraData.getWupExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_RESID);               
                if (resId != null) {
                    resFlg = resId;
                }
                Integer temp =  (Integer) wupReqExtraData.getWupExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_PID);
                if (temp != null) {                    
                    reportPid = temp; 
                }
                // 请求环境
                temp = (Integer) wupReqExtraData.getWupExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_ENV_FLG); 
                if (temp != null) {
                    envFlg = temp;
                }
                bundle = (Bundle) wupReqExtraData.getWupExtraData(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA);
            }
            
            if (appName == null) {
                appName = QRomWupBuildInfo.getAppPackageName();
            }
            String action = appName+QWupSdkConstants.ACTION_WUP_LOGSDK_REPORT_LOG_INFO;
//            QWupLog.i(TAG, "onProcessRspForLogSdk-> log ticket rsp action =  " + action);
            QWupLog.d(TAG, "onProcessRspForLogSdk -> reqId: " + reqId + ", log resId : " + resFlg 
                    + ", ticket: " + ticket + ", req app: " + appName);
            // 将ticket 发送给对应的app
            Intent intent = new Intent(action);
            intent.putExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_RESID, resFlg);
            intent.putExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_PID, reportPid);
            intent.putExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_APP_TICKET, ticket == null ? "" : ticket);
            intent.putExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_APP_TICKET_RSPCODE, resCode);
            // 请求环境
            intent.putExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_ENV_FLG, envFlg);
            // 附加数据
            if (bundle != null) {
                intent.putExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA, bundle);
            }
            // app相关信息
            intent.putExtra("app_guid", getGUIDBytes());
            intent.putExtra("app_qua", QRomWupImplEngine.getInstance().getAppQua());
            intent.putExtra("app_romId", QRomWupImplEngine.getInstance().getRomId());
            intent.putExtra("app_lc", QRomWupImplEngine.getInstance().getAppLC());
            intent.putExtra("app_pkgName", appName);
            context.sendBroadcast(intent);
        } else {
            QWupLog.w(TAG, "onProcessRspForLogSdk -> not process operType : " + operType);
        }
	}
	
	@Override
	public void onReceiveAllData(int fromModelType, int reqId, int operType,
			QRomWupReqExtraData wupReqExtraData,
			QRomWupRspExtraData wupRspExtraData, String serviceName,
			byte[] response) {
		

        if (fromModelType == MODEL_TYPE_WUP) {  // 内部用标记量
            QRomWupImplEngine.getInstance().getWupRunTimeManager()
                    .onReceiveAllData(fromModelType, reqId, operType, 
            				wupReqExtraData, wupRspExtraData, serviceName, response);
            
            return;
        } else if (fromModelType == MODEL_TYPE_LOG_SDK) {  // log sdk请求 
            onProcessRspForLogSdk(reqId, operType, response, wupReqExtraData);
        }		
	}

	@Override
	public void onReceiveError(int fromModelType, int reqId, int operType,
			QRomWupReqExtraData wupReqExtraData,
			QRomWupRspExtraData wupRspExtraData, String serviceName,
			int errorCode, String description) {
		
        if (fromModelType == MODEL_TYPE_WUP) {  // 内部用标记量
        	QRomWupImplEngine.getInstance().getWupRunTimeManager().onReceiveError(
        			fromModelType, reqId, operType, wupReqExtraData, wupRspExtraData,
        			serviceName, errorCode, description);
            return;
        } else if (fromModelType == MODEL_TYPE_LOG_SDK) {  // log sdk请求 
            
        }
	}

	@Override
	public void onGuidChanged(byte[] vGuid) {
		
	}

}
