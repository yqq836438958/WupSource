package qrom.component.wup.iplist.node;

import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.wup.base.utils.StringUtil;

/**
 *  对应一个IpList的条目
 * @author wileywang
 *
 */
public class IPPortNode extends Node {
	private String mIp = "";
	private int mPort = 0;
	
	public static IPPortNode parse(String ipPortStr) {
		if (ipPortStr == null) {
			return null;
		}
		
		String[] splits = ipPortStr.split(":");
		if (splits.length != 2) {
			return null;
		}
		
		try {
			int port = Integer.valueOf(splits[1]);
			return new IPPortNode(splits[0], port);
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static IPPortNode fromJson(String jsonStr) {
		if (StringUtil.isEmpty(jsonStr)) {
			return null;
		}
		
		IPPortNode resultNode = new IPPortNode();
		try {
			resultNode.fromJSON(new JSONObject(jsonStr));
			return resultNode;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
	
	IPPortNode() {
	}
	
	public IPPortNode(String ip, int port) {
		if (mIp == null) {
			throw new IllegalArgumentException("ip should not be null");
		}
		
		this.mIp = ip;
		this.mPort = port;
	}
	
	public IPPortNode(IPPortNode node) {
		this.mIp = node.getIp();
		this.mPort = node.getPort();
	}
	
	public String getIp() {
		return mIp;
	}
	
	public int getPort() {
		return mPort;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(32);
		builder.append("IPPortNode[");
		builder.append(mIp);
		builder.append(":");
		builder.append(mPort);
		builder.append("]");
		return builder.toString();
	}
	
	public String toUrlString() {
		StringBuilder builder = new StringBuilder(32);
		builder.append(mIp);
		builder.append(":");
		builder.append(mPort);
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IPPortNode)) {
			return false;
		}
		
		IPPortNode targetNode = (IPPortNode)obj;
		if (mPort != targetNode.getPort()) {
			return false;
		}
		
		if (!mIp.equals(targetNode.getIp())) {
			return false;
		}
		
		return true;
	}

	@Override
	public void toJSON(JSONObject nodeObject) throws JSONException {
		nodeObject.put("ip", mIp);
		nodeObject.put("port", mPort);
	}

	@Override
	public void fromJSON(JSONObject nodeObject) throws JSONException {
		mIp = nodeObject.getString("ip");
		mPort = nodeObject.getInt("port");
	}
	
}
