package qrom.component.wup.threads;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.PhoneStatUtils;

/**
 *  零时线程池
 * @author wileywang
 *
 */
public class TempThreadPool {
	private static TempThreadPool sInstance;
	
	public static TempThreadPool get() {
		if (sInstance == null) {
			synchronized(TempThreadPool.class) {
				if (sInstance == null) {
					sInstance = new TempThreadPool();
				}
			}
		}
		return sInstance;
	}
	
	private ThreadPoolExecutor mExecutor;
	
	private TempThreadPool() {
		int coreNum = PhoneStatUtils.getCpuCoreNum();
		if (coreNum <= 0) {
			coreNum = 1;
		}
        
        mExecutor = new ThreadPoolExecutor(coreNum, coreNum * 2, 5 * 60,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "TempThread");
                        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

							@Override
							public void uncaughtException(Thread thread, Throwable ex) {
								QRomLog.wtf("TempThread", ex.getMessage(), ex);
							}
                        });
                        return thread;
                    }
                }, new ThreadPoolExecutor.AbortPolicy());

        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	mExecutor.allowCoreThreadTimeOut(true);
        }
	}
	
	public void post(Runnable runnable) {
		mExecutor.execute(runnable);
	}
	
}
