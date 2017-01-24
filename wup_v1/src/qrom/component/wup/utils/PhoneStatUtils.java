package qrom.component.wup.utils;

import java.lang.reflect.Method;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


public class PhoneStatUtils {
    
    private static String PHONE_IMEI = null;
    
    private static String PHONE_MAC = null;
    
    /**
     * 设备cpu是几核
     */
    private static int mDeviceCpuCores = -1;


    public static int getCpuCoreNum() {
        if (mDeviceCpuCores != -1) {
            return mDeviceCpuCores;
        }
        mDeviceCpuCores = Runtime.getRuntime().availableProcessors();
        return mDeviceCpuCores;
    }
    
    /**
     * 获取手机imei
     * @param context
     * @return
     */
    public static String getImei(Context context){
        
        if (PHONE_IMEI != null && !"".equals(PHONE_IMEI)) {
            return PHONE_IMEI;
        }
        
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                PHONE_IMEI = mTelephonyManager.getDeviceId();
                if (TextUtils.isEmpty(PHONE_IMEI)) {
                    PHONE_IMEI = getSimImei();
                }
            }
            
            if (PHONE_IMEI != null) {
                PHONE_IMEI = PHONE_IMEI.toLowerCase();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return PHONE_IMEI;
    }
    
    private static String getSimImei() {
        Class<?> clazz;
        try {
            clazz = Class.forName("android.telephony.TelephonyManager");
            Method method = clazz.getDeclaredMethod("getSecondary", null);
            method.setAccessible(true);
            TelephonyManager telManager = (TelephonyManager) method.invoke(clazz, null);
            if (telManager != null) {
                return telManager.getDeviceId();
            }
        } catch (Throwable e) {
            QWupLog.w("wup_phonestatUtil", "getSimImei -> " + e.getMessage());
        } 
        return null;
    }
    
    /**
     * 获取mac地址
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        
        if (QWupStringUtil.isEmpty(PHONE_MAC)) {            
            try {
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if(wifi == null) {
                    return null;
                }
                WifiInfo info = wifi.getConnectionInfo();
                if(info != null) {
                    PHONE_MAC = info.getMacAddress();
                }
                if (PHONE_MAC != null) {
                    PHONE_MAC = PHONE_MAC.toLowerCase();
                }
            } catch (Exception e) {
                QWupLog.w("wup_phonestatUtil", "getMacAddress -> err msg: " + e.getMessage());
            }
        }
        return PHONE_MAC;
    }
    
    public static String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {

                    return appProcess.processName;
                }
            }
        } catch (Throwable e) {
            QWupLog.w("wup-PhoneStatUtils", "getCurProcessName -> err msg = " + e.getMessage());
        }
        return null;
    }
    
//    /**
//     * 检查rom底层的统一上报service是否可用
//     * (使用SDK自上报模式，还是ROM统一上报模式)
//     */
//    public static boolean isRomServiceAvailable(Context context) {
//    	// 判断rom底层的统一上报service是否可用
//    	boolean isAvailable = false;
//    	String packageName = QRomWupBuildInfo.getRomPackageName();
//    	if (context != null && !TextUtils.isEmpty(packageName)) {    		
//    		PackageManager packageManager = context.getPackageManager();  
//    		try {
//    			PackageInfo pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
//    			if (pkgInfo != null) {
//    				isAvailable = true;
//    			}
//    		} catch (Exception e) {
//    			QWupLog.w("==== wup phone state====", e);
//    		}	    	
//    	}
//        return isAvailable;
//    }

}
