package qrom.component.wup.transport.http;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfo;

public class HttpRouteInfo {
	private ConnectInfo mConnectInfo;  // 请求时对应的连接信息
	private HttpAddr mHttpAddr;  //请求的后台地址
	private RunEnvType mEnvType; //请求的地址对应的环境
	
	private IHttpRouteChooser mRouteChooser; // 提取改路由的选择器
	
	public HttpRouteInfo(ConnectInfo connectInfo
			, HttpAddr httpAddr
			, RunEnvType envType
			, IHttpRouteChooser routeChooser) {
		mConnectInfo = connectInfo;
		mHttpAddr = httpAddr;
		mEnvType = envType;
		mRouteChooser = routeChooser;
	}
	
	public ConnectInfo getConnectInfo() {
		return mConnectInfo;
	}
	
	public HttpAddr getHttpAddr() {
		return mHttpAddr;
	}
	
	public RunEnvType getEnvType() {
		return mEnvType;
	}
	
	public IHttpRouteChooser getRouteChooser() {
		return mRouteChooser;
	}
	
	public String getClientIP() {
		return "";
	}
	
	public String getStatSIP() {
		return "";
	}
}
