package qrom.component.wup.runInfo.processer;

import java.util.Map;

import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.aidl.IQRomWupService;
import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.sysImpl.QRomWupSerializUtils;
import qrom.component.wup.sysImpl.QWupRomSysProxyerImpl;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.WUP_OPER_TYPE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomIplistData;
import qrom.component.wup.wupData.QRomWupInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;


/**
 * app 在源码framework集成的rom中
 * @author sukeyli
 *
 */
public class QApp2RomSysInfoProcesser extends QApp2RomInfoProcesser {

    private String M_ROMSYS_IPLIST_ACTION = null;
    /** 处理rom sys返回的数据信息 */
    private final int MSG_WUP_ROMSYS_BROAD_RSP_WUPDATE = 501;
    public QApp2RomSysInfoProcesser() {
        TAG = "QApp2RomSysInfoProcesser";
    }
    
    
    @Override
    protected void addActionFilter(IntentFilter filter) {
        M_ROMSYS_IPLIST_ACTION = QRomWupBuildInfo.getAppPackageName() + QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_IPDATAS;
        // 注册找rom要相关数据监听信息
        filter.addAction(QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_RSPDATAS);
        filter.addAction(M_ROMSYS_IPLIST_ACTION);
    }
    
    /**
     * 更新rom guid信息
     */
    protected void updataGuidFromRom() {
        byte[] guidBytes = null;
        QWupLog.trace(TAG, "updataGuidFromRom -> is rom src framework");     
        // 在rom源码集成模式, 通过系统binder获取相关信息
        guidBytes = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_GUID);
    
