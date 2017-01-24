package qrom.component.wup.base.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.android.HandlerWorkRunner;
import android.os.Looper;

/**
  * 事件分发总线
  */
public final class EventBus {

    private static EventBus sDefaultBus;
    public static EventBus getDefault() {
        if (sDefaultBus == null) {
            synchronized (EventBus.class) {
                if (sDefaultBus == null) {
                    sDefaultBus = new EventBus();
                }
            }
        }
        return sDefaultBus;
    }
    
    private IWorkRunner mMainThreadRunner = new HandlerWorkRunner(Looper.getMainLooper());
    private final Map<EventType, CopyOnWriteArrayList<Subscription>> mSubcriberMap 
		= new ConcurrentHashMap<EventType, CopyOnWriteArrayList<Subscription>>();
    
    private SubsciberMethodHunter mMethodHunter = new SubsciberMethodHunter(mSubcriberMap);
    
    /**
     * private Constructor
     */
    public EventBus() {
    }

    /**
     * @return
     */
   

    /**
     * register a subscriber into the mSubcriberMap, the key is subscriber's
     * method's name and tag which annotated with {@see Subcriber}, the value is
     * a list of Subscription.
     * 
     * @param subscriber the target subscriber
     */
    public void register(IEventSubscriber subscriber) {
        if (subscriber == null) {
            return;
        }

        synchronized (this) {
            mMethodHunter.findSubcribeMethods(subscriber);
        }
    }


    /**
     * @param subscriber
     */
    public void unregister(IEventSubscriber subscriber) {
        if (subscriber == null) {
            return;
        }
        synchronized (this) {
            mMethodHunter.removeMethodsFromMap(subscriber);
        }
    }

    /**
     * post a event
     * 
     * @param event
     */
    public void post(final Object event) {
    	if (event == null) {
    		throw new IllegalArgumentException("event should not be null!");
    	}
    	
    	EventType eventType = new EventType(event.getClass());
    	
    	List<Subscription> subscriptions = mSubcriberMap.get(eventType);
        if (subscriptions == null) {
            return;
        }
        
        for (final Subscription subscription : subscriptions) {
            IWorkRunner eventRunner = subscription.subscriber.receiveEventOn(eventType);
            if (eventRunner == null) {
            	eventRunner = mMainThreadRunner;
            } 
            
            eventRunner.postWork(new Runnable() {
				@Override
				public void run() {
					try {
						subscription.targetMethod.invoke(subscription.subscriber, event);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
            	
            });
        }
    }

    /**
     * 返回订阅map
     * 
     * @return
     */
    public Map<EventType, CopyOnWriteArrayList<Subscription>> getSubscriberMap() {
        return mSubcriberMap;
    }

    /**
     * clear the events and subcribers map
     */
    public synchronized void clear() {
        mSubcriberMap.clear();
    }

}
