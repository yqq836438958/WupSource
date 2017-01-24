package qrom.component.wup.wupData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import qrom.component.wup.build.QRomWupBuildInfo;
import qrom.component.wup.stat.QRomWupStatEngine;
import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;


public class QRomWupInfo {
    
    private static final String TAG = "QRomWupInfo";
    
    /**
     * 重要！！！！
     *  当前版本号（最终文件中保存的版本信息格式为VERSION_1_...），修改该文件数据，必须对应修改版本号。
     *  VERSION:1 -- 初始版本号
     */
    private static final int CUR_VERSION = 1;

    private String SEPERATOR = "_";
    private String STR_NULL = "null";
    private String VER_PREFIX = "VERSION";

    /** 默认guid byte[] */
    public static final byte[] DEFAULT_GUID_BYTES = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    /** 默认guid 即未获取到guid的时候 */
    public static final String DEFAULT_GUID_STR = QWupStringUtil.byteToHexString(DEFAULT_GUID_BYTES);
    /** 默认guid长度 */
    public static final int DEFAULT_GUID_LEN = DEFAULT_GUID_BYTES.length;
    /** guid 基本长度 */
    public static final int DEFAULT_GUID_BASE_LEN = 4;
    
    private static final int TYPE_GUID = 1;    
    @Deprecated
    private static final int TYPE_PROXY = 3;
    @Deprecated
    private static final int LIST_PROXY = 1;
    @Deprecated
    private static final int TYPE_WUPSOCKET = 4;
    @Deprecated
    private static final int LIST_WUPSOCKET = 2;
    
    private static final int TYPE_PHONEINFO = 8;   
    
    /** iplist代理信息 数据类型 */
    public static final int TYPE_PROXY_IPLIST_DATA = 10;
    /** iplist wifi下 代理信息 数据类型 */
    public static final int TYPE_PROXY_WIFI_IPLIST_DATA = 11;    
    
    /** iplist代理信息 数据类型 */
    public static final int TYPE_WUP_SOCKET_IPLIST_DATA = 20;
    /** socket wifi对应bssid iplist缓存 */
    public static final int TYPE_WUP_SOCKET_WIFI_IPLIST_DATA = 21;
    
    
    private byte[] mVGUID;
    
    /** 对应接入点缓存的iplist 信息 */
    private SparseArray<QRomIplistData>  mProxyIpDatas = new SparseArray<QRomIplistData>();
    /** 对应wifi接入点缓存的iplist 信息 */
    private Map<String, QRomIplistData>  mWifiProxyIpDatas = new HashMap<String, QRomIplistData>();
    
    /** 对应接入点缓存的socket iplist 信息 */
    private SparseArray<QRomIplistData>  mWupSocketIpDatas = new SparseArray<QRomIplistData>();
    /** 对应wifi接入点缓存的socket iplist 信息 */
    private Map<String, QRomIplistData>  mWifiWupSocketIpDatas = new HashMap<String, QRomIplistData>();

//    /** wup 代理地址 缓存列表 */
//    private SparseArray<List<String>> mProxyInfos = new SparseArray<List<String>>();
//    /** wup socket 代理地址缓存列表 */
//    private SparseArray<List<String>> mWupSocketInfos = new SparseArray<List<String>>();
    
//    /** wup代理地址更新时间 */
//    private SparseArray<Long> mProxyInfosUpdataTime = new SparseArray<Long>();
//    /** wup代理地址请求时client ip */
//    private SparseArray<String> mProxyInfosReqClientIp = new SparseArray<String>();
    
//    /** wup SOCKET 代理地址更新时间 */
//    private SparseArray<Long> mSocketInfosUpdataTime = new SparseArray<Long>();
//    /** wup SOCKET代理地址请求时client ip */
//    private SparseArray<String> mSocketInfosReqClientIp = new SparseArray<String>();
    
//    /** wifi下不同代理接入点缓存 bssid - iplist */
//    private Map<String, List<String>> mWifiProxyInfolistInfos = new HashMap<String, List<String>>(5);
//    /** wifi下不同代理接入点缓存时间 bssid - time */
//    private Map<String, Long> mWifiProxyInfoUpdataTime = new HashMap<String, Long>(5);
    
//    /** wifi下不同socket代理接入点缓存 */
//    private Map<String, List<String>> mWifiSocketInfolistInfos = new HashMap<String, List<String>>(5);
//    /** wifi下不同socket代理接入点缓存时间 bssid - time */
//    private Map<String, Long> mWifiSocketInfoUpdataTime = new HashMap<String, Long>(5);
    
    /**wifi下最大缓存个数 */
    private int M_WIFI_IPCACHE_CNT = 5;
    
    /** 缓存文件机型信息 */
    private String mPhoneInfo = "";
    
    /** wup 数据缓存文件名 */
    private String mWupFileName = null;
    private String mWupSdFileName = null;
    /** sd卡缓存文件处理对象 */
    private QRomSdCacheInfo mSdCacheInfo;
    
    /** 文件上次修改时间 */
    private long mLastTime = -1;
    
    public QRomWupInfo(String wupFileName, String wupSdFileName) {    
        mWupFileName = wupFileName;
        mWupSdFileName = wupSdFileName;
    }
    
    public synchronized void reSet() {
        QWupLog.w(TAG, "reSet");
        mVGUID = DEFAULT_GUID_BYTES;
        clearIpInfos();
    }
    
    public void clearIpInfos() {
    	mProxyIpDatas.clear();
        mWupSocketIpDatas.clear();
        mWifiWupSocketIpDatas.clear();
    }
        
    public synchronized void reload(Context context) {
        
        long fileTime = getCurWupUserInfoFile(context).lastModified();
        if (mLastTime != fileTime) {      
            QWupLog.trace(TAG, "reload->file is changed!");
            reSet();
            load(context);
            mLastTime = fileTime;
        }
    }

