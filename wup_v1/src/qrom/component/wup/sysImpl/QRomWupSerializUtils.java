package qrom.component.wup.sysImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.wupData.QRomIplistData;
import TRom.IPListRsp;
import TRom.JoinIPInfo;
import android.util.SparseArray;

public class QRomWupSerializUtils {

    public static final String TAG = "QRomWupSerializUtils";
    
    /**
     * 序列号数据类型
     * @author sukeyli
     *
     */
    public  static final class SerializItemType {
        /** 序列对象个数 */
        public static final byte TYPE_SERIALIZ_HEAD_CNT = 0;
        /** 每个obj开始位标志 */
        public static final byte TYPE_ITEM_START_CNT = 1;
        /** int 类型*/
        public static final byte TYPE_INT = 2;
        /** string 类型 */
        public static final byte TYPE_STR = 3;
        /** long 类型*/
        public static final byte TYPE_LONG = 4;
        /** byte 类型 */
        public static final byte TYPE_BYTE = 5;
        /** byte[] 类型 */
        public static final byte TYPE_ARRAY_BYTE = 6;
        /** LIST STRING类型 */
        public static final byte TYPE_LIST_STR = 7;
    }
    

    /**
     * 将iplist解析为byte[]
     * @param ipType
     * @param ipInfos
     * @return
     */
    public static byte[] parasIpListInfos2Bytes(int ipType, SparseArray<List<String>> ipInfos) {
        if (ipInfos == null || ipInfos.size() == 0) {
            QWupLog.w(TAG, "parasIpListInfos2Bytes-> data is empty");
            return null;
        }
        int cnt = ipInfos.size();
        IPListRsp ipListRsp = new IPListRsp();
        ArrayList<JoinIPInfo> ipRsp = new ArrayList<JoinIPInfo>(cnt);
        JoinIPInfo joinIPInfo = null;
        int apnType = -1;
        List<String> ipList = null;
        for (int i = 0; i < cnt; i++) {
            apnType = ipInfos.keyAt(i);
            ipList = ipInfos.get(apnType);
            if (ipList == null || ipList.isEmpty()) {
                continue;
            }
            joinIPInfo = new JoinIPInfo();
            // ip类型
            joinIPInfo.eIPType = ipType;
            // ip apn索引
            joinIPInfo.eApnType = apnType;
            // ip列表
            joinIPInfo.vIPList = (ArrayList<String>)ipList;
            ipRsp.add(joinIPInfo);
        }
        return QRomWupDataBuilderImpl.parseJceStructToBytesInUTF_8(ipListRsp);
    }
            
    /**
     * 将byte字节流解析成对应的iplist信息
     * @param datas     byte[]
     * @param ipInfos   将返回的信息添加到指定的SparseArray中
     * @return  ipInfos
     */
    public static SparseArray<QRomIplistData> parasBytes2Map(byte[] datas,  int dataType) {

        if (datas == null || datas.length == 0) {
            QWupLog.trace(TAG, "parasBytes2Map-> data is empty ");
            return null;
        }
        
        IPListRsp ipListRsp = new IPListRsp();
        ipListRsp = (IPListRsp) QRomWupDataBuilderImpl.parseBytesToJceStructInUTF_8(datas, ipListRsp);
        if (ipListRsp == null || ipListRsp.vJoinIPInfo == null || ipListRsp.vJoinIPInfo.isEmpty()) {
            return null;
        }
        ArrayList<JoinIPInfo> items = ipListRsp.vJoinIPInfo;
        int cnt = items.size();        
        QRomIplistData iplistData = null;
        JoinIPInfo ipInfo = null;
        SparseArray<QRomIplistData> ipInfos = new SparseArray<QRomIplistData>(cnt);
        for (int i = 0; i < cnt; i++) {
            ipInfo = items.get(i);
            if (ipInfo == null ||  ipInfo.getVIPList() == null) {
                continue;
            }
            iplistData = new QRomIplistData(ipInfo.getEApnType(), ipInfo.getVIPList(), dataType, 0, null);
            ipInfos.put(ipInfo.getEApnType(), iplistData);
        }
        return ipInfos;
    }
    
