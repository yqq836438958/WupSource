package qrom.component.wup.statics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.NetType;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.build.BuildInfo;
import qrom.component.wup.framework.Request.RequestType;

/**
 *  统计请求的基础信息
 * @author wileywang
 *
 */
public abstract class ReqStatBaseInfo {
	
	private byte[] mGuidBytes;  // 请求的GuidBytes
	
	private RequestType mRequestType;  // 请求类型
	
	private String mServantName; // 请求Servant的名称
	
	private String mFunctionName; // 请求的服务名称
	
	private ConnectInfo mReqConnectInfo; // 请求时的网络连接信息
	private ConnectInfo mRespConnectInfo; // 回复时的网络连接信息
	
	protected ReqStatBaseInfo(byte[] guidBytes
			, RequestType requestType) {
		this.mGuidBytes = guidBytes;
		this.mRequestType = requestType;
	}
	
	public String getServantName() {
		return mServantName;
	}
	
	public void setServantName(String servantName) {
		this.mServantName = servantName;
	}
	
	public String getFunctionName() {
		return mFunctionName;
	}
	
	public void setFunctionName(String functionName) {
		this.mFunctionName = functionName;
	}
	
	public ConnectInfo getReqConnectInfo() {
		return mReqConnectInfo;
	}
	
	public void setReqConnectInfo(ConnectInfo connectInfo) {
		this.mReqConnectInfo = connectInfo;
	}
	
	public ConnectInfo getRespConnectInfo() {
		return mRespConnectInfo;
	}
	
	public void setRespConnectInfo(ConnectInfo connectInfo) {
		this.mRespConnectInfo = connectInfo;
	}
	
	public byte[] getGuidBytes() {
		return mGuidBytes;
	}
	
	public Map<String, String> buildStatMap() {
		Map<String, String> statMap = new HashMap<String, String>();
		
		statMap.put(StatConstants.KEY.SDK_VER, BuildInfo.BUILD_VERSION_DATE);
		statMap.put(StatConstants.KEY.SESSION, UUID.randomUUID().toString());
		
		if (mRequestType == RequestType.NORMAL_REQUEST) {
			statMap.put(StatConstants.KEY.ENCRYPT_TYPE, StatConstants.VALUE.ENCRYPT_TYPE_NORMAL);
		} else if (mRequestType == RequestType.ASYM_ENCRPT_REQUEST) {
			statMap.put(StatConstants.KEY.ENCRYPT_TYPE, StatConstants.VALUE.ENCRYPT_TYPE_ASYM);
		} else {
			statMap.put(StatConstants.KEY.ENCRYPT_TYPE, StatConstants.VALUE.ENCRYPT_TYPE_OTHER);
		}
		
		statMap.put(StatConstants.KEY.SERVANT, mServantName == null ? "" : mServantName);
		statMap.put(StatConstants.KEY.FUNCTION, mFunctionName == null ? "" : mFunctionName);
		statMap.put(StatConstants.KEY.REQ_NETSTAT, getStatNetType(mReqConnectInfo) + "_" + getStatNetType(mRespConnectInfo));
		
		doFillSubFields(statMap);
		
		return statMap;
	}
	
	private static final String STATE_NET_TYPE_WIFI = "wifi";
	private static final String STATE_NET_TYPE_NO = "na";
	private static final String STATE_NET_TYPE_UNKOWN = "--";
	
	private String getStatNetType(ConnectInfo connectInfo) {
		if (connectInfo == null) {
			return STATE_NET_TYPE_UNKOWN;
		}
		if (!connectInfo.isConnected()) {
			return STATE_NET_TYPE_NO;
		}
		if (connectInfo.getNetType() == NetType.NET_WIFI) {
			return STATE_NET_TYPE_WIFI;
		}
		if (StringUtil.isEmpty(connectInfo.getExtraInfo())) {
			return STATE_NET_TYPE_UNKOWN;
		}
		return connectInfo.getExtraInfo();
	}
	
	protected abstract void doFillSubFields(Map<String, String> statMap);
}
