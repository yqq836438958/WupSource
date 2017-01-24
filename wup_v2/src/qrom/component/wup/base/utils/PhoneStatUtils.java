package qrom.component.wup.base.utils;

import java.lang.reflect.Method;
import java.util.Locale;

import qrom.component.log.QRomLog;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 *  获取手机状态信息的工具类
 * @author wileywang
 *
 */
public class PhoneStatUtils {

	private static String PHONE_IMEI = null;

	private static String PHONE_MAC = null;

	/**
	 * 设备cpu是几核
	 */
	private static int mDeviceCpuCores = -1;

	public static int getCpuCoreNum() {
		if (mDeviceCpuCores != -1) {
			return mDeviceCpuCores;
		}
		mDeviceCpuCores = Runtime.getRuntime().availableProcessors();
		return mDeviceCpuCores;
	}

	/**
	 * 获取手机imei
	 * 
	 * @param context
	 * @return
	 */
	public static String getImei(Context context) {

		if (PHONE_IMEI != null && !"".equals(PHONE_IMEI)) {
			return PHONE_IMEI;
		}

		try {
			TelephonyManager mTelephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (mTelephonyManager != null) {
				PHONE_IMEI = mTelephonyManager.getDeviceId();
				if (TextUtils.isEmpty(PHONE_IMEI)) {
					PHONE_IMEI = getSimImei();
				}
			}

			if (PHONE_IMEI != null) {
				PHONE_IMEI = PHONE_IMEI.toLowerCase(Locale.getDefault());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return PHONE_IMEI;
	}

	private static String getSimImei() {
		Class<?> clazz;
		try {
			clazz = Class.forName("android.telephony.TelephonyManager");
			Method method = clazz.getDeclaredMethod("getSecondary");
			method.setAccessible(true);
			TelephonyManager telManager = (TelephonyManager) method
					.invoke(clazz);
			if (telManager != null) {
				return telManager.getDeviceId();
			}
		} catch (Throwable e) {
			QRomLog.w("wup_phonestatUtil", "getSimImei -> " + e.getMessage());
		}
		return null;
	}

	/**
	 * 获取mac地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getMacAddress(Context context) {
		if (StringUtil.isEmpty(PHONE_MAC)) {
			try {
				WifiManager wifi = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				if (wifi == null) {
					return null;
				}
				WifiInfo info = wifi.getConnectionInfo();
				if (info != null) {
					PHONE_MAC = info.getMacAddress();
				}
				if (PHONE_MAC != null) {
					PHONE_MAC = PHONE_MAC.toLowerCase(Locale.getDefault());
				}
			} catch (Exception e) {
				QRomLog.w("wup_phonestatUtil",
						"getMacAddress -> err msg: " + e.getMessage());
			}
		}
		return PHONE_MAC;
	}

	public static String getCurProcessName(Context context) {
		try {
			int pid = android.os.Process.myPid();
			ActivityManager mActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
					.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {

					return appProcess.processName;
				}
			}
		} catch (Throwable e) {
			QRomLog.w("wup-PhoneStatUtils", "getCurProcessName -> err msg = "
					+ e.getMessage());
		}
		return null;
	}
}
