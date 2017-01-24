/**   
* @Title: QubeWupEngine.java 
* @Package com.tencent.qube.wup 
* @author interzhang   
* @date 2012-7-19 下午06:23:36 
* @version V1.0   
*/
package qrom.component.wup.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import qrom.component.wup.utils.PhoneStatUtils;
import qrom.component.wup.utils.QWupLog;

import android.util.SparseArray;


public class QubeWupNetEngine {
    
    private String TAG = "QubeWupNetEngine";
    private static QubeWupNetEngine mInstance;
    
    private ThreadPoolExecutor mThreadPoolAsync = null;
    // 同步请求的线程池不会暴露给外部回调，否则外部回调在该线程池中回调，可能会出现死锁的问题
    private ThreadPoolExecutor mThreadPoolSync = null; 
    
    private SparseArray<QubeWupTask> mTaskCache = null;
    
    private QubeWupNetEngine() {
        
    }
    
    public static void init() {
    	 if (mInstance == null) {
             mInstance = new QubeWupNetEngine();
         }
    }
    
    public static QubeWupNetEngine getInstance() {
       
        return mInstance;
    }
    

    public synchronized void shutdownThreadPool() {
        if (mThreadPoolAsync != null) {
        	mThreadPoolAsync.purge();
        	mThreadPoolAsync.shutdownNow();
        	mThreadPoolAsync = null;
        }
        
        if (mThreadPoolSync != null) {
        	mThreadPoolSync.purge();
        	mThreadPoolSync.shutdown();
        	mThreadPoolSync = null;
        }
        
        if (mTaskCache != null) {
            mTaskCache.clear();
        }
        mInstance = null;
     }
    
    public synchronized boolean sendTask(QubeWupTask task) {
        if (mTaskCache == null) {
            mTaskCache = new SparseArray<QubeWupTask>(); 
        }
        
        try {
            mTaskCache.append(task.getTaskId(), task);
            QWupLog.trace(TAG, "sendTask: reqId = " + task.getTaskId());
            if (task.getWupTaskData().isSync()) {
            	getThreadPoolSync().execute(task);
            } else {
            	getThreadPoolAsync().execute(task);
            }
        } catch (Exception e) {
            QWupLog.trace(TAG, "err msg: " + e.getMessage() + ", e: " + e);
            return false;
        }
        return true;
    }
    
    public synchronized QubeWupTask cancelTask(int reqId) {
        if (mTaskCache == null) {
            return null;
        }
        QubeWupTask task = mTaskCache.get(reqId);
        if (task != null) {
        	if (task.getWupTaskData().isSync()) {
        		getThreadPoolSync().remove(task);
        	} else {
        		getThreadPoolAsync().remove(task);       
        	}
            mTaskCache.remove(reqId);
        }
        return task;
    }
    
    
    public synchronized boolean onFinishTask(int reqId) {
        if (mTaskCache != null) {
            QubeWupTask task = mTaskCache.get(reqId);
            if (task != null) {
                mTaskCache.remove(reqId);
                return true;
            }
        }
        return false;
    }

    private synchronized ThreadPoolExecutor getThreadPoolAsync() {
        if (null == mThreadPoolAsync) {
        	mThreadPoolAsync = getThreadPoolByCoreNum("wup_async_thread");
        }
        return mThreadPoolAsync;
    }
    
    private synchronized ThreadPoolExecutor getThreadPoolSync() {
        if (null == mThreadPoolSync) {
        	mThreadPoolSync = getThreadPoolByCoreNum("wup_sync_thread");
        }
        return mThreadPoolSync;
    }
    
    private static ThreadPoolExecutor getThreadPoolByCoreNum(final String name) {
    	int coreNum = PhoneStatUtils.getCpuCoreNum();
        int corePoolSize = coreNum <= 2 ? coreNum * 2 : coreNum;

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, 2 * corePoolSize, 5 * 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(48),
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, name);
//                        thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                        return thread;
                    }
                }, new ThreadPoolExecutor.DiscardOldestPolicy());

        if (android.os.Build.VERSION.SDK_INT >= 9) {
        	threadPool.allowCoreThreadTimeOut(true);
        }
        return threadPool;
    }

    /**
     * 根据reqId强制取消当前请求
     * @param reqId
     */
    public synchronized boolean foceCloseConnect(int reqId) {
    	QWupLog.trace( "QubeWupEngine", "Wup ENGINE -- timeout handleMessage -- cancel reqID =  " + reqId);
        if (mTaskCache == null || mTaskCache.size() == 0) {
            return false;
        }
        QubeWupTask task = mTaskCache.get(reqId);
        if (task != null) {  // 找到对应任务
            QWupLog.e("QubeWupEngine", "foceCloseConnect : task.releaseConnect()");
            task.cancel();
            return true;
        }
        
        return false;
    }
    
    public  QubeWupTask queryWupTask(int reqId) {
        
        if (mTaskCache == null || mTaskCache.size() == 0) {
            return null;
        }
        return mTaskCache.get(reqId);
    }
}
