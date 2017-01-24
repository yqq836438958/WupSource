package qrom.component.wup.transport;

import java.util.HashMap;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.framework.ITransportLayer;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Response;
import qrom.component.wup.statics.FailedReqStatInfo;
import qrom.component.wup.statics.ReqStatBaseInfo;
import qrom.component.wup.statics.StatProxy;
import qrom.component.wup.statics.SuccReqStatInfo;
import qrom.component.wup.transport.http.DefaultHttpSessionPool;
import qrom.component.wup.transport.http.HttpClientSession;
import qrom.component.wup.transport.http.HttpSession;
import qrom.component.wup.transport.http.IHttpRouteChooser;
import qrom.component.wup.transport.http.IHttpSessionPool;
import qrom.component.wup.transport.http.HttpSession.SessionRequest;
import qrom.component.wup.transport.http.HttpSession.SessionResponse;
import qrom.component.wup.transport.http.route.ChainHttpRouteChooser;
import qrom.component.wup.transport.http.route.DefaultHttpRouteChooser;
import qrom.component.wup.transport.http.route.HttpHookChooser;
import qrom.component.wup.transport.http.route.IPListHttpRouteChooser;

/**
 * 采用Http协议实现的传输层
 * @author wileywang
 *
 */
public class HttpTransportLayer implements ITransportLayer, HttpClientSession.ICallback {
	private static final String TAG = HttpTransportLayer.class.getSimpleName();
	
	private static class RequestEntry {
		private ITransportCallback mCallback;
		private HttpSession mHttpSession;
		private Request mRequest;
		
		public RequestEntry(
				ITransportCallback callback
				, Request request
				, HttpSession httpSession) {
			mRequest = request;
			mCallback = callback;
			mHttpSession = httpSession;
		}
		
		public ITransportCallback getCallback() {
			return mCallback;
		}
		
		public HttpSession getHttpSession() {
			return mHttpSession;
		}
		
		public Request getRequest() {
			return mRequest;
		}
	}
	
	private IWorkRunner mTrasnportWorkRunner;
	private IHttpSessionPool mHttpSessionPool;
	private Map<Long, RequestEntry> mRequestEntries;
	
	public HttpTransportLayer(IWorkRunner transportWorkRunner) {
		mTrasnportWorkRunner = transportWorkRunner;
		mHttpSessionPool = new DefaultHttpSessionPool();
		mRequestEntries = new HashMap<Long, RequestEntry>();
	}
	
	@Override
	public void onTransport(ITransportCallback callback, long requestId, Request request) {
		if (request.getTransportData() == null) {
			onError(callback, requestId, WUP_ERROR_CODE.WUP_INNER_ERROR, "request's transportData is null");
			return ;
		}
		if (request.getGuidBytes() == null) {
			onError(callback, requestId, WUP_ERROR_CODE.WUP_INNER_ERROR, "request's guid should not be null");
			return ;
		}
		if (request.getRequestEnvType() == null) {
			onError(callback, requestId, WUP_ERROR_CODE.WUP_INNER_ERROR, "request's requestEnvType should not be null");
			return ;
		}
		
		SessionRequest sessionRequest = new SessionRequest();
		if (request.getRequestOption().isEncrypRequest()) {
			sessionRequest.setQQEncrypt(true);
		}
		if (request.getRequestOption().getTimeoutMills() > 0) {
			sessionRequest.setSessionTimeout(request.getRequestOption().getTimeoutMills());
		}
		sessionRequest.setGuid(StringUtil.byteToHexString(request.getGuidBytes()));
		sessionRequest.setPostData(request.getTransportData());
		
		IHttpRouteChooser httpRouteChooser = null;
		
		/**
		 *  允许注册传输层，这样允许上层可以主动测试接入点，以及切换至额外的机房
		 */
		ITransportRouter hookRouter = TransportHook.getHookRouter();
		if (hookRouter != null) {
			httpRouteChooser = new HttpHookChooser(hookRouter, request.getRequestEnvType());
		} else {
			if (request.getRequestOption().isForceDefaultRoute()) {
				httpRouteChooser = new DefaultHttpRouteChooser(request.getRequestEnvType());
			} else {
				httpRouteChooser = new ChainHttpRouteChooser(
					new IPListHttpRouteChooser(request.getRequestEnvType())
					, new DefaultHttpRouteChooser(request.getRequestEnvType()));
			}
		}
		
		HttpSession httpSession = new HttpClientSession(requestId, sessionRequest, httpRouteChooser, this);
		if (mHttpSessionPool.postHttpSession(httpSession)) {
			mRequestEntries.put(requestId, new RequestEntry(callback, request, httpSession));
		} else {
			onError(callback, requestId, WUP_ERROR_CODE.WUP_TASK_TOO_FREQUENT, "request is too frequent");
		}
	}
	
