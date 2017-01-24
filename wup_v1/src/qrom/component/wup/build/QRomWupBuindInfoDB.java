package qrom.component.wup.build;

public class QRomWupBuindInfoDB {
	
	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓以下变量由脚本负责修修改，勿随意修改变量名 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
		
	/** ROM 使用wup模块的包名 */
	public static final String ROM_SDK_APP_PACKAGE_NAME_DB ="com.tencent.qrom.tms.tcm";
	/** ROM 使用wup模块的底层servant name */
	public static final String ROM_SDK_ROM_SERVICE_NAME_DB = "qrom.component.push.core.TCMService";
	
    /** 使用sdk的app的package name */
    public static String APP_PACKAGE_NAME_DB = "";
    
    /** 回调获配置信息类信息 */
    public static final String CONFIG_CLASSPATH_WUP_CONFIGCLASS_DB = "qrom.component.config.QRomWupConfig";
    
    /** rom framewok 源码模式回调获配置信息类信息 */
    public static final String CONFIG_CLASSPATH_ROM_SRC_WUP_CONFIGCLASS_DB = "qrom.component.config.QRomFrameWupConfig";

}
