package qrom.component.wup.category;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.framework.IModule;
import qrom.component.wup.framework.IRespModule;

/**
 *  网络策略模块
 * @author wileywang
 *
 */

public class NetworkCategoryModule implements IRespModule {

	@Override
	public Result onProcess(
			long reqId,
			IRespModule.Param param,
			IModuleProcessor<IRespModule.Param> processor) {
		// 连接超时，应该增加一次请求的重试
		int errorCode = param.getResponse().getErrorCode();
		if (errorCode == WUP_ERROR_CODE.WUP_CONNECTION_TIMEOUT) {
			// 网络连接超时，可能是暂时网络不通，可以增加一次重试
			if (param.getRequest().getRequestOption().getRetryTimes() <= 0) {
				param.getRequest().getRequestOption().setRetryTimes(1);
			}
			
			// 同时转换错误码
			param.getResponse().setSubErrorCode(errorCode);
			param.getResponse().setErrorCode(WUP_ERROR_CODE.WUP_NETWORK_ERROR);
			
		} else if (errorCode == WUP_ERROR_CODE.WUP_READ_TIMEOUT) {
			
			param.getResponse().setErrorCode(WUP_ERROR_CODE.WUP_NETWORK_ERROR);
			param.getResponse().setSubErrorCode(errorCode);
			
		} else if (errorCode == WUP_ERROR_CODE.WUP_CONNECTED_FAILED) {
			
			// 连接失败，但是网咯是联通状态，可以增加一次重试
			if (ConnectInfoManager.get().getConnectInfo().isConnected()) {
				if (param.getRequest().getRequestOption().getRetryTimes() <= 0) {
					param.getRequest().getRequestOption().setRetryTimes(1);
				}
			}
			
			param.getResponse().setErrorCode(WUP_ERROR_CODE.WUP_NETWORK_ERROR);
			param.getResponse().setSubErrorCode(errorCode);
			
		}
		
		return IModule.RESULT_OK;
	}

	@Override
	public void cancel(long reqId) {
	}

	@Override
	public String getModuleName() {
		return NetworkCategoryModule.class.getSimpleName();
	}

}