    /**
     * 加载缓存文件
     * @param context
     */
    public synchronized void load(Context context) {
        QWupLog.d(TAG, "load start : " + mWupFileName);
        int version = -100;
        // 获取缓存文件
        File userFile = getCurWupUserInfoFile(context);
        if (userFile != null && userFile.exists()) {
            // 读取新版本信息文件
            version = loadFromNewDataFileForVersion(context, userFile);
            // 获取当前手机信息
            String curPhone = getCurPhoneInfo();
         
            // 校验guid是否合法
            if (isGuidValidate(getGuidBytes()) 
                    && !QWupStringUtil.isEmpty(curPhone) && !STR_NULL.equals(curPhone)
                    && !QWupStringUtil.isEmpty(mPhoneInfo) && !STR_NULL.equals(mPhoneInfo)
                    && !curPhone.equals(mPhoneInfo)) {  // guid有效，但机型不匹配
                // 统计guid变化情况
                Map<String, String> statInfo = new HashMap<String, String>(2);
                statInfo.put("GUID_0", QWupStringUtil.byteToHexString(getGuidBytes()));
                statInfo.put("GUID_PHONE", curPhone+"_"+mPhoneInfo);
                QRomWupStatEngine.getInstance().statWupRequestInfoForce(context, statInfo);
                setSGUID(null, context);                            
                QWupLog.trace(TAG,  "load->phone is not match guid set null! cru: " + curPhone 
                        + ", cache phone: " + mPhoneInfo);
            }
        }
        
        if (mSdCacheInfo != null) {
            
            // 未从应用data缓存文件获取guid，则从sd卡中读缓存
            if (!isGuidValidate(getGuidBytes())) {  // guid不合法
                byte[] localGuid = null;
                localGuid = mSdCacheInfo.getGuidFromLocal(context);
                QWupLog.trace(TAG, "load -> getGuidFromLocal = " +  QWupStringUtil.byteToHexString(localGuid));
                mVGUID = localGuid;
            }
            // 将guid缓存到sd卡文件里
            mSdCacheInfo.saveGuidToSdCardByCacheNoExists(mVGUID, context);        
        }
        
        if (mVGUID == null) {
            mVGUID = DEFAULT_GUID_BYTES;
        }
     
        // 缓存文件版本不是当前版本且guid合法
        if (version > 0 && version != CUR_VERSION && isGuidValidate(mVGUID)) {  
            // 按当前文件格式保存缓存文件
            saveWupInfo(context);
        }
        
        QWupLog.trace(TAG, "load  finish fileVersion = " + version + ", guid =  " + getGuidStr() 
                + ", pkg name = " + (context == null ? "null" : context.getPackageName()));
    }

    
    /**
     * 同步用户数据到本地缓存
     *   -- data下缓存文件不存在且guid合法,则将内存信息写入缓存
     */
    public void syncWupInfo(Context context) {
        
        if (QRomWupInfo.isGuidValidate(getGuidBytes())) {  // guid合法
            // 用户缓存信息
            File file = getCurWupUserInfoFile(context);
            if (file != null && !file.exists()) {  // 文件不存在
                QWupLog.trace(TAG, "  syncWupInfo 将内存数据写入缓存");
                saveWupInfo(context);
                if (mSdCacheInfo != null) {
                    mSdCacheInfo.saveGuidToLocal(getGuidBytes(), context);
                }
            } else {
                QWupLog.d(TAG, "  syncWupInfo 用户信息文件存在，不重复保存" );
                if (mSdCacheInfo != null) {
                    mSdCacheInfo.saveGuidToSdCardByCacheNoExists(getGuidBytes(), context);
                }
            }
        } else {
            QWupLog.trace(TAG, "  syncWupInfo guid不合法，不重复保存" );
        }
    }
    
