package qrom.component.wup.transport.http;

import qrom.component.wup.base.utils.StringUtil;

/**
 *  wup 请求的http的地址定义
 * @author wileywang
 *
 */
public class HttpAddr {
	private String mDomain;  // 域名或者ip
	private int mPort;  // 端口
	
	public HttpAddr(String domain) {
		this(domain, 80);
	}
	
	public HttpAddr(String domain, int port) {
		if (StringUtil.isEmpty(domain)) {
			throw new IllegalArgumentException("domain should not be null or empty");
		}
		if (port <= 0) {
			throw new IllegalArgumentException("port should not <= 0");
		}
		
		mDomain = domain;
		mPort = port;
	}
	
	public String toUrlStr() {
		StringBuilder urlBuilder = new StringBuilder(mDomain.length() + 10);
		urlBuilder.append("http://");
		urlBuilder.append(mDomain);
		if (mPort != 80) {
			urlBuilder.append(":");
			urlBuilder.append(mPort);
		}
		return urlBuilder.toString();
	}
	
	public String getHttpHost() {
		StringBuilder httpHostBuilder = new StringBuilder(mDomain.length() + 10);
		httpHostBuilder.append(mDomain);
		if (mPort != 80) {
			httpHostBuilder.append(":");
			httpHostBuilder.append(mPort);
		}
		return httpHostBuilder.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(mDomain.length() + 16);
		builder.append("HttpAddr(");
		builder.append("mDomain=");
		builder.append(mDomain);
		builder.append(", mPort=");
		builder.append(mPort);
		builder.append(")");
		return builder.toString();
	}
}
