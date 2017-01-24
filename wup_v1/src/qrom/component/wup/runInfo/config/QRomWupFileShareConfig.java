package qrom.component.wup.runInfo.config;

import android.content.Context;


public class QRomWupFileShareConfig extends QRomBaseShareConfig implements IRomWupShareConfig {
    
    
    public QRomWupFileShareConfig(String path) {
       super(path, SHARE_PREFERENCES_NAME);
       TAG = "QRomWupFileShareConfig";
    }

    @Override
    public long getLastIPListTime(Context context) {
        return getLong(KEY_WUP_LAST_IPLISTS_SUCESS_TIME, 0);
    }

    @Override
    public void setLastIPListTime(Context context, long time) {
      
        putLong(KEY_WUP_LAST_IPLISTS_SUCESS_TIME, time);
    }

    @Override
    public long getLastLoginTime(Context context) {
        
        return getLong(KEY_WUP_LAST_REQ_LOGIN, 0);
    }

    @Override
    public void setLastLoginTime(Context context, long time) {
        putLong(KEY_WUP_LAST_REQ_LOGIN, time);
    }

//    @Override
//    public String getLastWifiBssidInfo(Context context) {
//        return getString(KEY_WUP_LAST_WIFI_BSSID);
//    }
//
//    @Override
//    public void setLastWifiBssidInfo(Context context, String bssid) {
//        putString(KEY_WUP_LAST_WIFI_BSSID, bssid);
//    }    

    @Override
    public long getGuidTime(Context context) {
        return getLong(KEY_WUP_GET_GUID, 0);
    }
    
    @Override
    public void setGuidTime(Context context, long time) {
        putLong(KEY_WUP_GET_GUID, time);
    }

    @Override
    public String getCheckFailStat(Context context) {
        return getString(KEY_WUP_CHECK_FAIL);
    }

    @Override
    public void setCheckFailStat(Context context, String flg) {
        putString(KEY_WUP_CHECK_FAIL, flg);
        
    }

    @Override
    public void setCheckStatTime(Context context, long time) {
        putLong(KEY_WUP_CHECK_TIME, time);
        
    }

    @Override
    public long getCheckStatTime(Context context) {
        return  getLong(KEY_WUP_CHECK_TIME, 0);
    }

    @Override
    public void setCheckStatCnt(Context context, int cnt) {
        putInt(KEY_WUP_CHECK_CNT, cnt);
        
    }

    @Override
    public int getCheckStatCnt(Context context) {
       
        return getInt(KEY_WUP_CHECK_CNT, 0);
    }
    
    @Override
    public void setCheckStatMaxCnt(Context context, int cnt) {
        putInt(KEY_WUP_CHECK_MAX_CNT, cnt);
        
    }

    @Override
    public int getCheckStatMaxCnt(Context context) {
       
        return getInt(KEY_WUP_CHECK_MAX_CNT, 0);
    }
}
