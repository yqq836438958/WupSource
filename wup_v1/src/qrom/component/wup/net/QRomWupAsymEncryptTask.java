package qrom.component.wup.net;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.runInfo.WupAppProtocolBuilder;
import qrom.component.wup.sync.security.AsymCipherManager;
import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupZipUtils;
import TRom.SECPROXY_RETCODE;
import TRom.SecureRsp;
import android.content.Context;

import com.qq.jce.wup.UniPacket;

/**
 * 非对策加密task
 * @author sukeyli
 *
 */
public class QRomWupAsymEncryptTask extends QubeWupTask {

    /** session获取失败时，默认重新获取session次数 */
    private static final int RETRY_CNT_SESSION_UPDATE = 1;
    /** 服务器返回session 超时，默认重试次数 */
    private static final int RETRY_CNT_DEFAULT_SESSION_TIMEIOUT = 1;
    
    protected String mSession;
    
    public QRomWupAsymEncryptTask(Context context, QubeWupTaskData taskData, IWupTaskCallBack taskCallBack, String wupProxyAddress) {
        super(context, taskData, taskCallBack, wupProxyAddress);
        TAG = "QRomWupAsymEncryptTask";
    }
    
    public QRomWupAsymEncryptTask(Context context, QubeWupTaskData taskData) {
        super(context, taskData);
        TAG = "QRomWupAsymEncryptTask";
    }
    
    @Override
    protected boolean onPrepare() {
    	 try {
             if (!preAsymReqData()) {  // 预处理数据失败
                 QWupLog.w(TAG, "run -> preAsymReqData fails...");
                 return false;
             }
         } catch (Exception e) {
             mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_REQ_DATA_FAILE;
             mErrMsg =  e.getMessage();
             QWupLog.w(TAG, e);
             QWupLog.trace(TAG, "preAsymReqData-> err msg: " + mErrMsg);
             QWupLog.trace(TAG, "preAsymReqData-> err data: \n" + QWupStringUtil.byteToHexString(mTaskData.mData));
             onResponseError(mErrCode, mErrMsg, mTaskData);
             return false;
         }
    	 return true;
    }
        
