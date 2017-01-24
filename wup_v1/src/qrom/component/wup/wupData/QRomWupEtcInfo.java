package qrom.component.wup.wupData;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import android.content.Context;


/**
 * wup额外信息处理对象
 * @author sukeyli
 *
 */
public class QRomWupEtcInfo {
    
    public final static String KEY_NET_ENVIROMENT ="net_env";
    
    public final static int NET_ENV_NONE = -1;
    public final static int NET_ENV_RELEASE = 0;
    public final static int NET_ENV_TEST = 1;
    
    private int mNetEnv = NET_ENV_NONE;
    
    private long mFileLastTime = -1; 
    
    /** wup环境切换配置信息 */
    private static final String SD_DEFAULT_NET = "/net";
    /** wup环境切换配置信息 */
    private static final String SD_DEFAULT_WUP_ENV_CONFIG_FILENAME = "env.ini";        
    
    /**
     * 加载配置文件
     * @param context
     * @return
     */
    public boolean load(Context context) {
        if (context == null) {
            return false;
        }
        return load(context.getPackageName());
    }

    public boolean load(String packageName) {
        InputStream inputStream = null;        
        
        File etcFile = getSdNetEnvConfigFile(packageName);
        if (etcFile == null || !etcFile.exists() || !etcFile.isFile()) {
            mNetEnv = NET_ENV_RELEASE;
            return false;
        }
        try {            

            inputStream = new FileInputStream(etcFile);
            
            Properties property = new Properties();
            property.load(inputStream);
            String wupEnv = property.getProperty(KEY_NET_ENVIROMENT);
            mNetEnv = Integer.valueOf(wupEnv);
            // 设置加载时间
            mFileLastTime = etcFile.lastModified();
            QWupLog.i("QubeWupEtcInfo", "mWupEnv = " + mNetEnv);
            return true;
        } catch (Exception e) {
            mNetEnv = NET_ENV_RELEASE;
            QWupLog.w("QubeWupEtcInfo", e);            
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }        
        return false;
    } 
    
    /**
     *   判断是否需要重新加载配置文件
     *       -- 当配置文件最后修改时间变化
     *       -- 或未加载过配置文件
     *     
     * @param context
     * @return
     */
    public boolean checkReLoadConfiInfo(Context context) {
        return checkReLoadConfiInfo(context.getPackageName());
    }
    
    public boolean checkReLoadConfiInfo(String packageName) {
        File etcFile = getSdNetEnvConfigFile(packageName);
        
        if (mFileLastTime <=0 || mFileLastTime != etcFile.lastModified() 
                || mNetEnv == NET_ENV_NONE) {
            QWupLog.w("QubeWupEtcInfo", "etcFile lastModified time changed ");            
            return load(packageName);
        }
        return false;
    }
    
    public int getNetEnvFlg() {
        return mNetEnv;
    }
    
    /**
     * 是否配置文件设置为测试环境
     *    -- 使用前先加载配置文件
     * @return
     */
    public boolean isEnvConfigTest(Context context) {
        checkReLoadConfiInfo(context);
        return mNetEnv == NET_ENV_TEST;
    }

    /**
     * 获取配置文件在sd卡上根目录
     *   -- 默任 sd卡/tencent/qrom
     * @return
     */
    protected File getSdEnvRootFile() {
        Context context = QRomWupImplEngine.getInstance().getContext();
        if (QRomWupBuildInfo.isSysRomSrcMode(context)) {
            return new File(QWupFileUtil.getSysRomDataRootPath());
        }
        return QWupFileUtil.getSdSdkRootFile();
    }
   
    
    /**
     * 获取wup环境变量配置文件的缓存路径
     * @param context
     * @return 默认tencent/qrom/对应包名/env目录
     */
    private File getSdNetEnvDir(String packageName) {
        
        String sdNetEnvDir = SD_DEFAULT_NET + File.separator 
                + QWupFileUtil.getPackageDirByName(packageName) ;
        return new File(getSdEnvRootFile(), sdNetEnvDir);
    }
    
    /**
     * 获取wup环境变量配置文件
     * @param context
     * @return
     */
    protected File getSdNetEnvConfigFile(String packageName) {

        return new File(getSdNetEnvDir(packageName), SD_DEFAULT_WUP_ENV_CONFIG_FILENAME);
    }
}
