package qrom.component.wup.framework;

/**
 *  传输层抽象的基础接口定义
 */
public interface ITransportLayer {
	
	public static interface ITransportCallback {
		public void onRequestFinished(long requestId, Response response);
	}
	
	public void onTransport(ITransportCallback callback, final long requestId , final Request request);
	
	public void cancel(final long requestId);
}
