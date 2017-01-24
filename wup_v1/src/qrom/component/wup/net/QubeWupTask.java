package qrom.component.wup.net;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.net.httpRequester.QubeWupBaseRequester;
import qrom.component.wup.net.httpRequester.QubeWupBaseRequester.ResponData;
import qrom.component.wup.net.httpRequester.QubeWupHttpClientRequester;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupStringUtil;
import android.content.Context;
import android.os.Process;
import android.util.Patterns;

import com.qq.jce.wup.UniPacket;


public class QubeWupTask implements Runnable {

    protected  String TAG = "QROM-QubeWupTask";
    
    public static class WupTaskType {
        /** WUP task 类型 -- 普通wup 请求 */
        public static final int WUP_TASK_TYPE_NORMAL = 1;
        /** WUP task 类型 -- 长连接 wup 请求  */
//        public static final int WUP_TASK_TYPE_LONG_LINK = 2;
        /** WUP task 类型 -- 非对称加密 wup 请求  */
        public static final int WUP_TASK_TYPE_ASYM_ENCRYPT = 3;
    }
    
    public static final int RESPONSE_OK = 200;
    
    IWupTaskCallBack mTaskCallBack;
    protected QubeWupTaskData mTaskData;
    
    private boolean mIsCancel = false;
    
    protected int mErrCode = 0;
    protected String mErrMsg = null; 
    
    protected QubeWupBaseRequester mWupBaseRequester = null;

    
    private Context mContext;
//    protected byte[] testLogData = null;
    public QubeWupTask() {
    }
    
    public QubeWupTask(Context context, QubeWupTaskData taskData) {
        mContext = context;
        mTaskData = taskData;
        if (mTaskData.mTimeout <= 0) { // 没置超时，给默认时间
            mTaskData.mTimeout = QWupSdkConstants.WUP_TIME_OUT;
        }
    }

    public QubeWupTask(Context context, QubeWupTaskData taskData, IWupTaskCallBack taskCallBack, String wupProxyAddress) {
        
    	mContext = context;
    	
        if (taskData == null) {
            taskData = new QubeWupTaskData();
        }
        setTaskCallBack(taskCallBack);

        mTaskData = taskData;
        if (QWupStringUtil.isEmpty(mTaskData.mUrl)) {
            mTaskData.mUrl = wupProxyAddress;
        }
        onTastStart();
    }      
    
    protected void onTastStart() {
        
        if (mTaskData.mTimeout <= 0) { // 没置超时，给默认时间
            mTaskData.mTimeout = QWupSdkConstants.WUP_TIME_OUT;
        } else { // 设置超时

            if (mTaskCallBack != null) {
                mTaskCallBack.onWupTaskStartTimeOut(mTaskData);
            }
        }
    }


    /**
     * 获取taskId
     * @return
     */
    public int getTaskId() {
        if (mTaskData != null) {
            return mTaskData.mReqId;
        }
        return -1;
    }
    
    /**
     * 请求失败
     * @param errorCode
     * @param description
     */
    public void onResponseError(int errorCode, String description, QubeWupTaskData taskData) {
    	try {
    		onResponseErrorMaybeThrow(errorCode, description, taskData);
    	} catch (Throwable e) {
    		QRomLog.trace(TAG, e);
    	}
    }
    
    private void onResponseErrorMaybeThrow(int errorCode, String description, QubeWupTaskData taskData) {
    	 taskData.onDataError(errorCode, description);
         if (QWupStringUtil.isEmpty(taskData.mNetType)) { // 请求网络类型为空
             String curNet = QRomWupStatUtils.getCurStatNetType(mContext);
             taskData.mNetType = curNet + "_" + curNet;
         } else {            
             // 请求失败, 更新网络状态
             taskData.mNetType += "_" + QRomWupStatUtils.getCurStatNetType(mContext);
         }
         // 回调wup请求失败
         if (mTaskCallBack != null) {
         	mTaskCallBack.onWupTaskReceivedError(taskData, errorCode, description + ", netInfo: " + taskData.mNetType);
         }
    }
    
    /**
     *  打印错误的数据信息
     * @param datas
     */
    protected void logErrDataInfo(byte[] datas) {
        
//        if (mErrCode == WUP_TASK_ERR_SESSION_ENCRYPT) {
//            QWupLog.w(TAG, "logErrDataInfo->session encrypt fails, req data:" + QWupStringUtil.byteToHexString(datas));
//        } else if (mErrCode == WUP_TASK_ERR_SESSION_RSP_DECCRYPT_FAILE) {
//            QWupLog.w(TAG, "logErrDataInfo->session decrypt fails, rsp data:" + QWupStringUtil.byteToHexString(datas));
//        }
    }

    /**
     * 一次请求成功
     * @param response
     */
    public void onResponseEnd(byte[] response, QubeWupTaskData taskData) {
    	taskData.onDataFinished(response);
        if (mTaskCallBack != null) {
            mTaskCallBack.onWupReceivedAllDataEnd(taskData, response);
        }
    }
    
    /**
     * 创建一个wup请求对象
     * @return
     */
    private QubeWupBaseRequester createWupRequestTask() {
    	
    	QubeWupBaseRequester wupBaseRequester = null;
    	
//    	if (ApnStatInfo.isUsedProxy() || ApnStatInfo.getApnType() == ApnStatInfo.TYPE_WAP) {   // 使用代理, 或者wap
//    		// 使用 httpUrlConnection连接
//    		wupBaseRequester = new QubeWupUrlConnectTask(mTaskData);
//    	} else {
    		// 使用httpClient连接
    		wupBaseRequester = new QubeWupHttpClientRequester(mTaskData.vGuid);
//    	}
    	
    	return wupBaseRequester;
    }
    
