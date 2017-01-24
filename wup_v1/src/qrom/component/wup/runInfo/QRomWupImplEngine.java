package qrom.component.wup.runInfo;

import java.util.ArrayList;
import java.util.List;

import qrom.component.wup.QRomQuaFactory;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.build.QWupUriFactory;
import qrom.component.wup.net.QubeWupNetEngine;
import qrom.component.wup.runInfo.processer.QApp2RomInfoProcesser;
import qrom.component.wup.stat.QRomWupStatEngine;
import qrom.component.wup.sync.security.AsymCipherManager;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.PhoneStatUtils;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.BASEINFO_ERR_CODE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomWupEtcInfo;
import qrom.component.wup.wupData.QRomWupInfo;
import TRom.RomBaseInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;


public class QRomWupImplEngine implements Callback {
	
	 public static final String TAG = "====QRomWupImplEngine";
    
    private static QRomWupImplEngine mInstance; 
    
    /** 请求id 从100开始自增*/
    protected static int mRequestId = 100;    
    /** 请求出错id */
    protected static int mRequestErrId = -100;    
    
    private Context mContext;
    
    /** 屏幕是否点亮 */
    private boolean mScreenOn = true;
    
    /** 屏幕状态及网络监听 */
    private WupBroadcastReceiver mReceiver = null;
    
    private QRomWupTimeoutManager mTimeOutManager;
    
    private QRomWupRunTimeManager mWupRunTimeManager;
    
    private QRomSdkInnerWupMananger mSdkInnerWupMananger;
    
    private AsymCipherManager mAsymCipherManager;
    
    private List<QRomWupManagerImpl> mWupCallbacks = new ArrayList<QRomWupManagerImpl>(3); 
    
    /** 运行环境配置信息 */
    private static QRomWupEtcInfo mWupEtcInfo;
    
//    private RomBaseInfo mBaseInfo = new RomBaseInfo(); // 废弃底层缓存, 不能做成单实例
    
    /** 自动配置获取的qua */
    private String QUA_BUILD = null;
    /** 自动配置获取的LC */
    private String LC_BUILD = null;
    
    private boolean mIsInit = false;
    
    /** 工作线程 */
    private Handler mTimeoutHandler = null;
    
    /**   网络改变广播 */
    private final int MSG_WORK_NETCHANGED = 12;
    
    private long mRegistTime = -1;
    
    private String mQimei;
    
    private QRomWupImplEngine() {
        
        mTimeOutManager = new QRomWupTimeoutManager();
        mWupRunTimeManager = new QRomWupRunTimeManager();        
    }
    
    public static QRomWupImplEngine getInstance() {
        if (mInstance == null) {
            mInstance = new QRomWupImplEngine();            
        }
        
        return mInstance;
    }
    
    /**
     * 初始化基本运行模式
     * @param context
     */
    public void initBase(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("wup startup -> context is null");
        }
        
