package qrom.component.wup.utils;

import java.util.ArrayList;
import java.util.List;

import qrom.component.log.QRomLog;

/**
 * 线程安全的回调列表
 * @author wileywang
 */
public abstract class ListenerList <T> {
	private List<T> mListenerList;
	private Object mLockObject;
	
	protected  ListenerList() {
		mListenerList = new ArrayList<T>();
		mLockObject = new Object();
	}
	
	public void registerListener(T listener) {
		if (listener == null) {
			return ;
		}
		
		synchronized(mLockObject) {
			if (!mListenerList.contains(listener)) {
				mListenerList.add(listener);
			}
		}
	}
	
	public void unregisterListener(T listener) {
		if (listener == null) {
			return ;
		}
		
		synchronized(mLockObject) {
			mListenerList.remove(listener);
		}
	}
	
	public void notifyListeners(Object... params) {
		synchronized(mLockObject) {
			for (T listener : mListenerList) {
				try {
					onNotifyListener(listener, params);
				} catch (Throwable e) {
					QRomLog.e(getExceptionTag(), e);
				}
			}
		}
	}
	
	protected String getExceptionTag() {
		return ListenerList.class.getSimpleName();
	}
	
	protected abstract void onNotifyListener(T listener, Object... params);
}