    @Override
    public void onResponseEnd(byte[] response, QubeWupTaskData taskData) {
        
        UniPacket proxyPkg = QRomWupDataBuilderImpl.getuniPacket(response, null);
        // 后台返回码
        int rspcode = QRomWupDataBuilderImpl.getUniPacketResult(proxyPkg);
        
        QWupLog.trace(TAG, "onResponseEnd -> rspcode  = " + rspcode);
        
        if (rspcode == SECPROXY_RETCODE._SP_RC_OK) {  // 请求成功
            // 解析wup包
            SecureRsp secureRsp = (SecureRsp) QRomWupDataBuilderImpl.getJceStructFromPkg(proxyPkg, "stRsp");
            if (secureRsp == null) { // 解析失败
                mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_RSP_PROXY_PKG_PARSE;
                mErrMsg = "QRomWupAsymEncryptTask-> parse wup proxy SecureRsp err ";   
                // 返回app失败
                onResponseError(mErrCode, mErrMsg, getWupTaskData());
                return;
            }
            
            QWupLog.trace(TAG, "AsymCipherManager().decrypt -> sessionId = " + mSession 
                    + ", secureRsp.iret = " + secureRsp.iRet + " , bzip = " + secureRsp.bZip  
                    + ", encrypt rsp len = " + (secureRsp.vRealRsp == null ? -1 : secureRsp.vRealRsp.length));
            // 用 session 解密数据
            byte[] decryptDatas = getAsymCipherManager().decrypt(mSession, secureRsp.vRealRsp);   
            if (decryptDatas == null || decryptDatas.length == 0) {
                mErrMsg = "_SP_RC_OK -> AsymCipherManager.decrypt failed";
                mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_RSP_DECCRYPT_FAILE;
                // TODO 测试用失败解码数据
//                testLogData = secureRsp.vRealRsp;
                // 返回app失败
                onResponseError(mErrCode, mErrMsg, getWupTaskData());
                return;
            }
            
            if (secureRsp.bZip) {  // 压缩数据
                // 解压数据
                decryptDatas = QWupZipUtils.unGzip(decryptDatas);
                if (decryptDatas == null) {
                    mErrMsg = "_SP_RC_OK -> unGzip failed";
                    mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_RSP_DECODE_FAILE;
                    // 返回app失败
                    onResponseError(mErrCode, mErrMsg, getWupTaskData());
                    return;
                }
            }  // ~ 解压完成
            
            // 解码成功，通知app请求成功，回调处理
            super.onResponseEnd(decryptDatas, taskData);
            return;            
        }  // ~ end 返回码成功处理完成
        
        // 返回码错误
        mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_RSP_CODE_FAILE;
        // 请求错误
        switch (rspcode) {
        case SECPROXY_RETCODE._SP_RC_SESSION_OUTDATE: // session过期，请重新beginSession
            // 重置session通道
            getAsymCipherManager().resetSession(getAppPkgName());
            // 重试
            if (taskData.mRetryTime <= 0) {  // session超时默认重试1次
                taskData.mRetryTime = RETRY_CNT_DEFAULT_SESSION_TIMEIOUT;
            }
            if (reTrySendRequest(taskData)) {  // 正在重试 
                QWupLog.w(TAG, "onResponseEnd ->_SP_RC_SESSION_OUTDATE, retrying ");
                return;
            }
            mErrMsg = "rsp code: session过期";
            break;
        case SECPROXY_RETCODE._SP_RC_UNREGIST_PACKAGE:  //未注册的包名 [需要告知后台配置包名]   
            mErrMsg = "rsp code: 未注册的包名";
            break;
        case SECPROXY_RETCODE._SP_RC_INVALID_SERVANT:  //servant非法  [需要后台配置]
            mErrMsg = "rsp code: servant非法";
            break;
            
        case SECPROXY_RETCODE._SP_RC_DECRYPT_FAILED: //解码失败
            mErrMsg = "rsp code: 解码失败";
            break;
        case SECPROXY_RETCODE._SP_RC_SESSION_PARAM_ERROR:   //session参数有误 [包名对应错误]
            mErrMsg = "rsp code: session参数有误";
            break;
        case SECPROXY_RETCODE._SP_RC_SERVER_ERR:
            mErrMsg = "rsp code: SERVER_ERR";
            break;
        case SECPROXY_RETCODE._SP_RC_INVALID_GUID: // guid 不合法
            mErrMsg = "rsp code: guid非法";
            break;
        case SECPROXY_RETCODE._SP_BUS_SERVER_FAILED:  // 业务调用失败
            mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_BUS_SERVICE_FAILE;
            mErrMsg = "rsp code: _SP_BUS_SERVER_FAILED";
            break;
        case SECPROXY_RETCODE._SP_RC_ZIP_FAILED:  // 服务器解压失败
            mErrMsg = "rsp code: _SP_RC_ZIP_FAILED";
            break;
        default:
            break;
        }
        // 通知app 失败
        onResponseError(mErrCode,mErrMsg, taskData);
    } 
    
    protected boolean preAsymReqData() {
        // 预处理数据 -- 获取session加密后的数据
        byte[] datas = onPreProcessDatas();
        if (datas == null || datas.length == 0) {  // 数据处理失败
            if (mErrCode >= 0) {
                mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_REQ_DATA_FAILE;
                mErrMsg = "QRomWupAsymEncryptTask -> session proxyPkg encode fails";
            }
            
            // 返回app失败
            onResponseError(mErrCode, mErrMsg, getWupTaskData());
            return false;
        }
        
//        // 组装非对策加密的中转服务请求
//        UniPacket proxyPkg = WupAppProtocolBuilder.createAsymEncryptProxyReqData(
//                mSession, mTaskData.vGuid, getAppPkgName(), datas);
//        proxyPkg.setRequestId(mTaskData.mReqId);
        // 编码代理请求数据包，并设置发送的数据
        mTaskData.mData = datas;        
        return true;
    }
    
    /**
     * 同步发送一次数据
     * @return
     */
//    protected UniPacket sendSynRequestForOneTime() {
//        
//        if (!preAsymReqData()) {  // 预处理数据失败
//            QWupLog.w(TAG, "runSyn -> preAsymReqData fails...");
//            return null;
//        }
//        // 加密代理服务器返回的数据
//        byte[] proxyRspDatas = super.runSyn();
//        QWupLog.trace(TAG, "sendSynRequestForOneTime -> rsp data len = " 
//            + (proxyRspDatas == null ? -1 : proxyRspDatas.length));
//        UniPacket proxyPkg = QRomWupDataBuilderImpl.getuniPacket(proxyRspDatas, null);
//        
//        return proxyPkg;
//    }
     
    
    protected void reSetRetryTaskData(QubeWupTaskData taskData) {
        // 重置数据 -- 数据需使用新的加密session
        taskData.mData = null;
        // 更新重试次数
        taskData.updateRetryCnt();
    }
    
