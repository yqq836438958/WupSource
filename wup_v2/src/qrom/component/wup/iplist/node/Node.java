package qrom.component.wup.iplist.node;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Node {
	public abstract void toJSON(JSONObject nodeObject) throws JSONException;
	
	public abstract void fromJSON(JSONObject nodeObject) throws JSONException;
}
