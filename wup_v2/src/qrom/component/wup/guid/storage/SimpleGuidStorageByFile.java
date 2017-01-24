package qrom.component.wup.guid.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.json.JSONObject;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.FileUtil;
import qrom.component.wup.base.utils.MD5;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.threads.StorageThread;

/**
 *  普通形式的guid文件，适用于放在非共用目录下
 * @author wileywang
 *
 */
class SimpleGuidStorageByFile implements IGuidStorage {
	private static final String TAG = SimpleGuidStorageByFile.class.getSimpleName();
	
	// nerver changed these field’s name
	private static final String GUID_KEY = "guid";  
	private static final String SIGN_KEY = "sign";  
	private static final String FOR_MD5_SUFFIX = "_guid_suffix"; 
	// ==============================
	
	private byte[] mGuidBytes;
	private File mStorageFile;
	
	public SimpleGuidStorageByFile(File storageFile) {
		mStorageFile = storageFile;
		
		readGuidFromFile();
	}
	
	public byte[] getGuidBytes() {
		return mGuidBytes;
	}
	
	private void readGuidFromFile() {
		BufferedReader jsonReader = null;
		try {
			if (!mStorageFile.exists()) {
				return ;
			}
			JSONObject rootObject = new JSONObject(new String(FileUtil.readFile(mStorageFile), "UTF-8"));
			
			String guidStr = rootObject.getString(GUID_KEY);
			if (StringUtil.isEmpty(guidStr)) {
				QRomLog.d(TAG, "readGuidFromFile, but guid is empty");
				return ;
			}
			
			String signStr = rootObject.getString(SIGN_KEY);
			if (StringUtil.isEmpty(signStr)) {
				QRomLog.d(TAG, "readGuidFromFile, but sign is empty");
				return ;
			}
			
			if (!signStr.equals(getSign(guidStr))) {
				QRomLog.d(TAG, "readGuidFromFile, but sign is not right");
				return ;
			}
			
			mGuidBytes = StringUtil.hexStringToByte(guidStr);
		} catch (Throwable e) {
			QRomLog.d(TAG, "readFromStorage Exception " + e.getMessage());
		} finally {
			if (jsonReader != null) {
				try {
					jsonReader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public void updateGuidBytes(byte[] newGuidBytes) {
		mGuidBytes = newGuidBytes;
		
		try {
			final JSONObject rootObject = new JSONObject();
			if (newGuidBytes == null) {
				rootObject.put(GUID_KEY, "");
			} else {
				String guidStr = StringUtil.byteToHexString(newGuidBytes);
				String signStr = getSign(guidStr);
				rootObject.put(GUID_KEY, guidStr);
				rootObject.put(SIGN_KEY, signStr);
			}
			
			if (StorageThread.isInSameThread()) {
				writeGuidToFile(rootObject);
			} else {
				StorageThread.get().getHandler().post(new Runnable() {
					@Override
					public void run() {
						writeGuidToFile(rootObject);
					}
				});
			}
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
	}
	
	private void writeGuidToFile(JSONObject rootObject) {
		FileUtil.writeFile(mStorageFile, rootObject.toString().getBytes(Charset.forName("UTF-8")));
	}
	
	private String getSign(String guidStr) {
		return MD5.toMD5(guidStr + FOR_MD5_SUFFIX);
	}
}
