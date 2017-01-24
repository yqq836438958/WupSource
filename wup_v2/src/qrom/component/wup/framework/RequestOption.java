package qrom.component.wup.framework;

import qrom.component.wup.base.IWorkRunner;

/**
 *  请求的选项配置
 * @author wileywang
 *
 */
public class RequestOption {
	private int mTimeoutMills = 0; // 外界设置的强制超时
	private IWorkRunner mCallbackRunner; // 回调执行的线程
	private int mRetryTimes = 0;  // 网络失败后重试次数
	
	private boolean mIsEncrytRequest = false; // 网络底层是否加密请求
	private boolean mIsForceDefaultRoute = false; // 网络底层强制走默认路由
	
	public RequestOption() {
	}
	
	public int getTimeoutMills() {
		return mTimeoutMills;
	}
	public RequestOption setTimeoutMills(int timeoutMills) {
		mTimeoutMills = timeoutMills;
		return this;
	}
	
	public IWorkRunner getCallbackRunner() {
		return mCallbackRunner;
	}
	
	public RequestOption setCallbackRunner(IWorkRunner runner) {
		mCallbackRunner = runner;
		return this;
	}
	
	public RequestOption setRetryTimes(int retryTimes) {
		this.mRetryTimes = retryTimes;
		return this;
	}
	
	public int getRetryTimes() {
		return mRetryTimes;
	}
	
	public RequestOption setIsEncryptRequest(boolean isEncryptRequest) {
		mIsEncrytRequest = isEncryptRequest;
		return this;
	}
	
	public boolean isEncrypRequest() {
		return mIsEncrytRequest;
	}
	
	public boolean isForceDefaultRoute() {
		return mIsForceDefaultRoute;
	}
	
	public RequestOption setForceDefaultRoute(boolean isForceDefaultRoute) {
		mIsForceDefaultRoute = isForceDefaultRoute;
		return this;
	}
}
