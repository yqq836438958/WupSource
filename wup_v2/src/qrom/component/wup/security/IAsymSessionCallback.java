package qrom.component.wup.security;

interface IAsymSessionCallback {
	public void onAsymSessionCallback(final long requestId
			, String sessionId
			, int errorCode, String errorMsg);
}
