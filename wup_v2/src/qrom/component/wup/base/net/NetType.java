package qrom.component.wup.base.net;

/**
 *  网络类型的描述
 * @author wileywang
 *
 */
public enum NetType {
	NET_NO(-1)
	, NET_UNKNOWN(0)
	, NET_WIFI(1)
	, NET_2G(2)
	, NET_3G(3)
	, NET_4G(4)
	;
	
	public static NetType from(int value) {
		if (value == -1) {
			return NetType.NET_NO;
		} else if (value == 0) {
			return NetType.NET_UNKNOWN;
		} else if (value == 1) {
			return NetType.NET_WIFI;
		} else if (value == 2) {
			return NetType.NET_2G;
		} else if (value == 3) {
			return NetType.NET_3G;
		} else if (value == 4) {
			return NetType.NET_4G;
		}
		
		return null;
	}
	
	private int mValue;
	NetType(int value) {
		mValue = value;
	}
	
	public int getValue() {
		return mValue;
	}
	
}