    public static byte[] parasLong2Bytes(long data) {
        return String.valueOf(data).getBytes();
    }
    
    /**
     * 将qromIplistData数据序列化
     * @param ipInfos  SparseArray<QRomIplistData>
     * @return  byte[]
     */
    public static byte[] parasIpListDatas2Bytes(SparseArray<QRomIplistData> ipInfos) {

        if (ipInfos == null || ipInfos.size() == 0) {
            QWupLog.trace(TAG, "parasIpListDatas2Bytes->SparseArray data is empty ");
            return null;
        }
        byte[] rsp = null;
        ByteArrayOutputStream byteOutStream = null;
        DataOutputStream outputStream = null;
        try {
            int n = ipInfos.size();
            QWupLog.trace(TAG, "parasIpListDatas2Bytes->SparseArray data size = " + n);
            QRomIplistData item = null;
            byteOutStream = new ByteArrayOutputStream();
            outputStream = new DataOutputStream(byteOutStream);
            outputStream.writeByte(SerializItemType.TYPE_SERIALIZ_HEAD_CNT);
            outputStream.writeInt(n);
            
            outputStream.writeByte(SerializItemType.TYPE_ITEM_START_CNT);
            outputStream.writeInt(QRomIplistData.M_ITEM_CNT);
            int key = -1;
            for (int i = 0; i < n; i++) {
                key = ipInfos.keyAt(i);
                item = ipInfos.get(key);
                if (item == null) {  // 这里直接获取下个数据，不做默认数据补齐
                    continue;
                }     
                if (QWupStringUtil.isEmpty(item.getDataFlg())) {
                    item.setDataFlg(String.valueOf(key));
                }
                item.serializData2OutputStream(outputStream);
            }
            byteOutStream.flush();
            rsp = byteOutStream.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteOutStream != null) {
                    byteOutStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rsp;
    
    }
    
    /**
     * 将qromIplistData数据序列化
     * @param ipInfos  Map<String, QRomIplistData>
     * @return  byte[]
     */
    public static byte[] parasIpListDatas2Bytes(Map<String, QRomIplistData> ipInfos) {

        if (ipInfos == null || ipInfos.size() == 0) {
            QWupLog.trace(TAG, "parasIpListDatas2Bytes-> map data is empty ");
            return null;
        }
        byte[] rsp = null;
        ByteArrayOutputStream byteOutStream = null;
        DataOutputStream outputStream = null;
        try {
            int n = ipInfos.size();
            QWupLog.trace(TAG, "parasIpListDatas2Bytes->map data size = " + n);
            QRomIplistData item = null;
            byteOutStream = new ByteArrayOutputStream();
            outputStream = new DataOutputStream(byteOutStream);
            outputStream.writeByte(SerializItemType.TYPE_SERIALIZ_HEAD_CNT);
            outputStream.writeInt(n);
            
            outputStream.writeByte(SerializItemType.TYPE_ITEM_START_CNT);
            outputStream.writeInt(QRomIplistData.M_ITEM_CNT);
            String key = null;
            for (Entry<String, QRomIplistData> entry : ipInfos.entrySet()) {
                key = null;
                item = null;
                if (entry != null) { 
                    key = entry.getKey();
                    item = entry.getValue();
                }
                if (item == null) { // 这里直接获取下个数据，不做默认数据补齐
                    continue;
                }
                if (QWupStringUtil.isEmpty(item.getDataFlg())) {
                    item.setDataFlg(key);
                }
                item.serializData2OutputStream(outputStream);
            }  // ~end 所有数据遍历完成
            byteOutStream.flush();
            rsp = byteOutStream.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteOutStream != null) {
                    byteOutStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rsp;    
    }
    

    /**
     * 解析对应接入点下iplist信息
     * @param dataBytes
     * @return
     */
    public static SparseArray<QRomIplistData> deSerializeQRomIplistDatas(byte[] dataBytes) {
        
        if (dataBytes == null || dataBytes.length == 0) {
            QWupLog.trace(TAG, "deSerializeQRomIplistDatas-> data is empty ");
            return null;
        }
        SparseArray<QRomIplistData> rspDatas = null;
        QRomIplistData itemData = null;
        ByteArrayInputStream byteInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            byteInputStream = new ByteArrayInputStream(dataBytes);
            dataInputStream = new DataInputStream(byteInputStream);
            int headFlg = dataInputStream.readByte();
            if (headFlg != SerializItemType.TYPE_SERIALIZ_HEAD_CNT) {  // 不是数据开始标识
                QWupLog.trace(TAG, "deSerializeQRomIplistDatas-> TYPE_SERIALIZ_HEAD_CNT flg err ");
                return null;
            }
            int itemCnt = dataInputStream.readInt();
            
            int filedFlg = dataInputStream.readByte();
            if(filedFlg != SerializItemType.TYPE_ITEM_START_CNT) {
                QWupLog.trace(TAG, "deSerializeQRomIplistDatas-> TYPE_ITEM_START_CNT flg err ");
                return null;
            }
            int filedCnt = dataInputStream.readInt();
            QWupLog.trace(TAG, "deSerializeQRomIplistDatas-> data len = " + dataBytes.length +  ", filedCnt = " + filedCnt);
            rspDatas = new  SparseArray<QRomIplistData>(itemCnt);
            int index = 0;
            int key = -1;
            while(index< itemCnt) {
                itemData = new QRomIplistData();
                itemData.deSerializDataFromInputStream(dataInputStream);         
                if (!itemData.isDataVaild()) {  // 数据无效
                    continue;
                }
                key = Integer.valueOf(itemData.getDataFlg().trim());
                rspDatas.put(key, itemData);
                index++;
            }
        } catch (Throwable e) {
            QWupLog.w(TAG, e);
            rspDatas = null;
        } finally {
            try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (Exception e) {
                QWupLog.w(TAG, e);
            }
        }
        return rspDatas;
    }


    /**
     * 解析wifi下iplist数据
     * @param dataBytes
     * @return
     */
    public static Map<String, QRomIplistData> deSerializeQRomIplistDatasForWifi(byte[] dataBytes) {
        
        if (dataBytes == null || dataBytes.length == 0) {
            return null;
        }
        Map<String, QRomIplistData> rspDatas = null;
        QRomIplistData itemData = null;
        ByteArrayInputStream byteInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            byteInputStream = new ByteArrayInputStream(dataBytes);
            dataInputStream = new DataInputStream(byteInputStream);
            int headFlg = dataInputStream.readByte();
            if (headFlg != SerializItemType.TYPE_SERIALIZ_HEAD_CNT) {  // 不是数据开始标识
                QWupLog.trace(TAG, "deSerializeQRomIplistDatasForWifi-> TYPE_SERIALIZ_HEAD_CNT err" );
                return null;
            }
            int itemCnt = dataInputStream.readInt();
            
            int filedFlg = dataInputStream.readByte();
            if(filedFlg != SerializItemType.TYPE_ITEM_START_CNT) {
                QWupLog.trace(TAG, "deSerializeQRomIplistDatasForWifi-> TYPE_ITEM_START_CNT flg err");
                return null;
            }
            int filedCnt = dataInputStream.readInt();
            QWupLog.trace(TAG, "deSerializeQRomIplistDatasForWifi-> data len = " + dataBytes.length +  ", filedCnt = " + filedCnt);
            rspDatas = new  HashMap<String, QRomIplistData>(itemCnt);
            int index = 0;
            while(index< itemCnt) {
                itemData = new QRomIplistData();
                itemData.deSerializDataFromInputStream(dataInputStream);         
                if (!itemData.isDataVaild()) {  // 数据无效
                    continue;
                }
                rspDatas.put(itemData.getDataFlg(), itemData);
                index++;
            }
        } catch (Throwable e) {
            QWupLog.w(TAG, e);
            rspDatas = null;
        } finally {
            try {
                if (byteInputStream != null) {
                    byteInputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (Exception e) {
                QWupLog.w(TAG, e);
            }
        }
        return rspDatas;
    }
}
