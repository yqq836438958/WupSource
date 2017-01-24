package qrom.component.wup.wupData;

import java.io.File;

import qrom.component.wup.QRomWupConstants;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;

import android.content.Context;
import android.os.Build;

import com.tencent.codec.des.DES;

public class QRomSdCacheInfo {

	private String TAG = "QRomSdCacheInfo";
	
	 private final String SEPERATOR = "_";
	 
	 private final String STR_NULL = "null";
	 
    // 本地加密密钥
    public static final byte[] MTT_KEY   = { (byte) 0x86, (byte) 0xf8, (byte) 0xe9, (byte) 0xac,
            (byte) 0x83, (byte) 0x71, (byte) 0x54, (byte) 0x63 };
	
    private String GUID_CACHE_VER = "VER1";
    
    private String mFileName;
    
	public QRomSdCacheInfo(String fileName) {
		
		mFileName = fileName;		
	}
	
    /**
     * 获取本地guid
     * @return
     */
    public byte[] getGuidFromLocal(Context context) {
        byte[] guid = null;
        // 当前sd缓存文件
        File curCacheFile = QWupFileUtil.getSdWupCacheInfoFile(context, mFileName);
        if (curCacheFile == null || !curCacheFile.exists()) {  // 当前版本的cache文件不存在
            QWupLog.trace( TAG, "cur sdFile is not exists: "
                      + (curCacheFile == null ? mFileName : curCacheFile.getAbsolutePath()));
            return null;
        } 
        guid = readLocalGuid(curCacheFile);
        return guid;
    }   
    
    /**
     * 若缓存guid文件不存在，则将guid写入sd卡缓存
     * @param sGUID
     */
    public void saveGuidToSdCardByCacheNoExists(byte[] guid, Context context) {
    	
    	if (!QRomWupInfo.isGuidValidate(guid)) {  // guid不合法
    		QWupLog.i(TAG, "-----saveGuidToSdCardByCacheNoExists guid err ------ ");
    		return;
    	}
        // 缓存文件
        File cacheFile = QWupFileUtil.getSdWupCacheInfoFile(context, mFileName);
        
        if (cacheFile != null && cacheFile.exists() && cacheFile.length() > 0) {  // 缓存文件存在
        	QWupLog.i(TAG, "-----saveGuidToSdCardByCacheNoExists -> cache file is exist ------ ");
        	return;
        }
        saveGuidToLocal(guid, context);

    }
    
    /**
     * 获取本地sd卡上缓存的guid信息
     * @param cacheFile
     * @return
     */
    private byte[] readLocalGuid(File cacheFile ) {
        QWupLog.i(TAG, "load cur cache : " + cacheFile.getAbsolutePath());
        byte[] guid = null;
        byte[] data = null;
        data = QWupFileUtil.read(cacheFile);        
        if (data == null || data.length <= 0) {
        	 QWupLog.w(TAG, "readLocalGuid -> file is err ! " + cacheFile.getAbsolutePath());
            return null;
        }
        
        try {
            // 解密后数据
            guid = DES.DesEncrypt(MTT_KEY, data, DES.FLAG_DECRYPT);
            if (guid == null || guid.length <= 0) {
                // 解密失败，数据可能被篡改，删除文件
            	QWupLog.w(TAG, "readLocalGuid -> DesEncrypt  err ! " + cacheFile.getAbsolutePath());
                cacheFile.delete();
                return null;
            }
            
            String guidCache = new String(guid);
            
            QWupLog.trace(TAG, 
                    " load getGuidFromLocal guidCache = " + guidCache);
            if (QWupStringUtil.isEmpty(guidCache)) {
                QWupLog.w(TAG, "guidCache  DesEncrypt err ");
                return null;
            }
           String[] values = guidCache.split(SEPERATOR);
            if (values== null || values.length == 0) { // 数据不合法
                QWupLog.w(TAG, "cache is err ");
                return null;
            }
            int n = values.length;
            
            if (n < 3) {
            	QWupLog.w(TAG, "cache len is err : < 3 ");
            	return null;
            }
            
            String guidStr = null;
            
            String ver = values[0];
            if (GUID_CACHE_VER.equals(ver)) {  // 版本1
                guidStr = getLocalGuidForVer1(values);
            }
            
            guid = QWupStringUtil.hexStringToByte(guidStr);
            QWupLog.i(TAG, "getGuidFromLocal -> guid = " + guid);
            if (guid == null || guid.length != QRomWupConstants.WUP_GUID_DEFAULT_LEN) {  // guid长度不对
            	guid = null;
                // 数据可能被篡改，删除文件
                cacheFile.delete();
                QWupLog.trace(TAG, "getGuidFromLocal -> guid len is err, delete cache" );
                return null;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return guid;
    }
    
    /**
     * 将guid保存到本地SD卡
     * @param guid
     * @return 
     */
    public boolean saveGuidToLocal(byte[] guid, Context context) {
        QWupLog.d(TAG, "-----saveGuidToLocal------");
        if (!QWupFileUtil.isExternalStorageAvailable()) {  // sd卡不可用
            QWupLog.d(TAG, "-----saveGuidToLocal sd card isnot used");
            return false;
        }
        
        if (guid == null || guid.length <= 0) {
            QWupLog.d(TAG, "-----saveGuidToLocal guid is not ok，donot save");
            return false; 
        }   
        
        // 当前缓存文件
        File cacheFile = QWupFileUtil.getSdWupCacheInfoFile(context, mFileName);
        
        String phone = getCurPhoneInfo();
        // 保存缓存的guid机型信息
        String guidCache = GUID_CACHE_VER 
                + SEPERATOR +  phone
                + SEPERATOR +  QWupStringUtil.byteToHexString(guid) ;        
        
        boolean res = false;
        try {
            res = QWupFileUtil.save(cacheFile, DES.DesEncrypt(MTT_KEY, 
                    guidCache.getBytes(),  DES.FLAG_ENCRYPT));
            QWupLog.i(TAG, "-----saveGuidToLocal guid save finish------ " + guidCache);
        } catch (Exception e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }
    
    private String getLocalGuidForVer1(String[] values) {
    	if (values == null || values.length < 3) {
    		return null;
    	}
    	
    	String phoneInfo = values[1]; 
        String guidStr = values[2];
        QWupLog.i(TAG, "new ver guid = " + guidStr);
        
        String curPhone = getCurPhoneInfo();
        if (!QWupStringUtil.isEmpty(curPhone) && !STR_NULL.equals(curPhone) 
                && !QWupStringUtil.isEmpty(phoneInfo) &&  !STR_NULL.equals(phoneInfo)
                && !curPhone.equals(phoneInfo)) {  // 缓存机型信息合法, 但当前机型和缓存机型不匹配，guid无效  
            QWupLog.w(TAG, "getLocalGuidForVer1 -> phone info is not match");            
            return null;
        }
        
        return guidStr;
    }
    
    private String getCurPhoneInfo() {
        String curPhonInfo = Build.MODEL;
        if (QWupStringUtil.isEmpty(curPhonInfo)) {
            curPhonInfo =STR_NULL;
        } else {
            curPhonInfo = curPhonInfo.replace(SEPERATOR, "*");
        }
        return  curPhonInfo;
    }
}
