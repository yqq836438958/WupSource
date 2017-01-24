package qrom.component.wup.runInfo;


import qrom.component.wup.build.QWupUriFactory;
import qrom.component.wup.utils.QWupLog;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class QRomWupProvider extends ContentProvider {

    private String TAG = "QRomWupProvider";
	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
	    
	    QRomWupProviderImpl.getInstance().initForWup(getContext());
	    int matcheType = QWupUriFactory.URI_ROM_MATCHER.match(uri);
		QWupLog.i(TAG, "query -> matcheType = " + matcheType + ", selection: " + selection);
        switch (matcheType) {
        
        case QWupUriFactory.URI_MATCH_GET_GUID:  //获取guid
        	return QRomWupProviderImpl.getInstance().getGuid();
        	
        case QWupUriFactory.URI_MATCH_GET_IPLIST_PROXY:  // 获取wup 代理地址
        	return QRomWupProviderImpl.getInstance().getProxyIpInfos();
        	
        case QWupUriFactory.URI_MATCH_GET_IPLIST_SOCKET: // 获取wup socket 代理地址
        	return QRomWupProviderImpl.getInstance().getSocketProxyIpInfos();
        case  QWupUriFactory.URI_MATCH_GET_ROM_ID:  // 获取romid
            return QRomWupProviderImpl.getInstance().getRomId();
        
        case QWupUriFactory.URI_MATCH_SYN_HOST_ROM_GUID:  // 同步其他rom host的guid
            return QRomWupProviderImpl.getInstance().getSynHostGuid();
        	
        case QWupUriFactory.URI_MATCH_GET_IPLIST_WIFI: // wifi下iplsit信息
            return QRomWupProviderImpl.getInstance().getIplistfos(selection, selectionArgs);
            
        case QWupUriFactory.URI_MATCH_DO_SPE_OPER: //执行对应操作
            return QRomWupProviderImpl.getInstance().doSpeOperByType(selection, selectionArgs);
        default:
            	break;
        }	
		
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    QRomWupProviderImpl.getInstance().initForWup(getContext());
	    
	    int matcheType = QWupUriFactory.URI_ROM_MATCHER.match(uri);
	    QWupLog.i(TAG, "insert -> matcheType = " + matcheType);
	    if (values == null) {
	        return null;
	    }
	    switch (matcheType) {
        case QWupUriFactory.URI_MATCH_GET_ROM_ID:  // 设置qromid
            long romId = values.getAsLong(QRomWupProviderImpl.WUP_ROM_PROVIDER_COLUMN.COLUMN_QROM_ID);
            QRomWupProviderImpl.getInstance().setRomId(romId);
            break;

        default:
            break;
        }
	    
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    QRomWupProviderImpl.getInstance().initForWup(getContext());
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
	    QRomWupProviderImpl.getInstance().initForWup(getContext());
		return 0;
	}

}
