package qrom.component.wup.base.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.text.TextUtils;

/**
 *  文件读写工具类
 * @author wileywang
 *
 */
public class FileUtil {
	public static byte[] readFile(File file) throws FileNotFoundException, IOException {
		FileInputStream fi = new FileInputStream(file);
		byte[] contentBytes = null;
		try {
			contentBytes = new byte[fi.available()];
			fi.read(contentBytes);
		} finally {
			fi.close();
		}
		
		return contentBytes;
	}
	
	public static void writeFile(File file, byte[] content) {
		FileOutputStream os = null;
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			os.write(content);
			os.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static boolean isExternalStorageAvailable() {
        try {
            /* 华为的手机会脑残，Environment.getExternalStorageState()会NullPointerException */
            if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())){
                return true;
            }
            return false;
        } catch(Throwable e) {
            return false;
        }
    }
}
