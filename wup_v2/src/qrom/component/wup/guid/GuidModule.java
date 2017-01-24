package qrom.component.wup.guid;

import qrom.component.wup.framework.IModule;
import qrom.component.wup.framework.IReqModule;

/**
 *  负责为底层提供GUID的支持
 * @author wileywang
 *
 */
public class GuidModule implements IReqModule {

	@Override
	public Result onProcess(
			long reqId,
			IReqModule.Param param,
			IModuleProcessor<IReqModule.Param> processor) {
		param.getRequest().setGuidBytes(GuidProxy.get().getGuidBytes());
		return IModule.RESULT_OK;
	}

	@Override
	public void cancel(long reqId) {
	}

	@Override
	public String getModuleName() {
		return GuidModule.class.getSimpleName();
	}

}
