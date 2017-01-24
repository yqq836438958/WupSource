package qrom.component.wup.runInfo;

import java.util.List;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.event.EventType;
import qrom.component.wup.base.event.IEventSubscriber;
import qrom.component.wup.base.event.Subscriber;
import qrom.component.wup.base.utils.ContentValuesHelper;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.guid.GuidEngine;
import qrom.component.wup.guid.GuidUpdateEvent;
import qrom.component.wup.iplist.ApnIndexConvertor;
import qrom.component.wup.iplist.IPListEngine;
import qrom.component.wup.iplist.IPListUpdateEvent;
import qrom.component.wup.iplist.IPPortNodeInfo;
import qrom.component.wup.iplist.SelectedIPPortResult;
import qrom.component.wup.iplist.node.IPListNode;
import qrom.component.wup.iplist.node.IPPortNode;
import qrom.component.wup.runInfo.ProviderConst.WUP_ROM_PROVIDER_COLUMN;
import qrom.component.wup.runInfo.ProviderConst.WUP_ROM_PROVIDER_PARAMS;
import TRom.EIPType;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;

public class QRomWupProvider extends ContentProvider implements IEventSubscriber {
	private static final String TAG = QRomWupProvider.class.getSimpleName();

	private static final int URI_MATCH_GET_GUID = 1;
	private static final int URI_MATCH_GET_IPLIST_PROXY = 2;
	private static final int URI_MATCH_GET_IPLIST_SOCKET = 3;
	private static final int URI_MATCH_GET_ROM_ID = 4;
	private static final int URI_MATCH_SYN_HOST_ROM_GUID = 5;
	private static final int URI_MATCH_GET_IPLIST_WIFI = 6;
	private static final int URI_MATCH_DO_SPE_OPER = 7;
	private static final int URI_MATCH_SELECT_IPPORT = 8;
	private static final int URI_MATCH_GET_APN_IPLIST = 9;
	
	private static final int URI_MATCH_REPORT_IPPORT_ERROR = 10;

	private final UriMatcher URI_ROM_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	
	private String mAuthority;
	private boolean mHasStartUp = false;

