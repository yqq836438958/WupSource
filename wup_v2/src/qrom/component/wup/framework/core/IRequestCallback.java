package qrom.component.wup.framework.core;

import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Response;

public interface IRequestCallback {
	public void onRequestFinished(final long requestId, final Request request, final Response response);
}
