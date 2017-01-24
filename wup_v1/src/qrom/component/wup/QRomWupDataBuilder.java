package qrom.component.wup;


import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.QWupStringUtil;
import android.content.Context;

import com.qq.jce.wup.UniPacket;
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
    
    private QRomWupDataBuilder() {
    }
    
    /**
     * 将16进制字符串转化为字节型数据
     */
    public static byte[] hexStringToByte(String hexString) {
       return QWupStringUtil.hexStringToByte(hexString);
    }
    
    /**
     * 将字节型数据转化为16进制字符串
     */
    public static String byteToHexString(byte[] bytes)  {
        
        return QWupStringUtil.byteToHexString(bytes);
    }
    
    public static byte[] loadGuidFromFile(Context context) {
        return QRomWupDataBuilderImpl.loadGuidFromFile(context);
    }
    
    /**
     * 判断guid是否合法
     * @param guid
     * @return  true : guid合法
     */
    public static boolean isGuidValidate(String guid) {
        return QRomWupDataBuilderImpl.isGuidValidate(guid);
    }
    
    /**
     * 判断guid是否合法
     * @param guid
     * @return   true : guid合法
     */
    public static boolean isGuidValidate(byte[] guid) {
    	return QRomWupDataBuilderImpl.isGuidValidate(guid);
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
        return QRomWupDataBuilderImpl.createReqUnipackage(serverName, functionName, paraFlg, data);
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
        
        return QRomWupDataBuilderImpl.createReqUnipackage(serverName, functionName, paraFlg, data, encodeName);
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
        return QRomWupDataBuilderImpl.createReqUnipackageV3(serverName, functionName, paraFlg, data, ENCODE_DEFAULT);
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

        return QRomWupDataBuilderImpl.createReqUnipackageV3(serverName, functionName, paraFlg, data, encodeName);
    }
    
    /**
     * 将UniPacket解析成对应的byte数组
     * @param packet
     * @return
     */
    public static byte[] parseWupUnipackage2Bytes(UniPacket packet) {
        
        return QRomWupDataBuilderImpl.parseWupUnipackage2Bytes(packet);
    }

    /**
     * 获取指定标识符表示的数据对象
     *     （按默认gbk格式编解码）
     * @param data  byte[]
     * @param flg    获取数据对应的key
     * @return JceStruct
     */
    public static JceStruct parseWupResponseByFlg(byte[] data, String flg) {

    	return QRomWupDataBuilderImpl.parseWupResponseByFlg(data, flg, null);
    }   

    /**
     * 根据指定的编码格式解析数据包
     * @param data                 byte[]
     * @param flg                   获取数据对应的key
     * @param encodeName  编码格式 （为空则按wup默认编码格式解包）
     * @return
     */
    public static JceStruct parseWupResponseByFlg(byte[] data, String flg, String encodeName) {

    	return QRomWupDataBuilderImpl.parseWupResponseByFlg(data, flg, encodeName);
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
        return  QRomWupDataBuilderImpl.parseWupResponseByFlgV3(data, flg, ENCODE_DEFAULT, proxy);
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

        return  QRomWupDataBuilderImpl.parseWupResponseByFlgV3(data, flg, encodeName, proxy);
    }
    
    /**
     * 返回通用标识“rsp”表示的数据对象
     *           -- 服务器返回码为>=0
     * 
     * @param data
     * @return JceStruct
     */
    public static JceStruct parseWupResponseWithResultMore0(byte[] data, String flg) {
        return QRomWupDataBuilderImpl.parseWupResponseWithResultMore0(data, flg, null);
    }
    
    public static JceStruct parseWupResponseWithResultMore0_UTF8(byte[] data, String flg) {
        return QRomWupDataBuilderImpl.parseWupResponseWithResultMore0(data, flg, ENCODE_UTF8);
    }
    
    public static JceStruct parseWupResponseWithResultMore0(byte[] data, String flg, String encoding) {

        return QRomWupDataBuilderImpl.parseWupResponseWithResultMore0(data, flg, encoding);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getuniPacket(byte[] data, String encodeName) {

        return QRomWupDataBuilderImpl.getuniPacket(data, encodeName);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @return
     */
    public static UniPacket getUniPacketV3(byte[] data) {

        
        return QRomWupDataBuilderImpl.getUniPacketV3(data, ENCODE_DEFAULT);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      --响应包正确（result == 0）则返回对应package，其他返回null
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getUniPacketV3(byte[] data, String encodeName) {

        
        return QRomWupDataBuilderImpl.getUniPacketV3(data, encodeName);
    }
   
    /**
     * 按默认编码格式（GBK）获取返回数据包
     *    -- 不checkpacket的 result
     * @param data
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResult(byte[] data) {
        return QRomWupDataBuilderImpl.getuniPacketWithouCheckResult(data, null);
    }
    /**
     * 按默认UTF-8编码格式获取返回数据包
     *     -- 不checkpacket的 result
     * @param data
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResultUTF8(byte[] data) {
        return QRomWupDataBuilderImpl.getuniPacketWithouCheckResult(data, ENCODE_UTF8);
    }
    
    /**
     * 按指定编码格式获取返回数据包
     *      -- 不checkpacket的 result
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static UniPacket getuniPacketWithouCheckResult(byte[] data, String encodeName) {

        return QRomWupDataBuilderImpl.getuniPacketWithouCheckResult(data, encodeName);
    }
    
    /**
     * 获取默认格式（GBK）的返回数据包的result
     * 
     * @param data
     * @return
     */
    public static int getuniPacketResult(byte[] data) {
     
        return QRomWupDataBuilderImpl.getuniPacketResult(data, null);
    }
    
    /**
     * 获取unipancket包的result
     * @param data
     * @return
     */
    public static int getuniPacketResultV3(byte[] data) {
        
        return QRomWupDataBuilderImpl.getuniPacketResultV3(data, null);
    }
    
    /**
     * 获取unipancket 的result
     * @param packet
     * @return
     */
    public static int getUniPacketResultV3(UniPacket packet) {
        return QRomWupDataBuilderImpl.getUniPacketResultV3(packet);
    }
    
    /**
     * 获取unipancket包的result
     * @param data
     * @param encode
     * @return
     */
    public static int getuniPacketResultV3(byte[] data, String encode) {
        return QRomWupDataBuilderImpl.getuniPacketResultV3(data, encode);
    }
    
    /**
     * 获取UTF-8格式的返回数据包的result
     * 
     * @param data
     * @return
     */
    public static int getuniPacketResultUTF8(byte[] data) {
        
        return QRomWupDataBuilderImpl.getuniPacketResult(data, ENCODE_UTF8);
    }
    
    /**
     * 获取返回数据包的result
     * 
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static int getuniPacketResult(byte[] data, String encodeName) {
        return QRomWupDataBuilderImpl.getuniPacketResult(data, encodeName);
    }
    
    /**
     * 获取返回数据包的result
     * 
     * @param data
     * @param encodeName   编码格式 （为空则暂默认编码格式解析）
     * @return
     */
    public static int getUniPacketResult(UniPacket packet) {
        
        return QRomWupDataBuilderImpl.getUniPacketResult(packet);
    }
    
    public static Integer getuniPacketResultCode(byte[] data, String encodeName) {

        // 获取返回结果
        return QRomWupDataBuilderImpl.getuniPacketResult(data, encodeName);
        
    }  
        
    /**
     * 将对应的jce对象转换为byte[]
     *   （默认gbk编码）
     * @param jceStruct
     * @return
     */
    public static byte[] parseJceStructToBytes(JceStruct jceStruct) {
        return QRomWupDataBuilderImpl.parseJceStructToBytes(jceStruct, null);
    }
    /**
     * 将对应的jce对象转换为byte[]
     *   （utf-8编码编码）
     * @param jceStruct
     * @return
     */
    public static byte[] parseJceStructToBytesInUTF_8(JceStruct jceStruct) {
        return QRomWupDataBuilderImpl.parseJceStructToBytes(jceStruct, ENCODE_UTF8);
    }
    
    /**
     * 按指定编码将对应的jce对象转换为byte[]
     * @param jceStruct
     * @param encoding
     */
    public static byte[] parseJceStructToBytes(JceStruct jceStruct, String encoding) {
        
        return QRomWupDataBuilderImpl.parseJceStructToBytes(jceStruct, encoding);
    }
    
    /**
     * 将byte[]按指定数据格式解析
     *   （默认gbk编码）
     * @param bytes
     * @param jceStruct
     * @return null 解析失败，其他 将传入 jceStruct对象解析并返回
     */
    public static JceStruct parseBytesToJceStruct(byte[] bytes, JceStruct jceStruct) {
        return QRomWupDataBuilderImpl.parseBytesToJceStruct(bytes, jceStruct, null);
    }
    
    /**
     * 将byte[]按指定数据格式解析
     *   （默认UTF-8编码）
     * @param bytes
     * @param jceStruct
     * @return null 解析失败，其他 将传入 jceStruct对象解析并返回
     */
    public static JceStruct parseBytesToJceStructInUTF_8(byte[] bytes, JceStruct jceStruct) {
        return QRomWupDataBuilderImpl.parseBytesToJceStruct(bytes, jceStruct, ENCODE_UTF8);
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
        
        return QRomWupDataBuilderImpl.parseBytesToJceStruct(bytes, jceStruct, encoding);
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
        return QRomWupDataBuilderImpl.encodeDesEncrypt(key, data, flg);
    }
    
}
