package qrom.component.wup;


import qrom.component.log.QRomLog;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.guid.GuidProxy;
import android.content.Context;

import com.qq.jce.wup.UniPacket;
import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import com.tencent.codec.des.DES;

public final class QRomWupDataBuilder {

    public static final String TAG = "QRomWupDataBuilder";
    
    public static final String ENCODE_UTF8 = "UTF-8";
    public static final String ENCODE_DEFAULT = "UTF-8";
    
    @Deprecated
    public static final int DES_FLAG_ENCRYPT = DES.FLAG_ENCRYPT;
    @Deprecated
    public static final int DES_FLAG_DECRYPT = DES.FLAG_DECRYPT;
    
    private static final int DEFAULT_RSP_CODE = -99;
    
    private QRomWupDataBuilder() {
    }
    
    /**
     * 将16进制字符串转化为字节型数据
     */
    public static byte[] hexStringToByte(String hexString) {
       return StringUtil.hexStringToByte(hexString);
    }
    
    /**
     * 将字节型数据转化为16进制字符串
     */
    public static String byteToHexString(byte[] bytes)  {
        return StringUtil.byteToHexString(bytes);
    }
    
    // 这个API就不应该被公布在外面，而且还放在DataBuilder里面，就更加不合理了
    @Deprecated
    public static byte[] loadGuidFromFile(Context context) {
    	ContextHolder.setApplicationContext(context);
    	return GuidProxy.get().getGuidBytes();
    }
    
    // 这里实现提到一处就够了，不知道为什么要转那么多遍，增加代码阅读难度
    /**
     * 判断guid是否合法
     * @param guid
     * @return  true : guid合法
     */
    public static boolean isGuidValidate(String guid) {
    	 if (guid != null 
    			&& (guid.length() < QRomWupConstants.DEFAULT_GUID_LEN * 2 
    				|| guid.length() % (QRomWupConstants.DEFAULT_GUID_BASE_LEN  * 2) != 0)) {
             return false;
         }
         return !StringUtil.isEmpty(guid) && !QRomWupConstants.DEFAULT_GUID_STR.equals(guid);
    }
    
    /**
     * 判断guid是否合法
     * @param guid
     * @return   true : guid合法
     */
	public static boolean isGuidValidate(byte[] guid) {
		if (guid != null
				&& (guid.length < QRomWupConstants.DEFAULT_GUID_LEN 
						|| guid.length % QRomWupConstants.DEFAULT_GUID_BASE_LEN != 0)) {
			return false;
		}
		
		return isGuidValidate(StringUtil.byteToHexString(guid));
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
        return createReqUnipackage(serverName, functionName, paraFlg, data, ENCODE_DEFAULT);
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
    	return createReqUnipackage(serverName, functionName, paraFlg, data, encodeName, false);
    }
    
