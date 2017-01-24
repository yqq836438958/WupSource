package qrom.component.wup.runInfo;

import java.util.List;
import java.util.Map;

import qrom.component.wup.QRomWupConstants;
import qrom.component.wup.QRomWupConstants.LOGIN_RSP_CODE;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.QRomWupConstants.WUP_RUNTIME_FLG;
import qrom.component.wup.QRomWupConstants.WUP_START_MODE;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.runInfo.processer.QApp2RomInfoProcesser;
import qrom.component.wup.runInfo.processer.QApp2RomSysInfoProcesser;
import qrom.component.wup.runInfo.processer.QAppWupInfoProcesser;
import qrom.component.wup.runInfo.processer.QRomSysWupInfoProcesser;
import qrom.component.wup.runInfo.processer.QRomWupInfoProcesser;
import qrom.component.wup.runInfo.processer.QWupBaseInfoProcesser;
import qrom.component.wup.sysImpl.QRomWupSerializUtils;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_ERR_INDEX;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_REQ_TYPE;
import qrom.component.wup.utils.QWupSdkConstants.WUP_OPER_TYPE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;
import qrom.component.wup.wupData.QRomIplistData;
import qrom.component.wup.wupData.QRomWupInfo;
import android.content.Context;
import android.util.SparseArray;

public class QRomWupRunTimeManager {

    public static final String TAG = "QRomWupRunTimeManager";
    
    /** wup 模块启动模式 -- startUp标记位*/
    private int mStartMode = WUP_START_MODE.WUP_START_GET_GUID;
    /** wup 模块运行环境 -- rom 模式还是 / app 独立拉取模式 / 在制定rom中的app模式*/
    private int mRunEnvMode = -1;
    
    QWupBaseInfoProcesser mBaseInfoProcesser;
    
    
    private int mSocketProxyErrCnt = 0;
    private Integer mProxyErrCnt = 0;
    
    private QRomIplistData mTestIpInfo;
    private QRomIplistData mDebugIpInfo;
    
    private QRomIplistData mTestSocketIpInfo;
    private QRomIplistData mDebugSocketIpInfo;
    
    protected QRomWupRunTimeManager() {
        
    }
    
    public synchronized void startUp(Context context, int startMode) {
       
        // 当前启动模式  -- 待扩展（是否自动拉取guid）
    	mStartMode = startMode;
    	// 设置当前运行环境模式
    	mRunEnvMode = QRomWupBuildInfo.getWupRunMode();
    	
        if (mBaseInfoProcesser == null) {
            mBaseInfoProcesser = getWupBaseInfoProcesser();
            mBaseInfoProcesser.startUp(context);
        }
        
        QWupLog.traceSdkI("startUp - >  mRunEnvMode = " + mRunEnvMode);
    }
        
    /**
     * 获取同步wup相关信息的处理对象
     *   如获取guid等
     */
    private QWupBaseInfoProcesser  getWupBaseInfoProcesser() {
        
        if (QRomWupBuildInfo.isSysRomSrcMode(getContext())) {  // 系统sdk源码集成rom中
            return new QRomSysWupInfoProcesser();
        }
        
        if (QRomWupBuildInfo.isAppInSysRomSrcMode(getContext())) {  // sdk 源码集成在framework中的rom中
            return new QApp2RomSysInfoProcesser();
        }
        
    	if (QRomWupBuildInfo.isWupForRom(getContext())) { // rom底层获取统一的guid
    		return new QRomWupInfoProcesser();
    	}
    	
    	
    	if (QRomWupBuildInfo.isRomWupApkExist(getContext())) {  // 有rom apk，走rom guid模式
    		return new QApp2RomInfoProcesser();
    	}
    	// app独立模式
        return new QAppWupInfoProcesser();
    }
    
