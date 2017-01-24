package qrom.component.wup.build;

import java.lang.reflect.Field;
import java.util.List;

import qrom.component.wup.QRomWupBaseConfig;
import qrom.component.wup.QRomWupConstants.WUP_RUNTIME_FLG;
import qrom.component.wup.build.RomHostDetector.HostAppInfo;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;
import android.content.Context;


public class QRomWupBuildInfo {
	private static final String TAG = "====QRomWupBuildInfo";
	
	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓以下变量由脚本负责同步，勿随意修改属性值 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	/** ROM 使用wup模块的底层servant name */
	private static final String ROM_SDK_ROM_SERVICE_NAME_DB = QRomWupBuindInfoDB.ROM_SDK_ROM_SERVICE_NAME_DB;
	
	/** 回调获配置信息类信息 */
	private static final String CONFIG_CLASSPATH_WUP_CONFIGCLASS_DB = QRomWupBuindInfoDB.CONFIG_CLASSPATH_WUP_CONFIGCLASS_DB;
    
	/** 使用sdk 应用的package name*/
    private static String APP_PACKAGE_NAME_DB = QRomWupBuindInfoDB.APP_PACKAGE_NAME_DB;
    
    /** app 打包环境是否是debug 模式 -- false: release包； ture : debug包*/
    private static boolean M_APP_PUBLISH_MODE_DEBUG = false; 
    
	/** wup是否运行在测试环境 */
	private static  boolean M_WUP_RUN_TEST_DEBUG = false;

	/** wup是否运使用调试代理地址 */
	private static  boolean M_WUP_USED_DEBUG_ADDR = false;
	
    /** wup 测试环境代理地址 */
	private static String WUP_TEST_PROXY_ADDR_DB = null;
    /** wup 测试环境 socket 代理地址 */
	private static String WUP_TEST_SOCKET_PROXY_ADDR_DB = null;	
	
	/** rom 模式app的package name*/
	private static String ROM_SDK_APP_PACKAGE_NAME = null;	
	
	private static List<HostAppInfo> mHostAppInfos;
	
	/** sdk 运行模式 */
	private static int mSdkRunMode = WUP_RUNTIME_FLG.WUP_RUNTIME_NO_INIT;
	
	/** 是否是framework源码集成的rom */
	private static boolean mIsSrcFrameWorkRom = false;
	
	/**
	 * 是否强制切换到测试环境<p>
	 *   -- app是release版本时，不允许代码强切测试环境
	 * @return
	 */
	public static boolean isWupRunTestForDebug() {
		checkConfigValidy();
		// release版本的app强制切换到正式环境，仅允许调试小工具切换测试环境
		// debug版本允许开发自行设置正式环境/测试环境
		return M_WUP_RUN_TEST_DEBUG && M_APP_PUBLISH_MODE_DEBUG;
	}
	
	/**
	 * app是否是debug发布模式
	 * @return  true: debug版本; false:release版本
	 */
	public static boolean isAppPublishModeDebug() {
	    return M_APP_PUBLISH_MODE_DEBUG;
	}
	
	/**
	 * 是否强制使用调试地址<p>
	 *   -- app是release版本时，不允许代码指定调试模式，走正常流程
	 * @return
	 */
	public static boolean isForcedUseDebugAddr() {
	    checkConfigValidy();
	    return M_WUP_USED_DEBUG_ADDR && M_APP_PUBLISH_MODE_DEBUG;
	}
	
	public static String getWupConfigFileInfo() {
		
		return CONFIG_CLASSPATH_WUP_CONFIGCLASS_DB;
	}
	
	/**
	 * 获取调用wup 的app的package name
	 * @return
	 */
	public static String getAppPackageName() {
		checkConfigValidy();
		return APP_PACKAGE_NAME_DB;
	}
	
	public static List<HostAppInfo> getWupHostInfo() {
	    return mHostAppInfos;
	}
	
