package qrom.component.wup.base.utils;

import java.util.concurrent.Semaphore;

import qrom.component.wup.base.IWorkRunner;

/**
 *  用于同步获取维持在其他线程的结果
 * @author wileywang
 *
 */
public abstract class ThreadResultHandler<T> implements Runnable  {
	
	protected T mResult;
	private Semaphore mSemaphore; 
	private IWorkRunner mWorkRunner;
	
	public ThreadResultHandler(IWorkRunner workRunner) {
		mSemaphore = new Semaphore(0);
		mWorkRunner = workRunner;
	}
	
	public abstract void fillResult();
		
	public T getResult() {
		if (mWorkRunner.getThread() == Thread.currentThread()) {
			fillResult();
		} else {
			try {
				mWorkRunner.postWork(this);
				mSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return mResult;
	}

	@Override
	public void run() {
		try {
			fillResult();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		mSemaphore.release();
	}
	
}
