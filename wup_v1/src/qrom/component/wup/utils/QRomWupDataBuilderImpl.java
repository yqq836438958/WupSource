package qrom.component.wup.utils;


import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.wupData.QRomWupInfo;
import TRom.EAPNTYPE;
import TRom.ENETTYPE;
import android.content.Context;

import com.qq.jce.wup.UniPacket;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.tencent.codec.des.DES;

public final class QRomWupDataBuilderImpl {

    public static final String TAG = "QRomWupDataBuilderImpl";
    
    public static final String ENCODE_UTF8 = "UTF-8";
    public static final String ENCODE_DEFAULT = "UTF-8";
    
    private static final int DEFAULT_RSP_CODE = -99;
    
    private QRomWupDataBuilderImpl() {
    }
    
    /**
     * 判断guid是否合法
     */
    public static boolean isGuidValidate(String guid) {
        return QRomWupInfo.isGuidValidate(guid);
    }
    
    /**
     * 判断guid是否合法
     * @param guid
     * @return
     */
    public static boolean isGuidValidate(byte[] guid) {
    	return QRomWupInfo.isGuidValidate(guid);
    }
    
    public static byte[] loadGuidFromFile(Context context) {
        return QRomWupImplEngine.getGuidForLoadFile(context);
    }
    
    /**
     * 创建wup请求包
     * 
     * @param serverName
     *            服务名
     * @param functionName
     *            方法名
     * @param paraFlg
     *            请求数据标识
     * @param data
     *            请求数据
     * @return UniPacket
     */
    public static UniPacket createReqUnipackage(String serverName,
            String functionName, String paraFlg, Object data) {
        return createReqUnipackage(serverName, functionName, paraFlg, data, null);
    }
    
    /**
     * 创建wup请求包
     * 
     * @param serverName
     *            服务名
     * @param functionName
     *            方法名
     * @param paraFlg
     *            请求数据标识
     * @param data
     *            请求数据
     * @return UniPacket
     */
    public static UniPacket createReqUnipackage(String serverName,
            String functionName, String paraFlg, Object data, String encodeName) {
        
        if (data == null) {
            return null;
        }

        UniPacket packet = new UniPacket();
        if (QWupStringUtil.isEmpty(encodeName)) {
            encodeName = ENCODE_DEFAULT;
        }
        packet.setEncodeName(encodeName);
        packet.setServantName(serverName);
        packet.setFuncName(functionName);
        if (QWupStringUtil.isEmpty(paraFlg)) {
            paraFlg = "req";
        }
        // 设置请求数据
        packet.put(paraFlg, data);

        return packet;
    }
    
    /**
     * 创建wup请求包(V3版本)
     * @param serverName
     * @param functionName
     * @param paraFlg
     * @param data
     * @return
     */
    public static UniPacket createReqUnipackageV3(String serverName,
            String functionName, String paraFlg, Object data) {
        return createReqUnipackageV3(serverName, functionName, paraFlg, data, ENCODE_DEFAULT);
    }
    
    /**
     * 创建wup请求包(V3版本)
     * 
     * @param serverName
     *            服务名
     * @param functionName
     *            方法名
     * @param paraFlg
     *            请求数据标识
     * @param data
     *            请求数据
     * @param encodeName 
     * 
     * @return UniPacket
     */
    public static UniPacket createReqUnipackageV3(String serverName,
            String functionName, String paraFlg, Object data, String encodeName) {
        
        if (data == null) {
            return null;
        }

        UniPacket packet = new UniPacket();
        if (QWupStringUtil.isEmpty(encodeName)) {
            encodeName = ENCODE_DEFAULT;
        }
        packet.setEncodeName(encodeName);
        packet.setServantName(serverName);
        packet.setFuncName(functionName);
        // 用v3版本编码
        packet.useVersion3();
        if (QWupStringUtil.isEmpty(paraFlg)) {
            paraFlg = "req";
        }
        // 设置请求数据
        packet.put(paraFlg, data);

        return packet;
    }
    
