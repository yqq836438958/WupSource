package qrom.component.wup;

import android.content.Context;
import qrom.component.log.QRomLog;
import qrom.component.wup.support.ISwitchListener;
import qrom.component.wup.support.ISwitchProvider;
import qrom.component.wup.support.FileSwitchProvider;
import qrom.component.wup.support.NormalSwitchProvider;

/**
 * 使用开关前，请优先选择开关模式(默认工作在内存模式下)
 * 
 * 注意：多进程支持可以使用文件开关的模式, 这样多个进程会共享文件开关。
 *     在暴露的进程创建时，请设置工作模式.另外文件开关的模式会有微小的性能损失.
 *     总之，如果想简单支持多进程模式，请在进程创建的时候将开关工作模式设置为WorkModeFile
 * @author wileywang
 *
 */
public class QRomWupEnvironment {
	private static QRomWupEnvironment sInstance;
	private static final String LOG_TAG = "QRomWupEnvironment";
	
	public static enum SwitchWorkMode {
		WorkModeNormal, // 普通内存开关
		WorkModeFile // 文件标志开关
	}
	
	public static enum QuaBuildMode {
		BuildFromDefault,   // 原始逻辑,普通的应用从apk中走
		BuildFromRomSrc  // 走Rom的系统属性
	}
	
	private static SwitchWorkMode sSwitchWorkMode = SwitchWorkMode.WorkModeNormal;
	private static QuaBuildMode sQuaBuildMode = QuaBuildMode.BuildFromDefault;
	
	public static void setSwitchWorkMode(SwitchWorkMode mode) {
		if (mode != null && sInstance == null) {
			sSwitchWorkMode = mode;
		}
	}
	
	public static SwitchWorkMode getSwitchWorkMode() {
		return sSwitchWorkMode;
	}
	
	public static void setBuildQuaMode(QuaBuildMode mode) {
		if (mode != null) {
			QRomLog.i(LOG_TAG, "qua build mode switch to " + mode.name());
			sQuaBuildMode = mode;
		}
	}
	
	public static QuaBuildMode getBuildQuaMode() {
		return sQuaBuildMode;
	}
	
	public static QRomWupEnvironment getInstance(Context context) {
		if (sInstance == null) {
			synchronized(QRomWupEnvironment.class) {
				if (sInstance == null) {
					sInstance = new QRomWupEnvironment(context.getApplicationContext());
				}
			}
		}
		return sInstance;
	}
	
	private ISwitchProvider mSwitchProvider;
	
	private QRomWupEnvironment(Context applicationContext) {
		QRomLog.d(LOG_TAG, "work mode is " + sSwitchWorkMode.name());
		if (sSwitchWorkMode == SwitchWorkMode.WorkModeFile) {
			mSwitchProvider = new FileSwitchProvider(applicationContext);
		} else {
			mSwitchProvider = new NormalSwitchProvider();
		}
	}
	
	public boolean isAllClosed() {
		return mSwitchProvider.isAllClosed();
	}
	
	public void setCloseAll(boolean needClose) {
		mSwitchProvider.setCloseAll(needClose);
	}
	
	public void registerSwitchListener(ISwitchListener listener) {
		mSwitchProvider.registerSwitchListener(listener);
	}
	
	public void unreigsterSwitchListener(ISwitchListener listener) {
		mSwitchProvider.unRegisterSwitchListener(listener);
	}
}
