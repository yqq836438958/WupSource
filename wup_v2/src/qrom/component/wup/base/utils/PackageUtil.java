package qrom.component.wup.base.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtil {
	public static String getCallingPkgName(Context context, int callingUid, int callingPid) {
		String dstPkgName = null;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = context.getPackageManager();

		List<RunningAppProcessInfo> apps = am.getRunningAppProcesses();
		if (apps == null)
			return dstPkgName;
		String processName = null;
		for (RunningAppProcessInfo app : apps) {
			if (app.uid == callingUid && app.pid == callingPid) {
				processName = app.processName;
			}
		}

		String[] pkgs = pm.getPackagesForUid(callingUid);
		try {
			for (String pkg : pkgs) {
				PackageInfo pkgInfo = pm.getPackageInfo(pkg, 0);
				if (pkgInfo != null
						&& pkgInfo.applicationInfo.processName.equals(processName)) {
					dstPkgName = pkgInfo.packageName;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dstPkgName;
	}
}