    /**
     * 将UniPacket解析成对应的byte数组
     * @param packet
     * @return
     */
    public static byte[] parseWupUnipackage2Bytes(UniPacket packet) {
        if (packet == null){
            return null;
        }
        
        byte[] datas = null;
        try {
            datas = packet.encode();
        } catch (Exception e) {
           QWupLog.w(TAG, e);
        }
        
        return datas;
    }

    /**
     * 获取指定标识符表示的数据对象
     *     （按默认gbk格式编解码）
     * @param data  byte[]
     * @param flg    获取数据对应的key
     * @return JceStruct
     */
    public static JceStruct parseWupResponseByFlg(byte[] data, String flg) {

    	return parseWupResponseByFlg(data, flg, null);
    }   
    
    public static JceStruct getJceStructFromPkg(UniPacket packet, String flg) {
        JceStruct jceStruct = null;
        try {
            if (null != packet) {
                jceStruct = packet.get(flg);
            }
        } catch (Exception e) {
            QWupLog.w(TAG, e);
        }
        return jceStruct;
    }

    /**
     * 根据指定的编码格式解析数据包
     * @param data                 byte[]
     * @param flg                   获取数据对应的key
     * @param encodeName  编码格式 （为空则按wup默认编码格式解包）
     * @return
     */
    public static JceStruct parseWupResponseByFlg(byte[] data, String flg, String encodeName) {
        UniPacket packet = getuniPacket(data, encodeName);
        JceStruct jceStruct = null;
        if (null != packet) {
            jceStruct = packet.get(flg);
        }
        return jceStruct;
    }
    
    /**
     *  解析V3编码格式数据包(默认UTF-8)
     * @param data       byte[]
     * @param flg          获取数据对应的key
     * @param proxy    对应协议的jceStruct
     * @return
     */
    public static JceStruct parseWupResponseByFlgV3(byte[] data, String flg, JceStruct proxy) {
        return parseWupResponseByFlgV3(data, flg, ENCODE_DEFAULT, proxy);
    }
    
    /**
     * 解析V3编码格式数据包
     * @param data                 byte[]
     * @param flg                   获取数据对应的key
     * @param encodeName  编码格式 （为空则按wup默认编码格式解包）
     * @param proxy              对应协议的jceStruct
     * @return
     */
    public static JceStruct parseWupResponseByFlgV3(byte[] data, String flg, String encodeName, JceStruct proxy) {
        UniPacket packet = getUniPacketV3(data, encodeName);
        JceStruct jceStruct = null;
        if (null != packet) {
            // v3版本解码
            jceStruct = packet.getByClass(flg, proxy);
//            jceStruct = packet.get(flg);
        }
        return jceStruct;
    }
    
    /**
     * 返回通用标识“rsp”表示的数据对象
     *           -- 服务器返回码为>=0
     * 
     * @param data
     * @return JceStruct
     */
    public static JceStruct parseWupResponseWithResultMore0(byte[] data, String flg) {
        return parseWupResponseWithResultMore0(data, flg, null);
    }
    
    public static JceStruct parseWupResponseWithResultMore0_UTF8(byte[] data, String flg) {
        return parseWupResponseWithResultMore0(data, flg, ENCODE_UTF8);
    }
    
    public static JceStruct parseWupResponseWithResultMore0(byte[] data, String flg, String encoding) {
        UniPacket packet = getuniPacketWithResCodeMore0(data, encoding);
        if (null != packet) {
            return packet.get(flg);
        }
        return null;
    }
    
