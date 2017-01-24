package qrom.component.wup.transport.http.route;

import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.iplist.SelectedIPPortResult;
import qrom.component.wup.transport.http.HttpAddr;
import qrom.component.wup.transport.http.HttpRouteInfo;
import qrom.component.wup.transport.http.IHttpRouteChooser;

public class IPListHttpRouteInfo extends HttpRouteInfo {

	private SelectedIPPortResult mSelectedIPPortResult;
	
	public IPListHttpRouteInfo(SelectedIPPortResult result
			, ConnectInfo connectInfo
			, IHttpRouteChooser routeChooser) {
		super(connectInfo
				, new HttpAddr(result.getNodeInfo().getNode().getIp()
								, result.getNodeInfo().getNode().getPort())
				, result.getEnvType()
				, routeChooser);
		
		this.mSelectedIPPortResult = result;
	}
	
	public SelectedIPPortResult getSelectedIPPortResult() {
		return mSelectedIPPortResult;
	}
	
	public String getClientIP() {
		return mSelectedIPPortResult.getNodeInfo().getClientIP();
	}
	
	@Override
	public String getStatSIP() {
		return mSelectedIPPortResult.getNodeInfo().getIPIndex() + "_" 
				+ mSelectedIPPortResult.getNodeInfo().getIPListSize() + "_"
				+ mSelectedIPPortResult.getApnIndex();
	}
}
