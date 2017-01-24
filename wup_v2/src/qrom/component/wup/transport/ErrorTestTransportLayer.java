package qrom.component.wup.transport;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.framework.ITransportLayer;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Response;

public class ErrorTestTransportLayer implements ITransportLayer {
	private IWorkRunner mTransportRunner;
	
	public ErrorTestTransportLayer(IWorkRunner transportRunner) {
		mTransportRunner = transportRunner;
	}
	
	@Override
	public void onTransport(final ITransportCallback callback, final long requestId,
			final Request request) {
		
		if (requestId % 3 == 0) {
			callback.onRequestFinished(requestId, new Response(WUP_ERROR_CODE.WUP_INNER_ERROR
					, "this is ErrorTestTransportLayer callback immediate error"));
		} else {
			mTransportRunner.postWorkDelayed(new Runnable() {

				@Override
				public void run() {
					callback.onRequestFinished(requestId, new Response(WUP_ERROR_CODE.WUP_INNER_ERROR
							, "this is ErrorTestTransportLayer callback delay error"));
				}
				
			}, 50);
		}
		
	}

	@Override
	public void cancel(long requestId) {
		
	}

}
