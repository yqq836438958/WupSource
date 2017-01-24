package qrom.component.wup.guid;

/**
 *  
 * @author wileywang
 *
 */
interface IGuidClient {
	public byte[] getGuidBytes();
	
	public int requestLogin();
	
	public void release();
}
