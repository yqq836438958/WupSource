package qrom.component.wup.transport.http.route;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.transport.http.HttpAddr;
import qrom.component.wup.transport.http.HttpRouteInfo;
import qrom.component.wup.transport.http.IHttpRouteChooser;

/**
 *  走域名的默认获取
 * @author wileywang
 *
 */
public class DefaultHttpRouteChooser implements IHttpRouteChooser {
	
	private RunEnvType mEnvType;
	
	public DefaultHttpRouteChooser() {
		this(null);
	}
	
	public DefaultHttpRouteChooser(RunEnvType envType) {
		this.mEnvType = envType;
	}
	
	@Override
	public HttpRouteInfo selectRouteInfo() {
		if (mEnvType == RunEnvType.Gamma) {
			return new HttpRouteInfo(ConnectInfoManager.get().getConnectInfo()
									, new HttpAddr("wtest.html5.qq.com", 55555), mEnvType, this);
		} 
		
		return new HttpRouteInfo(ConnectInfoManager.get().getConnectInfo()
								, new HttpAddr("w.html5.qq.com", 8080), mEnvType, this);
	}

	@Override
	public void reportNetworkError(HttpRouteInfo routeInfo, int errorCode) {
	}

}
