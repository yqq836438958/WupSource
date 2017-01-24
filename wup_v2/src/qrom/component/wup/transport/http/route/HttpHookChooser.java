package qrom.component.wup.transport.http.route;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.transport.ITransportRouter;
import qrom.component.wup.transport.http.HttpAddr;
import qrom.component.wup.transport.http.HttpRouteInfo;
import qrom.component.wup.transport.http.IHttpRouteChooser;
import TRom.EIPType;

public class HttpHookChooser implements IHttpRouteChooser {

	private ITransportRouter mHookRouter;
	private RunEnvType mEnvType;
	
	public HttpHookChooser(ITransportRouter hookRouter, RunEnvType envType) {
		mHookRouter = hookRouter;
		mEnvType = envType;
	}
	
	@Override
	public HttpRouteInfo selectRouteInfo() {
		if (mHookRouter == null) {
			return null;
		}
		
		String address = mHookRouter.getAddress(mEnvType, EIPType.WUPPROXY);
		if (StringUtil.isEmpty(address)) {
			return null;
		}
		String[] splits = address.split(":");
		if (splits.length != 2) {
			return null;
		}
		
		try {
			HttpAddr httpAddr = new HttpAddr(splits[0], Integer.valueOf(splits[1]));
			return new HttpRouteInfo(ConnectInfoManager.get().getConnectInfo(), httpAddr, mEnvType, this);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void reportNetworkError(HttpRouteInfo routeInfo, int errorCode) {
	}

}
