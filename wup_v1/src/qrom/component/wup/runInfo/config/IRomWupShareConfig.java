package qrom.component.wup.runInfo.config;

import android.content.Context;

public interface IRomWupShareConfig {
    

    public static final String SHARE_PREFERENCES_NAME = "wup_pref";
    
    
    /**
     * WUP上次拉取iplist时间
     */
    public static final String KEY_WUP_LAST_IPLISTS_SUCESS_TIME = "key_wup_iplists_sucess_time";

    /**
     * wifi的bssid信息
     */
    public static final String KEY_WUP_LAST_WIFI_BSSID = "key_wup_last_wifi_bssid";
    
    /**
     * 上次发送请求login的时间
     */
    public static final String KEY_WUP_LAST_REQ_LOGIN = "key_wup_last_req_login";
    
    /**
     * 拉取到guid的时间
     *   -- 用于多进程guid排重设置
     */
    public static final String KEY_WUP_GET_GUID = "key_wup_get_guid";
    /**
     * 是否打开检测失败的开关
     */
    public static final String KEY_WUP_CHECK_FAIL = "key_wup_check_fail";
    /**
     * 发送检测请求的时间
     */
    public static final String KEY_WUP_CHECK_TIME = "key_wup_check_time";
    /**
     * 发送检测请求次数
     */
    public static final String KEY_WUP_CHECK_CNT = "key_wup_check_cnt";
    /**
     * 发送检测请求最大次数
     */
    public static final String KEY_WUP_CHECK_MAX_CNT = "key_wup_check_max_cnt";
    
    /**
     * 获取上次获取iplist报文时间
     */
    public long getLastIPListTime(Context context);
    
    /**
     * 记录上次获取iplist的时间
     */
    public void setLastIPListTime(Context context, long time);
    
    /**
     * 获取上次发送login的时间
     */
    public long getLastLoginTime(Context context);
    /**
     * 记录上次获取iplist的时间
     */
    public void setGuidTime(Context context, long time);
    
    /**
     * 获取上次发送login的时间
     */
    public long getGuidTime(Context context);
    
    /**
     * 记录上次发送login的时间
     */
    public void setLastLoginTime(Context context,  long time);
    
//    /**
//     * 获取上次wifibssid
//     */
//    public String getLastWifiBssidInfo(Context context);
//    
//    /**
//     * 记录上次wifibssid
//     */
//    public void setLastWifiBssidInfo(Context context, String bssid);
    
    // -----------------20141216 添加检测网络服务器请求相关配置信息
    /**
     * 获取检测失败开关
     */
    public String getCheckFailStat(Context context);
    
    /**
     * 记录检测失败开关
     */
    public void setCheckFailStat(Context context, String flg);
    
    /**
     * 记录上次获取检测网络的时间
     */
    public void setCheckStatTime(Context context, long time);
    
    /**
     * 获取上次发送检测请求的时间
     */
    public long getCheckStatTime(Context context);
    /**
     * 记录上次获取检测网络的次数
     */
    public void setCheckStatCnt(Context context, int cnt);
    
    /**
     * 获取上次发送检测请求的次数
     */
    public int getCheckStatCnt(Context context);
    /**
     * 记录上次获取检测网络的最大次数
     */
    public void setCheckStatMaxCnt(Context context, int cnt);
    
    /**
     * 获取上次发送检测请求的最大次数
     */
    public int getCheckStatMaxCnt(Context context);
    
}
