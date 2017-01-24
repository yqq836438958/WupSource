package qrom.component.wup.guid.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.base.utils.FileUtil;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.PathService;
import qrom.component.wup.threads.StorageThread;
import android.os.Build;
import android.text.TextUtils;

import com.tencent.codec.des.DES;

/**
 *  结合多种存储方式， 同时兼容老版本的数据，完成数据迁移
 *  
 *  GUID获取算法：
 *  1. 优先读取V2版本的GUID， 如果读取到了，则直接使用。并清理V1版本的数据
 *  2. 负责从V1版本中的数据进行恢复
 *  
 *  为什么不在V1恢复V2的数据后，立即删除V1的数据呢？
 *  主要原因是利用V1的数据产生V2版本的数据后，出现意外crash，这个时候正好把文件删除了，导致丢失。所以保留一段时间，
 *  在下次重启时，完全确保V2数据可用后，再删除V1的数据，做到更完善的确保
 * @author wileywang
 *
 */
public class GuidStorageImpl implements IGuidStorage {
	private static final String TAG = GuidStorageImpl.class.getSimpleName();
	
	private SimpleGuidStorageByFile mInnerGuidStorage;
	private EncryptGuidStorageByFile mSdcardGuidStorage;
	
	private byte[] mGuidBytes;
	
	private File mV1RomUserCacheFile;
	private File mV1RomSdcardCacheFile;
	
	private File mV1AppUserCacheFile;
	private File mV1AppSdcardCacheFile;
	
	public GuidStorageImpl() {
		mV1RomUserCacheFile  = new File(PathService.getFilesDir(), "/wupData/wup_user_info_rom.inf");
		mV1RomSdcardCacheFile = new File(PathService.getSdcardPackageCacheDir(), "wup_user_rom.cache");
		mV1AppUserCacheFile = new File(PathService.getFilesDir(), "/wupData/wup_user_info_app.inf");
		mV1AppSdcardCacheFile = new File(PathService.getSdcardPackageCacheDir(), "wup_user_app.cache");
		
		mInnerGuidStorage = new SimpleGuidStorageByFile(new File(PathService.getFilesDir(), "/wup/inner_guid_cache_v2"));
		
		// 优先从内置存储中获取GUID
		mGuidBytes = mInnerGuidStorage.getGuidBytes();
		if (QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
			clearV1Data();
			
			// 内置存储正常，应该检查外置存储， 保证sdcard上面始终有备份
			StorageThread.get().getHandler().post(new Runnable() {
				@Override
				public void run() {
					if (mSdcardGuidStorage == null) {
						initSdcardGuidStorage();
						if (mSdcardGuidStorage != null
							&& !QRomWupDataBuilder.isGuidValidate(mSdcardGuidStorage.getGuidBytes())) {
							mSdcardGuidStorage.updateGuidBytes(mGuidBytes);
						}
					}
				}
			});
			
			return ;
		}
		
		// GUID读取失败， 尝试从外置存储中获取恢复
		initSdcardGuidStorage();
		if (mSdcardGuidStorage != null) {
			mGuidBytes = mSdcardGuidStorage.getGuidBytes();
			if (QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
				// 从外置存储卡恢复GUID，应该恢复内置存储
				mInnerGuidStorage.updateGuidBytes(mGuidBytes);
				clearV1Data();
				return ;
			}
		}
		
		// 数据迁移逻辑
		// 两个文件都不存在，试着获取前一个ROM版本文件数据， 如果能够获取，进行解析并迁移至新版本的存储中
		if (mV1RomUserCacheFile.exists()) {
			mGuidBytes = readGuidFromV1UserCache(mV1RomUserCacheFile);
			if (QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
				flushGuidBytes(mGuidBytes);
				return ;
			}
		}
		if (mV1RomSdcardCacheFile.exists()) {
			mGuidBytes = readGuidFromV1SdcardCache(mV1RomSdcardCacheFile);
			if (QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
				flushGuidBytes(mGuidBytes);
				return ;
			}
		}
		
		// 独立单发的时候，也可能产生GUID，直接复用
		if (mV1AppUserCacheFile.exists()) {
			mGuidBytes = readGuidFromV1UserCache(mV1AppUserCacheFile);
			if (QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
				flushGuidBytes(mGuidBytes);
				return ;
			}
		}
		if (mV1AppSdcardCacheFile.exists()) {
			mGuidBytes = readGuidFromV1SdcardCache(mV1AppSdcardCacheFile);
			if (QRomWupDataBuilder.isGuidValidate(mGuidBytes)) {
				flushGuidBytes(mGuidBytes);
				return ;
			}
		}
	}
	
	private void clearV1Data() {
		if (mV1RomUserCacheFile.exists()) {
			mV1RomUserCacheFile.delete();
		}
		if (mV1RomSdcardCacheFile.exists()) {
			mV1RomSdcardCacheFile.delete();
		}
		if (mV1AppUserCacheFile.exists()) {
			mV1AppUserCacheFile.delete();
		}
		if (mV1AppSdcardCacheFile.exists()) {
			mV1AppSdcardCacheFile.delete();
		}
	}
	