        if (mContext == null) {
            mContext = context.getApplicationContext();
            if (mContext == null) {  // framework中获取的ApplicationContext 为null，这里便直接使用对应的context
                mContext = context;
            }
            ApnStatInfo.init(mContext);
            // 初始化当前rom相关信息
            QRomWupBuildInfo.initWupHostInfo(mContext);
            QWupUriFactory.init(mContext);
            // 因统计需要，默认先给一个imei（灯塔在获取不到qimei是，默认给imei）
            // 统一在getRomBaseInfo里面处理
//            if (QWupStringUtil.isEmpty(mQimei)) {
//                mBaseInfo.sQIMEI = PhoneStatUtils.getImei(mContext);
//            }
//            if (QWupStringUtil.isEmpty(mBaseInfo.sQIMEI)) {
//                mBaseInfo.sQIMEI = BASEINFO_ERR_CODE.QIME_INIT_EMPTY_CODE;
//            }
            mTimeOutManager.startUp();
        }
    }
    
    public synchronized void startUp(Context context, int startMode) {
        QWupLog.trace(TAG, "startUp -> start");
        initBase(context);
    	QWupLog.trace(TAG, "startUp -> initBase end");
    	QWupLog.traceSdkI("startUp -> initBase end");
    	mIsInit = true;
    	// 初始化wup运行环境
    	mWupRunTimeManager.startUp(mContext, startMode);
        
    	// 初始化相关资源
        registBroadcast(mContext);
        QWupLog.trace(TAG, "startUp -> end");
    } 
    
    /**
     * 注册相关监听
     * @param context
     */
    private void registBroadcast(Context context) {
        
        if (context == null) {
            return;
        }
        
        if (mReceiver == null) {
            mReceiver = new WupBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            
            // 普通rom模式下的wup不用注册这个不必要的广播
            if (!QRomWupBuildInfo.isRomWupApkExist(context)) {
            	filter.addAction(Intent.ACTION_SCREEN_ON);
            	filter.addAction(Intent.ACTION_SCREEN_OFF);
            }
            
            filter.addAction(QRomWupBuildInfo.getAppPackageName()+QWupSdkConstants.ACTION_WUP_TOOL_GET_GUID);
            filter.addAction(QRomWupBuildInfo.getAppPackageName()+QWupSdkConstants.ACTION_WUP_LOGSDK_GETTICKET_INFO);
            mRegistTime = System.currentTimeMillis();
            context.registerReceiver(mReceiver, filter);
        }      
    }
    
    public void addWupCallback(QRomWupManagerImpl wupCallback) {
    	synchronized (mWupCallbacks) {
			
    		if (wupCallback == null || mWupCallbacks.contains(wupCallback)) {
    			return;
    		}
    		mWupCallbacks.add(wupCallback);
		}
    }
    
    public void removeWupCallback(QRomWupManagerImpl wupCallback) {
    	synchronized (mWupCallbacks) {
			
    		if (wupCallback == null || !mWupCallbacks.contains(wupCallback)) {
    			return;
    		}
    		mWupCallbacks.remove(wupCallback);
		}
    }
    
    public boolean isAllWupCallbackRlease() {
    	synchronized (mWupCallbacks) {
		
    		return mWupCallbacks.isEmpty();
		}
    }
    
    
//    public byte[] getGuidBytes() {    	
//    	return mWupRunTimeManager.getGuidBytes();
//    }

    public String getAppQua() {
        if (QWupStringUtil.isEmpty(QUA_BUILD)) {  // 只初始化一次
        	QUA_BUILD = getWupRunTimeManager().getQua();
        }
        return QUA_BUILD;
    }
    
    public String getAppLC() {

        if (QWupStringUtil.isEmpty(LC_BUILD)) {  // 只初始化一次
        	LC_BUILD = QRomQuaFactory.getLC(getContext());
        }
        return LC_BUILD;
    }
    
//    public RomBaseInfo getRomBaseInfoNoRefresh(){
//        return mBaseInfo;
//    }
    
    public RomBaseInfo getRomBaseInfo(){
//    	mBaseInfo.vGUID = mWupRunTimeManager.getGuidBytes();
//    	mBaseInfo.sQUA =getAppQua();
//    	mBaseInfo.sIMEI = PhoneStatUtils.getImei(getContext());
//    	mBaseInfo.sLC = getAppLC();
//    	mBaseInfo.iRomId = getRomId();
//    	if (QWupStringUtil.isEmpty(mBaseInfo.sQIMEI)) {  // qimei为空，用imei代替
//    	    mBaseInfo.sQIMEI = PhoneStatUtils.getImei(getContext());
//    	}
//    	if (mContext != null) {
//    	    mBaseInfo.sPackName = getContext().getPackageName();
//    	}
    	
    	RomBaseInfo romBaseInfo = new RomBaseInfo();
    	romBaseInfo.setVGUID(mWupRunTimeManager.getGuidBytes());
    	romBaseInfo.setSQUA(getAppQua());
    	romBaseInfo.setSIMEI(PhoneStatUtils.getImei(getContext()));
    	romBaseInfo.setSLC(getAppLC());
    	romBaseInfo.setIRomId(getRomId());
    	if (QWupStringUtil.isEmpty(mQimei)) {
    		romBaseInfo.setSQIMEI(romBaseInfo.getSIMEI());
    		if (QWupStringUtil.isEmpty(romBaseInfo.getSQIMEI())) {
    			romBaseInfo.setSQIMEI(BASEINFO_ERR_CODE.QIME_INIT_EMPTY_CODE);
    		}
    	} else {
    		romBaseInfo.setSQIMEI(mQimei);
    	}
    	romBaseInfo.setSPackName(getContext().getPackageName());
    	
    	return romBaseInfo;
    }
