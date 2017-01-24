package qrom.component.wup.security;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.sync.security.AsymCipherManager;

class AsymSessionProcessor {
	
	private ThreadPoolExecutor mExecutor;
	private AsymCipherManager mAsymCipherManager;
	
	public AsymSessionProcessor(AsymCipherManager asymClipherManager) {
		mAsymCipherManager = asymClipherManager;
	}
	
	public void getSession(long requestId
			, String packageName
			, RunEnvType envType
			, IWorkRunner callbackThread
			, IAsymSessionCallback callback) {
		getExecutor().execute(
				new AsymSessionTask(requestId
						, packageName, envType,  mAsymCipherManager, callbackThread, callback));
	}
	
	private ThreadPoolExecutor getExecutor() {
		if (mExecutor == null) {
			synchronized(this) {
				if (mExecutor == null) {
					mExecutor = new ThreadPoolExecutor(1, 2, 60,
			                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
			                new ThreadFactory() {
			                    public Thread newThread(Runnable r) {
			                        Thread thread = new Thread(r, "AsymSession");
			                        return thread;
			                    }
			                }, new ThreadPoolExecutor.AbortPolicy());

			        if (android.os.Build.VERSION.SDK_INT >= 9) {
			        	mExecutor.allowCoreThreadTimeOut(true);
			        }
				}
			}
		}
		return mExecutor;
	}
		
}
