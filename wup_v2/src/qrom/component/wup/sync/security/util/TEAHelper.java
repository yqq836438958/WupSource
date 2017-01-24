package qrom.component.wup.sync.security.util;

import java.security.SecureRandom;
import java.util.Random;

public class TEAHelper {

    /**
     * 生成指定位数的随机字符串
     * @param num
     * @return
     */
    public static byte[] getRandomKey(int num) {
        if (num <= 0)
            return null;
        
        byte[] key = new byte[num];
        Random random = new SecureRandom();
        random.nextBytes(key);
        
        return key;
    }
}
