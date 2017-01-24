package qrom.component.wup.core;

import java.io.File;

import qrom.component.wup.base.ContextHolder;

import android.os.Environment;

/**
 *  路径服务，用于统一规划模块使用的路径
 * @author wileywang
 *
 */
public class PathService {
	
	/**
	 *  内置文件存储目录
	 */
	public static File getFilesDir() {
		return ContextHolder.getApplicationContextForSure().getFilesDir();
	}
	
	/**
	 *  获取外置外置基础目录
	 */
	public static File getSdcardBaseDir() {
		return new File(Environment.getExternalStorageDirectory(), "/tencent/qrom/wup/");
	}
	
	/**
	 *  不用应用包名的外置存储路径
	 * @return
	 */
	public static File getSdcardPackageDir() {
		return new File(getSdcardBaseDir()
				, ContextHolder.getApplicationContextForSure().getPackageName().replace(".", "_"));
	}
	
	/**
	 *  获取对应应用名外置路径的缓存目录
	 * @return
	 */
	public static File getSdcardPackageCacheDir() {
		return new File(getSdcardPackageDir(), "/cache");
	}
	
}