        refreshGuidInfo(guidBytes);
    }
    
    /**
     * 更新WUP 代理地址
     */
    protected void updataProxyIpInfosFromRom() {
        QWupLog.trace(TAG, "updataProxyIpInfosFromRom");

        SparseArray<QRomIplistData> proxyIpLists = null;
        // 获取新接口iplist数据
        byte[] datas = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW);
        if (datas != null && datas.length > 0) {  // 新版本接口
            proxyIpLists = QRomWupSerializUtils.deSerializeQRomIplistDatas(datas);
        } else {  // 老版本接口
            proxyIpLists = QRomWupSerializUtils.parasBytes2Map(
                    getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_IPLIST),
                    QRomWupInfo.TYPE_PROXY_IPLIST_DATA);
        }
        
        // 获取wup wifi下接入点信息
        byte[] wifiDatas = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI);        
        Map<String, QRomIplistData> wifiProxyLists = 
                QRomWupSerializUtils.deSerializeQRomIplistDatasForWifi(wifiDatas);
        
        // 更新wup proxy 信息
        mWupInfoCache.updateAllProxyCache(proxyIpLists);
        // 更新wifi下wup proxy信息
        mWupInfoCache.updateAllWifiProxyCache(wifiProxyLists);
        
        showIplistLog(proxyIpLists);
        showIplistLog(wifiProxyLists);
    }
    
    /**
     * 更新WUP socket代理地址
     */
    protected void updataSocketProxyIpInfosFromRom() {
        QWupLog.trace(TAG, "updataSocketProxyIpInfosFromRom");
   
        SparseArray<QRomIplistData> proxyIpLists = null;
        // 获取新接口iplist数据
        byte[] datas = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_NEW);
        if (datas != null && datas.length > 0) {  // 新版本接口
            proxyIpLists = QRomWupSerializUtils.deSerializeQRomIplistDatas(datas);
        } else {  // 老版本接口
            proxyIpLists = QRomWupSerializUtils.parasBytes2Map(
                    getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST),
                    QRomWupInfo.TYPE_WUP_SOCKET_IPLIST_DATA);
        }
        
        // 获取wup wifi下接入点信息
        byte[] wifiDatas = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI);        
        Map<String, QRomIplistData> wifiProxyLists = 
                QRomWupSerializUtils.deSerializeQRomIplistDatasForWifi(wifiDatas);
        
        // 更新wup proxy 信息
        mWupInfoCache.updateAllSocketProxyCache(proxyIpLists);
        // 更新wifi下wup proxy信息
        mWupInfoCache.updateAllWifiSocketProxyCache(wifiProxyLists);
        showIplistLog(proxyIpLists);
        showIplistLog(wifiProxyLists);
    }
    
    
    
    @Override
    public long getRomId() {
        byte[] rsp = getDatasFromRomFramwork(WUP_DATA_TYPE.WUP_DATA_ROMID);
        if (rsp == null || rsp.length == 0) {
            QWupLog.trace(TAG, "getRomId -> getDatasFromRomFramwork, romid = 0");            
            return 0;
        }
        long romId = 0;
        try {
            romId = Long.parseLong(new String(rsp));
        } catch (Exception e) {
            QWupLog.trace(TAG, "getRomId -> err msg = " + e.getMessage());
        }
        QWupLog.trace(TAG, "getRomId -> getDatasFromRomFramwork, romid = " + romId);            
        return romId;
    }
    
    @Override
    protected boolean notifyRomHostUpdataIplist() {
        
        // 通过aidl通知rom更新iplist
        byte[] rsp = getDatasFromRomFramwork(WUP_OPER_TYPE.OPER_UPDATE_IPLIST2ROM);
        if (rsp == null || rsp.length == 0) {
            QWupLog.w(TAG, "notifyRomHostUpdataIplist-> rsp data is empty!");
            return false;
        }
        try {
            String rspCode = new String(rsp);
            QWupLog.trace(TAG, "notifyRomHostUpdataIplist-> rsp = " + rspCode);
        } catch (Exception e) {
            QWupLog.w(TAG, "notifyRomHostUpdataIplist-> paras rspData error!");
        }
        return true;
    }
    
    /**
     * 从系统framewok层获取数据
     * @param type
     * @return
     */
    private byte[] getDatasFromRomFramwork(int type) {
        byte[] datas = null;
        Context context = QRomWupImplEngine.getInstance().getContext();
        if (QRomWupBuildInfo.isQRomSys(context)) {  // 在rom源码集成模式, 通过系统binder获取相关信息
            IQRomWupService romWupService = QWupRomSysProxyerImpl.getInstance().getQRomBinderServiceForSdk(context);
            if (romWupService != null) {                
                try {
                    datas = romWupService.getWupDataByType(type);
                    QWupLog.i(TAG, "getDatasFromRomFramwork-> datas len = " + (datas == null ? -1 : datas.length));
                } catch (Throwable e) {
                    QWupLog.trace(TAG, "getDatasFromRomFramwork -> reqType = " + type +
                    		"err: " + e + ", err msg: " + e.getMessage());
                    QWupLog.reportBinderUseErr("getData->" + e + " : " + e.getMessage());
                }
            } else {                
                QWupLog.trace(TAG, "getDatasFromRomFramwork -> romWupService is null");
                QWupLog.reportBinderNull();
            }
            
            if (datas == null) {  // 未获取到数据, 使用备用方案，发送请求
                QWupLog.i(TAG, "getDatasFromRomFramwork -> send ACTION_WUP_SDK_ROMSYS_REQDATAS");
                Intent intent = new Intent(QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_REQDATAS);
                intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, type);
                intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_PKG, 
                        QRomWupBuildInfo.getAppPackageName());
                context.sendBroadcast(intent);
            }
        } else {
            QWupLog.trace(TAG, "getDatasFromRomFramwork not rom src framwoke mode, type = " + type);
        }
        return datas;
    }
    
    /**
     * 更新iplistInfo
     * @param iplistDatas
     * @param wifiIpListDatas
     */
    private void refreshProxyIplistInfo(byte[] iplistDatas, byte[] wifiIpListDatas) {
        
        SparseArray<QRomIplistData> proxyIpLists = null;
        Map<String, QRomIplistData> wifiProxyLists = null;

        proxyIpLists = QRomWupSerializUtils.deSerializeQRomIplistDatas(iplistDatas);

        wifiProxyLists = QRomWupSerializUtils.deSerializeQRomIplistDatasForWifi(wifiIpListDatas);
       
        // 更新wup proxy 信息
        mWupInfoCache.updateAllProxyCache(proxyIpLists);
        // 更新wifi下wup proxy信息
        mWupInfoCache.updateAllWifiProxyCache(wifiProxyLists);
        
        showIplistLog(proxyIpLists);
        showIplistLog(wifiProxyLists);
    }
    
    
    /**
     * 更新iplistInfo
     * @param iplistDatas
     * @param wifiIpListDatas
     */
    private void refreshSocketIplistInfo(byte[] iplistDatas, byte[] wifiIpListDatas) {
        
        SparseArray<QRomIplistData> proxyIpLists = null;
        Map<String, QRomIplistData> wifiProxyLists = null;

        proxyIpLists = QRomWupSerializUtils.deSerializeQRomIplistDatas(iplistDatas);        
     
        wifiProxyLists = QRomWupSerializUtils.deSerializeQRomIplistDatasForWifi(wifiIpListDatas);
       
        // 更新wup proxy 信息
        mWupInfoCache.updateAllSocketProxyCache(proxyIpLists);
        // 更新wifi下wup proxy信息
        mWupInfoCache.updateAllWifiSocketProxyCache(wifiProxyLists);
        
        showIplistLog(proxyIpLists);
        showIplistLog(wifiProxyLists);
    }
    
    /**
     * 处理rom sys返回的wup数据
     * @param reqType
     * @param pkg
     * @param datas
     * @param extra
     * @return
     */
    private boolean onprocessRomSysRspData(int reqType, String pkg, byte[] datas, byte[] extra) {
        QWupLog.d(TAG, "onprocessRomSysRspData-> pkgName = " + pkg
                +", reqType = " + reqType +  ", datasLen = " + (datas == null ? -1 : datas.length)
                + ", extralen = " + (extra == null ? -1 : extra.length));
        boolean rsp = true;
        switch (reqType) {
        case WUP_DATA_TYPE.WUP_DATA_GUID:  // guid  
            refreshGuidInfo(datas);
            break;
        case WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW: // iplist
            refreshProxyIplistInfo(datas, extra);                
            break;
        case WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI: // wifi iplist
            refreshProxyIplistInfo(null, extra);
            break;
            
        case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_NEW: // socket
            refreshSocketIplistInfo(datas, extra);
            break;
        case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI:  // wifi socket
            refreshSocketIplistInfo(null, extra);
            break;

        default:
            rsp = false;
            break;
        }
        return rsp;
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        
        int what = msg.what;
        switch (what) {
        case MSG_WUP_ROMSYS_BROAD_RSP_WUPDATE:
            Bundle bundle = (Bundle) msg.obj;
            int reqType = bundle.getInt(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE);
            byte[] datas = bundle.getByteArray(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_DATAS);
            byte[] extra = bundle.getByteArray(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_EXTRA);
            String pkg = bundle.getString(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_PKG);
            // 处理相关信息
            onprocessRomSysRspData(reqType, pkg, datas, extra);
            return true;

        default:
            break;
        }
        
        return super.handleMessage(msg);
    }
    
    @Override
    protected boolean onReceiveBroadMsg(Intent intent) {
        String action = intent.getAction();
        if (QWupStringUtil.isEmpty(action)) {
            QWupLog.w(TAG, "onReceiveBroadMsg-> action is empty");
            return false;
        }
        int reqType = -1;
        byte[] datas = null;
        byte[] extra = null;
        if (QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_RSPDATAS.equals(action)
                || action.equals(M_ROMSYS_IPLIST_ACTION)) {
            reqType = intent.getIntExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, -1);
            // 请求包名
            String pkgName = intent.getStringExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_PKG);
            // 数据
            datas = intent.getByteArrayExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_DATAS);
            extra = intent.getByteArrayExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_EXTRA);
            
            QWupLog.d(TAG, "onReceiveBroadMsg-> ACTION_WUP_SDK_ROMSYS_RSPDATAS: pkg = " + pkgName
                    +", reqType = " + reqType +  ", datasLen = " + (datas == null ? -1 : datas.length));
            // 转换数据，数据解析放到子线程处理
            Bundle bundle = new Bundle();
            bundle.putInt(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, reqType);
            bundle.putByteArray(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_DATAS, datas);
            bundle.putByteArray(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_EXTRA, extra);
            bundle.putString(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_PKG, pkgName);
            
            sendMsg(MSG_WUP_ROMSYS_BROAD_RSP_WUPDATE, reqType, bundle, 0);
        }
        
        return super.onReceiveBroadMsg(intent);
    }
}
