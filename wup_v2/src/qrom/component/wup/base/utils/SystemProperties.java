package qrom.component.wup.base.utils;

import java.io.InputStreamReader;

import qrom.component.log.QRomLog;

/**
 *  由于获取系统属性的API被hide，在这里采用反射的方法获取系统的属性
 * @author wileywang
 *
 */
public class SystemProperties {

	public static String get(String propertyName) {
		if (StringUtil.isEmpty(propertyName)) {
    		return null;
    	}
    	
		InputStreamReader inputStreamReader = null;
		try {
			Process process = Runtime.getRuntime().exec("getprop " + propertyName);
			inputStreamReader = new InputStreamReader(process.getInputStream());
			
			char[] buf = new char[15];
			int readLen = 0;
			StringBuilder strb = new StringBuilder();
			while ((readLen = inputStreamReader.read(buf)) != -1) {
				strb.append(buf, 0, readLen);
			}
			
			return strb.toString().trim();
		} catch (Throwable e) {
			QRomLog.e("SystemProperties", e.getMessage(), e);
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch(Throwable e) {
				}
			}
		}
		
		return null;
	}
	
	public static String get(String propertyName, String defaultValue) {
		String value = get(propertyName);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
}
