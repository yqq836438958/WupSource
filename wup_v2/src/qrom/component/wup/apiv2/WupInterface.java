package qrom.component.wup.apiv2;

import java.util.ArrayList;
import java.util.List;

import TRom.EIPType;

import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnv;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.event.EventType;
import qrom.component.wup.base.event.IEventSubscriber;
import qrom.component.wup.base.event.Subscriber;
import qrom.component.wup.base.utils.ListenerList;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.BaseInfoManager;
import qrom.component.wup.guid.GuidProxy;
import qrom.component.wup.guid.GuidUpdateEvent;
import qrom.component.wup.iplist.IPListProxy;
import qrom.component.wup.iplist.node.IPListNode;
import qrom.component.wup.iplist.node.IPPortNode;

/**
 * 面向上层调用方的Facade封装，封装了可统一获取的API, 静态接口层
 * @author wileywang
 *
 */
public class WupInterface implements IEventSubscriber {
	public static byte[] getGuidBytes() {
		return GuidProxy.get().getGuidBytes();
	}
	
	public static String getGuidStr() {
		return StringUtil.byteToHexString(getGuidBytes());
	}
	
	public static String getImei() {
		return BaseInfoManager.get().getImei();
	}
	
	public static String getQua() {
		return BaseInfoManager.get().getQua();
	}
	
	public static String getLC() {
		return BaseInfoManager.get().getLC();
	}
	
	public static List<String> getCurWupProxyIPList() {
		return getCurApnIPList(EIPType.WUPPROXY);
	}
	
	public static List<String> getCurWupSocketIPList() {
		return getCurApnIPList(EIPType.WUPSOCKET);
	}
	
	private static List<String> getCurApnIPList(EIPType ipType) {
		IPListNode ipListNode = 
				IPListProxy.get().getCurApnIPListNode(RunEnv.get().getEnvType(), ipType);
		if (ipListNode == null) {
			return null;
		}
		
		List<String> resultList = new ArrayList<String>();
		for (IPPortNode ipPortNode : ipListNode.getIPPortList()) {
			resultList.add(ipPortNode.toUrlString());
		}
		
		return resultList;
	}
	
	public static void registerGuidListener(IGuidListener listener) {
		WupInterface.get().addGuidListener(listener);
	}
	
	public static void unRegisterGuidListener(IGuidListener listener) {
		WupInterface.get().removeGuidListener(listener);
	}
	
	private static WupInterface sInstance;
	private static WupInterface get() {
		if (sInstance == null) {
			synchronized(WupInterface.class) {
				if (sInstance == null) {
					sInstance = new WupInterface();
				}
			}
		}
		return sInstance;
	}
	
	private ListenerList<IGuidListener> mGuidListenerList;
	private WupInterface() {
		mGuidListenerList = new ListenerList<IGuidListener>() {

			@Override
			protected void onNotifyListener(IGuidListener listener,
					Object... params) {
				listener.onGuidChanged((byte[])(params[0]));
			}
			
		};
		EventBus.getDefault().register(this);
	}
	
	void addGuidListener(IGuidListener listener) {
		if (listener == null) {
			return ;
		}
		
		mGuidListenerList.registerListener(listener);
	}
	
	void removeGuidListener(IGuidListener listener) {
		if (listener == null) {
			return ;
		}
		
		mGuidListenerList.unregisterListener(listener);
	}
	
	@Subscriber
	public void onGuidUpdateEvent(GuidUpdateEvent event) {
		mGuidListenerList.notifyListeners(event.getNewGuidBytes());
	}

	@Override
	public IWorkRunner receiveEventOn(EventType eventType) {
		return null;
	}
	
}
