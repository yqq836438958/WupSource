package qrom.component.wup.autoretry;

import java.util.HashMap;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.core.DispatcherFactory;
import qrom.component.wup.framework.IModule;
import qrom.component.wup.framework.IRespModule;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Response;
import qrom.component.wup.framework.core.IRequestCallback;

/**
 * 重试模块, 这个模块会尝试重新请求
 * 
 * 注意，不要再这个模块里面加什么网络策略去重试之类的。可以通过控制Request中重试次数，来触发重试.
 * @author wileywang
 *
 */
public class RetryModule implements IRespModule, IRequestCallback {
	private static final String TAG = RetryModule.class.getSimpleName();
	
	private static final String KEY_REAL_REQUEST_ID = "retry_real_request_id";
	
	private static class RetryEntry {
		private long mRetryRequestId;  // 重试的请求Id
		
		private long mRealRequestId; // 重试目的请求的Id
		private IModuleProcessor<IRespModule.Param> mModuleProcessor;
		private IRespModule.Param mRealRespParam;
		
		public RetryEntry( Request retryRequest
					, long realRequestId
					, IModuleProcessor<IRespModule.Param> moduleProcessor
					, IRespModule.Param respParam) {
			
			this.mRealRequestId = realRequestId;
			this.mModuleProcessor = moduleProcessor;
			this.mRealRespParam = respParam;
		}

		public long getRetryRequestId() {
			return mRetryRequestId;
		}
		
		public void setRetryRequestId(long retryRequestId) {
			this.mRetryRequestId = retryRequestId;
		}
		
		public long getRealRequestId() {
			return mRealRequestId;
		}
		
		public IModuleProcessor<IRespModule.Param> getModuleProcessor() {
			return mModuleProcessor;
		}
		
		public IRespModule.Param getRealRespParam() {
			return mRealRespParam;
		}
	}
	
	private IWorkRunner mWorkRunner;
	private Map<Long, RetryEntry> mRetryEntries;
	
	public RetryModule(IWorkRunner workRunner) {
		mWorkRunner = workRunner;
		mRetryEntries = new HashMap<Long, RetryEntry>();
	}
	
	@Override
	public Result onProcess(
			long reqId,
			IRespModule.Param param,
			IModuleProcessor<IRespModule.Param> processor) {
		if (!isNeedRetryRequest(param)) {
			return IModule.RESULT_OK;
		}
		
		if (doRetryRequest(reqId, processor, param)) {
			return IModule.RESULT_PENDING;
		}
		return IModule.RESULT_OK;
	}

	@Override
	public void cancel(long reqId) {
		RetryEntry retryEntry = mRetryEntries.remove(reqId);
		if (retryEntry == null) {
			return ;
		}
		
		DispatcherFactory.getDefault().cancel(retryEntry.getRetryRequestId());
	}

	@Override
	public String getModuleName() {
		return RetryModule.class.getSimpleName();
	}

	@Override
	public void onRequestFinished(long requestId, Request request, Response response) {
		Object realRequestIdObj = request.getUserData().getData(KEY_REAL_REQUEST_ID);
		if (realRequestIdObj == null) {
			QRomLog.w(TAG, "onRequestFinished unexpected! can not get user data key=" + KEY_REAL_REQUEST_ID);
			return ;
		}
		long realRequestId = (Long)realRequestIdObj;
		RetryEntry entry = mRetryEntries.remove(realRequestId);
		if (entry == null) {
			QRomLog.d(TAG, "onRequestFinished get retry entry is null for real request id=" + realRequestId
					+ ", maybe cancelled");
			return ;
		}
		
		if (response.getErrorCode() != 0) {
			// 非返回成功，并且还具备重试次数，则进行继续重试
			if (entry.getRealRespParam().getRequest().getRequestOption().getRetryTimes() > 0) {
				if (doRetryRequest(entry.getRealRequestId()
						, entry.getModuleProcessor(), entry.getRealRespParam())) {
					return ;
				}
			}
		} 
		
		entry.getRealRespParam().getResponse().setErrorCode(response.getErrorCode());
		entry.getRealRespParam().getResponse().setErrorMsg(response.getErrorMsg());
		entry.getRealRespParam().getResponse().setSubErrorCode(response.getSubErrorCode());
		entry.getRealRespParam().getResponse().setResponseContent(response.getResponseContent());
		
		entry.getModuleProcessor().continueProcess(this, realRequestId, entry.getRealRespParam());
	}
	
	private boolean isNeedRetryRequest(IRespModule.Param respParam) {
		// 底层请求成功, 不需要重试
		if (respParam.getResponse().getErrorCode() == 0) {
			return false;
		}
		
		if (respParam.getRequest().getRequestOption().getRetryTimes() <= 0) {
			return false;
		}
		if (respParam.getRequest().getUserData().getData(KEY_REAL_REQUEST_ID) != null) {
			// 本身就是一个重试请求，不要再继续拦截，否则造成重试死循环
			return false;
		}
		
		return true;
	}
	
	private boolean doRetryRequest(long realRequestId
						, IModuleProcessor<IRespModule.Param> moduleProcessor
						, IRespModule.Param realRespParam) {
		Request realRequest = realRespParam.getRequest();
		
		Request retryRequest = new Request(realRequest.getPacketEncodeData()
						, realRequest.getServiceName(), realRequest.getFuncName()
						, realRequest.getRequestType());
		retryRequest.setAppPkgInfo(realRequest.getAppPkgInfo());
		retryRequest.setRequestEnvType(realRequest.getRequestEnvType());
		retryRequest.getRequestOption().setIsEncryptRequest(realRequest.getRequestOption().isEncrypRequest());
		retryRequest.getRequestOption().setForceDefaultRoute(realRequest.getRequestOption().isForceDefaultRoute());
		retryRequest.getRequestOption().setTimeoutMills(realRequest.getRequestOption().getTimeoutMills());
		
		retryRequest.getRequestOption().setCallbackRunner(mWorkRunner);
		retryRequest.getUserData().putData(KEY_REAL_REQUEST_ID, realRequestId);
		
		// 减少原始请求的重试次数
		realRequest.getRequestOption().setRetryTimes(realRequest.getRequestOption().getRetryTimes() - 1);
		
		RetryEntry retryEntry = new RetryEntry(retryRequest, realRequestId, moduleProcessor, realRespParam);
		
		// 这个必须先放，因为回调可能在同一个线程里面
		mRetryEntries.put(realRequestId, retryEntry);
		long retryRequestId = DispatcherFactory.getDefault().send(retryRequest, this);
		if (retryRequestId <= 0) {
			mRetryEntries.remove(realRequestId);
			return false;
		}
		retryEntry.setRetryRequestId(retryRequestId);
		
		return true;
	}
	
}
