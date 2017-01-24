package qrom.component.wup.statics;

import java.util.Map;

import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.framework.Request.RequestType;

public class FailedReqStatInfo extends ReqStatBaseInfo {
	
	private int mErrorCode;
	private String mErrorMsg;
	
	private long mReqDataLength;
	
	private String mServerUrl;
	
	private String mSIP;
	
	private String mClientIP;
	
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public void setErrorCode(int errorCode) {
		this.mErrorCode = errorCode;
	}
	
	public String getErrorMsg() {
		return mErrorMsg;
	}
	
	public void setErrorMsg(String errorMsg) {
		this.mErrorMsg = errorMsg;
	}
	
	public String getServerUrl() {
		return mServerUrl;
	}
	
	public void setServerUrl(String serverUrl) {
		this.mServerUrl = serverUrl;
	}
	
	public String getSIP() {
		return mSIP;
	}
	
	public void setSIP(String sip) {
		this.mSIP = sip;
	}
	
	public String getClientIP() {
		return mClientIP;
	}
	
	public void setClientIP(String clientIP) {
		this.mClientIP = clientIP;
	}
	
	public long getReqDataLength() {
		return mReqDataLength;
	}
	
	public void setReqDataLength(long reqDataLength) {
		this.mReqDataLength = reqDataLength;
	}
	
	public FailedReqStatInfo(byte[] guidBytes, RequestType requestType) {
		super(guidBytes, requestType);
	}

	@Override
	protected void doFillSubFields(Map<String, String> statMap) {
		statMap.put(StatConstants.KEY.RESULT, StatConstants.VALUE.RESULT_FAIL);
		
		statMap.put(StatConstants.KEY.EXTRA, mErrorCode + "." + mErrorMsg);
		if (!StringUtil.isEmpty(mServerUrl)) {
			statMap.put(StatConstants.KEY.REQ_URL, mServerUrl);
		}
		
		if (!StringUtil.isEmpty(mSIP)) {
			statMap.put(StatConstants.KEY.IPLIST_INFO, mSIP);
		} else {
			statMap.put(StatConstants.KEY.IPLIST_INFO, "na");
		}
		
		if (!StringUtil.isEmpty(mClientIP)) {
			statMap.put(StatConstants.KEY.IPLIST_CLENT_IP, mClientIP);
		} else {
			statMap.put(StatConstants.KEY.IPLIST_CLENT_IP, "na");
		}
		
		statMap.put(StatConstants.KEY.REQDATA_LEN, String.valueOf(mReqDataLength));
	}

}
