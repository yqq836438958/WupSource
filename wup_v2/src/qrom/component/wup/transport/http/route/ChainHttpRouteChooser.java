package qrom.component.wup.transport.http.route;

import java.util.ArrayList;
import java.util.List;

import qrom.component.wup.transport.http.HttpRouteInfo;
import qrom.component.wup.transport.http.IHttpRouteChooser;

/**
 * 链式路由选择, 优先使用上游的选择器
 * @author wileywang
 *
 */
public class ChainHttpRouteChooser implements IHttpRouteChooser {
	private List<IHttpRouteChooser> mChooserList;
	
	public ChainHttpRouteChooser(IHttpRouteChooser... chooserList) {
		mChooserList = new ArrayList<IHttpRouteChooser>();
		for (IHttpRouteChooser chooser : chooserList) {
			if (chooser != null) {
				mChooserList.add(chooser);
			}
		}
	}
	
	@Override
	public HttpRouteInfo selectRouteInfo() {
		for (IHttpRouteChooser chooser : mChooserList) {
			HttpRouteInfo routeInfo = chooser.selectRouteInfo();
			if (routeInfo != null) {
				return routeInfo;
			}
		}
		return null;
	}

	@Override
	public void reportNetworkError(HttpRouteInfo routeInfo, int errorCode) {
	}

}
