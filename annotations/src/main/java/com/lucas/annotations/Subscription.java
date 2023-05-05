package com.lucas.annotations;

/**
 * Created by xujinjin
 * Date: 2023/04/27
 * Description: 保存注册了订阅者的类，即有register的类和加了注解Subscribe的方法的java类
 */
public class Subscription implements Comparable<Subscription>{
    //订阅者类
    private Object subscriber;
    //订阅者方法类
    private SubscribedMethod subscribedMethod;
    // 优先级
    private int priority;

    public Subscription(Object subscriber, SubscribedMethod subscribedMethod, int priority) {
        this.subscriber = subscriber;
        this.subscribedMethod = subscribedMethod;
        this.priority = priority;

    }

    public Object getSubscriber() {
        return subscriber;
    }

    public SubscribedMethod getSubscribedMethod() {
        return subscribedMethod;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "subscriber=" + subscriber +
                ", subscribedMethod=" + subscribedMethod +
                '}';
    }

    @Override
    public int compareTo(Subscription other) {
        return Integer.compare(other.priority, this.priority); // 降序排列
    }
}
