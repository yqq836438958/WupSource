package qrom.component.wup.qua;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import qrom.component.log.QRomLog;
import android.content.Context;
import android.content.res.AssetManager;


public class QRQuaConfigFileParser {
    
    public static final String TAG = "QuaConfigFileParser";
    
    private static final String KEY_CHANNEL = "CHANNEL";
    
    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ qua相关配置key （和assets/build_config.ini中的key相同）↓↓↓↓↓↓↓↓↓↓
    private static final String KEY_QUA_APP_SN__FLG = "BUILD_APP_SN_FLG";
    private static final String KEY_QUA_APP_SN_VER = "BUILD_APP_SN_VER";
    private static final String KEY_QUA_APP_SN_PUBLISH_TYPE = "BUILD_APP_SN_PUBLISH_TYPE";
    private static final String KEY_QUA_APP_VN_DAY = "BUILD_APP_VN_DAY";
    private static final String KEY_QUA_APP_BN_BUILD_NO = "BUILD_APP_BN_BUILD_NO";
    
    private static final String KEY_LC = "LC";
    private static final String KEY_LCID = "LCID";
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ qua相关配置key ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    
    /** app SN:软件名 -- 软件标识 【平台标识】【产品标识】*/
    private static String BUILD_APP_SN_FLG = null;
    /** app SN:软件名 --主次版本信息  【主版本 次版本】 */
    private static String BUILD_APP_SN_VER = null;
    /** app SN:软件名 --  【发布类型】 */
    private static String BUILD_APP_SN_PUBLISH_TYPE = null;
    /** app的VN:软件版本号 --     【年月日】（一般为发布当前日期） */
    private static String BUILD_APP_VN_DAY = null;
    /** app的  BN: build no */
    private static String BUILD_APP_BN_BUILD_NO = null;
    
    /** 渠道ID */
    private static String BUILD_APP_CHID = null;
    /** LCID */
    private static String BUILD_APP_LCID = null;
    /** LC */
    private static String BUILD_APP_LC = null;
    
    /**
     * 加载配置文件相关信息（qua配置文件； 渠道号配置文件）
     *     QRQuaFactory.QUA_CONFIG_FILENAME
     *     QRQuaFactory.CHID_CONFIG_FILENAME
     * @param context
     */
    public static void initQuaItemInfo(Context context) {
        if (isStrEmpty(BUILD_APP_SN_FLG) || isStrEmpty(BUILD_APP_SN_VER)
                || isStrEmpty(BUILD_APP_SN_PUBLISH_TYPE) || isStrEmpty(BUILD_APP_VN_DAY)
                || isStrEmpty(BUILD_APP_BN_BUILD_NO)
                || isStrEmpty(BUILD_APP_LCID) || isStrEmpty(BUILD_APP_LC)) {  // 参数为初始化
            // 加载配置文件            
            loadConfigInfo(context);
        }
        
        if (isStrEmpty(BUILD_APP_CHID)) {
            BUILD_APP_CHID = loadChannelID(context);
        }
    }
    
    public static void initQuaItemInfo(String configDirPath) {
        try {
            if (isStrEmpty(BUILD_APP_SN_FLG) || isStrEmpty(BUILD_APP_SN_VER)
                    || isStrEmpty(BUILD_APP_SN_PUBLISH_TYPE) || isStrEmpty(BUILD_APP_VN_DAY)
                    || isStrEmpty(BUILD_APP_BN_BUILD_NO)
                    || isStrEmpty(BUILD_APP_LCID) || isStrEmpty(BUILD_APP_LC)) {  // 参数为初始化
                // 加载配置文件            
                InputStream configInputStream = new FileInputStream(
                        new File(configDirPath, QRQuaFactoryImpl.QUA_CONFIG_FILENAME));
                loadConfigInfoFromStream(configInputStream);
            }
            
            if (isStrEmpty(BUILD_APP_CHID)) {
                InputStream channelInputStream = new FileInputStream(
                        new File(configDirPath, QRQuaFactoryImpl.CHID_CONFIG_FILENAME));
                BUILD_APP_CHID = loadChannelInfoFromStream(channelInputStream);
            }
        } catch (Exception e) {
            QRomLog.w(TAG, e);
        }
    }
    
