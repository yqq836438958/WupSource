package qrom.component.wup.runInfo;

import java.util.List;

import qrom.component.wup.IQubeWupNotifyCallback;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.QRomWupEnvironment;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.net.QRomWupAsymEncryptTask;
import qrom.component.wup.net.QubeWupNetEngine;
import qrom.component.wup.net.QubeWupSyncTaskData;
import qrom.component.wup.net.QubeWupTask;
import qrom.component.wup.net.QubeWupTask.IWupTaskCallBack;
import qrom.component.wup.net.QubeWupTask.WupTaskType;
import qrom.component.wup.net.QubeWupTaskData;
import qrom.component.wup.stat.QRomWupStatEngine;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_REQ_TYPE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomIplistData;
import TRom.RomBaseInfo;
import android.content.Context;
import android.os.Looper;
import android.os.Process;

import com.qq.jce.wup.UniPacket;



/**
 * 和后台wup Service通信管理对象
 * @author sukeyli
 *
 */
public class QRomWupManagerImpl implements IWupTaskCallBack {
    
    public static final String TAG = "QRomWupManagerImpl";
    
    protected QubeWupNetEngine mWupEngine = null;
    
    private IQubeWupNotifyCallback mWupCallback;
    
    public QRomWupManagerImpl(IQubeWupNotifyCallback callback) {
        mWupCallback = callback;
    }
        
    /**
     * 启动wup模块，并开始监听网络等手机状态状态   
     *   1. 开始管理guid，iplist
     *   2. 当iplist缓存超时后，拉取新的iplist
     * 
     * NOTES:
     * 传入context，可能会被hold住无法释放，引起内存泄露。
	 * 因此，尽量使用appContext，另外，退出时，一定要调用release()方法释放资源。
     * 
     * @param sdFilePath
     */
    public synchronized void startup(Context context, int startMode) {
        QWupLog.i(TAG, "starup");
        // 检测配置是否合法
        QRomWupBuildInfo.checkConfigValidy();
        // 启动wup
        QRomWupImplEngine.getInstance().initBase(context);
        // 更新baseInfo基本信息
//        updateBaseInfo(QRomWupImplEngine.getInstance().getRomBaseInfoNoRefresh());
        QRomWupImplEngine.getInstance().startUp(context, startMode);
        // 更新基本信息
        QRomWupImplEngine.getInstance().addWupCallback(this);
        QWupLog.trace(TAG, "startup -> pkg:  " + context.getPackageName()+", pid"  + Process.myPid());
    }
    
    
//    private void updateBaseInfo(RomBaseInfo baseInfo) {
//        if (mWupCallback != null) {
//            mWupCallback.updataBaseInfo(baseInfo);
//        }
//    }
    
    public void setQRomId(long romId) {
        QRomWupImplEngine.getInstance().setRomId(romId);
    }
    
    public long getQRomId() {
        return QRomWupImplEngine.getInstance().getRomId();
    }
    
    public int requestGuid() {
    	return QRomWupImplEngine.getInstance().getWupRunTimeManager().requestGuid();
    }
    
    public int requestIpList() {
    	return QRomWupImplEngine.getInstance().getWupRunTimeManager().requestIpList(
    	        IPLIST_REQ_TYPE.IPLIST_REQ_NORMAL);
    }
    
    public int sendForceIplistIgnoreCacheTimeout() {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().requestIpList(
                IPLIST_REQ_TYPE.IPLIST_REQ_IGNOR_CACHETIMEOUT);
    }
    
    public int doLogin() {
 
        QWupLog.trace(TAG, "doLogin");
//        updateBaseInfo(QRomWupImplEngine.getInstance().getRomBaseInfo());
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().doLogin();
    }
    
    public boolean isWupReqSendTest() {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().isWupRunTest();
    }
    
    
    /**
     * 获取rombaseInfo
     * @return
     */
    public RomBaseInfo getRomBaseInfo() {
        return QRomWupImplEngine.getInstance().getRomBaseInfo();
    }
        
    /**
     * 获取当前网络类型对应的wup 代理地址列表
     * @return
     */
    public List<String> getCurApnProxyList() {
    	return QRomWupImplEngine.getInstance().getWupRunTimeManager().getCurApnProxyList();
    }
    
