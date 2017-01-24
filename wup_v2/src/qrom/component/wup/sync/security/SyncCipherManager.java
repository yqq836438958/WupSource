package qrom.component.wup.sync.security;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.sync.security.core.SyncCipherProcesser;
import android.content.Context;


public class SyncCipherManager {
    
    private static final String TAG = "SyncCipherManager";
    private Context mContext;
    private SyncCipherProcesser mCipherProcesser;
    
    public SyncCipherManager(Context context, RunEnvType envType) {
        assert (context != null);
        mContext = context;
        mCipherProcesser = new SyncCipherProcesser(mContext, envType);
    }
    
    /**
     * 设置start的超时时间，单位：毫秒
     * @param timeout (0表示没有超时)
     */
    public void setTimeout(long timeout) {
        if (timeout < 0)
            return;
        mCipherProcesser.setTimeout(timeout);
    }
    
    /**
     * 开始与后台建立安全通道
     * @return 建立成功与否
     */
    public boolean start() {
        return mCipherProcesser.start();
    }
    
    /**
     * 判断安全通道是否已经建立
     * @return
     */
    public boolean isReady() {
        return mCipherProcesser.isReady();
    }
    
    /**
     * 加密数据
     * @param data
     * @return
     */
    public byte[] encrypt(byte[] data) {
        if (!isReady()) {
            QRomLog.e(TAG, "encrypt is not ready");
            return null;
        }
        return mCipherProcesser.encrypt(data);
    }
    
    /**
     * 解密数据
     * @param data
     * @return
     */
    public byte[] decrypt(byte[] data) {
        if (!isReady()) {
            QRomLog.e(TAG, "decrypt is not ready");
            return null;
        }
        return mCipherProcesser.decrypt(data);
    }
    
    /**
     * 获取本次安全通信的session id
     * @return
     */
    public String getSessionId() {
        if (!isReady()) {
            QRomLog.e(TAG, "getSessionId is not ready");
            return null;
        }
        return mCipherProcesser.getSessionId();
    }
}
