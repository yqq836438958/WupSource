package qrom.component.wup.framework;


/**
 *  请求处理模块
 * @author wileywang
 *
 */
public interface IReqModule extends IModule <IReqModule.Param> {
	public static class Param {
		private Request mRequest;
		
		public Param(Request request) {
			this.mRequest = request;
		}
		
		public Request getRequest() {
			return mRequest;
		}
	}
}
