package com.lucas.xbus;

import android.util.Log;

import com.lucas.annotations.Subscribe;
import com.lucas.annotations.SubscribedMethod;
import com.lucas.annotations.Subscription;
import com.lucas.xbus.invoke_strategy.AptAnnotationInvoke;
import com.lucas.xbus.invoke_strategy.MethodInvokeStrategy;
import com.lucas.xbus.invoke_strategy.ReflectInvoke;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by xujinjin
 * Date: 2023/04/27
 * Description:
 */
public class XEventBus {
    private static final String TAG = "XEventBus";
    static XEventBus instance = null;
    MethodInvokeStrategy invokeStrategy;
    /**
     * 同一类型EventType类与所有注册方法的集合
     * key: EventType类
     * value: EventType类对应的Subscription
     */
    private Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType = new HashMap<>();
    /**
     * 所有类有注册MyEventBus的  类与其内部所有处理方法集合。
     * key:Subscriber
     * value:该Subscriber中所有注册event的方法
     */
    private static final Map<Object, List<Class<?>>> typesBySubscriber = new ConcurrentHashMap<>();

    private static XBusBuilder DEFAULT_BUILDER = new XBusBuilder();

    public XEventBus() {
        this(DEFAULT_BUILDER);
    }

    public static XBusBuilder builder() {
        return new XBusBuilder();
    }

    public XEventBus(XBusBuilder myEventBusBuilder) {
        if (myEventBusBuilder != null && myEventBusBuilder.methodHandle != null) {
            //注解处理器获取订阅者方法和调用
            invokeStrategy = new AptAnnotationInvoke(myEventBusBuilder.methodHandle);
        } else {
            //反射获取订阅者方法和反射调用方法
            invokeStrategy = new ReflectInvoke();
        }
    }

    /**
     * 单例
     *
     * @return
     */
    public static XEventBus getDefault() {
        if (instance == null) {
            synchronized (XEventBus.class) {
                if (instance == null) {
                    instance = new XEventBus();
                }
            }
        }
        return instance;
    }

    /**
     * 注册subscriber到MyEventBus，并获取其所有加了{@link Subscribe} 的方法，并放入集合中
     *
     * @param subscriber 订阅者类，即通过register将this参数传过来的类，可以是activity、service、fragment、thread等。
     */
    public void register(Object subscriber) {

        // 检查订阅者是否已经注册
        if (typesBySubscriber.containsKey(subscriber)) {
            Log.w(TAG, "Subscriber is already registered.");
            return;
        }

        List<SubscribedMethod> allSubscribedMethods = invokeStrategy.getAllSubscribedMethods(subscriber);
        if (allSubscribedMethods == null) {
            Log.e(TAG, "register: null");
            return;
        }
        if (allSubscribedMethods.size() <= 0) {
            Log.e(TAG, "register: there is no mehod founded!");
            return;
        }
        for (SubscribedMethod subscribedMethod : allSubscribedMethods) {
            Class<?> eventType = subscribedMethod.getEventType();
            CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
            if (subscriptions == null) {
                subscriptions = new CopyOnWriteArrayList<>();
                subscriptionsByEventType.put(eventType, subscriptions);
            }

            // 以下为priority逻辑，在此处排序添加
            // 获取订阅方法的优先级
            int priority = subscribedMethod.getPriority();
            // 创建新的 Subscription 实例
            Subscription newSubscription = new Subscription(subscriber, subscribedMethod, priority);
            // 将新订阅插入到适当的位置以保持优先级顺序
            boolean inserted = false;
            for (int i = 0; i < subscriptions.size(); i++) {
                if (newSubscription.compareTo(subscriptions.get(i)) > 0) {
                    subscriptions.add(i, newSubscription);
                    inserted = true;
                    break;
                }
            }
            // 如果没有找到适当的插入位置，则将新订阅添加到列表末尾
            if (!inserted) {
                subscriptions.add(newSubscription);
            }
            // 至此完成优先级的排序

            printSubscriptionsByEventType(subscriptionsByEventType);
            // 获取这个订阅者类中记录的所有的eventType类型
            List<Class<?>> eventTypesInSubscriber = typesBySubscriber.get(subscriber);
            if (eventTypesInSubscriber == null) {
                eventTypesInSubscriber = new ArrayList<>();
                typesBySubscriber.put(subscriber, eventTypesInSubscriber);
            }
            eventTypesInSubscriber.add(eventType);
        }
        printTypesBySubscriber(typesBySubscriber, subscriber);
    }

    private void printSubscriptionsByEventType(Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType) {
        Set<Class<?>> classes = subscriptionsByEventType.keySet();
        for (Class<?> aClass : classes) {
            CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(aClass);
            for (Subscription subscription : subscriptions) {
                Log.d(TAG, "printSubscriptionsByEventType: aClass=" + aClass.getName() + " subscription=" + subscription.toString());
            }

        }
    }

    private void printTypesBySubscriber(Map<Object, List<Class<?>>> list, Object subscriber) {
        List<Class<?>> classes = list.get(subscriber);
        if (classes != null) {
            for (Class<?> aClass : classes) {
                Log.d(TAG, "register: typesBySubscriber=" + aClass.getName());
            }
        }
    }

    /**
     * 发送event消息到订阅者 处理方法
     *
     * @param event
     */
    public void post(Object event) {
        if (subscriptionsByEventType.size() <= 0) {
            Log.e(TAG, "post: no any eventbus registed named" + event.toString());
            return;
        }

        Log.d(TAG, "event.getClass()=" + event.getClass().getName());
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(event.getClass());
        for (Subscription subscription : subscriptions) {
            invokeStrategy.invokeMethod(subscription, event);
        }
    }

    /**
     * 解注册eventbus
     *
     * @param subscriber
     */
    public void unregister(Object subscriber) {
        List<Class<?>> subscribedEventTypes = typesBySubscriber.get(subscriber);
        if (subscribedEventTypes != null) {
            for (Class<?> eventType : subscribedEventTypes) {
                unsubscribe(subscriber, eventType);
            }
            typesBySubscriber.remove(subscriber);
        }
    }

    private void unsubscribe(Object subscriber, Class<?> eventType) {
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription.getSubscriber() == subscriber) {
                    subscriptions.remove(subscription);
                }
            }
            if (subscriptions.isEmpty()) {
                subscriptionsByEventType.remove(eventType);
            }
        }
    }
}
