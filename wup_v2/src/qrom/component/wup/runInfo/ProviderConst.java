package qrom.component.wup.runInfo;

/**
 *  Provider使用的常量定义
 * @author wileywang
 *
 */
public class ProviderConst {
	
	public static class WUP_ROM_PROVIDER_COLUMN {
		/** guid */
		public static final String COLUMN_GUID = "guid";
		/** 网络接入点 */
		public static final String COLUMN_NET_APN_TYPE = "net_apn_type";
		/** wup代理地址前缀 -- 后面跟对应的apn类型 */
		public static final String COLUMN_PROXY_IPLIST = "proxy_iplist";
		/** wup socket 代理地址前缀-- 后面跟对应的apn类型 */
		public static final String COLUMN_PROXY_SOCKET_IPLIST = "socket_iplist";
		/** wup socket 代理地址前缀-- 后面跟对应的apn类型 */
		public static final String COLUMN_QROM_ID = "qrom_id";

		/** iplist 对应缓存时间 */
		public static final String COLUMN_IPLIST_UPDATE_TIME = "iplist_update_time";
		/** iplist 对应 客户端ip */
		public static final String COLUMN_IPLIST_CLIENTIP = "iplist_clientip";
		/** wif iplist 类型（如: 普通wup代理，wupsocket代理） */
		public static final String COLUMN_IPLIST_TYPE = "iplist_type";
		/** 执行对应操作的返回码 */
		public static final String COLUMN_OPERRSP = "oper_rsp_code";
		
		/* 描述接入点 */
		public static final String COLUMN_IP = "ip";
		public static final String COLUMN_PORT = "port";
		public static final String COLUMN_IPLIST_SIZE = "iplist_size";
		public static final String COLUMN_IP_INDEX = "ip_index";
		
	}

	public static class WUP_ROM_PROVIDER_OPER_INFO {
		/** 强制更新 iplist 信息 */
		public static final String OPER_UPDATE_IPLIST_FORCE = "update_iplist_force";
		/** 检测请求guid */
		public static final String OPER_REQUEST_GUID_CHECK = "request_guid_check";
	}
	
	public static class WUP_ROM_PROVIDER_ACTIONS {
	
		/** 获取guid */
		public static final String ACTION_GET_GUID = "getGuid";
	
		/** 获取wup 代理地址 */
		public static final String ACTION_GET_IPLIST_PROXY = "getProxyList";
	
		/** 获取wup socket 代理地址 */
		public static final String ACTION_GET_IPLIST_SOCKET = "getSocketList";

		public static final String ACTION_GET_ROM_ID = "getRomId";
		
		/** 同步之前host的guid */
		public static final String ACTION_SYN_HOST_ROM_GUID = "synHostRomGuid";

		/** 获取对应wifi下的iplist缓存 */
		public static final String ACTION_GET_IPLIST_WIFI = "getIplistWifi";

		/** 执行对应操作id的操作 */
		public static final String ACTION_DO_SPE_OPER = "doSpeOper";
		
		
		/** 选择接入的IP节点  **/
		public static final String ACTION_SELECT_IPPORT = "selectIPPort";
		
		/** 获取当前接入点列表 **/
		public static final String ACTION_GET_APN_IPLIST = "getApnIPList";
		
		/** 上报接入点错误情况 */
		public static final String ACTION_REPORT_IPPORT_ERROR = "reportIPPortError";
	}
	
	// 操作的参数定义
	public static class WUP_ROM_PROVIDER_PARAMS {
		public static final String PARAM_ENV_TYPE = "envType";
		
		public static final String PARAM_IP_TYPE = "ipType";
		
		public static final String PARAM_APN_INDEX = "apnIndex";
		
		public static final String PARAM_BSSID = "bssid";
		
		public static final String PARAM_REPORT_IP = "reportIp";
		
		public static final String PARAM_REPORT_PORT = "reportPort";
		
		public static final String PARAM_ERRORCODE = "errorCode";
		
		public static final String PARAM_REPORT_IPLIST_SIZE = "ipListSize";
		
		public static final String PARAM_REPORT_IP_INDEX = "ipIndex";
		
		public static final String PARAM_REPORT_CLIENT_IP = "clientIP";
	}
	
	
	public static String WUP_PROVIDER_SUFFIX = ".wup.QRomProvider";
}
