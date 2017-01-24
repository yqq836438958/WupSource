package qrom.component.wup.wupData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import qrom.component.wup.sysImpl.QRomWupSerializUtils.SerializItemType;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_ERR_INDEX;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;
import android.util.SparseArray;

/**
 * 接入点对应的iplist信息<p>
 * 
 *    -- 当前apn对应的iplist<p>
 *    -- 当前iplist使用的ip，及对应ip的索引<p>
 *    -- 客户端ip<p>
 *   update by  2015.1.6
 * @author sukeyli
 *
 */
public class QRomIplistData {

    public static final String TAG = "QRomIplistData";
    
    // ↓↓↓↓↓↓ 仅切换使用，不参加序列化↓↓↓↓↓↓↓
    /** 当前ip 在缓存ip中的索引地址 */
    private int mCurIpIndex = 0;
    /** 当前使用ip */
    private String mCurProxyAddr = null;
    
    // ↓↓↓↓↓↓ 需序列化保存的数据对象↓↓↓↓↓↓↓
    
    /*
     * 为版本兼容，不影响使用接口反序列化， 序列号字段请勿删除；
     * 若有更新，请新增字段
     */
    
    /** 需序列号的字段个数 */
    public static final int M_ITEM_CNT = 5;

    /** 0: 当前iplist的标识，如apntype，或wifi的bssid */
    private String mDataFlg;    
    /* list信息先写入当前list的类别，做数据校验用*/
    /** 1: 当前缓存的ip类别： 如wup代理，socket的代理，wifi下的普通iplist */
    private int mCacheType;
    /** 2: iplist信息*/
    private List<String> mIplist = null;
    /** 3: iplist 更新时间 */
    private long mUpdateTime;
    /** 4: 拉取iplist时客户端ip(由后台下发)*/
    private String mClientIp;
    
    public QRomIplistData(String dataFlg, List<String> iplist, int cacheType, long updateTime, String clientIp) {
        this(dataFlg, iplist, cacheType, updateTime, clientIp, 0);
    }
    
    public QRomIplistData(int dataFlg, List<String> iplist, int cacheType, long updateTime, String clientIp) {
        this(String.valueOf(dataFlg), iplist, cacheType, updateTime, clientIp, 0);
    }
    
    public QRomIplistData() {
        this("default", null, -1, -1, null);
    }
    
    public QRomIplistData(String dataFlg, List<String> iplist, int cacheType, long updateTime, String clientIp, int defaultIndex) {
        mDataFlg = dataFlg;
        mIplist = iplist;
        if (mIplist == null) {
            mIplist = new ArrayList<String>(5);
        }
        mCacheType = cacheType;
        mUpdateTime = updateTime;
        mClientIp = clientIp;
        mCurIpIndex = defaultIndex;
         
        if (defaultIndex < 0 || defaultIndex >= mIplist.size()) {
            defaultIndex = 0;
        }
        if (!mIplist.isEmpty()) {
            mCurProxyAddr = mIplist.get(defaultIndex);
        }
    }
    
    public static QRomIplistData createDefaultIpListInfo() {
        List<String> testAddr = new ArrayList<String>();
        testAddr.add(QWupSdkConstants.REMOTE_WUP_PROXY);        
        QRomIplistData iplistData = new QRomIplistData("default_relese", 
                testAddr, QRomWupInfo.TYPE_PROXY_IPLIST_DATA,
                System.currentTimeMillis(), null, IPLIST_ERR_INDEX.IPLIST_EMPTY);
        return iplistData;
    }
    
