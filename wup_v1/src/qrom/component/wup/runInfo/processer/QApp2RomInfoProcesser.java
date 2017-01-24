package qrom.component.wup.runInfo.processer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.build.QTcmWupOldUriCompatible;
import qrom.component.wup.build.QWupUriFactory;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.runInfo.QRomWupProviderImpl;
import qrom.component.wup.runInfo.QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN;
import qrom.component.wup.runInfo.QRomWupProviderImpl.WUP_ROM_PROVIDER_OPER_INFO;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_REQ_TYPE;
import qrom.component.wup.utils.QWupSdkConstants.WUP_OPER_TYPE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomAppInRomCache;
import qrom.component.wup.wupData.QRomIplistData;
import qrom.component.wup.wupData.QRomWupInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;


public class QApp2RomInfoProcesser extends QWupBaseInfoProcesser {
    
    private long mLastNetChangedTime = 0;
    
    protected QRomAppInRomCache mWupInfoCache = new QRomAppInRomCache();
    
    private String M_WIFI_SELECTION = WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_TYPE +"=?";
    
    private BroadcastReceiver mReceiver;
    
    public QApp2RomInfoProcesser() {
        TAG ="====QApp2RomInfoProcesser";
    }
    
    @Override
    public void startUp(Context context) {
        super.startUp(context);        
        updataAllWupFromRom();
        if (mReceiver == null) {
            mReceiver = new WupRunTimeReciver();
            IntentFilter filter = new IntentFilter();
            // 监听iplist改变的广播
            filter.addAction(QWupSdkConstants.ACTION_WUP_SDK_BASEDATA_UPDATED);
            addActionFilter(filter);
            context.registerReceiver(mReceiver, filter);
        }
    }
   
    protected void addActionFilter(IntentFilter filter) {
        
    }
    
    @Override
    public int requestGuid() {
        
        if (QRomWupInfo.isGuidValidate(mWupInfoCache.getGuidBytes())) { // guid合法
        	QWupLog.w(TAG, "requestGuid -> guid is ok");
        	return -1;
        }
        QWupLog.w(TAG, "requestGuid -> sendGetRomWupDataMsg");
        updataGuidFromRom();
        return 0;
    }
    
    @Override
    public int requestIpList(int reqType) {
        QWupLog.w(TAG, "requestIpList: reqType = " + reqType);
        updataProxyIpInfosFromRom();
        if (IPLIST_REQ_TYPE.IPLIST_REQ_ROM_UPDATE == reqType) {  // 向rom请求iplist
            notifyRomHostUpdataIplist();
        }
        return 0;
    }
    
    @Override
    public void onCurApnIpListAllErr(List<String> proxyList) {
        // 重新拉取ip
        updataProxyIpInfosFromRom();
        // 通知rom host重新拉取iplist,忽略缓存时间
        notifyRomHostUpdataIplist();
    }

    @Override
    public void onCurApnSocketIpListAllErr(List<String> socketProxyList) {
                
        // 重新拉取ip
        updataSocketProxyIpInfosFromRom();
        notifyRomHostUpdataIplist();
    }

    @Override
    public byte[] getGUIDBytes() {
        
        if (!isGuidValidate(mWupInfoCache.getGuidBytes())) { // guid不合法
            updataGuidFromRom();
        }

        return mWupInfoCache.getGuidBytes();
    }
        
    @Override
    @Deprecated
    public List<String> getCurApnProxyList() {
        
        QRomIplistData iplistData = getCurApnProxyListData();
        return iplistData == null? null : iplistData.getIplistInfo();
        
    }
    
    @Override
    @Deprecated
    public List<String> getCurSocketProxyList() {
        QRomIplistData iplistData = getCurApnSocketListData();
        
        return iplistData == null? null : iplistData.getIplistInfo();
    }
    
