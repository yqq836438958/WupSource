package qrom.component.wup.qua;

import java.io.InputStreamReader;

import qrom.component.wup.utils.QWupLog;

import android.content.Context;

public class QRQuaSysRomConfigParser {

    private static final String TAG ="QRQuaSysRomConfigParser";
    
    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ qua相关配置key （和build.prop中的key相同）↓↓↓↓↓↓↓↓↓↓
    private static final String KEY_QUA_APP_SN__FLG = "ro.qrom.build.version.snflag";
    private static final String KEY_QUA_APP_SN_VER = "ro.qrom.build.version.snver";
    private static final String KEY_QUA_APP_SN_PUBLISH_TYPE = "ro.qrom.build.version.type";
    private static final String KEY_QUA_APP_VN_DAY = "ro.qrom.build.version.day";
    private static final String KEY_QUA_APP_BN_BUILD_NO = "ro.qrom.build.number";
    
    private static final String KEY_LC = "ro.qrom.build.lc";
    private static final String KEY_LCID = "ro.qrom.build.lcid";
    private static final String KEY_BUILD_TYPE = "ro.qrom.build.version.type";
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ qua相关配置key ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
  
    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ qua相关配置key : 渠道号 （和default.prop中的key相同）↓↓↓↓↓↓↓↓↓↓
    private static final String KEY_CHANNEL = "ro.qrom.build.channel";
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ qua相关配置key ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    
    
    /** rom SN:软件名 -- 软件标识 【平台标识】【产品标识】*/
    private static String BUILD_APP_SN_FLG = null;
    /** rom SN:软件名 --主次版本信息  【主版本 次版本】 */
    private static String BUILD_APP_SN_VER = null;
    /** rom SN:软件名 --  【发布类型】 */
    private static String BUILD_APP_SN_PUBLISH_TYPE = null;
    /** rom的VN:软件版本号 --     【年月日】（一般为发布当前日期） */
    private static String BUILD_APP_VN_DAY = null;
    /** rom的  BN: build no */
    private static String BUILD_APP_BN_BUILD_NO = null;
    
    /** rom 渠道ID */
    private static String BUILD_APP_CHID = null;
    /** rom LCID */
    private static String BUILD_APP_LCID = null;
    /** rom LC */
    private static String BUILD_APP_LC = null;
    
    /** rom 编译类型 */
    private static String BUILD_ROM_TYPE = null;
    
    /**
     * 加载配置文件相关信息（qua配置文件； 渠道号配置文件）
     *     系统build.prop文件
     *     系统default.prop文件（渠道号）
     * @param context
     */
    public static void initQuaItemInfo(Context context) {
        if (isStrEmpty(BUILD_APP_SN_FLG) || isStrEmpty(BUILD_APP_SN_VER)
                || isStrEmpty(BUILD_APP_SN_PUBLISH_TYPE) || isStrEmpty(BUILD_APP_VN_DAY)
                || isStrEmpty(BUILD_APP_BN_BUILD_NO)
                || isStrEmpty(BUILD_APP_LCID) || isStrEmpty(BUILD_APP_LC)
                || isStrEmpty(BUILD_ROM_TYPE)) {  // 参数为初始化
            // 加载配置文件            
            loadConfigInfo(context);
        }
        
        if (isStrEmpty(BUILD_APP_CHID)) {
            loadChannelID(context);
        }
    }
    
    public static String getSnFlg() {
        return BUILD_APP_SN_FLG;
    }
    public static String getSnVer() {
        return BUILD_APP_SN_VER;
    }
    public static String getSnPublishType() {
        return BUILD_APP_SN_PUBLISH_TYPE;
    }
    public static String getVNForDay() {
        return BUILD_APP_VN_DAY;
    }
    
    public static String getBuildNo() {
        return BUILD_APP_BN_BUILD_NO;
    }
    
    public static String getCHIDConfig() {
        return BUILD_APP_CHID;
    }
    
    public static String getLCID() {
        return BUILD_APP_LCID;
    }
    
    public static String getLC() {
        return BUILD_APP_LC;
    }
    
    public static String getBuildType() {
        return BUILD_ROM_TYPE;
    }
    
    /**
     * 从系统的build.prop文件获取version number
     * @return
     */
    public static String getVNFromProp() {
        try {
            String ver = BUILD_APP_SN_VER;
            if (isStrEmpty(ver)) { // 数据为空 重新获取
                ver = getSysProp(KEY_QUA_APP_SN_VER);                
            }
            
            String day = BUILD_APP_VN_DAY;
            if (isStrEmpty(day)) {  // 数据为空 重新获取
                day = getSysProp(KEY_QUA_APP_VN_DAY);
            }
            if (isStrEmpty(ver) || isStrEmpty(day)) {
                QWupLog.trace(TAG, "getVNFromProp -> sn version or day is empty");
                return null;
            }
            return ver +day;
        } catch (Exception e) {
            QWupLog.trace(TAG, "getVNFromProp -> err msg: " + e.getMessage() + ", e: " +e);
        }
        return null;
    }
    
    private static void loadConfigInfo(Context context) {
        try {
            BUILD_APP_SN_FLG = getSysProp(KEY_QUA_APP_SN__FLG);
            BUILD_APP_SN_VER = getSysProp(KEY_QUA_APP_SN_VER);
            BUILD_APP_SN_PUBLISH_TYPE = getSysProp(KEY_QUA_APP_SN_PUBLISH_TYPE);
            BUILD_APP_VN_DAY = getSysProp(KEY_QUA_APP_VN_DAY);
            BUILD_APP_BN_BUILD_NO = getSysProp(KEY_QUA_APP_BN_BUILD_NO);
            BUILD_APP_LC = getSysProp(KEY_LC);
            BUILD_APP_LCID = getSysProp(KEY_LCID);
            // rom编译版本类型
            BUILD_ROM_TYPE = getSysProp(KEY_BUILD_TYPE); 
        } catch (Exception e) {
            QWupLog.trace(TAG, "loadConfigInfo -> err msg: " + e.getMessage() + ", e: " +e);
        }
    }
    
    private static void loadChannelID(Context context) {
        try {
            BUILD_APP_CHID = getSysProp(KEY_CHANNEL);
        } catch (Exception e) {
            QWupLog.trace(TAG, "loadChannelID -> err msg: " + e.getMessage() + ", e: " +e);
        }
                
    }
    
    private static String getSysProp(String cmdItem) throws Exception{
        
        java.lang.Process process   = Runtime.getRuntime().exec("getprop "+ cmdItem);
        InputStreamReader inputStreamReader = new   InputStreamReader(process.getInputStream()); 
        char[] buf = new char[15];
        int readLen = 0;
        StringBuilder strb = new StringBuilder();;
        while ((readLen = inputStreamReader.read(buf)) != -1) {
            strb.append(buf, 0, readLen);
        }
      
        String resStr = strb.toString();
        if (resStr != null) {
            resStr = resStr.trim();
        }
       return resStr;
    }
    
    public static boolean isStrEmpty(String str) {
        return str == null || "".equals(str);
    }
    
}