    /**
     * 加载文件格式为版本1的数据信息
     * @param dis
     * @throws Exception
     */
    private void loadFileForVer1(DataInputStream dis) throws Exception {

    	if (dis == null) {
    		QWupLog.w(TAG, "loadFileForVer1 -> io is null ");
    		return;
    	}
    	
        // 列表类型
        int mapType = -1;
        boolean isGuidLoad = false;
        while (dis.available() > 0) {
        	mapType = dis.readInt(); 
        	QWupLog.i(TAG, "loadFileForVer1 -- maptype = " + mapType);
        	switch (mapType) {
        	case TYPE_GUID:
        	    if (!isGuidLoad) {        	        
        	        // 获取guid
        	        mVGUID = readBytesInfo(dis);
        	        isGuidLoad = true;
        	    }
                QWupLog.trace( TAG, 
                        "loadFileForVer1 guid = " + QWupStringUtil.byteToHexString(mVGUID));
        		break;
        	case TYPE_PHONEINFO:
        	    // 获取机型
        	    mPhoneInfo = readStrInfo(dis);
        	    break;        	
        	 // ↓↓↓↓↓↓↓↓↓↓新加数据类型-- 2015.1.6↓↓↓↓↓↓↓↓↓↓↓↓ 
    	  case TYPE_PROXY_IPLIST_DATA:  // 接入点iplist相关信息  -- by 2015.1.6
              mProxyIpDatas = readMapIplistInfo(dis, mProxyIpDatas, TYPE_PROXY_IPLIST_DATA);
              break;
          case TYPE_WUP_SOCKET_IPLIST_DATA:  // 接入点socket iplist相关信息 -- by 2015.1.6
              mWupSocketIpDatas = readMapIplistInfo(dis, mWupSocketIpDatas, TYPE_WUP_SOCKET_IPLIST_DATA);
              break;
              
          case TYPE_PROXY_WIFI_IPLIST_DATA: // wifi 接入点iplist相关信息  -- by 2015.1.6
              mWifiProxyIpDatas = readWifiMapIplistInfo(dis, mWifiProxyIpDatas, TYPE_PROXY_WIFI_IPLIST_DATA);
              break;
          case TYPE_WUP_SOCKET_WIFI_IPLIST_DATA: // wifi 接入点socket iplist相关信息 -- by 2015.1.6
              mWifiWupSocketIpDatas = readWifiMapIplistInfo(dis, mWifiWupSocketIpDatas, TYPE_WUP_SOCKET_WIFI_IPLIST_DATA);
              break;          
              // ↑↑↑↑↑↑↑↑↑↑↑↑↑新加数据类型-- 2015.1.6↑↑↑↑↑↑↑↑↑↑↑↑↑
        		
              // 老数据兼容操作不读取iplist，按新协议重新拉取对应iplist信息 -- 2015.1.6 
        	case TYPE_PROXY:
        		// 获取proxy list
                readMapInfo(dis, null, LIST_PROXY);
        		break;
        	case TYPE_WUPSOCKET:
        		// wupsocket list
                readMapInfo(dis, null, LIST_WUPSOCKET);
        		break;
        	default:
        	}
        }
        QWupLog.d(TAG, "loadFileForVer1 -> finish~~~~~~~~~~ " + getGuidStr());    
    }
    
    
    /**
     * 加载文件格式为默认兼容数据<p>
     *    -- 普通情况仅兼容guid信息
     * @param dis
     * @throws Exception
     */
    private void loadFileForDefaultInfo(DataInputStream dis) throws Exception {

        if (dis == null) {
            QWupLog.w(TAG, "loadFileForDefaultInfo -> io is null ");
            return;
        }
        
        // 列表类型
        int mapType = -1;
        boolean isGuidLoad = false;
        boolean isPhoneLoad = false;
        while (dis.available() > 0) {
            mapType = dis.readInt(); 
            QWupLog.i(TAG, "loadFileForDefaultInfo -- maptype = " + mapType);
            switch (mapType) {
            case TYPE_GUID:
                if (!isGuidLoad) {                  
                    // 获取guid
                    mVGUID = readBytesInfo(dis);
                    isGuidLoad = true;
                }
                QWupLog.trace( TAG, 
                        "loadFileForDefaultInfo guid = " + QWupStringUtil.byteToHexString(mVGUID));
                if (isPhoneLoad) {  // 机型已加载完成
                    QWupLog.trace( TAG, 
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
        QWupLog.d(TAG, "loadFileForDefaultInfo -> finish~~~~~~~~~~ " + getGuidStr());    
    }
     
            
    /**
     * 根据版本号获取信息
     * @param userFile
     * 
     * @return 缓存文件版本信息
     */
    private int loadFromNewDataFileForVersion(Context context, File userFile) {
    	if (!userFile.exists()) {
            return -1;
        }
        DataInputStream dis = null;
        int version = -3;
        try {
            dis = new DataInputStream(
                    QWupFileUtil.openInputStream(userFile));
            // 获取文件头 -- 文件版本信息
            String verStr = dis.readUTF();
            String[] splitStr = verStr.split(SEPERATOR);
            if (splitStr == null || splitStr.length< 2 
            		||!VER_PREFIX.equals(splitStr[0])
            		|| TextUtils.isEmpty(splitStr[1])) {
            	QWupLog.w(TAG, "cache file is err ~~~~");
            	 // 统计guid变化情况
                Map<String, String> statInfo = new HashMap<String, String>(2);
                statInfo.put("GUID_FILE", "versionErr:"+ verStr);
                QRomWupStatEngine.getInstance().statWupRequestInfoForce(context, statInfo);
            	return -2;
            }
            version = Integer.parseInt(splitStr[1].trim());
            QWupLog.trace( TAG, "cache file version = " + version);
            if (version == 1) {  // VERSION 1 文件格式加载
            	loadFileForVer1(dis);
            }  else {  // 未匹配到版本信息
                // 加载默认信息，如guid机型信息
                loadFileForDefaultInfo(dis);                      
                // 统计guid变化情况
                Map<String, String> statInfo = new HashMap<String, String>(2);
                statInfo.put("GUID_FILE", "versionErr, no ver1:"+ verStr);
                QRomWupStatEngine.getInstance().statWupRequestInfoForce(context, statInfo);
            }
            
        } catch (Throwable e) {
            e.printStackTrace();
            QWupLog.w(TAG, e);
            clearIpInfos();
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
    
    
    private String readStrInfo(DataInputStream dis) throws Exception {
    	if (dis == null) {
            return null;
        }
    	return dis.readUTF();
    }
    
    private void writeStrInfo(DataOutputStream dos, int type, String str) throws Exception {
    	 if (dos == null) {
             return;
         }
    	 if (str == null) {
    		 str ="";
    	 }
         dos.writeInt(type);
         dos.writeUTF(str);
    }
    
    private byte[] readBytesInfo(DataInputStream dis) throws Exception {
    	if (dis == null) {
            return null;
        }
    	int cnt = dis.readInt();
    	QWupLog.i(TAG, "readBytesInfo -- cnt = " + cnt);
    	byte[] datas = null;
    	if (cnt > 0) {
    		datas = new byte[cnt];
    		dis.read(datas);
    	}
    	return datas;
    }
    
    private void writeBytesInfo(DataOutputStream dos, int type, byte[] datas)  throws Exception {
    	  if (dos == null) {
              return;
          }
          dos.writeInt(type);
          int cnt = datas == null ? 0 : datas.length;
          QWupLog.i(TAG, "writeBytesInfo -- type =  " + type + ", cnt = " + cnt);
          dos.writeInt(cnt);
          if (cnt > 0) {
        	  dos.write(datas);
          }
    }
    
    /**
     * 读取list数据
     *    -- 先读取list的长度（int） 然后读取对应长度的string类型
     * @param dis           DataInputStream (若为空，不做任何处理，返回null)
     * @param tempList  传入的list容器
     * @return  tempList  将新的数据重置到tempList中（先清空原list，然后赋值，若传入为空，则初始化一个新的list）
     * @throws Exception
     */
    private List<String>  readListInfo(DataInputStream dis, List<String> tempList) throws Exception {
    	
    	if (dis == null) {
            return null;
        }
    	
    	if (tempList == null) {
    		tempList = new ArrayList<String>(3);
    	}
    	int proxyServerCnt = dis.readInt();
        tempList.clear();
        for (int i = 0; i < proxyServerCnt; i++) {
        	String url = dis.readUTF();
        	if (url != null && url.length() > 0) {
        		tempList.add(url);
        	}
        }

        QWupLog.trace( TAG, "readListInfo -- list = " + tempList);
        return tempList;
    }
    
    /**
     * @param mSGUID the mSGUID to set
     */
    public void setSGUID(byte[] sGUID, Context context) {
        if (sGUID != null && mSdCacheInfo != null) {
           // guid改变则保持新guid
            // 保存guid到sd卡
            mSdCacheInfo.saveGuidToLocal(sGUID, context);
        }
        if (sGUID == null) {
            sGUID = DEFAULT_GUID_BYTES;
        }
        //this.mUserInfo.vGUID = sGUID;       
        //this.mStrGUID = QubeStringUtil.toHexString(sGUID);
        mVGUID = sGUID;
    }
    
    
    public synchronized boolean saveWupInfo(Context context) {
        
        File userFile = getCurWupUserInfoFile(context);
        QWupLog.d(TAG, "userinfo save start~~~~~~~~~~ ");
        
        DataOutputStream dos = null;
        try {
            
            if (!userFile.exists()) {
                userFile.createNewFile();
            }
            dos = new DataOutputStream(QWupFileUtil.openOutputStream(userFile));
            // 写入版本号信息
            String verStr = VER_PREFIX + SEPERATOR + CUR_VERSION;
            dos.writeUTF(verStr);
            
            // 保存机型信息
            writeStrInfo(dos, TYPE_PHONEINFO, getCurPhoneInfo());
            
            // 保存guid
            writeBytesInfo(dos, TYPE_GUID, mVGUID);    

            // ----- 2015.1.6 增加缓存接入点缓存时间，及对应接入点iplist获取的client ip         
            // 保存wup代理地址相关信息
            writeMapIplistDataInfo(dos, TYPE_PROXY_IPLIST_DATA, mProxyIpDatas);
            // 保存wup socket代理地址信息
            writeMapIplistDataInfo(dos, TYPE_WUP_SOCKET_IPLIST_DATA, mWupSocketIpDatas);
            
            QWupLog.d(TAG, "saveWupInfo-> 更新wifi proxy 代理地址 ");
            updateWifiIpListCache(mWifiProxyIpDatas);
            // 保存wup wifi 代理地址相关信息
            writeWifiMapIplistDataInfo(dos, TYPE_PROXY_WIFI_IPLIST_DATA, mWifiProxyIpDatas);
            
            QWupLog.d(TAG, "saveWupInfo-> 更新wifi socket  代理地址 ");
            updateWifiIpListCache(mWifiWupSocketIpDatas);
            // 保存wup wifi socket代理地址信息
            writeWifiMapIplistDataInfo(dos, TYPE_WUP_SOCKET_WIFI_IPLIST_DATA, mWifiWupSocketIpDatas);
            
//            // 保存wup代理地址更新时间
//            writeMapLongOfType(dos, TYPE_PROXY_UPDATA_TIME, mProxyInfosUpdataTime);
//            // 保存iplist的client ip
//            writeMapStrOfType(dos, TYPE_PROXY_CLIENT_IP, mProxyInfosReqClientIp);
//            
//            // 保存wup socket代理地址更新时间
//            writeMapLongOfType(dos, TYPE_SOCKET_UPDATA_TIME, mSocketInfosUpdataTime);
//            // 保存iplist socket 的client ip
//            writeMapStrOfType(dos, TYPE_SOCKET_CLIENT_IP, mSocketInfosReqClientIp);
//            
//            
//            writeWifiIpListOfBssid(dos, TYPE_PROXY_WIFI_IPLIST, mWifiProxyInfolistInfos, mWifiProxyInfoUpdataTime, mWifiProxyInfosReqClientIp);
//            
//            QWupLog.d(TAG, "saveWupInfo-> 更新wifi socket  代理地址 ");
//            updateWifiIpListCache(mWifiSocketInfolistInfos, mWifiSocketInfoUpdataTime);
//            writeWifiIpListOfBssid(dos, TYPE_SOCKET_WIFI_IPLIST, mWifiSocketInfolistInfos, mWifiSocketInfoUpdataTime, mWifiSocketInfosReqClientIp);
            
            QWupLog.d(TAG, "userinfo save end~~~~~~~~~~");
            return true;
        } catch (Throwable e) {
            QWupLog.e(TAG, e);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                    dos = null;
                } catch (Exception e) {
                    QWupLog.w(TAG, e);
                }
            }
        }
        
        return false;
    }
    
    /**
     * 获取当前user文件
     * @param context
     * @return
     */
    private File getCurWupUserInfoFile(Context context) {
    	QWupLog.d(TAG, "getCurWupUserInfoFile -> " + mWupFileName);

    	File  userFile = null; 
    	if (!QRomWupBuildInfo.isSysRomSrcMode(context)) {  // 非系统rom源码集成模式
    	    
    	    userFile =  QWupFileUtil.getWupUserInfoFile(context, mWupFileName);
    	    if (mSdCacheInfo == null) {  // 初始化sdk缓存读取对象
    	        mSdCacheInfo = new QRomSdCacheInfo(mWupSdFileName);
    	    }
    	} else {    	    
    	    userFile = QWupFileUtil.getWupUserInfoFileForSysRomSrc(mWupFileName);
    	    QWupLog.trace(TAG, "getCurWupUserInfoFile -> getWupUserInfoFileForSysRomSrc ");
    	}
    	QWupLog.trace(TAG, "getCurWupUserInfoFile ->  " + userFile.getAbsolutePath()
    	        + ", sdCacheFile = " + mWupSdFileName);
    	return userFile;
    }
    /**
     * 获取指定类型的wup代理列表
     * @param apnTypeIndex
     * @return
     */
    public List<String> getProxyByType(int apnTypeIndex) {
    	return getIpListFromCaches(apnTypeIndex, mProxyIpDatas);
    }
    
    /**
     * 获取对应接入点类型的wup代理信息
     * @param apnIndex  当前接入点索引
     * @return  QRomIplistData
     */
    public QRomIplistData getProxyIpListDataByType(int apnIndex) {
        return mProxyIpDatas.get(apnIndex);
    }
    
    /**
     * 获取wifi对应bssid接入点wup代理地址
     * @param bssid   wifi对应的bssid
     * @return  QRomIplistData
     */
    public QRomIplistData getProxyIpListDataByBssid(String bssid) {
        return mWifiProxyIpDatas.get(bssid);
    }
    
    /**
     * 获取对应接入点类型的wup socket代理信息
     * @param apnIndex  当前接入点索引
     * @return QRomIplistData
     */
    public QRomIplistData getWupSocketIpListDataByType(int apnIndex) {
        return mWupSocketIpDatas.get(apnIndex);
    }
    
    /**
     * 获取wifi对应bssid接入点wup socket代理地址
     * @param bssid  wifi对应的bssid
     * @return QRomIplistData
     */
    public QRomIplistData getWupSocketIpListDataByBssid(String bssid) {
        return mWifiWupSocketIpDatas.get(bssid);
    }
    
    /**
     * 获取指定类型的wup socket代理列表
     * @param apnTypeIndex
     * @return
     */
    public List<String> getSocketProxyByType(int apnTypeIndex) {
        return getIpListFromCaches(apnTypeIndex, mWupSocketIpDatas);
    }
    
    private List<String> getIpListFromCaches(int index, SparseArray<QRomIplistData> datas) {
        if (datas == null || datas.size() == 0) {
            return null;
        }
        QRomIplistData iplistData = datas.get(index);
        return iplistData == null ? null : iplistData.getIplistInfo();
    }
    
    /**
     * 根据bssid获取对应缓存列表地址
     * @param bssid
     * @return
     */
    public List<String> getProxyByBsssid(String bssid) {
        return getIplistInfByBssid(bssid, mWifiProxyIpDatas);
    }
    
    /**
     * 根据bssid获取对应iplist缓存信息
     * @param bssid
     * @param datas
     * @return
     */
    private List<String> getIplistInfByBssid(String bssid, Map<String, QRomIplistData> datas) {
        if (datas == null || datas.isEmpty() || bssid == null) {
            return null;
        }
        
        QRomIplistData iplistData = datas.get(bssid);
        return iplistData == null ? null : iplistData.getIplistInfo();
    }
    
    /**
     * 刷新指定列表数据
     * @param apnTypeIndex
     * @param list
     */
    public void refreshProxyListByType(int apnTypeIndex, List<String> list, String clientIp) {
        
        QRomIplistData iplistData = mProxyIpDatas.get(apnTypeIndex);
        
        if (iplistData == null) {
            iplistData = new QRomIplistData(apnTypeIndex, list, 
                    TYPE_PROXY_IPLIST_DATA, System.currentTimeMillis(), clientIp);
            mProxyIpDatas.put(apnTypeIndex, iplistData);
            return;
        } 
        
    	iplistData.refreshIplistInfo(list, clientIp, System.currentTimeMillis());
    }
           
    /**
     * 根据apn 对应的缓存index获取proxy iplist缓存时间
     * @param apnIndex  对应apn缓存的index
     * @return  long 缓存iplist时间
     */
    public long getProxyIplistUpdateTimeByApnIndex(int apnIndex) {
        QRomIplistData iplistData = mProxyIpDatas.get(apnIndex);
        if (iplistData != null) {
            return iplistData.getIplistUpdateTime();
        }
        return 0;
    }
    
//    /**
//     * 根据apn 对应wifi的bssid缓存获取proxy iplist缓存时间
//     * @param bssid  对应wifi下bssid
//     * @return  long 缓存iplist时间
//     */
//    public long getProxyIplistUpdateTimeByBssid(String bssid) {
//        Long time = mWifiProxyInfoUpdataTime.get(bssid);
//        return time == null ? -1 : time;
//    }
    
//    /**
//     * 根据apn 对应wifi的bssid缓存获取socket iplist缓存时间
//     * @param bssid  对应wifi下bssid
//     * @return  long 缓存iplist时间
//     */
//    public long getSocketIplistUpdateTimeByBssid(String bssid) {
//        Long time = mWifiSocketInfoUpdataTime.get(bssid);
//        return time == null ? -1 : time;
//    }
    
    /**
     *  根据apn 对应的缓存index获取socket iplist缓存时间
     * @param apnIndex  对应apn缓存的index
     * @return  long 缓存socket iplist时间
     */
    public long getSocektIplistUpdateTimeByApnIndex(int apnIndex) {
        QRomIplistData iplistData = mWupSocketIpDatas.get(apnIndex);
        if (iplistData != null) {
            return iplistData.getIplistUpdateTime();
        }
        return 0;
    }
    
//    /**
//     * 获取请求wup代理地址时，client ip
//     * @param apnIndex   对应apn缓存的index
//     * @return  String
//     */
//    public String getProxyIplistUpadteClientIp(int apnIndex) {
//        return mProxyInfosReqClientIp.get(apnIndex);
//    }
//    
//    /**
//     * 获取请求wup socket代理地址时，client ip
//     * @param apnIndex   对应apn缓存的index
//     * @return  String
//     */
//    public String getSocektIplistUpadteClientIp(int apnIndex) {
//        return mSocketInfosReqClientIp.get(apnIndex);
//    }
    
    /**
     * 获取缓存的wup 列表不为空的类型个数
     * @return
     */
    public int getProxyListTypeCnt() {
    	int n = 0;
    	for (int i = 0; i < mProxyIpDatas.size(); i++) {
			if (mProxyIpDatas.valueAt(i) != null && !mProxyIpDatas.valueAt(i).isEmpty()) {
				n++;
			}
		}
    	return n;
    }
    
    /**
     * 刷新指定socket proxy列表数据
     * @param apnTypeIndex
     * @param list
     */
    public void refreshSocketProxyListByType(int apnTypeIndex, List<String> list, String clientIp) {

        QRomIplistData iplistData = mWupSocketIpDatas.get(apnTypeIndex);
        
        if (iplistData == null) {
            iplistData = new QRomIplistData(apnTypeIndex, list, 
                    TYPE_WUP_SOCKET_IPLIST_DATA, System.currentTimeMillis(), clientIp);
            mWupSocketIpDatas.put(apnTypeIndex, iplistData);
            return;
        }
        
        iplistData.refreshIplistInfo(list, clientIp, System.currentTimeMillis());
    }
    
    /**
     * 更新对应bssid的缓存列表
     * @param bssid
     * @param list
     */
    public void refreshWifiProxyListByBssid(String bssid, List<String> list, String ip) {
        
        if (QWupStringUtil.isEmpty(bssid)) {  // bssid为空
            QWupLog.w(TAG, "refreshWifiProxyListByBssid-> req info is err");
            return;
        }
        
        QRomIplistData iplistData = mWifiProxyIpDatas.get(bssid);
        if (iplistData == null) {
            iplistData = new QRomIplistData(bssid, list, TYPE_PROXY_WIFI_IPLIST_DATA,
                    System.currentTimeMillis(), ip);
            mWifiProxyIpDatas.put(bssid, iplistData);
            return;
        } 
        
        iplistData.refreshIplistInfo(list, ip, System.currentTimeMillis());
    }
    
    /**
     * 更新对应bssid的socket缓存列表
     * @param bssid
     * @param list
     */
    public void refreshWifiSocketListByBssid(String bssid, List<String> list, String ip) {
        
        if (QWupStringUtil.isEmpty(bssid)) {  // bssid为空
            QWupLog.w(TAG, "refreshWifiSocketListByBssid-> req info is err");
            return;
        }
        
        QRomIplistData iplistData = mWifiWupSocketIpDatas.get(bssid);
        if (iplistData == null) {
            iplistData = new QRomIplistData(bssid, list, TYPE_WUP_SOCKET_WIFI_IPLIST_DATA,
                    System.currentTimeMillis(), ip);
            mWifiWupSocketIpDatas.put(bssid, iplistData);
            return;
        } 
        
        iplistData.refreshIplistInfo(list, ip, System.currentTimeMillis());
    }
    
    /**
     * 更新wif下iplist缓存数据，保持最大5个wifi list缓存
     */
    private void updateWifiIpListCache(Map<String, QRomIplistData> wifiCaches) {
        if (wifiCaches == null || wifiCaches.isEmpty()) {
            QWupLog.i(TAG, "updateWifiIpListCache-> cache is empty");
            return;
        }
        
        if (wifiCaches.size() <= M_WIFI_IPCACHE_CNT) {  // 缓存未达到最大限制
            QWupLog.trace(TAG, "updateWifiIpListCache-> cache is not max，not delete!");
            return;
        }        

        // 删除的缓存个数
        int delCnt = wifiCaches.size() - M_WIFI_IPCACHE_CNT;  
 
        List<Entry<String, QRomIplistData>> listTimes = new ArrayList<Map.Entry<String,QRomIplistData>>(wifiCaches.entrySet());
        // 升序排序
        Collections.sort(listTimes, new Comparator<Map.Entry<String, QRomIplistData>>() {
            @Override
            public int compare(Entry<String, QRomIplistData> lhs, Entry<String, QRomIplistData> rhs) {
                
                if (lhs == null || lhs.getValue() == null) {
                    return -1;
                }
                if (rhs == null || rhs.getValue() == null) {
                    return 1;
                }
                
                long ltime = lhs.getValue().getIplistUpdateTime();
                long rtime = rhs.getValue().getIplistUpdateTime();                    
                
                return (int)(ltime - rtime);
            }            
        });
        
        // 删除时间最早的缓存
        for (int i = 0; i < delCnt; i++) {
            if (listTimes.get(i) == null || listTimes.get(i).getKey() == null) {
                continue;
            }
            // 删除指定数据
            wifiCaches.remove(listTimes.get(i).getKey());
        }
    }
    
    /**
     * 将iplist相关信息写入缓存文件<p>
     *    -- 写入格式  0: 缓存类型; 1: 缓存数据长度; 2: 每个QRomIplistData缓存数据
     * @param dos                DataOutputStream
     * @param mapType       缓存类型
     * @param datas             相关iplist缓存信息 SparseArray<QRomIplistData>
     * @throws Exception
     */
    private void writeMapIplistDataInfo(DataOutputStream dos, int mapType,
            SparseArray<QRomIplistData> datas) throws Exception {
        if (dos == null) {
            QWupLog.w(TAG, "writeMapOfIplistDataInfo -- dos is null ");
            return;
        }
        dos.writeInt(mapType);
        // 写入map长度
        if (datas == null) {
            dos.writeInt(0);
            QWupLog.w(TAG, "writeMapOfIplistDataInfo -- map is null, type = " + mapType);
            return;
        }
         int size = datas.size();
         // 写入缓存个数
        dos.writeInt(size);
        QWupLog.trace( TAG, "writeMapOfIplistDataInfo -- map type = " + mapType + ", size =" + size);
        QRomIplistData iplistData = null;
        // 开始写入每个iplist数据格式，写入顺序： 接入点信息, iplist信息, 更新时间，clientip  
        for (int i = 0; i < size; i++) {
            iplistData = datas.valueAt(i);
            if (iplistData == null) {  // 写入默认数据
                iplistData = new QRomIplistData("", null, mapType, 0, "");
            } 
            iplistData.serializData2OutputStream(dos);
            QWupLog.trace(TAG, "writeMapOfIplistDataInfo -- " + iplistData);
        }  // ~ end 数据保存完成
    }
    
    /**
     * 读取对应类型的iplist缓存 <p>
     *     -- 数据类型在外层读取<p>
     *     -- 读取格式  0:  缓存数据长度; 2: 每个QRomIplistData缓存数据(数据格式和writeMapIplistDataInfo接口写入格式一致)
     * @param dis               DataInputStream
     * @param datas           需保存的数据SparseArray<QRomIplistData>
     * @param dataType     数据类型
     * @return SparseArray<QRomIplistData>
     * @throws Exception
     */
    private SparseArray<QRomIplistData> readMapIplistInfo(DataInputStream dis,
            SparseArray<QRomIplistData> datas, int dataType) throws Exception {
        if (dis == null) {
            QWupLog.w(TAG, "readMapIplistInfo -- dis is null = ");
            return null;
        }
        
        if (datas == null) {
            datas = new SparseArray<QRomIplistData>();
            QWupLog.w(TAG, "readMapIplistInfo -- map is null  init map");
        }
        datas.clear();
        int size = dis.readInt();
        QWupLog.trace(TAG, "readMapIplistInfo -- start read map type = " + dataType );
        int key = -1;
        QRomIplistData iplistData = null;
        // 开始加载对应map中的item，数据格式和writeMapIplistDataInfo接口写入格式一致
        for (int i = 0; i < size; i++) {  
            iplistData = new QRomIplistData();
            iplistData.deSerializDataFromInputStream(dis);
            if (iplistData.isDataVaild()) {  // 是指定类型，保存数据             
                key = Integer.valueOf(iplistData.getDataFlg());
                datas.put(key, iplistData);
            }
            QWupLog.trace(TAG, "readMapIplistInfo -- " + iplistData);
        }  // ~end 相应数据保存完成
     
        return datas;
    }
    
    /**
     * 将wifi下对应bssid iplist相关信息写入缓存文件<p>
     *    -- 写入格式  0: 缓存类型(bssid); 1: 缓存数据长度; 2: 每个QRomIplistData缓存数据
     * @param dos                DataOutputStream
     * @param mapType       缓存类型
     * @param datas             相关iplist缓存信息 SparseArray<QRomIplistData>
     * @throws Exception
     */
    private void writeWifiMapIplistDataInfo(DataOutputStream dos, int mapType,
            Map<String, QRomIplistData> datas) throws Exception {
        if (dos == null) {
            QWupLog.w(TAG, "writeWifiMapIplistDataInfo -- dos is null ");
            return;
        }
        dos.writeInt(mapType);
        // 写入map长度
        if (datas == null) {
            dos.writeInt(0);
            QWupLog.w(TAG, "writeWifiMapIplistDataInfo -- map is null, type = " + mapType);
            return;
        }
         int size = datas.size();
        dos.writeInt(size);
        QWupLog.trace( TAG, "writeWifiMapIplistDataInfo -- map type = " + mapType + ", size =" + size);
        
        QRomIplistData iplistData = null;
        // 开始写入每个iplist数据格式，写入顺序： 接入点信息, iplist信息, 更新时间，clientip  
        for (Entry<String, QRomIplistData> entry : datas.entrySet()) {
            iplistData = null;
            if (entry != null) {
                iplistData = entry.getValue();                
            }            
            if (iplistData == null) {  // 写入默认数据
                iplistData = new QRomIplistData("", null, mapType, 0, "");
            } 
            iplistData.serializData2OutputStream(dos);           
            QWupLog.trace(TAG, "writeWifiMapIplistDataInfo -- " + iplistData);
        }  // ~ end 数据保存完成
    }    
    
    /**
     * 读取wifi对应bssid类型的iplist缓存 <p>
     *     -- 数据类型在外层读取<p>
     *     -- 读取格式  0:  缓存数据长度; 2: 每个QRomIplistData缓存数据(数据格式和writeMapIplistDataInfo接口写入格式一致)
     * @param dis               DataInputStream
     * @param datas           需保存的数据SparseArray<QRomIplistData>
     * @param dataType     数据类型
     * @return SparseArray<QRomIplistData>
     * @throws Exception
     */
    private Map<String, QRomIplistData> readWifiMapIplistInfo(DataInputStream dis,
            Map<String, QRomIplistData> datas, int dataType) throws Exception {
        if (dis == null) {
            QWupLog.w(TAG, "readWifiMapIplistInfo -- dis is null = ");
            return null;
        }
        
        if (datas == null) {
            datas = new HashMap<String, QRomIplistData>();
            QWupLog.w(TAG, "readWifiMapIplistInfo -- map is null  init map");
        }
        datas.clear();
        int size = dis.readInt();
        
        QRomIplistData iplistData = null;
        // 开始加载对应map中的item，数据格式和writeMapIplistDataInfo接口写入格式一致
        QWupLog.trace(TAG, "readWifiMapIplistInfo -- dataType = " + dataType);
        for (int i = 0; i < size; i++) {  
            iplistData = new QRomIplistData();         
            iplistData.deSerializDataFromInputStream(dis);
            if (iplistData.isDataVaild()) {  // 是指定类型，保存数据             
                // 初始化对应数据
                datas.put(iplistData.getDataFlg(), iplistData);
            }
            QWupLog.trace(TAG, "readWifiMapIplistInfo -- iplistData: " +iplistData);
        }  // ~end 相应数据保存完成
     
        return datas;
    }
    
    /**
     * 获取所有接入点 wup 代理地址
     * @return
     */
    public SparseArray<List<String>> getAllProxyInfos() {        

    	return getAllIplistInfos(mProxyIpDatas);
    }
    
    /**
     * 获取所有接入点 wup 代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupProxyIplistDatas() {        

        return mProxyIpDatas;
    }
    
    public Map<String, QRomIplistData> getAllWupWifiProxyInfos() {
        return mWifiProxyIpDatas;
    }
    
    public Map<String, QRomIplistData> getAllWupWifiSocketProxyInfos() {
        return mWifiWupSocketIpDatas;
    }
    
    /**
     * 获取所有接入点 wup socket代理地址
     * @return
     */
    public SparseArray<List<String>> getAllSocketProxyInfos() {
    	return getAllIplistInfos(mWupSocketIpDatas);
    } 
    
    /**
     * 获取所有接入点 wup 代理地址
     * @return
     */
    public SparseArray<QRomIplistData> getAllWupSocketProxyIplistDatas() {        

        return mWupSocketIpDatas;
    }
    
    /**
     * 获取对应类型数据所有接入点代理地址
     * @return
     */
    private SparseArray<List<String>> getAllIplistInfos(SparseArray<QRomIplistData> ipinfos) {

        if (ipinfos == null || ipinfos.size() == 0) {
            return null;
        }
        SparseArray<List<String>> allInfo = new SparseArray<List<String>>(ipinfos.size());
        QRomIplistData iplistData = null;
        int key = -1;
        for (int i = 0; i < ipinfos.size(); i++) {
            key = ipinfos.keyAt(i);
            iplistData = ipinfos.get(key);
            if (iplistData == null || iplistData.isEmpty()) {
                continue;
            }
            allInfo.put(key, iplistData.getIplistInfo());
        }
        return allInfo;
    }

    /**
     * @return the mStrGUID
     */
    public String getGuidStr() {
        return QWupStringUtil.byteToHexString(mVGUID);
    }
    
    public byte[] getGuidBytes() {
        if (mVGUID == null) {
            return DEFAULT_GUID_BYTES;
        }
        
        return mVGUID;
    }

    /**
     * 判断guid是否合法
     */
    public static boolean isGuidValidate(String guid) {
        if (guid != null && (guid.length() < DEFAULT_GUID_LEN * 2 || guid.length() % (DEFAULT_GUID_BASE_LEN  * 2) != 0)) {
            return false;
        }
        return !QWupStringUtil.isEmpty(guid) && !DEFAULT_GUID_STR.equals(guid);
    }
    
    /**
     * 判断guid是否合法
     * @param guid
     * @return
     */
    public static boolean isGuidValidate(byte[] guid) {
        if (guid != null && (guid.length < DEFAULT_GUID_LEN || guid.length % DEFAULT_GUID_BASE_LEN != 0)) {
            return false;
        }
    	return isGuidValidate(QWupStringUtil.byteToHexString(guid));
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
    
    
    /**
     * 读取SparseArray<List<String>>缓存信息
     * @param dis
     * @param map
     * @param valueType
     * @return
     * @throws Exception
     */
    private SparseArray<List<String>> readMapInfo(DataInputStream dis, SparseArray<List<String>> map, int valueType) throws Exception {
      if (dis == null) {
          QWupLog.w(TAG, "readMapInfo -- dis is null = ");
            return null;
        }
      
      if (map == null) {
          map = new SparseArray<List<String>>();
          QWupLog.w(TAG, "readMapInfo -- map is null  init map");
      }
      map.clear();
      int size = dis.readInt();
      QWupLog.trace( TAG, "readMapInfo -- map type = " + valueType + "   size = " + size);
      int key = -1;
      // list类型
      int type = -1;
      List<String> value = null;
      for (int i = 0; i < size; i++) {
          // 读取key
          key = dis.readInt();
          // 读取list类型
          type = dis.readInt();
          value = map.get(key);
          // 获取list
          value = readListInfo(dis, value);

          QWupLog.trace(TAG, "readMapInfo --  "  + "   key = " + key + "  value type = " + type + "  value size  = " + value.size());
          if (type == valueType) {  // 是指定类型，保存数据             
              map.put(key, value);
          } else {
              map.put(key, null);
          }
      }
      return map;
    }
    
    
//  /**
//   * 保存指定类型的map数据信息
//   *   -- 保存格式为 maptype + size + key +valuelist（list格式参看list保存逻辑） +（key + valuelist）...
//   *   
//   * @param mapType   map类型 如MAP_PROXY
//   * @param map
//   * @param valueType  value list的type，如LIST_PROXY等
//   * @throws Exception
//   */
//  private void writeMapStringListOfType(DataOutputStream dos, int mapType, 
//        SparseArray<List<String>> map, int valueType) throws Exception {
//    if (dos == null) {
//        QWupLog.w(TAG, "writeMapStringListOfType -- dos is null ");
//          return;
//      }
//    dos.writeInt(mapType);
//    QWupLog.w(TAG, "writeMapStringListOfType -- maptype =  " + mapType + "  value Type= " + valueType);
//    // 写入map长度
//    if (map == null) {
//          dos.writeInt(0);
//          QWupLog.w(TAG, "writeMapStringListOfType -- map is null  ");
//          return;
//      }
//     
//    dos.writeInt(map.size());
//    QWupLog.trace( TAG, "writeMapStringListOfType -- map size =  " + map.size());
//    int key = -1;
//    List<String> value = null;
//    
//    for (int i = 0; i < map.size(); i++) {
//        key  = map.keyAt(i);
//        dos.writeInt(key);
//        QWupLog.trace( TAG, "writeMapStringListOfType -- map key =  " + key);
//        value = map.get(key);
//        writeListInfoOfType(dos, valueType, value);
//    }       
//  }
  
///**
// * 保存指定类型的list信息
// * @param dos
// * @param type
// * @param list
// * @throws Exception
// */
//private void writeListInfoOfType(DataOutputStream dos, int type, List<String> list) throws Exception {
//    if (dos == null) {
//        return;
//    }
//    dos.writeInt(type);
//
//    if (list == null) {
//        dos.writeInt(0);
//       QWupLog.trace( TAG, "writeListInfoOfType -- list null,  type =  " + type);
//        return;
//    }
//    dos.writeInt(list.size());
//    QWupLog.trace( TAG, "writeListInfoOfType -- list type = "+ type + ", size = " + list.size() + ", list= " + list);
//    for (String proxyServer : list) {
//        dos.writeUTF(proxyServer == null ? "" : proxyServer);
//    }
//}
  
//  /**
//   * 将string的map写入缓存文件
//   * @param dos            DataOutputStream
//   * @param mapType   map缓存类型
//   * @param datas         缓存数据 SparseArray<String>
//   * @throws Exception
//   */
//  private void writeMapStrOfType(DataOutputStream dos, int mapType,
//          SparseArray<String> datas) throws Exception {
//      if (dos == null) {
//          QWupLog.w(TAG, "writeMapStringListOfType -- dos is null ");
//          return;
//      }
//      dos.writeInt(mapType);
//      if (datas == null) {
//          dos.writeInt(0);
//          QWupLog.w(TAG, "writeMapStringOfType -- map is null. type = " + mapType);
//          return;
//      }
//      
//      int size = datas.size();
//      dos.writeInt(size);
//      QWupLog.trace(TAG, "writeMapStringOfType -- map type =" + mapType + ", size = "+ size);
//      int key = -1;
//      String value = null;
//      for (int i = 0; i < size; i++) {  // 保存每个string
//          key = datas.keyAt(i);
//          value = datas.get(key);
//          dos.writeInt(key);
//          dos.writeUTF(value == null ? "" : value);
//      }
//  }
//  
//  /**
//   * 读出对应类型的map str缓存信息
//   * @param dis    DataInputStream
//   * @param map  SparseArray<String>
//   * @return   SparseArray<String>
//   * @throws Exception
//   */
//  private SparseArray<String> readMapStrInfo(DataInputStream dis, SparseArray<String> map) throws Exception {
//      if (dis == null) {
//          QWupLog.w(TAG, "readMapStrInfo -- dis is null!");
//          return null;
//      }
//      
//      if (map == null) {
//          map = new SparseArray<String>();
//          QWupLog.w(TAG, "readMapStrInfo -- map is null  init map");
//      }
//      map.clear();
//      int size = dis.readInt();
//      QWupLog.trace( TAG, "readMapStrInfo -- map size = " + size);
//      String value = null;
//      int key = -1;
//      for (int i = 0; i < size; i++) {
//          key = dis.readInt();
//          value = dis.readUTF();
//          map.put(key, value);
//      }  // ~读取map string完成
//      
//      return map;
//  }    
  
//  /**
//   * 将long的map写入缓存文件
//   * @param dos            DataOutputStream
//   * @param mapType   map缓存类型
//   * @param datas         缓存数据 SparseArray<Long>
//   * @throws Exception
//   */
//  private void writeMapLongOfType(DataOutputStream dos, int mapType,
//          SparseArray<Long> datas) throws Exception {
//      if (dos == null) {
//          QWupLog.w(TAG, "writeMapLongOfType -- dos is null ");
//          return;
//      }
//      dos.writeInt(mapType);
//      if (datas == null) {
//          dos.writeInt(0);
//          QWupLog.w(TAG, "writeMapLongOfType -- map is null. type = " + mapType);
//          return;
//      }
//      
//      int size = datas.size();
//      dos.writeInt(size);
//      QWupLog.trace(TAG, "writeMapLongOfType -- map type =" + mapType + ", size = "+ size);
//      int key = -1;
//      Long value = null;
//      for (int i = 0; i < size; i++) {  // 保存每个long
//          key = datas.keyAt(i);
//          value = datas.get(key);
//          dos.writeInt(key);
//          dos.writeLong(value == null ? 0 : value);
//      }
//  }
  
//  /**
//   * 读出对应类型的map str缓存信息
//   * @param dis    DataInputStream
//   * @param map  SparseArray<Long>
//   * @return   SparseArray<Long>
//   * @throws Exception
//   */
//  private SparseArray<Long> readMapLongInfo(DataInputStream dis, SparseArray<Long> map) throws Exception {
//      if (dis == null) {
//          QWupLog.w(TAG, "readMapLongInfo -- dis is null!");
//          return null;
//      }
//      
//      if (map == null) {
//          map = new SparseArray<Long>();
//          QWupLog.w(TAG, "readMapLongInfo -- map is null  init map");
//      }
//      map.clear();
//      int size = dis.readInt();
//      QWupLog.trace( TAG, "readMapLongInfo -- map size = " + size);
//      long value = 0;
//      int key = -1;
//      for (int i = 0; i < size; i++) {
//          key = dis.readInt();
//          value = dis.readLong();
//          map.put(key, value);
//      }  // ~读取map Long完成
//      
//      return map;
//  }
  
//  /**
//   * 写入wifi相关的iplist信息
//   * @param dos
//   * @param dataType    数据类型
//   * @param datas         iplist数据
//   * @param cacheTime iplist缓存时间
//   * @param cacheIps    缓存时客户端ip
//   * @throws Exception
//   */
//  private void writeWifiIpListOfBssid(DataOutputStream dos, int dataType,
//          Map<String, List<String>> datas, Map<String, Long> cacheTimes,
//          Map<String, String> cacheIps) throws Exception {
//      
//      if (dos == null) {
//          QWupLog.w(TAG, "writeWifiIpListOfBssid -- dos is null ");
//          return;
//      }
//      dos.writeInt(dataType);
//      if (datas == null || datas.isEmpty()) {
//       // 写入数据长度
//          dos.writeInt(0);
//          return;
//      }
//      int size = datas.size();
//      // 写入数据长度
//      dos.writeInt(size);
//      List<String> iplist = null;
//      String key = null;
//      String clientIp = null;
//      long cacheTime = -1;
//      for (Entry<String, List<String>> entry : datas.entrySet()) {
//          if (entry == null) {
//              key = null;
//              iplist = null;
//          } else {                
//              key = entry.getKey();
//              iplist = entry.getValue();
//          }
//          cacheTime = 0;
//          clientIp = null;
//          if (cacheTimes != null && key != null) {
//              cacheTime = cacheTimes.get(key);
//          }
//          if (cacheIps != null && key != null) {
//              clientIp = cacheIps.get(key);
//          }
//          
//          if (key == null) {
//              key = "";
//          }
//          if (clientIp == null) {
//              clientIp = "";
//          }
//          // 写入bssid
//          dos.writeUTF(key);
//          
//          // 写入对应的iplist
//          writeListInfoOfType(dos, dataType, iplist);
//          // 写入更新时间
//          dos.writeLong(cacheTime);
//          // 写入client ip
//          dos.writeUTF(clientIp);
//      }  // 对应map中的信息写入完成
//      
//  }
  
//  /**
//   *  读取wifi想对应数据iplist信息
//   * @param dis
//   * @param dataType       缓存类型
//   * @param datas             保存的数据对象（若传入为null，则创建一个新的map并返回）
//   * @param cacheTimes    保存的缓存时间（若传入为null，则无数据缓存）
//   * @param cacheIps         保存的客户端ip （若传入为null，则无数据缓存）
//   * @throws Exception
//   * 
//   * @return  Map<String, List<String>> datas
//   */
//  private Map<String, List<String>> readWifiIpListOfBssid(DataInputStream dis, int dataType,
//          Map<String, List<String>> datas, Map<String, Long> cacheTimes,
//          Map<String, String> cacheIps) throws Exception {
//      
//      if (dis == null) {
//          QWupLog.w(TAG, "readWifiIpListOfBssid-> dis is null ");
//          return null;
//      }
//      
//      if (datas == null) {
//          QWupLog.w(TAG, "readWifiIpListOfBssid -> map is null  init map");
//          datas = new HashMap<String, List<String>>(5);
//      }
//      if (cacheTimes != null) {
//          cacheTimes.clear();
//      }
//      if (cacheIps != null) {
//          cacheIps.clear();
//      }
//      
//      datas.clear();
//      int size = dis.readInt();
//      String key = null;
//      List<String> iplist = null;
//      long cacheTime = -1;
//      String clientIp = null;
//      int listType = 0;
//      for (int i = 0; i < size; i++) {  // 开始读取数据
//          key = dis.readUTF();
//          if (key != null) {
//              iplist = datas.get(key);          
//          }
//          // iplist 类型，默认和map类型一直
//          listType = dis.readInt();
//          iplist = readListInfo(dis, iplist);
//          cacheTime = dis.readLong();
//          clientIp = dis.readUTF();
//          
//          if (QWupStringUtil.isEmpty(key) || iplist == null || iplist.isEmpty() || listType != dataType) {  // 数据不对
//              QWupLog.trace(TAG,
//                      "readWifiIpListOfBssid-> file type is not match data type! filetype = "
//                              + listType + ", dataType =" + dataType);
//              continue;
//          }
//          
//          datas.put(key, iplist);
//              
//          if (cacheTimes != null) {  // 缓存时间
//              cacheTimes.put(key, cacheTime);
//          }
//          if (cacheIps != null) {  // 缓存客户端ip
//              cacheIps.put(key, clientIp);
//          }
//      }
//      
//      return datas;                 
//  }
}
