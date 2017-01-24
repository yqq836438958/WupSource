package qrom.component.wup.net;

import com.qq.jce.wup.UniPacket;

import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;



public class QubeWupTaskData {
    
    /** 统计用： wup请求类型 */
    public int mReqType = -1;
    public String mUrl = null;
    public int mReqId = -1;
    public int mFromModelType = 0;
    public int mOperType = -1;
    
    /** 请求数据 -- 普通请求为 UniPacket编码后的字节流 */
    public byte[] mData = null;
    
    public byte[] mOrgDatas = null;
    
    /** 请求数据包 -- 非对策加密请求数据 */
    public UniPacket mReqPkg = null;
    
    public long mTimeout = -1;
    /** 设置需重试的次数 */
    public int mRetryTime = 0;
    /** 已重试发送的次数 -- 该值 大于 mRetryTime时不再重试*/
    public int mReSendCnt = 0;
    
    /** 是否加密 */
    public boolean mIsEncrpt = false;

    /** 请求服务的servant name */
    public String mReqServiceName = null;
    /** 请求服务的function name*/
    public String mReqFuncName = null;
    /** 请求的服务模块名 */
    public String mReqAppName = null;
    
    public QRomWupReqExtraData mReqExtraData = null;

    public QRomWupRspExtraData mRspExtraData = null;
    
    public byte[] vGuid = null;
    
    
    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓统计用相关信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    /** wup统计用 -- 当前请求网络类型 */
    public String mNetType = "";
    /** wup统计用 -- 请求session */
    public String mReqSession = "";
    /** wup统计用 -- 当前ip 在缓存ip中的索引地址 by 1014.12.30*/
    public int mCurIpIndex;
    /** wup统计用 -- 当初接入点iplist的个数 by 1014.12.30*/
    public int mCurIplistSize;
    /** wup统计用 -- 拉取iplist时客户端ip(由后台下发) by 1014.12.30*/
    public String mClientIp;
    /** wup统计用 -- 当前iplist的接入点信息 by 1015.1.14*/
    public String mCurIplistApn;
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑统计用相关信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    
    private boolean mIsWupClosed = false; // 为了实现wup的关闭功能，但是目前应用程序不关心返回值，还是在回调中做更加符合逻辑
    
//    /** wup请求类型 -- 线程池/单线程请求 */
//    public int mWupType = QubeWupTask.WUP_TASK_NOMAL;
    
//    public QubeWupTaskData(byte[] datas) {
//        mOrgDatas = datas;
//    }
//    
//    public QubeWupTaskData(UniPacket reqPkg) {
//        mReqPkg = reqPkg;
//    }
    
    public void updateRetryCnt() {
        mReSendCnt++;
    }
    
    public boolean isNeedRetry() {
        
        return mReSendCnt < mRetryTime;
    }
    
    /**
     * 判断数据是否为空
     * @return
     */
    public boolean isDataEmpty() {
    	return (mData == null || mData.length == 0) && mReqPkg == null;
    }
    
    public QRomWupReqExtraData getWupReqExtraData() {
        return mReqExtraData;
    }
    
    public void setWupReqExtraData(QRomWupReqExtraData reqExtraData) {
        mReqExtraData = reqExtraData;
    }
    
    public boolean isWupClosed() {
    	return mIsWupClosed;
    }
    
    public void setWupClosed(boolean isClosed) {
    	mIsWupClosed = isClosed;
    }
    
    /**
     *  非常不想要这样的方法，当时目前的设计中，并没有对流程上有抽象化的设计
     *  这里利用这个Data结构强行承载一些多态的逻辑
     * @return
     */
    public boolean isSync() {
    	return false;
    }
    
    // 以下方法主要为同步逻辑增加
    public void onDataFinished(byte[] responseData) {
    }
    
    public void onDataError(int errorCode, String errorMsg) {
    }
    
    public void onSyncNotify() {
    }

}
