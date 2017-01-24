package qrom.component.wup.stat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import qrom.component.wup.QRomWupConstants;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.net.QubeWupTask.WupTaskType;
import qrom.component.wup.net.QubeWupTaskData;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.sync.security.StatisticListener;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.BASEINFO_ERR_CODE;
import qrom.component.wup.utils.QWupStringUtil;
import android.content.Context;


public class QRomWupStatEngine implements StatisticListener {
    

    private String TAG = "QRomWupStatEngine";
    /** 统计sdk 接口类名*/
    private String STAT_IMPL_CLASS_NAME = "qrom.component.statistic.QStatExecutor";
 
    /** 统计sdk 统计wup数据接口名 --  参数 (Map<String, String>)*/
    private String STAT_IMPL_FUNCTION_STAT_WUP = "triggerWupMonitorData";
    /** 统计sdk 初始化数据接口名 --  参数 (context)*/
    private String STAT_INIT_FUNCTION_INIT = "init";
    /** 统计sdk 获取qimei --  参数 ()*/
    private String STAT_IMPL_FUNCTION_GETQIMEI = "getQIMEI";
    
    
    private static QRomWupStatEngine mInstance;
    /** 统计sdk是否存在 */
    private boolean mIsStatSdkExist = true;
    private static byte[] mLock = new byte[0];
    
    
    /** 统计接口反射类 */
    private Class<?> mStatClassImpl = null;
    /** 统计wup方法接口 */
    private Method mStatWupMethod = null;
    /** 获取qimei方法接口 */
    private Method mGetQimeMethod = null;
    
    private QRomWupStatEngine() {
        
    }
    
