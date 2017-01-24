package qrom.component.wup.build;

import qrom.component.wup.utils.QWupStringUtil;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

public class QWupUriFactory {

    
    private static final String ROM_WUP_AUTHORIT_SUFF = ".wup.QRomProvider";
    
    /** 获取guid */
    private static final String ACTION_GET_GUID = "getGuid";
    /** 获取wup 代理地址 */
    private static final String ACTION_GET_IPLIST_PROXY = "getProxyList";
    /** 获取wup socket 代理地址 */
    private static final String ACTION_GET_IPLIST_SOCKET = "getSocketList";
    
    private static final String ACTION_GET_ROM_ID = "getRomId";
    /** 同步之前host的guid */
    private static final String ACTION_SYN_HOST_ROM_GUID = "synHostRomGuid";
    
    /** 获取对应wifi下的iplist缓存 */
    private static final String ACTION_GET_IPLIST_WIFI = "getIplistWifi";
    
    /** 执行对应操作id的操作  */
    private static final String ACTION_DO_SPE_OPER = "doSpeOper";
    
    public static final int URI_MATCH_GET_GUID = 1;
    public static final int URI_MATCH_GET_IPLIST_PROXY = 2;
    public static final int URI_MATCH_GET_IPLIST_SOCKET = 3;
    public static final int URI_MATCH_GET_ROM_ID = 4;
    public static final int URI_MATCH_SYN_HOST_ROM_GUID = 5;
    public static final int URI_MATCH_GET_IPLIST_WIFI = 6;
    public static final int URI_MATCH_DO_SPE_OPER = 7;
        
    /** wup rom 层获取wup数据的provider  */
    private static String ROM_WUP_AUTHORITY = null;
    
    /** wup 当前app作为host的 provider */
    private static String HOST_APP_WUP_AUTHORITY = null;
    
    private static Uri URI_ROM_AUTHORITY = null;
    /** 获取Rom guid */
    private static Uri URI_WUP_GET_GUID = null;
    /** 获取 iplist */
    private static Uri URI_WUP_GET_IPLIST_PROXY = null;
    /** 获取 socket iplist*/
    private static Uri URI_WUP_GET_IPLIST_SOCKET = null;
    /** 获取 rom id*/
    private static Uri URI_WUP_GET_ROM_ID = null;
    /** 获取 wifi下iplist信息*/
    private static Uri URI_WUP_GET_WIFI_IPLIST = null;
    /** 执行对应操作 */
    private static Uri URI_WUP_DO_SPE_OPER = null;
    
    public static final UriMatcher URI_ROM_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static boolean M_IS_INIT = false;

    public static void init(Context context) {
        
        if (!M_IS_INIT) {  // rom 模式            
            // 初始化相关uri信息
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_GET_GUID, URI_MATCH_GET_GUID);
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_GET_IPLIST_PROXY, URI_MATCH_GET_IPLIST_PROXY);
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_GET_IPLIST_SOCKET, URI_MATCH_GET_IPLIST_SOCKET);
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_GET_ROM_ID, URI_MATCH_GET_ROM_ID);
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_SYN_HOST_ROM_GUID, URI_MATCH_SYN_HOST_ROM_GUID);
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_GET_IPLIST_WIFI, URI_MATCH_GET_IPLIST_WIFI);
            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_DO_SPE_OPER, URI_MATCH_DO_SPE_OPER);
            M_IS_INIT = true;
//        } else {
//            URI_ROM_MATCHER.addURI(getRomBaseAuthority(), ACTION_GET_GUID, URI_MATCH_GET_GUID);
//            URI_ROM_MATCHER.addURI(getHostAppBaseAuthority(), ACTION_SYN_HOST_ROM_GUID, URI_MATCH_SYN_HOST_ROM_GUID);
        }
    }
    
    private static Uri getRomBaseAuthorUri() {
        
        if (URI_ROM_AUTHORITY == null) {
            URI_ROM_AUTHORITY = Uri.parse("content://" + getRomBaseAuthority());
        }
        return URI_ROM_AUTHORITY;
    }
    
    private static String getRomBaseAuthority() {
        if (QWupStringUtil.isEmpty(ROM_WUP_AUTHORITY)) {
            ROM_WUP_AUTHORITY = getWupAuthorityByPackageName(QRomWupBuildInfo.getRomPackageName());
        }
        return ROM_WUP_AUTHORITY;
    }
    
    private static String getHostAppBaseAuthority() {
        if (QWupStringUtil.isEmpty(HOST_APP_WUP_AUTHORITY)) {
            HOST_APP_WUP_AUTHORITY = getWupAuthorityByPackageName(QRomWupBuildInfo.getAppPackageName());
        }
        return HOST_APP_WUP_AUTHORITY;
    }
    
    /**
     * 根据package name获取provier的authority
     * @param pkgName
     * @return
     */
    private static String getWupAuthorityByPackageName(String pkgName) {
        
        return pkgName + ROM_WUP_AUTHORIT_SUFF;
    }
    
    public static Uri getRomGuidUri() {
        
        if (URI_WUP_GET_GUID == null) {
            
            URI_WUP_GET_GUID = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_GUID);
        }
        return URI_WUP_GET_GUID;
    }
    
    public static Uri getRomProxyIpListUri() {
        if (URI_WUP_GET_IPLIST_PROXY == null) {
            
            URI_WUP_GET_IPLIST_PROXY = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_IPLIST_PROXY);
        }
        
        return URI_WUP_GET_IPLIST_PROXY;
    }
    
    public static Uri getRomWifiIpListUri() {
        if (URI_WUP_GET_WIFI_IPLIST == null) {
            
            URI_WUP_GET_WIFI_IPLIST = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_IPLIST_WIFI);
        }
        
        return URI_WUP_GET_WIFI_IPLIST;
    }
    
    public static Uri getRomProxySocketIpListUri() {
        if (URI_WUP_GET_IPLIST_SOCKET == null) {
            
            URI_WUP_GET_IPLIST_SOCKET = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_IPLIST_SOCKET);
        }
        
        return URI_WUP_GET_IPLIST_SOCKET;
    }
    
    public static Uri getRomIdUri() {
        if (URI_WUP_GET_ROM_ID == null) {
            
            URI_WUP_GET_ROM_ID = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_ROM_ID);
        }
        return URI_WUP_GET_ROM_ID;
    }
    
    /**
     * 获取同步其他rom模式guid的uri
     * @param pkgName
     * @return
     */
    public static Uri getSynHostGuidUri(String pkgName) {
        Uri pkgAuthor = Uri.parse("content://" + getWupAuthorityByPackageName(pkgName));
        return Uri.withAppendedPath(pkgAuthor, ACTION_SYN_HOST_ROM_GUID);
    }
    
    public static Uri getDoSpeOperUri() {
        if (URI_WUP_DO_SPE_OPER== null) {
            
            URI_WUP_DO_SPE_OPER = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_DO_SPE_OPER);
        }
        
        return URI_WUP_DO_SPE_OPER;
    }
    
}
