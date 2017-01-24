package qrom.component.wup.framework;

/**
 *  回包处理流程的统一抽象
 * @author wileywang
 *
 */
public interface IRespModule extends IModule <IRespModule.Param> {

	public static class Param {
		private Request mRequest;
		private Response mResponse;
		
		public Param(Request request, Response response) {
			this.mRequest = request;
			this.mResponse = response;
		}
		
		public Request getRequest() {
			return mRequest;
		}
		
		public Response getResponse() {
			return mResponse;
		}
	}
	
}
