package qrom.component.wup.base.android;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import qrom.component.wup.base.IWorkRunner;

/**
 * 适配到Android的Handler机制的WorkRunner
 * @author wileywang
 */
public class HandlerWorkRunner implements IWorkRunner {
	private Handler mHandler;
	
	private Looper mLooper;
	private HandlerThread mHandlerThread;
	
	public HandlerWorkRunner(Looper looper) {
		if (looper == null) {
			throw new IllegalArgumentException("looper should not be null");
		}
		
		this.mLooper = looper;
	}
	
	public HandlerWorkRunner(HandlerThread handlerThread) {
		if (handlerThread == null) {
			throw new IllegalArgumentException("handlerThred should not be null");
		}
		
		this.mHandlerThread = handlerThread;
	}
	
	/**
	 *  为什么不在HandlerThread构造里面直接写，因为HandlerThread创建获取Looper会导致线程等待
	 *  很多情况下，我们并不是会立即用到这个Handler，只是需要一个句柄而已
	 * @return
	 */
	private Handler getHandler() {
		if (mHandler == null) {
			synchronized(this) {
				if (mHandler == null) {
					if (mLooper != null) {
						mHandler = new Handler(mLooper);
					} else {
						mHandler = new Handler(mHandlerThread.getLooper()); 
					}
				}
			}
		}
		return mHandler;
	}
	
	@Override
	public Thread getThread() {
		return getHandler().getLooper().getThread();
	}

	@Override
	public void postWork(Runnable runnable) {
		getHandler().post(runnable);
	}

	@Override
	public void postWorkDelayed(Runnable runnable, long timeDelayed) {
		getHandler().postDelayed(runnable, timeDelayed);
	}
}
