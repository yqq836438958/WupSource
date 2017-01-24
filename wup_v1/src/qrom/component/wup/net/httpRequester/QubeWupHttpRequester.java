package qrom.component.wup.net.httpRequester;

import java.util.HashMap;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.net.QubeWupTask;
import qrom.component.wup.net.QubeWupTaskData;
import qrom.component.wup.net.base.HttpHeader;
import qrom.component.wup.utils.QWupZipUtils;
import qrom.component.wup.utils.QWupSdkConstants;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.TEACoding;


public class QubeWupHttpRequester extends QubeWupBaseRequester{
	
    private static final String TAG = "QROM-QubeWupHttpRequester";
    
    /** 默认wup 请求需添加的header信息 */
    protected static HashMap<String, String> DEFALUT_HEADER = new HashMap<String, String>(5);
    
    public QubeWupHttpRequester(byte[] vGuid) {
        initHeader(vGuid);
    }    
    
    
    /**
     * 初始化默认httpheader
     */
    private void initHeader(byte[] vGuid) {
        if (DEFALUT_HEADER.isEmpty()) {
            DEFALUT_HEADER.put(HttpHeader.REQ.CONTENT_TYPE, HttpHeader.CONTENT_TYPE);
            DEFALUT_HEADER.put(HttpHeader.REQ.QGUID, QWupStringUtil.byteToHexString(vGuid));
//            String host = getProxyHost(mTaskData.mUrl);
//            QWupLog.w(TAG, "host = " + host);
//            DEFALUT_HEADER.put(HttpHeader.REQ.HOST, host);
//            post.setHeader(HttpHeader.REQ.QUA, getQUA());
            // 支持压缩
            DEFALUT_HEADER.put(HttpHeader.REQ.ACCEPT_ENCODING, HttpHeader.WUP_HEADER_GZIP_VALUE);
            DEFALUT_HEADER.put(HttpHeader.REQ.QQ_S_ZIP,  HttpHeader.WUP_HEADER_GZIP_VALUE);
        }
    }
    
    /**
     * 执行wup请求
     *    -- 1 . 调用 processRequestWupTaskData 处理请求数据
     *        2.  调用 doHttpRequest 发送http请求
     *        3.  processResponseWupTaskData 处理返回数据
     * @return
     * @throws Exception
     */
    public ResponData excute(QubeWupTaskData taskData) throws Exception {
    
    	// 请求网络
		ResponData responData = null;

		// 处理请求数据
		if (!processRequestWupTaskData(taskData)) { // 数据处理失败
			return null;
		}

		// 请求网络
		responData = doHttpRequest(taskData);

		// 处理返回数据
		processResponseWupTaskData(responData);

    	return responData;
    }
    
    /**
     * 发送http网络请求
     * 
     * @return
     */
    protected ResponData doHttpRequest(QubeWupTaskData taskData) {
    	
    	return null;
    }
    
    @Override
    public void cancelConnect() {
        
    }
    
    
	/**
	 * 处理wup请求数据
	 *   -- 默认都压缩，根据标志位判断是否加密
	 * @param taskData
	 */
    protected boolean processRequestWupTaskData(QubeWupTaskData taskData) {
		if (taskData == null || taskData.isDataEmpty()) {  // 数据为空
			mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_REQDATA_EMPTY;
			mErrMsg = "req taskData is empty";
			return false;
		}
		
		// --压缩
		taskData.mData = QWupZipUtils.gZip(taskData.mData);
       
        if (taskData.mIsEncrpt) {
            QWupLog.trace( TAG, " QubeWupTask -- 加密信息");
            TEACoding enTeaCoding = new TEACoding(QWupSdkConstants.WUP_ENCRYPT_BYTES_KEY);
            // -- 加密
            taskData.mData = enTeaCoding.encode(taskData.mData);
        }
        
        if (taskData.isDataEmpty()) {
        	mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_REQDATA_EMPTY;
        	mErrMsg = " req taskData gzip or encrpt err";
        	return false;
        }
        return true;
	}
    
    
    /**
	 * 处理wup请求返回数据
	 *    -- 判断是否需要进行解压和解密操作
	 * @param responData
	 */
    protected void processResponseWupTaskData(ResponData responData) {
		
    	if (mErrCode != 0) {  // 有其他错误
    		return;
    	}
    	
		if (responData == null) { //未出现其他错误，数据为空 
			mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY;
			mErrMsg =" rsponse is parse null";
			return;
		}
		
		if (responData.mRspStatusCode != QubeWupTask.RESPONSE_OK) {  // 服务器返回错误码
			mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_SERVICE_RSPCODE;
			mErrMsg = "response err code: " + responData.mRspStatusCode;
			return;
		}
		
		if (responData.mRspDatas == null || responData.mRspDatas.length == 0) {
			mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY;
			mErrMsg =" rsponse data is empty";
			return;
		}
		
		byte[] bytes = responData.mRspDatas;
		if (!responData.mIsGizStream && responData.isContentEncodingGzip()) {  // 标准协议头gzip
			QWupLog.e(TAG, "------ unGzip ------");
			bytes =  QWupZipUtils.unGzip(bytes);
		}
		
		if (responData.isQQEncryp()) {  // 加密数据
			QWupLog.trace( TAG, "QubeWupTask -- 解密信息");
            TEACoding deTeaCoding = new TEACoding(QWupSdkConstants.WUP_ENCRYPT_BYTES_KEY);
            // -- 解密
            bytes = deTeaCoding.decode(bytes);
		}
		
		if (responData.isQQZip()) {  // 自定义压缩
			 // -- 解压
            bytes = QWupZipUtils.unGzip(bytes);
		}
		
		if (bytes == null || bytes.length == 0) {
			mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_RSP_PARSE;
			mErrMsg = "respone data gzip or decode fails";
		}
		responData.mRspDatas = bytes;
	}
	
    protected String getProxyHost(String url) {
        
        if (!QWupStringUtil.isEmpty(url)) {             
            return url.replaceFirst("http://+", "");
        }
        return QWupSdkConstants.REMOTE_WUP_PROXY.replaceFirst("http://+", "");
    }
    
	
	
	protected long getLongValue(String value) {
		
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

    
}
