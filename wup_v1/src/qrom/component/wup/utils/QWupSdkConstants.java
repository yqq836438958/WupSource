/**   
* @Title: QubeConstant.java 
* @Package com.tencent.qube 
* @author interzhang   
* @date 2012-5-16 下午01:18:02 
* @version V1.0   
*/
package qrom.component.wup.utils;


public final class QWupSdkConstants {

    private QWupSdkConstants() {
    }
    
    /**
     * 一小时的毫秒数
     */
    public static final int MILLIS_FOR_HOUR = 60 * 60000;
    
    /** 一天的毫秒数 */
    public static final int MILLIS_FOR_DAY = 24 * MILLIS_FOR_HOUR;
    /**
     * 一分钟的毫秒数
     */
    public static final int MILLIS_FOR_MINUTE = 60000;
    /**yyy 一秒毫秒数 */
    public static final int MILLIS_FOR_SECOND = 1000;
    
    /**yyy 远程请求模块 -- notify service */
    public static final int REMOTE_MODEL_TYPE_WUP = -1000;
    
    public static final String REMOTE_SERVICE_ADDRESS_SEPARATOR = ":";
    /** 小工具查询guid 广播*/
    public static final String ACTION_WUP_TOOL_GET_GUID = ".get.tencent.qrom.tools.wup.guid";
    /** 小工具接收guid 广播*/
    public static final String ACTION_WUP_TOOL_SHOW_GUID = "show.tencent.qrom.tools.wup.guid";
    
    /** log sdk发送获取ticket 广播*/
    public final static String ACTION_WUP_LOGSDK_GETTICKET_INFO = ".qrom.intent.action.wup.logsdk.getLogTicket";
    /** log sdk 发送ticket相关信息 */
    public final static String ACTION_WUP_LOGSDK_REPORT_LOG_INFO = ".qrom.intent.action.REPORT_LOG_INFO";
    
    /** wup sdk -- 数据更新广播*/
    public static final String ACTION_WUP_SDK_BASEDATA_UPDATED = "qrom.component.wup.sdk.baseData.updated";
    /** wup sdk -- 向rom sys请求数据 */
    public static final String ACTION_WUP_SDK_ROMSYS_REQDATAS = "qrom.component.wup.sdk.romsys.ReqDatas";
    /** wup sdk -- rom sys响应数据 */
    public static final String ACTION_WUP_SDK_ROMSYS_RSPDATAS = "qrom.component.wup.sdk.romsys.RspDatas";
    /** wup sdk -- rom sys iplist数据 */
    public static final String ACTION_WUP_SDK_ROMSYS_IPDATAS = ".qrom.component.wup.sdk.romsys.RspDatas.iplist";
    
    /** wup 通过push接收广播发起IpList更新请求  **/
    public static final String ACTION_WUP_SDK_UPDATE_IPLIST = "qrom.component.push.action.updateIplist";
    
    public static final String ACTION_WUP_SDK_PARM_FLG_TYPE = "req_type";
    public static final String ACTION_WUP_SDK_PARM_FLG_PKG = "pkg_name";
    public static final String ACTION_WUP_SDK_PARM_FLG_DATAS = "datas";
    public static final String ACTION_WUP_SDK_PARM_FLG_EXTRA = "extra";
    
    //  ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ log sdk发送数据的key ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    
    public static final String LOGSDK_PARAM_KEY_APP_PKGNAME = "app_pkgName";
    public static final String LOGSDK_PARAM_KEY_REPORT_RESID = "report_resId";
    public static final String LOGSDK_PARAM_KEY_TICKET_TIMEOUT = "ticket_timeout";
    public static final String LOGSDK_PARAM_KEY_REPORT_PID = "report_pid";
    public static final String LOGSDK_PARAM_KEY_REPORT_EXTRA_DATA = "report_extra_data";
    public static final String LOGSDK_PARAM_KEY_APP_TICKET = "app_ticket";
    public static final String LOGSDK_PARAM_KEY_APP_TICKET_RSPCODE = "log_ticket_rspcode";
    public static final String LOGSDK_PARAM_KEY_ENV_FLG = "env_flg";
    //  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ log sdk发送数据的key ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
    
    /**yyy wup代理地址 */
    public static final String REMOTE_WUP_PROXY = "http://w.html5.qq.com:8080";

    /**yyy wup代理地址 -- 测试地址 */
    public static final String REMOTE_WUP_PROXY_TEST = "http://223.167.80.29:55555";
    
    public static final String  REMOTE_WUP_SOCKET_PROXY_HOST = "wl.html5.qq.com";
    public static final int  REMOTE_WUP_SOCKET_PROXY_PORT = 8080;
    /**yyy wup长连接代理地址 */
    public static final String REMOTE_WUP_SOCKET_PROXY = 
            REMOTE_WUP_SOCKET_PROXY_HOST + REMOTE_SERVICE_ADDRESS_SEPARATOR + 
            String.valueOf(REMOTE_WUP_SOCKET_PROXY_PORT);
    
//    public static final String REMOTE_WUP_SOCKET_PROXY_TEST = "114.80.102.182:18002"; 
    //yyy
    public static final String REMOTE_WUP_SOCKET_PROXY_TEST = "223.167.80.29:55141"; 
    
