package qrom.component.wup.iplist;

import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.iplist.node.IPListNode;
import TRom.EIPType;

/**
 * 
 * @author wileywang
 *
 */
class IPListClientByEngine  implements IIPListClient {

	@Override
	public SelectedIPPortResult selectIPPort(
			RunEnvType envType, EIPType ipType, ConnectInfo connectInfo) {
		int apnIndex = ApnIndexConvertor.getApnIndex(connectInfo);
		
		IPPortNodeInfo selectedIPPortNode = IPListEngine.get().selectIPPort(envType, ipType, apnIndex);
		if (selectedIPPortNode == null) {
			return null;
		}
		
		return new SelectedIPPortResult(
				selectedIPPortNode, ipType, envType, apnIndex, connectInfo.getBssid()).setExtraData("engine_result");
	}

	@Override
	public void reportError(SelectedIPPortResult result, int errorCode) {
		if (!ConnectInfoManager.get().getConnectInfo().isConnected()) {
			return ;
		}
		
		IPListEngine.get().reportError(result, errorCode);
	}

	@Override
	public IPListNode getCurApnIPListNode(RunEnvType envType, EIPType ipType) {
		return IPListEngine.get().copyIPListNode(envType, ipType
						, ApnIndexConvertor.getApnIndex(ConnectInfoManager.get().getConnectInfo()));
	}

	@Override
	public void release() {
	}


}