    public static QRomIplistData createDefaultSocketIpListInfo() {
        List<String> testAddr = new ArrayList<String>();
        testAddr.add(QWupSdkConstants.REMOTE_WUP_SOCKET_PROXY);        
        QRomIplistData iplistData = new QRomIplistData("default_socketRelese", 
                testAddr, QRomWupInfo.TYPE_WUP_SOCKET_IPLIST_DATA, 
                System.currentTimeMillis(), null, IPLIST_ERR_INDEX.IPLIST_EMPTY);
        return iplistData;
    }
    
    
    public static QRomIplistData createTestIpListInfo(String addr, int addrIndex) {
     
        List<String> testAddr = new ArrayList<String>();
        testAddr.add(addr);        
        QRomIplistData iplistData = new QRomIplistData("default_test",
                testAddr, QRomWupInfo.TYPE_PROXY_IPLIST_DATA, 
                System.currentTimeMillis(), null, addrIndex);
        return iplistData;
    }
    
    public int getCurIplistSize() {
        if (mIplist == null) {
            return 0;
        }
        return mIplist.size();
    }
    
    
    public void refreshIplistInfo(List<String> iplistList, String clientIp, long updateTime) {
        
        if (mIplist == null) {  // 清除缓存
            mIplist = new ArrayList<String>(4);
        }
        
        if (iplistList == mIplist) {  // 同一个ipslit缓存
            QWupLog.trace(TAG, "refreshIplistInfo-> the iplistList is cur cached one, cance update");
            return;
        }
        
        mIplist.clear();
        if (iplistList != null && !iplistList.isEmpty()) {  // 更新iplist
            mIplist.addAll(iplistList);
            if (updateTime <= 0) {
                updateTime = System.currentTimeMillis();
            }
            mUpdateTime = updateTime;
        }

        // 更新客户端ip
        mClientIp = clientIp;
    }
    
    public boolean setIndex(int index) {
        
        mCurIpIndex = index;
        int size = mIplist.size();
        if (mCurIpIndex >= size) {  // 所有ip无效
            mCurProxyAddr = QWupSdkConstants.REMOTE_WUP_PROXY;
            return false;
        }
        
        if (mCurIpIndex < 0) {  // 索引错误
            QWupLog.trace(TAG, "setIndex-> iplist index err: " + mCurIpIndex);          
            mCurProxyAddr = QWupSdkConstants.REMOTE_WUP_PROXY;
            return false;
        }
        
        String address = null;
        for (int i = mCurIpIndex; i < size; i++) {
            // 这里要校验返回ProxyList中address的合法性
            address = QWupUrlUtil.resolvValidUrl(mIplist.get(i));    
            if (QWupStringUtil.isEmpty(address)) {  // 非法url
                continue;
            }

            // 合法的address返回，不合法需要向下遍历
            mCurIpIndex = i;
            mCurProxyAddr = address;
            return true;
        }
        mCurIpIndex = IPLIST_ERR_INDEX.IPLIST_INVALID;
        mCurProxyAddr = QWupSdkConstants.REMOTE_WUP_PROXY;
        return false;
    }
    
    public List<String> getIplistInfo() {
        return mIplist;
    }
    
    public int getCurIpIndex() {
        return mCurIpIndex;
    }
    
    public long getIplistUpdateTime() {
        return mUpdateTime;
    }
    
    public String getClientIp() {
        if (mClientIp == null) {
            return "";
        }
        return mClientIp;
    }
    
    public String getDataFlg() {
        return mDataFlg;
    }
    
    public int getCacheType() {
        return mCacheType;
    }
    
    public void setDataFlg(String dataFlg) {
        mDataFlg = dataFlg;
    }
    
    public boolean isEmpty() {
        return mIplist.isEmpty();
    }
    
    /**
     * 数据是否合法
     *    -- dataType不为空，切iplist不为空
     * @return
     */
    public boolean isDataVaild() {
        return !QWupStringUtil.isEmpty(mDataFlg) && !mIplist.isEmpty();
    }
    
    public boolean isIndexVaild() {
        return mCurIpIndex >= 0 && mCurIpIndex < getCurIplistSize();
    }
    
    public String getCurIpAddr() {
        return mCurProxyAddr;
    }
    
    public void clear() {
   
        mClientIp = "";
        mCurIpIndex = 0;
        mCurProxyAddr = "";
        mIplist.clear();
        mUpdateTime = 0;
    }
    
