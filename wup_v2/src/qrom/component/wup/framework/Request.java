package qrom.component.wup.framework;

import qrom.component.wup.base.RunEnvType;

import com.qq.jce.wup.UniPacket;

/**
 *  请求对象的封装
 * @author wileywang
 *
 */
public class Request implements ISupportUserData {
	
	public static enum RequestType {
		NORMAL_REQUEST,   // 普通的wup请求
		ASYM_ENCRPT_REQUEST  // 非对称加密请求
	}
	
	private RequestOption mRequestOption;
	private UserData mUserData;
	
	private UniPacket mReqPacket;
	
	private byte[] mReqPacketBytes; // 存储最原始的UniPacket的数据
	private String mReqServiceName;
	private String mReqFuncName;
	// ===============================
	
	private RequestType mRequestType;
	private byte[] mGuidBytes;
	
	private String mAppPkgInfo;
	
	private byte[] mTransportData;
	
	private RunEnvType mRequestEnvType;  // 强制指定环境类型
	
	public Request(byte[] packetBytes
			, String serviceName
			, String funcName
			, RequestType requestType) {
		if (packetBytes == null) {
			throw new IllegalArgumentException("packetBytes should not be null");
		}
		
		mReqPacketBytes = packetBytes;
		mReqServiceName = serviceName;
		mReqFuncName = funcName;
		
		
		mRequestType = requestType;
		mRequestOption = new RequestOption();
	}
	
	public Request(UniPacket packet, RequestType requestType) {
		if (packet == null) {
			throw new IllegalArgumentException("packet should not be null");
		}
		if (requestType == null) {
			throw new IllegalArgumentException("requestType should not be null");
		}
		
		mReqPacket = packet;
		mReqServiceName = mReqPacket.getServantName();
		mReqFuncName = mReqPacket.getFuncName();
		
		mRequestType = requestType;
		mRequestOption = new RequestOption();
	}
	
	public byte[] getPacketEncodeData() {
		if (mReqPacketBytes == null) {
			try {
				mReqPacketBytes = mReqPacket.encode(); 
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return mReqPacketBytes;
	}
	
	public String getServiceName() {
		return mReqServiceName;
	}
	
	public String getFuncName() {
		return mReqFuncName;
	}
	
	public RequestOption getRequestOption() {
		return mRequestOption;
	}
	
	public byte[] getTransportData() {
		return mTransportData;
	}
	
	public Request setTransportData(byte[] transportData) {
		mTransportData = transportData;
		return this;
	}
	
	public RequestType getRequestType () {
		return mRequestType;
	}
	
	public byte[] getGuidBytes() {
		return mGuidBytes;
	}
	
	public Request setGuidBytes(byte[] guidBytes) {
		mGuidBytes = guidBytes;
		return this;
	}
	
	public String getAppPkgInfo() {
		return mAppPkgInfo;
	}
	
	public Request setAppPkgInfo(String appPkgInfo) {
		mAppPkgInfo = appPkgInfo;
		return this;
	}
	
	public RunEnvType getRequestEnvType() {
		return mRequestEnvType;
	}
	
	public Request setRequestEnvType(RunEnvType envType) {
		this.mRequestEnvType = envType;
		return this;
	}
	
	@Override
	public UserData getUserData() {
		if (mUserData == null) {
			synchronized(this) {
				if (mUserData == null) {
					mUserData = new UserData();
				}
			}
		}
		return mUserData;
	}
}
