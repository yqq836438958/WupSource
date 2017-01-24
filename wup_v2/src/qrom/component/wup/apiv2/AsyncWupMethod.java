package qrom.component.wup.apiv2;

import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.threads.TempThreadPool;

/**
 * 公开异步回调模型的异步wup方法
 * @author wileywang
 *
 */
public abstract class AsyncWupMethod extends WupMethod {
	
	protected AsyncWupMethod(String servantName, String funcName,
			AsyncWupOption wupOption) {
		super(servantName, funcName, wupOption);
	}
	
	@Override
	protected void handleError(final int errorCode, final String errorMsg) {
		if (mRequestId <= 0) {
			// 说明是取消的请求
			return ;
		}
		
		if (getCallbackRunner() != null) {
			getCallbackRunner().postWork(new Runnable() {
				@Override
				public void run() {
					onError(errorCode, errorMsg);
				}
				
			});
		} else {
			TempThreadPool.get().post(new Runnable() {

				@Override
				public void run() {
					onError(errorCode, errorMsg);
				}
				
			});
		}
	}
	
	private IWorkRunner getCallbackRunner() {
		return ((AsyncWupOption)mWupOption).getCallbackRunner();
	}
	
	@Override
	protected void handleFinished() {
		// 说明是取消的请求
		if (mRequestId <= 0) {
			return ;
		}
		
		if (getCallbackRunner() != null) {
			getCallbackRunner().postWork(new Runnable() {
				@Override
				public void run() {
					onFinished();
				}
				
			});
		} else {
			TempThreadPool.get().post(new Runnable() {

				@Override
				public void run() {
					onFinished();
				}
			});
		}
	}

	protected void onError(int errorCode, String errorMsg) {
	}
	
	protected void onFinished() {
	}
	
}
