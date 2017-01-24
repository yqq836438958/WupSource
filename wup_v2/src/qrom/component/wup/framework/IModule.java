package qrom.component.wup.framework;

/**
 *  流程模块的基础抽象
 * @author wileywang
 */
public interface IModule <ModuleParamType> {
	public static enum ResultType {
		E_PENDING,  // 请求被处理被pending，不发往下个流程
		E_OK,     // 可以进行下一阶段处理
		E_CANCEL  // 应该取消请求
	}
	
	public static class Result {
		private ResultType mResultType;
		private int mResultCode;
		private String mResultDescription;
		
		public Result(ResultType resultType) {
			this(resultType, 0, "");
		}
		
		public Result(ResultType resultType
				, int resultCode
				, String resultDescription) {
			mResultType = resultType;
			mResultCode = resultCode;
			mResultDescription = resultDescription;
		}
		
		public ResultType getResultType() {
			return mResultType;
		}
		
		public int getResultCode() {
			return mResultCode;
		}
		
		public void setResultCode(int resultCode) {
			this.mResultCode = resultCode;
		}
		
		public String getResultDescription() {
			return mResultDescription;
		}
		
		public void setResultDescription(String resultDescription) {
			this.mResultDescription = resultDescription;
		}
	}
	
	public static final Result RESULT_PENDING = new Result(ResultType.E_PENDING);
	public static final Result RESULT_OK = new Result(ResultType.E_OK);
	
	
	public static interface IModuleProcessor <ModuleParamType> {
		public void continueProcess(IModule<ModuleParamType> module, final long reqId, ModuleParamType param);
		
		public void cancelProcess(IModule<ModuleParamType> module
				, final long reqId, ModuleParamType param, int errorCode, String errorMsg);
	}
	
	/**
	 *  当执行返回PENDING状态时， 需调用对应的PendingProcessor对任务进行后续处理
	 * @author wileywang
	 *
	 */
	public Result onProcess(final long reqId, ModuleParamType param, IModuleProcessor<ModuleParamType> processor);
	
	/**
	 *  取消一个Pending的处理
	 */
	public void cancel(final long reqId);
	
	/**
	 *  模块名，辅助模块开发和寻找对应的bug
	 * @return
	 */
	public String getModuleName();
}