    // wup task超时时间
    public static final int WUP_TIME_OUT = 60000;    
//    public static final int WUP_TIME_OUT = 5000;    
    
    /** wup sdk内部请求login的超时时间  */
    public static final int WUP_INNER_REQ_TIMEOUT = 5 * MILLIS_FOR_MINUTE;
    /** wup sdk内部请求IPLIST的请求最短超时时间  */
    public static final int WUP_IPLIST_REQ_TIMEOUT = 3 * MILLIS_FOR_MINUTE;
    /** wup sdk内部请求IPLIST -- APP向rom发起更新iplist请求最小超时时间  */
    public static final int WUP_IPLIST_REQ2ROM_TIMEOUT = 30 * MILLIS_FOR_MINUTE;
//    public static final int WUP_IPLIST_REQ_TIMEOUT = MILLIS_FOR_MINUTE / 10;
    /** IPLIST 缓存超时时间*/
    public static final int WUP_IPLIST_CACHE_TIMEOUT =  10 * MILLIS_FOR_HOUR;
    
    /** wup sdk内部请求的最小时间间隔 */
    public static final int WUP_INNER_REQ_MIN_INTERVAL = MILLIS_FOR_MINUTE / 4;
    
    /** wup 加密key */
    //    public static final String WUP_ENCRYPT_KEY = "sDf434ol*123+-KD";
    public static final byte[] WUP_ENCRYPT_BYTES_KEY = {0x73, 0x44, 0x66, 0x34, 0x33, 0x34, 0x6F, 0x6C, 0x2A, 
            0x31, 0x32, 0x33, 0x2B, 0x2D, 0x4B, 0x44};
    
    public static final int NET_CHANGE_INTERVAL_TIME = MILLIS_FOR_SECOND  * 2;
    
    /** sd卡guid确认延时时间 */
    public static final int GUID_SD_STATE_CHECK_TIME_DELAY = MILLIS_FOR_SECOND  * 5;
    /** sd卡guid确认延时 重试最大次数 */
    public static final int GUID_SD_STATE_CHECK_MAX_CNT = 2;    
       
    public static class LOGIN_RSP_CODE {

        /** login 返回码 -- 请求已发送 */
        public static final int LOGIN_REQ_SENDING = 9999;
        /** login 返回码 -- guid已获取（仅在主动拉取guid时返回） */
        public static final int LOGIN_CANCEL_GUID_OK = 0;
        /** login 返回码 -- 正在执行login */
        public static final int LOGIN_CANCEL_RUNING = -1;
        /** login 返回码 -- 执行太频繁 */
        public static final int LOGIN_CANCEL_FREQ = -2;
        /** login 返回码 -- SD卡未准备好，延时 */
        public static final int LOGIN_DELAY_SD_NOREADY= -3;
        /** login 返回码 -- 其他进程执行可能执行了 */
        public static final int LOGIN_DELAY_OTHE_RUN= -4;
        /** login 返回码 -- 请求失败 */
        public static final int LOGIN_REQ_FAIL= -5;
        /** login 返回码 -- 不需要请求 */
        public static final int LOGIN_CANCEL_NONEED= -6;
    }
    
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
    
    /**
     * 统计上报的iplist的索引类型
     * @author sukeyli
     *
     */
    public static class IPLIST_ERR_INDEX {
        /** 无缓存的iplist信息 */
        public static final int IPLIST_EMPTY = -100;
        /** 所有iplist 数据无效 */
        public static final int IPLIST_INVALID = -101;
        /** 测试环境 ip */
        public static final int IPLIST_TEST_ENV = -110;
        /** app自定义ip */
        public static final int IPLIST_SELF_DEFINE = -120;
    }
    
    /**
     * wup请求操作类型（从10000开始编号）
     *   -- 不可同qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE的常量值相同
     *   -- aild都使用IQRomWupService.getWupDataByType接口请求数据
     * @author sukeyli
     *
     */
    public static class WUP_OPER_TYPE {
        /** wup 操作 -- 向rom强制请求iplist */
        public static final int OPER_WUP_DEFAULT = 10000;
        /** wup 操作 -- 向rom强制请求iplist */
        public static final int OPER_UPDATE_IPLIST2ROM = OPER_WUP_DEFAULT + 1;
    }
    
    /**
     * iplist请求类型
     * @author sukeyli
     *
     */
    public static class IPLIST_REQ_TYPE {
        /** iplist请求 -- 检测缓存超时时间等正常流程 */
        public static final int IPLIST_REQ_NORMAL = 0;
        /** iplist请求 -- 忽略缓存超时时间，仅检测屏幕状态及发送频率等 */
        public static final int IPLIST_REQ_IGNOR_CACHETIMEOUT = 1;
        /** iplist请求 -- 通知rom更新iplist请求 */
        public static final int IPLIST_REQ_ROM_UPDATE = 2;
    }
    
    /**
     * login请求类型
     * @author sukeyli
     *
     */
    public static class LOGIN_REQ_TYPE {
        /** 普通login请求 */
        public static final int LOGIN_REQ_DOLOGIN = 0;
        /** 主动获取guid的login请求 */
        public static final int LOGIN_REQ_GETGUID = 1;
    }
}
