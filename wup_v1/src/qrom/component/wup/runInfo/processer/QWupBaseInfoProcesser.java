package qrom.component.wup.runInfo.processer;

import java.util.List;
import java.util.Map;

import qrom.component.wup.QRomQuaFactory;
import qrom.component.wup.QRomWupConstants.LOGIN_RSP_CODE;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.build.QWupUriFactory;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.runInfo.QRomWupProviderImpl;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.wupData.QRomIplistData;
import qrom.component.wup.wupData.QRomWupInfo;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;


public abstract class QWupBaseInfoProcesser implements Callback {
    
    protected String TAG = "QubeWupBaseInfoProcesser";
    
    /** wup 代理服务器索引 */
    protected int mProxyIndex = 0;
    private final Integer mProxyIndexLock = 0;
    
    /** wup socket代理服务器索引 */
    protected int mSocketProxyIndex = 0;
    private byte[] mSocketProxyLock = new byte[0];
    
    protected long mRomId = -1;
    
    /** 当前使用的default iplist信息 */
    protected QRomIplistData mDefaultIpData = null;
    /** 当前使用的default socket iplist信息 */
    protected QRomIplistData mDefaultSocketIpData = null;
    
    protected Handler mTimeoutHandler = null;
    
    public void startUp(Context context) {
    }
    
    /**
     * 更新wup缓存信息
     * @return
     */
    public boolean refreshInfos() {
        return false;
    }
    
    /**
     * 主动发起获取guid请求
     *   -- 仅当guid无效是发送请求
     * @return
     */
    public int requestGuid() {
        return LOGIN_RSP_CODE.LOGIN_CANCEL_NONEED;
    }
    
    
    /**
     * 发送guid的请求
     * 
     * @return 参考LOGIN_RSP_CODE
     */
    public int sendGuidRequest() {
        return LOGIN_RSP_CODE.LOGIN_CANCEL_NONEED;
    }
    
    /**
     * 主动发起获取ipList请求
     * @param isForce 是否强制更新<br>
     *        true: 忽略缓存超时时间，检测发送间隔是否频繁,及当前屏幕状态;<br>
     *        false: 判断缓存是否超时，缓存超时且亮屏时才发起请求
     * @return
     */
    public int requestIpList(int reqType) {
        return -1;
    }
    
    public int doLogin() {
        
        
        return LOGIN_RSP_CODE.LOGIN_CANCEL_NONEED;
    }
    
	/**
	 * 获取当前所有接入点的wup代理地址
	 * @return
	 */
	public SparseArray<List<String>> getAllWupProxyInfos() {  
		
		return null;							
	}
	/**
	 * 获取当前所有接入点的wup socket 代理地址
	 * @return
	 */
	public SparseArray<List<String>> getAllWupSocketProxyInfos() {  
		
		return null;				
	}
	
	   /**
     * 获取当前所有接入点的wup代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupProxyIplistDatas() {  
        
        return null;                            
    }
    
    /**
  * 获取所有wifi接入点的wup代理地址
  * @return
  */
 public Map<String, QRomIplistData> getAllWupWifiProxyIplistDatas() {  
     
     return null;                            
 }
    
    /**
     * 获取当前所有接入点的wup socket 代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupSocketProxyIplistDatas() {  
        
        return null;                
    }
    
    /**
     * 获取所有wifi接入点的wup socket 代理地址
     * @return
     */
    public Map<String, QRomIplistData> getAllWupWifiSocketProxyIplistDatas() {  
        
        return null;                
    }
	    
    /**
     * 获取wup http代理地址
     * @return
     */
    public QRomIplistData getWupProxyAddressData() {        

        QRomIplistData iplistData = getCurApnProxyListData();
        
        if (iplistData == null || iplistData.isEmpty()) { // 无对应接入点iplist信息
            QWupLog.trace(TAG, "====getWupProxyAddressData -> cur Iplist data is null, usedDefault");
            if (mDefaultIpData == null) {
                mDefaultIpData = QRomIplistData.createDefaultIpListInfo();
            }
            return mDefaultIpData;
        }  
        
        if (!iplistData.setIndex(mProxyIndex)) {  // 索引更换失败             
            // 通知iplist全部失败
            QWupLog.trace(TAG, "====getWupProxyAddress -> proxyList: " + iplistData);
            onCurApnIpListAllErr(iplistData.getIplistInfo());
        }

        // 返回对应的iplist信息
        return  iplistData;
    }
    
    /**
     * 获取wup http代理地址
     * @return
     */
    public QRomIplistData getWupSocketProxyAddressData() {        

        QRomIplistData iplistData = getCurApnSocketListData();
        
        if (iplistData == null || iplistData.isEmpty()) { // 无对应接入点iplist信息
            QWupLog.trace(TAG, "====getWupProxyAddressData -> cur Iplist data is null, usedDefault");
            if (mDefaultIpData == null) {
                mDefaultIpData = QRomIplistData.createDefaultSocketIpListInfo();
            }
            return mDefaultIpData;
        }  
        
        if (!iplistData.setIndex(mSocketProxyIndex)) {  // 索引更换失败             
            // 通知iplist全部失败
            QWupLog.trace(TAG, "====getWupProxyAddress -> proxyList: " + iplistData);
            onCurApnSocketIpListAllErr(iplistData.getIplistInfo());
        }

        // 返回对应的iplist信息
        return  iplistData;
    }
        
    public void setRomId(long romId) {
        
    }
    
