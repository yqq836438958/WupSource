package qrom.component.wup.iplist.node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import TRom.EIPType;

/**
 *  IPList的根节点, 按照IPList的类型区分
 * @author wileywang
 *
 */
public class IPRootNode extends Node {
	private Map<Integer, IPTypeNode> mIPTypeNodes;
	
	public IPRootNode() {
		mIPTypeNodes = new HashMap<Integer, IPTypeNode>();
	}
	
	public IPTypeNode getIPTypeNode(EIPType ipType) {
		if (ipType == null) {
			return null;
		}
		
		return mIPTypeNodes.get(ipType.value());
	}
	
	public void updateIpTypeNode(EIPType ipType, IPTypeNode ipTypeNode) {
		if (ipType == null) {
			return ;
		}
		
		if (ipTypeNode == null) {
			mIPTypeNodes.remove(ipType.value());
			return ;
		}
		
		mIPTypeNodes.put(ipType.value(), ipTypeNode);
	}

	@Override
	public void toJSON(JSONObject nodeObject) throws JSONException {
		for (Entry<Integer, IPTypeNode> entry : mIPTypeNodes.entrySet()) {
			JSONObject ipListNodeObject = new JSONObject();
			entry.getValue().toJSON(ipListNodeObject);
			
			nodeObject.put(entry.getValue().getIPType().toString(), ipListNodeObject);
		}
	}

	@Override
	public void fromJSON(JSONObject nodeObject) throws JSONException {
		Iterator<String> keysIt = nodeObject.keys();
		while(keysIt.hasNext()) {
			String key = keysIt.next();
			
			try {
				EIPType ipType = EIPType.convert(key);
				if (ipType == null) {
					continue;
				}
				
				IPTypeNode ipTypeNode = new IPTypeNode(ipType);
				ipTypeNode.fromJSON(nodeObject.getJSONObject(key));
				
				mIPTypeNodes.put(ipType.value(), ipTypeNode);
			
			} catch (Throwable e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
