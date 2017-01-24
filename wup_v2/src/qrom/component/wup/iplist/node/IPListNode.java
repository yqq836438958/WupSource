package qrom.component.wup.iplist.node;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *  描述一种特定的条目描述, 只是描述的数据结构，不要加逻辑
 * @author wileywang
 *
 */
public class IPListNode extends Node {
	
	private int mApnIndex = -1;  
	
	private List<IPPortNode> mIPPortNodeList; //具体的IPList
	private int mPreferNodeIndex;  // 倾向IPPortNode索引
	private int mPreferNodeErrorTimes; // 倾向索引的错误次数
	
	private String mClientIP;  // 获取时服务端返回的ClientIp
	private long mLastUpdateTimestamp; // 上层更新时间
	
	public IPListNode(int apnIndex) {
		this.mApnIndex = apnIndex;
		
		this.mIPPortNodeList = new ArrayList<IPPortNode>();
		this.mPreferNodeIndex = -1;
		this.mLastUpdateTimestamp = 0;
		this.mClientIP = "";
		this.mPreferNodeErrorTimes = 0;
	}
	
	public IPListNode(IPListNode ipListNode) {
		this.mApnIndex = ipListNode.getApnIndex();
		
		this.mPreferNodeIndex = ipListNode.getPreferNodeIndex();
		this.mPreferNodeErrorTimes = ipListNode.getPreferNodeErrorTimes();
		this.mClientIP = ipListNode.getClientIP();
		this.mLastUpdateTimestamp = ipListNode.getLastUpdateTimestamp();
		
		this.mIPPortNodeList = new ArrayList<IPPortNode>();
		for (IPPortNode ipPortNode : ipListNode.getIPPortList()) {
			mIPPortNodeList.add(new IPPortNode(ipPortNode));
		}
	}
	
	public int getApnIndex() {
		return mApnIndex;
	}
	
	public int getPreferNodeIndex() {
		return mPreferNodeIndex;
	}
	
	public void setPreferNodeIndex(int preferNodeIndex) {
		this.mPreferNodeIndex = preferNodeIndex;
	}
	
	public int getPreferNodeErrorTimes() {
		return mPreferNodeErrorTimes;
	}
	
	public void setPreferNodeErrorTimes(int errorTimes) {
		this.mPreferNodeErrorTimes = errorTimes;
	}
	
	public IPPortNode getPreferIPPortNode() {
		if (mPreferNodeIndex >= 0 && mPreferNodeIndex < mIPPortNodeList.size()) {
			return mIPPortNodeList.get(mPreferNodeIndex);
		}
		return null;
	}
	
	public final List<IPPortNode> getIPPortList() {
		return mIPPortNodeList;
	}
	
	public void setIPPortList(List<IPPortNode> ipPortNodeList) {
		clearIPPortNodeList();
		if (ipPortNodeList != null) {
			for (IPPortNode ipPortNodeEntry : ipPortNodeList) {
				if (ipPortNodeEntry != null) {
					mIPPortNodeList.add(ipPortNodeEntry);
				}
			}
		}
	}
	
	public void clearIPPortNodeList() {
		mIPPortNodeList.clear();
		mPreferNodeIndex = -1;
		mPreferNodeErrorTimes = 0;
	}
	
	public long getLastUpdateTimestamp() {
		return mLastUpdateTimestamp;
	}
	
	public void setLastUpdatTimestamp(long updateTimestamp) {
		this.mLastUpdateTimestamp = updateTimestamp;
	}
	
	public String getClientIP() {
		return mClientIP;
	}
	
	public void setClientIP(String clientIP) {
		this.mClientIP = clientIP;
	}

	@Override
	public void toJSON(JSONObject nodeObject) throws JSONException {
		nodeObject.put("lastUpdateTimestamp", mLastUpdateTimestamp);
		nodeObject.put("clientIP", mClientIP);
		nodeObject.put("preferNodeIndex", mPreferNodeIndex);
		
		JSONArray ipPortNodesArray = new JSONArray();
		for (int index = 0; index < mIPPortNodeList.size(); ++index) {
			JSONObject ipPortNodeObject = new JSONObject();
			mIPPortNodeList.get(index).toJSON(ipPortNodeObject);
			ipPortNodesArray.put(ipPortNodeObject);
		}
		
		nodeObject.put("ipPortNodeList", ipPortNodesArray);
	}

	@Override
	public void fromJSON(JSONObject nodeObject) throws JSONException {
		mLastUpdateTimestamp = nodeObject.optLong("lastUpdateTimestamp");
		mClientIP = nodeObject.optString("clientIP");
		mPreferNodeIndex = nodeObject.optInt("preferNodeIndex");
		
		JSONArray ipPortNodesArray = nodeObject.optJSONArray("ipPortNodeList");
		if (ipPortNodesArray != null) {
			for (int index = 0; index < ipPortNodesArray.length(); ++index) {
				IPPortNode ipPortNode = new IPPortNode();
				ipPortNode.fromJSON(ipPortNodesArray.getJSONObject(index));
				mIPPortNodeList.add(ipPortNode);
			}
		}
	}
	
}
