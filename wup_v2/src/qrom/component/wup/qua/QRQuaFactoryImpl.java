package qrom.component.wup.qua;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.base.utils.SystemProperties;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

public class QRQuaFactoryImpl {
    
    /** qua相关配置文件名 */
    public static String QUA_CONFIG_FILENAME = "build_config.ini";
    /** 渠道号配置文件名 */
    public static String CHID_CONFIG_FILENAME = "channel.ini"; 
    /** 从build文件中获取的机型信息 */
    private static String PHONE_MODEL_BUILD = null;
    
    private QRQuaFactoryImpl() {
        
    }
    
    
    /**
     * 拼装qua
     *    
     *    qua相关版本信息从 assets/build_config.ini文件中获取
     *        (当前qua版本V5)
     * @param context
     * @return
     */
    public  static String buildQua(Context context) {
        QRQuaConfigFileParser.initQuaItemInfo(context);
        return buildQua(context, 
                QRQuaConfigFileParser.getAppSnFlg(), 
                QRQuaConfigFileParser.getAppSnVer(), 
                QRQuaConfigFileParser.getAppSnPublishType(), 
                QRQuaConfigFileParser.getAppVNForDay(), 
                QRQuaConfigFileParser.getAppBuildNo(),
                QRQuaConfigFileParser.getCHIDConfig(context), getLCID(context));
    }
    
    /**
     * 获取app的软件版本号(QUA中的vn)
     *    app的sn主次版本 + buildNO
     *    例：主次版本=10，buildNO = 140605；
     *           版本号 = 10140605
     *           
     * @param context
     * @return 软件版本号
     */
    public static String getAppBuildVN(Context context) {   
        
        QRQuaConfigFileParser.initQuaItemInfo(context);
        
        return QRQuaConfigFileParser.getAppSnVer()+QRQuaConfigFileParser.getAppVNForDay();
    }
    
    /**
     * 获取app渠道号文件中的渠道信息
     * @param context
     * @return
     */
    public static String getCHIDConfig(Context context) {
       
        return QRQuaConfigFileParser.getCHIDConfig(context);
    }
    
    /**
     * 获取app配置文件中的lcid
     * @param context
     * @return
     */
    public static String getLCID(Context context) {
       
        return QRQuaConfigFileParser.getLCID(context);
    }
    
    /**
     * 获取app配置文件中的lc
     * @param context
     * @return
     */
    public static String getLC(Context context) {
        
        return QRQuaConfigFileParser.getLC(context);
    }
    
    /**
     * 获取app的build no
     * @param context
     * @return
     */
    public static String getAppBN(Context context) {
        QRQuaConfigFileParser.initQuaItemInfo(context);
        return QRQuaConfigFileParser.getAppBuildNo();
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
        // 手机制造商 （MANUFACTURER ： 硬件制造商）
        String vc = Build.MANUFACTURER;
        // 手机型号
        String mo = getPhoneModel();
        // 源码控制版本号
        String rv = getRVInfo();
        // sdk版本
        String sdkOs = Build.VERSION.RELEASE;
        // 屏幕分辨率
        String rl = getScreenInfo(context);
        return buildQua(appSnFlg, appSnVer, appPublishType, appVersionNo, bn, vc, mo, rl, chid, lcid, rv, sdkOs, "V5");
    }
    
    //  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓获取rom的qua相关信息接口↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    
    /**
     * 拼装Rom的qua
     *     -- 系统源码framework集成模式qua
     * @param context
     * @param configDir
     * @return
     */
    public  static String buildQuaForSysRom(Context context) {
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return buildQua(context, 
                QRQuaSysRomConfigParser.getSnFlg(), 
                QRQuaSysRomConfigParser.getSnVer(), 
                QRQuaSysRomConfigParser.getSnPublishType(), 
                QRQuaSysRomConfigParser.getVNForDay(), 
                QRQuaSysRomConfigParser.getBuildNo(),
                QRQuaSysRomConfigParser.getCHIDConfig(), 
                QRQuaSysRomConfigParser.getLCID());
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
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return QRQuaSysRomConfigParser.getSnVer()+QRQuaSysRomConfigParser.getVNForDay();
    }
    
    /**
     * 获取Rom系统的渠道号文件中的渠道信息
     * @param context
     * @return
     */
    public static String getCHIDConfigForSysRom(Context context) {
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return QRQuaSysRomConfigParser.getCHIDConfig();
    }
    
    /**
     * 获取Rom系统配置文件中的lcid
     * @param context
     * @return
     */
    public static String getLCIDForSysRom(Context context) {
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return QRQuaSysRomConfigParser.getLCID();
    }
    
    /**
     * 获取rom系统配置文件中的lc
     * @param context
     * @return
     */
    public static String getLCForSysRom(Context context) {
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return QRQuaSysRomConfigParser.getLC();
    }
    
    /**
     * 获取rom系统配置文件中的build no
     * @param context
     * @return
     */
    public static String getBNForSysRom(Context context) {
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return QRQuaSysRomConfigParser.getBuildNo();
    }
    