	private void onError(ITransportCallback callback
			, long requestId, int errorCode, String errorMsg) {
		callback.onRequestFinished(requestId, new Response(errorCode, errorMsg));
	}

	@Override
	public void cancel(long requestId) {
		RequestEntry requestEntry = mRequestEntries.remove(requestId);
		if (requestEntry != null) {
			mHttpSessionPool.cancel(requestEntry.getHttpSession());
		}
	}
	
	private void onHttpResponse(HttpSession httpSession, SessionResponse sessionResponse) {
		RequestEntry requestEntry = mRequestEntries.remove(httpSession.getSessionId());
		if (requestEntry == null) {
			QRomLog.d(TAG, "onHttpResponse drop session(because is not found), sessionId=" + httpSession.getSessionId());
			return ;
		}
		
		ReqStatBaseInfo reqStatInfo = null;
		// 统计上报
		if (sessionResponse.getErrorCode() == 0) {
			SuccReqStatInfo succReqStatInfo = new SuccReqStatInfo(
					requestEntry.getRequest().getGuidBytes()
					, requestEntry.getRequest().getRequestType());
			
			succReqStatInfo.setConnectTimeMs(httpSession.getSessionConnectTimeMs());
			succReqStatInfo.setReadTimeMs(httpSession.getSessionReadTimeMs());
			succReqStatInfo.setSendTimeMs(httpSession.getSessionSendTimeMs());
			succReqStatInfo.setReqDataLength(httpSession.getSessionReqSize());
			succReqStatInfo.setRespDataLength(httpSession.getSessionRespSize());
			
			reqStatInfo = succReqStatInfo;
		} else {
			FailedReqStatInfo failedReqStatInfo = new FailedReqStatInfo(
					requestEntry.getRequest().getGuidBytes()
					, requestEntry.getRequest().getRequestType());
			
			failedReqStatInfo.setErrorCode(sessionResponse.getErrorCode());
			failedReqStatInfo.setErrorMsg(sessionResponse.getErrorMsg());
			failedReqStatInfo.setReqDataLength(httpSession.getSessionReqSize());
			
			if (httpSession.getHttpRouteInfo() != null) {
				failedReqStatInfo.setClientIP(httpSession.getHttpRouteInfo().getClientIP());
				failedReqStatInfo.setServerUrl(httpSession.getHttpRouteInfo().getHttpAddr().toUrlStr());
				failedReqStatInfo.setSIP(httpSession.getHttpRouteInfo().getStatSIP());
			}
			
			reqStatInfo = failedReqStatInfo;
		}
		if (httpSession.getHttpRouteInfo() != null) {
			reqStatInfo.setReqConnectInfo(httpSession.getHttpRouteInfo().getConnectInfo());
		}
		reqStatInfo.setRespConnectInfo(ConnectInfoManager.get().getConnectInfo());
		reqStatInfo.setServantName(requestEntry.getRequest().getServiceName());
		reqStatInfo.setFunctionName(requestEntry.getRequest().getFuncName());
		StatProxy.get().reportReqStat(reqStatInfo);
		
		// 回调处理
		if (sessionResponse.getErrorCode() != 0) {
			onError(requestEntry.getCallback(), httpSession.getSessionId()
					, sessionResponse.getErrorCode(), sessionResponse.getErrorMsg());
			return ;
		} 
		
		Response resp = new Response();
		resp.setResponseContent(sessionResponse.getRespData().getResponseContent());
		requestEntry.getCallback().onRequestFinished(httpSession.getSessionId(), resp);
	}

	@Override
	public void onSessionResponse(final HttpSession httpSession, final SessionResponse sessionResponse) {
		if (mTrasnportWorkRunner.getThread() == Thread.currentThread()) {
			onHttpResponse(httpSession, sessionResponse);
		} else {
			mTrasnportWorkRunner.postWork(new Runnable() {

				@Override
				public void run() {
					try {
						onHttpResponse(httpSession, sessionResponse);
					} catch (Throwable e) {
						QRomLog.e(TAG, e.getMessage(), e);
					}
				}
				
			});
		}
	}
	
}