    protected void initWupRequester() {
        
        ApnStatInfo.init(mContext);
        mWupBaseRequester = createWupRequestTask();        
    }
    
    /**
     * 处理请求返回数据
     * @param responData
     */
    protected void onRequestResponse(ResponData responData, QubeWupTaskData taskData) {
        
        if (responData != null && responData.mRspStatusCode == RESPONSE_OK) {
            String logInfo = "  QubeWupTask -- 请求成功返回 mReqType = "
                    + mTaskData.mFromModelType  + " mOperType = " + mTaskData.mOperType
                    + "  mreqId = " + mTaskData.mReqId;
            QWupLog.trace( TAG, logInfo);            
            onResponseEnd(responData.mRspDatas, taskData);
        } else {
        	mErrCode = mWupBaseRequester.mErrCode;
        	mErrMsg = mWupBaseRequester.mErrMsg;
        	if (mErrCode >=0) {
        	    mErrCode = responData ==null ? WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY : WUP_ERROR_CODE.WUP_TASK_ERR_SERVICE_RSPCODE;
        	    mErrMsg += ", reset errCode";
        	} 

            onResponseError(mErrCode,mErrMsg, taskData);
        }
    }
    
    @Override
    public void run() {
        try {
        	if (mTaskData.isWupClosed()) {
                onResponseError(WUP_ERROR_CODE.WUP_CLOSED, "wup closed", mTaskData);
           	 	return ;
        	}
            Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            
            if (!onPrepare()) {
            	return ;
            }
            
            QWupLog.i(TAG, "run");
            String loginfo = " QubeWupTask -- 发送请求     ModelType = "
                    + mTaskData.mFromModelType + " mOperType = " + mTaskData.mOperType
                    + "  mreqId = " + mTaskData.mReqId + "  url: " + mTaskData.mUrl
                    + ", servant = " +mTaskData.mReqServiceName 
                    + ", function = " + mTaskData.mReqFuncName;
            QWupLog.trace( TAG, loginfo);
            QWupLog.traceSdkI(loginfo);
            // check 请求url
            if (!Patterns.WEB_URL.matcher(mTaskData.mUrl).matches()) {  // url不合法
                mTaskData.mUrl = QWupSdkConstants.REMOTE_WUP_PROXY;
                QWupLog.trace( TAG, "run -> reqUrl is err, use default url!");
            }
            
            // 初始化请求对象
            initWupRequester();
            
            if (QWupStringUtil.isEmpty(mTaskData.mReqFuncName)) {  // functionName 为空
                UniPacket packet = QRomWupDataBuilderImpl.getuniPacket(mTaskData.mData, null);
                if (packet != null) {
                    mTaskData.mReqFuncName = packet.getFuncName();
                }
            }
            
            // 发送wup请求已更新了网络状态，这里直接获取缓存信息
            mTaskData.mNetType = QRomWupStatUtils.getCurStatNetType();
            // 发起请求，并处理回包
            onRequestResponse(mWupBaseRequester.excute(mTaskData), mTaskData);
            
            // 结束请求
            finishRequest();
        } catch (Throwable e) {
            QWupLog.trace( TAG, e);
            onResponseError(WUP_ERROR_CODE.WUP_TASK_ERR_OTHER, e.getMessage(), mTaskData);
        }
//        QWupLog.traceSdkI("QubeWupTask -- 异步请求执行完成");
        mContext = null;
    }
    
    // 请求准备
    protected boolean onPrepare() {
    	return true;
    }
    
    /**
     * 一次请求完成
     */
    protected void finishRequest() {
        mWupBaseRequester = null;
    }

 
    public void cancel() {
        mIsCancel = true;
        releaseConnect();
    }
    
    /**
     * 释放当前连接
     */
    public void releaseConnect() {
//        QWupLog.d(TAG, " releaseConnect");
        String info = "mTaskData err";
        if (mTaskData != null) {
        	info = "  mReqId = " + mTaskData.mReqId + "  mFromModelType = " + mTaskData.mFromModelType 
        			+ "   mOperType = " + mTaskData.mOperType;
        	
        }
        QWupLog.trace( TAG, "releaseConnect （超时强制取消线程） " + info );
        if (mWupBaseRequester != null) {
        	mWupBaseRequester.cancelConnect();
        }
    }
    
    public int getErrcode() {
    	return mErrCode;
    }
    
    public String getErrMsg() {
    	return mErrMsg;
    }
    
    /**
     * @return the mTaskCallBack
     */
    public IWupTaskCallBack getTaskCallBack() {
        return mTaskCallBack;
    }

    /**
     * @param mTaskCallBack the mTaskCallBack to set
     */
    public void setTaskCallBack(IWupTaskCallBack taskCallBack) {
        this.mTaskCallBack = taskCallBack;
    }

    /**
     * @return the mIsCancel
     */
    public boolean isCancel() {
        return mIsCancel;
    }
    
    public QubeWupTaskData getWupTaskData() {
    	return mTaskData;
    }
    
    public boolean sendData(QubeWupTaskData taskData) {
        return false;
    }

    public interface IWupTaskCallBack {
        
        /**
         * wup任务开始记录超时
         * 
         * @param isLongLink  是否是长连接
         * @param taskData    任务数据
         */
        public void onWupTaskStartTimeOut(QubeWupTaskData taskData);
        
        public void onWupTaskReceivedError(QubeWupTaskData taskData, int errorCode, String description);
        
        public void onWupReceivedAllDataEnd(QubeWupTaskData taskData, byte[] response);

        /**
         * wup请求重试
         * @param taskType  任务类型
         * @param taskData
         */
        public int onWupRetry(int taskType, QubeWupTaskData taskData);
    }
    
}