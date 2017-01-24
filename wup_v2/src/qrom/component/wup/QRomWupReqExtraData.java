package qrom.component.wup;

import java.util.HashMap;

/**
 * wup请求需缓存的额外数据
 * 
 * <b>"parserType"为保留字段，各个进程不要使用它作为key</b>
 * @author sukeyli
 *
 */
public class QRomWupReqExtraData {
	
	public String extraStr;
	
	private HashMap<String, Object> mReqExtraMap = null;

	public void addWupReqExtraData(String key, Object extra) {
	    if (mReqExtraMap == null) {
	        mReqExtraMap = new HashMap<String, Object>(1);
	    }
	    mReqExtraMap.put(key, extra);
	}
	
	public void removeWupExtraData(String key) {
	    if (mReqExtraMap == null) {
	        return;
	    }
	    mReqExtraMap.remove(key);
	}
	
	public Object getWupExtraData(String key) {
	    if (mReqExtraMap == null) {
	        return null;
	    }
	    return mReqExtraMap.get(key);
	}
}