    /**
     * 获取当前网络类型对应的wup socket代理地址列表        
     * @return
     */
    public List<String> getCurSocketProxyList() {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().getCurSocketProxyList();
    }
    
    public void reloadWupInfo() {
    	QRomWupImplEngine.getInstance().reloadWupInfo();      
    }
    
    /**
     * 加载wup附属配置
     *    -- 如：切换测试环境配置文件
     * @param context
     * @return
     */
    public boolean loadWupEtcInfo(Context context) {
        return QRomWupImplEngine.getInstance().reLoadWupEtcInfo(context);
    }
    
    /**
     * 获取配置文件中环境变量
     * @return
     */
    public int getWupEtcWupEnviFlg() {
        return QRomWupImplEngine.getInstance().getWupEtcWupEnviFlg();
    }
    
    /**
     * 释放资源
     */
    public synchronized void release() {
        QWupLog.trace( TAG, " release () 释放资源" );
        QRomWupImplEngine.getInstance().release(this);
    }           

    
    /**
     * 获取wup http代理地址
     * @return
     */
    public String getWupProxyAddress() {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupProxyAddress();
    }
    private String getWupProxyAddressByIpListInfo(QRomIplistData iplistData) {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupProxyAddress(iplistData);
    }
    /**
     * 获取wup长连接socket地址
     * @return
     */
    public String getWupProxySocketAddress() {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupProxySocketAddress();
    }
    
    
    public void onWupBaseDataChanged(int dataType) {
    	
    	if (mWupCallback == null) {
    		return;
    	}
    	
    	switch (dataType) {
    	case WUP_DATA_TYPE.WUP_DATA_GUID:  // guid改变
    		mWupCallback.onGuidChanged(getGUIDBytes());
    		break;
		case WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW:  // iplist改变
			mWupCallback.sendWupDataChangeMsg(dataType);
			break;

		default:
			break;
		}
    }
    
    public QubeWupTask cancelTask(int reqId) {
        if (mWupEngine != null) {
            return mWupEngine.cancelTask(reqId);
        }
        return null;
    }
        
    /**
     * 发送wup请求(http短连接)
     * @param taskData   请求数据
     * @return int 连接id
     */
    public synchronized int requestWup(int wupReqType, QubeWupTaskData taskData) {
        
        return requestWup(wupReqType, taskData, true);
    }
    
    public synchronized int requestWup(int wupReqType, QubeWupTaskData taskData, boolean isCheckEncryptService) {
        int reqId = -1;
        try {
            if (taskData != null) {
//                QWupLog.i(TAG, "requestWup -> getguid");
                if (reqId <= 0) {
                	reqId = QRomWupImplEngine.getInstance().getRequestId();
                }
                taskData.mReqId = reqId;
                // TODO 设置guid, 具体方式请waley看下 
                taskData.vGuid = getGUIDBytes();
                if (taskData.mReqType < 0) {  // 设置请求类型
                    taskData.mReqType = wupReqType;
                }
                // 统计用session
                taskData.mReqSession = String.valueOf(System.currentTimeMillis())
                        +"_" + taskData.mFromModelType + "_" + taskData.mOperType;
                if (isCheckEncryptService) {  // check是否对应服务的请求数据需要加密                    
                    // 设置是否需要加密 
                    taskData.mIsEncrpt = isEncryptRequest(taskData.mReqServiceName);
                }
                
                taskData.setWupClosed(QRomWupEnvironment.getInstance(QRomWupImplEngine.getInstance().getContext()).isAllClosed());
                
                QubeWupTask wupTask = null;
                // 当前请求iplist 的信息
                QRomIplistData ipData = null;
                
                if (wupReqType == QubeWupTask.WupTaskType.WUP_TASK_TYPE_ASYM_ENCRYPT) {  // 非对策加密请求
                    ipData = QRomWupImplEngine.getInstance().getWupRunTimeManager().getCurPorxyIpInfo();
                    wupTask = new QRomWupAsymEncryptTask(QRomWupImplEngine.getInstance().getContext(), 
                            taskData, this, getWupProxyAddressByIpListInfo(ipData));
                }  else {  // 普通wup请求
                    ipData = QRomWupImplEngine.getInstance().getWupRunTimeManager().getCurPorxyIpInfo();
                    wupTask = new QubeWupTask(QRomWupImplEngine.getInstance().getContext(),
                            taskData, this, getWupProxyAddressByIpListInfo(ipData));
                }
                
                if (ipData != null) {  // 设置相关统计信息
                    taskData.mClientIp = ipData.getClientIp();             
                    taskData.mCurIpIndex = ipData.getCurIpIndex();
                    taskData.mCurIplistSize = ipData.getCurIplistSize();
                    taskData.mCurIplistApn = ipData.getDataFlg();
                }
                if (mWupEngine == null) {
                    QubeWupNetEngine.init();
                    mWupEngine = QubeWupNetEngine.getInstance();
                }
                boolean res = mWupEngine.sendTask(wupTask);
//                QWupLog.i(TAG, "requestWup -> sendTask res = " + res);
            } else {
                reqId = QRomWupImplEngine.getInstance().getRequestErrId();
            }
        } catch (Exception e) {
            reqId = QRomWupImplEngine.getInstance().getRequestErrId();
            QWupLog.trace( TAG, "requestWup -> Exception：msg =  " + e.getMessage());
        }
        
        return reqId;
    }

        
    