    /**
     * 获取rom编译的版本类型，如 DD，TD等
     * @param context
     * @return
     */
    public static String getBuildTypeForSysRom(Context context) {
        QRQuaSysRomConfigParser.initQuaItemInfo(context);
        return QRQuaSysRomConfigParser.getBuildType();
    }
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑获取rom的qua相关信息接口↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    
    /**
     * 拼装qua
     * 
     *      SN:软件名          -> appFlg + appSnVer +'_' + appPublishType 
     *      VN:软件版本号   -> appVer + appVersionNo
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
        
        if (appSnFlg == null || "".equals(appSnFlg)) {  // 参数不合法
            QRomLog.trace("qua", "config -- appSnFlg is err!");
            return "";
        }
        if (appSnVer == null || "".equals(appSnVer)) {  // 参数不合法
            QRomLog.trace( "qua", "config -- appSnVer is err!");
            return "";
        }
        if (appPublishType == null || "".equals(appPublishType)) {  // 参数不合法
            QRomLog.trace("qua", "config -- appPublishType is err!");
            return "";
        }

        // 过滤相关信息的特殊字符
        vc = filterQuaItem(vc);
        mo = filterQuaItem(mo);
        rv = filterQuaItem(rv);
        os = filterQuaItem(os);
        os = "Android" + os;
        chid = filterQuaItem(chid);
        lcid = filterQuaItem(lcid);
        bn = filterQuaItem(bn);
        
        StringBuilder quaBuilder = new StringBuilder("SN=");
        // 拼装软件名：SN=ADRQRPRI10_GA
        quaBuilder.append(appSnFlg).append(appSnVer).append("_").append(appPublishType);
        quaBuilder.append("&");    
        // 拼装软件版本号：VN=10141225
        quaBuilder.append("VN=").append(appSnVer).append(appVersionNo);
        quaBuilder.append("&");
        quaBuilder.append("BN=").append(bn);
        quaBuilder.append("&");    
        // 拼装厂商信息： VC=HTC
        quaBuilder.append("VC=").append(vc);
        quaBuilder.append("&");    
        // 拼装设备信息： MO=M8T
        quaBuilder.append("MO=").append(mo);
        quaBuilder.append("&");    
        // 拼装屏幕分辨率： RL=1080_1920
        quaBuilder.append("RL=").append(rl);
        quaBuilder.append("&");    
        // 拼装渠道号： CHID=10000_10000
        quaBuilder.append("CHID=").append(chid);
        quaBuilder.append("&");    
        // 拼装渠道号： LCID=1200
        quaBuilder.append("LCID=").append(lcid);
        quaBuilder.append("&");    
        // 拼装渠道号： RV=1200
        quaBuilder.append("RV=").append(rv);
        quaBuilder.append("&");    
        // 拼装系统信息： OS=Android4.4.2
        quaBuilder.append("OS=").append(os);
        quaBuilder.append("&");    
        // 拼装qua版本：QV=V4
        quaBuilder.append("QV=").append(qv);
        
        return quaBuilder.toString();
    }

    /**
     *  item中的特殊字符替换为#
     * @param item
     * @return
     */
    private static String filterQuaItem(String item) {
        if (item == null || "".equals(item)) {
            return "NA";
        }
        item = item.replace("/", "#10");
        item = item.replace("&", "#20");
        item = item.replace("|", "#30");
        item = item.replace("=", "#40");
        return item;
    }
    
    /**
     * 获取机型信息
     * @return
     */
    public static String getPhoneModel() {
        try {
            if (StringUtil.isEmpty(PHONE_MODEL_BUILD)) {
                PHONE_MODEL_BUILD  = SystemProperties.get("ro.qrom.product.device", "");
            }
        } catch (Throwable e) {
            QRomLog.trace("qua", "getPhoneModel -> err msg: " + e.getMessage());
        }
        
        if (!StringUtil.isEmpty(PHONE_MODEL_BUILD) ) {
            return PHONE_MODEL_BUILD;
        }
        return Build.MODEL;
    }
    
    /**
     * 获取屏幕分辨率信息
     * 
     * @param context
     * @return  【屏幕宽】 _【屏幕高】（竖屏状态）
     */
    private static String getScreenInfo(Context context ) {
        android.view.WindowManager manager = 
                (android.view.WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int mScreenWidth = 0;
        int mScreenHeight = 0;
        if  (manager != null) {
            DisplayMetrics dm = new DisplayMetrics();
            Display display = manager.getDefaultDisplay();
            
            if(display != null) {
                manager.getDefaultDisplay().getMetrics(dm);
                // 获取屏幕的大小
                mScreenWidth = dm.widthPixels;
                mScreenHeight = dm.heightPixels; 
            }
        }
        
        if (mScreenWidth > mScreenHeight) {  // 横屏
            int temp = mScreenWidth;
            mScreenWidth = mScreenHeight;
            mScreenHeight = temp;
        }
        return mScreenWidth + "_" + mScreenHeight;
    }
    
    private static String getRVInfo() {
        
        String qRomVerNum = null;
        String qRomBrand = null;
        
        try {
            qRomBrand = SystemProperties.get("ro.qrom.build.brand", "");
            
            // 获取新属性中rom version
            qRomVerNum = QRQuaSysRomConfigParser.getVNFromProp();
            if (qRomVerNum == null || "".equals(qRomVerNum)) {
                // 兼容老版本及新蜂rom，新版本切换到新属性上
                qRomVerNum = SystemProperties.get("ro.qrom.build.version.number", "");
            }
        } catch (Exception e) {
            QRomLog.w("qua -> rv ", "getRVInfo -> errMsg = " + e.getMessage());
        }
        
        if (qRomVerNum == null || "".equals(qRomVerNum.trim())) {  // 数据为空
            qRomVerNum = Build.VERSION.INCREMENTAL;
        }
        
        if (qRomBrand == null || "".equals(qRomBrand.trim())) {
            qRomBrand = Build.BRAND;
        }
        
        if (qRomVerNum == null) {
            qRomVerNum = "NA";
        }
        
        if (qRomBrand == null) {
            qRomBrand = "NA";
        }        

        String rv = qRomBrand.trim() +"." +qRomVerNum.trim();
        QRomLog.trace("qua",  "getRVInfo -> rv = " + rv);
        return rv;        
    }
    
}
