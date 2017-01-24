package qrom.component.wup.runInfo;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.net.QubeWupNetEngine;
import qrom.component.wup.net.QubeWupTask;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupLog;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Handler.Callback;

public class QRomWupTimeoutManager implements Callback{

    private final String TAG = "QRomWupTimeoutManager";
    
    /** wup超时计时开始 -- 目前公用notify进程统一的超时线程计时 */
    private static final int MSG_WUP_TIMEOUT_REQ = 121;
    /** 任务超时后处理 -- 回调处理具体wup超时线程 */
    private static final int MSG_WUP_TIMEOUT_CALLBACK = 122;
    
    /** wup请求计时线程handler */
    private Handler mWupTimeoutHandler;
    private HandlerThread mTimeoutThread = null;
    
    
    /** 处理timeout回调后续操作 -- handler*/
    private Handler mTimeoutCallbackHandler = null;
    /** 超时后回调处理线程 */
    private HandlerThread mTimeoutCallbackThread = null;
    
    protected QRomWupTimeoutManager() {
    	
    }
    
    public void startUp() {
        initTimeoutHandler();
    }
    
    public Looper getTimeoutLooper() {
    	if (mTimeoutThread == null) {
    	    initTimeoutHandler();
    	}
    	return mTimeoutThread.getLooper();
    }
    
    /**
     * 初始化超时线程handler
     *       
     */
    private synchronized void initTimeoutHandler() {
        
        if (mTimeoutThread == null) {
            mTimeoutThread = new HandlerThread(TAG);
            mTimeoutThread.start();
        }
        
        if (mWupTimeoutHandler == null) {
            Looper  looper = mTimeoutThread.getLooper();
            if (looper != null) {
                QWupLog.d(TAG, "-----initTimeoutHandler------");
                mWupTimeoutHandler = new Handler(looper, this);
            }
        }
    }
    
    /**
     * 初始化超时回调处理线程
     *    -- 这里另起一个线程，避免回调操作阻塞计时线程
     */
    private void initWupTimeoutCallBackThread() {
        if (mTimeoutCallbackHandler == null) {
            QWupLog.i(TAG, "initWupTimeoutCallBackThread");
            mTimeoutCallbackThread = new HandlerThread("WupTimeoutCallback");
            mTimeoutCallbackThread.start();
            mTimeoutCallbackHandler = new Handler(mTimeoutCallbackThread.getLooper(), this);
        }
    }
    
    
    public void startWupTimeoutRequest(int reqId, long timeout) {
    	
    	initTimeoutHandler();
    	/*
         * msg 中 what用taskid作为what区分
         *             arg1 记录超时操作类型
         */
        Message message = mWupTimeoutHandler.obtainMessage(reqId);
        message.arg1 = MSG_WUP_TIMEOUT_REQ;
        
        // 延时机制时间稍稍放宽2s，handler是软计时超时可能执行超时操作时比设置时间早了 或者数据刚好返回，优先响应数据
        mWupTimeoutHandler.sendMessageDelayed(message, timeout + 2 * QWupSdkConstants.MILLIS_FOR_SECOND);
    }
    
    /**
     * 取消对应的超时记录
     * @param reqId
     */
    public void removeWupTimeoutRequest(int reqId) {
        QWupLog.trace( TAG,  
                "removeWupTimeoutRequest reqId = " + reqId);
        if (mWupTimeoutHandler == null) {
            return;
        }

        Message message = mWupTimeoutHandler.obtainMessage(reqId);
        message.arg1 = MSG_WUP_TIMEOUT_REQ;
        mWupTimeoutHandler.removeMessages(reqId);
    }
    
    /**
     * 处理请求超时回调操作
     * @param reqId
     */
    private void onWupRequestTimeout(int reqId) {
        if (mTimeoutCallbackHandler != null) {
            QWupLog.i(TAG, "onWupRequestTimeout -> reqId = " + reqId);
            Message msg = mTimeoutCallbackHandler.obtainMessage(reqId);
            msg.arg1 = MSG_WUP_TIMEOUT_CALLBACK;
            // 有的手机延时超时后会自动将网络断掉，抛出exception，这里延时1s，若系统不处理超时，则强制回调超时处理
            mTimeoutCallbackHandler.sendMessageDelayed(msg, 1000);
        }
    }
    
    private void onWupTaskTimeout(int reqId) {
        if (QubeWupNetEngine.getInstance() != null 
                && QubeWupNetEngine.getInstance().foceCloseConnect(reqId)) {
            QWupLog.trace( TAG, 
                    "WupTimeoutThread -> timeout handleMessage -- foceCloseConnect reqID =  " + reqId);
            // 初始化超时处理超时操作工作线程
            initWupTimeoutCallBackThread();
            // 发送超时后续处理操作
            onWupRequestTimeout(reqId);
        }       
    }
    
    private void onForceCancelWupTask(int reqId) {
        QubeWupTask cancelTask = QubeWupNetEngine.getInstance().cancelTask(reqId);
        String timeoutInfo = "WupTimeoutThread -> onForceCancelWupTask  -- 强制回调 timeout err reqID =  " 
                + reqId + "   cancelTask = " + cancelTask;
        QWupLog.trace( TAG, timeoutInfo);
        if (cancelTask != null && cancelTask.getTaskCallBack() != null) {  // 找到对应的请求
            
            // 回调失败
            cancelTask.getTaskCallBack().onWupTaskReceivedError(
                    cancelTask.getWupTaskData(), 
                    WUP_ERROR_CODE.WUP_TASK_ERR_FORCE_TIMEOUT, "network is force timeout");

        }
    }
    
    
    @Override
    public boolean handleMessage(Message msg) {
        int reqId = msg.what;
        int msgType = msg.arg1;
        QWupLog.d(TAG, "====handleMessage   " + reqId);
        switch (msgType) {  // 超时类型
        case MSG_WUP_TIMEOUT_REQ:  // 请求任务超时 -- 开始
            QWupLog.d(TAG, "WupTimeoutThread -> timeout handleMessage -- cancel reqID =  " + reqId);            
            onWupTaskTimeout(reqId);
            break;
        case MSG_WUP_TIMEOUT_CALLBACK:  // 超时-- 强制取消任务        
            QWupLog.trace(TAG, "wup timeout callback msg");
            onForceCancelWupTask(reqId);
            break;

        default:
            break;
        }
        
        return false;
    }
    
    public void release() {
    	
        if (mTimeoutThread != null) {
            mTimeoutThread.quit();
            mTimeoutThread = null;
            mWupTimeoutHandler = null;
        }
        
        if (mTimeoutCallbackThread != null) {
        	mTimeoutCallbackThread.quit();
        	mTimeoutCallbackThread = null;
        	mTimeoutCallbackHandler = null;
        }
    }
}
