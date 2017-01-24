package qrom.component.wup.apiv2;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.utils.StringUtil;

public class WupOption {
	public static enum WupType {
		WUP_NORMAL_REQUEST
		, WUP_ASYM_ENCRYPT_REQUEST
	}
	
	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String CHARSET_GBK = "GBK";
	
	private WupType mWupType;
	private int mRetryTimes;
	private int mTimeoutMs;
	private String mCharset;
	
	private boolean mIsUseUniPacketV3 = true;
	
	private String mRequestPkgInfo;  
	private RunEnvType mRequestEnvType; 
	
	public WupOption(WupType wupType) {
		if (wupType == null) {
			throw new IllegalArgumentException("wupType should not be null");
		}
		mWupType = wupType;
	}
	
	public WupOption(WupOption option) {
		if (option == null) {
			throw new IllegalArgumentException("option should not be null");
		}
		
		mWupType = option.getWupType();
		mRetryTimes = option.getRetryTimes();
		mTimeoutMs = option.getTimeoutMs();
		mCharset = option.getCharset();
		mIsUseUniPacketV3 = option.isUseUniPacketV3();
		
		mRequestPkgInfo = option.getRequestPkgInfo();
		mRequestEnvType = option.getRequestEnvType();
	}
	
	public WupType getWupType() {
		return mWupType;
	}
	
	public int getRetryTimes() {
		return mRetryTimes;
	}
	
	public WupOption setRetryTimes(int retryTimes) {
		if (retryTimes > 0) {
			mRetryTimes = retryTimes;
		}
		return this;
	}
	
	public int getTimeoutMs() {
		return mTimeoutMs;
	}
	
	public WupOption setTimeoutMs(int timeoutMs) {
		this.mTimeoutMs = timeoutMs;
		return this;
	}
	
	public String getCharset() {
		if (StringUtil.isEmpty(mCharset)) {
			return CHARSET_UTF8;
		}
		return mCharset;
	}
	
	public WupOption setCharset(String charset) {
		mCharset = charset;
		return this;
	}
	
	public boolean isUseUniPacketV3() {
		return mIsUseUniPacketV3;
	}
	
	public WupOption setIsUseUniPacketV3(boolean isUseUniPacketV3) {
		this.mIsUseUniPacketV3 = isUseUniPacketV3;
		return this;
	}
	
	public String getRequestPkgInfo() {
		return mRequestPkgInfo;
	}
	
	/**
	 *  主要用于非对称加密，自定义加密包信息
	 * @param pkgInfo
	 * @return
	 */
	public WupOption setRequestPkgInfo(String pkgInfo) {
		this.mRequestPkgInfo = pkgInfo;
		return this;
	}
	
	public RunEnvType getRequestEnvType() {
		return mRequestEnvType;
	}
	
	/**
	 * 主动设置请求的环境，如果不设置，则发送请求时会根据当前环境自动选择
	 * @param envType
	 * @return
	 */
	public WupOption setRequestEnvType(RunEnvType envType) {
		mRequestEnvType = envType;
		return this;
	}
	
}
