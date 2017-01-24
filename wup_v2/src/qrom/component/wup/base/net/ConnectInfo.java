package qrom.component.wup.base.net;

import org.json.JSONException;
import org.json.JSONObject;

import qrom.component.wup.base.utils.StringUtil;

/**
 *  连接信息
 * @author wileywang
 *
 */
public class ConnectInfo {
	// CM代理让我很费解，目前只是为了区分电信代理
	public static final byte PROXY_TYPE_CM = 0; 
    public static final byte PROXY_TYPE_CT = 1; // 电信代理
    
    // APN 名称
    public static final String APN_NAME_UNKNOWN = "N/A";
    public static final String APN_NAME_NET = "Net";
    public static final String APN_NAME_WAP = "Wap";
    public static final String APN_NAME_WIFI = "Wlan";
    
    public static final String APN_CMWAP = "cmwap";
    public static final String APN_CMNET = "cmnet";
    public static final String APN_3GWAP = "3gwap";
    public static final String APN_3GNET = "3gnet";
    public static final String APN_UNIWAP = "uniwap";
    public static final String APN_UNINET = "uninet";
    public static final String APN_CTWAP = "ctwap";
    public static final String APN_CTNET = "ctnet";
    public static final String APN_777 = "#777";
    
    public static final String PROXY_CTWAP_HOST = "10.0.0.200";
    
	private NetType mNetType;
	
	private ApnType mApnType;
	private String mApnName = "";
	
	private String mExtraInfo = "";
	
	private String mProxyHost = ""; 
	private int mProxyPort = 80;
	private int mProxyType = PROXY_TYPE_CM;
	
	private boolean mIsUseProxy = false;
	private String mBssid;
	
	public ConnectInfo(NetType netType) {
		mNetType = netType;
	}
	
	public NetType getNetType() {
		return mNetType;
	}
	
	public ApnType getApnType() {
		return mApnType;
	}
	
	public ConnectInfo setApnType(ApnType apnType) {
		mApnType = apnType;
		return this;
	}
	
	public String getApnName() {
		return mApnName;
	}
	
	public ConnectInfo setApnName(String apnName) {
		mApnName = apnName;
		return this;
	}
	
	public String getExtraInfo() {
		return mExtraInfo;
	}
	
	public ConnectInfo setExtraInfo(String extraInfo) {
		mExtraInfo = extraInfo;
		return this;
	}
	
	public String getBssid() {
		return mBssid;
	}
	
	public ConnectInfo setBssid(String bssid) {
		mBssid = bssid;
		return this;
	}
	
	public String getProxyHost() {
		return mProxyHost;
	}
	
	public ConnectInfo setProxyHost(String proxyHost) {
		mProxyHost = proxyHost;
		return this;
	}
	
	public int getProxyPort() {
		return mProxyPort;
	}
	
	public ConnectInfo setProxyPort(int proxyPort) {
		mProxyPort = proxyPort;
		return this;
	}
	
	public boolean isUseProxy() {
		return mIsUseProxy;
	}
	
	public ConnectInfo setIsUseProxy(boolean isUseProxy) {
		mIsUseProxy = isUseProxy;
		return this;
	}
	
	public int getProxyType() {
		return mProxyType;
	}
	
	public ConnectInfo setProxyType(int proxyType) {
		mProxyType = proxyType;
		return this;
	}
	
	public boolean isConnected() {
		return (mNetType != null) && (mNetType != NetType.NET_NO);
	}
	
	public String getHttpProxyUrl() {
		StringBuilder hostBuilder = new StringBuilder(64);
		hostBuilder.append("http://");
		hostBuilder.append(mProxyHost);
		if (mProxyPort != 80) {
			hostBuilder.append(":");
			hostBuilder.append(mProxyPort);
		}
		return hostBuilder.toString();
	}
	
	public String toJsonString() throws JSONException {
		JSONObject rootObj = new JSONObject();
		
		if (mNetType != null) {
			rootObj.put("netType", mNetType.getValue());
		}
		if (mApnType != null) {
			rootObj.put("apnType", mApnType.getValue());
		}
		rootObj.put("extraInfo", mExtraInfo);
		rootObj.put("proxyHost", mProxyHost);
		rootObj.put("proxyPort", mProxyPort);
		rootObj.put("proxyType", mProxyType);
		rootObj.put("isUseProxy", mIsUseProxy);
		rootObj.put("bssid", mBssid);
		
		return rootObj.toString();
	}
	
	public void fromJson(JSONObject rootObj) {
		mNetType = NetType.from(rootObj.optInt("netType", -99));
		mApnType = ApnType.from(rootObj.optInt("apnType", -99));
		mExtraInfo = rootObj.optString("extraInfo", "");
		mProxyHost = rootObj.optString("proxyHost", "");
		mProxyPort = rootObj.optInt("proxyPort", 0);
		mProxyType = rootObj.optInt("proxyType", PROXY_TYPE_CM);
		mIsUseProxy = rootObj.optBoolean("isUseProxy", mIsUseProxy);
		mBssid = rootObj.optString("bssid", "");
	}
	
	public static ConnectInfo fromJson(String jsonStr) {
		if (StringUtil.isEmpty(jsonStr)) {
			return null;
		}
		try {
			ConnectInfo connectInfo = new ConnectInfo(null);
			connectInfo.fromJson(new JSONObject(jsonStr));
			return connectInfo;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		
		builder.append("ConnectInfo(mNetType=");
		builder.append(mNetType);
		builder.append(", mApnType=");
		builder.append(mApnType);
		builder.append(", mApnName=");
		builder.append(mApnName);
		builder.append(", mExtraInfo=");
		builder.append(mExtraInfo);
		builder.append(", mProxyHost=");
		builder.append(mProxyHost);
		builder.append(", mProxyPort=");
		builder.append(mProxyPort);
		builder.append(", mProxyType=");
		builder.append(mProxyType);
		builder.append(", mIsUseProxy=");
		builder.append(mIsUseProxy);
		builder.append(", mBssid=");
		builder.append(mBssid);
		builder.append(")");
		
		return builder.toString();
	}
	
}
