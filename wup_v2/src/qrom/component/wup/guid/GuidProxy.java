package qrom.component.wup.guid;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.event.EventType;
import qrom.component.wup.base.event.IEventSubscriber;
import qrom.component.wup.base.event.Subscriber;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.Detector;


/**
 *  Guid获取的代理层, 用于屏蔽直接从进程内部获取guid还是寻找外部进程
 *  用于屏蔽获取细节
 * @author wileywang
 *
 */
public class GuidProxy implements IEventSubscriber {
	private static final String TAG = "GuidProxy";
	
	private static GuidProxy sInstance;
	public static GuidProxy get() {
		if (sInstance == null) {
			synchronized(GuidProxy.class) {
				if (sInstance == null) {
					sInstance = new GuidProxy();
				}
			}
		}
		return sInstance;
	}
	
	private IGuidClient mGuidClient;
	
	protected GuidProxy() {
		if (GuidEngine.get() != null) {
			// 说明是直接可以获取guid的进程
			mGuidClient = new GuidClientByEngine();
			return ;
		}
		
		if (Detector.get().isTcmExists()) {
			mGuidClient = new GuidClientByProvider(Detector.get().getTcmPackageName());
		} else if (!StringUtil.isEmpty(Detector.get().getTcmProxyPackageName())) { 
			mGuidClient = new GuidClientByProvider(Detector.get().getTcmProxyPackageName());
		} else {
			mGuidClient = new GuidClientByProvider(
					ContextHolder.getApplicationContextForSure().getPackageName());
		}
		
		EventBus.getDefault().register(this);
		// 这里再做一次确保，Guid的查询才会促使Provider中GuidEngine的创建
		if (GuidEngine.get() != null) {
			QRomLog.d(TAG, "GuidProxy Found GuidEngine, switch to engine mode");
			switchGuidClientByEngine();
		}
		
		QRomLog.d(TAG, "Current IGuid Client is " + mGuidClient);
	}
	
	public byte[] getGuidBytes() {
		return mGuidClient.getGuidBytes();
	}
	
	public int doLogin() {
		return mGuidClient.requestLogin();
	}
	
	@Subscriber
	public void onGuidEngineStartedEvent(GuidEngineStartedEvent event) {
		if (mGuidClient != null && mGuidClient instanceof GuidClientByEngine) {
			return ;
		}
		QRomLog.d(TAG, "onGuidEngineStartedEvent, switchGuidClient to engine mode");
		switchGuidClientByEngine();
	}
	
	private void switchGuidClientByEngine() {
		IGuidClient orignalGuidClient = mGuidClient;
		mGuidClient = new GuidClientByEngine();
		if (orignalGuidClient != null) {
			orignalGuidClient.release();
		}
	}

	@Override
	public IWorkRunner receiveEventOn(EventType eventType) {
		return null;
	}
}
