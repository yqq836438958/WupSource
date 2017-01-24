package qrom.component.wup.transport;

public class TransportHook {
	
	private static ITransportRouter sHookRouter;
	
	public static void setHookRouter(ITransportRouter hookRouter) {
		sHookRouter = hookRouter;
	}
	
	public static ITransportRouter getHookRouter() {
		return sHookRouter;
	}
	
}
