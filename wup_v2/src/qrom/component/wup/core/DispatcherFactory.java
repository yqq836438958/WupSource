package qrom.component.wup.core;

import qrom.component.wup.autoretry.RetryModule;
import qrom.component.wup.base.android.HandlerWorkRunner;
import qrom.component.wup.category.NetworkCategoryModule;
import qrom.component.wup.framework.core.Dispatcher;
import qrom.component.wup.guid.GuidModule;
import qrom.component.wup.security.AsymSecurityModule;
import qrom.component.wup.switcher.SwitchModule;
import qrom.component.wup.transport.HttpTransportLayer;
import android.os.HandlerThread;
import android.os.Looper;

/**
 *  负责提供模块Dispatcher的构造，模块的拼接, 构建系统的组装核心类
 * @author wileywang
 *
 */
public class DispatcherFactory {
	public static Dispatcher getDefault() {
		if (sFactoryInstance == null) {
			synchronized(DispatcherFactory.class) {
				if (sFactoryInstance == null) {
					sFactoryInstance = new DispatcherFactory();
				}
			}
		}
		return sFactoryInstance.getDispatcher();
	}
	
	private static DispatcherFactory sFactoryInstance;
	
	private static class DispatcherImpl extends Dispatcher {
		
		private AsymSecurityModule mAsymSecurityModule;
		
		public DispatcherImpl(Looper looper) {
			super(new HandlerWorkRunner(looper));
			
			getDispatcherRunner().postWork(new Runnable() {

				@Override
				public void run() {
					initModules();
					initTransportLayer();
				}
			});
		}
		
		protected void initModules() {
			mAsymSecurityModule = new AsymSecurityModule(getDispatcherRunner());
			
			initReqModules();
			initRespModules();
		}
		
		protected void initTransportLayer() {
			mTransportLayer = new HttpTransportLayer(getDispatcherRunner());
		}
		
		protected void initReqModules() {
			// 开关模块放在第一个
			mReqModuleList.add(new SwitchModule());
			
			// 填充请求的guid
			mReqModuleList.add(new GuidModule());
			
			// 加密处理一定放在最后
			mReqModuleList.add(mAsymSecurityModule.getReqModule());
		}
		
		protected void initRespModules() {
			// 优先进行解密处理
			mRespModuleList.add(mAsymSecurityModule.getRespModule());
			
			// 网络策略调整
			mRespModuleList.add(new NetworkCategoryModule());
			
			// 回复模块在最后按照策略，对进行进行重试
			mRespModuleList.add(new RetryModule(getDispatcherRunner()));
		}
		
	}
	
	private HandlerThread mDispatcherThread;
	private DispatcherImpl mDispatcherImpl;
	
	private DispatcherFactory() {
		mDispatcherThread  = new HandlerThread("WupDispatcher", android.os.Process.THREAD_PRIORITY_FOREGROUND);
		mDispatcherThread.start();
		mDispatcherImpl = new DispatcherImpl(mDispatcherThread.getLooper());
	}
	
	public Dispatcher getDispatcher() {
		return mDispatcherImpl;
	}
	
}