    public long getRomId() {

        // 从rom层获取
        Context context = QRomWupImplEngine.getInstance().getContext();
        long romId = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    QWupUriFactory.getRomIdUri(), null, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            // 获取romId
            romId = cursor.getLong(cursor.getColumnIndex(
                    QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_QROM_ID));
            if (romId < 0) {
                romId = 0;
            }
        } catch (Exception e) {
            QWupLog.w(TAG, "getRomId -> err msg: " + e.getMessage());
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return romId;
    }
    
    public long getRomIdFromTsf() {
        return 0;
    }
    
    public String getQua(Context context) {
        return QRomQuaFactory.buildQua(context);
    }
    
    /**
     * 检测guid是否合法，不合法则发起guid更新请求
     * @param delay  延时时间
     * @return boolean
     */
    public boolean checkGuid(int delay) {
        return false;        
    }
    
    /**
     * 网络改变
     * @param newApn
     * @param oldApn
     * @return
     */
    public boolean onConnectivityChanged(int newApn, int oldApn) {
      
        if (newApn != oldApn) {  // 网络类型变更, index 设置为0
            setProxyServer(0);
            setSocketProxyServer(0);
        }
        return true;
    }
    
    /**
     * guid是否合法
     * @return
     */
    protected boolean isGuidValidate(byte[] guid) {
        return QRomWupInfo.isGuidValidate(guid);
    }
    
    public void changeToNextAddr() {
        
        setProxyServer(mProxyIndex + 1);
    }
    
    
    public void changeToNextSocketAddr() {
        setSocketProxyServer(mSocketProxyIndex + 1);
    }
    
    /**
     * 设置代理服务器索引
     */
    protected void setProxyServer(int index) {
        synchronized (mProxyIndexLock) {
            QRomIplistData iplistData = getCurApnProxyListData();
            if ( iplistData != null && index > iplistData.getCurIplistSize()) {  // 从头开始轮询
                QWupLog.i(TAG, "setProxyServer: index reset 0,");
                mProxyIndex = 0;
            } else {
                QWupLog.i(TAG, "setProxyServer: index = " + index);
                mProxyIndex = index;
            }            
        }
    }
    
    
    /**
     * 设置wup socket代理服务器索引
     */
    protected void setSocketProxyServer(int index) {
        
        synchronized (mSocketProxyLock) {
            QRomIplistData iplistData = getCurApnSocketListData();
            if (iplistData != null && index >iplistData.getCurIplistSize()) {  // 从头开始轮询
                QWupLog.i(TAG, "setSocketProxyServer: index reset 0,");
                mSocketProxyIndex = 0;
            } else {
                QWupLog.i(TAG, "setSocketProxyServer: index = " + index);
                mSocketProxyIndex = index;
            }
        }
    }
    
    protected void removeMsg(int what) {
        if (mTimeoutHandler != null) {            
            mTimeoutHandler.removeMessages(what);
        }
    }
    
    protected boolean sendMsg(int msgType, long delay) {
        return sendMsg(msgType, -1, delay);
    }
    
    protected boolean sendMsg(int msgType, int arg1, long delay) {
      
        return sendMsg(msgType, arg1, null, delay);
    }
    
    protected synchronized boolean sendMsg(int msgType, int arg1, Object obj, long delay) {
        
        if (mTimeoutHandler == null) {
            Looper looper =  QRomWupImplEngine.getInstance()
                    .getWupTimeoutMananger().getTimeoutLooper();
            if (looper == null) {
                QWupLog.trace(TAG, "sendMsg -> init looper null!");
                return false;
            }
            mTimeoutHandler = new Handler(looper, this);
        }
        
        Message message = mTimeoutHandler.obtainMessage(msgType);
        message.arg1 = arg1;
        message.obj = obj;
        return mTimeoutHandler.sendMessageDelayed(message, delay);
    }
    
    public boolean handleMessage(Message msg) {
        return false;
    }
    
    public void onReceiveAllData(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response) {
        String info = "onReceivedAllData reqID = " + reqId + " modelType = " + fromModelType 
                + " operType = " + operType;
        QWupLog.trace( TAG, info);
    }
    
    public void onReceiveError(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, QRomWupRspExtraData wupRspExtraData, 
            String serviceName, int errorCode, String description) {
        String info = "onReceivedError reqID = " + reqId +"  modelType = " + fromModelType 
                + " operType = " + operType 
                + "   errorCode: " + errorCode+ "  description: " + description;
        QWupLog.trace( TAG, info);
    }
    
    public Context getContext() {
        return QRomWupImplEngine.getInstance().getContext();
    }
    
    public void release() {
        
    }
    
    /**
     * 当前类型的wu代理都失效了
     * @param proxyList
     */
    public abstract void onCurApnIpListAllErr(List<String> proxyList);
    /**
     * 当前类型的wup socket 代理都失效了
     * @param socketProxyList
     */
    public abstract void onCurApnSocketIpListAllErr(List<String> socketProxyList);
    /**
     * 获取guid
     * @return
     */
    public abstract byte[] getGUIDBytes();
    /**
     * 获取当前接入点的wup 代理地址列表
     * @return
     */
    @Deprecated
    public abstract List<String> getCurApnProxyList();
    
    /**
     * 获取当前接入点的wup socket 代理地址列表
     * @return
     */
    @Deprecated
    public abstract List<String> getCurSocketProxyList();
    
    /**
     * 获取当前接入点的wup 代理地址列表
     * @return
     */
    public abstract QRomIplistData getCurApnProxyListData();
    
    
    /**
     * 获取当前接入点的wup socket 代理地址列表
     * @return
     */
    public abstract QRomIplistData getCurApnSocketListData();
    
}
