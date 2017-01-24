package qrom.component.wup.iplist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.iplist.node.IPListNode;
import qrom.component.wup.iplist.node.IPPortNode;
import qrom.component.wup.runInfo.ProviderConst;
import qrom.component.wup.runInfo.ProviderConst.WUP_ROM_PROVIDER_COLUMN;
import qrom.component.wup.runInfo.ProviderConst.WUP_ROM_PROVIDER_PARAMS;
import qrom.component.wup.threads.StorageThread;
import TRom.EIPType;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 * @author wileywang
 *
 */
public class IPListClientByProvider implements IIPListClient {
	private static final String TAG = IPListClientByProvider.class.getSimpleName();
	
	private static final int MAX_CACHED_TIMEMS = 30 * 60 *1000;  // 内存中，刷新缓存的时间
	
	private Uri mSelectIPPortBaseUri;
	private Uri mGetCurApnIPListBaseUri;
	private Uri mReportErrorBaseUri;
	
	private Map<Integer, CacheEntry> mCacheResults;
	
	private static class CacheEntry {
		private SelectedIPPortResult mCachedResult;
		private long mCachedTimestampMs;
		
		public CacheEntry(SelectedIPPortResult result, long cachedTimestampMs) {
			this.mCachedResult = result;
			this.mCachedTimestampMs = cachedTimestampMs;
		}
		
		public SelectedIPPortResult getCachedResult() {
			return mCachedResult;
		}
		
		public long getCachedTimestampMs() {
			return mCachedTimestampMs;
		}
	}
	
	private ContentObserver mContentObserver = new ContentObserver(StorageThread.get().getHandler()) {
		@Override
		public void onChange(boolean selfChange) {
			mCacheResults.clear();
		}
		
	};
	
	public IPListClientByProvider(String baseAuthority) {
		mSelectIPPortBaseUri = Uri.parse("content://" +  baseAuthority + ProviderConst.WUP_PROVIDER_SUFFIX
				+ "/" + ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_SELECT_IPPORT);
		mGetCurApnIPListBaseUri = Uri.parse("content://" + baseAuthority + ProviderConst.WUP_PROVIDER_SUFFIX
				+ "/" + ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_APN_IPLIST);
		mReportErrorBaseUri = Uri.parse("content://"  + baseAuthority + ProviderConst.WUP_PROVIDER_SUFFIX
				+ "/" + ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_REPORT_IPPORT_ERROR);
		
		mCacheResults = new ConcurrentHashMap<Integer, CacheEntry>();
		
		ContextHolder.getApplicationContextForSure().getContentResolver().registerContentObserver(
				mSelectIPPortBaseUri, true, mContentObserver);
	}
	
	public void release() {
		ContextHolder.getApplicationContextForSure().getContentResolver().unregisterContentObserver(mContentObserver);
		mCacheResults.clear();
	}
	
	@Override
	public SelectedIPPortResult selectIPPort(RunEnvType envType, EIPType ipType, ConnectInfo connectInfo) {
		int key = generateKey(envType, ipType);
		
		// 这句话很有用, 不要改成mLastSelectedResult的直接应用
		// 快速拷贝引用，后续如果IPList更新导致mLastSelectedResult失效， 从而不会出现空指针问题
		CacheEntry cacheEntry = mCacheResults.get(key);
		
		int apnIndex = ApnIndexConvertor.getApnIndex(connectInfo);
		if (cacheEntry != null) {
			// 缓存命中， 同时对应的apn索引一致，则可以复用
			if (apnIndex == cacheEntry.getCachedResult().getApnIndex() &&
				(System.currentTimeMillis() - cacheEntry.getCachedTimestampMs()) < MAX_CACHED_TIMEMS) {
				return new SelectedIPPortResult(
						cacheEntry.getCachedResult().getNodeInfo(), ipType, envType, apnIndex, connectInfo.getBssid())
						.setExtraData("from_cache");
			}
		}
		
		// 更新选取IPList 
		IPPortNodeInfo selectedIPPortNode = selectedNewestIPPort(envType, ipType, connectInfo);
		if (selectedIPPortNode == null) {
			return null;
		}
		
		SelectedIPPortResult selectedResult 
			= new SelectedIPPortResult(selectedIPPortNode, ipType
					, envType, apnIndex, connectInfo.getBssid()).setExtraData("updated_result");
		mCacheResults.put(key, new CacheEntry(selectedResult, System.currentTimeMillis()));
		
		return selectedResult;
	}

