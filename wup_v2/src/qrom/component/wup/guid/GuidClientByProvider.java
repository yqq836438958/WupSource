package qrom.component.wup.guid;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.utils.ByteUtil;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.runInfo.ProviderConst;
import qrom.component.wup.threads.StorageThread;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

/**
 * 通过对应的Provider获取guid的实现
 * @author wileywang
 *
 */
class GuidClientByProvider implements IGuidClient {
	private static final String TAG = "GuidClientByProvider";
	
	private byte[] mGuidBytes;
	private final Uri mUri;
	
	private ContentObserver mContentObserver = new ContentObserver(StorageThread.get().getHandler()) {
		
		@Override
		public void onChange(boolean selfChange) {
			byte[] orignalGuidBytes = mGuidBytes;
			updateGuidBytes();
			if (!ByteUtil.isEquals(orignalGuidBytes, mGuidBytes)) {
				EventBus.getDefault().post(new GuidUpdateEvent(mGuidBytes));
			}
		}
	};
	
	public GuidClientByProvider(String baseAuthority) {
		mUri = Uri.parse("content://" + baseAuthority + ProviderConst.WUP_PROVIDER_SUFFIX
				+ "/" + ProviderConst.WUP_ROM_PROVIDER_ACTIONS.ACTION_GET_GUID);
		updateGuidBytes();
		
		// 注册ContentObserver, 收取远端更新通知
		ContextHolder.getApplicationContextForSure()
				.getContentResolver().registerContentObserver(mUri, true, mContentObserver);
	}
	
	public void release() {
		ContextHolder.getApplicationContextForSure().getContentResolver().unregisterContentObserver(mContentObserver);
	}
	
	private void updateGuidBytes() {
		String guidStr = null;
		Cursor cursor = null;
		try {
			cursor = ContextHolder.getApplicationContextForSure()
					.getContentResolver().query(mUri, null, null, null, null);
			QRomLog.trace(TAG, "updateGuidBytes : uri = " + mUri);
			if (cursor != null && cursor.moveToFirst()) { 
				guidStr = cursor.getString(cursor.getColumnIndex(ProviderConst.WUP_ROM_PROVIDER_COLUMN.COLUMN_GUID));
			}
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
				}
			}
		}
		
		if (!QRomWupDataBuilder.isGuidValidate(guidStr)) {
			mGuidBytes = QRomWupConstants.WUP_DEFAULT_GUID;
		} else {
			mGuidBytes = StringUtil.hexStringToByte(guidStr);
		}
		
		QRomLog.d(TAG, "updateGuidBytes guid=" +  StringUtil.byteToHexString(mGuidBytes));
	}
	
	@Override
	public byte[] getGuidBytes() {
		if (!QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
			updateGuidBytes();
		}
		
		return mGuidBytes;
	}

	@Override
	public int requestLogin() {
		return -1;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "(" + mUri + ")";
	}

}
