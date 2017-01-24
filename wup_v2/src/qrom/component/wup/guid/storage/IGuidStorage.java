package qrom.component.wup.guid.storage;

public interface IGuidStorage {
	public byte[] getGuidBytes();
	
	public void updateGuidBytes(byte[] newGuidBytes);
}
