package qrom.component.wup.statics;

import java.util.Map;

import qrom.component.wup.framework.Request.RequestType;

public class SuccReqStatInfo extends ReqStatBaseInfo {
	private long mConnectTimeMs;
	private long mReadTimeMs;
	private long mSendTimeMs;
	private long mReqDataLength;
	private long mRespDataLength;
	
	public SuccReqStatInfo(byte[] guidBytes, RequestType requestType) {
		super(guidBytes, requestType);
	}
	
	public long getConnectTimeMs() {
		return mConnectTimeMs;
	}
	
	public void setConnectTimeMs(long connectTimeMs) {
		this.mConnectTimeMs = connectTimeMs;
	}
	
	public long getReadTimeMs() {
		return mReadTimeMs;
	}
	
	public void setReadTimeMs(long readTimeMs) {
		this.mReadTimeMs = readTimeMs;
	}
	
	public long getSendTimeMs() {
		return mSendTimeMs;
	}
	
	public void setSendTimeMs(long sendTimeMs) {
		this.mSendTimeMs = sendTimeMs;
	}
	
	public long getReqDataLength() {
		return mReqDataLength;
	}
	
	public void setReqDataLength(long reqDataLength) {
		this.mReqDataLength = reqDataLength;
	}
	
	public long getRespDataLength() {
		return mRespDataLength;
	}
	
	public void setRespDataLength(long respDataLength) {
		this.mRespDataLength = respDataLength;
	}
	
	

	@Override
	protected void doFillSubFields(Map<String, String> statMap) {
		statMap.put(StatConstants.KEY.RESULT, StatConstants.VALUE.RESULT_SUCCESS);
		
		statMap.put(StatConstants.KEY.TIME_CONNECT, String.valueOf(mConnectTimeMs));
		statMap.put(StatConstants.KEY.TIME_SEND, String.valueOf(mSendTimeMs));
		statMap.put(StatConstants.KEY.TIME_RECEIVE, String.valueOf(mReadTimeMs));
		statMap.put(StatConstants.KEY.REQDATA_LEN, String.valueOf(mReqDataLength));
		statMap.put(StatConstants.KEY.RSPDATA_LEN, String.valueOf(mRespDataLength));
	}

}
