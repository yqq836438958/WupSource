package qrom.component.wup.base.net;

/**
 * 大致的APN分类枚举定义
 * @author wileywang
 */
public enum ApnType {
	  APN_TYPE_UNKNOWN(0)
	, APN_TYPE_NET(1)
	, APN_TYPE_WAP(2)
	, APN_TYPE_WIFI(3)
	;
	  
	public static ApnType from(int value) {
		if (value == 0) {
			return ApnType.APN_TYPE_UNKNOWN;
		} else if (value == 1) {
			return ApnType.APN_TYPE_NET;
		} else if (value == 2) {
			return ApnType.APN_TYPE_WAP;
		} else if (value == 3) {
			return ApnType.APN_TYPE_WIFI;
		}
		return null;
	}
	
	private int mValue;
	ApnType(int value) {
		mValue = value;
	}
	
	public int getValue() {
		return mValue;
	}
}