    public byte[] getGuidBytes() {
        /*
         * 判断是否独立拉取guid
         * 1. app 的wup sdk 判断是rom模式，则用rom guid，不主动拉去
         * 2. app 的wup sdk 是非rom模式，则自己去拉取
         * 3. rom 的wup 判断是否拉取guid
         * 
         */
    	if (mBaseInfoProcesser == null) {
    		throw new IllegalStateException("state is not right, did you forget to startup?");
    	}
        byte[] guid = mBaseInfoProcesser.getGUIDBytes();
        if (!QRomWupInfo.isGuidValidate(guid)) { // guid不合法
            mBaseInfoProcesser.refreshInfos();
            mBaseInfoProcesser.checkGuid(QWupSdkConstants.WUP_INNER_REQ_MIN_INTERVAL * 2);
            QWupLog.traceSdkI("getGUIDBytes -> guid is empty, refresh cache,");            
        }
        guid = mBaseInfoProcesser.getGUIDBytes();
        return guid;
    }
    
	/**
	 * 获取当前所有接入点的wup代理地址
	 * @return
	 */
	public SparseArray<List<String>> getAllWupProxyInfos() {  
		
		if (isWupRunInTest()) {  // 测试环境
			QWupLog.i(TAG, "getAllWupProxyInfos -> test mode return null;");
			return null;
		}
		
		return mBaseInfoProcesser.getAllWupProxyInfos();
		
//		SparseArray<List<String>> testMap = new SparseArray<List<String>>();
//		
//		List<String> testList = null;
//		String str = "";
//		for (int i = 0; i < 5; i++) {
//			 testList = new ArrayList<String>();
//			 for (int j = 0; j < 3; j++) {
//				 str =  "" + i  + j  + "." + i + j + j + "." + i + j +j + "." + j;
//				 QWupLog.w("====", "testip = " + str);
//				 testList.add(str);
//			 }
//			 
//			 testMap.put(i, testList);
//			 
//		}
//		return testMap;
	}
	
	public SparseArray<QRomIplistData> getAllWupProxyIplistDatas() {  
	    return mBaseInfoProcesser.getAllWupProxyIplistDatas();
	}
	
	public Map<String, QRomIplistData> getAllWupWifiProxyIplistDatas() {  
	    return mBaseInfoProcesser.getAllWupWifiProxyIplistDatas();
	}
	public Map<String, QRomIplistData> getAllWupWifiSocketIplistDatas() {  
	    return mBaseInfoProcesser.getAllWupWifiSocketProxyIplistDatas();
	}
	
	   /**
     * 获取当前所有接入点的wup socket 代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupSocketProxyIplistDatas() {  
        
        return mBaseInfoProcesser.getAllWupSocketProxyIplistDatas();              
    }
	
	/**
	 * 获取当前所有接入点的wup socket 代理地址
	 * @return
	 */
	public SparseArray<List<String>> getAllWupSocketProxyInfos() {  
		
		return mBaseInfoProcesser.getAllWupSocketProxyInfos();				
	}
    
        
    /**
     * 同步原应用的guid
     *   -- 将设置进来的guid替换并保存
     * @param guid
     */
    public boolean synOrgAppGuidForApp(Context context, byte[] guid) {
        
        if (guid == null || !QRomWupInfo.isGuidValidate(guid)) {  // guid非法
            return false;
        }
        
        QRomWupInfo wupInfo = new QRomWupInfo(
        		QWupFileUtil.FILE_USER_WUP_INFO_APP, 
        		QWupFileUtil.SD_USER_WUP_INFO_APP);
        wupInfo.load(context);
        wupInfo.setSGUID(guid, context);
        
        return wupInfo.saveWupInfo(context);
    }
    
    /**
     * 获取当前使用的wup代理地址
     * @return String
     */
    public String getWupProxyAddress() {
     
        QRomIplistData ipData = getCurPorxyIpInfo();
        return getWupProxyAddress(ipData);
    }
    
