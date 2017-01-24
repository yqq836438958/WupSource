package qrom.component.wup.build;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import qrom.component.wup.sysImpl.QWupRomSysProxyerImpl;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

/**
 * rom 集成模式探测类
 *    -- 负责找出指定的集成模式的主app
 * @author sukeyli
 *
 */
public class RomHostDetector {

    private static final String TAG = "WUP-RomHostDetector";
   
    /** 集成到rom源码中虚拟app包名 */
//    private static final String SYS_ROM_SRC_SDK_ABSTRACT_PACKAGE_NAME ="trom.system.framwork.sdk.appname";
    private static final String SYS_ROM_SRC_SDK_ABSTRACT_PACKAGE_NAME ="android";
    private static final int SYS_ROM_SRC_SDK_PACKAGE_HOST_VALUE = 10000;
    /** ROM 使用wup模块的包名 */
    private static final String ROM_SDK_DEFAULT_PACKAGE_NAME_TCM ="com.tencent.qrom.tms.tcm";
    private static final int ROM_SDK_DEFAULT_PACKAGE_HOST_VALUE = 1000;
    
    /** 设置了tcm host的app 的action */
    private final static String TCM_SERVICE_ACTION_START = "qrom.compoent.tcm.action.start";
   
    /** tcm host的key*/
    private final static String TCM_HOST_KEY = "tcm_proxy";
    
    private final static String TCM_HOST_FRAMEWORK_PORP = "1";
    
    private static String ROM_PACKAGE_NAME = null;
    
    

    private static final int DETCTOR_NO_INIT = -1;
    private static final int DETCTOR_ROM_APP_EXIST = 1;
    private static final int DETCTOR_NO_ROM_APP_EXIST = 0;
    
    private static int M_DETECTOR_MODE = DETCTOR_NO_INIT;
    