    protected boolean reTrySendRequest(QubeWupTaskData taskData) {
        
        if (mTaskCallBack == null || !taskData.isNeedRetry()) {
            QWupLog.w(TAG, "reTrySendRequest -> no need resend...");
            return false;
        }
        // 重置重试数据
        reSetRetryTaskData(taskData);
        // 重试
        int reqId = mTaskCallBack.onWupRetry(WupTaskType.WUP_TASK_TYPE_ASYM_ENCRYPT, taskData);
        return reqId > 0;
    }
    
    /**
     * 预处理数据
     */
    protected byte[] onPreProcessDatas() {

        String session = null;
        QWupLog.trace(TAG, "onPreProcessDatas -> start reqId = " + mTaskData.mReqId);
        // 通知app失败
        int retryCnt = RETRY_CNT_SESSION_UPDATE;

        String appName = getAppPkgName();
        if (QWupStringUtil.isEmpty(session)) { // session 获取失败
            for (int i = 0; i < retryCnt; i++) {
                // 获取session
                session = getAsymCipherManager().getSessionId(appName);
                QWupLog.trace(TAG, "onPreProcessDatas -> session = " + session 
                        +  ", reqId = " + mTaskData.mReqId + ", app pkgName = " + appName);
                if (!QWupStringUtil.isEmpty(session)) { // 获取到session
                    break;
                } // ~ 重试后期可以加延时逻辑
            } // ~ end 重试获取session
        }

        if (QWupStringUtil.isEmpty(session)) { // session 获取不对
            mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_GET_SESSION_FAIL;
            mErrMsg = "onPreProcessDatas -> get session fails, app pkgName = " + appName;
            return null;
        } // ~ session处理完毕
        
        // 保存session
        mSession = session;
        // 原始数据
        byte[] orgDatas = QRomWupDataBuilderImpl.parseWupUnipackage2Bytes(mTaskData.mReqPkg);
        int orgLen =  orgDatas == null ? -1 : orgDatas.length;
//        boolean isNeedZip = orgLen > 500 ? true : false;
        boolean isNeedZip = true;
        if (isNeedZip) {            
            // 压缩数据
            orgDatas = QWupZipUtils.gZip(orgDatas);
        }
        if (orgDatas == null) {
            mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_REQ_DATA_FAILE;
            mErrMsg = "onPreProcessDatas -> data zip fails, bzip: " + isNeedZip;
            return null;
        }
        QWupLog.trace(TAG, "onPreProcessDatas -> reqId = " + mTaskData.mReqId 
                + ", iszip = " + isNeedZip + ", org data len = " + orgLen 
                + ", gzip len = " + (orgDatas == null ? -1 : orgDatas.length));
        // 用session 加密数据
        byte[] encrytData = getAsymCipherManager().encrypt(mSession, orgDatas);
        
        if (encrytData == null) {
            mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_ENCRYPT;
            mErrMsg = "onPreProcessDatas -> session encrypt fails";
//            testLogData = orgDatas;
            return null;
        }
     
        // 组装非对策加密的中转服务请求
        UniPacket proxyPkg = WupAppProtocolBuilder.createAsymEncryptProxyReqData(
                mSession, mTaskData.vGuid, getAppPkgName(), encrytData, isNeedZip);
        proxyPkg.setRequestId(mTaskData.mReqId);
        // 编码代理请求数据包，并设置发送的数据
        encrytData = QRomWupDataBuilderImpl.parseWupUnipackage2Bytes(proxyPkg);     
        
        return encrytData;
    }
    

//    /**
//     * 获取session 相关信息 
//     * @return sessionId 
//     */
//    protected String onProcessSession() {
//        
////        String servant = mTaskData.mReqServiceName;
////        String function = mTaskData.mReqFuncName;
//        String session = null;
//        // 通过servant和function判断通道是否建立
//        session = getAsymCipherManager().getSessionId(getAppPkgName());
//        
//        return session;
//    }
    
    private AsymCipherManager getAsymCipherManager() {
        return  QRomWupImplEngine.getInstance().getAsymCipherManager();
    }

    private String getAppPkgName() {
        if (QWupStringUtil.isEmpty(mTaskData.mReqAppName)) {
            return QRomWupBuildInfo.getAppPackageName();
        }
        return mTaskData.mReqAppName;
    }
}
