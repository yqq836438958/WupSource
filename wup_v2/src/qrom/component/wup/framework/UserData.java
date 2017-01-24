package qrom.component.wup.framework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户数据定义，主要用户在流程的调用过程中能够相互传递
 * 
 * 注意： 为了安全的使用用户数据，携带的数据中存放的数据不能修改，且所有权转给Userdata
 * @author wileywang
 */
public class UserData  {
	private Map<String, Object> mDataMap;
	
	public UserData() {
		mDataMap = new ConcurrentHashMap<String, Object>();
	}
	
	public void putData(String key, Object value) {
		if (key == null || value == null) {
			return ;
		}
		
		mDataMap.put(key, value);
	}
	
	public void removeData(String key) {
		mDataMap.remove(key);
	}
	
	public Object getData(String key) {
		return mDataMap.get(key);
	}
	
	public UserData copy() {
		UserData newUserData = new UserData();
		newUserData.mDataMap.putAll(mDataMap);
		return newUserData;
	}
	
	public void clear() {
		mDataMap.clear();
	}
	
}
