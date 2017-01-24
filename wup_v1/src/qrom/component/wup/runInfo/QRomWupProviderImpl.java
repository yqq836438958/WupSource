package qrom.component.wup.runInfo;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import qrom.component.wup.QRomWupConstants.IPLIST_RSP_CODE;
import qrom.component.wup.QRomWupConstants.LOGIN_RSP_CODE;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.QRomWupConstants.WUP_START_MODE;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_REQ_TYPE;
import qrom.component.wup.utils.QWupSdkConstants.WUP_OPER_TYPE;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomIplistData;
import qrom.component.wup.wupData.QRomWupInfo;
import android.content.Context;
import android.database.MatrixCursor;
import android.util.SparseArray;


public class QRomWupProviderImpl {
    
    private String TAG = "QRomWupProviderImpl";

	public static class WUP_ROM_PROVIDER_COLUMN {
		/** guid */
		public static final String COLUMN_GUID = "guid";
		/** 网络接入点 */
		public static final String COLUMN_NET_APN_TYPE = "net_apn_type";
		/** wup代理地址前缀 -- 后面跟对应的apn类型 */
		public static final String COLUMN_PROXY_IPLIST = "proxy_iplist";
		/** wup socket 代理地址前缀-- 后面跟对应的apn类型 */
		public static final String COLUMN_PROXY_SOCKET_IPLIST = "socket_iplist";
		/** wup socket 代理地址前缀-- 后面跟对应的apn类型 */
		public static final String COLUMN_QROM_ID = "qrom_id";
		
		/** iplist 对应缓存时间 */
		public static final String COLUMN_IPLIST_UPDATE_TIME = "iplist_update_time";
		/** iplist 对应 客户端ip */
		public static final String COLUMN_IPLIST_CLIENTIP = "iplist_clientip";
		/** wif iplist 类型（如: 普通wup代理，wupsocket代理） */
		public static final String COLUMN_IPLIST_TYPE = "iplist_type";
		/** 执行对应操作的返回码 */
		public static final String COLUMN_OPERRSP = "oper_rsp_code";
	}
	
	
	public static class WUP_ROM_PROVIDER_OPER_INFO {
	    /** 强制更新 iplist 信息*/
	    public static final String OPER_UPDATE_IPLIST_FORCE = "update_iplist_force";
	    /** 检测请求guid */
	    public static final String OPER_REQUEST_GUID_CHECK = "request_guid_check";
	}
	
	public static QRomWupProviderImpl mInstance;
	
	private Context mContext;
	
	public static QRomWupProviderImpl getInstance() {
		if (mInstance == null) {
			mInstance = new QRomWupProviderImpl();
		}
		return mInstance;
	}
	

	public MatrixCursor getGuid() {
	    
		String guidStr = QWupStringUtil.byteToHexString(
				QRomWupImplEngine.getInstance().getWupRunTimeManager().getGuidBytes());
		
		if (!QRomWupInfo.isGuidValidate(guidStr)) {  // guid不合法
			return null;
		}
		
		MatrixCursor cursor = null; 
		cursor = new MatrixCursor(new String[]{WUP_ROM_PROVIDER_COLUMN.COLUMN_GUID});
		
		cursor.addRow(new String[]{guidStr});		
		return cursor;
	}
	
	/**
	 * 获取同步的host rom guid
	 * @return
	 */
	public MatrixCursor getSynHostGuid(){

	    // 获取之前host模式的rom guid
        QRomWupInfo romWupInfo = new QRomWupInfo(
                QWupFileUtil.FILE_USER_WUP_INFO_ROM, QWupFileUtil.SD_USER_WUP_INFO_ROM);
        romWupInfo.load(getAppContext());
        String guidStr = QWupStringUtil.byteToHexString(romWupInfo.getGuidBytes());
        
        if (!QRomWupInfo.isGuidValidate(guidStr)) {  // guid不合法
            return null;
        }
        
        MatrixCursor cursor = null; 
        cursor = new MatrixCursor(new String[]{QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_GUID});
        
        cursor.addRow(new String[]{guidStr});       
        return cursor;
	}
	
	
	/**
	 * 获取所有的 wup 代理地址
	 * @return
	 */
	public MatrixCursor getProxyIpInfos() {
	    QWupLog.trace(TAG, "getProxyIpInfos");
		SparseArray<QRomIplistData> ipInfos = QRomWupImplEngine.getInstance()
				.getWupRunTimeManager().getAllWupProxyIplistDatas();
		
		return getIpInfosCursor(
				QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_IPLIST, ipInfos); 
	}
	
	/**
	 * 获取所有wup socket代理地址
	 * @return
	 */
	public MatrixCursor getSocketProxyIpInfos() {
	    QWupLog.trace(TAG, "getSocketProxyIpInfos");
		SparseArray<QRomIplistData> ipInfos = QRomWupImplEngine.getInstance()
				.getWupRunTimeManager().getAllWupSocketProxyIplistDatas();
		
		return getIpInfosCursor(
				QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_SOCKET_IPLIST, ipInfos); 
	}
	
