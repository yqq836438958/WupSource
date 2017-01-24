package qrom.component.wup.runInfo;

import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.runInfo.config.IRomWupShareConfig;
import qrom.component.wup.runInfo.config.QRomWupFileShareConfig;
import qrom.component.wup.utils.QWupFileUtil;
import android.content.Context;

public class QRomWupSharedpreferences {

    private static IRomWupShareConfig mConfig = null;
    
//    private static String TAG = "====QRomWupSharedpreferences";
    
    /**
     * 获取上次获取iplist报文时间
     */
    public static long getLastIPListTime(Context context) {
        long time = getShareConfig(context).getLastIPListTime(context);
//        QWupLog.w(TAG, "wup config: getLastIPListTime -> " + time);
        return time;
    }
    
    /**
     * 记录上次获取iplist的时间
     */
    public static void setLastIPListTime(Context context,
            long time) {
//        QWupLog.w(TAG, "wup config: setLastIPListTime ->, time = " + time);   
        getShareConfig(context).setLastIPListTime(context, time);
    } 
    
    /**
     * 获取上次发送login的时间
     */
    public static long getLastLoginTime(Context context) {
        long time = getShareConfig(context).getLastLoginTime(context);
//        QWupLog.w(TAG, "wup config: getLastLoginTime -> " + time);
        return time;
    }
    
    /**
     * 记录上次发送login的时间
     */
    public static void setLastLoginTime(Context context,
            long time) {
//        QWupLog.e(TAG, "wup config: setLastLoginTime -> " + time);
        getShareConfig(context).setLastLoginTime(context, time);
    } 
    
    /**
     * 获取上次发送login的时间
     */
    public static long getGuidTime(Context context) {
        long time = getShareConfig(context).getGuidTime(context);
//        QWupLog.w(TAG, "wup config: getGuidTime -> " + time);
        return time;
    }
    
    /**
     * 记录上次发送login的时间
     */
    public static void setGuidTime(Context context,
            long time) {
//        QWupLog.e(TAG, "wup config: setGuidTime -> " + time);
        getShareConfig(context).setGuidTime(context, time);
    } 
    
//    /**
//     * 获取上次wifibssid
//     */
//    public static String getLastWifiBssidInfo(Context context) {
//    	return getShareConfig(context).getLastWifiBssidInfo(context);
//    }
//    
//    /**
//     * 记录上次wifibssid
//     */
//    public static void setLastWifiBssidInfo(Context context,
//    		String bssid) {
//    	getShareConfig(context).setLastWifiBssidInfo(context, bssid);
//    }
    
    /**
     * 获取检测失败开关
     */
    public static String getCheckFailStat(Context context) {
        return getShareConfig(context).getCheckFailStat(context);
    }
    
    /**
     * 记录检测失败开关
     */
    public static void setCheckFailStat(Context context, String flg) {
        getShareConfig(context).setCheckFailStat(context, flg);
    }
    
    /**
     * 记录上次获取检测网络的时间
     */
    public static void setCheckStatTime(Context context, long time) {
        getShareConfig(context).setCheckStatTime(context, time);
    }
    
    /**
     * 获取上次发送检测请求的时间
     */
    public static long getCheckStatTime(Context context) {
        return getShareConfig(context).getCheckStatTime(context);
    }
    /**
     * 记录上次获取检测网络的次数
     */
    public static void setCheckStatCnt(Context context, int cnt) {
        getShareConfig(context).setCheckStatCnt(context, cnt);
    }
    
    /**
     * 获取上次发送检测请求的次数
     */
    public static int getCheckStatCnt(Context context) {
        return getShareConfig(context).getCheckStatCnt(context);
    }
    /**
     * 记录上次获取检测网络的最大次数
     */
    public static void setCheckStatMaxCnt(Context context, int cnt) {
        getShareConfig(context).setCheckStatMaxCnt(context, cnt);
    }
    
    /**
     * 获取上次发送检测请求的最大次数
     */
    public static int getCheckStatMaxCnt(Context context) {
        return getShareConfig(context).getCheckStatMaxCnt(context);
    }
    
    
    private static IRomWupShareConfig getShareConfig(Context context) {
        
        if (mConfig == null) {
            synchronized (QRomWupSharedpreferences.class) {
                if (mConfig == null) {
                    if (QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 源码集成模式
                        mConfig = new QRomWupFileShareConfig(QWupFileUtil.getWupDataRootForSysRomSrc().getAbsolutePath());
                    } else {  // 非源码集成模式
                        mConfig = new QRomWupFileShareConfig(QWupFileUtil.getWupDataDir(context).getAbsolutePath());
//                        mConfig = new QRomWupSysShareConfig();
                    }  
                }                
            }
        }       
        
        return mConfig;        
    }
    
}
