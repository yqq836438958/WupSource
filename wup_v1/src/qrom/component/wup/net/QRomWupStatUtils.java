package qrom.component.wup.net;

import java.util.Locale;

import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupLog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class QRomWupStatUtils {

    private static String TAG = "QRomWupStatUtils";
    
    /** wup请求网络状态 -- 无网络 */
    public static final String           NET_NONE         = "na";
    /** wup请求网络状态 -- wifi */
    public static final String          NET_WIFI             = "wifi";
    /** wup请求网络状态 -- 为知 */
    public static final String          NET_UNKOWN    = "--";

    /**
     * 获取当前网络的apn 信息
     * @param context
     * @return
     */
    public static String getCurStatNetType(Context context) {

        if (context == null) {
            return NET_UNKOWN;
        }

        try {
            
            QWupLog.d(TAG, "getCurStatNetType -> start");
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();

            boolean isConnected = networkInfo == null ? false : networkInfo.isConnected();
            
            if (!isConnected) {   // 网络未连接
                return NET_NONE;
            }
            
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {  // wifi
                return NET_WIFI;
            } 
            
            String extraInfo = networkInfo.getExtraInfo();

            if (extraInfo == null || "".equals(extraInfo.trim())) {  // 无法确定网络类型
                return NET_UNKOWN;
            }
            extraInfo = extraInfo.trim().replace("_", "-");
            QWupLog.d(TAG, "getCurStatNetType-> end");
            return extraInfo.toLowerCase(Locale.getDefault());
        }  catch (Exception e) {
            QWupLog.trace(TAG, "getCurStatNetType -> err msg: " + e.getMessage() + ", e: " + e);
        }
        return NET_UNKOWN;
    }    
    
    /**
     * 获取ApnStatInfo中的缓存apn信息
     * @return
     */
    public static String getCurStatNetType() {
        
        if (!ApnStatInfo.isNetConnected()) {
            return NET_NONE;
        }
        
        if (ApnStatInfo.isWifiMode()) {
            return NET_WIFI;
        }
        
        String extraInfo = ApnStatInfo.M_APN_EA_INFO; 
        if (extraInfo == null || "".equals(extraInfo.trim())) {  // 无法确定网络类型
            return NET_UNKOWN;
        }
        return extraInfo.replace("_", "-");
    }
    
}
