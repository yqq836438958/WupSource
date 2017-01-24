package qrom.component.wup.guid;

/**
 *  Guid更新事件
 * @author wileywang
 *
 */
public class GuidUpdateEvent {
	private byte[] mNewGuidBytes;
	
	public GuidUpdateEvent(byte[] newGuidBytes) {
		this.mNewGuidBytes = newGuidBytes;
	}
	
	public byte[] getNewGuidBytes() {
		return mNewGuidBytes;
	}
}
