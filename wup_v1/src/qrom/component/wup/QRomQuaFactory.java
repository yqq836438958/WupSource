package qrom.component.wup;

import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.qua.QRQuaFactoryImpl;
import qrom.component.wup.utils.QWupFileUtil;
import android.content.Context;

public class QRomQuaFactory {

    /**
     * 拼装qua
     *    
     *    qua相关版本信息从 assets/build_config.ini文件中获取
     *        (当前qua版本V5)
     * @param context
     * @return
     */
    public  static String buildQua(Context context) {
    	// 自己内部特定的apk会关心模式，已设置的模式优先
        if (QRomWupEnvironment.getBuildQuaMode() == QRomWupEnvironment.QuaBuildMode.BuildFromRomSrc) {
        	return QRQuaFactoryImpl.buildQuaForSysRom(context, QWupFileUtil.getQuaConfigDirForSysRomSrc());
        }
        
        // 默认逻辑的遗留代码,
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {
            return QRQuaFactoryImpl.buildQuaForSysRom(context,
                    QWupFileUtil.getQuaConfigDirForSysRomSrc());
        }
        return QRQuaFactoryImpl.buildQua(context);
    }
    
    /**
     * 拼装qua （V5版）
     *    
     * @param context
     * @param appSnFlg             app软件标识为  【平台标识】【产品标识】
     * @param appSnVer            app的主次版本信息  【主版本次版本】
     * @param appPublishType   app的发布类型  【发布类型】
     * @param appVersionNo         app的编译号     【年月日】（一般为发布当前日期）
     * @param chid                     渠道ID
     * @param lcid                   LCID
     * @return
     */
    public static String buildQua(Context context, String appSnFlg, String appSnVer, 
            String appPublishType, String appVersionNo, String bn, String chid, String lcid) {
        
        return QRQuaFactoryImpl.buildQua(context, appSnFlg, appSnVer, appPublishType, appVersionNo, bn, chid, lcid);
    }
    
    /**
     * 拼装qua
     * 
     *      SN:软件名          -> appFlg + appSnVer +'_' + appPublishType 
     *      VN:软件版本号   -> appVer + appBuildNo
     *      
     * @param appSnFlg             app软件标识为  【平台标识】【产品标识】
     * @param appSnVer            app的主次版本信息  【主版本次版本】
     * @param appPublishType   app的发布类型  【发布类型】
     * @param appVersionNo     app的软件版本号     【年月日】（一般为发布当前日期）
     * @param bn                       app的build     （一般为RDM编译的序列号）
     * @param vc                        设备的厂商名
     * @param mo                      机型信息
     * @param rl                         屏幕分辨率         【屏幕宽】 _【屏幕高】
     * @param chid                     渠道ID
     * @param lcid                   LC ID
     * @param rv                        ROM版本号
     * @param os                        操作系统版本
     * @param qv                       Q-UA版本号
     * @return
     */
    public static String buildQua(String appSnFlg, String appSnVer, String appPublishType, String appVersionNo, 
            String bn, String vc, String mo, String rl, String chid, String lcid, 
            String rv, String os, String qv) {
        return QRQuaFactoryImpl.buildQua(appSnFlg, appSnVer, appPublishType, appVersionNo, 
                bn, vc, mo, rl, chid, lcid, rv, os, qv);
    }
    
    /**
     * 获取app的软件版本号(QUA中的vn)
     *    app的sn主次版本 + 年月日
     *    例：主次版本=10，年月日 = 140605；
     *           版本号 = 10140605
     *           
     * @param context
     * @return 软件版本号
     */
    public static String getAppVersionNo(Context context) {
        
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 系统源码模式
            return QRQuaFactoryImpl.getBuildVNForSysRom(context);
        }
        return QRQuaFactoryImpl.getAppBuildVN(context);
    }
    
    /**
     * 获取配置文件中的lcid
     * @param context
     * @return
     */
    public static String getLCID(Context context) {
        
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 系统源码模式
            return QRQuaFactoryImpl.getLCIDForSysRom(context);
        }
        // 普通app模式
        return QRQuaFactoryImpl.getLCID(context);
    }
    
    /**
     * 获取配置文件中的lc
     * @param context
     * @return
     */
    public static String getLC(Context context) {
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 系统源码模式
            return QRQuaFactoryImpl.getLCForSysRom(context);
        }
        // 普通app模式
        return QRQuaFactoryImpl.getLC(context);
    }
    
    /**
     * 获取当前渠道号文件中的渠道信息
     * @param context
     * @return
     */
    public static String getCurCHID(Context context) {
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 系统源码模式
            return QRQuaFactoryImpl.getCHIDConfigForSysRom(context);
        }
        // 普通app模式
        return QRQuaFactoryImpl.getCHIDConfig(context);
    }
    
    /**
     * 获取当前app的build no
     * @param context
     * @return
     */
    public static String getAppBn(Context context) {
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 系统源码模式
            return QRQuaFactoryImpl.getBNForSysRom(context);
        }
        // 普通app模式
        return QRQuaFactoryImpl.getAppBN(context);
    }
    
    /**
     * 获取手机的机型信息
     *   -- 优先获取tos自定义的机型信息，若获取失败则返回系统标准接口的机型信息
     * @return
     */
    public static String getPhoneModel() {
        return QRQuaFactoryImpl.getPhoneModel();
    }
 //  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓获取rom的qua相关信息接口↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    
    /**
     * 拼装Rom的qua
     *     --TOS ROM自己的qua 
     *     -- 系统源码framework集成模式qua
     * @param context
     * @param configDir
     * @return
     */
    public  static String buildQuaForSysRom(Context context) {
        return QRQuaFactoryImpl.buildQuaForSysRom(context, "");
    }    
    
    /**
     * 获取rom的软件版本号(QUA中的vn)
     *    app的sn主次版本 + buildNO
     *    例：主次版本=10，buildNO = 140605；
     *           版本号 = 10140605
     *           
     * @param context
     * @return 软件版本号
     */
    public static String getBuildVNForSysRom(Context context) {
        return QRQuaFactoryImpl.getBuildVNForSysRom(context);
    }
    
    /**
     * 获取Rom系统的渠道号文件中的渠道信息
     * @param context
     * @return
     */
    public static String getCHIDConfigForSysRom(Context context) {
        return QRQuaFactoryImpl.getCHIDConfigForSysRom(context);
    }
    
    /**
     * 获取Rom系统配置文件中的lcid
     * @param context
     * @return
     */
    public static String getLCIDForSysRom(Context context) {
        return QRQuaFactoryImpl.getLCIDForSysRom(context);
    }
    
    /**
     * 获取rom系统配置文件中的lc
     * @param context
     * @return
     */
    public static String getLCForSysRom(Context context) {
        return QRQuaFactoryImpl.getLCForSysRom(context);
    }
    
    /**
     * 获取rom系统配置文件中的build no
     * @param context
     * @return
     */
    public static String getBNForSysRom(Context context) {
        return QRQuaFactoryImpl.getBNForSysRom(context);
    }
    
    /**
     * 获取rom配置文件的编译版本 build type
     * @param context
     * @return
     */
    public static String getBuildTypeForSysRom(Context context) {
        return QRQuaFactoryImpl.getBuildTypeForSysRom(context);
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑获取rom的qua相关信息接口↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    
    
}
