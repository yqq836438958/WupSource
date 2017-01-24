package qrom.component.wup.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupBaseConfig;
import qrom.component.wup.base.net.NetActions;

/**
 *  wup的运行环境接口类
 * @author wileywang
 */
public class RunEnv extends BroadcastReceiver {
	private static final String TAG = RunEnv.class.getSimpleName();
	
	private static RunEnv sInstance;
	
	public static RunEnv get() {
		if (sInstance == null) {
			synchronized(RunEnv.class) {
				if (sInstance == null) {
					sInstance = new RunEnv(ContextHolder.getApplicationContextForSure());
				}
			}
		}
		return sInstance;
	}
	
	private QRomWupBaseConfig mBaseConfig;
	
	private RunEnvType mEnvType;
	
	private int mNetEnvValue = 0;
	private File mEnvFile;
	private long mEnvFileLastModifyTime = 0;
	
	protected RunEnv(Context context) {
		initBaseConfig(context);
		
		String packageConfigDir = context.getPackageName().replace(".", "_");
		mEnvFile = new File(Environment.getExternalStorageDirectory(), 
				"/tencent/qrom/net/" + packageConfigDir + "/env.ini");
		
		loadEnvConfig();
		registerBroadcast(context);
	}
	
	/**
	 * 
	 * 优先以程序主动设置环境为主(不保证跨进程)
	 * 然后尝试读取 qrom.component.config.QRomWupConfig类中的isRunTestForced方法的值
	 * 最后读取net_env.ini配置文件的内容
	 * 
	 * @return
	 */
	public RunEnvType getEnvType() {
		if (mEnvType != null) {
			return mEnvType;
		}
		
		if (mBaseConfig != null && mBaseConfig.isRunTestForced()) {
			return RunEnvType.Gamma;
		}
		
		if (mNetEnvValue == 1) {
			return RunEnvType.Gamma;
		}
		
		return RunEnvType.IDC;
	}
	
	public void setEnvType(RunEnvType envType) {
		mEnvType = envType;
	}
	
	private synchronized void loadEnvConfig() {
		if (!mEnvFile.exists()) {
			return ;
		}
		
		if (mEnvFileLastModifyTime != 0 && mEnvFile.lastModified() == mEnvFileLastModifyTime) {
			return ;
		}
		
		FileInputStream inputStream = null; 
		try {
			inputStream = new FileInputStream(mEnvFile);
        
			Properties property = new Properties();
			property.load(inputStream);
			String wupEnv = property.getProperty("net_env");
			
			mNetEnvValue = Integer.valueOf(wupEnv);
			mEnvFileLastModifyTime = mEnvFile.lastModified();
			
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			QRomLog.d(TAG, "loadEnvConfig " + mEnvFile.getAbsolutePath() + " finished! mNetEnvValue=" + mNetEnvValue);
		}
	}
	
	public int getNetEnvValue() {
		return mNetEnvValue;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		loadEnvConfig();
	}
	
	private void registerBroadcast(Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NetActions.ACTION_NET_CHANGED);
		context.registerReceiver(this, intentFilter);
	}
	
	private void initBaseConfig(Context context) {
		try {
			Class<?> clazz = Class.forName("qrom.component.config.QRomWupConfig");
			mBaseConfig = (QRomWupBaseConfig)clazz.newInstance();
		} catch (ClassNotFoundException e) {
			QRomLog.d(TAG, e.getMessage());
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
	}
	
}