    /**
     * 序列化数据
     * @param dos
     * @throws Exception
     */
    public void serializData2OutputStream(DataOutputStream dos) throws Exception{
        
        if (dos == null) {
            QWupLog.trace(TAG, "serializData2InputStream-> dos is err");
            return;
        }
        // 写入序列化item的个数
        dos.writeInt(M_ITEM_CNT);
        
        // 按序号开始写入各个item
        // 0: 写入数据标识（apn等信息）
        writeStr(dos, mDataFlg);
        // 1: 写入list cache 类型
        writeInt(dos, mCacheType);        
        // 2:写入iplist信息 (在依次写入iplist信息)
        writeListStrInfos(dos, mIplist);
        // 3:写入缓存更新时间
        writeLong(dos, mUpdateTime);
        // 4:写入缓存的client ip
        writeStr(dos, mClientIp);
    }
    
    /**
     * 反序列化
     * @param dis
     * @throws Exception
     */
    public void deSerializDataFromInputStream(DataInputStream dis) throws Exception{
        
        if (dis == null) {
            QWupLog.trace(TAG, "deSerializDataFromInputStream-> dis is err");
            return;
        }
        // 解析出item field字段个数
        int itemCnt = dis.readInt();
        QWupLog.trace(TAG, "deSerializDataFromInputStream-> itemCnt = " + itemCnt);
        // 读出所有字段信息
        SparseArray<Object> fields = readAllItems(dis, itemCnt);
        
        // 按序解析出具体数据
        // 0: 读出数据标识（apn类型，或bssid）
        mDataFlg = (String) fields.get(0);
        // 1: 读出iplist cacheType
        Integer integer = (Integer) fields.get(1);
        mCacheType = integer == null ? 0 : integer;
        // 2:在依次读iplist信息)
       @SuppressWarnings("unchecked")
       List<String> list = (List<String>) fields.get(2);
       if (list != null && !list.isEmpty()) {
           mIplist.clear();
           mIplist.addAll(list);
       }
        // 3: 读出缓存时间
       Long longFileld = (Long) fields.get(3);
        mUpdateTime = longFileld == null ? 0 : longFileld;
        // 4：读出clientIp
        mClientIp = (String) fields.get(4);
        
        //~ end 目前仅4个字段，无兼容，这里读取完成，后续若扩充字段，通过itemCnt判断兼容性
        
    }
    
    /**
     * 希尔指定List<String>信息
     * @param dos
     * @param list
     * @throws Exception
     */
    private void writeListStrInfos(DataOutputStream dos, List<String> list) throws Exception {
        if (dos == null) {
            return;
        }
        dos.writeByte(SerializItemType.TYPE_LIST_STR);

        if (list == null || list.size() == 0) {
            dos.writeInt(0);
           QWupLog.trace( TAG, "writeListStrInfos -- list null");
            return;
        }
        int size = list.size();
        dos.writeInt(list.size());
        QWupLog.trace( TAG, "writeListStrInfos -- list size = " + list.size() + ", list= " + list);
        String proxyServer = null;
        for (int i = 0; i < size; i++) {
            proxyServer = list.get(i);
            dos.writeUTF(proxyServer == null ? "" : proxyServer);
        }
    }
    
    /**
     * 读取list数据
     *    -- 先读取list的长度（int） 然后读取对应长度的string类型
     * @param dis           DataInputStream (若为空，不做任何处理，返回null)
     * @return  List<String>  
     * @throws Exception
     */
    private List<String>  readListStrInfos(DataInputStream dis) throws Exception {
        
        if (dis == null) {
            return null;
        }
        //  读出iplist 的长度信息
        int cnt = dis.readInt();
        List<String> tempList = new ArrayList<String>(cnt);
        String str = null;
        // 顺序读出所有 str信息
        for (int i = 0; i < cnt; i++) {
            str = dis.readUTF();
            if (str != null && str.length() > 0) {
                tempList.add(str);
            }
        }

        QWupLog.trace( TAG, "readListInfo -- list = " + tempList);
        return tempList;
    }
    
