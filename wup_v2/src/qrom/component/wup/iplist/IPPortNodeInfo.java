package qrom.component.wup.iplist;

import qrom.component.wup.iplist.node.IPPortNode;

public class IPPortNodeInfo {
	private IPPortNode mNode;
	private int mIPListSize;
	private int mIPIndex;
	private String mClientIP = "";

	public IPPortNodeInfo(IPPortNode node, int ipListSize, int ipIndex, String clientIP) {
		this.mNode = node;
		this.mIPListSize = ipListSize;
		this.mIPIndex = ipIndex;
		this.mClientIP = clientIP;
	}

	public IPPortNode getNode() {
		return mNode;
	}

	public int getIPListSize() {
		return mIPListSize;
	}

	public int getIPIndex() {
		return mIPIndex;
	}

	public String getClientIP() {
		return mClientIP;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(48);
		builder.append("IPPortNodeInfo=[mNode=");
		builder.append(mNode);
		builder.append(", mIPListSize=");
		builder.append(mIPListSize);
		builder.append(", mIPIndex=");
		builder.append(mIPIndex);
		builder.append(", mClientIP=");
		builder.append(mClientIP);
		builder.append("]");
		return builder.toString();
	}
}