    public static void initWupHostInfo(Context context) {

        mHostAppInfos = RomHostDetector.detectDescHostAppInfo(context);
        // 获取当前rom 的信息
        ROM_SDK_APP_PACKAGE_NAME = RomHostDetector.getRomPackageName(context);

        if (getRomPackageName().equals(RomHostDetector.getSysRomSrcSdkPkgName())) {  // rom packageName 是源码集成模式
            // sdk源码集成模式的rom
            mIsSrcFrameWorkRom = true;
        } else {
            // 普通rom
            mIsSrcFrameWorkRom = false;
        }
        
        if (RomHostDetector.getSysRomSrcSdkPkgName().equals(getAppPackageName())) {  // rom源码集成模式
            mSdkRunMode = WUP_RUNTIME_FLG.WUP_RUNTIME_SYS_ROM_SRC;
        } else if (mHostAppInfos != null && !mHostAppInfos.isEmpty()) { // 有host app 信息

            if (mIsSrcFrameWorkRom) {  // app在sdk源码集成的rom中， 所有app都是app in rom模式中
                mSdkRunMode = WUP_RUNTIME_FLG.WUP_RUNTIME_APP_IN_ROM_SRC;
            } else if (getAppPackageName().equals(getRomPackageName())) { // 当前app 在普通rom中，且作为rom 功能的apk
                mSdkRunMode = WUP_RUNTIME_FLG.WUP_RUNTIME_ROM_APK;
            } else {  // app在普通rom模式中
                mSdkRunMode = WUP_RUNTIME_FLG.WUP_RUNTIME_APP_IN_ROM;
            }
        } else { // 无任何host信息, app独立模式
            mSdkRunMode = WUP_RUNTIME_FLG.WUP_RUNTIME_APP_ALONE;
        }
    }
	
	/**
	 * 获取rom 中使用wup模块的 拉取guid的包名
	 * @return
	 */
	public static String getRomPackageName() {	    

	    if (ROM_SDK_APP_PACKAGE_NAME == null) {
	        ROM_SDK_APP_PACKAGE_NAME = "";
	    }
	    
	    return ROM_SDK_APP_PACKAGE_NAME;
	}
	
	public static String getRomServiceName() {
		return ROM_SDK_ROM_SERVICE_NAME_DB;
	}	
	   
    /**
     * 获取当前sdk运行模式
     * @return
     */
    public static int getWupRunMode() {
        return mSdkRunMode;
    }
    
	/**
	 * 是否是rom底层的wup模块的app
	 *    -- 负责rom guid拉取的模块
	 * @return
	 */
	public static boolean isWupForRom(Context context) {
		
	    if (mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_NO_INIT) {
	        initWupHostInfo(context);
	    }
	    
		return mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_ROM_APK;
	}
	
	/**
	 * 是否运行在指定rom中（rom模式）
	 *   -- 负责拉取rom guid的app是否存在
	 * @return
	 */
	public static boolean isRomWupApkExist(Context context) {
		if (mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_NO_INIT) {
		    initWupHostInfo(context);
		}
		
		return mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_APP_IN_ROM;
	}
	
	/**
	 * 是否是rom 源码framework集成模式
	 * @param context
	 * @return
	 */
	public static boolean isSysRomSrcMode(Context context) {
	       if (mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_NO_INIT) {
	            initWupHostInfo(context);
	        }
	        
	        return mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_SYS_ROM_SRC;
	}
	
	/**
	 * app运行在sdk源码集成的rom上
	 * @param context
	 * @return
	 */
	public static boolean isAppInSysRomSrcMode(Context context) {
	    if (mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_NO_INIT) {
            initWupHostInfo(context);
        }
	    return mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_APP_IN_ROM_SRC;
	}
	
	/**
	 *  是否是Q Rom framework 集成的系统
	 * @return 
	 */
	public static boolean isQRomSys(Context context) {
        if (mSdkRunMode == WUP_RUNTIME_FLG.WUP_RUNTIME_NO_INIT) {
            initWupHostInfo(context);
        }
	    return mIsSrcFrameWorkRom;
	}
		
	/**
	 * 获取wup 测试用代理地址
	 * @return
	 */
	public static String getWupTestProxyAddr() {
	    
        if (QWupStringUtil.isEmpty(WUP_TEST_PROXY_ADDR_DB)) {
            QWupLog.trace(TAG, "getWupTestProxyAddr -> test addr is null, used test default!");
            return QWupSdkConstants.REMOTE_WUP_PROXY_TEST;
        }
		return WUP_TEST_PROXY_ADDR_DB;
	}
	
	/**
	 * 获取wup指定调试用代理地址
	 * @return
	 */
	public static String getWupDebugProxyAddr() {
	    
        if (QWupStringUtil.isEmpty(WUP_TEST_PROXY_ADDR_DB)) {
            QWupLog.trace(TAG, "getWupDebugProxyAddr -> test addr is null, used release default!");
            return QWupSdkConstants.REMOTE_WUP_PROXY;
        }
        return WUP_TEST_PROXY_ADDR_DB;
	}
	
