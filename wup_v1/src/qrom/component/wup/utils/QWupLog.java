/**
 * @Title: QubeLog.java
 * @Package com.tencent.qube.utils
 * @author interzhang
 * @date 2012-5-10 下午03:36:18 
 * @version V1.0
 */
package qrom.component.wup.utils;

import qrom.component.log.QRomLog;

public final class QWupLog {

    private static String TAG_SDK ="TCM SDK";
    private static String FLG_SDK ="WUP_";
    
    private static final int ERR_ROMSYS_BINDER_NULL = -1;
    private static final int ERR_ROMSYS_BINDER_ERR = -2;
    
    public static void i(String tag, String msg) {
        QRomLog.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        QRomLog.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        QRomLog.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        QRomLog.w(tag, msg);
    }
    
    public static void w(String tag, Throwable throwable) {
        QRomLog.w(tag, throwable);
    }
    
    public static void w(String tag, String msgFlg, Throwable throwable) {
        QRomLog.w(tag, msgFlg+ "->err type = " + throwable + ", err msg = " + throwable.getMessage());
    }

    public static void e(String tag, String msg) {
        QRomLog.e(tag, msg);
    }

    public static void e(String tag, Throwable throwable) {
        QRomLog.e(tag, throwable);
    }
    
    public static void trace(String tag, String msg) {
        i(tag, msg);
//        QRomLog.trace(QRomLogBaseConfig.TRACE_MODULE_WUP, tag, msg);
    }
    
    public static void trace(String tag, Throwable e) {
        w(tag, e);
//        QRomLog.trace(QRomLogBaseConfig.TRACE_MODULE_WUP, tag, e);
    }

    /**
     * guid相关提示日志
     * @param msg
     */
    public static void traceGuidInfo(String msg) {
        traceSdkI("GUID", msg);
    }
    
    /**
     * guid相关 警告提示日志
     * @param msg
     */
    public static void traceGuidWarn(String msg) {
        traceSdkW("GUID", msg);
    }
    
    /**
     * 一般提示日志
     * @param msg
     */
    public static void traceSdkI(String msg) {
        traceSdkI("BASE", msg);
    }
    /**
     * 一般型提示警告信息
     * @param msg
     */
    public static void traceSdkW(String msg) {
        traceSdkW("BASE", msg);
    }
    
    /**
     * 按模块记录提示日志
     * @param model
     * @param msg
     */
    public static void traceSdkI(String model, String msg) {
        QRomLog.w(TAG_SDK, FLG_SDK + model + ":" + msg);
    }
    
    /**
     * 按模块提示警告日志
     * @param model
     * @param msg
     */
    public static void traceSdkW(String model, String msg) {
        QRomLog.w(TAG_SDK, FLG_SDK + model + ":" + msg);
    }
    
    public static int reportBinderNull() {
        
        return reportLogInfo(ERR_ROMSYS_BINDER_NULL, "rom sys-> wup Binder null", false);
    }
    
    public static int reportBinderUseErr(String errMsg) {
        return reportLogInfo(ERR_ROMSYS_BINDER_ERR, "wup Binder: " + errMsg, false);
    }
    
    public static int reportLogInfo(int errCode, String errMsg, boolean reportFile) {
        int rsp = 0;
        try {
            if (reportFile) {                
                rsp = QRomLog.reportTraceInfoAndLogFiles("WupSdk", errCode, errMsg);
            } else {
                rsp = QRomLog.reportTraceInfoOnly("WupSdk", errCode, errMsg);
            }
        } catch (Throwable e) {
            w("REPORT_LOG", "reportLogInfo", e);
        }
        return rsp;
    }
    
}
