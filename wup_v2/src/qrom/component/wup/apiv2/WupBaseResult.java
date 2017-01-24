package qrom.component.wup.apiv2;

/**
 *  请求结果的公共描述
 * @author wileywang
 *
 */
public class WupBaseResult {
	private int mErrorCode;
	private String mErrorMsg;
	
	private long mRequestId;  // 结果对应的请求ID
	
	public WupBaseResult() {
		this(0, "");
	}
	
	public WupBaseResult(int errorCode, String errorMsg) {
		mErrorCode = errorCode;
		mErrorMsg = errorMsg;
	}
	
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public String getErrorMsg() {
		return mErrorMsg;
	}
	
	public long getRequestId() {
		return mRequestId;
	}
	
	public void setRequestId(long requestId) {
		this.mRequestId = requestId;
	}
	
}
