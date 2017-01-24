package qrom.component.wup.transport.http.route;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.iplist.IPListProxy;
import qrom.component.wup.iplist.SelectedIPPortResult;
import qrom.component.wup.transport.http.HttpRouteInfo;
import qrom.component.wup.transport.http.IHttpRouteChooser;
import TRom.EIPType;

public class IPListHttpRouteChooser implements IHttpRouteChooser {
	private RunEnvType mEnvType;
	
	public IPListHttpRouteChooser() {
	}
	
	public IPListHttpRouteChooser(RunEnvType envType) {
		this.mEnvType = envType;
	}
	
	@Override
	public HttpRouteInfo selectRouteInfo() {
		// 非正式不走IPList
		if (mEnvType != RunEnvType.IDC) {
			return null;
		}
		
		ConnectInfo connectInfo = ConnectInfoManager.get().getConnectInfo();
		SelectedIPPortResult result = IPListProxy.get().selectIPPort(mEnvType, EIPType.WUPPROXY, connectInfo);
		if (result == null) {
			return null;
		}
		
		return new IPListHttpRouteInfo(result, connectInfo, this);
	}

	@Override
	public void reportNetworkError(HttpRouteInfo routeInfo, int errorCode) {
		if (!routeInfo.getConnectInfo().isConnected()) {
			// 无网络，不进行上报
			return ;
		}
		
		IPListProxy.get().reportError(((IPListHttpRouteInfo)routeInfo).getSelectedIPPortResult(), errorCode);
	}

}