    /**
     * 同步发送wup请求
     *    -- 该方法会阻塞调用线程，直到网络数据返回 
     * @param pkt          请求的协议数据
     * @param timeout   超时时间  
     *       （该值为网络连接的超时，部分手机上在假网络时该值无效，系统自动使用默认值；
     *       若请求对时间敏感，请自己另设计时器，或调用异步接口处理网络请求,如requestWup(...)接口）
     * @return  byte[] 服务器返回数据
     */
    public byte[] sendSynWupRequest(UniPacket pkt, long timeout) {
        
        byte[] reqDatas = QRomWupDataBuilder.parseWupUnipackage2Bytes(pkt);
        
        if (reqDatas == null) {
            QWupLog.w(TAG, "sendSynWupRequest -> pkt encoding err");
            return null;
        }
        
        QubeWupSyncTaskData taskData = new QubeWupSyncTaskData();
        // 请求模块类型
        taskData.mFromModelType = -1;
        // 请求操作类型
        taskData.mOperType = -1;
        // 请求数据
        taskData.mData = reqDatas;
        taskData.mReqServiceName = pkt.getServantName();
        taskData.mTimeout = timeout;
        // 设置是否加密
        taskData.mIsEncrpt = isEncryptRequest(taskData.mReqServiceName);
        
        taskData.vGuid = getGUIDBytes();
        
        return sendSynWupRequest(WupTaskType.WUP_TASK_TYPE_NORMAL, taskData);
    }
    

    /**
     * 发送同步非对策加密请求
     * @param reqPkg
     * @param appPkgName
     * @return
     */
    public byte[] sendSynAsymEncryptRequest(UniPacket reqPkg, String appPkgName, long timeout) {
        
        if (reqPkg == null || QWupStringUtil.isEmpty(appPkgName)) {
            QWupLog.w(TAG, "sendSynAsymEncryptRequest -> req param is empty");
            return null;
        }
        
        QubeWupSyncTaskData taskData = new QubeWupSyncTaskData();
        // 请求模块类型
        taskData.mFromModelType = -1;
        // 请求操作类型
        taskData.mOperType = -1;
        taskData.mTimeout = timeout;
        // 请求数据
        taskData.mReqPkg = reqPkg;
        // 请求服务相关信息
        taskData.mReqServiceName = reqPkg.getServantName();
        taskData.mReqFuncName = reqPkg.getFuncName();
        taskData.mReqAppName = appPkgName;
        
        return sendSynWupRequest(WupTaskType.WUP_TASK_TYPE_ASYM_ENCRYPT, taskData);
    }

    /**
     * 同步发送wup请求
     *    -- 该方法会阻塞调用线程，直到网络数据返回  
     *    -- 非ui线程才能调用
     * @param taskData
     * @return
     */
    public byte[] sendSynWupRequest(QubeWupSyncTaskData taskData) {        
      
        return sendSynWupRequest(WupTaskType.WUP_TASK_TYPE_NORMAL, taskData);
    }
    
    
    /**
     * 发送指定类型的同步wup请求
     * @param taskType
     * @param taskData
     * @return
     */
    public byte[] sendSynWupRequest(int taskType, QubeWupSyncTaskData taskData) {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {  // 主线程抛出异常
            throw new IllegalThreadStateException("can't call this method in main thread");
        }
        
        if (taskData == null || taskData.isDataEmpty()) {
            QWupLog.trace(TAG, 
                    "sendSynWupRequest -> reqData is err ");
            return null;
        }
        
        Object waitObject = new Object();
        taskData.setLockObject(waitObject);
        
        int requestId = requestWup(taskType, taskData);
        if (requestId <= 0) {
        	QWupLog.trace(TAG,  "sendSynWupRequest -> requestWup failed!");
        	return null;
        }
        
        synchronized(waitObject) {
        	try {
				waitObject.wait();
			} catch (InterruptedException e) {
				QWupLog.trace(TAG, e);
			}
        }
        
        return taskData.getResponseBytes();
    }
    
    
    
