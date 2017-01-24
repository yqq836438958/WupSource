package qrom.component.wup.guid.storage;

import java.io.File;
import java.nio.charset.Charset;

import org.json.JSONObject;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.FileUtil;
import qrom.component.wup.base.utils.MD5;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.BaseInfoManager;
import qrom.component.wup.threads.StorageThread;
import android.os.Build;

import com.tencent.codec.des.DES;

/**
 * 加密形式文件存储，主要用于放置在sdcard上的文件形式
 * @author wileywang
 *
 */
class EncryptGuidStorageByFile implements IGuidStorage {
	private static final String TAG = EncryptGuidStorageByFile.class.getSimpleName();
	
	// never changed these fields
	private static final byte[] ENCRYPT_KEY   = { 
		(byte) 0x86, (byte) 0xf8, (byte) 0xe9, (byte) 0xac,
        (byte) 0x83, (byte) 0x71, (byte) 0x54, (byte) 0x63 };
	
	private static final String GUID_KEY = "guid";
	private static final String SIGN_KEY = "sign";
	private static final String SIGN_SUFFIX = "_encrypt_guid_suffix";
	private static final String PHONE_MODEL_KEY = "model";
	private static final String IMEI_KEY = "imei";
	
	// =========================
	
	private byte[] mGuidBytes;
	private File mStorageFile;
	
	public EncryptGuidStorageByFile(
			File storageFile
			, boolean needInit) {
		mStorageFile = storageFile;
		
		if (needInit) {
			readGuidFromFile();
		}
	}
	
	@Override
	public byte[] getGuidBytes() {
		return mGuidBytes;
	}

	@Override
	public void updateGuidBytes(byte[] newGuidBytes) {
		mGuidBytes = newGuidBytes;
		
		try {
			final JSONObject rootObject = new JSONObject();
			if (newGuidBytes == null || newGuidBytes.length <= 0) {
				rootObject.put(GUID_KEY, "");
			} else {
				String guidStr = StringUtil.byteToHexString(newGuidBytes);
				String modelStr = (Build.MODEL == null) ? "" : Build.MODEL;
				String imeiStr = BaseInfoManager.get().getImei();
				if (imeiStr == null) {
					imeiStr = "";
				}
				rootObject.put(GUID_KEY, guidStr);
				rootObject.put(PHONE_MODEL_KEY, modelStr);
				rootObject.put(IMEI_KEY, imeiStr);
				rootObject.put(SIGN_KEY, getSign(guidStr, modelStr, imeiStr));
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
		byte[] encryptContent = 
				DES.DesEncrypt(ENCRYPT_KEY
						, rootObject.toString().getBytes(Charset.forName("UTF-8"))
						, DES.FLAG_ENCRYPT);
		if (encryptContent == null) {
			QRomLog.e(TAG, "encrypt json content failed!");
			return ;
		}
		FileUtil.writeFile(mStorageFile,  encryptContent);
	}
	
	private void readGuidFromFile() {
		if (!mStorageFile.exists()) {
			return ;
		}
		try {
			byte[] jsonContent = DES.DesEncrypt(ENCRYPT_KEY, FileUtil.readFile(mStorageFile), DES.FLAG_DECRYPT);
			if (jsonContent == null) {
				QRomLog.e(TAG, "readGuidFromFile decode " + mStorageFile.getAbsolutePath() + " failed!");
				mStorageFile.delete();
				return ;
			}
			
			JSONObject rootObject = new JSONObject(new String(jsonContent, "UTF-8"));
			
			String guidStr = rootObject.getString(GUID_KEY);
			if (StringUtil.isEmpty(guidStr)) {
				QRomLog.d(TAG, "guidStr is empty!");
				return ;
			}
			String modelStr = rootObject.getString(PHONE_MODEL_KEY);
			String imeiStr = rootObject.getString(IMEI_KEY);
			String signStr = rootObject.getString(SIGN_KEY);
			
			String shouldSignStr = getSign(guidStr, modelStr, imeiStr);
			if (!shouldSignStr.equals(signStr)) {
				QRomLog.e(TAG, "readGuidFromFile sign is not right, should sign=" + shouldSignStr + ", but get " + signStr);
				mStorageFile.delete();
				return ;
			}
			
			String currentPhoneModel = Build.MODEL;
			if (!StringUtil.isEmpty(currentPhoneModel) 
					&& !StringUtil.isEmpty(modelStr)
					&& !currentPhoneModel.equals(modelStr)) {
				// 手机的型号无法和记录的型号对等，则认为记录无效
				QRomLog.e(TAG, "readGuidFromFile phoneModel=" + modelStr + ", currentPhoneModel=" + currentPhoneModel
						+ " is not equals, file invalid");
				mStorageFile.delete();
				return ;
			}
			
			String currentPhoneImei = BaseInfoManager.get().getImei();
			if (!StringUtil.isEmpty(imeiStr) 
					&& !StringUtil.isEmpty(currentPhoneImei) 
					&& !currentPhoneImei.equals(imeiStr)) {
				// 手机的imei和记录的imei不对等，则文件一定不可用
				QRomLog.e(TAG, "readGuidFromFile imei=" + imeiStr + ", currentPhoneImei=" + currentPhoneImei
						+ " is not equals, file invalid");
				mStorageFile.delete();
				return ;
			}
			
			mGuidBytes = StringUtil.hexStringToByte(guidStr);
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
			mStorageFile.delete();
		}
	}
	
	private String getSign(String guidStr, String modelStr, String imeiStr) {
		StringBuilder builder = new StringBuilder(64);
		builder.append(guidStr);
		builder.append(modelStr);
		builder.append(imeiStr);
		builder.append(SIGN_SUFFIX);
		return MD5.toMD5(builder.toString());
	}
}
