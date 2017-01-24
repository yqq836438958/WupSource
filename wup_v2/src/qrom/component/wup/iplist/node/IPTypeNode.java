package qrom.component.wup.iplist.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import TRom.EIPType;

public class IPTypeNode extends Node {
	private EIPType mEIPType;
	
	private Map<Integer, IPListNode> mApnIpListNodes;
	
	public IPTypeNode(EIPType ipType) {
		if (ipType == null) {
			throw new IllegalArgumentException("ipType should not be null");
		}
		
		this.mEIPType = ipType;
		this.mApnIpListNodes = new HashMap<Integer, IPListNode>();
	}
	
	public EIPType getIPType() {
		return mEIPType;
	}
	
	public List<IPListNode> copyAllIPListNodes() {
		List<IPListNode> allIPListNode = new ArrayList<IPListNode>();
		
		for (Entry<Integer, IPListNode> entry : mApnIpListNodes.entrySet()) {
			allIPListNode.add(new IPListNode(entry.getValue()));
		}
		
		return allIPListNode;
	}
	
	public IPListNode copyIPListNode(int apnIndex) {
		IPListNode ipListNode = mApnIpListNodes.get(apnIndex);
		if (ipListNode == null) {
			return null;
		}
		
		return new IPListNode(ipListNode);
	}
	
	public IPListNode getApnNode(int apnIndex) {
		return mApnIpListNodes.get(apnIndex);
	}
	
	public void updateApnNode(int apnIndex, IPListNode ipListNode) {
		if (ipListNode == null) {
			mApnIpListNodes.remove(apnIndex);
			return ;
		}
		
		mApnIpListNodes.put(apnIndex, ipListNode);
	}

	@Override
	public void toJSON(JSONObject nodeObject) throws JSONException {
		for (Entry<Integer, IPListNode> entry : mApnIpListNodes.entrySet()) {
			JSONObject ipListNodeObject = new JSONObject();
			entry.getValue().toJSON(ipListNodeObject);
			
			nodeObject.put(entry.getKey().toString(), ipListNodeObject);
		}
	}

	@Override
	public void fromJSON(JSONObject nodeObject) throws JSONException {
		Iterator<String> keysIt = nodeObject.keys();
		while(keysIt.hasNext()) {
			String key = keysIt.next();
			
			try {
				IPListNode ipListNode = new IPListNode(Integer.valueOf(key));
				ipListNode.fromJSON(nodeObject.getJSONObject(key));
			
				mApnIpListNodes.put(ipListNode.getApnIndex(), ipListNode);
			} catch (Throwable e) {
				e.printStackTrace();
				continue;
			}
		}
		
	}
}
