package qrom.component.wup.security;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.sync.security.AsymCipherManager;

class AsymSessionTask implements Runnable {
	private static final String TAG = AsymSessionTask.class.getSimpleName();
	
	private IWorkRunner mWorkRunner;
	private IAsymSessionCallback mCallback;
	private long mRequestId;
	private String mPackageName;
	private AsymCipherManager mAsymClipherManager;
	private RunEnvType mEnvType;
	
	public AsymSessionTask(
			long requestId
			, String packageName
			, RunEnvType envType
			, AsymCipherManager asymClipherManager
			, IWorkRunner workRunner
			, IAsymSessionCallback callback) {
		if (StringUtil.isEmpty(packageName)) {
			throw new IllegalArgumentException("packageName should not be null");
		}
		if (asymClipherManager == null) {
			throw new IllegalArgumentException("asymClipherManager should not be null");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback should not be null");
		}
		
		this.mRequestId = requestId;
		this.mPackageName = packageName;
		this.mEnvType = envType;
		this.mAsymClipherManager = asymClipherManager;
		this.mWorkRunner = workRunner;
		this.mCallback = callback;
	}
	
	@Override
	public void run() {
		String sessionId = null;
		int retryCount = 1;
		do {
			sessionId = mAsymClipherManager.getSessionId(mPackageName, mEnvType);
			if (!StringUtil.isEmpty(sessionId)) {
				break;
			}
		} while((retryCount--) > 0);
		
		if (StringUtil.isEmpty(sessionId)){
			onFinished(null, WUP_ERROR_CODE.WUP_TASK_ERR_GET_SESSION_FAIL, "asym encrpt get session failed!");
			return ;
		}
		
		onFinished(sessionId, 0, "");
	}
	
	private void onFinished(final String sessionId, final int errorCode, final String errorMsg) {
		if (mWorkRunner != null && mWorkRunner.getThread() != Thread.currentThread()) {
			mWorkRunner.postWork(new Runnable() {
				@Override
				public void run() {
					onCallback(sessionId, errorCode, errorMsg);
				}
			});
		} else {
			onCallback(sessionId, errorCode, errorMsg);
		}
	}
	
	private void onCallback(String sessionId, int errorCode, String errorMsg) {
		try {
			mCallback.onAsymSessionCallback(mRequestId, sessionId, errorCode, errorMsg);
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
		}
	}

}
