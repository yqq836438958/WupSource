package qrom.component.wup.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import qrom.component.log.QRomLog;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.base.utils.ValueCacheHolder;

/**
 *  用于探听系统，辅助构建系统的类
 * @author wileywang
 *
 */
public class Detector {
	private static final String TAG = Detector.class.getSimpleName();
	
	private static Detector sInstance;
	public static Detector get() {
		if (sInstance == null) {
			synchronized(Detector.class) {
				if (sInstance == null) {
					sInstance = new Detector();
				}
			}
		}
		
		return sInstance;
	}
	
	private ValueCacheHolder<Boolean> mIsTcmExitsValueHolder  = new ValueCacheHolder<Boolean>(){
		@Override
		protected Boolean buildValue() {
			PackageManager packageManager = ContextHolder.getApplicationContextForSure().getPackageManager();
			if (packageManager == null) {
				QRomLog.d(TAG, "detect tcm, but can not get package manager!");
				return null;
			}
			
			try {
				packageManager.getPackageInfo(getTcmPackageName(), 0);
				QRomLog.d(TAG, "detect tcm, found it!");
				return Boolean.valueOf(true);
			} catch (NameNotFoundException e) {
				QRomLog.d(TAG, "package not found for " + getTcmPackageName());
				return Boolean.valueOf(false);
			}
		}
	};
	
	private static class HostAppInfo {
        
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
        public int getHostId() {
            return mHostId;
        }
        
    }
	
	private ValueCacheHolder<String> mTcmProxyPackageNameHolder = new ValueCacheHolder<String>() {
		
		@Override
		protected String buildValue() {
			if (isTcmExists()) {
				return "";
			}
			
			List<ResolveInfo> resolveInfos = null;
			try {
				Intent intent = new Intent("qrom.compoent.tcm.action.start");
				resolveInfos = ContextHolder.getApplicationContextForSure().getPackageManager().queryIntentServices(
						intent, PackageManager.GET_META_DATA);
			} catch (Throwable e) {
				QRomLog.e(TAG, e.getMessage(), e);
			}
			
			if (resolveInfos == null || resolveInfos.size() <= 0) {
				return "";
			}
			
			List<HostAppInfo> hostAppInfos = new ArrayList<HostAppInfo>();
			for (ResolveInfo resolveInfo : resolveInfos) {
	            if (resolveInfo == null || resolveInfo.serviceInfo == null 
	                    || resolveInfo.serviceInfo.applicationInfo.metaData == null) {
	                continue;
	            }            

	            int hostId = resolveInfo.serviceInfo.applicationInfo.metaData.getInt("tcm_proxy", 0);
	            String pkgName = resolveInfo.serviceInfo.packageName;
	            
	            if (hostId <= 0 || StringUtil.isEmpty(pkgName) 
	                    || getTcmPackageName().equals(pkgName)) {
	                continue;
	            }
	            
	            hostAppInfos.add(new HostAppInfo(pkgName, hostId));
			}
			
			if (hostAppInfos.isEmpty()) {
				return "";
			}
			
			if (hostAppInfos.size() > 1) {
				// 排序算法复用V1版本算法
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
							return rhsId - lhsId;
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
				});
			} 
			
			StringBuilder tcmProxysBuilder = new StringBuilder(128);
			tcmProxysBuilder.append("dect tcm proxys :");
			for (HostAppInfo hostAppInfo : hostAppInfos) {
				tcmProxysBuilder.append("{packageName=");
				tcmProxysBuilder.append(hostAppInfo.getAppPkgName());
				tcmProxysBuilder.append(", hostId=");
				tcmProxysBuilder.append(hostAppInfo.getHostId());
				tcmProxysBuilder.append("} ");
			}
			QRomLog.d(TAG, tcmProxysBuilder.toString());
			QRomLog.d(TAG, "using tcm proxy package name=" + hostAppInfos.get(0).getAppPkgName());
			return hostAppInfos.get(0).getAppPkgName();
		}
		
	};
	
	protected Detector() {
	}
	
	public boolean isTcmExists() {
		Boolean exists = mIsTcmExitsValueHolder.getValue();
		if (exists == null) {
			return false;
		}
		return exists.booleanValue();
	}
	
	public String getTcmPackageName() {
		return "com.tencent.qrom.tms.tcm";
	}
	
	/**
	 *  获取TCM Proxy的包名
	 * @return
	 */
	public String getTcmProxyPackageName() {
		return mTcmProxyPackageNameHolder.getValue();
	}
}