    public static QRomWupStatEngine getInstance() {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new QRomWupStatEngine();
                }
            }
        }
        
        return mInstance;
    }
    
    /**
     * 初始化统计sdk
     * @param context
     * @return
     * @throws Exception
     */
    private boolean initStatMode(Context context) throws Exception {
    
        if (context == null) {
            return false;
        }
        
        if (mStatClassImpl == null) {                
            mStatClassImpl = Class.forName(STAT_IMPL_CLASS_NAME);
            Method initMethod = mStatClassImpl.getMethod(STAT_INIT_FUNCTION_INIT, new Class[] {Context.class});
            initMethod.invoke(mStatClassImpl, new Object[] {context});
            mIsStatSdkExist = true;
            
            mStatWupMethod = mStatClassImpl.getMethod(STAT_IMPL_FUNCTION_STAT_WUP, new Class[] {Map.class});
            mGetQimeMethod = mStatClassImpl.getMethod(STAT_IMPL_FUNCTION_GETQIMEI);
        }
        
        return true;    
    }

    /**
     * 统计wup相关数据
     * @param context
     * @param info
     * @return
     */
    public int statWupRequestInfo(Context context, Map<String, String> info) {
        
        if (context == null || info == null || info.isEmpty()) {
            QWupLog.trace(TAG, "statWupRequestInfo -> stat info is empty");
            return -91;
        }
        // 添加sdk版本信息
        info.put(WUP_STAT_KEY.WUP_STAT_SDK_VER, QRomWupConstants.WUP_SDK_VER);
        synchronized (mLock) {
            try {
                if (mIsStatSdkExist && initStatMode(context)) {  // 初始化统计环境
                    QWupLog.trace(TAG, "statWupRequestInfo -> mStatWupMethod invoke");
                    mStatWupMethod.invoke(mStatClassImpl, new Object[] {info});
                }
                QWupLog.trace(TAG, "statWupRequestInfo -> mStatWupMethod end");
                return 1;
            } catch (ClassNotFoundException e) {
                // 无统计class 类
                mIsStatSdkExist = false;
                mStatClassImpl = null;
                mStatWupMethod = null;
                mGetQimeMethod = null;
                QWupLog.trace(TAG, "statWupRequestInfo -> ClassNotFoundException err msg: " + e.getMessage() + ", e : " + e);
            } catch (Throwable e) {
                mStatClassImpl = null;
                mStatWupMethod = null;
                mGetQimeMethod = null;
                QWupLog.trace(TAG, "statWupRequestInfo -> err msg: " + e.getMessage() + ", e : " + e);
            }
        }        
        
        return 0;
    }
    
  public String statGetQimei(Context context) {
        
        if (context == null ) {
            QWupLog.trace(TAG, "statGetQimei -> context is null");
            return null;
        }
        
        String qimei = null;
        synchronized (mLock) {
            try {
                if (mIsStatSdkExist && initStatMode(context)) {  // 初始化统计环境
                    QWupLog.trace(TAG, "statGetQimei -> mStatWupMethod invoke");
                    qimei = (String) mGetQimeMethod.invoke(mStatClassImpl);
                } else if (!mIsStatSdkExist) {
                    qimei = BASEINFO_ERR_CODE.QIME_NO_FIND_STAT;
                }
                
                QWupLog.trace(TAG, "statGetQimei ->  end, " + qimei);
                return qimei;
            } catch (ClassNotFoundException e) {
                // 无统计class 类
                mIsStatSdkExist = false;
                mStatClassImpl = null;
                mStatWupMethod = null;
                mGetQimeMethod = null;
                qimei = BASEINFO_ERR_CODE.QIME_NO_FIND_STAT;
                QWupLog.trace(TAG, "statGetQimei -> ClassNotFoundException err msg: " + e.getMessage() + ", e : " + e);
            } catch (Throwable e) {
                mStatClassImpl = null;
                mStatWupMethod = null;
                mGetQimeMethod = null;
                QWupLog.trace(TAG, "statGetQimei -> err msg: " + e.getMessage() + ", e : " + e);
            }
        }        
        
        return qimei;
    }
    
    /**
     * 统计wup请求成功信息
     * @param context
     * @param taskData
     * @return
     */
    public int statWupStatSucessDatas(Context context, QubeWupTaskData taskData) {
        
        if (taskData == null || QWupStringUtil.isEmpty(taskData.mReqServiceName)) {
            QWupLog.trace(TAG, "statWupStatSucessDatas -> stat info is empty");
            return -1;
        }
        
        if (!isNeedStat(taskData.vGuid)) {
            QWupLog.trace(TAG, "statWupStatSucessDatas -> no need report wup info");
            return -2;
        }

        QWupLog.trace(TAG, "statWupStatSucessDatas -> start");
        Map<String, String> info = new HashMap<String, String>(12);
        // 更新基本信息
        setWupStatBaseInfo(info, taskData, WUP_STAT_CONSTANTS.WUP_RESULT_SUCCESS);
        
        // 请求成功额外响应 -- 请求各项时间
        QRomWupRspExtraData rspExtraData = taskData.mRspExtraData;
        if (rspExtraData != null) {                
            if (rspExtraData.mConnectTime < 0) {
                rspExtraData.mConnectTime = 0;
            }
            if (rspExtraData.mSendTime < 0) {
                rspExtraData.mSendTime = 0;
            }
            if (rspExtraData.mGetDataTime < 0) {
                rspExtraData.mGetDataTime = 0;
            }
            info.put(WUP_STAT_KEY.WUP_STAT_TIME_CONNECT, String.valueOf(rspExtraData.mConnectTime));
            info.put(WUP_STAT_KEY.WUP_STAT_TIME_SEND, String.valueOf(rspExtraData.mSendTime));
            info.put(WUP_STAT_KEY.WUP_STAT_TIME_RECEIVE, String.valueOf(rspExtraData.mGetDataTime));
            info.put(WUP_STAT_KEY.WUP_STAT_REQDATA_LEN, String.valueOf(rspExtraData.mReqDataLen));
            info.put(WUP_STAT_KEY.WUP_STAT_RSPDATA_LEN, String.valueOf(rspExtraData.mRspDataLen));
        } else { // 无响应数据
            info.put(WUP_STAT_KEY.WUP_STAT_TIME_CONNECT, "NA");
            info.put(WUP_STAT_KEY.WUP_STAT_TIME_SEND, "NA");
            info.put(WUP_STAT_KEY.WUP_STAT_TIME_RECEIVE, "NA");
            info.put(WUP_STAT_KEY.WUP_STAT_REQDATA_LEN, "NA");
            info.put(WUP_STAT_KEY.WUP_STAT_RSPDATA_LEN, "NA");
        }
        
        return statWupRequestInfo(context, info);
    }
    
    /**
     * 统计wup请求失败信息
     * @param context
     * @param taskData
     * @param errcode
     * @param errMsg
     * @return
     */
    public int statWupStatFailsDatas(Context context, QubeWupTaskData taskData, int errcode, String errMsg) {

        
        if (taskData == null || QWupStringUtil.isEmpty(taskData.mReqServiceName)) {
            QWupLog.trace(TAG, "statWupStatFailsDatas -> stat info is empty");
            return -1;
        }
                
        if (!isNeedStat(taskData.vGuid)) {
            QWupLog.trace(TAG, "statWupStatFailsDatas -> no need report wup info");
            return -2;
        }
        
        QWupLog.trace(TAG, "statWupStatFailsDatas -> start");
       
        Map<String, String> info = new HashMap<String, String>(10);
        // 更新基本信息
        setWupStatBaseInfo(info, taskData, WUP_STAT_CONSTANTS.WUP_RESULT_FAIL);
        
        if (errcode == WUP_ERROR_CODE.WUP_TASK_ERR_EXCEPTION
                || errcode == WUP_ERROR_CODE.WUP_TASK_ERR_SERVICE_RSPCODE) {
            // 请求的url
            info.put(WUP_STAT_KEY.WUP_STAT_REQ_URL, taskData.mUrl);
            // 校验请求数据
           String checkInfo = new QRomNetCheck().checkServiceStat(context);
           // wup统计用 -- httpservice校验信息 
           if (!QWupStringUtil.isEmpty(checkInfo)) {
               info.put(WUP_STAT_KEY.WUP_STAT_CHECK_FAIL, checkInfo);
           }
        }

        String tempApn = taskData.mCurIplistApn;
        if (QWupStringUtil.isEmpty(tempApn)) {
            tempApn = "na";
        } else {
            tempApn = tempApn.replace("_", "-");
        }
        // 增加失败是当前iplist的信息
        info.put(WUP_STAT_KEY.WUP_STAT_IPLIST_INFO, taskData.mCurIpIndex 
                + "_" + taskData.mCurIplistSize + "_"+ tempApn);

        String clientIp = taskData.mClientIp;
        if (QWupStringUtil.isEmpty(clientIp)) {
            clientIp = "na";
        }
        info.put(WUP_STAT_KEY.WUP_STAT_IPLIST_CLENT_IP, clientIp);
        if (taskData.mRspExtraData != null) {  // 请求数据长度
            info.put(WUP_STAT_KEY.WUP_STAT_REQDATA_LEN, String.valueOf(taskData.mRspExtraData.mReqDataLen));
        }
        
        String errInfo = errcode + ". " +errMsg;
        info.put(WUP_STAT_KEY.WUP_STAT_EXTRA, errInfo);
        
        return statWupRequestInfo(context, info);
    }
    
    /**
     * 强制统计wup相关信息，不进行用户抽样
     * @param context
     * @param info
     * @return
     */
    public int statWupRequestInfoForce(Context context, Map<String, String> info) {
        return statWupRequestInfo(context, info);
    }
    
    private String getET(int taskType) {
        switch (taskType) {
        case WupTaskType.WUP_TASK_TYPE_NORMAL:
            return WUP_STAT_CONSTANTS.WUP_ENCRYT_TYPE_NORMAL;
        case WupTaskType.WUP_TASK_TYPE_ASYM_ENCRYPT:
            return WUP_STAT_CONSTANTS.WUP_ENCRYT_TYPE_ASYM;
        default:
            break;
        }
        return WUP_STAT_CONSTANTS.WUP_ENCRYT_TYPE_OTHER;
    }

    /**
     * 设置wup统计基本统计数据
     * @param info
     * @param taskData
     * @param resultCode
     */
    private void setWupStatBaseInfo(Map<String, String>info, QubeWupTaskData taskData, String resultCode) {
        
        if (info == null) {
            return;
        }
        
        String funName = taskData.mReqFuncName;
        if (QWupStringUtil.isEmpty(funName)) {
            funName = taskData.mFromModelType + "." +  taskData.mOperType;
        }
        
        info.put(WUP_STAT_KEY.WUP_STAT_SERVANT, taskData.mReqServiceName);
        info.put(WUP_STAT_KEY.WUP_STAT_FUNCTION, funName);
        info.put(WUP_STAT_KEY.WUP_STAT_ENCRYPT_TYPE, getET(taskData.mReqType));
        // 执行结果
        info.put(WUP_STAT_KEY.WUP_STAT_RESULT, resultCode);
        // 添加请求网络类型
        info.put(WUP_STAT_KEY.WUP_STAT_REQ_NETSTAT, String.valueOf(taskData.mNetType));        
        // 添加请求session
        info.put(WUP_STAT_KEY.WUP_STAT_SESSION, taskData.mReqSession);       
    }
    
    private boolean isNeedStat(byte[] guid) {
        int checkFlg = 0;
        if (guid != null && guid.length >0) {
            checkFlg = guid[guid.length -1] & 0Xff;
        }
        
        checkFlg = checkFlg % 100;
        
        int dayFlg = (int) (System.currentTimeMillis() / QWupSdkConstants.MILLIS_FOR_DAY) % 100;
        return checkFlg == dayFlg;
    }

    @Override
    public void onStartDuration(String packageName, long duration) {
        Map<String, String> info = new HashMap<String, String>(1);
        info.put(WUP_STAT_KEY.WUP_STAT_ASYM_APP_PACKAGE, packageName);
        info.put(WUP_STAT_KEY.WUP_STAT_ASYM_START_DURATION, String.valueOf(duration));
        
        statWupRequestInfo(QRomWupImplEngine.getInstance().getContext(), info);
    }

    @Override
    public void onVerfiyAppKeyResult(String packageName, boolean sucess) {
        Map<String, String> info = new HashMap<String, String>(1);
        String res = WUP_STAT_CONSTANTS.WUP_RESULT_FAIL;
        if (sucess) {
            res = WUP_STAT_CONSTANTS.WUP_RESULT_SUCCESS;
        }
        info.put(WUP_STAT_KEY.WUP_STAT_ASYM_APP_PACKAGE, packageName);
        info.put(WUP_STAT_KEY.WUP_STAT_ASYM_APPKEY, res);
        
        statWupRequestInfo(QRomWupImplEngine.getInstance().getContext(), info);
    }

    @Override
    public void onVerifyRootKeyResult(String packageName, boolean sucess) {
        
        Map<String, String> info = new HashMap<String, String>(1);
        String res = WUP_STAT_CONSTANTS.WUP_RESULT_FAIL;
        if (sucess) {
            res = WUP_STAT_CONSTANTS.WUP_RESULT_SUCCESS;
        }
        info.put(WUP_STAT_KEY.WUP_STAT_ASYM_APP_PACKAGE, packageName);
        info.put(WUP_STAT_KEY.WUP_STAT_ASYM_ROOTKEY, res);
        
        statWupRequestInfo(QRomWupImplEngine.getInstance().getContext(), info);
    }
    
    class WUP_STAT_KEY {
        /** wup 统计key -- SDK版本信息*/
        public static final String WUP_STAT_SDK_VER = "VER";
        /** wup 统计key -- 请求服务*/
        public static final String WUP_STAT_SERVANT = "ST";
        /** wup 统计key -- 请求功能*/
        public static final String WUP_STAT_FUNCTION = "FN";
        /** wup 统计key -- 加密类型 */
        public static final String WUP_STAT_ENCRYPT_TYPE = "ET";
        /** wup 统计key -- 请求结果 */
        public static final String WUP_STAT_RESULT = "RS";
        /** wup 统计key -- 连接时间*/
        public static final String WUP_STAT_TIME_CONNECT = "CO";
        /** wup 统计key -- 发送时间 */
        public static final String WUP_STAT_TIME_SEND = "SD";
        /** wup 统计key -- 接收时间 */
        public static final String WUP_STAT_TIME_RECEIVE = "RE";
        /** wup 统计key -- 请求数据长度 */
        public static final String WUP_STAT_REQDATA_LEN = "RDL";
        /** wup 统计key -- 响应数据长度 */
        public static final String WUP_STAT_RSPDATA_LEN = "SDL";
        /** wup 统计key -- 额外信息 */
        public static final String WUP_STAT_EXTRA = "EA";
        /** wup 统计key -- 非对策加密包名 */
        public static final String WUP_STAT_ASYM_APP_PACKAGE = "AP";
        /** wup 统计key --非对称加密root key */
        public static final String WUP_STAT_ASYM_ROOTKEY = "RK";
        /** wup 统计key --非对称加密app key */
        public static final String WUP_STAT_ASYM_APPKEY = "AK";
        /** wup 统计key -- 非对称加密app key */
        public static final String WUP_STAT_ASYM_START_DURATION = "PSD";
        /** wup 统计key -- 非对称加密 解密接口 */
        public static final String WUP_STAT_ASYM_DECRYPT = "DK";
        /** wup 统计key --请求网络状态 */
        public static final String WUP_STAT_REQ_NETSTAT = "NWT";
        /** wup 统计key --请求服务器url */
        public static final String WUP_STAT_REQ_URL = "SURL";
        /** wup 统计key --请求Session */
        public static final String WUP_STAT_SESSION = "SEN";
        /** wup 统计key --校验网络请求 */
        public static final String WUP_STAT_CHECK_FAIL = "NCK";
        /** wup 统计key --请求ip的索引等信息 */
        public static final String WUP_STAT_IPLIST_INFO = "SIP";
        /** wup 统计key -- 请求iplist时客户端ip */
        public static final String WUP_STAT_IPLIST_CLENT_IP = "CIP";
    }
    
    class WUP_STAT_CONSTANTS {
        
        /** wup 统计常量-- 加密类型：普通 */
        public static final String WUP_ENCRYT_TYPE_NORMAL = "0";
        /** wup 统计常量-- 加密类型：非对策加密 */
        public static final String WUP_ENCRYT_TYPE_ASYM = "1";
        /** wup 统计常量-- 加密类型：其他类型 */
        public static final String WUP_ENCRYT_TYPE_LONG = "2";
        /** wup 统计常量-- 加密类型：其他类型 */
        public static final String WUP_ENCRYT_TYPE_OTHER = "9";
        
        /** wup 统计常量-- 请求结果 ： 成功 */
        public static final String WUP_RESULT_SUCCESS = "1";
        /** wup 统计常量-- 请求结果 ： 失败 */
        public static final String WUP_RESULT_FAIL = "0";
    }
    
}