	@Override
	public byte[] getGuidBytes() {
		return mGuidBytes;
	}

	@Override
	public void updateGuidBytes(byte[] newGuidBytes) {
		mGuidBytes = newGuidBytes;
		flushGuidBytes(newGuidBytes);
	}
	
	public void flushGuidBytes(byte[] newGuidBytes) {
		mInnerGuidStorage.updateGuidBytes(newGuidBytes);
		if (mSdcardGuidStorage == null) {
			initSdcardGuidStorage();
			if (mSdcardGuidStorage != null) {
				mSdcardGuidStorage.updateGuidBytes(newGuidBytes);
			}
		} else {
			mSdcardGuidStorage.updateGuidBytes(newGuidBytes);
		}
	}
	
	private synchronized void initSdcardGuidStorage() {
		if (!FileUtil.isExternalStorageAvailable()) {
			return ;
		}
		if (mSdcardGuidStorage != null) {
			return ;
		}
		
		mSdcardGuidStorage = new EncryptGuidStorageByFile(
				new File(PathService.getSdcardPackageCacheDir(), "sdcard_guid_cache_v2")
				, true);
	}
	
	// 这部分代码从老版本中迁移过来， 尽量维持原装, 进行简单的类屏蔽
	
	private static final String STR_NULL = "null";
	private static final String SEPERATOR = "_";
	private static final String VER_PREFIX = "VERSION";
	
	private static class V1GuidUserCacheLoader {
		private static final int TYPE_GUID = 1;  
		private static final int TYPE_PHONEINFO = 8;
		
		private byte[] mVGUID;
		private String mPhoneInfo = "";
		
		public byte[] getGuidBytes() {
			return mVGUID;
		}
		
		public String getPhoneInfo() {
			return mPhoneInfo;
		}
		