    @Override
    public QRomIplistData getCurApnProxyListData() {
        
        if (mWupInfoCache == null || !mWupInfoCache.hasProxyDataCached()) {
            updataProxyIpInfosFromRom();
        }
        
        QRomIplistData iplistData = null;
        int proxyIndex = ApnStatInfo.getCurApnProxyIndex();
        if (proxyIndex == ApnStatInfo.PROXY_LIST_WIFI) {  // wifi下优先获取对应bssid缓存
            iplistData = mWupInfoCache.getProxyIpListDataByBssid(ApnStatInfo.getWifiBSSID(getContext()));
        }
        if (iplistData == null || iplistData.isEmpty()) {  // 对应bssid下未找到缓存ip，则使用上次拉取的默认wifi索引下的iplist
            iplistData = mWupInfoCache.getProxyIpListDataByType(proxyIndex);
        }
        QWupLog.trace(TAG, "getCurApnProxyListData-> proxyIndex = " + proxyIndex 
                + ", apnName =" + ApnStatInfo.getApnName());
//        // 测试用
//        List<String> iplist = new  ArrayList<String>();
//        iplist.add("1.1.1.1:111");
//        iplist.add("2.1.1.2:111");
//        iplist.add("3.1.1.3:111");
////        iplist.add("4.1.1.4:111");
//        if (iplistData == null) {
//            iplistData = new QRomIplistData("test", iplist, -1, 0, "");
//        } else {
//            iplistData.getIplistInfo().clear();
//            iplistData.getIplistInfo().addAll(iplist);
//        }
        return iplistData;
    }
    
    @Override
    public QRomIplistData getCurApnSocketListData() {
        
        if (mWupInfoCache == null || !mWupInfoCache.hasSocketProxyDataCached()) {
            updataSocketProxyIpInfosFromRom();
        }
        
        QRomIplistData iplistData = null;
        int proxyIndex = ApnStatInfo.getCurApnProxyIndex();
        if (proxyIndex == ApnStatInfo.PROXY_LIST_WIFI) {  // wifi下优先获取对应bssid缓存
            iplistData = mWupInfoCache.getWupSocketIpListDataByBssid(ApnStatInfo.getWifiBSSID(getContext()));
        }
        if (iplistData == null || iplistData.isEmpty()) {  // 对应bssid下未找到缓存ip，则使用上次拉取的默认wifi索引下的iplist
            iplistData = mWupInfoCache.getWupSocketIpListDataByType(proxyIndex);
        }

        return iplistData;
    }
    
    @Override
    public boolean refreshInfos() {
        QWupLog.w(TAG, "refreshInfos");
        updataAllWupFromRom();
        return true;
    }

    @Override
    public boolean onConnectivityChanged(int newApn, int oldApn) {
        super.onConnectivityChanged(newApn, oldApn);
        if (newApn == oldApn || !ApnStatInfo.isNetConnected()) {  // 接入点未变
            QWupLog.trace(TAG, "onConnectivityChanged -> apn not changed or, net  is not ok");
            return false;
        }
        long netChangedSubTime = System.currentTimeMillis() - mLastNetChangedTime;
        if (netChangedSubTime < QWupSdkConstants.MILLIS_FOR_MINUTE) {
            QWupLog.trace(TAG, "onConnectivityChanged -> net change time is not ok");
            return false;
        }
        mLastNetChangedTime = System.currentTimeMillis();
        updataProxyIpInfosFromRom();
        updataSocketProxyIpInfosFromRom();
        return true;
    }
    
//    @Override
//    public long getRomId() {
//        Context context = QRomWupImplEngine.getInstance().getContext();
//        if (QRomWupBuildInfo.isQRomSys(context)) {  // 在rom源码集成模式, 通过系统binder获取相关信息
//            byte[] rsp = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_ROMID);
//            if (rsp == null || rsp.length == 0) {
//                QWupLog.trace(TAG, "getRomId -> getDatasFromRomFramwork, romid = 0");            
//                return 0;
//            }
//            long romId = Long.parseLong(new String(rsp));
//            QWupLog.trace(TAG, "getRomId -> getDatasFromRomFramwork, romid = " + romId);            
//            return romId;
//        }
//        return super.getRomId();
//    }
    
