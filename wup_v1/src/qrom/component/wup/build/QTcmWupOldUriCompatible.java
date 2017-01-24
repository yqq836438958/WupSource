package qrom.component.wup.build;

import android.net.Uri;

public class QTcmWupOldUriCompatible {

    private static final String ROM_WUP_AUTHORIT_SUFF = ".wup.QRomProvider";
    /** wup rom 层获取wup数据的provider  */
    private static String TCM_ROM_WUP_AUTHORITY = "com.tenctent.qrom.component" + ROM_WUP_AUTHORIT_SUFF;
    
    private static final String TCM_PACKAGE_NAME = "com.tencent.qrom.tms.tcm";
    
    /** 获取guid */
    private static final String ACTION_GET_GUID = "getGuid";
    /** 获取wup 代理地址 */
    private static final String ACTION_GET_IPLIST_PROXY = "getProxyList";
    /** 获取wup socket 代理地址 */
    private static final String ACTION_GET_IPLIST_SOCKET = "getSocketList";
    
    private static Uri URI_TCM_ROM_AUTHORITY = null;
    
    /** 获取Rom guid */
    private static Uri URI_OLD_TCM_WUP_GET_GUID = null;
    /** 获取 iplist */
    private static Uri URI_OLD_TCM_WUP_GET_IPLIST_PROXY = null;
    /** 获取 socket iplist*/
    private static Uri URI_OLD_TCM_WUP_GET_IPLIST_SOCKET = null;
    
    private static Uri getRomBaseAuthorUri() {
        
        if (URI_TCM_ROM_AUTHORITY == null) {
            URI_TCM_ROM_AUTHORITY = Uri.parse("content://" + getRomBaseAuthority());
        }
        return URI_TCM_ROM_AUTHORITY;
    }
    
    private static String getRomBaseAuthority() {
//        if (QWupStringUtil.isEmpty(TCM_ROM_WUP_AUTHORITY)) {
//            ROM_WUP_AUTHORITY = getWupAuthorityByPackageName(QRomWupBuildInfo.getRomPackageName());
//        }
        return TCM_ROM_WUP_AUTHORITY;
    }
    
    public static Uri getOldTcmRomGuidUri() {
        
        if (URI_OLD_TCM_WUP_GET_GUID == null) {
            
            URI_OLD_TCM_WUP_GET_GUID = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_GUID);
        }
        return URI_OLD_TCM_WUP_GET_GUID;
    }
    
    
    public static Uri getOldTcmRomProxyIpListUri() {
        if (URI_OLD_TCM_WUP_GET_IPLIST_PROXY == null) {
            
            URI_OLD_TCM_WUP_GET_IPLIST_PROXY = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_IPLIST_PROXY);
        }
        
        return URI_OLD_TCM_WUP_GET_IPLIST_PROXY;
    }
    
    public static Uri getOldTcmRomProxySocketIpListUri() {
        if (URI_OLD_TCM_WUP_GET_IPLIST_SOCKET == null) {
            
            URI_OLD_TCM_WUP_GET_IPLIST_SOCKET = Uri.withAppendedPath(getRomBaseAuthorUri(), ACTION_GET_IPLIST_SOCKET);
        }
        
        return URI_OLD_TCM_WUP_GET_IPLIST_SOCKET;
    }
    
    public static boolean isTcmRomMode() {
        
        return TCM_PACKAGE_NAME.equals(QRomWupBuildInfo.getRomPackageName());
    }
}