    /**
     * 写入一个int型数据
     *   
     * @param stream         DataOutputStream
     * @param data             int
     * @throws Exception
     */
    public void writeInt(DataOutputStream stream, int data) throws Exception {

        stream.writeByte(SerializItemType.TYPE_INT);
        stream.writeInt(data);
    }
    
    /**
     * 写入一个String型数据
     * @param stream       DataOutputStream
     * @param data           String
     * @throws Exception
     */
    public void writeStr(DataOutputStream stream, String data) throws Exception {

        stream.writeByte(SerializItemType.TYPE_STR);
        if (data == null) {
            stream.writeUTF("");
        } else {            
            stream.writeUTF(data);
        }
    }
    
    /**
     * 写入一个long型数据
     * @param stream
     * @param data
     * @throws Exception
     */
    public void writeLong(DataOutputStream stream, long data) throws Exception {
        
        stream.writeByte(SerializItemType.TYPE_LONG);
        stream.writeLong(data);
    }
    
//    public void writeByte(DataOutputStream stream, byte data) throws Exception {
//        
//        stream.writeByte(SerializItemType.TYPE_BYTE);
//        stream.writeByte(data);
//    }
//    
//    public void writeArrayByte(DataOutputStream stream, byte[] data) throws Exception {
//        int cnt = 0;
//        if (data != null) {
//            cnt = data.length;
//        }
//        stream.writeByte(SerializItemType.TYPE_ARRAY_BYTE);
//        stream.writeInt(cnt);
//        if (cnt > 0) {            
//            stream.write(data);
//        }
//    }
    
    protected SparseArray<Object> readAllItems(DataInputStream stream, int fieldCnt) throws Exception {
        byte type = -1;
        byte[] data = null;
        QWupLog.i("====", "start field cnt = " + fieldCnt);
        SparseArray<Object> rspDatas = new SparseArray<Object>(fieldCnt);

        // 遍历所有item
        for (int i = 0; i < fieldCnt; i++) {
            type = stream.readByte();
            QWupLog.w(TAG, "=== read type: " + type);
            switch (type) {
            case SerializItemType.TYPE_INT:
                rspDatas.put(i, stream.readInt());
                break;
            case SerializItemType.TYPE_LONG:
                rspDatas.put(i, stream.readLong());
                break;
            case SerializItemType.TYPE_STR:
                rspDatas.put(i, stream.readUTF());
                break;
            case SerializItemType.TYPE_BYTE:
                rspDatas.put(i, stream.readByte());
                break;
            case SerializItemType.TYPE_ARRAY_BYTE:
                data = null;
                int dataLen = stream.readInt();
                if (dataLen > 0) {
                    data = new byte[dataLen];
                    stream.read(data);
                    rspDatas.put(i, data);
                }
                break;
                
            case SerializItemType.TYPE_LIST_STR: // list<String>类型
                rspDatas.put(i, readListStrInfos(stream));
                break;

            default:
                throw new IllegalArgumentException("QwupSerializUtils.readLeftItems -> type is not match");
            }
        }        

        return rspDatas;
    }
    
    @Override
    public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("cacheType: ");
         buffer.append(mCacheType);
         buffer.append(", iplistType: ");
         buffer.append(mDataFlg);
         buffer.append(", ipInfos: ");
         if (getIplistInfo() == null) {
             buffer.append("[null]");
         } else {
             buffer.append(getIplistInfo());
         }
         buffer.append(", updateTime: ");
         buffer.append(mUpdateTime);
         buffer.append(", clentIp: ");
         if (mClientIp != null) {
             buffer.append(mClientIp);
         }
         buffer.append(", index: ");
         buffer.append(mCurIpIndex);
         
        return buffer.toString();
    }
    
}
