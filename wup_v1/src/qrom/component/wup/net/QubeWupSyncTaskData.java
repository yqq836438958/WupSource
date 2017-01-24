package qrom.component.wup.net;


/**
 * 同步请求的数据承载  
 * @author wileywang
 */
public class QubeWupSyncTaskData extends QubeWupTaskData {
	private byte[] mResponseBytes;
	private Object mLockObject;
	private int mErrorCode;
	private String mErrorMsg;
	
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public String getErrorMsg() {
		return mErrorMsg;
	}
	
	public byte[] getResponseBytes() {
		return mResponseBytes;
	}
	
	public void setLockObject(Object lockObject) {
		mLockObject = lockObject;
	}
	
	@Override
	public void onDataFinished(byte[] responseData) {
		mResponseBytes = responseData;
	}
	
	@Override
	public void onDataError(int errorCode, String errorMsg) {
		mErrorCode = errorCode;
		mErrorMsg = errorMsg;
	}
	
	@Override
	public boolean isSync() {
		return true;
	}
	
	@Override
	public void onSyncNotify() {
		if (mLockObject != null) {
			synchronized(mLockObject) {
				mLockObject.notify();
			}
		}
	}
}