	/**
	 * 获取wup socket 代理地址
	 * @return
	 */
	public static String getWupTestSocketProxyAddr() {
	    
	    if (QWupStringUtil.isEmpty(WUP_TEST_SOCKET_PROXY_ADDR_DB)) {
            QWupLog.trace(TAG, "getWupTestSocketProxyAddr -> test addr is null, used test default!");
            return QWupSdkConstants.REMOTE_WUP_SOCKET_PROXY_TEST;
        }
		return WUP_TEST_SOCKET_PROXY_ADDR_DB;
	}
	
	   /**
     * 获取wup socket指定调试用代理地址
     * @return
     */
    public static String getWupDebugSocketProxyAddr() {
        
        if (QWupStringUtil.isEmpty(WUP_TEST_PROXY_ADDR_DB)) {
            QWupLog.trace(TAG, "getWupDebugSocketProxyAddr -> test addr is null, used release default!");
            return QWupSdkConstants.REMOTE_WUP_SOCKET_PROXY;
        }
        return WUP_TEST_PROXY_ADDR_DB;
    }
	
    /**
     * 获取制定方法的返回值
     * @param functionName
     * @return
     */
    private static void initAppInfoByAppConfig(String clazz) {
    	
    	if (!QWupStringUtil.isEmpty(APP_PACKAGE_NAME_DB)) {  // 已拉到配置信息
    		return;
    	}
    	
        try {
            Class<?> clzz = Class.forName(clazz);
            
            QRomWupBaseConfig config = (QRomWupBaseConfig) clzz.newInstance();
            APP_PACKAGE_NAME_DB = config.getAppPackageName();
            // 是否强制运行在测试环境
            M_WUP_RUN_TEST_DEBUG = config.isRunTestForced();
            
            // 设置测试地址
            WUP_TEST_PROXY_ADDR_DB = QWupUrlUtil.resolvValidUrl(config.getTestWupProxyAddr());                        
			WUP_TEST_SOCKET_PROXY_ADDR_DB 
			        = QWupUrlUtil.resolvValidUrl(config.getTestWupSocketProxyAddr());
			
			// 是否使用指定的调试地址标识
			M_WUP_USED_DEBUG_ADDR = config.isForcedUsedDebugAddress();			 
			// 初始化app的发布模式
			initWupAppPublishMode();
        } catch (Exception e) {
        	APP_PACKAGE_NAME_DB = null;
            QWupLog.w(TAG, "initAppInfoByAppConfig -> err msg : " + e.getMessage());
        }        
    }
    
    /**
     * 检测wup配置的合法性
     */
    public static void checkConfigValidy() {
        // 反射指定app的配置文件
    	initAppInfoByAppConfig(getWupConfigFileInfo());
    	if (QWupStringUtil.isEmpty(APP_PACKAGE_NAME_DB)) {
    	    // rom 反射rom用配置文件
    	    initAppInfoByAppConfig(QRomWupBuindInfoDB.CONFIG_CLASSPATH_ROM_SRC_WUP_CONFIGCLASS_DB);
    	}
    	
    	if (QWupStringUtil.isEmpty(APP_PACKAGE_NAME_DB)) {
    	    M_APP_PUBLISH_MODE_DEBUG = false;
    		throw new IllegalArgumentException("please check the wup config file is alread exist: " + getWupConfigFileInfo());
    	}
    }
    
    /**
     * 初始化app的发布模式是否是debug版本
     */
    private static void initWupAppPublishMode() {
        try {
            Class<?> clz = Class.forName(
                    getAppPackageName() + ".BuildConfig");
            Field f = clz.getField("DEBUG");
            M_APP_PUBLISH_MODE_DEBUG = f.getBoolean(null);
            QWupLog.i(TAG, "initWupAppPublishMode-> M_APP_PUBLISH_MODE_DEBUG = "
                    + M_APP_PUBLISH_MODE_DEBUG);
        } catch (Throwable e) {
            M_APP_PUBLISH_MODE_DEBUG = false;
            QWupLog.w(TAG, "initAppPublishMode-> err: " + e + ", msg: " + e.getMessage());
        }
    }
}