//    
    public void setRomId(long romId) {
//        mBaseInfo.setIRomId(romId);   
        mWupRunTimeManager.setRomId(romId);
    }
    
    public long getRomId() {
        return mWupRunTimeManager.getRomId();
    }
        
    public void reloadWupInfo() {
    	mWupRunTimeManager.reloadWupInfo();
    }
    
    /**
     * 获取wup配置文件环境变量
     * @return
     */
    private static QRomWupEtcInfo getWupEtcInfo() {
        if (mWupEtcInfo == null) {
            mWupEtcInfo = new QRomWupEtcInfo();
        }
        return mWupEtcInfo;
    }
    
    public int getWupEtcWupEnviFlg() {
        return getWupEtcInfo().getNetEnvFlg();
    }
    
    /**
     * 重新加载配置信息
     * @return
     */
    public boolean reLoadWupEtcInfo(Context context) {
       
        return getWupEtcInfo().load(context);
    }    
    
    /**
     * 配置文件是否是设置wup测试环境
     * @return
     */
    public boolean isWupEctConfigInTest() {
    	if (mContext == null) {
            return false;
        }
    	
        return getWupEtcInfo().isEnvConfigTest(mContext);
    }
    
    /**
     * 获取wup请求id
     * 
     * @return
     */
    public int getRequestId() {
        mRequestId++;
        return mRequestId;
    }
    
    /**
     * 获取请求失败id
     * @return
     */
    public int getRequestErrId() {
    	mRequestErrId--;
    	return mRequestErrId;
    }
    
    public QRomWupTimeoutManager getWupTimeoutMananger() {
        return mTimeOutManager;
    }
    
    public QRomWupRunTimeManager getWupRunTimeManager() {
    	return mWupRunTimeManager;
    }
    
    public synchronized QRomSdkInnerWupMananger getSdkInnerWupManger() {
    	if (mSdkInnerWupMananger == null) {
    		mSdkInnerWupMananger = new QRomSdkInnerWupMananger();
    		mSdkInnerWupMananger.startup(getContext());    	
    	}
    	return mSdkInnerWupMananger;
    }
    
    public synchronized AsymCipherManager getAsymCipherManager() {
        if (mAsymCipherManager == null) {
            mAsymCipherManager = new AsymCipherManager(getContext(), getSdkInnerWupManger());
            mAsymCipherManager.setStatisticListener(QRomWupStatEngine.getInstance());
        }
        return mAsymCipherManager;
    }
    
    public boolean isScreenOn() {
        return mScreenOn;
    }   
    
    /**
     * wup socket请求服务器返回错误
     * @param modelType
     * @param operType
     */
    public void onWupSocketServiceRspCodeErr(int modelType, int operType) {
    	mWupRunTimeManager.onWupSocketServiceRspCodeErr(modelType, operType);
    }
    
    /**
     * wup 请求服务器返回错误
     * @param modelType
     * @param operType
     */
    public void onWupServiceRspCodeErr(int modelType, int operType, String reqUrl) {
    	mWupRunTimeManager.onWupServiceRspCodeErr(modelType, operType, reqUrl);
    }
    
    public Context getContext() {
    	return mContext;
    }
    
    public void onWupBaseDataChanged(int dataType) {
    	
    	synchronized (mWupCallbacks) {
			
    		if (mWupCallbacks.isEmpty()) {
    			return;
    		}
    		
    		int cnt = mWupCallbacks.size();
    		QRomWupManagerImpl wupCallback = null;
    		for (int i = 0; i < cnt; i++) {  // 通知所有对象数据改变
    			wupCallback = mWupCallbacks.get(i);
    			if (wupCallback == null) {
    				continue;
    			}
    			// 通知guid改变
    			wupCallback.onWupBaseDataChanged(dataType);
    		}
		}
    	
    }
    
    /**
     * 取消wup注册
     */
    private void unregisterWupReceiver() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    /**
     * 若所有wup完成工作，则释放所有资源
     * @param wupCallback
     */
    public synchronized void release(QRomWupManagerImpl wupCallback) {
    	
    	removeWupCallback(wupCallback);
    	
    	if (!isAllWupCallbackRlease()) { // wup callback 未全部release
    		return;
    	}
    	// 取消注册
    	unregisterWupReceiver();
    	if (mTimeoutHandler != null) {
    	    mTimeoutHandler.removeMessages(MSG_WORK_NETCHANGED);
    	    mTimeoutHandler = null;
    	}
    	mIsInit = false;
    	mTimeOutManager.release();
    	mWupRunTimeManager.release();
    	mWupEtcInfo = null;

    	if (QubeWupNetEngine.getInstance() != null) { 
    		QubeWupNetEngine.getInstance().shutdownThreadPool();
    	}
    	 mInstance = null;
    	 mWupRunTimeManager = null;
    	 mTimeOutManager = null;
    	 mAsymCipherManager = null;
    	 mContext = null;
    }
    
    public boolean isWupEngineStartup() {
        return mIsInit;
    }
    
    public static byte[] getGuidForLoadFile(Context context0) {
        Context context = context0.getApplicationContext();
        if (context == null) {
            context = context0;
        }
        if (mInstance != null && mInstance.isWupEngineStartup() 
                && mInstance.getWupRunTimeManager() != null) {  // wup 模块已启动
            return mInstance.getWupRunTimeManager().getGuidBytes();
        }
        
        QRomWupBuildInfo.initWupHostInfo(context);
        QWupUriFactory.init(context);
        
        QRomWupInfo qRomWupInfo = null;
        if (QRomWupBuildInfo.isWupForRom(context)) { // 当前是rom 
            qRomWupInfo = new QRomWupInfo(
                    QWupFileUtil.FILE_USER_WUP_INFO_ROM, QWupFileUtil.SD_USER_WUP_INFO_ROM);
            qRomWupInfo.load(context);
            return qRomWupInfo.getGuidBytes();
        }
        
        if (QRomWupBuildInfo.isRomWupApkExist(context)) {  // 有rom apk，走rom guid模式
            
            QApp2RomInfoProcesser app2RomInfoProcesser = new QApp2RomInfoProcesser();
            byte[] guid = app2RomInfoProcesser.getGuidFromCursor(context);
            if (guid == null || guid.length == 0) {
                return QRomWupInfo.DEFAULT_GUID_BYTES;
            }
            return guid;
        }
        
        // app独立模式
        qRomWupInfo = new QRomWupInfo(
                QWupFileUtil.FILE_USER_WUP_INFO_APP, QWupFileUtil.SD_USER_WUP_INFO_APP);
        qRomWupInfo.load(context);
        return qRomWupInfo.getGuidBytes();         
    }
    
    private void sendWorkMsg(int what, long delay) {
        if (mTimeoutHandler == null) {
            
            Looper looper =  QRomWupImplEngine.getInstance()
                    .getWupTimeoutMananger().getTimeoutLooper();
            if (looper == null) {
                return;
            }
            mTimeoutHandler = new Handler(looper, this);
        }
        Message message = mTimeoutHandler.obtainMessage(what);
        mTimeoutHandler.sendMessageDelayed(message, delay);
    }
    
    private void removeWorkMsg(int what) {
        if (mTimeoutHandler == null) {
            return;
        }
        mTimeoutHandler.removeMessages(what);
    }
    
    /**
     * 获取进程间启动的delay时间
     *   -- 延时尽量控制在1s左右
     * @return
     */
    public int getProcessReqDelay() {
        int time = (int)(System.currentTimeMillis() % 100);
        int proc = android.os.Process.myPid() % 100;
        
        int delay = time * 100 + proc * 10;
        
        if (delay > 1000) {  // 延时大于1s
            delay = delay % 1000;
        }
        if (delay == 0) {
            delay = (time + proc) * 5;
        }
        QWupLog.trace(TAG, "getProcessReqDelay ->   time = " + time + ", proc = " + proc + ", delay = " + delay);
        return delay;
    }
    
    public void updateQimei(String qimei) {
    	this.mQimei = qimei;
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        
        int what = msg.what;
        try {
            switch (what) {
            case MSG_WORK_NETCHANGED:  // 网络改变
                if (mWupRunTimeManager != null) {
                    mWupRunTimeManager.onConnectivityChanged(mContext);
                }
                break;

            default:
                break;
            }
        } catch (Exception e) {
           QWupLog.w(TAG, e);
        }
        
        return false;
    }
    
    class WupBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = "";
            if (intent != null) {
                action = intent.getAction();
            }
            QWupLog.d(TAG, "onReceive -> action = " + action);
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {  // 网络改变
                removeWorkMsg(MSG_WORK_NETCHANGED);
                QWupLog.d(TAG, "onReceive -> CONNECTIVITY_ACTION cur time = " + System.currentTimeMillis());
               
                int delay = getProcessReqDelay();
                
                if (System.currentTimeMillis() - mRegistTime < 2000) {  // 第一次注册收到的广播和启动时间解接近，这里延时处理下
                    delay = delay + 5000;
                }                
                sendWorkMsg(MSG_WORK_NETCHANGED, delay);
                QWupLog.traceSdkW("onReceive -> getProcessReqDelay = " + delay);
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {  // 黑屏
                mScreenOn = true;
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {  // 亮屏
                mScreenOn = false;
            } else {
                if (getContext() == null) {
                    QWupLog.w(TAG, "onReceive -> getContext null, cancel");
                    return;
                }
                String toolGetGuidAction = QRomWupBuildInfo.getAppPackageName()+QWupSdkConstants.ACTION_WUP_TOOL_GET_GUID;
                if (toolGetGuidAction.equals(action)) {  // 查询guid的请求
                    QWupLog.i(TAG, "onReceive -> send show guid broadcast");
                    Intent rspIntent = new Intent(QWupSdkConstants.ACTION_WUP_TOOL_SHOW_GUID);
                    rspIntent.putExtra("app_pkgName", QRomWupBuildInfo.getAppPackageName());
                    rspIntent.putExtra("app_guid", getWupRunTimeManager().getGuidBytes());
                    rspIntent.putExtra("app_test", getWupRunTimeManager().isWupRunTest());
                    rspIntent.putExtra("app_qua", getWupRunTimeManager().getQua());
                    rspIntent.putExtra("app_lc", getAppLC());
                    rspIntent.putExtra("app_romId", getWupRunTimeManager().getRomId());
                    getContext().sendBroadcast(rspIntent);
                } else {
                    String logSdkAction = QRomWupBuildInfo.getAppPackageName()+QWupSdkConstants.ACTION_WUP_LOGSDK_GETTICKET_INFO;
                    if (logSdkAction.equals(action)) {  // sdk 发送获取ticket请求
                        // 请求的包名
                        String reqAppPkg = intent.getStringExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_APP_PKGNAME);
                        // 请求超时时间
                        int reqTimeout = intent.getIntExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_TICKET_TIMEOUT, 30000);
                        // 请求的id
                        int reportResId = intent.getIntExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_RESID, -99);
                        int reoprtPid =  intent.getIntExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_PID, -99);                        
                        Bundle extraData = intent.getBundleExtra(QWupSdkConstants.LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA);
                        
                        // 发送ticket请求 
                        int reqId = getSdkInnerWupManger().sendLogSdkGetTicketRequest(reqAppPkg, reportResId, reqTimeout, reoprtPid, extraData);
                        QWupLog.traceSdkW("onReceive -> sendLogSdkGetTicketRequest reqId= " + reqId);
                    }
                }
            }
        }
        
    };
    
}
