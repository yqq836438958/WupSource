package qrom.component.wup.apiv2;

import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.android.HandlerWorkRunner;
import android.os.Looper;

/**
 *  异步请求的请求配置
 * @author wileywang
 *
 */
public class AsyncWupOption extends WupOption {
	
	private IWorkRunner mCallbackRunner;
	
	public AsyncWupOption(WupType wupType) {
		this(wupType, new HandlerWorkRunner(Looper.myLooper()));
	}
	
	public AsyncWupOption(WupOption wupOption) {
		this(wupOption, new HandlerWorkRunner(Looper.myLooper()));
	}
	
	public AsyncWupOption(WupOption wupOption, IWorkRunner callbackRunner) {
		super(wupOption);
		this.mCallbackRunner = callbackRunner;
	}
	
	public AsyncWupOption(WupType wupType, IWorkRunner callbackRunner) {
		super(wupType);
		this.mCallbackRunner = callbackRunner;
	}
	
	public IWorkRunner getCallbackRunner() {
		return mCallbackRunner;
	}
}
