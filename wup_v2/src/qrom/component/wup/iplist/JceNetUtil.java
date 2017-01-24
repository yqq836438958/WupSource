package qrom.component.wup.iplist;

import TRom.EAPNTYPE;
import TRom.ENETTYPE;
import qrom.component.wup.base.net.ApnType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.NetType;

/**
 * 转换连接信息和Jce的映射
 * @author wileywang
 *
 */
public class JceNetUtil {
	
	public static EAPNTYPE getEAPNTYPE(ConnectInfo connectInfo) {
		ApnType apnType = connectInfo.getApnType();
		String apnName = connectInfo.getApnName();
		if (apnName == null) {
			apnName = "";
		}
		
		if (apnType == ApnType.APN_TYPE_WAP) {
			if (apnName.contains(ConnectInfo.APN_CMNET)) {
				return EAPNTYPE.APN_CMNET;
			} else if (apnName.contains(ConnectInfo.APN_CTNET)) {
				return EAPNTYPE.APN_CTNET;
			} else if (apnName.contains(ConnectInfo.APN_UNINET)) {
				return EAPNTYPE.APN_UNNET;
			} else if (apnName.contains(ConnectInfo.APN_3GNET)) {
				return EAPNTYPE.APN_3GNET;
			} else {
				return EAPNTYPE.APN_CTNET;
			}
		} else if (apnType == ApnType.APN_TYPE_NET) {
			if (apnName.contains(ConnectInfo.APN_CMWAP)) {
                return EAPNTYPE.APN_CMWAP;
            } else if (apnName.contains(ConnectInfo.APN_CTWAP)) {
                return EAPNTYPE.APN_CTWAP;
            } else if (apnName.contains(ConnectInfo.APN_UNIWAP)) {
                return EAPNTYPE.APN_UNWAP;
            } else if (apnName.contains(ConnectInfo.APN_3GWAP)) {
            	return EAPNTYPE.APN_3GWAP;
            } else {
            	return EAPNTYPE.APN_CTWAP;
            }
		}
		
		return EAPNTYPE.APN_UNKNOWN;
	}
	
	public static ENETTYPE getNETTYPE(ConnectInfo connectInfo) {
		NetType netType = connectInfo.getNetType();
		
		if (netType == NetType.NET_WIFI) {
			return ENETTYPE.NET_WIFI;
		} else if (netType == NetType.NET_2G) {
			return ENETTYPE.NET_2G;
		} else if (netType == NetType.NET_3G) {
			return ENETTYPE.NET_3G;
		} else if (netType == NetType.NET_4G) {
			return ENETTYPE.NET_4G;
		}
		
		return ENETTYPE.NET_UNKNOWN;
	}
}
