package qrom.component.wup.base.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  压缩工具类
 * @author wileywang
 *
 */
public class ZipUtils {
	
    /**
     * 压缩Gzip
     * 
     * @param data
     * @return
     * @throws IOException 
     */
    public static byte[] gZip(byte[] data) throws IOException {
        if (data == null) {
            return null;
        }
        ByteArrayOutputStream bos = null;
        GZIPOutputStream gzip = null;
        try {
            bos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            return  bos.toByteArray();
        } finally {
            try {
                if (gzip != null) {
                    gzip.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /***
     * 解压GZip
     * 
     * @param data
     * @return
     * @throws IOException 
     */
    public static byte[] unGzip(byte[] data) throws IOException {
        if (data == null) {
            return null;
        }
        ByteArrayInputStream bis = null;
        GZIPInputStream gzip = null;
        ByteArrayOutputStream baos = null;
        try {
            bis = new ByteArrayInputStream(data);
            gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            return baos.toByteArray();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (gzip != null) {
                    gzip.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
            }
        }
    }
    
}
