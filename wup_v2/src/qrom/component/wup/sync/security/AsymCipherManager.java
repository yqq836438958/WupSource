package qrom.component.wup.sync.security;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.sync.security.core.SyncCipherProcesser;
import android.content.Context;
import android.os.SystemClock;

public class AsymCipherManager {
    
    public static final String TAG = "AsymCipherManager";
    private static final int DEFAULT_SESSION_TIMEOUT = 8 * 60 * 1000;   // 8mins
    private static final int EXPIRED_MAP_SIZE = 10;
    private Context mContext;
    private Object mLock = new Object();
    private StatisticListener mStatListener;
    private ConcurrentHashMap<RunEnvType, ConcurrentHashMap<String, ProcesserInfo>> mEnvPackageMap;
    private ConcurrentHashMap<RunEnvType, ConcurrentHashMap<String, ProcesserInfo>> mEnvSessionMap;
    private ConcurrentHashMap<RunEnvType, LinkedHashMap<String, ProcesserInfo>> mEnvExpiredSessionMap;
    
    class ProcesserInfo {
        long createTime = 0;
        SyncCipherProcesser processer;
    }
    
    public AsymCipherManager(Context context) {
        assert (context != null);
        mContext = context;
        mEnvPackageMap = new ConcurrentHashMap<RunEnvType, ConcurrentHashMap<String, ProcesserInfo>>();
        mEnvSessionMap = new ConcurrentHashMap<RunEnvType, ConcurrentHashMap<String, ProcesserInfo>>();
        mEnvExpiredSessionMap = new ConcurrentHashMap<RunEnvType, LinkedHashMap<String, ProcesserInfo>>();
    }
    
    /**
     * 按照packageName的颗粒度建立并获取session id
     * @param packageName
     * @return
     */
    public String getSessionId(String packageName, RunEnvType envType) {
        return getSessionIdInternal(packageName, envType);
    }
    
    /**
     * 用于session过期后重置，会发起请求重新获取session id
     * @param packageName
     */
    public boolean resetSession(String packageName, RunEnvType envType) {
        return resetSessionInternal(packageName, envType);
    }
    
    /**
     * 通知该session已经无效（过期）
     * @param packageName
     */
    public void notifySessionInvalid(String packageName, RunEnvType envType) {
        removeProcceser(packageName, envType);
    }
    
    /**
     * 加密数据
     * @param sessionId: 已经建立的session id
     * @param data
     * @return
     */
    public byte[] encrypt(String sessionId, byte[] data, RunEnvType envType) {
        ProcesserInfo info = null;
        synchronized (mLock) {
            if (!getSessionMap(envType).containsKey(sessionId)) {
                QRomLog.w(TAG, "encrypt failed: session(" + sessionId + ") is going to expire");
                if (!getExpiredSessionMap(envType).containsKey(sessionId)) {
                    QRomLog.e(TAG, "encrypt failed: session(" + sessionId + ") is invalid");
                    return null;
                } else {
                    info = getExpiredSessionMap(envType).get(sessionId);
                }
            } else {
                info = getSessionMap(envType).get(sessionId);
            }
        }
        
        byte[] result = getSessionMap(envType).get(sessionId).processer.encrypt(data);
        if (result == null) {
            QRomLog.e(TAG, "encrypt failed: packageName=" + info.processer.getPackageName());
            QRomLog.e(TAG, "encrypt failed: processor create time=" + new Date(info.createTime));
            QRomLog.e(TAG, "encrypt failed: processor session id=" + info.processer.getSessionId());
            QRomLog.e(TAG, "encrypt failed: sessionId=" + sessionId);
        }
        
        return result;
    }
    
    /**
     * 加密数据
     * @param sessionId: 已经建立的session id
     * @param data
     * @return
     */
    public byte[] decrypt(String sessionId, byte[] data, RunEnvType envType) {
        ProcesserInfo info = null;
        synchronized (mLock) {
            if (!getSessionMap(envType).containsKey(sessionId)) {
                QRomLog.w(TAG, "decrypt: session(" + sessionId + ") is going to expire");
                if (!getExpiredSessionMap(envType).containsKey(sessionId)) {
                    QRomLog.e(TAG, "decrypt failed: session(" + sessionId + ") is invalid");
                    return null;
                } else {
                    info = getExpiredSessionMap(envType).get(sessionId);
                }
            } else {
                info = getSessionMap(envType).get(sessionId);
            }
        }
        
        byte[] result = info.processer.decrypt(data);
        if (result == null) {
            QRomLog.e(TAG, "decrypt failed: packageName=" + info.processer.getPackageName());
            QRomLog.e(TAG, "decrypt failed: processor create time=" + new Date(info.createTime));
            QRomLog.e(TAG, "decrypt failed: processor session id=" + info.processer.getSessionId());
            QRomLog.e(TAG, "decrypt failed: sessionId=" + sessionId);
        }
        
        return result;
    }
    