    /**
     * 判断是否是需要加密的服务
     * @param service
     */
    private boolean isEncryptRequest(String service) {
//        boolean res = true;
//        for (int i = 0; i < NO_ENCRYPT_SERVICE_LIST.size(); i++) {
//            if (NO_ENCRYPT_SERVICE_LIST.get(i).equals(service)) {
//                res = false;
//                break;
//            }
//        }
//        return res;
        return true;
    }
 
    /**
     * wup请求失败
     * @param taskData
     * @param errorCode
     * @param description
     */
    public void onReceivedError(QubeWupTaskData taskData, int errorCode, String description) {
        if (taskData == null) {
            QWupLog.trace( TAG, "onReceivedError   -> res data is empty");
            QWupLog.traceSdkW("onReceivedError   -> res data is empty");
            return;
        }
        int modeType = taskData.mFromModelType;
        
        String info = "onReceivedError reqID = " + taskData.mReqId +"  modelType = " + modeType
                + " operType = " + taskData.mOperType + "   errorCode: " + errorCode+ "  description: " + description;
        QWupLog.trace( TAG, info);
        QWupLog.traceSdkW(info);
        if (!taskData.isSync()) {
        	if (mWupCallback != null) {
        		mWupCallback.onReceiveError(taskData.mFromModelType, taskData.mReqId, taskData.mOperType,
        				taskData.getWupReqExtraData(), taskData.mRspExtraData,
        				taskData.mReqServiceName, errorCode, description);
        	}
        } else {
        	taskData.onSyncNotify();
        }
    }
    
    /**
     * wup请求成功
     * @param taskData
     * @param response
     */
    public void onReceivedAllData(QubeWupTaskData taskData, byte[] response) {
    	if (taskData == null) {
            QWupLog.trace( TAG, "onReceivedAllData   -> res data is empty ");
            QWupLog.traceSdkW("onReceivedAllData   -> res data is empty ");
            return;
        }       
        
    	int modeType = taskData.mFromModelType;
    	
        String info = "onReceivedAllData reqID = " + taskData.mReqId + " modelType = " +  modeType
        		+ " operType = " + taskData.mOperType;        
        QWupLog.trace( TAG, info);
        QWupLog.traceSdkI(info);
        
        if (!taskData.isSync()) {
        	if (mWupCallback != null) {
        		mWupCallback.onReceiveAllData(modeType, taskData.mReqId,
        				taskData.mOperType, taskData.getWupReqExtraData(),
        				taskData.mRspExtraData, taskData.mReqServiceName, response);
        	}
        } else {
        	taskData.onSyncNotify();
        }

    }
    
    public void onWupTaskReceivedError(QubeWupTaskData taskData, int errorCode,
            String description, boolean isForceCancel) {
    	QWupLog.trace(TAG, "onWupTaskReceivedError");
    	if ((mWupEngine != null && mWupEngine.onFinishTask(taskData.mReqId)) || isForceCancel) {  // 任务未取消
    		onReceivedError(taskData, errorCode, description);
    		// 切换ip
    		if (errorCode == WUP_ERROR_CODE.WUP_TASK_ERR_SERVICE_RSPCODE 
    		        || errorCode == WUP_ERROR_CODE.WUP_TASK_ERR_EXCEPTION) { // 服务器返回码错误 或 网络异常
    		    QRomWupImplEngine.getInstance().onWupServiceRspCodeErr(taskData.mFromModelType, taskData.mOperType, taskData.mUrl);
    		}
    	}
    }

