package qrom.component.wup.transport;

import qrom.component.wup.base.RunEnvType;
import TRom.EIPType;

/**
 * @author wileywang
 *
 */
public interface ITransportRouter {
	/**
	 *  传输层通用路由器定义
	 * @param envType 运行环境
	 * @param ipType IP类型
	 * @return 返回的形式为ip:port的形式字符串
	 */
	public String getAddress(RunEnvType envType, EIPType ipType);
}
