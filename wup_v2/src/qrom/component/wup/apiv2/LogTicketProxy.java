package qrom.component.wup.apiv2;

import android.os.Handler;
import qrom.component.wup.apiv2.WupOption.WupType;
import qrom.component.wup.base.RunEnv;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.android.HandlerWorkRunner;
import TRom.log.GetTicketReq;
import TRom.log.LogReportStubAndroid;
import TRom.log.LogReportStubAndroid.GetTicketResult;

/**
 *  获取Log上报Ticket的类， 主要屏蔽一些和后台的细节
 * @author wileywang
 *
 */
public class LogTicketProxy {
	
	public static interface LogTicketProxyCallback {
		public void onTicketCallback(RunEnvType envType, GetTicketResult result);
	}
	
	private static class TicketCallbackProxy implements LogReportStubAndroid.IGetTicketCallback {
		private LogTicketProxyCallback mCallback;
		private RunEnvType mEnvType;
		
		public TicketCallbackProxy(RunEnvType envType, LogTicketProxyCallback callback) {
			this.mCallback = callback;
			this.mEnvType = envType;
		}
		
		@Override
		public void onGetTicketCallback(GetTicketResult result) {
			mCallback.onTicketCallback(mEnvType, result);
		}
		
	}
	
	private LogReportStubAndroid mStub;
	private RomBaseInfoBuilder mRomBaseInfoBuilder;
	
	public LogTicketProxy() {
		mStub = new LogReportStubAndroid("treport");
	}
	
	public void setRomBaseInfoBuilder(RomBaseInfoBuilder builder) {
		mRomBaseInfoBuilder = builder;
	}
	
	public RomBaseInfoBuilder getRomBaseInfoBuilder() {
		if (mRomBaseInfoBuilder == null) {
			mRomBaseInfoBuilder = new RomBaseInfoBuilder();
		}
		return mRomBaseInfoBuilder;
	}
	
	public void asyncGetTicket(LogTicketProxyCallback callback) throws WupException {
		asyncGetTicket(callback, null);
	}
	
	public void asyncGetTicket(LogTicketProxyCallback callback, Handler callbackHandler) throws WupException{
		asyncGetTicket(callback, 0, callbackHandler);
	}
	
	public void asyncGetTicket(LogTicketProxyCallback callback, int timeoutMs, Handler callbackHandler) throws WupException {
		if (callback == null) {
			throw new IllegalArgumentException("callback should not be null");
		}
		
		GetTicketReq req = new GetTicketReq();
		req.setStRomBaseInfo(getRomBaseInfoBuilder().build());
		
		AsyncWupOption wupOption = null;
		if (callbackHandler != null) {
			wupOption = new AsyncWupOption(WupType.WUP_ASYM_ENCRYPT_REQUEST
					, new HandlerWorkRunner(callbackHandler.getLooper()));
		} else {
			wupOption = new AsyncWupOption(WupType.WUP_ASYM_ENCRYPT_REQUEST);
		}
		
		RunEnvType envType = RunEnv.get().getEnvType();
		wupOption.setRequestEnvType(envType);
		wupOption.setRequestPkgInfo("report");
		if (timeoutMs > 0) {
			wupOption.setTimeoutMs(timeoutMs);
		}
		
		mStub.asyncGetTicket(req, new TicketCallbackProxy(envType, callback), wupOption);
	}
}
