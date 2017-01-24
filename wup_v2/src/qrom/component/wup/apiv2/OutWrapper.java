package qrom.component.wup.apiv2;

public class OutWrapper <T> {
	private T mOut;
	
	public OutWrapper() {
	}
	
	public T getOut() {
		return mOut;
	}
	
	public void setOut(T out) {
		mOut = out;
	}
}
