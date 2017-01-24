package qrom.component.wup.switcher;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.QRomWupEnvironment;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.framework.IModule;
import qrom.component.wup.framework.IReqModule;

/**
 *  wup 开关模块
 * @author wileywang
 *
 */
public class SwitchModule implements IReqModule {

	@Override
	public Result onProcess(
			long reqId,
			IReqModule.Param param,
			IModuleProcessor<IReqModule.Param> processor) {
		if (QRomWupEnvironment.getInstance(ContextHolder.getApplicationContextForSure()).isAllClosed()) {
			Result cancelResult = new Result(ResultType.E_CANCEL);
			cancelResult.setResultCode(WUP_ERROR_CODE.WUP_CLOSED);
			cancelResult.setResultDescription("wup closed!");
			return cancelResult;
		}
		
		return IModule.RESULT_OK;
	}

	@Override
	public void cancel(long reqId) {
	}

	@Override
	public String getModuleName() {
		return SwitchModule.class.getSimpleName();
	}

}