    public void setStatisticListener(StatisticListener listener) {
        mStatListener = listener;
    }
    
    private String getSessionIdInternal(String packageName, RunEnvType envType) {
        if (getPackageMap(envType).containsKey(packageName)) {
            long createTime = getPackageMap(envType).get(packageName).createTime;
            if ((System.currentTimeMillis() - createTime) < DEFAULT_SESSION_TIMEOUT) { 
                return getPackageMap(envType).get(packageName).processer.getSessionId();
            } else {
                removeProcceser(packageName, envType);
            }
        }
        
        SyncCipherProcesser processer = new SyncCipherProcesser(mContext, envType);
        processer.setPackageName(packageName);
        processer.setStatisticListener(mStatListener);
        long beginStart = SystemClock.elapsedRealtime();
        if (!processer.start()) {
            QRomLog.e(TAG, "getSessionId processer start failed");
            return null;
        }
        long endStart = SystemClock.elapsedRealtime();
        if (mStatListener != null) {
            mStatListener.onStartDuration(packageName, endStart - beginStart);
        }
        
        long nowTime = System.currentTimeMillis();
        ProcesserInfo info = makeProcesserInfo(nowTime, processer);
        QRomLog.d(TAG, "getSessionIdInternal: packageName=" + processer.getPackageName());
        QRomLog.d(TAG, "getSessionIdInternal: processor create time=" + new Date(nowTime));
        QRomLog.d(TAG, "getSessionIdInternal: processor sessionId=" + processer.getSessionId());
        synchronized (mLock) {
            if (getPackageMap(envType).contains(packageName)) {
                return getPackageMap(envType).get(packageName).processer.getSessionId();
            }
            getPackageMap(envType).put(packageName, info);
            getSessionMap(envType).put(processer.getSessionId(), info);
        }
        
        return processer.getSessionId();
    }
    
    private boolean resetSessionInternal(String packageName, RunEnvType envType) {
        removeProcceser(packageName, envType);
        return getSessionIdInternal(packageName, envType) != null;
    }
    
    private void removeProcceser(String packageName, RunEnvType envType) {
        if (!getPackageMap(envType).containsKey(packageName)) {
            QRomLog.w(TAG, "remove invalid proccesser, packageName=" + packageName);
            return;
        }
        
        ProcesserInfo info = getPackageMap(envType).get(packageName);
        synchronized (mLock) {
            getPackageMap(envType).remove(packageName);
            getSessionMap(envType).remove(info.processer.getSessionId());
            if (getExpiredSessionMap(envType).size() >= EXPIRED_MAP_SIZE) {
                Entry<String, ProcesserInfo> first = getExpiredSessionMap(envType).entrySet().iterator().next();
                getExpiredSessionMap(envType).remove(first.getKey());
                QRomLog.d(TAG, "removeProcceser: remove session(" + first.getKey() + ")");
            }
            getExpiredSessionMap(envType).put(info.processer.getSessionId(), info);
        }
    }
    
    private ProcesserInfo makeProcesserInfo(long time, SyncCipherProcesser processer) {
        ProcesserInfo info = new ProcesserInfo();
        info.createTime = time;
        info.processer = processer;
        return info;
    }
    
    private ConcurrentHashMap<String, ProcesserInfo> getPackageMap(RunEnvType envType) {
        if (mEnvPackageMap.containsKey(envType)) {
            return mEnvPackageMap.get(envType);
        }
        ConcurrentHashMap<String, ProcesserInfo> packageMap 
                = new ConcurrentHashMap<String, ProcesserInfo>();
        mEnvPackageMap.put(envType, packageMap);
        return packageMap;
    }
    
    private ConcurrentHashMap<String, ProcesserInfo> getSessionMap(RunEnvType envType) {
        if (mEnvSessionMap.containsKey(envType)) {
            return mEnvSessionMap.get(envType);
        }
        ConcurrentHashMap<String, ProcesserInfo> sessionMap
                = new ConcurrentHashMap<String, ProcesserInfo>();
        mEnvSessionMap.put(envType, sessionMap);
        return sessionMap;
    }
    
    private LinkedHashMap<String, ProcesserInfo> getExpiredSessionMap(RunEnvType envType) {
        if (mEnvExpiredSessionMap.containsKey(envType)) {
            return mEnvExpiredSessionMap.get(envType);
        }
        LinkedHashMap<String, ProcesserInfo> expiredSessionMap
                = new LinkedHashMap<String, ProcesserInfo>(EXPIRED_MAP_SIZE);
        mEnvExpiredSessionMap.put(envType, expiredSessionMap);
        return expiredSessionMap;
    }
}
