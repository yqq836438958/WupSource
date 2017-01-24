package qrom.component.wup;

import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.TEACoding;

import com.tencent.codec.Codec;
import com.tencent.codec.des.DES;

public class QRomWupCodecUtils {
    
    public static final int DES_FLAG_ENCRYPT = DES.FLAG_ENCRYPT;
    public static final int DES_FLAG_DECRYPT = DES.FLAG_DECRYPT;

    
    /**
     * des 加密接口
     * @param key
     * @param data
     * @param flg
     * @return
     */
    public static byte[] desEncode(byte[] key, byte[] data) {
        return QRomWupDataBuilderImpl.encodeDesEncrypt(key, data, DES_FLAG_ENCRYPT);
    }
    
    /**
     * des 解密接口
     * @param key
     * @param data
     * @param flg
     * @return
     */
    public static byte[] desDecode(byte[] key, byte[] data) {
        return QRomWupDataBuilderImpl.encodeDesEncrypt(key, data, DES_FLAG_DECRYPT);
    }
    
    /**
     * tea加密
     * 
     * @param in
     *            需要进行加密的字节数组
     * @return byte[] 加密后的字节数组
     */
    public static byte[] teaEncode(byte[]key, byte[] inDatas) {
        TEACoding teaCoding = new TEACoding(key);
        return teaCoding.encode(inDatas);
    }
    
    /**
     * tea解密
     * 
     * @param code
     *            加密后的字节数组
     * @return byte[] 解密后的字节数组
     */
    public static byte[] teaDecode(byte[]key, byte[] datas) {
        TEACoding teaCoding = new TEACoding(key);
        return teaCoding.decode(datas);
    }
    
    /**
     * codec 编码
     * @param key
     * @param datas
     * @return
     */
    public static byte[] codecEncode(byte[]key, byte[] datas) {
        Codec codec = new Codec();
        return codec.encodeTAF(datas, key);
    }
    
    /**
     * codec 解码
     * @param key
     * @param datas
     * @return
     */
    public static byte[] codecDecode(byte[]key, byte[] datas) {
        Codec codec = new Codec();
        return codec.decodeTAF(datas, key);
    }
}
