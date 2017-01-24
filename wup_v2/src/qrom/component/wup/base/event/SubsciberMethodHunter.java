package qrom.component.wup.base.event;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * the subscriber method hunter, find all of the subscriber's methods which
 * annotated with @Subcriber.
 * 
 * @author mrsimple
 */
class SubsciberMethodHunter {

    /**
     * the event bus's subscriber's map
     */
    Map<EventType, CopyOnWriteArrayList<Subscription>> mSubcriberMap;

    /**
     * @param subscriberMap
     */
    public SubsciberMethodHunter(Map<EventType, CopyOnWriteArrayList<Subscription>> subscriberMap) {
        mSubcriberMap = subscriberMap;
    }

    /**
     * @param subscriber
     * @return
     */
    public void findSubcribeMethods(IEventSubscriber subscriber) {
        if (mSubcriberMap == null) {
            throw new NullPointerException("the mSubcriberMap is null. ");
        }
        Class<?> clazz = subscriber.getClass();
        // 查找类中符合要求的注册方法,直到Object类
        while (clazz != null && !isSystemCalss(clazz.getName())) {
            final Method[] allMethods = clazz.getDeclaredMethods();
            for (int i = 0; i < allMethods.length; i++) {
                Method method = allMethods[i];
                // 根据注解来解析函数
                Subscriber annotation = method.getAnnotation(Subscriber.class);
                if (annotation != null) {
                    // 获取方法参数
                    Class<?>[] paramsTypeClass = method.getParameterTypes();
                    // just only one param
                    if (paramsTypeClass != null && paramsTypeClass.length == 1) {
                        Class<?> paramType = convertType(paramsTypeClass[0]);
                        EventType eventType = new EventType(paramType);
                        TargetMethod subscribeMethod = new TargetMethod(method, eventType);
                        subscibe(eventType, subscribeMethod, subscriber);
                    }
                }
            } // end for

            // 获取父类,以继续查找父类中符合要求的方法
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * @param event
     * @param method
     * @param subscriber
     */
    private void subscibe(EventType event, TargetMethod method, IEventSubscriber subscriber) {
        CopyOnWriteArrayList<Subscription> subscriptionLists = mSubcriberMap
                .get(event);
        if (subscriptionLists == null) {
            subscriptionLists = new CopyOnWriteArrayList<Subscription>();
        }

        Subscription newSubscription = new Subscription(subscriber, method);
        if (subscriptionLists.contains(newSubscription)) {
            return;
        }

        subscriptionLists.add(newSubscription);
        // 将事件类型key和订阅者信息存储到map中
        mSubcriberMap.put(event, subscriptionLists);
    }

    /**
     * remove subscriber methods from map
     * 
     * @param subscriber
     */
    public void removeMethodsFromMap(IEventSubscriber subscriber) {
        Iterator<CopyOnWriteArrayList<Subscription>> iterator = mSubcriberMap
                .values().iterator();
        while (iterator.hasNext()) {
            CopyOnWriteArrayList<Subscription> subscriptions = iterator.next();
            if (subscriptions != null) {
                List<Subscription> foundSubscriptions = new
                        LinkedList<Subscription>();
                Iterator<Subscription> subIterator = subscriptions.iterator();
                while (subIterator.hasNext()) {
                    Subscription subscription = subIterator.next();
                    // 获取引用
                    IEventSubscriber cacheObject = subscription.subscriber;
                    if (cacheObject.equals(subscriber)) {
                        Log.d("", "### remove subscribe" + subscriber.getClass().getName());
                         foundSubscriptions.add(subscription);
                    }
                }

                // 移除该subscriber的相关的Subscription
                 subscriptions.removeAll(foundSubscriptions);
            }

            // 如果针对某个Event的订阅者数量为空了,那么需要从map中清除
            if (subscriptions == null || subscriptions.size() == 0) {
                iterator.remove();
            }
        }
    }

    /**
     * if the subscriber method's type is primitive, convert it to corresponding
     * Object type. for example, int to Integer.
     * 
     * @param eventType origin Event Type
     * @return
     */
    private Class<?> convertType(Class<?> eventType) {
        Class<?> returnClass = eventType;
        if (eventType.equals(boolean.class)) {
            returnClass = Boolean.class;
        } else if (eventType.equals(int.class)) {
            returnClass = Integer.class;
        } else if (eventType.equals(float.class)) {
            returnClass = Float.class;
        } else if (eventType.equals(double.class)) {
            returnClass = Double.class;
        }

        return returnClass;
    }

    private boolean isSystemCalss(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.");
    }

}
