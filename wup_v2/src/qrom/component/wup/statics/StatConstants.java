package qrom.component.wup.statics;

public class StatConstants {
	
	public static class KEY {
        /** wup 统计key -- SDK版本信息*/
        public static final String SDK_VER = "VER";
        /** wup 统计key -- 请求服务*/
        public static final String SERVANT = "ST";
        /** wup 统计key -- 请求功能*/
        public static final String FUNCTION = "FN";
        /** wup 统计key -- 加密类型 */
        public static final String ENCRYPT_TYPE = "ET";
        /** wup 统计key -- 请求结果 */
        public static final String RESULT = "RS";
        /** wup 统计key -- 连接时间*/
        public static final String TIME_CONNECT = "CO";
        /** wup 统计key -- 发送时间 */
        public static final String TIME_SEND = "SD";
        /** wup 统计key -- 接收时间 */
        public static final String TIME_RECEIVE = "RE";
        /** wup 统计key -- 请求数据长度 */
        public static final String REQDATA_LEN = "RDL";
        /** wup 统计key -- 响应数据长度 */
        public static final String RSPDATA_LEN = "SDL";
        /** wup 统计key -- 额外信息 */
        public static final String EXTRA = "EA";
        /** wup 统计key -- 非对策加密包名 */
        public static final String ASYM_APP_PACKAGE = "AP";
        /** wup 统计key --非对称加密root key */
        public static final String ASYM_ROOTKEY = "RK";
        /** wup 统计key --非对称加密app key */
        public static final String ASYM_APPKEY = "AK";
        /** wup 统计key -- 非对称加密app key */
        public static final String ASYM_START_DURATION = "PSD";
        /** wup 统计key -- 非对称加密 解密接口 */
        public static final String ASYM_DECRYPT = "DK";
        /** wup 统计key --请求网络状态 */
        public static final String REQ_NETSTAT = "NWT";
        /** wup 统计key --请求服务器url */
        public static final String REQ_URL = "SURL";
        /** wup 统计key --请求Session */
        public static final String SESSION = "SEN";
        /** wup 统计key --校验网络请求 */
        public static final String CHECK_FAIL = "NCK";
        /** wup 统计key --请求ip的索引等信息 */
        public static final String IPLIST_INFO = "SIP";
        /** wup 统计key -- 请求iplist时客户端ip */
        public static final String IPLIST_CLENT_IP = "CIP";
    }
    
    public static class VALUE {
        
        /** wup 统计常量-- 加密类型：普通 */
        public static final String ENCRYPT_TYPE_NORMAL = "0";
        
        /** wup 统计常量-- 加密类型：非对策加密 */
        public static final String ENCRYPT_TYPE_ASYM = "1";
        
        /** wup 统计常量-- 加密类型：其他类型 */
        public static final String ENCRYPT_TYPE_OTHER = "9";
        
        /** wup 统计常量-- 请求结果 ： 成功 */
        public static final String RESULT_SUCCESS = "1";
        /** wup 统计常量-- 请求结果 ： 失败 */
        public static final String RESULT_FAIL = "0";
    }
	
}
