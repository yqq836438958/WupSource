/**
 * @Title: QubeFileUtil.java
 * @Package com.tencent.qube.utils
 * @author interzhang
 * @date 2012-5-10 下午03:35:11 
 * @version V1.0
 */
package qrom.component.wup.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public final class QWupFileUtil {

    public static final String TAG = "QWupFileUtil";

    private static final String DIR_WUP_DATA = "wupData";
    

    /** app 应用data/data 下缓存文件名 */
    public static final String FILE_USER_WUP_INFO_APP = "wup_user_info_app.inf";
    /** rom 应用data/data 下缓存文件名*/
    public static final String FILE_USER_WUP_INFO_ROM = "wup_user_info_rom.inf";
    /** app 应用sd 下缓存文件名 */
    public static final String SD_USER_WUP_INFO_APP = "wup_user_app.cache";
    /** rom 模式 sd 下缓存文件名 */
    public static final String SD_USER_WUP_INFO_ROM = "wup_user_rom.cache";
    /** SDK sd卡缓存根目录 */
    private static final String SD_DEFAULT_SDK_ROOT_PATH = "/tencent/qrom";
    /** wup 缓存跟目录 */
    private static final String SD_DEFAULT_WUP_ROOT_PATH = SD_DEFAULT_SDK_ROOT_PATH + "/wup/";    
   
    private static final String SD_DEFAULT_WUP_INFO_CACHE = "/cache";
    /** 系统rom源码集成模式缓存文件目录根目录 */
    private static final String SYS_ROM_SRC_DIR_ROOT = "/data/system/qrom/";
    /** 系统rom源码集成模式 wup缓存文件目录根目录 */
//    private static final String SYS_ROM_SRC_DIR_WUP_ROOT = SYS_ROM_SRC_DIR_ROOT + DIR_WUP_DATA;
    /** 系统rom源码集成模式qua 配置文件目录 */
    private static final String SYS_ROM_SRC_DIR_QUA = "system/lib/framework-tcm/" + "qua_config";
    
    private QWupFileUtil() {
    }
    
    /**
     * 获取系统下data/data/app packaget /files目录
     *
     * @return
     */
    public static File getFilesDir(Context appContext) {
        return appContext.getFilesDir();
    }
    
    /**
     * 获取data/data/ 下wup缓存数据目录
     *
     * @return
     */
    public static File getWupDataDir(Context appContext) {
        return getDir(getFilesDir(appContext), DIR_WUP_DATA);
    }
    
    /**
     * 根据文件名获取wup的缓存文件
     *
     * @return
     */
    public static File getWupUserInfoFile(Context context, String fileName) {
        return new File(getWupDataDir(context), fileName);
    }
    
    /**
     * 获取rom源码集成模式wup缓存根目录
     * @return
     */
    public static File getWupDataRootForSysRomSrc() {
//        return new File(SYS_ROM_SRC_DIR_WUP_ROOT);
        return getDir(new File(SYS_ROM_SRC_DIR_ROOT), DIR_WUP_DATA);
    }
    
    /**
     * 获取rom 源码集成模式下缓存文件的根目录
     * @return
     */
    public static String getSysRomDataRootPath() {
        return SYS_ROM_SRC_DIR_ROOT;
    }
    
    /**
     * 获取rom源码集成模式wup qua配置文件目录
     * @return
     */
    public static String getQuaConfigDirForSysRomSrc() {
        return SYS_ROM_SRC_DIR_QUA;
    }
    
    /**
     * 获取rom源码集成模式文件缓存路径
     * @param fileName
     * @return
     */
    public static File getWupUserInfoFileForSysRomSrc(String fileName) {
        return new File(getWupDataRootForSysRomSrc(), fileName);
    }
    
    /**
     * 获得名为childName的缓存文件夹
     * @return
     */
    public static File getDir(File parent, String dirName) {
        if (parent == null || dirName == null || dirName.length() == 0) {
            return null;
        }

        File childDir = new File(parent, dirName);
        if (!childDir.exists()) {
            childDir.mkdirs();
        }

        return childDir;
    }

    /**
     * 检查SD卡是否可用
     *
     * @return boolean
     */
    public static boolean isExternalStorageAvailable() {
        try {
            /* 华为的手机会脑残，Environment.getExternalStorageState()会NullPointerException */
            if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())){
                return true;
            }
            return false;
        } catch(Exception ex) {
            return false;
        }
    }
    
    public static File createNewFile(String fileDir, String fileName) {
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName)) {
            return null;
        }

        // 文件夹路径
        File dir = new File(fileDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }

        // 创建文件
        File file = new File(fileDir, fileName);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return file;
    }
        
    
    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file
                        + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file
                    + "' does not exist");
        }
        return new FileInputStream(file);
    }
    

    
    
    /**
     * 获取手机sd卡路径
     * @return
     */
    public static File getSdCardFile() {
    	return Environment.getExternalStorageDirectory();
    }
    
    /**
     * 获取sdk sd卡的根路径
     * @return
     */
    public static File getSdSdkRootFile() {
        return new File(getSdCardFile(), SD_DEFAULT_SDK_ROOT_PATH);
    }
    
    /**
     * 获取wup sd 卡上缓存根目录
     * @return
     */
    public static File getSdWupRootFile() {
    	
    	  return new File(getSdCardFile(), SD_DEFAULT_WUP_ROOT_PATH);
    }
    
    /**
     * 获取对应包名的文件夹名
     * @param context
     * @return
     */
    public static String getPackageDir(Context context) {

        return getPackageDirByName(context.getPackageName());
    }
    
    public static String getPackageDirByName(String pkgName) {
        if(pkgName == null) {
            return null;
        }
        String pkgCacheFileName = pkgName.replace(".", "_");
        return pkgCacheFileName;
    }
    
    /**
     * 获取对应app sd 卡上缓存文件目录
     * @param context
     * @return
     */
    public static File getSdWupCacheDir(Context context) {
    	String cacheFileDir = getPackageDir(context) + File.separator + SD_DEFAULT_WUP_INFO_CACHE;
    	return new File(getSdWupRootFile(), cacheFileDir);
    }

    /**
     * 获取sd 卡上wup缓存文件信息
     * 
     * @param context
     * @param cacheFileName  缓存文件名
     * @return
     */
    public static File getSdWupCacheInfoFile(Context context, String cacheFileName) {
    	return new File(getSdWupCacheDir(context), cacheFileName);
    }
    

    
    /**
     * 读取文件流
     * @param file
     * @return
     */
    public static byte[] read(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        InputStream in = null;
        try {
            in = openInputStream(file);
            int bufLen = 512;
            byte[] buffer = new byte[bufLen];
            ByteArrayBuffer arrayBuffer = new ByteArrayBuffer(bufLen);

            int len = 0;
            while ((len = in.read(buffer) )!= -1) {
                arrayBuffer.append(buffer, 0, len);
            }

            return arrayBuffer.toByteArray();
        } catch (Exception e) {
            QWupLog.w(TAG, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                QWupLog.w(TAG, e);
            }
        }
        return null;
    }

    public static FileOutputStream openOutputStream(File file)
            throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file
                        + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file
                        + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("File '" + file
                            + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }
    
    /**
     * 保存文件
     *
     * @param file
     * @param data
     * @return
     */
    public static boolean save(File file, byte[] data) {
        OutputStream os = null;
        try {
            os = openOutputStream(file);
            os.write(data, 0, data.length);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
