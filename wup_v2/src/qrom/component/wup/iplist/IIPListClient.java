package qrom.component.wup.iplist;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.iplist.node.IPListNode;
import TRom.EIPType;

/**
 * IPList的API的获取抽象
 * @author wileywang
 *
 */
interface IIPListClient {
	/**
	 *  选择一个IPList的接入节点
	 * @param ipType IP类型
	 * @param envType 运行环境
	 * @return null: 未找到
	 */
	public SelectedIPPortResult selectIPPort(
			final RunEnvType envType, final EIPType ipType, ConnectInfo connectInfo);
	
	/**
	 *  获取当前接入点的iplist
	 * @return
	 */
	public IPListNode getCurApnIPListNode(
			final RunEnvType envType, final EIPType ipType);
	
	/**
	 *  主动告知错误，方便选择策略切换
	 * @param result
	 */
	public void reportError(final SelectedIPPortResult result, final int errorCode);
	
	/**
	 *  主动释放
	 */
	public void release();
}
