package qrom.component.wup.runInfo.processer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.IPLIST_RSP_CODE;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.QRomWupEnvironment;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.runInfo.QRomSdkInnerWupMananger;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.runInfo.QRomWupSharedpreferences;
import qrom.component.wup.runInfo.WupAppProtocolBuilder;
import qrom.component.wup.stat.QRomWupStatEngine;
import qrom.component.wup.support.ISwitchListener;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.PhoneStatUtils;
import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.BASEINFO_ERR_CODE;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_REQ_TYPE;
import qrom.component.wup.utils.QWupSdkConstants.LOGIN_REQ_TYPE;
import qrom.component.wup.utils.QWupSdkConstants.LOGIN_RSP_CODE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomIplistData;
import qrom.component.wup.wupData.QRomWupInfo;
import TRom.EIPType;
import TRom.ELOGINRET;
import TRom.ENETTYPE;
import TRom.IPListRsp;
import TRom.JoinIPInfo;
import TRom.LoginRsp;
import TRom.RomBaseInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.SparseArray;

import com.qq.jce.wup.UniPacket;

/**
 * 主动拉取wup 信息处理对象
 *    -- 主动拉取guid
 *    -- 主动拉取iplist
 *    
 * @author sukeyli
 *
 */
public abstract class QWupActivedInfoProcesser extends QWupBaseInfoProcesser implements ISwitchListener {

    /** wup确认SD卡guid状态 延时 */
    private static final int MSG_WUP_CHECK_SDGUID_TIMEOUT = 123;
    /** wup -- 延时初始化 */
    private static final int MSG_WUP_PROCESS_INIT_DELAY = 200;
    /** wup确认 guid状态 发送请求 */
    private static final int MSG_WUP_CHECK_SEND_GUID = 201;
    /** wup确认 -- 发送请求 IPLIST*/
    private static final int MSG_WUP_CHECK_SEND_IPLIST = 211;
    
    /** wup确认 -- broadcast获取wup数据*/
    protected static final int MSG_WUP_ROMSYS_BROAD_REQ_WUPDATE = 401;
    
   
    /** 
     * wup的operType。为了与使用wup的业务不冲突，这里取值从1000开始。
      */
    /** 拉取guid */
    private static final int WUP_OPERTYPE_GET_GUID = 1000;
    /** 拉取iplist */
    private static final int WUP_OPERTYPE_GET_IPLIST = 1001;
    
    
    // 是否正在走login服务
    private boolean mIsLogining = false;
    // 是否正在更新iplist
    private boolean mIsProxyUpdating = false;
	
    private int mCheckSdGuidDelayCnt = 0;

    // 
    private long mLastNetChangReqTime = System.currentTimeMillis();    
    
    /** wup基本信息，如guid/iplist */
    protected QRomWupInfo mQubeWupInfo;
	
    private int mLoginCnt = 0;
    
    private String mProcessName = null;
    
    private long mGuidTime = -1;
    
