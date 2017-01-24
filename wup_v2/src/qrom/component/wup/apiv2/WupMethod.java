package qrom.component.wup.apiv2;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.apiv2.WupOption.WupType;
import qrom.component.wup.core.DispatcherFactory;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Request.RequestType;
import qrom.component.wup.framework.Response;
import qrom.component.wup.framework.core.IRequestCallback;

import com.qq.jce.wup.UniPacket;

/**
 * 所有wup方法的公共基类
 * @author wileywang
 *
 */
public abstract class WupMethod implements IRequestCallback {
	private static final String TAG = WupMethod.class.getSimpleName();
	
	private String mServantName;
	private String mFuncName;
	protected WupOption mWupOption;
	
	protected long mRequestId = -1;
	
	protected WupMethod(
			String servantName
			, String funcName
			, WupOption wupOption) {
		this.mServantName = servantName;
		this.mFuncName = funcName;
		this.mWupOption = wupOption;
	}
	
	public void start() throws WupException {
		UniPacket reqPacket = new UniPacket();
		
		reqPacket.setServantName(mServantName);
		reqPacket.setFuncName(mFuncName);
		if (mWupOption.isUseUniPacketV3()) {
			reqPacket.useVersion3();
		}
		reqPacket.setEncodeName(mWupOption.getCharset());
		
		fillReqUniPacket(reqPacket);
		
		RequestType requestType = RequestType.NORMAL_REQUEST;
		if (mWupOption.getWupType() == WupType.WUP_ASYM_ENCRYPT_REQUEST) {
			requestType = RequestType.ASYM_ENCRPT_REQUEST;
		}
		Request request = new Request(reqPacket, requestType);
		request.setAppPkgInfo(mWupOption.getRequestPkgInfo());
		request.setRequestEnvType(mWupOption.getRequestEnvType());
		request.getRequestOption().setRetryTimes(mWupOption.getRetryTimes());
		request.getRequestOption().setTimeoutMills(mWupOption.getTimeoutMs());
		
		mRequestId = DispatcherFactory.getDefault().send(request, this);
		if (mRequestId <= 0) {
			throw new WupException(WUP_ERROR_CODE.WUP_INNER_ERROR, "send request failed!");
		}
	}
	
	public long getRequestId() {
		return mRequestId;
	}
	
	public void cancel() {
		DispatcherFactory.getDefault().cancel(mRequestId);
		mRequestId = -1;
	}

	@Override
	public void onRequestFinished(long requestId, Request request, Response response) {
		if (response.getErrorCode() != 0) {
			handleError(response.getErrorCode(), response.getErrorMsg());
			return ;
		}
		
		if (response.getResponseContent() == null || response.getResponseContent().length <= 0) {
			handleError(WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY, "response content is empty!");
			return ;
		}
		
		UniPacket respPacket = new UniPacket();
		try {
			if (mWupOption.isUseUniPacketV3()) {
				respPacket.useVersion3();
			}
			respPacket.setEncodeName(mWupOption.getCharset());
			respPacket.decode(response.getResponseContent());
			
			fillFieldsByUniPacket(respPacket);
			
			handleFinished();
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
			handleError(WUP_ERROR_CODE.WUP_PARSE_WUP_PACKET_FAILED, e.getMessage());
			return ;
		}
	}
	
	protected abstract void handleError(final int errorCode, final String errorMsg);
	protected abstract void handleFinished();
	
	protected abstract void fillReqUniPacket(UniPacket reqPacket);
	protected abstract void fillFieldsByUniPacket(UniPacket respPacket);
	
}
