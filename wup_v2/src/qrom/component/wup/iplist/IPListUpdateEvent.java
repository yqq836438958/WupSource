package qrom.component.wup.iplist;

import qrom.component.wup.base.RunEnvType;

/**
 *  IPList更新事件定义
 * @author wileywang
 *
 */
public class IPListUpdateEvent {
	private RunEnvType mUpdateEnvType;
	
	public IPListUpdateEvent(RunEnvType updateEnvType) {
		this.mUpdateEnvType = updateEnvType;
	}
	
	public RunEnvType getUpdateEnvType() {
		return mUpdateEnvType;
	}
}