    private BroadcastReceiver mIpListUpdateReceivor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null 
				&& intent.getAction().equalsIgnoreCase(QWupSdkConstants.ACTION_WUP_SDK_UPDATE_IPLIST)) {
				QRomLog.d(TAG, "===received iplist update push message===");
				sendIpListUpdateByCheck(true, 0, true);
			}
		}
    };
    
	public QWupActivedInfoProcesser(String wupCacheName, String wupSdCacheName) {
		mQubeWupInfo = new QRomWupInfo(wupCacheName, wupSdCacheName);
		TAG = "QWupInfoActivedProcesser";
	}
	
	public void startUp(Context context) {
		super.startUp(context);
		// 延时初始化
		sendMsg(MSG_WUP_PROCESS_INIT_DELAY, 150);
		registerIpListUpdateBroadCast();
		
		QRomWupEnvironment.getInstance(getContext()).registerSwitchListener(this);
	}
	
    public byte[] getGUIDBytes() {
        
        return mQubeWupInfo.getGuidBytes();
    }
    
    public String getGUIDStr() {
        return mQubeWupInfo.getGuidStr();
    }
    
    public List<String> getCurApnProxyList() {                

        QRomIplistData iplistData = getCurApnProxyListData();
        
        return iplistData == null ? null : iplistData.getIplistInfo();
    }
    
    
    public List<String> getCurSocketProxyList() {
        return  mQubeWupInfo.getSocketProxyByType(ApnStatInfo.getCurApnProxyIndex());
    }
    
	/**
	 * 获取当前所有接入点的wup代理地址
	 * @return
	 */
	public SparseArray<List<String>> getAllWupProxyInfos() {  
		
		return mQubeWupInfo.getAllProxyInfos();							
	}
	/**
	 * 获取当前所有接入点的wup socket 代理地址
	 * @return
	 */
	public SparseArray<List<String>> getAllWupSocketProxyInfos() {  
		
		return mQubeWupInfo.getAllSocketProxyInfos();				
	}
	
	@Override
	public SparseArray<QRomIplistData> getAllWupProxyIplistDatas() {
	    return mQubeWupInfo.getAllWupProxyIplistDatas();
	}
	
	@Override
	public SparseArray<QRomIplistData> getAllWupSocketProxyIplistDatas() {
	    return mQubeWupInfo.getAllWupSocketProxyIplistDatas();
	}
	
	
	@Override
	public Map<String, QRomIplistData> getAllWupWifiProxyIplistDatas() {
	    return mQubeWupInfo.getAllWupWifiProxyInfos();
	}
	
	@Override
	public Map<String, QRomIplistData> getAllWupWifiSocketProxyIplistDatas() {
	    return mQubeWupInfo.getAllWupWifiSocketProxyInfos();
	}
	
	private boolean sendMsgGetGuid(long delay, int reqType) {
	    return sendMsg(MSG_WUP_CHECK_SEND_GUID, reqType, delay);
	}
	
    /**
     * 主动发送获取guid命令
     *   -- guid不合法时才发起请求
     * @param userInfo
     */
    public int requestGuid() {
        if (isGuidValidate(getGUIDBytes())) {  // 不获取guid（guid合法，或不需要主动拉取guid）
//            QWupLog.i(TAG, "requestGuid ->guid ok,  no send wup");
            QWupLog.traceGuidWarn("requestGuid ->guid ok,  no send wup");
            return LOGIN_RSP_CODE.LOGIN_CANCEL_GUID_OK;
        }    
        int reqId = LOGIN_RSP_CODE.LOGIN_REQ_FAIL;
        if (sendMsgGetGuid(100, LOGIN_REQ_TYPE.LOGIN_REQ_GETGUID)) {
            reqId = LOGIN_RSP_CODE.LOGIN_REQ_SENDING;
            QWupLog.traceGuidInfo("requestGuid -> LOGIN_REQ_SENDING");
        }
//        return sendGuidRequest();
        return reqId;
    }
    
    @Override
    public int requestIpList(int reqType) {
        // 主动请求iplist，check缓存超时时间
        QWupLog.trace(TAG, "requestIpList->reqType = " + reqType);
        // 是否忽略缓存时间
        boolean isIgnorCache = false;
        // wup请求频率
        long reqFreq = QWupSdkConstants.WUP_IPLIST_REQ_TIMEOUT;
        
        switch (reqType) {
        case IPLIST_REQ_TYPE.IPLIST_REQ_NORMAL: // 普通模式iplist请求
            isIgnorCache = false;
            break;
        case  IPLIST_REQ_TYPE.IPLIST_REQ_IGNOR_CACHETIMEOUT: // 忽略缓存时间
            isIgnorCache = true;
            break;
        case IPLIST_REQ_TYPE.IPLIST_REQ_ROM_UPDATE: // 通知rom更新,
            isIgnorCache = true;
            //  请求超时时间最小时间
            reqFreq = QWupSdkConstants.WUP_IPLIST_REQ2ROM_TIMEOUT;
            break;
        default:
            break;
        }
        
        return sendIpListUpdateByCheck(isIgnorCache, reqFreq, false);
    }
    
    public boolean refreshInfos() {
        mQubeWupInfo.reload(getContext());
        mGuidTime = QRomWupSharedpreferences.getGuidTime(getContext());
        return true;
    }
    
    @Override
    public int doLogin() {
        
        if (!isLoginTimeout(QWupSdkConstants.WUP_INNER_REQ_TIMEOUT)) {  // 时间超时 /异常
            QWupLog.trace(TAG, "doLogin -> time is not ok");
            return LOGIN_RSP_CODE.LOGIN_CANCEL_FREQ;            
        }
        
        int reqId = LOGIN_RSP_CODE.LOGIN_REQ_FAIL;
        if (sendMsgGetGuid(100, LOGIN_REQ_TYPE.LOGIN_REQ_DOLOGIN)) {
            reqId = LOGIN_RSP_CODE.LOGIN_REQ_SENDING;
        }
//        int reqId = sendGuidRequest();
//        QWupLog.trace(TAG, "doLogin - > send login reqId = " + reqId);
        QWupLog.traceGuidInfo("doLogin - > send login reqId = " + reqId);
//        if (reqId > 0) {  // 请求发送成功
//            QRomWupSharedpreferences.setLastLoginTime(getContext(), System.currentTimeMillis());
//        }       
        
        return reqId;
    }
    
    private boolean isLoginTimeout(long timeout) {
        if (timeout <= 0) {
            timeout =QWupSdkConstants.WUP_INNER_REQ_TIMEOUT;
        }
        long lastReqLoginTime = QRomWupSharedpreferences.getLastLoginTime(getContext());
        long subTime =  System.currentTimeMillis() - lastReqLoginTime;
        if (subTime < timeout 
                && subTime > 0) {  // 时间超时 /异常
            return false;
        }
        return true;
    }
    
    /**
     * 网络改变，需要切换 代理地址 -- 有监听网络状态模块调用（如APN广播）
     * @param newApn
     * @param oldApn
     * @return
     */
    public synchronized boolean onConnectivityChanged(int newApn, int oldApn) {

        super.onConnectivityChanged(newApn, oldApn);

        // wifi下bssid是否改变
//        boolean isForceUpdate = isWifiBSSIDInfoChanged(getContext());

//        QWupLog.i(TAG, "onConnectivityChanged: isWifiBSSIDInfoChanged = " + isForceUpdate);
        if (isNetStateChangeOk()) {  // 网络改变时间ok
            // 更新网络变化时间
//            QWupLog.w(TAG, "onConnectivityChanged: net is connected");
            
            // 效验guid是否合法
            checkGuid();
            int reqId = sendIpListUpdateByCheck(false, 0, false);
            QWupLog.trace(TAG, "onConnectivityChanged-> newApn = " + newApn 
                    + ", old apn = " + oldApn + ", sendIpListUpdateByCheck: reqId = " + reqId);

        } else {// 网络改变未到指定时间未
//            QWupLog.w(TAG, "onConnectivityChanged: net change time is not ok");
        }
        
        return false;
    }
    
    @Override
    public void onCurApnIpListAllErr(List<String>proxyList) {

        // ip失败后，不清除缓存
        //  跳过缓存超时限制，发起请求
        sendIpListUpdateByCheck(true, 0, false);
    }
    
    @Override
    public void onCurApnSocketIpListAllErr(List<String> proxyList) {
        
        // ip失败后，不清除缓存
        //  跳过缓存超时限制，发起请求
        sendIpListUpdateByCheck(true, 0, false);
    }
     
    @Override
    public QRomIplistData getCurApnProxyListData() {
        QRomIplistData iplistData = null;
        int proxyIndex = ApnStatInfo.getCurApnProxyIndex();
        if (proxyIndex == ApnStatInfo.PROXY_LIST_WIFI) {  // wifi下优先获取对应bssid缓存
            iplistData = mQubeWupInfo.getProxyIpListDataByBssid(ApnStatInfo.getWifiBSSID(getContext()));
        }
        if (iplistData == null || iplistData.isEmpty()) {  // 对应bssid下未找到缓存ip，则使用上次拉取的默认wifi索引下的iplist
            iplistData = mQubeWupInfo.getProxyIpListDataByType(proxyIndex);
        }
        QWupLog.trace(TAG, "getCurApnProxyListData-> proxyIndex = " + proxyIndex 
                + ", apnName =" + ApnStatInfo.getApnName());
 //       // 测试用代码
//        ArrayList<String> iplist = new ArrayList<String>();
//        iplist.add("1.1.1.1:8888");
//        iplist.add("2.2.2.2:8888");
//        iplist.add("3.3.3.3:8888");
//        iplistData = new QRomIplistData("test", iplist, System.currentTimeMillis(),"0.0.0.0");
        return iplistData;
    }
    
    @Override
    public QRomIplistData getCurApnSocketListData() {
        
        QRomIplistData iplistData = null;
        int proxyIndex = ApnStatInfo.getCurApnProxyIndex();
        if (proxyIndex == ApnStatInfo.PROXY_LIST_WIFI) {  // wifi下优先获取对应bssid缓存
            iplistData = mQubeWupInfo.getWupSocketIpListDataByBssid(ApnStatInfo.getWifiBSSID(getContext()));
        }
        if (iplistData == null || iplistData.isEmpty()) {  // 对应bssid下未找到缓存ip，则使用上次拉取的默认wifi索引下的iplist
            iplistData = mQubeWupInfo.getWupSocketIpListDataByType(proxyIndex);
        }
 //       // 测试用代码
//        ArrayList<String> iplist = new ArrayList<String>();
//        iplist.add("1.1.1.1:8888");
//        iplist.add("2.2.2.2:8888");
//        iplist.add("3.3.3.3:8888");
//        iplistData = new QRomIplistData("test", iplist, System.currentTimeMillis(),"0.0.0.0");
        return iplistData;
    }
    
    private boolean isNetStateChangeOk() {

        if (!ApnStatInfo.isNetConnected()) {
            return false;
        }
        long curTime = System.currentTimeMillis();
        // 上次网络改变的时间，避免网络状态连续改变，且无缓存是，发送多次网络请求
        long subTime = curTime - mLastNetChangReqTime;
        // 网络改变时间间隔是否ok
        boolean isNetChangeOk = (subTime > QWupSdkConstants.NET_CHANGE_INTERVAL_TIME ) 
                 || (subTime < 0 && subTime < -QWupSdkConstants.NET_CHANGE_INTERVAL_TIME);
        
        if (isNetChangeOk) {  // 网络改变时间ok
            // 更新网络变化时间
            mLastNetChangReqTime = System.currentTimeMillis();
        }
        QWupLog.d(TAG, "isNetStateChangeOk -> " + isNetChangeOk);
        return isNetChangeOk;
    }

    /**
     * 
     *  验证是否发送更新iplist请求
     * @param isForceUpdate   是否强制更新 （true:忽略缓存超时时间，仅有发送频率check，黑屏等状态检测）
     * @return true : iplist请求发送，false：请求未发送
     * 
     */
    private int sendIpListUpdateByCheck(boolean isIgnorCache, long freqCheckTime, boolean bForceAll) {
        if (QRomWupImplEngine.getInstance().getWupRunTimeManager().isWupRunTest()) {  // 测试环境，不拉去iplist
            QWupLog.w(TAG, "sendIpListUpdateByCheck -> test mode， do not get IP list");           
            return IPLIST_RSP_CODE.IPLIST_CANCEL_TEST_ENV;
        }
        
        if (!bForceAll && !QRomWupImplEngine.getInstance().isScreenOn()) {
            QWupLog.trace(TAG, "sendIpListUpdateByCheck-> screen off ! cancel " );
            return IPLIST_RSP_CODE.IPLIST_CANCEL_SCREEN_OFF;
        }
        
        int proxyIndex = ApnStatInfo.getCurApnProxyIndex();
        if (proxyIndex < 0) {  // apn类型不对或正在发送中
            QWupLog.trace(TAG, "sendIpListUpdateByCheck -> proxyIndex iserr : " + proxyIndex);
            return IPLIST_RSP_CODE.IPLIST_CANCEL_APN_ERR;
        }
        
        if (!ApnStatInfo.isNetConnected()) {
            QWupLog.trace(TAG, "sendIpListUpdateByCheck -> net is not ok" );
            return IPLIST_RSP_CODE.IPLIST_CANCEL_NO_NET;
        }
        
        // 上次发送请求的时间
        long lastIpTime = QRomWupSharedpreferences.getLastIPListTime(getContext());
        long curTime = System.currentTimeMillis();
        // 上次拉取ip到目前的时差
        long subTime = curTime - lastIpTime;        
        if (freqCheckTime < QWupSdkConstants.WUP_IPLIST_REQ_TIMEOUT) {
            freqCheckTime = QWupSdkConstants.WUP_IPLIST_REQ_TIMEOUT;
        }
        
        // 是否请求频繁(多进程会发起多次请求), 强制更新所有表示很重要的更新
        if (!bForceAll && subTime > 0 && subTime < freqCheckTime) {  // 请求太频繁
            QWupLog.trace(TAG, "sendIpListUpdateByCheck->  req is frequently! checktime = " +freqCheckTime);
            return IPLIST_RSP_CODE.IPLIST_CANCEL_FREQ;
        } 
        // 当前接入点的iplist 信息
        QRomIplistData iplistData = getCurApnProxyListData();
        QWupLog.trace(TAG, "---  sendIpListUpdateByCheck: isForceUpdate =" + isIgnorCache);
        boolean isIpTimeOut = false;
        if (subTime < 0 ) {  // 时间有误，默认当前时间肯定比上次时间大，若subTime<0，时间设置有问题
            isIpTimeOut = true;
        } else if (subTime >= QWupSdkConstants.WUP_IPLIST_CACHE_TIMEOUT) {   // 上次发送iplist时间超时         
            isIpTimeOut = true;
        } else {  // 发送时间未超时, 判断缓存是否超时            
            long cacheTime = 0;
            if (iplistData != null) {
                cacheTime = iplistData.getIplistUpdateTime();
            }
            // 当前接入点缓存时间差
            cacheTime = curTime - cacheTime;
            if (cacheTime >= QWupSdkConstants.WUP_IPLIST_CACHE_TIMEOUT  
                    || cacheTime < -QWupSdkConstants.WUP_IPLIST_REQ_TIMEOUT) {  // 缓存超时
                QWupLog.trace(TAG, "sendIpListUpdateByCheck->  cache is timeout! ");
                isIpTimeOut = true;
            }
        }  // ~ end 时间check 完成
        
        if (isIpTimeOut || (iplistData == null || iplistData.isEmpty()) || isIgnorCache) { // 没有缓存到代理地址
            int reqId = IPLIST_RSP_CODE.IPLIST_REQ_FAIL;
            if (sendIpListMsg(bForceAll)) {
                reqId = IPLIST_RSP_CODE.IPLIST_REQ_SENDING;
            }
            
            QWupLog.trace(TAG, "sendIpListUpdateByCheck-> 数据为空/超时/强制更新iplist reqid = "+ reqId 
                    + ", isIpTimeOut = " + isIpTimeOut + ", isIgnorCache = " + isIgnorCache + ", bForceAll=" + bForceAll);
            
            if (reqId > 0 ) {  // 发送iplist请求
                return reqId;
            } 
        } else {
            QWupLog.trace(TAG, "sendIpListUpdateByCheck->  proxylist is alread cache, not timeout, cancel req");
            if (iplistData == null || iplistData.isEmpty()) {  // 数据为空
                // 重新加载基本信息
                refreshInfos();
            }
        }
        
        return IPLIST_RSP_CODE.IPLIST_CANCEL_NO_TIMEOUT;
    }
    
    /**
     * 判断对应命令字请求是否正在执行
     * @param cmdType
     * @return  false : 该命令字正在请求不需要发送， ture ： 该命令字没有请求，取消操作
     */
    private boolean isNeedSendRequest(int cmdType) {
        
        if ((mIsLogining && (cmdType == WUP_OPERTYPE_GET_GUID))
                || (mIsProxyUpdating && cmdType == WUP_OPERTYPE_GET_IPLIST) ) {
            if (cmdType == WUP_OPERTYPE_GET_GUID &&
                    isLoginTimeout(QWupSdkConstants.WUP_INNER_REQ_TIMEOUT)) {  // 正在login，但时间过长
                QWupLog.trace(TAG, "isNeedSendRequest -> is logining, but timeout");
                return true;
            }
            // 正在login,/统计/crash上报则不在报
            return false;
        } 
        
        return true;
    }
    
    /**
     * 移除sd检测guid消息
     */
    private void removeCheckSdGuidMsg() {
        removeMsg(MSG_WUP_CHECK_SDGUID_TIMEOUT);
    }    
    
    /**
     * 同步用户数据到本地缓存
     *   -- data下缓存文件不存在且guid合法,则将内存信息写入缓存
     */
    private void syncWupInfo() {

    	mQubeWupInfo.syncWupInfo(getContext());
    }
    
    /**
     * 效验guid，不合法就去后台拉取
     */
    protected void checkGuid() {
        
        QWupLog.d(TAG, "checkGuid -> 强制检查guid");           
        if (!isGuidValidate(mQubeWupInfo.getGuidBytes())) {  // guid不合法

            QWupLog.traceGuidWarn("checkGuid -> 发送获取guid命令");
//            sendGuidRequest();
            sendMsgGetGuid(QRomWupImplEngine.getInstance().getProcessReqDelay(), 
                    LOGIN_REQ_TYPE.LOGIN_REQ_GETGUID);            
        } else {  // guid合法
            QWupLog.d(TAG, "checkGuid -> 强制检查, guid合法判断是否保存当前内存信息");
            if (mGuidTime != QRomWupSharedpreferences.getGuidTime(getContext())) {  // guid获取时间有更新，重新加载guid
                refreshInfos();
            }
            syncWupInfo();
        }
    }
    
    @Override
    public boolean checkGuid(int delay) {
        if (!isGuidValidate(mQubeWupInfo.getGuidBytes())) {  // guid不合法
            QWupLog.traceGuidWarn("checkGuid delay -> 发送check guid命令");
            if (delay <= 0) {
                delay = QWupSdkConstants.WUP_INNER_REQ_MIN_INTERVAL;
            }
            
            return sendMsgGetGuid(delay, LOGIN_REQ_TYPE.LOGIN_REQ_GETGUID);
        } 
        return false;
    }
    
    public int sendGuidRequest() {
        
        // 若sd卡无法使用，延时5s后重新发起请求
        if (mCheckSdGuidDelayCnt < QWupSdkConstants.GUID_SD_STATE_CHECK_MAX_CNT
                && !QWupFileUtil.isExternalStorageAvailable()) {  // sd卡不用, 延时发送请求
            removeCheckSdGuidMsg();
            boolean res = sendMsg(MSG_WUP_CHECK_SDGUID_TIMEOUT, 
                    QWupSdkConstants.GUID_SD_STATE_CHECK_TIME_DELAY);
            mCheckSdGuidDelayCnt++;
            QWupLog.i(TAG, "====sendGuidRequest -> sd卡未准备好，延时请求 res = " + res);
            QWupLog.traceGuidWarn("====sendGuidRequest -> sd卡未准备好，延时请求");
            return LOGIN_RSP_CODE.LOGIN_DELAY_SD_NOREADY;
        }

        QWupLog.trace(TAG,  "sendGuidRequest -> requestGuid : sdcard check end ");
        
        if (!isNeedSendRequest(WUP_OPERTYPE_GET_GUID)) {  // 判断是否正在请求, 返回false 表示正在执行不需要请求
            
            QWupLog.trace(TAG, "sendGuidRequest ->  正在请求guid, cancel ");
            QWupLog.traceGuidWarn("sendGuidRequest ->  正在请求guid, cancel ");
            return LOGIN_RSP_CODE.LOGIN_CANCEL_RUNING;
        }      
        
        
        if (!isLoginTimeout(QWupSdkConstants.WUP_INNER_REQ_MIN_INTERVAL)) {
            QWupLog.trace(TAG, "sendGuidRequest ->  请求频繁, 可能其他进程已执行");
            QWupLog.traceGuidWarn("sendGuidRequest ->  请求频繁, 可能其他进程已执行 ");
            return LOGIN_RSP_CODE.LOGIN_DELAY_OTHE_RUN;
        }
        
        int operType = WUP_OPERTYPE_GET_GUID;
        RomBaseInfo baseInfo = QRomWupImplEngine.getInstance().getRomBaseInfo();

        if (!QRomWupBuildInfo.isQRomSys(getContext()) && QWupStringUtil.isEmpty(baseInfo.sQUA)) {  // qua异常
            String warrnInfo = "==qua is empty: 请在app的asset目录配置qua文件 build_config.ini文件==";
            QWupLog.e(TAG, warrnInfo);
            QWupLog.traceGuidWarn(warrnInfo);
            try {
                throw new IllegalArgumentException(warrnInfo);
            } catch (Exception e) {
                QWupLog.w(TAG, e);
            }
        }
        
        if (QWupStringUtil.isEmpty(baseInfo.sQIMEI) || 
                BASEINFO_ERR_CODE.QIME_INIT_EMPTY_CODE.equals(baseInfo.sQIMEI)) {
            
            String statQimei = QRomWupStatEngine.getInstance().statGetQimei(getContext());
            if (!QWupStringUtil.isEmpty(statQimei)) { // qimei不为空
                baseInfo.sQIMEI = statQimei;
                // 同时更新qimei 
                QRomWupImplEngine.getInstance().updateQimei(statQimei);
            }
        }
        
        if (QWupStringUtil.isEmpty(baseInfo.sQIMEI)) {
            baseInfo.sQIMEI = BASEINFO_ERR_CODE.QIME_REPORT_EMPTY_CODE;
        }
        
        if (baseInfo.sQIMEI.startsWith(BASEINFO_ERR_CODE.QIME_ERR_CODE_SUFF)) {  // 非法qimei
            
            if (QWupStringUtil.isEmpty(mProcessName)) {
                mProcessName = PhoneStatUtils.getCurProcessName(getContext());
            }
            if (mLoginCnt == 0) { // 第一次上报login
                if (QWupStringUtil.isEmpty(mProcessName)) {
                    baseInfo.sQIMEI =  baseInfo.sQIMEI +"_null";
                } else {
                    baseInfo.sQIMEI =  baseInfo.sQIMEI +"_" + mProcessName;
                }
                mLoginCnt++;
            } else {
                if (baseInfo.sQIMEI.contains("_null") && !QWupStringUtil.isEmpty(mProcessName)) {  // 进程名不为空                    
                    // 将空进程名替换
                    baseInfo.sQIMEI = baseInfo.sQIMEI.replace("_null", "_" + mProcessName);
                }
            }           
        }
        QWupLog.trace(TAG, "sendGuidRequest ->  baseInfo.sQIMEI = "+baseInfo.sQIMEI );
        UniPacket packet = WupAppProtocolBuilder.createLoginReqData(baseInfo,
                PhoneStatUtils.getMacAddress(getContext()), null);
//        UniPacket packet = WupAppProtocolBuilder.createLoginReqData(baseInfo,
//                PhoneStatUtils.getMacAddress(getContext()), PhoneStatUtils.getCurProcessName(getContext()));
        
        QRomWupReqExtraData wupReqExtraData = new QRomWupReqExtraData();
        // 缓存请求的guid
        wupReqExtraData.extraStr = QWupStringUtil.byteToHexString(baseInfo.getVGUID());
        
		int reqId = QRomWupImplEngine.getInstance().getSdkInnerWupManger().sendWupRequest(operType, packet, wupReqExtraData);
		QWupLog.trace(TAG,  "sendGuidRequest -> requestGuid : reqId = " + reqId 
						+ ", mQubeWupInfo guid: " + mQubeWupInfo.getGuidStr()
						+ ", baseInfo guid: " + QWupStringUtil.byteToHexString(baseInfo.vGUID));
		QWupLog.traceGuidWarn("sendGuidRequest -> requestGuid : reqId =" + reqId);
        if (reqId > 0) { // 请求发送成功  
            QRomWupSharedpreferences.setLastLoginTime(getContext(), System.currentTimeMillis());
            setCmdTypeRequestState(operType, true);
        }
        return reqId;
    }
	
	private boolean sendIpListMsg(boolean bForceAll) {
		Message msg = mTimeoutHandler.obtainMessage();
		msg.what = MSG_WUP_CHECK_SEND_IPLIST;
		msg.obj = Boolean.valueOf(bForceAll);
	    return mTimeoutHandler.sendMessageDelayed(msg, 300);
	}
	
	 /**
     * 请求iplist
     * @return
     */
    private int onSendIpListRequestByCheck(boolean bForceAll) {     
    	long subtime = System.currentTimeMillis() - QRomWupSharedpreferences.getLastIPListTime(getContext());
        if (!isNeedSendRequest(WUP_OPERTYPE_GET_IPLIST) 
                && subtime > 0 
                && subtime < QWupSdkConstants.WUP_IPLIST_REQ_TIMEOUT) {  // 判断是否正在请求, 返回false 表示正在执行不需要请求
            
            QWupLog.trace(TAG, "sendIpListRequest -> iplist is requesting ");
            return IPLIST_RSP_CODE.IPLIST_CANCEL_RUNING;
        }
    	
        boolean bAll = false;
        int netType =  QRomWupDataBuilderImpl.getNetTypeOfService(ApnStatInfo.getNetType());
        
        // 1. 缓存列表中存在7个列表，说明已经拉取过列表（未拉取过列表，则全量拉取，无视网络）
        // 2. wifi网络下总是全量拉取
        // 3. 强制全量更新 
        if (bForceAll || mQubeWupInfo.getProxyListTypeCnt() < 7 || netType == ENETTYPE._NET_WIFI) {
            bAll = true;
        }
        
        return sendIpListRequest(bAll);
    }
    
    private int sendIpListRequest(boolean bAll) {
    	int operType = WUP_OPERTYPE_GET_IPLIST;
        // 设置请求类型
        ArrayList<Integer> typeList = new ArrayList<Integer>(2);
        typeList.add(EIPType._WUPPROXY);
        typeList.add(EIPType._WUPSOCKET);
        
     // 当前网络类型
        int apnType = QRomWupDataBuilderImpl.getApnType(ApnStatInfo.getCurApnProxyIndex());
        int netType = QRomWupDataBuilderImpl.getNetTypeOfService(ApnStatInfo.getNetType());
        
        UniPacket packet = WupAppProtocolBuilder.createIpListReqData(getGUIDBytes(), 
                typeList, apnType, netType, bAll);
        QRomWupReqExtraData reqExtraData = null;
        if (netType == ENETTYPE._NET_WIFI) {  // 获取当前bssid信息            
            String bssid = ApnStatInfo.getWifiBSSID(getContext());
            if (!QWupStringUtil.isEmpty(bssid)) {
                reqExtraData = new QRomWupReqExtraData();
                reqExtraData.extraStr = bssid;
                QWupLog.trace(TAG, "onSendIpListRequest-> wifi bssid: " + reqExtraData.extraStr);
            }
        }
		int reqId = QRomWupImplEngine.getInstance().getSdkInnerWupManger()
				.sendWupRequest(operType, packet, reqExtraData);
        if (reqId > 0) {
            setCmdTypeRequestState(operType, true);
            QRomWupSharedpreferences.setLastIPListTime(getContext(), System.currentTimeMillis());
        }
        QWupLog.trace(TAG, 
                "sendIpListRequest -> reqI= " + reqId + ", apnType = " + apnType + ", netType = " + netType);
        return reqId;
    }
   
	
    /**
     * 设置对应命令请求状态
     * @param cmdType
     * @param state  是否在进行中等状态
     */
    private void setCmdTypeRequestState(int operType, boolean state) {
        QWupLog.w(TAG, "setCmdTypeRequestState -> cmdType - state " + operType + " - " + state);
        switch (operType) {
        case WUP_OPERTYPE_GET_GUID:
            mIsLogining = state;
            break;
        case WUP_OPERTYPE_GET_IPLIST:
            mIsProxyUpdating = state;
        default:
            break;
        }
    }
	

    /**
     * 处理loginRsp
     *    --目前有获取guid和上报基本信息走该协议
     * @param response
     */
    private void onProcessLoginRsp(int reqId, byte[] response, QRomWupReqExtraData wupReqExtraData) {
        QWupLog.trace(TAG, "====onProcessLoginRsp ->parase guid start");
//        LoginRsp loginRsp = (LoginRsp) QubeWupDataBuidler.parseWupResponseByFlg(response, "stLoginRsp");
        // 重置标志位 -- 现在login和guid走相同逻辑
        LoginRsp loginRsp = new LoginRsp();
        String info = null;
        loginRsp = (LoginRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(response, "stLoginRsp", loginRsp);
        QWupLog.trace(TAG, "====onProcessLoginRsp-> parase guid  -- end parseWupResponseByFlgV3");
        if (loginRsp == null || loginRsp.vGUID == null || loginRsp.vGUID.length == 0) { // 服务器未返回guid，用本地缓存的guid
            int res = QRomWupDataBuilder.getuniPacketResultV3(response);
            info = "onProcessLoginRsp -> guid返回为空 ， resCode = " + res;
            if (res == ELOGINRET._E_QUA_ERROR) {
                info = info +" : qua出现错误，请确认qua相关信息！";
            } else if (res == ELOGINRET._E_QUA_SN_UNCONF) {
                info = info +" : qua sn 未配置，请在相关配置平台加入配置信息！";
            }
            QWupLog.w(TAG, info);
            QWupLog.trace(TAG, info);
            QWupLog.traceGuidWarn(info);
        } else { // 获取到guid 
            QWupLog.trace(TAG, "  ====onProcessLoginRsp -> parase guid  --  saveWupInfo");
            long curTime = System.currentTimeMillis();
            // 请求时的guid
            if (wupReqExtraData != null && !QRomWupInfo.isGuidValidate(wupReqExtraData.extraStr)) {  // 请求时guid不合法
                long guidSubTime = curTime - QRomWupSharedpreferences.getGuidTime(getContext());
                // 重新加载guid等信息，避免多进程同时请求导致其他进程guid不一致
                mQubeWupInfo.reload(getContext());
                if (guidSubTime > 0 && guidSubTime < 5000 
                        && QRomWupInfo.isGuidValidate(mQubeWupInfo.getGuidBytes())) {  // 获取guid的时间在5s内 且 已重新加载到guid
                    info =  "====onProcessLoginRsp-> parase guid, 缓存文件中已有新guid, 取消此次guid变更!  ";
                    QWupLog.trace(TAG, info);
                    QWupLog.traceGuidInfo(info);
                    return;
                }
            }
            // 设置新的guid
            mQubeWupInfo.setSGUID(loginRsp.vGUID, getContext());
            // 通知其他app guid改变
            sendBroadcastForWupBaseDataUpdate(WUP_DATA_TYPE.WUP_DATA_GUID);
            // 设置拉取guid的时间
            QRomWupSharedpreferences.setGuidTime(getContext(), curTime);            
            if (!mQubeWupInfo.saveWupInfo(getContext())) { // guid保存失败
                QWupLog.trace(TAG, "  ====onProcessLoginRsp -- parase guid  --  save fails");
                Map<String, String> statInfo = new HashMap<String, String>(2);
                statInfo.put("GUID_SAVE_F", QWupStringUtil.byteToHexString(loginRsp.vGUID));
                QRomWupStatEngine.getInstance().statWupRequestInfoForce(getContext(), statInfo);
            }
            mGuidTime = curTime;
            QWupLog.trace(TAG, "====onProcessLoginRsp -> parase guid  --  onWupBaseDataChanged");
            // 通知数据改变
            QRomWupImplEngine.getInstance().onWupBaseDataChanged(WUP_DATA_TYPE.WUP_DATA_GUID);
            info = "====onProcessLoginRsp-> 保存guid  "
                    + QWupStringUtil.byteToHexString(loginRsp.vGUID);
			QWupLog.trace(TAG, info);
			QWupLog.traceGuidInfo(info);
        }    

        QWupLog.d(TAG, "====onProcessLoginRsp -> parase guid end, login finish");
    }
    
    /**
     * 解析iplist数据
     * @param reqId
     * @param response
     * @param reqExtraData
     */
    private void onProcessIpList(int reqId, byte[] response, QRomWupReqExtraData reqExtraData) {
        QWupLog.trace(TAG, "onProcessIpList start ");
        IPListRsp ipListRsp = new IPListRsp();
        ipListRsp = (IPListRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(response, "stIPListRsp", ipListRsp);
        if (ipListRsp == null) {
            QWupLog.trace(TAG, "onProcessIpList -> 返回数据为空 ");
            return;
        }
        
        ArrayList<JoinIPInfo> ipInfoList =  ipListRsp.vJoinIPInfo;
        if (ipInfoList == null || ipInfoList.isEmpty()) {
            QWupLog.d(TAG, " onProcessIpList -> ipInfoList为空");
            return;
        }
        Context context = getContext();
        // 重新加载下缓存文件，避免多进程拉取guid，导致guid变化
        mQubeWupInfo.reload(context);
        int proxyIndex = -1;
        QWupLog.d(TAG, " refresProxyList -> ipListRsp： " + ipListRsp);
        //  获取后台返回的client ip信息
        String reqClientIp = ipListRsp.sClientIp;
        String bssid = null;
        for(JoinIPInfo info : ipInfoList) {
            if (info == null) {
                continue;
            }
            bssid = null;
            if (info.eNetType == ENETTYPE._NET_WIFI) {  // 网络类型是wifi
                proxyIndex = ApnStatInfo.PROXY_LIST_WIFI;
                bssid = reqExtraData == null ? null : reqExtraData.extraStr;                
            } else if (info.eNetType == ENETTYPE._NET_4G) {  // 4g 这里后台协议暂时未在ApnType中区分wap，net，这里统一存放在4gnet
                proxyIndex = ApnStatInfo.PROXY_LIST_4G;                
            } else {                
                proxyIndex = QRomWupDataBuilderImpl.getUserApnProxyIndex(info.eApnType);
            }
            
            switch (info.eIPType) {
            case EIPType._WUPPROXY :    // 普通wup iplist信息                    
//                reqClientIp = "1.1.1.1:";
//                reqClientIp += proxyIndex;
//                QWupLog.i("====", "_WUPPROXY reqClientIp:" + reqClientIp);
                // 更新相关请求信息
                mQubeWupInfo.refreshProxyListByType(proxyIndex, info.vIPList, reqClientIp);
                if (!QWupStringUtil.isEmpty(bssid)) {  // 获取到对应的bssid
                    // 缓存到对应的wifi bssid中
                    mQubeWupInfo.refreshWifiProxyListByBssid(bssid,  info.vIPList, reqClientIp);
//                    List<String> testIplist = new ArrayList<String>();
//                    long curTime = System.currentTimeMillis();
//                    testIplist.add(bssid+":111" + "_" + reqId + "_" + curTime);
//                    testIplist.add(bssid+":112" + "_" + reqId + "_" + curTime);
//                    testIplist.add(bssid+":113" + "_" + reqId + "_" + curTime);
//                    mQubeWupInfo.refreshWifiProxyListByBssid(bssid,  testIplist, reqClientIp);
                }
                setProxyServer(0);           
                break;
                
            case EIPType._WUPSOCKET:   // socket wup iplist信息                    
//                reqClientIp = "2.3.3.3:";
//                reqClientIp += proxyIndex;
//                QWupLog.i("====", "_WUPSOCKET reqClientIp:" + reqClientIp);
                mQubeWupInfo.refreshSocketProxyListByType(proxyIndex, info.vIPList, reqClientIp);
                // 更新相关请求信息
                if (!QWupStringUtil.isEmpty(bssid)) {  // 获取到对应的bssid
                    // 缓存到对应的wifi bssid中
                    mQubeWupInfo.refreshWifiSocketListByBssid(bssid,  info.vIPList, reqClientIp);
                }
                setSocketProxyServer(0);
                break;
            }  // ~ 不同类型iplist更新完成
        }  // ~ end iplst 更新

        QWupLog.d(TAG, "  onProcessIpList 保存ip信息 ");
        if (mQubeWupInfo.saveWupInfo(context)) {
            QRomWupSharedpreferences.setLastIPListTime(context, 
                    System.currentTimeMillis());
         // 发送通知iplist改变
           sendBroadcastForWupBaseDataUpdate(WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW);
            // 通知数据改变
            QRomWupImplEngine.getInstance().onWupBaseDataChanged(WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW);
        }
    }    
    
    /**
     * 发送通知数据改变的广播
     * @param reqType 请求类型
     */
    protected void sendBroadcastForWupBaseDataUpdate(int reqType) {
        Context context = getContext();
        if (context == null) {
            QWupLog.i(TAG, "sendBroadcastForWupBaseDataUpdate-> context is null");
            return;
        }
        // 发送通知数据改变
        Intent intent = new Intent(QWupSdkConstants.ACTION_WUP_SDK_BASEDATA_UPDATED);
        intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, reqType);
        context.sendBroadcast(intent);
        QWupLog.v(TAG, "sendBroadcastForWupBaseDataUpdate-> ACTION_WUP_SDK_BASEDATA_UPDATED reqType = " + reqType);
    }
    
    /**
     * 是否主进程处理了该请求
     * @return
     */
    protected boolean isRequestGuidByMainProcess(int reqType) {
        return false;
    }
    
    @Override
    public void release() {
    	super.release();
    	mIsLogining = false;
    	mIsProxyUpdating = false;
    	syncWupInfo();
    	mQubeWupInfo.reSet();
    	mCheckSdGuidDelayCnt = 0;
    	QRomWupEnvironment.getInstance(getContext()).unreigsterSwitchListener(this);
    	unregisterIpListUpdateBroadcast();
    }
    
    public void onReceiveAllData(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response) {
        String info = "onReceivedAllData reqID = " + reqId + " modelType = " + fromModelType 
                + " operType = " + operType;
        QWupLog.trace(TAG, info);        
        if (fromModelType == QRomSdkInnerWupMananger.MODEL_TYPE_WUP) {
            try {
                if (operType == WUP_OPERTYPE_GET_GUID) {
                    onProcessLoginRsp(reqId, response, wupReqExtraData);
                    QWupLog.traceGuidInfo("login, guid请求完成");
                } else if (operType == WUP_OPERTYPE_GET_IPLIST) {
                    onProcessIpList(reqId, response, wupReqExtraData);
                    QWupLog.traceGuidInfo("login, iplist请求完成");
                }
            } catch (Exception e) {
                QWupLog.w(TAG, e);
            }
            setCmdTypeRequestState(operType, false);
            return;
        }
    }
    
    public void onReceiveError(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, QRomWupRspExtraData wupRspExtraData, 
            String serviceName, int errorCode, String description) {
        String info = "onReceivedError reqID = " + reqId +"  modelType = " + fromModelType 
                + " operType = " + operType 
                + "   errorCode: " + errorCode+ "  description: " + description;
        QWupLog.trace(TAG, info);
        if (fromModelType == QRomSdkInnerWupMananger.MODEL_TYPE_WUP) {
            if (operType == WUP_OPERTYPE_GET_GUID) {
                QWupLog.traceGuidWarn("login, guid请求失败");
            }            
            setCmdTypeRequestState(operType, false);               
            return;
        }
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        
        switch (msg.what) {
        
        case MSG_WUP_CHECK_SDGUID_TIMEOUT:  // sd未准备好，guid延时请求
            // 重新加载guid
            refreshInfos();
            
            // 请求guid
            requestGuid();
            break;
        case MSG_WUP_CHECK_SEND_GUID:
            QWupLog.i(TAG, "====handleMessage ->sendGuidRequest start");
            // 请求转给provider主逻辑进程
            int reqType = msg.arg1;
            boolean hasReq = false;
            if (!QRomWupInfo.isGuidValidate(getGUIDBytes())) {  // guid不合法
                hasReq = isRequestGuidByMainProcess(reqType);
            } else {  // guid合法
                if (!isLoginTimeout(0)) {  // 请求未超时
                    hasReq = true; 
                }
            }
            
            int reqId =  99;
            refreshInfos();
            if (!hasReq) {  // 需要继续请求
                reqId = sendGuidRequest();
            }
            QWupLog.trace(TAG, "====handleMessage ->sendGuidRequest hasReq = "
                    + hasReq + ", reqId = " + reqId);
            if (reqId > 0) {  // 请求已发送
                removeMsg(MSG_WUP_CHECK_SEND_GUID);
            }
            break;
            
        case MSG_WUP_CHECK_SEND_IPLIST:  // 发送iplist请求
            int id = onSendIpListRequestByCheck((Boolean)msg.obj);
            QWupLog.trace(TAG, "====handleMessage ->onSendIpListRequest reqId = " + id);
            if (id > 0) {  // 请求成功发送
                // 移除多余消息
                removeMsg(MSG_WUP_CHECK_SEND_IPLIST);
            }
            break;
            
        case MSG_WUP_PROCESS_INIT_DELAY:  // 延时初始化
//            if (!isGuidValidate(mQubeWupInfo.getGuidBytes())) { // guid不合法
//                mQubeWupInfo.load(getContext());
//            }
            QWupLog.trace(TAG, "====handleMessage ->MSG_WUP_PROCESS_INIT_DELAY load wup file ");
            // 加载主动拉取的guid等相关信息
//            mQubeWupInfo.reload(getContext());  
            refreshInfos();
            checkGuid();
            break;

        default:
            break;
        }
        return true;
    }
    
    private void registerIpListUpdateBroadCast() {
    	IntentFilter filter = new IntentFilter();
        filter.addAction(QWupSdkConstants.ACTION_WUP_SDK_UPDATE_IPLIST);
        getContext().registerReceiver(mIpListUpdateReceivor, filter);
    }
    
    private void unregisterIpListUpdateBroadcast() {
    	getContext().unregisterReceiver(mIpListUpdateReceivor);
    }
    
    // 开关开启，尝试拉取IpList，可以及时避免由于IpList在开启时未及时拉取IpList
    public void onSwitchChanged() {
    	if (!QRomWupEnvironment.getInstance(getContext()).isAllClosed()) {
    		sendIpListUpdateByCheck(false, 0, false);
    	}
    }
	
}
