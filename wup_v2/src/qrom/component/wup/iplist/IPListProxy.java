package qrom.component.wup.iplist;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.event.EventType;
import qrom.component.wup.base.event.IEventSubscriber;
import qrom.component.wup.base.event.Subscriber;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.Detector;
import qrom.component.wup.iplist.node.IPListNode;
import TRom.EIPType;

/**
 *  IPList的接口代理层
 *  用于屏蔽获取细节
 * @author wileywang
 *
 */
public class IPListProxy implements IEventSubscriber {
	private static final String TAG = "IPListProxy";
	
	private static IPListProxy sInstance;
	public static IPListProxy get() {
		if (sInstance == null) {
			synchronized(IPListProxy.class) {
				if (sInstance == null) {
					sInstance = new IPListProxy();
				}
			}
		}
		return sInstance;
	}
	
	private IIPListClient mIPListClient;
	
	private IPListProxy() {
		if (IPListEngine.get() != null) {
			// 说明是直接可以获取guid的进程
			mIPListClient = new IPListClientByEngine();
			return ;
		} 
		
		if (Detector.get().isTcmExists()) {
			mIPListClient = new IPListClientByProvider(Detector.get().getTcmPackageName());
		} else if (!StringUtil.isEmpty(Detector.get().getTcmProxyPackageName())) {
			mIPListClient = new IPListClientByProvider(Detector.get().getTcmProxyPackageName());
		} else {
			mIPListClient = new IPListClientByProvider(
					ContextHolder.getApplicationContextForSure().getPackageName());
		}
		
		EventBus.getDefault().register(this);
		if (IPListEngine.get() != null) {
			QRomLog.d(TAG, "IPListProxy found IPListEngine, swtich to engine model");
			switchIPListClientByEngine();
		}
	}
	
	public SelectedIPPortResult selectIPPort(RunEnvType envType, EIPType ipType, ConnectInfo connectInfo) {
		return mIPListClient.selectIPPort(envType, ipType, connectInfo);
	}
	
	public IPListNode getCurApnIPListNode(final RunEnvType envType, final EIPType ipType) {
		return mIPListClient.getCurApnIPListNode(envType, ipType);
	}
	
	public void reportError(final SelectedIPPortResult result, final int errorCode) {
		mIPListClient.reportError(result, errorCode);
	}
	
	@Subscriber
	public void onIPListEngineStartedEvent(IPListEngineStartedEvent event) {
		if (mIPListClient != null && mIPListClient instanceof IPListClientByEngine) {
			return ;
		}
		QRomLog.d(TAG, "onIPListEngineStartedEvent, switchIPListClient to engine model");
		switchIPListClientByEngine();
	}
	
	private synchronized void switchIPListClientByEngine() {
		IIPListClient orignalIPListClient = mIPListClient;
		mIPListClient = new IPListClientByEngine();
		if (orignalIPListClient != null) {
			orignalIPListClient.release();
		}
	}

	@Override
	public IWorkRunner receiveEventOn(EventType eventType) {
		return null;
	}
}
