package qrom.component.wup.wupData;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.SparseArray;

public class QRomAppInRomCache {
    
    private byte[] mVGUID;
    
    /** 对应接入点缓存的iplist 信息 */
    private SparseArray<QRomIplistData>  mProxyIpDatas = new SparseArray<QRomIplistData>();
    /** 对应wifi接入点缓存的iplist 信息 */
    private Map<String, QRomIplistData>  mWifiProxyIpDatas = new HashMap<String, QRomIplistData>();
    
    /** 对应接入点缓存的socket iplist 信息 */
    private SparseArray<QRomIplistData>  mWupSocketIpDatas = new SparseArray<QRomIplistData>();
    /** 对应wifi接入点缓存的socket iplist 信息 */
    private Map<String, QRomIplistData>  mWifiWupSocketIpDatas = new HashMap<String, QRomIplistData>();


    public void load() {
        
    }
    
    public byte[] getGuidBytes() {
        if (mVGUID == null) {
            return QRomWupInfo.DEFAULT_GUID_BYTES;
        }
        
        return mVGUID;
    }
    
    /**
     * @param mSGUID the mSGUID to set
     */
    public void setSGUID(byte[] sGUID, Context context) {

        if (sGUID == null) {
            sGUID = QRomWupInfo.DEFAULT_GUID_BYTES;
        }
        mVGUID = sGUID;
    }
    
    /**
     * 获取对应接入点类型的wup代理信息
     * @param apnIndex  当前接入点索引
     * @return  QRomIplistData
     */
    public QRomIplistData getProxyIpListDataByType(int apnIndex) {
        return mProxyIpDatas.get(apnIndex);
    }
    
    /**
     * 获取wifi对应bssid接入点wup代理地址
     * @param bssid   wifi对应的bssid
     * @return  QRomIplistData
     */
    public QRomIplistData getProxyIpListDataByBssid(String bssid) {
        return mWifiProxyIpDatas.get(bssid);
    }
    
    /**
     * 获取对应接入点类型的wup socket代理信息
     * @param apnIndex  当前接入点索引
     * @return QRomIplistData
     */
    public QRomIplistData getWupSocketIpListDataByType(int apnIndex) {
        return mWupSocketIpDatas.get(apnIndex);
    }
    
    /**
     * 获取wifi对应bssid接入点wup socket代理地址
     * @param bssid  wifi对应的bssid
     * @return QRomIplistData
     */
    public QRomIplistData getWupSocketIpListDataByBssid(String bssid) {
        return mWifiWupSocketIpDatas.get(bssid);
    }
    
    public void updateProxyIpListDataByType(int apnIndex, QRomIplistData data) {
        mProxyIpDatas.put(apnIndex, data);
    }
    
    public void updateWifiProxyIpListDataByType(String bssid, QRomIplistData data) {
        mWifiProxyIpDatas.put(bssid, data);
    }
    
    public void updateWupSocketIpListDataByType(int apnIndex, QRomIplistData data) {
        mWupSocketIpDatas.put(apnIndex, data);
    }
    
    public void clearWupProxyInfo() {
        mProxyIpDatas.clear();
    }
    
    public void clearWupWifiProxyInfo() {
        mWifiProxyIpDatas.clear();
    }
    
    public void clearWupSocketProxyInfo() {
        mWupSocketIpDatas.clear();
    }
    
    /**
     * 更新所有的wup 代理地址
     * @param iplistDatas
     */
    public void updateAllProxyCache(SparseArray<QRomIplistData> iplistDatas) {
        if (iplistDatas == null || iplistDatas.size() == 0) {
            return;
        }
        
        if (mProxyIpDatas == iplistDatas) {  // 是同一个对象
            return;
        }        
        mProxyIpDatas.clear();
        int size = iplistDatas.size();
        int key = -1;
        for (int i = 0; i < size; i++) {
            key = iplistDatas.keyAt(i);
            mProxyIpDatas.put(key, iplistDatas.get(key));
        }        
    }
    
    /**
     * 更新所有的socket代理地址
     * @param iplistDatas
     */
    public void updateAllSocketProxyCache(SparseArray<QRomIplistData> iplistDatas) {
        if (iplistDatas == null || iplistDatas.size() == 0) {
            return;
        }
        
        if (mWupSocketIpDatas == iplistDatas) {  // 是同一个对象
            return;
        }        
        mWupSocketIpDatas.clear();
        int size = iplistDatas.size();
        int key = -1;
        for (int i = 0; i < size; i++) {
            key = iplistDatas.keyAt(i);
            mWupSocketIpDatas.put(key, iplistDatas.get(key));
        }        
    }
    
    /**
     * 更新所有wifi下bssid 缓存的iplsit
     * @param iplistDatas
     */
    public void updateAllWifiProxyCache(Map<String, QRomIplistData> iplistDatas) {
       
        if (iplistDatas == null || iplistDatas.size() == 0) {
            return;
        }
        
        if (mWifiProxyIpDatas == iplistDatas) {  // 是同一个对象
            return;
        }
        
        mWifiProxyIpDatas.clear();
        mWifiProxyIpDatas.putAll(iplistDatas);
    }
    
    /**
     * 更新所有wifi下bssid 缓存的socket iplsit
     * @param iplistDatas
     */
    public void updateAllWifiSocketProxyCache(Map<String, QRomIplistData> iplistDatas) {
       
        if (iplistDatas == null || iplistDatas.size() == 0) {
            return;
        }
        
        if (mWifiWupSocketIpDatas == iplistDatas) {  // 是同一个对象
            return;
        }
        
        mWifiWupSocketIpDatas.clear();
        mWifiWupSocketIpDatas.putAll(iplistDatas);
    }
    
    /**
     * 获取所有接入点 wup 代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupProxyIplistDatas() {        

        return mProxyIpDatas;
    }
    
    /**
     * 获取所有接入点 wup 代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupSocketProxyIplistDatas() {        

        return mWupSocketIpDatas;
    }
    
    public Map<String, QRomIplistData> getAllWupWifiProxyInfos() {
        return mWifiProxyIpDatas;
    }
    
    public Map<String, QRomIplistData> getAllWupWifiSocketProxyInfos() {
        return mWifiWupSocketIpDatas;
    }
    
    public boolean hasProxyDataCached() {
        return mProxyIpDatas.size() != 0 && !mWifiProxyIpDatas.isEmpty();
    }
    
    public boolean hasSocketProxyDataCached() {
        return mWupSocketIpDatas.size() != 0 && !mWifiWupSocketIpDatas.isEmpty();
    }
    
    public void clear() {
        mProxyIpDatas.clear();
        mWifiProxyIpDatas.clear();
        mWupSocketIpDatas.clear();
        mWifiWupSocketIpDatas.clear();
        mVGUID = QRomWupInfo.DEFAULT_GUID_BYTES;
        
    }
    
}
