package qrom.component.wup.threads;

import qrom.component.log.QRomLog;
import android.os.Handler;
import android.os.HandlerThread;

/**
 *  负责存储，刷数据等作用的线程
 * @author wileywang
 */
public class StorageThread extends HandlerThread {
	private static StorageThread sInstance;
	
	public static StorageThread get() {
		if (sInstance == null) {
			synchronized(StorageThread.class) {
				if (sInstance == null) {
					sInstance = new StorageThread();
				}
			}
		}
		return sInstance;
	}
	
	public static boolean isInSameThread() {
		if (sInstance == null) {
			return false;
		}
		return sInstance == Thread.currentThread();
	}
	
	private Handler mHandler;
	private StorageThread() {
		super("StorageThread");
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				QRomLog.wtf("StorageThread", ex.getMessage(), ex);
			}
		});
		start();
	}
	
	public Handler getHandler() {
		if (mHandler == null) {
			synchronized(this) {
				if (mHandler == null) {
					mHandler = new Handler(getLooper());
				}
			}
		}
		return mHandler;
	}
	
}