	public MatrixCursor getIplistfos(String selection, String[] selectionArgs) {
	    
	    if (QWupStringUtil.isEmpty(selection) || selectionArgs == null || selectionArgs.length == 0) {  //参数错误
	        QWupLog.trace(TAG, "getIplistfos -> req param err, cancel");
	        return null;
	    }
	    
	    if (!selection.contains(WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_TYPE)) { // 未指定查询类型
	        QWupLog.trace(TAG, "getIplistfos -> donot get req type, cancel");
	        return null;
	    }
	    int reqListType = -1;
	    try {
            reqListType = Integer.valueOf(selectionArgs[0]);
        } catch (Exception e) {
           QWupLog.w(TAG, "getIplistfos-> errmsg, req param parse fails: " + selectionArgs[0]);
        }
	    Map<String, QRomIplistData> iplistInfos = null;
	    QWupLog.trace(TAG, "getIplistfos ->reqListType = " + reqListType);
	    switch (reqListType) {
        case WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI:  // wifi下wup代理地址
            iplistInfos = QRomWupImplEngine.getInstance()
                    .getWupRunTimeManager().getAllWupWifiProxyIplistDatas();
            break;
        case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI:  // // wifi下wup socket代理地址
            iplistInfos = QRomWupImplEngine.getInstance()
                    .getWupRunTimeManager().getAllWupWifiSocketIplistDatas();
            break;

        default:
            QWupLog.trace(TAG, "getIplistfos ->donot match reqListType = " + reqListType);
            break;
        }
	    // 转换iplist数据
	    
	    return getIpListInfosCursor(iplistInfos);
	}
	
	public MatrixCursor getRomId() {
	    QWupLog.trace(TAG, "getRomId");
	    long romId = QRomWupImplEngine.getInstance().getWupRunTimeManager().getRomIdFromTSF();
	    String[] columnsNames = new String[] {QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_QROM_ID};
	    MatrixCursor cursor = new MatrixCursor(columnsNames);      
	    cursor.addRow(new String[]{String.valueOf(romId)});
	    return cursor;
	}
	
	public void setRomId(long romId) {
	    QRomWupImplEngine.getInstance().setRomId(romId);
	}
	
	/**
	 * 将制定类型的代理地址数据转换成cursor
	 * @param columnName   ip信息的字段名 （参考WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_SOCKET_IPLIST）
	 * @param ipInfos
	 * @return
	 */
	private MatrixCursor getIpInfosCursor(String columnName, SparseArray<QRomIplistData> ipInfos) {
		
		if (ipInfos == null || ipInfos.size() == 0) {
		    QWupLog.trace(TAG, "getIpInfosCursor-> ipinfos is empty");
			return null;
		}
		
		int n = ipInfos.size();
		int apnType = -1;
		List<String> list = null;
		// 多一个字段用户存放key信息 (APN类型，ip信息， 缓存时间，client ip)
		String[] columnsNames = new String[] {
				QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_NET_APN_TYPE, 
				columnName, 
				WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME,
				WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP};
		MatrixCursor cursor = new MatrixCursor(columnsNames);		
		int listCnt = 0;
		String listItem = null;;
		QRomIplistData iplistData = null;
		String updateTime = null;
		String clientIp = null;
		for (int i = 0; i < n; i++) {  // 开始循环所有的iplist数据
			apnType = ipInfos.keyAt(i);
			iplistData = ipInfos.get(apnType);
			if (iplistData == null || iplistData.isEmpty()) {
			    continue;
			}
			list = iplistData.getIplistInfo();
			listCnt = list.size();
			clientIp = iplistData.getClientIp();
			updateTime = String.valueOf(iplistData.getIplistUpdateTime());
			// 添加key后面所有的item
			for (int j = 0; j < listCnt; j++) {
				listItem = list.get(j);
				if (QWupStringUtil.isEmpty(listItem)) {
					continue;
				}
				cursor.addRow(new String[]{String.valueOf(apnType), listItem, updateTime, clientIp});
			}  // ~ 一个key的list 拼装完成
			QWupLog.trace(TAG, "getIpInfosCursor-> " + iplistData);
		}
		return cursor;
	}
	/**
	 * 将制定类型的代理地址数据转换成cursor
	 * @param ipInfos
	 * @return
	 */
	private MatrixCursor getIpListInfosCursor(Map<String, QRomIplistData> ipInfos) {
	    
	    if (ipInfos == null || ipInfos.isEmpty()) {
	        QWupLog.trace(TAG, "getIpListInfosCursor-> ipInfos is empty");
	        return null;
	    }
	    
	    List<String> list = null;
	    // 多一个字段用户存放key信息 (APN类型，ip信息， 缓存时间，client ip)
	    String[] columnsNames = new String[] {
	            WUP_ROM_PROVIDER_COLUMN.COLUMN_NET_APN_TYPE, 
	            WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_IPLIST, 
	            WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME,
	            WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP};
	    MatrixCursor cursor = new MatrixCursor(columnsNames);		
	    int listCnt = 0;
	    String key = null;
	    String listItem = null;;
	    QRomIplistData iplistData = null;
	    String updateTime = null;
	    String clientIp = null;
	    for (Entry<String, QRomIplistData> entry : ipInfos.entrySet()) {
	        key = entry.getKey();
	        iplistData = entry.getValue();
	        if (QWupStringUtil.isEmpty(key) || iplistData == null || iplistData.isEmpty()) {
	            continue;
	        }
	        list = iplistData.getIplistInfo();
	        listCnt = list.size();
	        clientIp = iplistData.getClientIp();
	        updateTime = String.valueOf(iplistData.getIplistUpdateTime());
	        // 添加key后面所有的item
	        for (int j = 0; j < listCnt; j++) {
	            listItem = list.get(j);
	            if (QWupStringUtil.isEmpty(listItem)) {
	                continue;
	            }
	            cursor.addRow(new String[]{key, listItem, updateTime, clientIp});
	        }  // ~ 一个key的list 拼装完成
	        QWupLog.trace(TAG, "getIpListInfosCursor-> " + iplistData);
	    }
	    return cursor;
	}
	