	@Override
	public boolean onCreate() {
		mAuthority = getContext().getPackageName() + ProviderConst.WUP_PROVIDER_SUFFIX;
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_GUID, URI_MATCH_GET_GUID);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_IPLIST_PROXY, URI_MATCH_GET_IPLIST_PROXY);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_IPLIST_SOCKET, URI_MATCH_GET_IPLIST_SOCKET);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_ROM_ID, URI_MATCH_GET_ROM_ID);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_SYN_HOST_ROM_GUID, URI_MATCH_SYN_HOST_ROM_GUID);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_IPLIST_WIFI, URI_MATCH_GET_IPLIST_WIFI);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_DO_SPE_OPER, URI_MATCH_DO_SPE_OPER);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_SELECT_IPPORT, URI_MATCH_SELECT_IPPORT);
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_APN_IPLIST, URI_MATCH_GET_APN_IPLIST);
		
		URI_ROM_MATCHER.addURI(mAuthority
				, ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_REPORT_IPPORT_ERROR, URI_MATCH_REPORT_IPPORT_ERROR);
		
		return false;
	}
	
	private void doStartUpLazy() {
		if (!mHasStartUp) {
			synchronized(this) {
				if (mHasStartUp) {
					return ;
				}
				
				ContextHolder.setApplicationContext(getContext());
				
				GuidEngine.startUp();
				IPListEngine.startUp();
				
				EventBus.getDefault().register(this);
				
				mHasStartUp = true;
			}
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		doStartUpLazy();
		
		int matcheType = URI_ROM_MATCHER.match(uri);
		QRomLog.i(TAG, "query -> matcheType = " + matcheType 
				+ ", uri=" + uri + ", callingPid=" + Binder.getCallingPid());
		switch (matcheType) {
		
		// 获取guid
		case URI_MATCH_GET_GUID: 
			return getGuidCursor();
			
		// 获取wup 代理地址  
		case URI_MATCH_GET_IPLIST_PROXY: 
			return getProxyIpInfos();
			
			// 获取wup socket 代理地址
		case URI_MATCH_GET_IPLIST_SOCKET: 
			return getSocketProxyIpInfos();
		
		// 获取romid
		case URI_MATCH_GET_ROM_ID: 
			return getRomIdCursor();

		case URI_MATCH_SYN_HOST_ROM_GUID: // 同步其他rom host的guid
			return null;
		
		// 目前不再按照bssid缓存IPList, 作用不大
		case URI_MATCH_GET_IPLIST_WIFI: // wifi下iplsit信息
			return null;

		// 执行对应操作, 废弃，实际上并没有什么用
		case URI_MATCH_DO_SPE_OPER:
			return null;
		
		// 以下为新版本加入能力, by wileywang
		// 帮助选取合适的接入IP
		case URI_MATCH_SELECT_IPPORT :
			return selectIPPort(uri);
			
		// 获取当前接入点
		case URI_MATCH_GET_APN_IPLIST : 
			return getApnIPList(uri);
			
		default:
			break;
		}

		return null;
	}
	
	private Cursor getGuidCursor() {
		String guidStr = StringUtil.byteToHexString(GuidEngine.get().getGuidBytes());
		if (!QRomWupDataBuilder.isGuidValidate(guidStr)) {  // guid不合法
			return null;
		}
		
		MatrixCursor cursor = new MatrixCursor(new String[]{ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_GUID});
		cursor.addRow(new String[]{guidStr});		
		return cursor;
	}
	
	private Cursor getRomIdCursor() {
		String[] columnsNames = new String[] {ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_QROM_ID};
	    MatrixCursor cursor = new MatrixCursor(columnsNames);
	    cursor.addRow(new String[]{"0"});
	    return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		doStartUpLazy();
		QRomLog.d(TAG, "insert uri=" + uri.toString() + ", callingPid=" + Binder.getCallingPid());
		int matcheType = URI_ROM_MATCHER.match(uri);
		if (matcheType == URI_MATCH_REPORT_IPPORT_ERROR) {
			ContentValuesHelper valuesHelper = new ContentValuesHelper(values);
			
			String ip = valuesHelper.getAsString(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_IP, "");
			if (StringUtil.isEmpty(ip)) {
				return null;
			}
			
			int port = valuesHelper.getAsInt(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_PORT, 0);
			if (port <= 0) {
				return null;
			}
			
			RunEnvType envType = RunEnvType.from(
					valuesHelper.getAsInt(WUP_ROM_PROVIDER_PARAMS.PARAM_ENV_TYPE, -1));
			if (envType == null) {
				return null;
			}
			
			EIPType ipType = EIPType.convert(valuesHelper.getAsInt(WUP_ROM_PROVIDER_PARAMS.PARAM_IP_TYPE, -1));
			if (ipType == null) {
				return null;
			}
			
			int apnIndex = valuesHelper.getAsInt(WUP_ROM_PROVIDER_PARAMS.PARAM_APN_INDEX, -1);
			if (apnIndex < 0) {
				return null;
			}
			Integer errorCode = values.getAsInteger(WUP_ROM_PROVIDER_PARAMS.PARAM_ERRORCODE);
			if (errorCode == null) {
				return null;
			}
			
			String bssid = valuesHelper.getAsString(WUP_ROM_PROVIDER_PARAMS.PARAM_BSSID, "");
			int ipListSize = valuesHelper.getAsInt(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_IPLIST_SIZE, 0);
			int ipIndex = valuesHelper.getAsInt(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_IP_INDEX, -1);
			String clientIP = valuesHelper.getAsString(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_CLIENT_IP, "");
			
			IPListEngine.get().reportError(new SelectedIPPortResult(
					new IPPortNodeInfo(new IPPortNode(ip, port), ipListSize, ipIndex, clientIP)
					, ipType, envType, apnIndex, bssid), errorCode);
			
			return uri;
		}
		
		return null;
	}
	
	

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
	private MatrixCursor getProxyIpInfos() {
		List<IPListNode> ipListNodes = IPListEngine.get().copyAllIPListNode(RunEnvType.IDC, EIPType.WUPPROXY);
		if (ipListNodes == null) {
			return null;
		}
		
		return getIpInfosCursor(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_IPLIST, ipListNodes); 
	}
	
	private MatrixCursor getSocketProxyIpInfos() {
		List<IPListNode> ipListNodes = IPListEngine.get().copyAllIPListNode(RunEnvType.IDC, EIPType.WUPSOCKET);
		if (ipListNodes == null) {
			return null;
		}
		
		return getIpInfosCursor(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_PROXY_SOCKET_IPLIST, ipListNodes); 
	}
	
	private MatrixCursor getIpInfosCursor(String columnName, List<IPListNode> ipListNodes) {
		// 多一个字段用户存放key信息 (APN类型，ip信息， 缓存时间，client ip)
		String[] columnsNames = new String[] {
				ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_NET_APN_TYPE, 
				columnName, 
				ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME,
				ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP};
		MatrixCursor cursor = new MatrixCursor(columnsNames);
		
		for (IPListNode ipListNode : ipListNodes) {  // 开始循环所有的iplist数据
			int apnType = ApnIndexConvertor.getOrignalApnTypeList(ipListNode.getApnIndex());
			List<IPPortNode> ipPortNodes = ipListNode.getIPPortList();
			if (ipPortNodes.isEmpty()) {
				continue;
			}
			String clientIp = ipListNode.getClientIP();
			String updateTime = String.valueOf(ipListNode.getLastUpdateTimestamp() * 1000);
			// 添加key后面所有的item
			for (IPPortNode ipPortNode : ipPortNodes) {
				cursor.addRow(new String[]{String.valueOf(apnType), ipPortNode.toUrlString(), updateTime, clientIp});
			}  
		}
		
		return cursor;
	}
	
	private static final String[] SELECT_IP_PORT_COLUMNS = new String[] {
		  WUP_ROM_PROVIDER_COLUMN.COLUMN_IP
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_PORT
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_SIZE
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_IP_INDEX
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP
	};
	
	private MatrixCursor selectIPPort(Uri uri) {
		try {
			RunEnvType envType = RunEnvType.from(
					Integer.valueOf(uri.getQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_ENV_TYPE)));
			if (envType == null) {
				return null;
			}
			
			EIPType ipType = EIPType.convert(
					Integer.valueOf(uri.getQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_IP_TYPE)));
			if (ipType == null) {
				return null;
			}
			
			int apnIndex = Integer.valueOf(uri.getQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_APN_INDEX));
			
			IPPortNodeInfo ipPortNodeInfo = IPListEngine.get().selectIPPort(envType, ipType, apnIndex); 
			if (ipPortNodeInfo == null) {
				return null;
			}
			
			MatrixCursor cursor = new MatrixCursor(SELECT_IP_PORT_COLUMNS);
			
			Object[] row = new Object[SELECT_IP_PORT_COLUMNS.length];
			row[0] = ipPortNodeInfo.getNode().getIp();
			row[1] = ipPortNodeInfo.getNode().getPort();
			row[2] = ipPortNodeInfo.getIPListSize();
			row[3] = ipPortNodeInfo.getIPIndex();
			row[4] = ipPortNodeInfo.getClientIP();
			
			cursor.addRow(row);
			
			return cursor;
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
		return null;
	}
	
	private static final String[] APN_IPLIST_COLUMNS = new String[] {
		  WUP_ROM_PROVIDER_COLUMN.COLUMN_IP
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_PORT
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME
		, WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP
	};
	
	private MatrixCursor getApnIPList(Uri uri) {
		try {
			RunEnvType envType = RunEnvType.from(
					Integer.valueOf(uri.getQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_ENV_TYPE)));
			if (envType == null) {
				return null;
			}
			
			EIPType ipType = EIPType.convert(
					Integer.valueOf(uri.getQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_IP_TYPE)));
			if (ipType == null) {
				return null;
			}
			
			int apnIndex = Integer.valueOf(uri.getQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_APN_INDEX));
			
			IPListNode ipListNode = IPListEngine.get().copyIPListNode(envType, ipType, apnIndex);
			if (ipListNode == null) {
				return null;
			}
			
			MatrixCursor cursor = new MatrixCursor(APN_IPLIST_COLUMNS);
			
			for (IPPortNode ipPortNode : ipListNode.getIPPortList()) {
				Object[] row = new Object[APN_IPLIST_COLUMNS.length];
				
				row[0] = ipPortNode.getIp();
				row[1] = ipPortNode.getPort();
				row[2] = ipListNode.getLastUpdateTimestamp();
				row[3] = ipListNode.getClientIP();
				
				cursor.addRow(row);
			}
			
			return cursor;
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
		return null;
	}
	
	@Subscriber
	public void onGuidUpdateEvent(GuidUpdateEvent event) {
		QRomLog.d(TAG, "onGuidUpdateEvent notify guid update to content obbserver, newGuidBytes=" + event.getNewGuidBytes());
		getContext().getContentResolver().notifyChange(
				Uri.parse("content://" + mAuthority + "/" + ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_GUID)
				, null);
	}
	
	@Subscriber
	public void onIPListUpdateEvent(IPListUpdateEvent event) {
		QRomLog.d(TAG, "onIPListUpdateEvent notify iplist update to content observer, envType="
					+ event.getUpdateEnvType());
		getContext().getContentResolver().notifyChange(
				Uri.parse("content://" + mAuthority + "/" + ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_SELECT_IPPORT)
				, null);
	}

	@Override
	public IWorkRunner receiveEventOn(EventType eventType) {
		return null;
	}

}