    public String getWupProxyAddress(QRomIplistData ipData) {
        if (ipData == null || QWupStringUtil.isEmpty(ipData.getCurIpAddr())) {
            if (isWupRunInTest()) {  // 测试环境
                return QWupSdkConstants.REMOTE_WUP_PROXY_TEST;
            }
            QWupLog.trace(TAG, "getWupProxyAddress-> iplist data is empty, use default addr!");
            return QWupSdkConstants.REMOTE_WUP_PROXY;
        }
        return ipData.getCurIpAddr();
    }
        
    
    /**
     * 获取当前ip相关的信息
     *   -- 如ip在当前iplist中的索引位置， 当前iplist中ip的个数，及请求iplist当时客户端的ip信息
     *   
     * @return
     */
    public QRomIplistData getCurPorxyIpInfo() {
        
        if (isWupRunInTest()) {  // 测试环境
            String testWupAddress = QRomWupBuildInfo.getWupTestProxyAddr();
            if (mTestIpInfo == null) {
                mTestIpInfo = QRomIplistData.createTestIpListInfo(testWupAddress, IPLIST_ERR_INDEX.IPLIST_TEST_ENV);
            }
            
            return mTestIpInfo;
        }

        if (isForcedUseDebugAddr()) {  // 使用指定的调试地址
            String testWupAddress = QRomWupBuildInfo.getWupDebugProxyAddr();
            if (QWupStringUtil.isEmpty(testWupAddress)) {
                QWupLog.trace(TAG, "getWupProxyAddress -> isForcedUseDebugAddr is null, used REMOTE_WUP_PROXY!");
                testWupAddress = QWupSdkConstants.REMOTE_WUP_PROXY;
            }
            if (mDebugIpInfo == null) {
                mDebugIpInfo = QRomIplistData.createTestIpListInfo(testWupAddress, IPLIST_ERR_INDEX.IPLIST_SELF_DEFINE);
            }
            QWupLog.trace(TAG, "getWupProxyAddress -> isForcedUseDebugAddr");
            return mDebugIpInfo;
        }        

        return mBaseInfoProcesser.getWupProxyAddressData();
//        if (mTestIpInfo == null) {
//            mTestIpInfo = QRomIplistData.createTestIpListInfo(QWupSdkConstants.REMOTE_WUP_PROXY_TEST, 
//                    IPLIST_ERR_INDEX.IPLIST_TEST_ENV);
//        }
//        return mTestIpInfo;
    }
    
    /**
     * 获取当前socket ip相关的信息
     *   -- 如ip在当前sokcet iplist中的索引位置， 当前iplist中ip的个数，及请求iplist当时客户端的ip信息
     *   
     * @return
     */
    public QRomIplistData getCurSocketPorxyIpInfo() {
        
        if (isWupRunInTest()) {  // 测试环境
            String testWupAddress = QRomWupBuildInfo.getWupTestSocketProxyAddr();
            if (mTestSocketIpInfo == null) {
                mTestSocketIpInfo = QRomIplistData.createTestIpListInfo(testWupAddress, IPLIST_ERR_INDEX.IPLIST_TEST_ENV);
            }
            
            return mTestSocketIpInfo;
        }

        if (isForcedUseDebugAddr()) {  // 使用指定的调试地址
            String testWupAddress = QRomWupBuildInfo.getWupDebugSocketProxyAddr();
            if (QWupStringUtil.isEmpty(testWupAddress)) {
                QWupLog.trace(TAG, "getCurSocketPorxyIpInfo -> isForcedUseDebugAddr is null, used REMOTE_WUP_PROXY!");
                testWupAddress = QWupSdkConstants.REMOTE_WUP_PROXY;
            }
            if (mDebugSocketIpInfo == null) {
                mDebugSocketIpInfo = QRomIplistData.createTestIpListInfo(testWupAddress, IPLIST_ERR_INDEX.IPLIST_SELF_DEFINE);
            }
            QWupLog.trace(TAG, "getCurSocketPorxyIpInfo -> isForcedUseDebugAddr");
            return mDebugSocketIpInfo;
        }        
        
        return mBaseInfoProcesser.getWupSocketProxyAddressData();
    }
    
    /**
     * 获取wup长连接socket地址
     * @return
     */
    public String getWupProxySocketAddress() {
        
        QRomIplistData ipData = getCurSocketPorxyIpInfo();
        if (ipData == null || QWupStringUtil.isEmpty(ipData.getCurIpAddr())) {
            if (isWupRunInTest()) {  // 测试环境
                return QWupSdkConstants.REMOTE_WUP_SOCKET_PROXY_TEST;
            }
            return QWupSdkConstants.REMOTE_WUP_SOCKET_PROXY;
        }
        return ipData.getCurIpAddr();        
    }
    
