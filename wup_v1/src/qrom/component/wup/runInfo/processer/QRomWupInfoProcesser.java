package qrom.component.wup.runInfo.processer;

import java.util.List;

import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.build.QWupUriFactory;
import qrom.component.wup.build.RomHostDetector.HostAppInfo;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.runInfo.QRomWupProviderImpl;
import qrom.component.wup.runInfo.QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN;
import qrom.component.wup.runInfo.QRomWupProviderImpl.WUP_ROM_PROVIDER_OPER_INFO;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomWupInfo;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;


/**
 * rom apk 中 负责获取guid/iplist等数据的处理对象
 * 
 * @author sukeyli
 *
 */
public class QRomWupInfoProcesser extends QWupActivedInfoProcesser {

	public QRomWupInfoProcesser() {
        super(QWupFileUtil.FILE_USER_WUP_INFO_ROM,
                QWupFileUtil.SD_USER_WUP_INFO_ROM);
		TAG = "QRomWupInfoProcesser";
	}
				
	@Override
	public int sendGuidRequest() {
	    if (!isGuidValidate(getGUIDBytes())) {  // guid不合法
	        // 同步其他rom 模式下的guid
	        byte[] guid = checkGuidFromOtherRom(QRomWupImplEngine.getInstance().getContext(),
	                QRomWupBuildInfo.getWupHostInfo());
	        if (isGuidValidate(guid)) { // guid 合法
	            QWupLog.trace(TAG, "sendGuidRequest -> get other host guid ok, save guid");
	            // 同步guid
	            mQubeWupInfo.setSGUID(guid, getContext());
	            mQubeWupInfo.saveWupInfo(getContext());
	            return 0;
	        } else {
	            QWupLog.trace(TAG, "sendGuidRequest -> get other host guid is empty");
	        }
	    }
	    return super.sendGuidRequest();
	}
	   
    protected byte[] checkGuidFromOtherRom(Context context, List<HostAppInfo> mHostAppInfos) {
                
        byte[] guid = null;
        if (mHostAppInfos != null && !mHostAppInfos.isEmpty()) {  // 无host信息
            
            int cnt = mHostAppInfos.size();
            HostAppInfo appInfo = null;
            String hostPkg = null;
            for (int i = 0; i < cnt; i++) {  // 获取优先级最高的rom app 信息
                appInfo = mHostAppInfos.get(i);
                if (appInfo == null || QWupStringUtil.isEmpty(appInfo.getAppPkgName())
                        || QRomWupBuildInfo.getAppPackageName().equals(appInfo.getAppPkgName())) {
                    continue;
                }
                hostPkg = appInfo.getAppPkgName();                
                             
                // 获取rom的uri
                Uri uriGuid = QWupUriFactory.getSynHostGuidUri(hostPkg);
                // 同步guid
                guid = synGuidFromHostRomApp(context, uriGuid);
                if (QRomWupInfo.isGuidValidate(guid)) {
                    QWupLog.trace(TAG, "checkGuidFromOtherRom -> hostPkg = " + hostPkg 
                            + ", guid = " +  QWupStringUtil.byteToHexString(guid));
                    break;
                } else {
                    guid = null;
                }

            } // ~ end seacher              
        }
        
        return guid;
    }
    
	public byte[]  synGuidFromHostRomApp(Context context, Uri uriGuid) {
        String guidStr = null;
         Cursor cursor = null;
         try {
             cursor = context.getContentResolver().query(uriGuid, null, null, null, null);
             if (cursor == null || !cursor.moveToFirst()) {
                 QWupLog.trace(TAG, "updateGuidFromOtherRom : cursor = " + cursor + ", uri = " + uriGuid);
                 return null;
             }
             
             guidStr = cursor.getString(cursor.getColumnIndex(
                     QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_GUID));
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
         QWupLog.i(TAG, "synGuidFromHostRomApp -> guid = " + guidStr + ", uriGuid = " + uriGuid);
         if (!QRomWupInfo.isGuidValidate(guidStr)) {  // guid 不合法
             return null;
         }
         
         byte[] guidBytes = QWupStringUtil.hexStringToByte(guidStr);
         return guidBytes;
 }
	
	
	public long getRomIdFromTsf() {
	    Context context = QRomWupImplEngine.getInstance().getContext();

        if (context == null) {
            return -1;
        }

        mRomId = 0;
//        try {
//            String authTokenType = "com.tencent.qrom";
//            AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
//            Account[] accounts = am.getAccountsByType(authTokenType);
//            String romid = null;
//            if (accounts.length == 1) {  //我们现在是单帐号
//                romid = am.getUserData(accounts[0], "romid");
//                QWupLog.d(TAG, "romid is " + romid);
//            }
//            if (!QWupStringUtil.isEmpty(romid)) {
//                mRomId = Long.valueOf(romid);
//            }
//        } catch (Exception e) {
//            QWupLog.w(TAG, e);
//        }
        
        try {
            long romid = Settings.System.getLong(context.getContentResolver(), "romid");
            mRomId = romid;
        } catch (Throwable e) {
            QWupLog.w(TAG, "romid get fail : err msg = " + e.getMessage());
        }
        QWupLog.i(TAG, "getRomId -> romId = " + mRomId);
        if (mRomId <0) {
            mRomId = 0;
        }
        return mRomId;
	}
	
	
	@Override
	public long getRomId() {
        return super.getRomId();
//	    return getRomIdFromTsf();
	}
	
	@Override
	protected boolean isRequestGuidByMainProcess(int reqType) {
        
        Context context = QRomWupImplEngine.getInstance().getContext();
        Cursor cursor = null;
        try {
            Uri uri = QWupUriFactory.getDoSpeOperUri();
            QWupLog.trace(TAG, "isRequestGuidByMainProcess-> uri : " + uri);
            cursor = context.getContentResolver().query(uri, null, 
                    WUP_ROM_PROVIDER_OPER_INFO.OPER_REQUEST_GUID_CHECK, 
                    new String[]{String.valueOf(reqType)}, null);
            if (cursor == null || cursor.getCount() == 0) {
                QWupLog.w(TAG, "isRequestGuidByMainProcess-> rsp cursor is empty");
                return false;
            }
            int rspCode = -99;
            if (cursor.moveToFirst()) {
                rspCode = cursor.getInt(cursor.getColumnIndex(
                        WUP_ROM_PROVIDER_COLUMN.COLUMN_OPERRSP));
                QWupLog.trace(TAG, "isRequestGuidByMainProcess-> reqtype = " + reqType + ", rspCode : " + rspCode);
                return true;
            }
            QWupLog.trace(TAG, "isRequestGuidByMainProcess->get rspCode fail ");
            return false;
        } catch (Throwable e) {
            QWupLog.w(TAG, "isRequestGuidByMainProcess", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    QWupLog.w(TAG, "isRequestGuidByMainProcess: cursor close err", e);
                }
            }        
        }
        return false;	    
	}
}