	/**
	 * 处理数据请求
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public MatrixCursor doSpeOperByType(String selection, String[] selectionArgs) {
	    if (QWupStringUtil.isEmpty(selection)) {
	        return null;
	    }
	    MatrixCursor cursor = null;
	    String[] columnsNames = null;
	    if (WUP_ROM_PROVIDER_OPER_INFO.OPER_UPDATE_IPLIST_FORCE.equals(selection)) {  // 更新iplist
	        String operType = null;
	        if (selectionArgs != null && selectionArgs.length > 0) {
	            operType = selectionArgs[0];
	        }
	        int rspCode = IPLIST_RSP_CODE.IPLIST_REQ_FAIL;
	        if (!QWupStringUtil.isEmpty(operType)) {
	            QWupLog.trace(TAG, "doSpeOperByType->OPER_UPDATE_IPLIST_FORCE operType = " + operType);
	            int ipstReq = IPLIST_REQ_TYPE.IPLIST_REQ_NORMAL;
	            if (operType.equals(String.valueOf(WUP_OPER_TYPE.OPER_UPDATE_IPLIST2ROM))) { // 向rom请求更新iplist
	                ipstReq = IPLIST_REQ_TYPE.IPLIST_REQ_ROM_UPDATE;
	            } 
	            
	            rspCode = QRomWupImplEngine.getInstance().getWupRunTimeManager().requestIpList(ipstReq);
	        }
	        // 拼装返回数据
	        columnsNames = new String[] {
	                WUP_ROM_PROVIDER_COLUMN.COLUMN_OPERRSP};
	        cursor = new MatrixCursor(columnsNames);       
	        cursor.addRow(new Integer[]{rspCode});
	        QWupLog.i(TAG, "doSpeOperByType-> OPER_UPDATE_IPLIST_FORCE rspCode =" + rspCode);
	    } else if (WUP_ROM_PROVIDER_OPER_INFO.OPER_REQUEST_GUID_CHECK.equals(selection)) {  // 请求guid
	        String reqType = null;
	        if (selectionArgs != null && selectionArgs.length > 0) {
	            reqType = selectionArgs[0];
            }
	        if (QWupStringUtil.isEmpty(reqType)) {
	            reqType = "";
	        }
	        int rspId  = LOGIN_RSP_CODE.LOGIN_CANCEL_NONEED;
//	        if (reqType.equals(LOGIN_REQ_TYPE.LOGIN_REQ_GETGUID)) {
	            rspId = QRomWupImplEngine.getInstance().getWupRunTimeManager()
	                    .sendGuidRequestForceForMainProces();
//	        }	        
	        QWupLog.i(TAG, "doSpeOperByType-> OPER_REQUEST_GUID_CHECK: reqType =" + reqType + ", rspId = " + rspId);
	        // 拼装返回数据
	        columnsNames = new String[] {
	                WUP_ROM_PROVIDER_COLUMN.COLUMN_OPERRSP};
            cursor = new MatrixCursor(columnsNames);       
            cursor.addRow(new Integer[]{rspId});
	    }
	    return cursor;
	}
	
	/**
	 * 初始化wup
	 *    -- 防止 provider在app启动之前初始化，获取相关数据
	 */
	public void initForWup(Context context) {
	    QWupLog.i(TAG, "initWup");
	    if (mContext == null) {	        
	        mContext = context;
	        QRomWupImplEngine.getInstance().startUp(
	                getAppContext(), WUP_START_MODE.WUP_START_GET_GUID);
	    }
	}
	
	public Context getAppContext() {
		return mContext;
	}
	
}