    public List<String> getCurSocketProxyList() {
        if (isWupRunInTest()) {  // 测试环境--未拉取iplist，这里返回null
//            QRomLog.i(TAG, "getCurSocketProxyList-> testmode, return null");
            return null;
        }
        QRomIplistData iplistData = mBaseInfoProcesser.getCurApnSocketListData();
        return  iplistData == null ? null : iplistData.getIplistInfo();
    }
    
    
    public List<String> getCurApnProxyList() {
        
        if (isWupRunInTest()) {  // 测试环境--未拉取iplist，这里返回null
//            QRomLog.i(TAG, "getCurApnProxyList-> testmode, return null");
            return null;
        }
        QRomIplistData iplistData = mBaseInfoProcesser.getCurApnProxyListData();
        return  iplistData == null ? null : iplistData.getIplistInfo();
    }
    
    public void reloadWupInfo() {
        mBaseInfoProcesser.refreshInfos();
    }
    
    /**
     * 设置romid
     * @param romId
     */
    public void setRomId(long romId) {
        mBaseInfoProcesser.setRomId(romId);
    }
    
    /**
     * 获取romId
     * @return
     */
    public long getRomId() {
        return mBaseInfoProcesser.getRomId();
    }
    
    public long getRomIdFromTSF() {
        return mBaseInfoProcesser.getRomIdFromTsf();
    }
    
    public String getQua() {
        return mBaseInfoProcesser.getQua(getContext());
    }
    
    /**
     * 切换到下一个ip
     */
    public void changeToNextProxyAddr() {
        mBaseInfoProcesser.changeToNextAddr();
        
    }
    /**
     * 切换到下一个ip
     */
    public void changeToNextSocketProxyAddr() {
         mBaseInfoProcesser.changeToNextSocketAddr();
    }    
    
    /**
     * 基础数据变化 
     * @param type   数据类型
     */
    public void onBaseInfoChanged(int type) {
        
    }
        
    public void onConnectivityChanged(Context context) {
        
        QWupLog.d(TAG, "onConnectivityChanged ->  mBaseInfoProcesser = " + mBaseInfoProcesser);
        int oldApn = ApnStatInfo.getApnType();
        ApnStatInfo.init(context);       
        mBaseInfoProcesser.onConnectivityChanged(ApnStatInfo.getApnType(), oldApn);
    }
    
    public Context getContext() {
        return QRomWupImplEngine.getInstance().getContext();
    }
        
    /**
     * 是否需要将请求强制切换到测试环境
     *    默认变量mIsRemoteTest == true 或者 WupEtcInfo.mWupEnv == 1
     * @return
     */
    private static boolean isWupRunInTest() {
     
        if (QRomWupBuildInfo.isWupRunTestForDebug() 
        		|| QRomWupImplEngine.getInstance().isWupEctConfigInTest()) {  // 测试环境
            
            int etcFlg = QRomWupImplEngine.getInstance().getWupEtcWupEnviFlg();
            QWupLog.trace(TAG, "isWupRunInTest -> isWupRunTestForDebug = " 
        		+ QRomWupBuildInfo.isWupRunTestForDebug() + ", ectFlg = " + etcFlg 
        		+ ", app debug mode: " + QRomWupBuildInfo.isAppPublishModeDebug());
            return true;
        }
        
        return false;
    }
    
    private static boolean isForcedUseDebugAddr() {
        boolean isUsedDebugAddr = QRomWupBuildInfo.isForcedUseDebugAddr();
        
        QWupLog.trace(TAG, "isForcedUseDebugAddr ->  " 
                + isUsedDebugAddr 
                + ", app debug mode: " + QRomWupBuildInfo.isAppPublishModeDebug());
       return isUsedDebugAddr;
    }
    
    public boolean isWupRunTest() {
        return isWupRunInTest();
    }
   