    /**
     * 创建wup请求包(V3版本)<p>
     *     -- 回包数据请使用V3版本数据接口解包（parseWupResponseByFlgV3）
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
     *    -- 回包数据请使用V3版本数据接口解包（parseWupResponseByFlgV3）
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
    	return createReqUnipackage(serverName, functionName, paraFlg, data, encodeName, true);
    }
    
    private static UniPacket createReqUnipackage(String serverName
    		, String functionName
    		, String paraFlg
    		, Object data
    		, String encodeName
    		, boolean isV3) {
    	if (data == null) {
			return null;
		}

		UniPacket packet = new UniPacket();
		if (StringUtil.isEmpty(encodeName)) {
			encodeName = ENCODE_DEFAULT;
		}
		packet.setEncodeName(encodeName);
		packet.setServantName(serverName);
		packet.setFuncName(functionName);
		// 用v3版本编码
		if (isV3) {
			packet.useVersion3();
		}
		if (StringUtil.isEmpty(paraFlg)) {
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
            QRomLog.w(TAG, e);
        }
        
        return datas;
    }

    /**
     * 获取指定标识符表示的数据对象
     *       （按默认gbk格式编解码）|| (wileywang@2015-06-17 comment modify)坑爹的注释，实际实现还是走的utf8,
     * @param data  byte[]
     * @param flg    获取数据对应的key
     * @return JceStruct
     */
    public static JceStruct parseWupResponseByFlg(byte[] data, String flg) {
    	return parseWupResponseByFlg(data, flg, null);
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
     *  解析V3编码格式数据包(默认UTF-8)<p>
     *     -- 当请求包使用v3请求数据时(createReqUnipackageV3)，回包数据请使用该接口
     * @param data       byte[]
     * @param flg          获取数据对应的key
     * @param proxy    对应协议的jceStruct
     * @return
     */
    public static JceStruct parseWupResponseByFlgV3(byte[] data, String flg, JceStruct proxy) {
        return parseWupResponseByFlgV3(data, flg, ENCODE_DEFAULT, proxy);
    }
    
    /**
     * 解析V3编码格式数据包<p>
     * 
     *     -- 当请求包使用v3请求数据时(createReqUnipackageV3)，回包数据请使用该接口
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
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getuniPacket(byte[] data, String encodeName) {
    	return getUniPacketInner(data, encodeName, false);
    }
    
    private static UniPacket getuniPacketWithResCodeMore0(byte[] data, String encodeName) {
    	UniPacket packet = getuniPacket(data, encodeName);
    	if (packet == null) {
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
     * @return
     */
    public static UniPacket getUniPacketV3(byte[] data) {
        return getUniPacketV3(data, ENCODE_DEFAULT);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getUniPacketV3(byte[] data, String encodeName) {
    	 return getUniPacketInner(data, encodeName, true);
    }
   
    /**
     * 按默认编码格式（GBK）获取返回数据包 || (wileywang@2015-06-17 comment modify)坑爹的注释，实际实现还是走的utf8,
     *    -- 不checkpacket的 result
     * @param data
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResult(byte[] data) {
        return getuniPacket(data, null);
    }
    /**
     * 按默认UTF-8编码格式获取返回数据包
     *     -- 不checkpacket的 result
     * @param data
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResultUTF8(byte[] data) {
        return getuniPacket(data, ENCODE_UTF8);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      -- 不checkpacket的 result
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResult(byte[] data, String encodeName) {
        return getuniPacket(data, encodeName);
    }
    
    /**
     * 获取默认格式（GBK）的返回数据包的result
     * 		|| (wileywang@2015-06-17 comment modify)坑爹的注释，实际实现还是走的utf8,
     * @param data
     * @return
     */
    public static int getuniPacketResult(byte[] data) {
        return getuniPacketResult(data, null);
    }
    
    /**
     * 获取unipancket包的result
     * @param data
     * @return
     */
    public static int getuniPacketResultV3(byte[] data) {
        return getuniPacketResultV3(data, null);
    }
    
    /**
     * 获取unipancket 的result
     * @param packet
     * @return
     */
    public static int getUniPacketResultV3(UniPacket packet) {
    	if (packet == null) {
            return DEFAULT_RSP_CODE;
        }
        // 获取返回结果
        Integer result = null;
        try {
            result = Integer.valueOf(-100);
            result = packet.getByClass("", result);
        } catch (Exception e) {
            QRomLog.trace(TAG, e);
        }
        if (null != result) {
            return result;
        }
        return DEFAULT_RSP_CODE;
    }
    
    /**
     * 获取unipancket包的result
     * @param data
     * @param encode
     * @return
     */
    public static int getuniPacketResultV3(byte[] data, String encode) {
    	return getUniPacketResultV3(getUniPacketV3(data, encode));
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
    	return getUniPacketResult(getuniPacket(data, encodeName));
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
    
    public static Integer getuniPacketResultCode(byte[] data, String encodeName) {
        // 获取返回结果
        return getuniPacketResult(data, encodeName);
    }  
        
    /**
     * 将对应的jce对象转换为byte[]
     *   （默认gbk编码）
     * @param jceStruct
     * @return
     */
    @Deprecated
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
             if (!StringUtil.isEmpty(encoding)) {
                 jceOutputStream.setServerEncoding(encoding);
             }
             jceStruct.writeTo(jceOutputStream);
             return jceOutputStream.toByteArray();
         } catch (Exception e) {
            QRomLog.e(TAG, "parseJceStructToBytes> err msg:" + e + " msg:" + e.getMessage()
                    +", jcestruct: "+ jceStruct, e);
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
             if (!StringUtil.isEmpty(encoding)) {
                 jceInputStream.setServerEncoding(encoding);
             }
             jceStruct.readFrom(jceInputStream);
             return  jceStruct;
         } catch (Exception e) {
             QRomLog.w(TAG, "parseBytesToJceStruct> err msg:" + e + " msg:" + e.getMessage()
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
    @Deprecated
    public static byte[] encodeDesEncrypt(byte[] key, byte[] data, int flg) {
    	return DES.DesEncrypt(key, data, flg);
    }
    
    private static UniPacket getUniPacketInner(byte[] data, String encodeName, boolean isV3) {
    	if (null == data) {
            return null;
        }

        UniPacket packet = new UniPacket();
        if (StringUtil.isEmpty(encodeName)) {
            encodeName = ENCODE_DEFAULT;
        }
        try {
        	if (isV3) {
        		packet.useVersion3();
        	}
            packet.setEncodeName(encodeName);
            packet.decode(data);
            
        } catch (Exception e) {
            QRomLog.e(TAG, "getUniPacketInner-> err msg: " + e + ", msg:" + e.getMessage(), e);
            return null;
        }
        
        return packet;
    }
}
