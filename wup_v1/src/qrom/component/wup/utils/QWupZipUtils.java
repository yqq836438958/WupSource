package qrom.component.wup.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class QWupZipUtils {
	
    private static final String TAG = "QWupZipUtils";

    /**
     * 压缩Gzip
     * 
     * @param data
     * @return
     */
    public static byte[] gZip(byte[] data) {
        if (data == null) {
            return null;
        }
        byte[] b = null;
        QWupLog.d(TAG, "gzip begin");
        ByteArrayOutputStream bos = null;
        GZIPOutputStream gzip = null;
        try {
            bos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            
            b = bos.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (gzip != null) {
                    gzip.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        QWupLog.d(TAG, "gzip end");
        return b;
    }

    /***
     * 解压GZip
     * 
     * @param data
     * @return
     */
    public static byte[] unGzip(byte[] data) {
        if (data == null) {
            return null;
        }
        byte[] b = null;
        QWupLog.d(TAG, "unGzip begin");
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
            b = baos.toByteArray();
            baos.flush();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
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
                e.printStackTrace();
            }
            
        }
        QWupLog.d(TAG, "unGzip end");
        return b;
    }
    
}