    /**
     * 获取返回码>=0的数据包
     * 
     * @param data
     * @return UniPacket
     */
    private static UniPacket getuniPacketWithResCodeMore0(byte[] data, String encodeName) {

        if (null == data) {
            return null;
        }

        UniPacket packet = new UniPacket();
        try {
            if (QWupStringUtil.isEmpty(encodeName)) {
                encodeName = ENCODE_DEFAULT;
            }
            packet.setEncodeName(encodeName);
            packet.decode(data);
        } catch (Exception e) {
            QWupLog.e(TAG, "getuniPacketWithResCodeMore0 -> errmsg: "+ e
                    + ", msg: " + e.getMessage());
            return null;
        }

        // 获取返回结果
        Integer result = packet.get("");
        if (null != result && result >= 0) {
            return packet;
        }
        return null;
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getuniPacket(byte[] data, String encodeName) {

        if (null == data) {
            return null;
        }

        UniPacket packet = new UniPacket();
        if (QWupStringUtil.isEmpty(encodeName)) {
            encodeName = ENCODE_DEFAULT;
        }
        try {
            packet.setEncodeName(encodeName);
            packet.decode(data);
            
        } catch (Throwable e) {
            QWupLog.e(TAG, "getuniPacket-> err msg: " + e.getMessage());
            return null;
        }

//        // 获取返回结果
//        Integer result = packet.get("");
//        if (null != result && result == 0) {
//            return packet;
//        }

        return packet;
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getUniPacketV3(byte[] data, String encodeName) {

        if (null == data) {
            return null;
        }

        UniPacket packet = new UniPacket();
        if (QWupStringUtil.isEmpty(encodeName)) {
            encodeName = ENCODE_DEFAULT;
        }
        try {
            packet.useVersion3();
            packet.setEncodeName(encodeName);
            packet.decode(data);
            
        } catch (Exception e) {
            QWupLog.e(TAG, "getUniPacketV3-> err msg: " + e + ", msg:" + e.getMessage());
            return null;
        }
        
        return packet;
    }
   
    /**
     * 按默认编码格式（GBK）获取返回数据包
     *    -- 不checkpacket的 result
     * @param data
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResult(byte[] data) {
        return getuniPacketWithouCheckResult(data, null);
    }
    /**
     * 按默认UTF-8编码格式获取返回数据包
     *     -- 不checkpacket的 result
     * @param data
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResultUTF8(byte[] data) {
        return getuniPacketWithouCheckResult(data, ENCODE_UTF8);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      -- 不checkpacket的 result
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResult(byte[] data, String encodeName) {

        if (null == data) {
            return null;
        }

        UniPacket packet = new UniPacket();
        if (QWupStringUtil.isEmpty(encodeName)) {
            encodeName = ENCODE_DEFAULT;
        }
        try {
            packet.setEncodeName(encodeName);
            packet.decode(data);
            
        } catch (Exception e) {
            QWupLog.e(TAG, "getuniPacketWithouCheckResult-> err msg:" +e 
                    + ", msg: " + e.getMessage());
            return null;
        }

        return packet;
    }
    
    /**
     * 获取默认格式（GBK）的返回数据包的result
     * 
     * @param data
     * @return
     */
    public static int getuniPacketResult(byte[] data) {
     
        return getuniPacketResult(data, null);
    }
    
    /**
     * 获取UTF-8格式的返回数据包的result
     * 
     * @param data
     * @return
     */
    public static int getuniPacketResultUTF8(byte[] data) {
        
        return getuniPacketResult(data, ENCODE_UTF8);
    }
    
    /**
     * 获取返回数据包的result
     * 
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static int getuniPacketResult(byte[] data, String encodeName) {

        UniPacket packet = getuniPacketWithouCheckResult(data, encodeName);

        
        return getUniPacketResult(packet);
    }
    
    /**
     * 获取返回数据包的result
     * 
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static int getUniPacketResult(UniPacket packet) {

        if (packet == null) {
            return DEFAULT_RSP_CODE;
        }
        // 获取返回结果
        Integer result = packet.get("");
        if (null != result) {
            return result;
        }
        
        return DEFAULT_RSP_CODE;
    }
    
    /**
     * 获取v3版本的resultcode
     * @param data
     * @param encodeName
     * @return
     */
    public static int getuniPacketResultV3(byte[] data, String encodeName) {
        
        UniPacket packet = getUniPacketV3(data, encodeName);        
       
        return getUniPacketResultV3(packet);
    }
    
    public static int getUniPacketResultV3(UniPacket packet) {
        if (packet == null) {
            return DEFAULT_RSP_CODE;
        }
        // 获取返回结果
     // 获取返回结果
        Integer result = null;
        try {
            result = Integer.valueOf(-100);
            result = packet.getByClass("", result);
        } catch (Exception e) {
            QWupLog.trace(TAG, e);
        }
        if (null != result) {
            return result;
        }
        return DEFAULT_RSP_CODE;
    }
    
//    public static Integer getuniPacketResultCode(byte[] data, String encodeName) {
//
//        if (null == data) {
//            return null;
//        }
//
//        UniPacket packet = new UniPacket();
//        if (QWupStringUtil.isEmpty(encodeName)) {
//            encodeName = ENCODE_DEFAULT;
//        }
//        try {
//            packet.setEncodeName(encodeName);
//            packet.decode(data);
//            
//        } catch (Exception e) {
//            QWupLog.trace(TAG, e);
//            return null;
//        }
//
//        // 获取返回结果
//        return packet.get("");        
//    }  
    
    /**
     * 根据传入的apnType proxyindex获取wup协议定义的apnType
     *    -- 当前apn的proxyIndex可以通过ApnStatInfo.getCurApnProxyIndex获取
     * @param apnType  客户端定义的apnType
     * @return
     */
    public static int getApnType(int apnType) {
    	
    	switch (apnType) {
		case ApnStatInfo.PROXY_LIST_CMNET:
			return EAPNTYPE._APN_CMNET;
			
		case ApnStatInfo.PROXY_LIST_CMWAP:
			return EAPNTYPE._APN_CMWAP;
			
		case ApnStatInfo.PROXY_LIST_CTNET:
			return EAPNTYPE._APN_CTNET;
			
		case ApnStatInfo.PROXY_LIST_CTWAP:
			return EAPNTYPE._APN_CTWAP;
			
		case ApnStatInfo.PROXY_LIST_UNNET:
			return EAPNTYPE._APN_UNNET;
			
		case ApnStatInfo.PROXY_LIST_UNWAP:
			return EAPNTYPE._APN_UNWAP;
			
		case ApnStatInfo.PROXY_LIST_3GWAP:
			return EAPNTYPE._APN_3GWAP;
			
		case ApnStatInfo.PROXY_LIST_3GNET:
			return EAPNTYPE._APN_3GNET;
		default:
			break;
		}
    	
    	return EAPNTYPE._APN_UNKNOWN;
    }
    
    /**
     * 根据服务器端返回的apntype类型，获取对应缓存list的index值
     * @param apnType   EAPNTYPE
     * @return  ApnStatInfo.PROXY_LIST_CTNET等
     */
    public static int getUserApnProxyIndex(int apnType) {
    
    	switch (apnType) {
    	
		case EAPNTYPE._APN_CMNET:
			return ApnStatInfo.PROXY_LIST_CMNET;
		case EAPNTYPE._APN_CMWAP:
			return ApnStatInfo.PROXY_LIST_CMWAP;
		case EAPNTYPE._APN_CTNET:
			return ApnStatInfo.PROXY_LIST_CTNET;
		case EAPNTYPE._APN_CTWAP:
			return ApnStatInfo.PROXY_LIST_CTWAP;
		case EAPNTYPE._APN_UNNET:
			return ApnStatInfo.PROXY_LIST_UNNET;
		case EAPNTYPE._APN_UNWAP:
			return ApnStatInfo.PROXY_LIST_UNWAP;
		case EAPNTYPE._APN_3GNET:
			return ApnStatInfo.PROXY_LIST_3GNET;
		case EAPNTYPE._APN_3GWAP:
			return ApnStatInfo.PROXY_LIST_3GWAP;

		default:
			break;
		}
    	
    	return ApnStatInfo.PROXY_LIST_UNKOWN;
    }
    
    /**
     * 获取servic端对应的netType
     * @param netType   ApnStatInfo.TYPE_2G
     * @return    ENETTYPE._NET_2G等
     */
    public static int getNetTypeOfService(int netType) {
        
        switch (netType) {
        case ApnStatInfo.TYPE_2G:
            return ENETTYPE._NET_2G;
            
        case ApnStatInfo.TYPE_3G:
            return ENETTYPE._NET_3G;
            
        case ApnStatInfo.TYPE_WIFI:
            return ENETTYPE._NET_WIFI;
            
        case ApnStatInfo.TYPE_4G:
            return ENETTYPE._NET_4G;
            
        default:
            break;
        }
        
        return ENETTYPE._NET_UNKNOWN;
    }
   
    
    /**
     * 将对应的jce对象转换为byte[]
     *   （默认gbk编码）
     * @param jceStruct
     * @return
     */
    public static byte[] parseJceStructToBytes(JceStruct jceStruct) {
        return parseJceStructToBytes(jceStruct, null);
    }
    /**
     * 将对应的jce对象转换为byte[]
     *   （utf-8编码编码）
     * @param jceStruct
     * @return
     */
    public static byte[] parseJceStructToBytesInUTF_8(JceStruct jceStruct) {
        return parseJceStructToBytes(jceStruct, ENCODE_UTF8);
    }
    
    /**
     * 按指定编码将对应的jce对象转换为byte[]
     * @param jceStruct
     * @param encoding
     */
    public static byte[] parseJceStructToBytes(JceStruct jceStruct, String encoding) {
        
        if (jceStruct == null) {
            return null;
        }
        try {
            JceOutputStream jceOutputStream = new JceOutputStream();
            if (!QWupStringUtil.isEmpty(encoding)) {
                jceOutputStream.setServerEncoding(encoding);
            }
            jceStruct.writeTo(jceOutputStream);
            return jceOutputStream.toByteArray();
        } catch (Exception e) {
           QWupLog.w(TAG, "parseJceStructToBytes> err msg:" + e + " msg:" + e.getMessage()
                   +", jcestruct: "+ jceStruct);
        }
        return null;
    }
    
    /**
     * 将byte[]按指定数据格式解析
     *   （默认gbk编码）
     * @param bytes
     * @param jceStruct
     * @return null 解析失败，其他 将传入 jceStruct对象解析并返回
     */
    public static JceStruct parseBytesToJceStruct(byte[] bytes, JceStruct jceStruct) {
        return parseBytesToJceStruct(bytes, jceStruct, null);
    }
    
    /**
     * 将byte[]按指定数据格式解析
     *   （默认UTF-8编码）
     * @param bytes
     * @param jceStruct
     * @return null 解析失败，其他 将传入 jceStruct对象解析并返回
     */
    public static JceStruct parseBytesToJceStructInUTF_8(byte[] bytes, JceStruct jceStruct) {
        return parseBytesToJceStruct(bytes, jceStruct, ENCODE_UTF8);
    }
    
    /**
     * 将byte[]按指定数据格式解析
     * @param bytes
     * @param jceStruct
     * @param encoding
     * 
     * @return null 解析失败，其他 将传入 jceStruct对象解析并返回
     */
    public static JceStruct parseBytesToJceStruct(byte[] bytes, JceStruct jceStruct, String encoding) {
        
        if (bytes == null || bytes.length == 0 || jceStruct == null) {
            return null;
        }
        try {
            JceInputStream jceInputStream = new JceInputStream(bytes);
            if (!QWupStringUtil.isEmpty(encoding)) {
                jceInputStream.setServerEncoding(encoding);
            }
            jceStruct.readFrom(jceInputStream);
            return  jceStruct;
        } catch (Exception e) {
            QWupLog.w(TAG, "parseBytesToJceStruct> err msg:" + e + " msg:" + e.getMessage()
                    +", jcestruct: "+ jceStruct);
        }
        return null;
    }
    /**
     * des编码接口
     * @param key
     * @param data
     * @param flg
     * @return
     */
    public static byte[] encodeDesEncrypt(byte[] key, byte[] data, int flg) {
        return DES.DesEncrypt(key, data, flg);
    }
}
