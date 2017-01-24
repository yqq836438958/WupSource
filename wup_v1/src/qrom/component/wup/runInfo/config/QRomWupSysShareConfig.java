package qrom.component.wup.runInfo.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class QRomWupSysShareConfig implements IRomWupShareConfig {
    
    /**
     * 获取上次获取iplist报文时间
     */
    public long getLastIPListTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        long time = sp.getLong(KEY_WUP_LAST_IPLISTS_SUCESS_TIME, 0);
//        QWupLog.w("==== wup-share", "getLastIPListTime -> " + time);
        return time;
    }
    
    /**
     * 记录上次获取iplist的时间
     */
    public void setLastIPListTime(Context context,
            long time) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putLong(KEY_WUP_LAST_IPLISTS_SUCESS_TIME, time).commit();
//        QWupLog.w("==== wup-share", "setLastIPListTime -> res" + res + ", time = " + time);        
    } 
    
    /**
     * 获取上次发送login的时间
     */
    public  long getLastLoginTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        long time = sp.getLong(KEY_WUP_LAST_REQ_LOGIN, 0);
//        QWupLog.w("====", "getLastLoginTime -> " + time);
        return time;
    }
    
    /**
     * 记录上次发送login的时间
     */
    public void setLastLoginTime(Context context,
            long time) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putLong(KEY_WUP_LAST_REQ_LOGIN, time).commit();
//        QWupLog.e("====", "setLastLoginTime -> " + time);
    } 
    
    /**
     * 获取上次wifibssid
     */
    public String getLastWifiBssidInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        return sp.getString(KEY_WUP_LAST_WIFI_BSSID, "");
    }
    
    /**
     * 记录上次wifibssid
     */
    public  void setLastWifiBssidInfo(Context context,
            String bssid) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putString(KEY_WUP_LAST_WIFI_BSSID, bssid).commit();
    }
    
    @Override
    public long getGuidTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        long time = sp.getLong(KEY_WUP_GET_GUID, 0);
//        QWupLog.w("====", "getGuidTime -> " + time);
        return time;
    }
    
    @Override
    public void setGuidTime(Context context, long time) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putLong(KEY_WUP_GET_GUID, time).commit();
    }
    
    private  int updataShareMode() {
        if (Build.VERSION.SDK_INT <11) {
            return Context.MODE_WORLD_READABLE;
        } else {
            return Context.MODE_MULTI_PROCESS;
        }
        
    }

    @Override
    public String getCheckFailStat(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        return sp.getString(KEY_WUP_CHECK_FAIL, "");
    }

    @Override
    public void setCheckFailStat(Context context, String flg) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putString(KEY_WUP_CHECK_FAIL, flg).commit();
    }

    @Override
    public void setCheckStatTime(Context context, long time) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putLong(KEY_WUP_CHECK_TIME, time).commit();
        
    }

    @Override
    public long getCheckStatTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        long time = sp.getLong(KEY_WUP_CHECK_TIME, 0);
//        QWupLog.i("====", "getCheckStatTime -> " + time);
        return time;
    }

    @Override
    public void setCheckStatCnt(Context context, int cnt) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putInt(KEY_WUP_CHECK_CNT, cnt).commit();
    }

    @Override
    public int getCheckStatCnt(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        int cnt = sp.getInt(KEY_WUP_CHECK_CNT, 0);
//        QWupLog.i("====", "getCheckStatCnt -> " + cnt);
        return cnt;
    }
    
    @Override
    public void setCheckStatMaxCnt(Context context, int cnt) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        sp.edit().putInt(KEY_WUP_CHECK_MAX_CNT, cnt).commit();
    }

    @Override
    public int getCheckStatMaxCnt(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                SHARE_PREFERENCES_NAME, updataShareMode());
        int cnt = sp.getInt(KEY_WUP_CHECK_MAX_CNT, 0);
//        QWupLog.i("====", "getCheckStatMaxCnt -> " + cnt);
        return cnt;
    }
}
