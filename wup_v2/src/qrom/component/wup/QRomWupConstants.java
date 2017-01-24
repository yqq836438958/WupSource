package qrom.component.wup;

import qrom.component.wup.base.utils.StringUtil;

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
    
    // update from QRomWupInfo by wileywang
    static final byte[] DEFAULT_GUID_BYTES = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    /** 默认guid 即未获取到guid的时候 */
    static final String DEFAULT_GUID_STR = StringUtil.byteToHexString(DEFAULT_GUID_BYTES);
    
    /** 默认guid长度 */
    static final int DEFAULT_GUID_LEN = DEFAULT_GUID_BYTES.length;
    /** guid 基本长度 */
    static final int DEFAULT_GUID_BASE_LEN = 4;
    
    // ============update end ===============
    
    // 这些变量全部被公开化， 很多不应该被公开的内容也被公开
    /** 默认guid长度 */
	public static final int WUP_GUID_DEFAULT_LEN = DEFAULT_GUID_BYTES.length;
	/** 默认guid -- byte[] 16个0 */
	public static final byte[] WUP_DEFAULT_GUID = DEFAULT_GUID_BYTES;	   
	
	/** 默认guid -- String WUP_DEFAULT_GUID的16进制字符串*/
    public static final String WUP_DEFAULT_GUID_STR = QRomWupDataBuilder.byteToHexString(WUP_DEFAULT_GUID);
    
    public static class BASEINFO_ERR_CODE {
        
        /** baseinfo --错误码前缀 */
        public static final String QIME_ERR_CODE_SUFF = "-";
        /** baseinfo -- login上报是qimei为空 */
        public static final String QIME_REPORT_EMPTY_CODE = "-20001";
        /** baseinfo -- 初始化时是qimei为空 */
        public static final String QIME_INIT_EMPTY_CODE = "-20002";
        /** baseinfo -- 未找到统计sdk */
        public static final String QIME_NO_FIND_STAT = "-20003";
    }
	
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
    
    
    public static class WUP_ERROR_CODE {
    	/** 请求数据为空 */
        public static final int WUP_TASK_ERR_REQDATA_EMPTY = -1;
        /** 返回数据为空 */
        public static final int WUP_TASK_ERR_RSPDATA_EMPTY = -2;
        /** 服务器返回码错误 -- 非200 */
        public static final int WUP_TASK_ERR_SERVICE_RSPCODE = -3;
        
        // 这个错误码实际没有用
        /** 网络无效 */
        @Deprecated
        public static final int WUP_TASK_ERR_NETWORK_INVALID  = -4;
        
        // 这个错误码，目前主要用在网络异常中，修改名字，增加可读性，并且统一网络异常错误吗
        // 网络错误与错误码共享
        /** 出现异常 */
        @Deprecated
        public static final int WUP_TASK_ERR_EXCEPTION  = -5;
        public static final int WUP_NETWORK_ERROR = -5;
        
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
        
        /**
         *  wup 自身内部错误, 系统异常
         */
        public static final int WUP_INNER_ERROR = -17;
        
        /**
         *  以下异常用于wup内部网络错误分析，会被转化为通用的网络错误
         */
        public static final int WUP_CONNECTION_TIMEOUT = -18;  // 连接超时
        public static final int WUP_READ_TIMEOUT = -19; // 读超时
        public static final int WUP_CONNECTED_FAILED = -20; // 连接失败
        
        
        /** 解析回包失败 */
        public static final int WUP_PARSE_WUP_PACKET_FAILED = -60;
        
        
        public static final int WUP_TASK_TOO_FREQUENT = -90; // wup请求过度频繁，底层开始丢失
        
        
        
        /**其他-- 系统异常 */
        public static final int WUP_TASK_ERR_OTHER  = -99;
    }
}