    public int requestGuid() {
    	
    	if (mStartMode != WUP_START_MODE.WUP_START_GET_GUID) {  // 不主动获取guid
    		QWupLog.trace(TAG, "requestGuid -> mStartMode not WUP_START_GET_GUID");
    		return LOGIN_RSP_CODE.LOGIN_CANCEL_NONEED;
    	}
    	QWupLog.trace(TAG, "requestGuid");
        return mBaseInfoProcesser.requestGuid();
    }
    
    /**
     * 强制发送获取guid请求
     * @return
     */
    public int sendGuidRequestForceForMainProces() {
        return mBaseInfoProcesser.sendGuidRequest();
    }
    
    /**
     * 主动发起获取ipList请求
     * @param isForce 是否强制更新<br>
     *        true: 忽略缓存超时时间，检测发送间隔是否频繁,及当前屏幕状态;<br>
     *        false: 判断缓存是否超时，缓存超时且亮屏时才发起请求
     * @return
     */
    public int requestIpList(int reqType) {
    	
    	if (isWupRunInTest()) {  // 测试环境，不拉去iplist
    		QWupLog.w(TAG, "requestIpList -> test mode， do not get IP list");    		
    		return QRomWupConstants.IPLIST_RSP_CODE.IPLIST_CANCEL_TEST_ENV;
    	}
    	
        return mBaseInfoProcesser.requestIpList(reqType);
    }
        
    public int doLogin() {        
        
        return mBaseInfoProcesser.doLogin();
    }
    
    /**
     * wup socket服务器请求返回码错误
     * （如wup服务器超载拒绝访问）
     * @param modelType
     * @param operType
     */
    public void onWupSocketServiceRspCodeErr(int modelType, int operType) {
        QWupLog.trace( TAG, " onWupServiceRspCodeErr -- "+ modelType + " : " + operType);
        Context mContext = getContext();
        if (mContext != null && QWupUrlUtil.isNetWorkConnected(mContext)) {
            if (mSocketProxyErrCnt >= 2) { // 网络ok, wup失败次数超过指定值,则代理地址替换
                QWupLog.trace(TAG, "  onWupSocketServiceRspCodeErr -- 更换ip 计数清空");
                // 改变服务器地址索引
                changeToNextSocketProxyAddr();
                // 计数清0
                mSocketProxyErrCnt = 0;
            }  else {
              QWupLog.trace(TAG, "  onWupSocketServiceRspCodeErr --错误次数+1");
              mSocketProxyErrCnt++;
            }
        }  //~end 网络判断，网络不合法，不切换地址    
    }
    
    /**
     * wup服务器请求返回码错误
     * （如wup服务器超载拒绝访问）
     * @param modelType
     * @param operType
     */
    public void onWupServiceRspCodeErr(int modelType, int operType, String reqUrl) {
        QWupLog.trace(TAG, " onWupServiceRspCodeErr -- "+ modelType + " : " + operType);
        
        if (getContext() == null || !QWupUrlUtil.isNetWorkConnected(getContext())) {
            QWupLog.trace(TAG,
                    " onWupServiceRspCodeErr -- net is not ok, or context null, donot changed ip");
            return;
        }  //~end 网络判断，网络不合法，不切换地址
        
        QWupLog.trace(TAG, " onWupServiceRspCodeErr -- "+ modelType + " : " + operType);
        if (mRunEnvMode == WUP_RUNTIME_FLG.WUP_RUNTIME_APP_IN_ROM) {  // app 在rom模式中，ip
            QRomIplistData iplistData = mBaseInfoProcesser.getCurApnProxyListData();
            QRomIplistData socketIplistData = mBaseInfoProcesser.getCurApnSocketListData();
            if (iplistData == null || iplistData.isIndexVaild()
                    ||  socketIplistData == null || socketIplistData.isIndexVaild()) {  // iplist 索引合法
                
                mBaseInfoProcesser.refreshInfos();
            }           
        }  // ~ app in rom
        
        if (!QWupStringUtil.isEmpty(reqUrl) && !reqUrl.equals(getWupProxyAddress())) {  // 当前请求失败的url，非正在使用的url，不切换索引
            QWupLog.trace(TAG,
                    " onWupServiceRspCodeErr -- url is already chaned, donot changed ip");
            return;
        }
        
        if (mProxyErrCnt >= 2) { // 网络ok, wup失败次数超过指定值,则代理地址替换
            QWupLog.trace(TAG, "  onWupServiceRspCodeErr -- 更换ip 计数清空");
            // 改变服务器地址索引
            changeToNextProxyAddr();
            // 计数清0
            mProxyErrCnt = 0;
        }   else {
          QWupLog.trace(TAG, "  onWupServiceRspCodeErr --错误次数+1");
            mProxyErrCnt++;
        }        
    }
    
