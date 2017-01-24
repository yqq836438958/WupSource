package qrom.component.wup.base.net;

/**
 * @author wileywang
 */
public interface IConnectInfoListener {
	/**
	 *  连接信息被重新加载, 可以用于判断网络变更
	 */
	public void onConnectInfoReload();
	
	/**
	 *  收到网络变更消息
	 */
	public void onReceiveNetworkChanged();
}
