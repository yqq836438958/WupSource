package qrom.component.wup.transport.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.net.IConnectInfoListener;
import qrom.component.wup.base.net.NetType;
import qrom.component.wup.base.utils.PhoneStatUtils;

/**
 *  利用简单的固定线程池实现SessionPool的方式
 * @author wileywang
 *
 */
public class DefaultHttpSessionPool implements IHttpSessionPool, IConnectInfoListener {
	private static final String TAG = DefaultHttpSessionPool.class.getSimpleName();
	
	private ThreadPoolExecutor mSessionPool = null;
	private int mCorePoolSize;
	private int mMaxPoolSize;
	
	public DefaultHttpSessionPool() {
		int coreNum = PhoneStatUtils.getCpuCoreNum();
		if (coreNum <= 0) {
			coreNum = 1;
		}
		mCorePoolSize = coreNum <= 2 ? coreNum * 2 : coreNum;
		mMaxPoolSize = getMaxCoreNumber(ConnectInfoManager.get().getConnectInfo());
		
		ConnectInfoManager.get().registerConnectInfoListener(this);
	}
	
	@Override
	public boolean postHttpSession(HttpSession session) {
		if (session == null) {
			return false;
		}
		
		try {
			getSessionPool().execute(session);
			return true;
		} catch (Throwable e) {
			QRomLog.e(TAG, "PostHttpSession failed, " + e.getMessage(), e);
		}
		return false;
	}

	@Override
	public void cancel(HttpSession session) {
		if (session == null) {
			return ;
		}
		
		session.cancel();
		getSessionPool().remove(session);
	}
	
	@Override
	public void destroy() {
		ConnectInfoManager.get().unRegisterConnectInfoListener(this);
		if (mSessionPool != null) {
			mSessionPool.shutdown();
		}
	}
	
	private ThreadPoolExecutor getSessionPool() {
		if (mSessionPool == null) {
			synchronized(this) {
				if (mSessionPool == null) {
			        mSessionPool = new ThreadPoolExecutor(mCorePoolSize, mMaxPoolSize, 60,
			                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(60),
			                new ThreadFactory() {
			                    public Thread newThread(Runnable r) {
			                        Thread thread = new Thread(r, "HttpSessionThread");
//			                        thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			                        return thread;
			                    }
			                }, new ThreadPoolExecutor.AbortPolicy());

			        if (android.os.Build.VERSION.SDK_INT >= 9) {
			        	mSessionPool.allowCoreThreadTimeOut(true);
			        }
				}
			}
		}
		return mSessionPool;
	}

	@Override
	public void onConnectInfoReload() {
		mMaxPoolSize = getMaxCoreNumber(ConnectInfoManager.get().getConnectInfo());
		if (mSessionPool != null) {
			mSessionPool.setMaximumPoolSize(mMaxPoolSize);
		}
	}
	
	private int getMaxCoreNumber(ConnectInfo connectInfo) {
		if (connectInfo.getNetType() == NetType.NET_WIFI) {
			return mCorePoolSize * 4;
		} else {
			return mCorePoolSize * 2;
		}
	}

	@Override
	public void onReceiveNetworkChanged() {
		
	}

}