		public int loadFromNewDataFileForVersion(File userFile) {
			DataInputStream dis = null;
	        int version = -3;
	        try {
	            dis = new DataInputStream(new FileInputStream(userFile));
	            // 获取文件头 -- 文件版本信息
	            String verStr = dis.readUTF();
	            String[] splitStr = verStr.split(SEPERATOR);
	            if (splitStr == null || splitStr.length< 2 
	            		||!VER_PREFIX.equals(splitStr[0])
	            		|| TextUtils.isEmpty(splitStr[1])) {
	            	QRomLog.w(TAG, "cache file is err ~~~~");
	            	return -2;
	            }
	            version = Integer.parseInt(splitStr[1].trim());
	            QRomLog.trace( TAG, "old cache file version = " + version);
	            
	            
	            if (version == 1) {  // VERSION 1 文件格式加载
	            	loadFileForVer1(dis);
	            }  else {  // 未匹配到版本信息
	                // 加载默认信息，如guid机型信息
	                loadFileForDefaultInfo(dis);                      
	                // 统计guid变化情况
	            }
	        } catch (Throwable e) {
	        	QRomLog.e(TAG, e.getMessage(), e);
	        } finally {
	        	try {
	                if (dis != null) {
	                    dis.close();
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        return version;
		}
		
		private void loadFileForVer1(DataInputStream dis) throws Exception {
			// 写文件时先写入的机型信息，然后写入guid
			int type = dis.readInt();
			if (type == TYPE_PHONEINFO) {
				mPhoneInfo = readStrInfo(dis);
			} else {
				QRomLog.e(TAG, "loadFileForVer1 failed, first type is not TYPE_PHONEINFO");
			}
			type = dis.readInt();
			if (type == TYPE_GUID) {
				mVGUID = readBytesInfo(dis);
			} else {
				QRomLog.e(TAG, "loadFileForVer1 failed, second type is not TYPE_GUID");
			}
		}
		
		private void loadFileForDefaultInfo(DataInputStream dis) throws Exception {
			// 列表类型
			int mapType = -1;
			boolean isGuidLoad = false;
			boolean isPhoneLoad = false;
			while (dis.available() > 0) {
				mapType = dis.readInt();
				QRomLog.i(TAG, "loadFileForDefaultInfo -- maptype = " + mapType);
				switch (mapType) {
				case TYPE_GUID:
					if (!isGuidLoad) {
						// 获取guid
						mVGUID = readBytesInfo(dis);
						isGuidLoad = true;
					}
					QRomLog.trace(TAG, "loadFileForDefaultInfo guid = " + StringUtil.byteToHexString(mVGUID));
					if (isPhoneLoad) { // 机型已加载完成
						QRomLog.trace(TAG,
								"loadFileForDefaultInfo -> default info load finish! ");
						return;
					}
					break;
				case TYPE_PHONEINFO:
					// 获取机型
					mPhoneInfo = readStrInfo(dis);
					if (!isPhoneLoad) {
						isPhoneLoad = true;
					}
					break;
				default:
					break;
				}
			}
			QRomLog.d(TAG, "loadFileForDefaultInfo -> finish~~~~~~~~~~");
		}
		
		private byte[] readBytesInfo(DataInputStream dis) throws Exception {
			if (dis == null) {
				return null;
			}
			int cnt = dis.readInt();
			QRomLog.i(TAG, "readBytesInfo -- cnt = " + cnt);
			byte[] datas = null;
			if (cnt > 0) {
				datas = new byte[cnt];
				dis.read(datas);
			}
			return datas;
		}
		
		private String readStrInfo(DataInputStream dis) throws Exception {
	    	if (dis == null) {
	            return null;
	        }
	    	return dis.readUTF();
	    }
	}
	
	private String getCurPhoneInfo() {
        String curPhonInfo = Build.MODEL;
        if (StringUtil.isEmpty(curPhonInfo)) {
            curPhonInfo =STR_NULL;
        } else {
            curPhonInfo = curPhonInfo.replace(SEPERATOR, "*");
        }
        return  curPhonInfo;
    }
	
	private byte[] readGuidFromV1UserCache(File file) {
		QRomLog.d(TAG, "readGuidFromV1UserCache file=" + file.getAbsolutePath());
		
		V1GuidUserCacheLoader loader = new V1GuidUserCacheLoader();
		loader.loadFromNewDataFileForVersion(file);
        // 获取当前手机信息
        String curPhone = getCurPhoneInfo();
        if (!StringUtil.isEmpty(curPhone) && !STR_NULL.equals(curPhone)
            && !StringUtil.isEmpty(loader.getPhoneInfo()) && !STR_NULL.equals(loader.getPhoneInfo())
            && !curPhone.equals(loader.getPhoneInfo())) {
        	return null;
        }
		return loader.getGuidBytes();
	}
	
	private static final byte[] MTT_KEY   = { (byte) 0x86, (byte) 0xf8, (byte) 0xe9, (byte) 0xac,
        (byte) 0x83, (byte) 0x71, (byte) 0x54, (byte) 0x63 };
	private static final String GUID_CACHE_VER = "VER1";
	
	private byte[] readGuidFromV1SdcardCache(File file) {
		QRomLog.i(TAG, "readGuidFromV1SdcardCache " + file.getAbsolutePath());
		byte[] guid = null;
		try {
			byte[] data = FileUtil.readFile(file);
			if (data == null || data.length <= 0) {
				QRomLog.w(TAG, "readGuidFromV1SdcardCache -> file is err ! " + file.getAbsolutePath());
				return null;
			}
			
			// 解密后数据
			guid = DES.DesEncrypt(MTT_KEY, data, DES.FLAG_DECRYPT);
			if (guid == null || guid.length <= 0) {
				// 解密失败，数据可能被篡改，删除文件
				QRomLog.w(TAG,
						"readGuidFromV1SdcardCache -> DesEncrypt  err ! " + file.getAbsolutePath());
				file.delete();
				return null;
			}

			String guidCache = new String(guid);

			QRomLog.trace(TAG, "readGuidFromV1SdcardCache guidCache = " + guidCache);
			if (StringUtil.isEmpty(guidCache)) {
				QRomLog.w(TAG, "guidCache  DesEncrypt err ");
				return null;
			}
			String[] values = guidCache.split(SEPERATOR);
			if (values == null || values.length == 0) { // 数据不合法
				QRomLog.w(TAG, "cache is err ");
				return null;
			}
			int n = values.length;

			if (n < 3) {
				QRomLog.w(TAG, "cache len is err : < 3 ");
				return null;
			}

			String guidStr = null;

			String ver = values[0];
			if (GUID_CACHE_VER.equals(ver)) { // 版本1
				guidStr = getLocalGuidForVer1(values);
			}

			guid = StringUtil.hexStringToByte(guidStr);
			QRomLog.i(TAG, "getGuidFromLocal -> guid = " + guid);
			if (guid == null
					|| guid.length != QRomWupConstants.WUP_GUID_DEFAULT_LEN) { // guid长度不对
				guid = null;
				// 数据可能被篡改，删除文件
				file.delete();
				QRomLog.trace(TAG,
						"readGuidFromV1SdcardCache -> guid len is err, delete cache");
				return null;
			}
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
		return guid;
	}
	
	private String getLocalGuidForVer1(String[] values) {
    	if (values == null || values.length < 3) {
    		return null;
    	}
    	
    	String phoneInfo = values[1]; 
        String guidStr = values[2];
        QRomLog.i(TAG, "getLocalGuidForVer1 ver guid = " + guidStr);
        
        String curPhone = getCurPhoneInfo();
        if (!StringUtil.isEmpty(curPhone) && !STR_NULL.equals(curPhone) 
                && !StringUtil.isEmpty(phoneInfo) &&  !STR_NULL.equals(phoneInfo)
                && !curPhone.equals(phoneInfo)) {  
            QRomLog.w(TAG, "getLocalGuidForVer1 -> phone info is not match");            
            return null;
        }
        
        return guidStr;
    }
    
	
}
