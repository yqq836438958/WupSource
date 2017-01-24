package qrom.component.wup.apiv2;

/**
 *  WupMethod并不直接暴露给外界，取消请求通过这一层隔离，目的是为了防止调用方自以为是，随意调用WupMethod的start方法
 * @author wileywang
 *
 */
public class WupHandle {
	
	private WupMethod mWupMethod;
	
	public WupHandle(WupMethod method) {
		this.mWupMethod = method;
	}
	
	public void cancel() {
		mWupMethod.cancel();
	}
	
	public long getRequestId() {
		return mWupMethod.getRequestId();
	}
}
