package qrom.component.wup;

import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.wupData.QRomWupInfo;

public class QRomWupConstants {

    /**
     * WUP SDK 版本信息
     *    --格式：修改时间.版本信息
     *    
     *    ver1: 20141127.1.1 -- 加入初始版本信息号(统计wup 请求发送情况，加入版本信息)
     *    ver2: 20141202.1.2 -- wup统计加入请求url，及网络类型
     *    ver3: 20141205.1.3 -- wup统计加入统计session（用于后台去重）
     *    ver4: 20141216.1.4 -- wup统计加入统计NCK（用于检测网络是否联通）
     *    ver5: 20141230.1.5 -- wup统计加入统计iplist的个数及索引，修改iplist轮询逻辑
     *    ver6: 20150115.1.6 -- wup修改iplist轮询逻辑,统计增加当前iplist的apntype
     *    
     */
    public static final String WUP_SDK_VER ="20150115.1.6";
    
    /** 默认guid长度 */
	public static final int WUP_GUID_DEFAULT_LEN = QRomWupInfo.DEFAULT_GUID_LEN;
	/** 默认guid -- byte[] 16个0 */
	public static final byte[] WUP_DEFAULT_GUID = QRomWupInfo.DEFAULT_GUID_BYTES;	   
	/** 默认guid -- String WUP_DEFAULT_GUID的16进制字符串*/
    public static final String WUP_DEFAULT_GUID_STR = QRomWupDataBuilder.byteToHexString(WUP_DEFAULT_GUID);
	
	/**wup 默认http 代理地址 */
	public static final String WUP_HTTP_PROXY_DEFAULT = QWupSdkConstants.REMOTE_WUP_PROXY;
	/**wup 默认socket 代理地址 */
	public static final String WUP_SOCKET_PROXY_DEFALUT = QWupSdkConstants.REMOTE_WUP_SOCKET_PROXY;
	
	/**
	 * wup的运行模式
	 * @author sukeyli
	 *
	 */
	public static class WUP_RUNTIME_FLG {
	    /** WUP SDK 未初始化 */
	    public static final int WUP_RUNTIME_NO_INIT =-1;
		/** WUP SDK 所在的app 运行在 rom上 */
		public static final int WUP_RUNTIME_APP_IN_ROM =1;
		/** WUP SDK 所在的app 运行在 独立模式 */
		public static final int WUP_RUNTIME_APP_ALONE =2;
		/** WUP SDK 所在的 rom 底层运行 （管理rom guid） */
		public static final int WUP_RUNTIME_ROM_APK =3;
		/** WUP SDK 集成在sys 源码里（管理rom guid） */
		public static final int WUP_RUNTIME_SYS_ROM_SRC =4;
		/** WUP SDK 所在的app运行在 源码集成的rom中 */
		public static final int WUP_RUNTIME_APP_IN_ROM_SRC =5;
	}
	
	/**
	 * wup 数据类型
	 * 
	 * @author sukeyli
	 *
	 */
	public static class WUP_DATA_TYPE {
	    /*
	     *  数据类型的取值不要超过1000
	     *  数据类型不可与qrom.component.wup.utils.QWupSdkConstants.WUP_OPER_TYPE中的类型重复
	     *  rom framework模式下，通过aidl接口获取对应数据(IQRomWupService.getWupDataByType）  
	     */
		/** 所有wup数据 */
		public static final int WUP_DATA_ALL = 0;
		/** guid */
		public static final int WUP_DATA_GUID = 1;
		/** wup iplist*/
		@Deprecated
		public static final int WUP_DATA_IPLIST = 2;
		/** wup socket iplist */
		@Deprecated		
		public static final int WUP_DATA_SOCKET_IPLIST = 4; 
		
		/** ROMID */
		public static final int WUP_DATA_ROMID = 8; 
        /** 新 wup iplist*/
      public static final int WUP_DATA_IPLIST_NEW = 10;
      /** 新 wup wifi下bssid对应iplist*/
      public static final int WUP_DATA_IPLIST_WIFI = 11;
        /** 新wup socket iplist */
      public static final int WUP_DATA_SOCKET_IPLIST_NEW = 20; 
      /** 新 wup wifi下bssid对应的 socket iplist */
      public static final int WUP_DATA_SOCKET_IPLIST_WIFI = 21; 
	}	

	public static class WUP_START_MODE {
		/** wup 启动模式 -- 主动获取guid */
		public static final int WUP_START_GET_GUID = 0;
	}
	
    /**
     * WUP 代理地址的网络类型
     */
	public static class WUP_IPLIST_NET_TYPE {

	    public static final int             IP_LIST_CMWAP   = ApnStatInfo.PROXY_LIST_CMWAP;
	    public static final int             IP_LIST_CMNET    = ApnStatInfo.PROXY_LIST_CMNET;
	    public static final int             IP_LIST_UNWAP   = ApnStatInfo.PROXY_LIST_UNWAP;
	    public static final int             IP_LIST_UNNET    = ApnStatInfo.PROXY_LIST_UNNET;
	    public static final int             IP_LIST_CTWAP    = ApnStatInfo.PROXY_LIST_CTWAP;
	    public static final int             IP_LIST_CTNET     = ApnStatInfo.PROXY_LIST_CTNET;
	    public static final int             IP_LIST_WIFI        = ApnStatInfo.PROXY_LIST_WIFI;
	    public static final int             IP_LIST_3GWAP   = ApnStatInfo.PROXY_LIST_3GWAP;
	    public static final int             IP_LIST_3GNET    = ApnStatInfo.PROXY_LIST_3GNET;
	    public static final int             IP_LIST_4G          = ApnStatInfo.PROXY_LIST_4G;
	    public static final int             IP_LIST_UNKOWN    = ApnStatInfo.PROXY_LIST_UNKOWN;
	}
	