    /**
     * 通过aidl的binder方式获取对应类型数据
     * @param dataType
     * @return
     */
    public byte[] getWupDataForAidl(int dataType) {
        QWupLog.trace(TAG, "getWupDataForAidl ->  dataType = " + dataType);
        byte[] rspDatas = null;
        if (dataType > WUP_OPER_TYPE.OPER_WUP_DEFAULT) {  // 操作类型的请求            
            rspDatas = doWupOper(dataType);
        } else {  // 普通数据请求            
            rspDatas = getWupDatasByAidlType(dataType);
        }
        
        return rspDatas;
    }
    
    /**
     * 按数据了下获取数据
     * @param dataType 参看QRomWupConstants.WUP_DATA_TYPE
     * @return  byte[]
     */
    private byte[] getWupDatasByAidlType(int dataType) {
        
        switch (dataType) {
        case WUP_DATA_TYPE.WUP_DATA_GUID:  // 获取guid
            return getGuidBytes();
        case WUP_DATA_TYPE.WUP_DATA_IPLIST:
            return QRomWupSerializUtils.parasIpListInfos2Bytes(dataType, getAllWupProxyInfos());
        case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST:
            return QRomWupSerializUtils.parasIpListInfos2Bytes(dataType, getAllWupSocketProxyInfos());
            
        case WUP_DATA_TYPE.WUP_DATA_ROMID: // rom id
            return QRomWupSerializUtils.parasLong2Bytes(getRomId());
            
        case WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW:  // 新接入点iplist数据
            return QRomWupSerializUtils.parasIpListDatas2Bytes(getAllWupProxyIplistDatas());
            
        case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_NEW: // 新接入点socket iplist数据
            return QRomWupSerializUtils.parasIpListDatas2Bytes(getAllWupSocketProxyIplistDatas());
            
        case WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI:  //wifi下对应接入点的iplist数据信息
            return QRomWupSerializUtils.parasIpListDatas2Bytes(getAllWupWifiProxyIplistDatas());
            
        case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI:  //wifi下对应接入点的socket iplist数据信息
            return QRomWupSerializUtils.parasIpListDatas2Bytes(getAllWupWifiSocketIplistDatas());
            
        case WUP_DATA_TYPE.WUP_DATA_ALL:
            return null;
        default:
            break;
        }
        return null;
    }
    
    /**
     * 执行对应操作 
     * @param operType  参看QWupSdkConstants.WUP_OPER_TYPE
     * @return
     */
    private byte[] doWupOper(int operType) {
        byte[] rsp = null;
        switch (operType) {
        case WUP_OPER_TYPE.OPER_UPDATE_IPLIST2ROM:  // 向rom请求iplist
            int rspCode = requestIpList(IPLIST_REQ_TYPE.IPLIST_REQ_ROM_UPDATE);
            rsp = String.valueOf(rspCode).getBytes();
            break;

        default:
            break;
        }
        
        return rsp;
    }
    
    public synchronized void release() {
        mBaseInfoProcesser = null;
        QWupLog.traceSdkW("release");
    }
    
    public void onReceiveAllData(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response) {
        mBaseInfoProcesser.onReceiveAllData(fromModelType, reqId, operType, wupReqExtraData,
                wupRspExtraData, serviceName, response);
    }
    
    public void onReceiveError(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, QRomWupRspExtraData wupRspExtraData, 
            String serviceName, int errorCode, String description) {
        mBaseInfoProcesser.onReceiveError(fromModelType, reqId, operType, wupReqExtraData, 
                wupRspExtraData, serviceName, errorCode, description);
    }    
}
