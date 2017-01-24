package qrom.component.wup.framework.core;

import qrom.component.wup.framework.IModule;

/**
 * 最后请求任务的执行器
 * @author wileywang
 *
 */
public interface IModuleTaskExecutor<ModuleParamType> {
	/**
	 *  所有的模块都已经处理完毕， 告知执行器进行对应的操作
	 */
	public void onTaskFinished(final long requestId, final ModuleParamType paramType);
	
	/**
	 *  有模块中断任务的执行
	 */
	public void onTaskCancelled(final long requestId
			, final ModuleParamType paramType
			, int errorCode
			, String errorMsg
			, IModule<ModuleParamType> module);
}