    public static class LOGIN_RSP_CODE {

        /** login 返回码 -- 请求已发送 */
        public static final int LOGIN_REQ_SENDING = QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_REQ_SENDING;
        /** login 返回码 -- 正在执行login */
        public static final int LOGIN_CANCEL_RUNING = QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_CANCEL_RUNING;
        /** login 返回码 -- 执行太频繁 */
        public static final int LOGIN_CANCEL_FREQ = QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_CANCEL_FREQ;
        /** login 返回码 -- SD卡未准备好，延时 */
        public static final int LOGIN_DELAY_SD_NOREADY= QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_DELAY_SD_NOREADY;
        /** login 返回码 -- 其他进程执行可能执行了 */
        public static final int LOGIN_DELAY_OTHE_RUN= QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_DELAY_OTHE_RUN;
        /** login 返回码 -- 请求失败 */
        public static final int LOGIN_REQ_FAIL= QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_REQ_FAIL;
        /** login 返回码 -- 不需要请求 */
        public static final int LOGIN_CANCEL_NONEED= QWupSdkConstants.LOGIN_RSP_CODE.LOGIN_CANCEL_NONEED;
    }

    public static class IPLIST_RSP_CODE {
        /** IPLIST 返回码 -- 请求已发送 */
        public static final int IPLIST_REQ_SENDING = 9999;
        /** IPLIST 返回码 -- 正在执行请求中 */
        public static final int IPLIST_CANCEL_RUNING = -1;
        /** IPLIST 返回码 -- 执行太频繁 */
        public static final int IPLIST_CANCEL_FREQ = -2;
        /** IPLIST 返回码 -- 无网络 */
        public static final int IPLIST_CANCEL_NO_NET = -3;
        /** IPLIST 返回码 -- 当前apn参数错误 */
        public static final int IPLIST_CANCEL_APN_ERR = -4;
        /** IPLIST 返回码 -- 缓存未超时 */
        public static final int IPLIST_CANCEL_NO_TIMEOUT = -5;
        /** IPLIST 返回码 -- 黑屏 */
        public static final int IPLIST_CANCEL_SCREEN_OFF = -6;
        /** IPLIST 返回码 -- 请求失败 */
        public static final int IPLIST_REQ_FAIL= -10;
        /** IPLIST 返回码 -- 测试环境不拉取 */
        public static final int IPLIST_CANCEL_TEST_ENV = -99;
        
        
    }
    
    public static class WUP_ERROR_CODE {
    	/** 请求数据为空 */
        public static final int WUP_TASK_ERR_REQDATA_EMPTY = -1;
        /** 返回数据为空 */
        public static final int WUP_TASK_ERR_RSPDATA_EMPTY = -2;
        /** 服务器返回码错误 -- 非200 */
        public static final int WUP_TASK_ERR_SERVICE_RSPCODE = -3;
        /** 网络无效 */
        public static final int WUP_TASK_ERR_NETWORK_INVALID  = -4;
        /** 出现异常 */
        public static final int WUP_TASK_ERR_EXCEPTION  = -5;
        /** 解密/解压 解析数据失败 */
        public static final int WUP_TASK_ERR_RSP_PARSE  = -6;
        /** 任务强制超时 */
        public static final int WUP_TASK_ERR_FORCE_TIMEOUT  = -7;
        /** sesion获取失败 */
        public static final int WUP_TASK_ERR_GET_SESSION_FAIL  = -8;
        /** sesion 加密请求数据失败 */
        public static final int WUP_TASK_ERR_SESSION_ENCRYPT  = -9;
        /** sesion -- 非对称加密请求回包(wup 代理)解析失败 */
        public static final int WUP_TASK_ERR_SESSION_RSP_PROXY_PKG_PARSE  = -10;
        /** sesion -- 非对称加密请求回包返回码错误 */
        public static final int WUP_TASK_ERR_SESSION_RSP_CODE_FAILE  = -11;
        /** sesion -- 非对称请求数据预处理失败 */
        public static final int WUP_TASK_ERR_SESSION_REQ_DATA_FAILE  = -12;
        /** sesion -- 非对称请求业务调用失败 */
        public static final int WUP_TASK_ERR_SESSION_BUS_SERVICE_FAILE  = -13;
        /** sesion -- 非对称响应数据解压失败 */
        public static final int WUP_TASK_ERR_SESSION_RSP_DECODE_FAILE  = -14;
        /** sesion -- 非对称响应数据解密失败 */
        public static final int WUP_TASK_ERR_SESSION_RSP_DECCRYPT_FAILE  = -15;
        
        /** wup 已经被关闭  */
        public static final int WUP_CLOSED = -16;
        
        /**其他-- 系统异常 */
        public static final int WUP_TASK_ERR_OTHER  = -99;
    }
}
