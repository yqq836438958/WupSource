package qrom.component.wup.transport.http;

public interface IHttpSessionPool {
	
	public boolean postHttpSession(HttpSession session);
	
	public void cancel(HttpSession session);
	
	public void destroy();
}
