package qrom.component.wup.iplist;

import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.NetType;
import TRom.EAPNTYPE;
import TRom.ENETTYPE;
import TRom.JoinIPInfo;

public class ApnIndexConvertor {
	public static final int APN_INDEX_4G = 90;
	public static final int APN_INDEX_WIFI = 99;
	
	// 原有wup使用apn的索引类型，这里需要复用
	public static final int PROXY_LIST_CMWAP   = 0;
	public static final int PROXY_LIST_CMNET    = 1;
	public static final int PROXY_LIST_UNWAP   = 2;
	public static final int PROXY_LIST_UNNET    = 3;
	public static final int PROXY_LIST_CTWAP    = 4;
	public static final int PROXY_LIST_CTNET     = 5;
	public static final int PROXY_LIST_WIFI        = 6;
	public static final int PROXY_LIST_3GWAP   = 7;
	public static final int PROXY_LIST_3GNET    = 8;
	public static final int PROXY_LIST_4G    = 9;
	public static final int PROXY_LIST_UNKOWN    = 99;
	
	
	public static int getApnIndex(ConnectInfo connectInfo) {
		if (connectInfo.getNetType() == NetType.NET_WIFI) {
			return APN_INDEX_WIFI;
		}
		if (connectInfo.getNetType() == NetType.NET_4G) {
			return APN_INDEX_4G;
		}
		
		return JceNetUtil.getEAPNTYPE(connectInfo).value();
	}
	
	public static int getApnIndex(JoinIPInfo ipInfo) {
		if (ipInfo.getENetType() == ENETTYPE._NET_WIFI) {
			return APN_INDEX_WIFI;
		}
		if (ipInfo.getENetType() == ENETTYPE._NET_4G) {
			return APN_INDEX_4G;
		}
		
		return ipInfo.getEApnType();
	}
	
	// 将现有的apnIndex，转化为wup老版的index
	public static int getOrignalApnTypeList (int apnIndex) {
		if (apnIndex == EAPNTYPE._APN_3GNET) {
	    	return PROXY_LIST_3GNET;
	    } else if (apnIndex == EAPNTYPE._APN_3GWAP) {
	    	return PROXY_LIST_3GWAP;
	    } else if (apnIndex == EAPNTYPE._APN_CMNET) {
	    	return PROXY_LIST_CMNET;
	    } else if (apnIndex == EAPNTYPE._APN_CMWAP) {
	    	return PROXY_LIST_CMWAP;
	    } else if (apnIndex == EAPNTYPE._APN_CTNET) {
	    	return PROXY_LIST_CTNET;
	    } else if (apnIndex == EAPNTYPE._APN_CTWAP) {
	    	return PROXY_LIST_CTWAP;
	    } else if (apnIndex == EAPNTYPE._APN_UNNET) {
	    	return PROXY_LIST_UNNET;
	    } else if (apnIndex == EAPNTYPE._APN_UNWAP) {
	    	return PROXY_LIST_UNWAP;
	    } else if (apnIndex == APN_INDEX_4G) {
	    	return PROXY_LIST_4G;
	    } else if (apnIndex == APN_INDEX_WIFI) {
	    	return PROXY_LIST_WIFI;
	    }
	    return PROXY_LIST_UNKOWN;
	     	
	}
	
}