    /**
     * 统计rom host app主动更新iplist
     * @return
     */
    protected boolean notifyRomHostUpdataIplist() {
        
        Context context = QRomWupImplEngine.getInstance().getContext();
        Cursor cursor = null;
        try {
            Uri uri = QWupUriFactory.getDoSpeOperUri();
            QWupLog.trace(TAG, "notifyRomHostUpdataIplist-> uri : " + uri);
            cursor = context.getContentResolver().query(uri, null, 
                    WUP_ROM_PROVIDER_OPER_INFO.OPER_UPDATE_IPLIST_FORCE, 
                    new String[]{String.valueOf(WUP_OPER_TYPE.OPER_UPDATE_IPLIST2ROM)}, null);
            if (cursor == null || cursor.getCount() == 0) {
                QWupLog.w(TAG, "notifyRomHostUpdataIplist-> rsp cursor is empty ");
                return false;
            }

            if (cursor.moveToFirst()) {
                int rspCode = cursor.getInt(cursor.getColumnIndex(
                        QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_OPERRSP));
                QWupLog.trace(TAG, "notifyRomHostUpdataIplist-> rspCode : " + rspCode);
            } else {
                QWupLog.trace(TAG, "notifyRomHostUpdataIplist-> cursor.moveToFirst failed!");
            }
            return true;
        } catch (Throwable e) {
            QWupLog.w(TAG, "notifyRomHostUpdataIplist-> e: " + e + ", err msg: " + e.getMessage());
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    QWupLog.w(TAG, e);
                }
            }
        
        }
        return false;
    }
    
    /**
     * 从rom获取wup信息
     * @param dataType
     */
    protected void updataAllWupFromRom() {
    	
    	// 仅更新guid
    	updataGuidFromRom();
    	
    	// 仅更新 wup代理地址
    	updataProxyIpInfosFromRom();
    	
    	// 仅更新 wup socket代理地址
    	updataSocketProxyIpInfosFromRom();
    }
    
    /**
     * 更新rom guid信息
     */
    protected void updataGuidFromRom() {
        QWupLog.trace(TAG, "updataGuidFromRom");
        byte[] guidBytes = null;
        Context context = QRomWupImplEngine.getInstance().getContext();	
        // 找rom app获取rom guid           
        guidBytes = getGuidFromCursor(context);
        refreshGuidInfo(guidBytes);
    }
    
    protected void refreshGuidInfo(byte[] guid) {
        String guidStr = QWupStringUtil.byteToHexString(guid);
        QWupLog.i(TAG, "refreshGuidInfo -> guid: " + guidStr);
        if (QRomWupInfo.isGuidValidate(guidStr)) {
            mWupInfoCache.setSGUID(guid, getContext());
        }
    }
    
    public byte[] getGuidFromCursor(Context context) {
        
        String guidStr = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    QWupUriFactory.getRomGuidUri(), null, null, null, null);
            QWupLog.trace(TAG, "updataGuidFromRom : uri = " + QWupUriFactory.getRomGuidUri());

            if (cursor == null || !cursor.moveToFirst()) {
                QWupLog.trace(TAG, "updataGuidFromRom : check uri or data , cursor = " + cursor);
                return null;
            }
            
            guidStr = cursor.getString(cursor.getColumnIndex(
                    QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_GUID));
        } catch (Throwable e) {
            QWupLog.w(TAG, e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    QWupLog.w(TAG, e);
                }
            }
        }
        QWupLog.i(TAG, "sendGetRomWupDataMsg -> guid = " + guidStr);
        if (!QRomWupInfo.isGuidValidate(guidStr)) {  // guid 不合法
            return null;
        }
        
        byte[] guidBytes = QWupStringUtil.hexStringToByte(guidStr);
        return guidBytes;
    }
    
    /**
     * 更新WUP 代理地址
     */
    protected void updataProxyIpInfosFromRom() {
        // 更新默认接入点iplist信息
        updataDefaultProxyIpInfosFromRom();
        // 更新wifi下bssid对应的iplist信息
        updateWifiIpListsFromRom(mWupInfoCache.getAllWupWifiProxyInfos(),
                WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI, 
                QRomWupInfo.TYPE_PROXY_WIFI_IPLIST_DATA);
    }
    
    /**
     * 更新默认WUP 代理地址（apnType缓存）
     */
    protected void updataDefaultProxyIpInfosFromRom() {
        QWupLog.trace(TAG, "updataDefaultProxyIpInfosFromRom");
    	// 是rom apk集成模式
    	Cursor cursor = null;
    	try {
            cursor = getContext().getContentResolver().query(
            		QWupUriFactory.getRomProxyIpListUri(), null, null, null, null);
//            QWupLog.trace(TAG, "updataDefaultProxyIpInfosFromRom : uri = " + QWupUriFactory.getRomProxyIpListUri());
//            if (cursor == null && QTcmWupOldUriCompatible.isTcmRomMode()) {  // TCM app 是rom 模式
//                QWupLog.trace(TAG, "updataDefaultProxyIpInfosFromRom : old uri ");
//                cursor = getContext().getContentResolver().query(
//                        QTcmWupOldUriCompatible.getOldTcmRomProxyIpListUri(), null, null, null, null);
//            }
            
            if (cursor == null || cursor.getCount() == 0) {
                QWupLog.trace(TAG, "updataDefaultProxyIpInfosFromRom : cursor empty ");
            	return;
            }
            
            int colCnt = cursor.getColumnCount();
            QWupLog.trace(TAG, "updataDefaultProxyIpInfosFromRom-> colCnt = " + colCnt);
            QRomIplistData iplistData = null;
            String ipItem = null;
            List<String> ipList = null;
            int apnType = -1;
            long updateTime = 0;
            String clientIp = null;
            // 清空缓存
            mWupInfoCache.clearWupProxyInfo();
            while (cursor.moveToNext()) {
            	ipItem = cursor.getString(cursor.getColumnIndex(
            			WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_IPLIST));    	
            	apnType = cursor.getInt(cursor.getColumnIndex(
            			WUP_ROM_PROVIDER_COLUMN.COLUMN_NET_APN_TYPE));    	
            	
            	if (QWupStringUtil.isEmpty(ipItem)) {
            		continue;
            	}
            	
            	iplistData = mWupInfoCache.getProxyIpListDataByType(apnType);
            	if (iplistData == null) {  // 生成一个新的iplist信息            	    
            	    ipList = new ArrayList<String>(5);
            	    if (colCnt > 2) {  // 新版本
            	        updateTime = cursor.getLong(cursor.getColumnIndex(
            	                WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME));      
            	        clientIp = cursor.getString(cursor.getColumnIndex(
            	                WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP));      
            	    } else {  // 老版本无更新时间及client ip
            	        updateTime = 0;
            	        clientIp = "";
            	    }
            	    // 读取新的数据
            	    iplistData = new QRomIplistData(apnType, ipList, 
            	            QRomWupInfo.TYPE_PROXY_IPLIST_DATA, updateTime, clientIp);
            	    mWupInfoCache.updateProxyIpListDataByType(apnType, iplistData);
            	} else {
            	    ipList = iplistData.getIplistInfo();
            	}
            	
            	ipList.add(ipItem);
            }  // ~end while wup proxylist
            showIplistLog(mWupInfoCache.getAllWupProxyIplistDatas());
            QWupLog.trace(TAG, "updataDefaultProxyIpInfosFromRom-> finish ");
        } catch (Exception e) {
            QWupLog.w(TAG, e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    QWupLog.w(TAG, e);
                }
            }
        }
    }
    


    /**
     * 获取wifi下对应bssid的缓存地址
     * @param caches       缓存信息 （caches == null，则不进行更新操作）
     * @param cacheType 缓存类型 （WUP_ROM_IPLIT_TYPE，普通wifi下iplist/socket下wif ilist）
     */
    protected void updateWifiIpListsFromRom(Map<String, QRomIplistData> caches, int reqDataType, int cacheDataType) {
        
        if (caches == null) {
            QWupLog.trace(TAG, "updateWifiIpListsFromRom->caches is null,  reqDataType = " + reqDataType);
            return;
        }
        QWupLog.trace(TAG, "updateWifiIpListsFromRom-> reqDataType = " + reqDataType);
        // 是rom apk集成模式
        Cursor cursor = null;
        try {
            
            // 查询wifi下对应iplist缓存
            cursor = getContext().getContentResolver().query(
                    QWupUriFactory.getRomWifiIpListUri(), null, M_WIFI_SELECTION, 
                    new String[]{String.valueOf(reqDataType)}, null);
            
            QWupLog.trace(TAG, "updateWifiIpListsFromRom : uri = " + QWupUriFactory.getRomWifiIpListUri());
            
            if (cursor == null || cursor.getCount() == 0) {
                QWupLog.w(TAG, "updateWifiIpListsFromRom :  cursor is empty");
                return;
            }
            QRomIplistData iplistData = null;
            String ipItem = null;
            List<String> ipList = null;
            String apnType = null;
            long updateTime = 0;
            String clientIp = null;
            // 清空缓存
            caches.clear();
            while (cursor.moveToNext()) {
                ipItem = cursor.getString(cursor.getColumnIndex(
                        WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_IPLIST));      
                apnType = cursor.getString(cursor.getColumnIndex(
                        WUP_ROM_PROVIDER_COLUMN.COLUMN_NET_APN_TYPE));      
                updateTime = cursor.getLong(cursor.getColumnIndex(
                        WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME));      
                clientIp = cursor.getString(cursor.getColumnIndex(
                        WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP));      
                if (QWupStringUtil.isEmpty(ipItem) || QWupStringUtil.isEmpty(apnType)) {
                    continue;
                }
                
                iplistData = caches.get(apnType);
                if (iplistData == null) {  // 生成一个新的iplist信息                    
                    ipList = new ArrayList<String>(5);                    
                    // 读取新的数据
                    iplistData = new QRomIplistData(apnType, ipList, cacheDataType, updateTime, clientIp);
                    caches.put(apnType, iplistData);
                } else {
                    ipList = iplistData.getIplistInfo();
                }
                
                ipList.add(ipItem);
            }  // ~end while wup proxylist
            showIplistLog(caches);
            QWupLog.i(TAG, "updateWifiIpListsFromRom-> finsih~ reqDataType= " + reqDataType);
        } catch (Exception e) {
            QWupLog.w(TAG, e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    QWupLog.w(TAG, e);
                }
            }
        }
    }
    
    /**
     * 更新WUP socket代理地址
     */
    protected void updataSocketProxyIpInfosFromRom() {
        updataDefaultSocketProxyIpInfosFromRom();
        // 更新wifi下bssid对应的socket iplist信息
        updateWifiIpListsFromRom(mWupInfoCache.getAllWupWifiSocketProxyInfos(),
                WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI,
                QRomWupInfo.TYPE_WUP_SOCKET_WIFI_IPLIST_DATA);
    }
    
    /**
     * 更新WUP socket默认接入点代理地址
     */
    protected void updataDefaultSocketProxyIpInfosFromRom() {
        QWupLog.trace(TAG, "updataDefaultSocketProxyIpInfosFromRom");
    	Context context = QRomWupImplEngine.getInstance().getContext();
    	
        // 非rom framework 源码集成模式：rom apk集成模式
    	Cursor cursor = null;
    	try {
            cursor = context.getContentResolver().query(
                    QWupUriFactory.getRomProxySocketIpListUri(), null, null, null, null);
            QWupLog.trace(TAG, "updataDefaultSocketProxyIpInfosFromRom : uri = " + QWupUriFactory.getRomProxySocketIpListUri());
//            if (cursor == null && QTcmWupOldUriCompatible.isTcmRomMode()) {  // TCM app 是rom 模式
//                QWupLog.trace(TAG, "updataDefaultSocketProxyIpInfosFromRom : old uri ");
//
//                cursor = context.getContentResolver().query(
//                        QTcmWupOldUriCompatible.getOldTcmRomProxySocketIpListUri(), null, null, null, null);
//            }
            
            if (cursor == null || cursor.getCount() == 0) {
                QWupLog.trace(TAG, "updataDefaultSocketProxyIpInfosFromRom : curor is empty ");
            	return;
            }
            int colCnt = cursor.getColumnCount();
            QWupLog.trace(TAG, "updataDefaultSocketProxyIpInfosFromRom -> colCnt = " + colCnt);
            QRomIplistData iplistData = null;
            String ipItem = null;
            List<String> ipList = null;
            int apnType = -1;
            long updateTime = 0;
            String clientIp = null;
            mWupInfoCache.clearWupSocketProxyInfo();
            while (cursor.moveToNext()) {
            	ipItem = cursor.getString(cursor.getColumnIndex(
            			QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_SOCKET_IPLIST));    	
            	apnType = cursor.getInt(cursor.getColumnIndex(
            			QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_NET_APN_TYPE));    	
            	
            	if (QWupStringUtil.isEmpty(ipItem)) {
            		continue;
            	}
            	
            	iplistData = mWupInfoCache.getWupSocketIpListDataByType(apnType);
                if (iplistData == null) {  // 生成一个新的iplist信息                    
                    ipList = new ArrayList<String>(5);
                    if (colCnt > 2) {  // 新版本
                        updateTime = cursor.getLong(cursor.getColumnIndex(
                                WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME));      
                        clientIp = cursor.getString(cursor.getColumnIndex(
                                WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP));      
                    } else {  // 老版本无更新时间及client ip
                        updateTime = 0;
                        clientIp = "";
                    }
                    // 读取新的数据
                    iplistData = new QRomIplistData(apnType, ipList, 
                            QRomWupInfo.TYPE_WUP_SOCKET_IPLIST_DATA, updateTime, clientIp);
                    mWupInfoCache.updateWupSocketIpListDataByType(apnType, iplistData);
                } else {
                    ipList = iplistData.getIplistInfo();
                }
   
            	ipList.add(ipItem);
            } // ~end while
            showIplistLog(mWupInfoCache.getAllWupSocketProxyIplistDatas());
            QWupLog.trace(TAG, "updataDefaultSocketProxyIpInfosFromRom-> finish ");
        } catch (Exception e) {
            QWupLog.w(TAG, e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    QWupLog.w(TAG, e);
                }
            }
        }    	
    }
    
    protected void showIplistLog(SparseArray<QRomIplistData> datas) {
        if (datas == null || datas.size() == 0) {
            QWupLog.w(TAG, "showIplistLog-> SparseArray datas is empty");
            return;
        }
        int key = -1;
        for (int i = 0; i <datas.size(); i++) {
            key = datas.keyAt(i);
            QWupLog.trace(TAG, "app2rom iplistType: " + key + ", " + datas.get(key));
        }
    }
    
    protected void showIplistLog(Map<String, QRomIplistData> datas) {
        if (datas == null || datas.isEmpty()) {
            QWupLog.w(TAG, "showIplistLog-> Map datas is empty");
            return;
        }
        for (Entry<String, QRomIplistData> entry : datas.entrySet()) {
            QWupLog.trace(TAG, "app2rom: iplistType: " + entry.getKey() + ", " + entry.getValue());
        }
    }

    @Override
    public void release() {
        Context context = getContext();
        if (mReceiver != null && context != null) {            
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.release();
    }
    
    protected boolean onReceiveBroadMsg(Intent intent) {
        return false;
    }
    
    class WupRunTimeReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int reqType = -1;
                String action = intent.getAction();
                if (QWupSdkConstants.ACTION_WUP_SDK_BASEDATA_UPDATED.equals(action)) { // 更新iplist
                    reqType = intent.getIntExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, -1);
                    QWupLog.i(TAG, "WupRunTimeReciver-> ACTION_WUP_SDK_BASEDATA_UPDATED reqType = " + reqType);
                    updataAllWupFromRom();
                } else {
                    onReceiveBroadMsg(intent);
                }
            } catch (Exception e) {
                QWupLog.w(TAG, "WupRunTimeReciver: app2rom", e);
            }
            
        }
    }
    
}