    /**
     *   发送wup请求
     * @param fromModelType  请求的模块标识  （1000勿用，内部已用）
     * @param operType            请求操作标识
     * @param reqPacket           请求数据
     * @return                           reqId  <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     *                                    （仅当reqId > 0的情况才会有成功或失败的回调）
     */
    public  int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket) {        
        
        return requestWupNoRetry(fromModelType, operType, reqPacket, 0);
    }
    
    /**
     * 请求发送wup请求
     *    -- 该请求不传入serviceName，默认传输的data数据走http请求，会加密数据后发送
     * @param fromModelType
     * @param operType
     * @param reqPacket
     * @param timeout
     * @return
     */
    @Deprecated
    public int requestWupNoRetry(int fromModelType, int operType, byte[] reqPacket, long timeout) {
                        
        return requestWupNoRetry(fromModelType, operType, reqPacket, null, "reqPacket_bytes", "", timeout);
    }
    
    /**
     * 请求发送wup请求
     * @param fromModelType  
     * @param operType
     * @param reqPacketBytes
     * @param extraData
     * @param serviceName
     * @param timeout
     * @return
     */
//    public synchronized int requestWupNoRetry(int fromModelType, int operType, byte[] reqPacketBytes, 
//            QRomWupReqExtraData extraData, String serviceName, long timeout) {        
//        return  requestWupNoRetry(fromModelType, operType, reqPacketBytes, 
//                extraData, serviceName, null, timeout);
//    }
    
    /**
     * 请求发送wup请求
     * @param fromModelType  
     * @param operType
     * @param reqPacketBytes
     * @param extraData
     * @param serviceName
     * @param funcName
     * @param timeout
     * @return
     */
    public synchronized int requestWupNoRetry(int fromModelType, int operType, byte[] reqPacketBytes, 
            QRomWupReqExtraData extraData, String serviceName, String funcName, long timeout) {
        if (reqPacketBytes == null) {
            QWupLog.w(TAG, "requestWupNoRetry -> reqPacketBytes is empty");
            return QRomWupImplEngine.getInstance().getRequestErrId();
        }
        
        QubeWupTaskData taskData = new QubeWupTaskData();
        // 请求模块类型
        taskData.mFromModelType = fromModelType;
        // 请求操作类型
        taskData.mOperType = operType;
        taskData.mData = reqPacketBytes;
        taskData.mTimeout = timeout;
        taskData.mReqServiceName = serviceName;
        taskData.mReqFuncName = funcName;
        taskData.mReqExtraData = extraData;
        
        return requestWup(WupTaskType.WUP_TASK_TYPE_NORMAL, taskData);
    }
    
    /**
     *    notify进程中调用发送wup请求
     * @param fromModelType
     * @param operType     操作类型
     * @param reqPacket    请求数据
     * @param timeout       超时时间（单位：毫秒）
     * @return 请求id reqId<0 发送失败; reqId> 0 成功发送; reqId =0 未发送
     */
    public int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket, long timeout) {
        
        return requestWupNoRetry(fromModelType, operType, reqPacket, null, timeout);
    }
    
    /**
     * 发送wup请求
     * @param fromModelType
     * @param operType
     * @param reqPacket
     * @param extraData
     * @param timeout
     * @return
     */
    public synchronized int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket, QRomWupReqExtraData extraData, long timeout) {
        
        if (reqPacket == null) {
            QWupLog.w(TAG, "requestWupNoRetry -> param is empty");
            return QRomWupImplEngine.getInstance().getRequestErrId();
        }
        
        byte[] datas = null;
        try {
            datas = reqPacket.encode();
        } catch (Exception e) {
            e.printStackTrace();
            return QRomWupImplEngine.getInstance().getRequestErrId();
        }
        return requestWupNoRetry(fromModelType, operType, datas, extraData, reqPacket.getServantName(), reqPacket.getFuncName(), timeout);
    }
    
    
    public synchronized int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg, QRomWupReqExtraData extra, long timeout) {

        // 非对策加密的请求的service会统一中转到后台指定服务，
        // 由中转服务在统一发个原servant的服务（业务数据reqPkg中指定的servant）
        return requestAsymEncryptWup(fromModel, operType, reqPkg, extra, timeout, null);
    }
    
    public synchronized int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg, 
            QRomWupReqExtraData extra, long timeout, String appPkgInfo) {
        
        if (reqPkg == null) {
            QWupLog.w(TAG, "requestAsymEncryptWup -> param is empty");
            return QRomWupImplEngine.getInstance().getRequestErrId();
        }
        
        QubeWupTaskData taskData = new QubeWupTaskData();
        // 请求模块类型
        taskData.mFromModelType = fromModel;
        // 请求操作类型
        taskData.mOperType = operType;
        taskData.mTimeout = timeout;
        // 请求数据
        taskData.mReqPkg = reqPkg;
        // 请求服务相关信息
        taskData.mReqServiceName = reqPkg.getServantName();
        taskData.mReqFuncName = reqPkg.getFuncName();
        taskData.mReqExtraData = extra;
        // 强制走加密流程
        taskData.mIsEncrpt = true;
        taskData.mReqAppName = appPkgInfo;
        // 非对策加密的请求的service会统一中转到后台指定服务，
        // 由中转服务在统一发个原servant的服务（业务数据reqPkg中指定的servant）
        return requestWup(WupTaskType.WUP_TASK_TYPE_ASYM_ENCRYPT, taskData, false);
    }
    
   
    @Override
    public void onWupTaskStartTimeOut(QubeWupTaskData taskData) {
        
        QWupLog.i(TAG, "add wup timeout reqId=" + taskData.mReqId 
                + "  mFromModelType = " + taskData.mFromModelType 
                + "  operType= " + taskData.mOperType + " mTimeout = " + taskData.mTimeout);
        QRomWupImplEngine.getInstance().getWupTimeoutMananger()
                .startWupTimeoutRequest(taskData.mReqId, taskData.mTimeout);
        
    }
    
    private Context getAppContext() {
        return QRomWupImplEngine.getInstance().getContext();
    }
    
    @Override
    public void onWupTaskReceivedError(QubeWupTaskData taskData, int errorCode,
            String description) {
        QWupLog.trace(TAG, "onWupTaskReceivedError -> description: " + description);
        // 清除计时
        QRomWupImplEngine.getInstance().getWupTimeoutMananger()
                .removeWupTimeoutRequest(taskData.mReqId);
        
    	onWupTaskReceivedError(taskData, errorCode, description, 
    			errorCode == WUP_ERROR_CODE.WUP_TASK_ERR_FORCE_TIMEOUT);
    	QRomWupStatEngine.getInstance().statWupStatFailsDatas(getAppContext(), taskData, errorCode, description);
    	QWupLog.trace(TAG, "onWupTaskReceivedError -> end" );
    }

    @Override
    public void onWupReceivedAllDataEnd(QubeWupTaskData taskData, byte[] response) {
        // 清除计时
        QRomWupImplEngine.getInstance().getWupTimeoutMananger().removeWupTimeoutRequest(taskData.mReqId);
        
        boolean isFinsihTask = false;
        if (mWupEngine != null) {
            isFinsihTask = mWupEngine.onFinishTask(taskData.mReqId);
        }        
        
        // 处理返回数据
        if (isFinsihTask) {  // 任务未取消
            if (response != null) {
                onReceivedAllData(taskData, response);
                QRomWupStatEngine.getInstance().statWupStatSucessDatas(getAppContext(), taskData);
            } else {
                onReceivedError(taskData, WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY, "rsp data is empty");
                QRomWupStatEngine.getInstance().statWupStatFailsDatas(getAppContext(), taskData, 
                		WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY, "rsp data is empty");
            }
        }
    }
    
    @Override
    public int onWupRetry(int taskType, QubeWupTaskData taskData) {
        
        if (taskData == null) {
            return -1;
        }
        QWupLog.trace(TAG, "onWupRetry -> start ord reqId = " + taskData.mReqId);
        // 是否做延时重试-- 待定
        // 按原数据模式重发请求
        int reSendReqId = requestWup(taskType, taskData, false);
        QWupLog.trace(TAG, "onWupRetry -> ord reqId = " + taskData.mReqId + ", new reqId = " + reSendReqId);
        return reSendReqId;
    }
    
    /**
     * 获取guid
     * @return byte[]
     */
    public byte[] getGUIDBytes() {
    	return QRomWupImplEngine.getInstance().getWupRunTimeManager().getGuidBytes();
    }
    
    public String getQua() {

    	return QRomWupImplEngine.getInstance().getAppQua(); 
    }

    public IQubeWupNotifyCallback getWupCallBack() {
        return mWupCallback;
    }
    
}
