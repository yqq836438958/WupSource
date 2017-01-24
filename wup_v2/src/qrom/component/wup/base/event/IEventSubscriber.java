package qrom.component.wup.base.event;

import qrom.component.wup.base.IWorkRunner;

/**
 *  消息接收者
 * @author wileywang
 *
 */
public interface IEventSubscriber {
	/**
	 *  指定在那个线程上接收广播
	 * @return
	 */
	public IWorkRunner receiveEventOn(EventType eventType);
	

}