    /**
     * 检查rom底层的 app 是否可用
     */
    private static boolean isRomServiceAvailable(Context context, String defaultRomPkgName) {
        // 判断rom底层的统一上报service是否可用
        boolean isAvailable = false;
        String packageName = defaultRomPkgName;
        if (context != null && !TextUtils.isEmpty(packageName)) {           
            PackageManager packageManager = context.getPackageManager();  
            try {
                PackageInfo pkgInfo = packageManager.getPackageInfo(packageName, 
                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                if (pkgInfo != null) {
                    isAvailable = true;
                }
            } catch (Exception e) {
                printLog("isRomServiceAvailable -> err : " + e + ", err msg: " + e.getMessage());
            }           
        }
        return isAvailable;
    }
    
    /**
     * 是否运行在framework集成rom中
     * @param context
     * @return
     */
    private static boolean isRunFrameworkRom(Context context) {
        boolean isInRomSrc = false;
        // 当getApplicationContext()为null时或者可获取到wup
        if (context != null && 
                (context.getApplicationContext() == null
                || QWupRomSysProxyerImpl.getInstance().getQRomBinderServiceForSdk(context) != null
                || TCM_HOST_FRAMEWORK_PORP.equals(getSysFromworkFlg()))) {
            isInRomSrc = true;
        }
        printLog("isRunRomFramework -> isInRomSrc = " + isInRomSrc 
                + ", cur pkg = " + (context == null ? "null" : context.getPackageName()));
        return isInRomSrc;
    }
   
    /**
     * 探测所有tcmHost的app信息，并更具value id降序排列
     * @param context
     * @param defaultRomPkgName 默认Rom模式的package name，（如tcm）
     * @return
     */
    public static List<HostAppInfo> detectDescHostAppInfo(Context context) {
        // 初始化探测标记位
        M_DETECTOR_MODE = DETCTOR_NO_ROM_APP_EXIST;
        String pkg = context == null ? "null" :  context.getPackageName();
        HostAppInfo sysRomHostInfo = null;
        // 判断是否是rom 源码集成模式
        boolean isInRomSrc = isRunFrameworkRom(context);       
        if (isInRomSrc) {  // 在系统源码中
            //  已集成到rom源码中 
            sysRomHostInfo = new HostAppInfo(SYS_ROM_SRC_SDK_ABSTRACT_PACKAGE_NAME, 
                    SYS_ROM_SRC_SDK_PACKAGE_HOST_VALUE);
        }
        // 先判断tcm app是否存在，若存在则优先使用tcm
        HostAppInfo tcmHostInfo = null;
        boolean isRomPkgOk = isRomServiceAvailable(context, ROM_SDK_DEFAULT_PACKAGE_NAME_TCM);
        if (isRomPkgOk) {  // 有指定的rom app            
            //  找到指定的rom app（tcm） 
            tcmHostInfo = new HostAppInfo(ROM_SDK_DEFAULT_PACKAGE_NAME_TCM, 
                    ROM_SDK_DEFAULT_PACKAGE_HOST_VALUE);
        }
        
        // 无指定的romapp，则判断tcm host优先级
        /*
         * host 的meta格式为: 
         * <meta-data android:name="tcm_proxy"
         *                     android:value="100"/>
         */

        List<ResolveInfo> resolveInfos = null;

        try {
            Intent intent = new Intent(TCM_SERVICE_ACTION_START);
            resolveInfos = context.getPackageManager().queryIntentServices(intent,
                    PackageManager.GET_META_DATA);
        } catch (Exception e) {
            printLog("detectHostAppInfo ->find err: TCM_SERVICE_ACTION_START, cur pkg = " + pkg);
        }

        int size = 0;
        
        if (resolveInfos != null && !resolveInfos.isEmpty()) {
            size = resolveInfos.size();
        }
        printLog("detectHostAppInfo -> TCM_SERVICE_ACTION_START size = " + size 
                + ", cur pkg = " + pkg);
       
        String pkgName = null;
        int hostId = -1;
        int maxHostId = -1;
        
        List<HostAppInfo> hostAppInfos = new ArrayList<HostAppInfo>(size + 1);
        HostAppInfo hostAppInfo = null;
        
        ResolveInfo info = null;
        for (int i = 0; i <size; i++) { // 获取所有的app 的 host信息
            info = resolveInfos.get(i);
            if (info == null || info.serviceInfo == null 
                    || info.serviceInfo.applicationInfo.metaData == null) {
                continue;
            }            

            hostId = info.serviceInfo.applicationInfo.metaData.getInt(TCM_HOST_KEY, 0);
            pkgName = info.serviceInfo.packageName;
            
            if (hostId <= 0 || QWupStringUtil.isEmpty(pkgName) 
                    || ROM_SDK_DEFAULT_PACKAGE_NAME_TCM.equals(pkgName)) {
                continue;
            }
            hostAppInfo = new HostAppInfo(pkgName, hostId);
            hostAppInfos.add(hostAppInfo);
            if (maxHostId < hostId) {
                maxHostId = hostId;
                // 找到合法的host rom app
                M_DETECTOR_MODE = DETCTOR_ROM_APP_EXIST;
            }
        }    // ~ end host app 查询完成
        
        // 倒序排列所以host app信息
        Collections.sort(hostAppInfos, new Comparator<HostAppInfo>() {
            @Override
            public int compare(HostAppInfo lhs, HostAppInfo rhs) {
                if (lhs == null) {
                    return 1;
                }
                if (rhs == null) {
                    return -1;
                }

                int lhsId = lhs.getHostId();
                int rhsId = rhs.getHostId();
                if (lhsId != rhsId) {
                    return rhsId -lhsId;
                } 

                String lhsPkg = lhs.getAppPkgName();
                String rhsPkg = rhs.getAppPkgName();
                
                if (lhsPkg == null) {  
                    lhsPkg = "";
                }
                if (rhsPkg == null) {
                    rhsPkg = "";
                }
                // id设置一样则比较packageName
                return lhsPkg.compareTo(rhsPkg);                
            }
        });  // ~排序结束
        
        ROM_PACKAGE_NAME = null;
        
        if (tcmHostInfo != null) {  // 有tcm app
            M_DETECTOR_MODE = DETCTOR_ROM_APP_EXIST;
            ROM_PACKAGE_NAME = ROM_SDK_DEFAULT_PACKAGE_NAME_TCM;
            if (!hostAppInfos.isEmpty()) {                
                hostAppInfos.add(0, tcmHostInfo);
            } else {
                hostAppInfos.add(tcmHostInfo);
            }
        } 
        
        if (sysRomHostInfo != null) {  // 找到sys 集成 rom -- 优先级最高，放在队列首
            M_DETECTOR_MODE = DETCTOR_ROM_APP_EXIST;
            ROM_PACKAGE_NAME = SYS_ROM_SRC_SDK_ABSTRACT_PACKAGE_NAME;
            if (!hostAppInfos.isEmpty()) {                
                hostAppInfos.add(0, sysRomHostInfo);
            } else {
                hostAppInfos.add(sysRomHostInfo);
            }
        }
        
        if (ROM_PACKAGE_NAME == null || "".equals(ROM_PACKAGE_NAME))  {  // 未匹配到rom 
            if (!hostAppInfos.isEmpty()) {
                // 更新rom host name
                ROM_PACKAGE_NAME = hostAppInfos.get(0).getAppPkgName();
            }
        }
        printLog("detectDescHostAppInfo -> rom package = " + ROM_PACKAGE_NAME 
                + ", cur pkg = " + pkg);
        return hostAppInfos;
    }
    
    /**
     * 获取当前rom packge name
     *    -- 若已探测过一次，则返回缓存数据
     *    -- 若要更新数据请，先调用 {@link detectDescHostAppInfo方法}
     * @param context
     * @return
     */
    public static String getRomPackageName(Context context) {
        if (M_DETECTOR_MODE == DETCTOR_NO_INIT) {
            detectDescHostAppInfo(context);
        }
        return ROM_PACKAGE_NAME;
    }
    
    /**
     * 获取sdk 在rom源码集成模式中的虚拟包名
     * @return
     */
    public static String getSysRomSrcSdkPkgName() {
        return SYS_ROM_SRC_SDK_ABSTRACT_PACKAGE_NAME;
    }
    
    public static void printLog(String str) {
        QWupLog.trace(TAG, str);
    }
    
    private static String getSysFromworkFlg() {
        String cmd = "getprop ro.qrom.build.tcmhost";
        StringBuilder strb;
        String res = null;
        try {
            java.lang.Process process   = Runtime.getRuntime().exec(cmd);
            InputStreamReader inputStreamReader = new   InputStreamReader(process.getInputStream()); 
            char[] buf = new char[15];
            int readLen = 0;
            strb = new StringBuilder();;
            while ((readLen = inputStreamReader.read(buf)) != -1) {
                strb.append(buf, 0, readLen);
            }
            res = strb.toString();
        } catch (Exception e) {
            printLog("getSysFromworkFlg -> err msg : " + e.getMessage() + ", e: " + e);
        }
      
       printLog("getSysFromworkFlg -> res = " + res);
       if(res != null) {
           res = res.trim();
       }
       return res == null ? "" : res;
    }
    
    /**
     * host app的相关信息
     * @author sukeyli
     *
     */
    public static class HostAppInfo {
        
        /** app 的packageName*/
        private String mAppPkgName;

        /** tcm host的value */
        private int mHostId;
        
        public HostAppInfo(String appPkgName, int hostId) {
            mAppPkgName = appPkgName;
            mHostId = hostId;
        }
        
        
        public String getAppPkgName() {
            return mAppPkgName;
        }
        public void setAppPkgName(String appPkgName) {
            this.mAppPkgName = appPkgName;
        }
        public int getHostId() {
            return mHostId;
        }
        public void setHostId(int hostId) {
            this.mHostId = hostId;
        }
    }

}
