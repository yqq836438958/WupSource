package qrom.component.wup.net.httpRequester;

import qrom.component.wup.net.QubeWupTaskData;
import qrom.component.wup.net.base.HttpHeader;
import qrom.component.wup.utils.QWupLog;


public abstract class QubeWupBaseRequester {
	
    private static final String TAG = "QROM-QubeWupBaseRequester";
    public int mErrCode = 0;
    public String mErrMsg;

        
    /**
     * 执行一次wup请求
     *   -- 发送一次 后接收一次
     * @return
     * @throws Exception
     */
    public abstract ResponData excute(QubeWupTaskData taskData) throws Exception;
    
    /**
     * 释放当前连接
     */
    public abstract void cancelConnect();
	
    protected String getProxyHost(String url) {
        return "";
    }     
	
    /**
     * 
     * @param e
     */
    protected void checkOOM(Throwable e) {
        if (e != null  && e instanceof OutOfMemoryError) {
            QWupLog.e(TAG, "OutOfMemoryError  -- kill process");
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        System.gc();
    }
    

    public class ResponData {
        
    	public int mRspStatusCode;
    	public String mRspQQZip;
    	public String mRspQQEncrypt;
    	public String mRspContentEncoding;
    	public long mRspContentLen;
        
    	public byte[] mRspDatas; 
    	
    	public boolean mIsGizStream = false;
        
    	/**
    	 * 判断ContentEncoding是否是gzip
    	 * @return
    	 */
    	public boolean isContentEncodingGzip() {
    		if (mRspContentEncoding != null 
    				&& HttpHeader.WUP_HEADER_GZIP_VALUE.equalsIgnoreCase(mRspContentEncoding.trim())) {
    			return true;
    		}
    		return false;
    	}
    	
    	/**
    	 * 判断是否有自定义加密头
    	 * @return
    	 */
    	public boolean isQQEncryp() {
    		if (mRspQQEncrypt != null 
    				&& HttpHeader.WUP_HEADER_ENCRYPT_VALUE.equalsIgnoreCase(mRspQQEncrypt.trim())) {
    			return true;
    		}
    		return false;
    	}
    	
    	/**
    	 * 判断是否有自定义压缩gzip头
    	 * @return
    	 */
    	public boolean isQQZip() {
    		if (mRspQQZip != null 
    				&& HttpHeader.WUP_HEADER_GZIP_VALUE.equalsIgnoreCase(mRspQQZip.trim())) {
    			return true;
    		}
    		return false;
    	}
    }
    
}
