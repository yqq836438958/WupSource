package qrom.component.wup.transport.http;

/**
 * 提供Http访问路线 
 * @author wileywang
 *
 */
public interface IHttpRouteChooser {
	/**
	 *  选择路由
	 * @return
	 */
	public HttpRouteInfo selectRouteInfo();
	
	/**
	 *  路由错误信息收集
	 * @param routeInfo
	 * @param errorCode
	 */
	public void reportNetworkError(HttpRouteInfo routeInfo, int errorCode);
	
}
