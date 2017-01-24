package qrom.component.wup.transport.http;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.StringUtil;

/**
 *  wup采用Http形式的请求，一次请求在一个HttpSession中进行
 * @author wileywang
 *
 */
public abstract class HttpSession implements Runnable {
	private static final String TAG = HttpSession.class.getSimpleName();
	
	protected static int CONNECTION_TIMEOUTMS_IN_WIFI = 15000; // wifi下连接超时，15s
	protected static int CONNECTION_TIMEOUTMS_IN_34G = 30000; // 3g和4g网络下的连接超时，30s
	protected static int CONNECTION_TIMEOUTMS_IN_2G = 45000; // 2g网络下的连接超时时间
	
	protected static final byte[] QQ_ENCRYPT_BYTES_KEY = {
		0x73, 0x44, 0x66, 0x34, 0x33, 0x34, 0x6F, 0x6C, 0x2A, 
        0x31, 0x32, 0x33, 0x2B, 0x2D, 0x4B, 0x44};
	
	public static class SessionRequest {
		private boolean mIsQQEncrypt = false;  // 是否用特定的QQ加密方式进行加密
		private String mGuid;
		
		private byte[] mPostData;
		private int mSessionTimeoutMs = 60*1000; // 请求的超时时间
		
		private Map<String, String> mUserHeaders;
		
		public byte[] getPostData() {
			return mPostData;
		}
		
		public SessionRequest setPostData(byte[] postData) {
			mPostData = postData;
			return this;
		}
		
		public boolean isQQEncrypt() {
			return mIsQQEncrypt;
		}
		
		public SessionRequest setQQEncrypt(boolean isQQEncrypt) {
			mIsQQEncrypt = isQQEncrypt;
			return this;
		}
		
		public String getGuid() {
			return mGuid;
		}
		
		public SessionRequest setGuid(String guid) {
			mGuid = guid;
			return this;
		}

		public Map<String, String> getUserHeaders() {
			return mUserHeaders;
		}
		
		public SessionRequest putUserHeader(String headerName, String headerValue) {
			if (StringUtil.isEmpty(headerName) || StringUtil.isEmpty(headerValue)) {
				throw new IllegalArgumentException("headerName and headerValue should not be null or empty");
			}
			
			if (mUserHeaders == null) {
				mUserHeaders = new HashMap<String, String>();
			}
			
			mUserHeaders.put(headerName, headerValue);
			return this;
		}
		
		public int getSessionTimeoutMs() {
			return mSessionTimeoutMs;
		}
		
		public SessionRequest setSessionTimeout(int sessionTimeoutMs) {
			mSessionTimeoutMs = sessionTimeoutMs;
			return this;
		}
	}
	
	public static class RespData {
		private int mStatusCode;
    	private String mQQZip;
    	private String mQQEncrypt;
    	private String mContentEncoding;
    	private long mContentLength;
        
    	private byte[] mResponseContent; 
    	
    	public int getStatusCode() {
    		return mStatusCode;
    	}
    	
    	public RespData setStatusCode(int statusCode) {
    		mStatusCode = statusCode;
    		return this;
    	}
    	
    	public boolean isStatusHttpOK() {
    		return mStatusCode == 200;
    	}
    	
    	public long getContentLength() {
    		return mContentLength;
    	}
    	
    	public RespData setContentLength(long contentLength) {
    		mContentLength = contentLength;
    		return this;
    	}
    	
    	public String getQQZip() {
    		return mQQZip;
    	}
    	
    	public String getQQEncrypt() {
    		return mQQEncrypt;
    	}
    	
    	public String getContentEncoding() {
    		return mContentEncoding;
    	}
    	
    	public RespData setContentEncoding(String contentEncoding) {
    		mContentEncoding = contentEncoding;
    		return this;
    	}
    	
    	public byte[] getResponseContent() {
    		return mResponseContent;
    	}
    	
    	public RespData setResponseContent(byte[] responseContent) {
    		mResponseContent = responseContent;
    		return this;
    	}
    	
    	public RespData setQQEncrypt(String qqEncrypt) {
    		mQQEncrypt = qqEncrypt;
    		return this;
    	}
    	
    	public RespData setQQZip(String qqZip) {
    		mQQZip = qqZip;
    		return this;
    	}
    	
    	public boolean isContentEncodingGzip() {
    		if (mContentEncoding != null 
    				&& mContentEncoding.trim().toLowerCase(Locale.getDefault())
    					.contains(HttpHeader.WUP_HEADER_GZIP_VALUE)) {
    			return true;
    		}
    		return false;
    	}
    	
