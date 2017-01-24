/**   
* @Title: QubeStringUtil.java
* @Package com.tencent.qube.utils 
* @author interzhang   
* @date 2012-5-16 下午01:18:02 
* @version V1.0   
*/
package qrom.component.wup.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class QWupStringUtil {
    
    
    private QWupStringUtil() {
    }
    
    /**yyy
     * 判断字符串是否为空
     * @param src
     * @return
     */
    public static boolean isEmpty(String src) {
        if (src == null || src.length() == 0) {
            return true;
        }
        return false;
    }
    
    /**yyy
     * 判断是否有中文
     */
    public static boolean hasNotAscII(String aInput) {
        
        int length = aInput.length();
        
        for (int i = 0; i < length; i++)  {
            if (aInput.charAt(i) > 255) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 将字节型数据转化为16进制字符串
     */
    public static String byteToHexString(byte[] bytes)  {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        StringBuffer buf = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            if ((bytes[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString(bytes[i] & 0xff, 16));
        }
        return buf.toString();
    }
    
    /**
     * 将16进制字符串转化为字节型数据
     */
    public static byte[] hexStringToByte(String hexString) {
        if (hexString == null || hexString.equals("") || hexString.length() % 2 != 0)
        {
            return null;
        }
        byte[] bData = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bData[i / 2] = (byte) (Integer.parseInt(hexString.substring(i, i + 2), 16) & 0xff);
        }
        return bData;
    }
    
    /**
     * 获取对应时间的yyyy-MM-dd格式
     * @param time
     * @return
     */
    public static String getDateForYYYYMMDD(long time) {
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());  
        Date d1=new Date(time);  
        return format.format(d1); 
    }
}
