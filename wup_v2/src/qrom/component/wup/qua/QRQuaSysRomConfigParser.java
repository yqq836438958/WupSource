package qrom.component.wup.qua;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.base.utils.SystemProperties;
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
        if (StringUtil.isEmpty(BUILD_APP_SN_FLG) || StringUtil.isEmpty(BUILD_APP_SN_VER)
                || StringUtil.isEmpty(BUILD_APP_SN_PUBLISH_TYPE) || StringUtil.isEmpty(BUILD_APP_VN_DAY)
                || StringUtil.isEmpty(BUILD_APP_BN_BUILD_NO)
                || StringUtil.isEmpty(BUILD_APP_LCID) || StringUtil.isEmpty(BUILD_APP_LC)
                || StringUtil.isEmpty(BUILD_ROM_TYPE)) {  // 参数为初始化
            // 加载配置文件            
            loadConfigInfo(context);
        }
        
        if (StringUtil.isEmpty(BUILD_APP_CHID)) {
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
            if (StringUtil.isEmpty(ver)) { // 数据为空 重新获取
                ver = SystemProperties.get(KEY_QUA_APP_SN_VER, "");                
            }
            
            String day = BUILD_APP_VN_DAY;
            if (StringUtil.isEmpty(day)) {  // 数据为空 重新获取
                day = SystemProperties.get(KEY_QUA_APP_VN_DAY, "");
            }
            if (StringUtil.isEmpty(ver) || StringUtil.isEmpty(day)) {
                QRomLog.trace(TAG, "getVNFromProp -> sn version or day is empty");
                return null;
            }
            return ver +day;
        } catch (Exception e) {
            QRomLog.trace(TAG, "getVNFromProp -> err msg: " + e.getMessage() + ", e: " +e);
        }
        return null;
    }
    
    private static void loadConfigInfo(Context context) {
        try {
            BUILD_APP_SN_FLG = SystemProperties.get(KEY_QUA_APP_SN__FLG, "");
            BUILD_APP_SN_VER = SystemProperties.get(KEY_QUA_APP_SN_VER, "");
            BUILD_APP_SN_PUBLISH_TYPE = SystemProperties.get(KEY_QUA_APP_SN_PUBLISH_TYPE, "");
            BUILD_APP_VN_DAY = SystemProperties.get(KEY_QUA_APP_VN_DAY, "");
            BUILD_APP_BN_BUILD_NO = SystemProperties.get(KEY_QUA_APP_BN_BUILD_NO, "");
            BUILD_APP_LC = SystemProperties.get(KEY_LC, "");
            BUILD_APP_LCID = SystemProperties.get(KEY_LCID, "");
            // rom编译版本类型
            BUILD_ROM_TYPE = SystemProperties.get(KEY_BUILD_TYPE, ""); 
        } catch (Exception e) {
            QRomLog.trace(TAG, "loadConfigInfo -> err msg: " + e.getMessage() + ", e: " +e);
        }
    }
    
    private static void loadChannelID(Context context) {
        try {
            BUILD_APP_CHID = SystemProperties.get(KEY_CHANNEL, "");
        } catch (Exception e) {
            QRomLog.trace(TAG, "loadChannelID -> err msg: " + e.getMessage() + ", e: " +e);
        }
    }
        
}