    	/**
    	 * 判断是否有自定义加密头
    	 * @return
    	 */
    	public boolean isQQEncrypt() {
    		if (mQQEncrypt != null 
    				&& HttpHeader.WUP_HEADER_ENCRYPT_VALUE.equalsIgnoreCase(mQQEncrypt.trim())) {
    			return true;
    		}
    		return false;
    	}
    	
    	/**
    	 * 判断是否有自定义压缩gzip头
    	 * @return
    	 */
    	public boolean isQQZip() {
    		if (mQQZip != null 
    				&& HttpHeader.WUP_HEADER_GZIP_VALUE.equalsIgnoreCase(mQQZip.trim())) {
    			return true;
    		}
    		return false;
    	}
	}
	
	public static class SessionResponse {
		private int mErrorCode;
		private String mErrorMsg;
		
		private RespData mRespData;
		
		public int getErrorCode() {
			return mErrorCode;
		}
		public SessionResponse setErrorCode(int errorCode) {
			mErrorCode = errorCode;
			return this;
		}
		
		public String getErrorMsg() {
			return mErrorMsg;
		}
		
		public SessionResponse setErrorMsg(String errorMsg) {
			mErrorMsg = errorMsg;
			return this;
		}
		
		public RespData getRespData() {
			return mRespData;
		}
		
		public SessionResponse setRespData(RespData respData) {
			mRespData = respData;
			return this;
		}
		
	}
	
	public static interface ICallback {
		public void onSessionResponse(final HttpSession httpSession, final SessionResponse sessionResponse);
	}
	
	protected SessionRequest mSessionRequest;
	protected ICallback mCallback;
	protected long mSessionId; 
	protected IHttpRouteChooser mRouteChooser;
	
	protected boolean mIsCancelled;
	
	protected long mSessionCreateTimestampMs;
	
	private long mSessionStartTimeMs;
	private long mSessionRespSize;
	
	public HttpSession(long sessionId
			, SessionRequest sessionRequest
			, IHttpRouteChooser routeChooser
			, ICallback callback) {
		if (StringUtil.isEmpty(sessionRequest.getGuid())) {
			throw new IllegalArgumentException("SessionRequest's guid should not be null or empty");
		}
		if (sessionRequest.getPostData() == null || sessionRequest.getPostData().length <= 0) {
			throw new IllegalArgumentException("SessionRequest's postData should not be null or empty");
		}
		
		mSessionId = sessionId;
		mSessionRequest = sessionRequest;
		mCallback = callback;
		mRouteChooser = routeChooser;
		
		mSessionCreateTimestampMs = System.currentTimeMillis();
	}
	
	public abstract HttpRouteInfo getHttpRouteInfo();
	
	/**
	 *  开始请求到Session实际开始执行的之间
	 * @return
	 */
	public long getSessionStartTimeMs() {
		return mSessionStartTimeMs;
	}
	
	public abstract long getSessionConnectTimeMs();
	public abstract long getSessionSendTimeMs();
	public abstract long getSessionReadTimeMs();
	
	public long getSessionReqSize() {
		if (mSessionRequest.getPostData() == null) {
			return 0;
		}
		
		return mSessionRequest.getPostData().length;
	}
	
	public long getSessionRespSize() {
		return mSessionRespSize;
	}
	
	
	public long getSessionId() {
		return mSessionId;
	}
	
	/**
	 *  不被外界直接调用，HttpSession的取消被IHttpSessionPool触发
	 */
	void cancel() {
		if (mIsCancelled) {
			return ;
		}
		mIsCancelled = true; 
		
		// 注意，这里不要重置任何参数， 可能会造成执行过程中的空指针
		// 通过cancelled标志去控制回调逻辑
		// 这里，不要强行要求一定能够取消成功
		doCancel();
		QRomLog.d(TAG, "HttpSession cancelled, mSessionId=" + mSessionId);
	}
	
	protected abstract void doCancel();
	protected abstract void doExecute();
	
	public void run() {
		if (mIsCancelled) {
			return ;
		}
		
		mSessionStartTimeMs = System.currentTimeMillis() - mSessionCreateTimestampMs;
		doExecute();
	}
	
	protected void onFinished(RespData rspData, int errorCode, String errorMsg) {
		if (mIsCancelled) {
			return ;
		}
		
		if (rspData != null && rspData.getResponseContent() != null) {
			mSessionRespSize = rspData.getResponseContent().length;
		}
		
		if (mCallback != null) {
			try {
				SessionResponse sessionResponse = new SessionResponse();
				sessionResponse.setErrorCode(errorCode);
				sessionResponse.setErrorMsg(errorMsg);
				sessionResponse.setRespData(rspData);
				
				mCallback.onSessionResponse(this, sessionResponse);
				
			} catch (Throwable e) {
				QRomLog.e(TAG, e.getMessage(), e);
			}
			mCallback = null;
		}
	}
	
}