    protected static String getAppSnFlg() {
        return BUILD_APP_SN_FLG;
    }
    
    protected static String getAppSnVer() {
        return BUILD_APP_SN_VER;
    }
    
    protected static String getAppSnPublishType() {
        return BUILD_APP_SN_PUBLISH_TYPE;
    }
    
    protected static String getAppVNForDay() {
        return BUILD_APP_VN_DAY;
    }
    protected static String getAppBuildNo() {
        return BUILD_APP_BN_BUILD_NO;
    }
    
    protected static String getCHIDConfig(Context context) {

        initQuaItemInfo(context);
        return BUILD_APP_CHID;
    }
    
    protected static String getLCID(Context context) {
        initQuaItemInfo(context);
        return BUILD_APP_LCID;
    }
    
    protected static String getLC(Context context) {
        if (isStrEmpty(BUILD_APP_LC)) { // 加载lcid
            initQuaItemInfo(context);
        }
        return BUILD_APP_LC;
    }
    
    public static boolean isStrEmpty(String str) {
        return str == null || "".equals(str);
    }
    
    public static InputStream openAssetsInput(Context context, String assetsFileName) {

        try {
            return context.getAssets().open(assetsFileName, AssetManager.ACCESS_STREAMING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * 从数据流中读取渠道ID
     */
    public static String loadChannelID(Context context) {
        
        InputStream inputStream = openAssetsInput(context, QRQuaFactoryImpl.CHID_CONFIG_FILENAME);
        if (inputStream == null) {
            QRomLog.trace(TAG, "loadChannelID inputStream is null");
            return null;
        }
        
        return loadChannelInfoFromStream(inputStream);        
    }
    
    /**
     * 从数据流中读取渠道ID
     */
    public static boolean loadConfigInfo(Context context) {
        
        InputStream inputStream = openAssetsInput(context, QRQuaFactoryImpl.QUA_CONFIG_FILENAME);
        if (inputStream == null) {
            QRomLog.trace(TAG, "qua flg etc inputStream is null");
            return false;
        }

        return loadConfigInfoFromStream(inputStream);
    }
    
    /**
     * 解析配置文件流
     * @param inputStream
     * @return
     */
    private static boolean loadConfigInfoFromStream(InputStream inputStream) {
        try {
            Properties property = new Properties();
            property.load(inputStream);
            BUILD_APP_SN_FLG = property.getProperty(KEY_QUA_APP_SN__FLG);
            BUILD_APP_SN_VER = property.getProperty(KEY_QUA_APP_SN_VER);
            BUILD_APP_SN_PUBLISH_TYPE = property.getProperty(KEY_QUA_APP_SN_PUBLISH_TYPE);
            BUILD_APP_VN_DAY = property.getProperty(KEY_QUA_APP_VN_DAY);
            BUILD_APP_BN_BUILD_NO = property.getProperty(KEY_QUA_APP_BN_BUILD_NO);
            
            BUILD_APP_LC = property.getProperty(KEY_LC);
            BUILD_APP_LCID = property.getProperty(KEY_LCID);
            return true;
        } catch (Exception e) {
            
            BUILD_APP_SN_FLG = null;
            BUILD_APP_SN_VER = null;
            BUILD_APP_SN_PUBLISH_TYPE = null;
            BUILD_APP_VN_DAY = null;
            
            BUILD_APP_LC = null;
            BUILD_APP_LCID = null;            
            QRomLog.w(TAG, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    private static String loadChannelInfoFromStream(InputStream inputStream) {
        try {
            Properties property = new Properties();
            property.load(inputStream);
            String channelId = property.getProperty(KEY_CHANNEL);
            return channelId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