	@Override
	public void reportError(final SelectedIPPortResult reportResult, final int reportErrorCode) {
		if (reportErrorCode == WUP_ERROR_CODE.WUP_READ_TIMEOUT) {
			// 读超时，很多时候偶尔网络慢，但是不属于需要切换IPList
			return ;
		} else {
			mCacheResults.remove(generateKey(reportResult.getEnvType(), reportResult.getIPType()));
		}
		
		StorageThread.get().getHandler().post(new Runnable() {
			@Override
			public void run() {
				try {
					ContentValues reportValues = new ContentValues();
				
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_IP
							, reportResult.getNodeInfo().getNode().getIp());
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_PORT
							, reportResult.getNodeInfo().getNode().getPort());
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_ENV_TYPE, reportResult.getEnvType().value());
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_IP_TYPE, reportResult.getIPType().value());
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_APN_INDEX, reportResult.getApnIndex());
					if (!StringUtil.isEmpty(reportResult.getBssid())) {
						reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_BSSID, reportResult.getBssid());
					}
					
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_ERRORCODE, reportErrorCode);
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_IPLIST_SIZE, reportResult.getNodeInfo().getIPListSize());
					reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_IP_INDEX, reportResult.getNodeInfo().getIPIndex());
					
					if (!StringUtil.isEmpty(reportResult.getNodeInfo().getClientIP())) {
						reportValues.put(WUP_ROM_PROVIDER_PARAMS.PARAM_REPORT_CLIENT_IP, reportResult.getNodeInfo().getClientIP());
					}
				
					Uri result = ContextHolder.getApplicationContextForSure().getContentResolver().insert(
									mReportErrorBaseUri, reportValues);
					if (result == null) {
						QRomLog.i(TAG, "reportError to provider failed! result=" + reportResult 
										+ ", errorCode=" + reportErrorCode);
					}
				} catch (Throwable e) {
					QRomLog.e(TAG, e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public IPListNode getCurApnIPListNode(RunEnvType envType, EIPType ipType) {
		Uri.Builder builder = new Uri.Builder();
		
		builder.scheme(mGetCurApnIPListBaseUri.getScheme());
		builder.authority(mGetCurApnIPListBaseUri.getAuthority());
		builder.path(mGetCurApnIPListBaseUri.getPath());
		
		ConnectInfo connectInfo = ConnectInfoManager.get().getConnectInfo();
		int apnIndex = ApnIndexConvertor.getApnIndex(connectInfo);
		
		builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_ENV_TYPE, String.valueOf(envType.value()));
		builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_IP_TYPE, String.valueOf(ipType.value()));
		builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_APN_INDEX
				, String.valueOf(apnIndex));
		
		Cursor cursor =  null;
		try {
			cursor = ContextHolder.getApplicationContextForSure().getContentResolver()
					.query(builder.build(), null, null, null, null);
			
			if (cursor != null && cursor.moveToFirst()) {
				IPListNode ipListNode = new IPListNode(apnIndex);
				
				ipListNode.setLastUpdatTimestamp(
						cursor.getLong(cursor.getColumnIndexOrThrow(WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_UPDATE_TIME)));
				ipListNode.setClientIP(
						cursor.getString(cursor.getColumnIndexOrThrow(WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP)));
				do {
					String ip = cursor.getString(cursor.getColumnIndexOrThrow(WUP_ROM_PROVIDER_COLUMN.COLUMN_IP));
					int port = cursor.getInt(cursor.getColumnIndexOrThrow(WUP_ROM_PROVIDER_COLUMN.COLUMN_PORT));
					
					ipListNode.getIPPortList().add(new IPPortNode(ip, port));
					
				} while (cursor.moveToNext());
				
				return ipListNode;
			}
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return null;
	}
	
	private int generateKey(RunEnvType envType, EIPType ipType) {
		return envType.value() * 1000 + ipType.value();
	}

	private IPPortNodeInfo selectedNewestIPPort(RunEnvType envType, EIPType ipType, ConnectInfo connectInfo) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(mSelectIPPortBaseUri.getScheme());
		builder.authority(mSelectIPPortBaseUri.getAuthority());
		builder.path(mSelectIPPortBaseUri.getPath());
		
		builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_ENV_TYPE, String.valueOf(envType.value()));
		builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_IP_TYPE, String.valueOf(ipType.value()));
		builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_APN_INDEX
				, String.valueOf(ApnIndexConvertor.getApnIndex(connectInfo)));
		
		if (StringUtil.isEmpty(connectInfo.getBssid())) {
			builder.appendQueryParameter(WUP_ROM_PROVIDER_PARAMS.PARAM_BSSID, connectInfo.getBssid());
		}
		
		Cursor cursor =  null;
		try {
			cursor = ContextHolder.getApplicationContextForSure().getContentResolver()
					.query(builder.build(), null, null, null, null);
			
			if (cursor != null && cursor.moveToFirst()) {
				String ip = cursor.getString(
						cursor.getColumnIndexOrThrow(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_IP));
				int port = cursor.getInt(
						cursor.getColumnIndexOrThrow(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_PORT));
				int ipListSize = cursor.getInt(
						cursor.getColumnIndexOrThrow(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_SIZE));
				int ipIndex = cursor.getInt(
						cursor.getColumnIndexOrThrow(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_IP_INDEX));
				String clientIP = cursor.getString(
						cursor.getColumnIndexOrThrow(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_IPLIST_CLIENTIP));
				
				return new IPPortNodeInfo(
							new IPPortNode(ip, port), ipListSize, ipIndex, clientIP);
			}
			
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return null;
	}

	
}
