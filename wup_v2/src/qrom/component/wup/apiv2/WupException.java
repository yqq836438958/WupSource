package qrom.component.wup.apiv2;

/**
 *  wup的异常接口
 * @author wileywang
 *
 */
public class WupException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private int mErrorCode;
	
	public WupException(int errorCode, String errorMsg) {
		super(errorMsg);
		this.mErrorCode = errorCode;
	}
	
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public String getErrorMsg() {
		return super.getMessage();
	}
}
