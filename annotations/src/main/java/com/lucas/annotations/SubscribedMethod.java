package com.lucas.annotations;


import java.lang.reflect.Method;

public class SubscribedMethod {
    private Method method;
    private Class<?> eventType;
    private ThreadMode threadMode;
    private int priority;
    private String methodName;
    private Class<?> subscriberClass;

    public SubscribedMethod(Method method, Class<?> eventType, ThreadMode threadMode, int priority) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
    }

    public SubscribedMethod(Class<?> subscriberClass, Class<?> eventType, ThreadMode threadMode, int priority, String methodName) {
        this.subscriberClass = subscriberClass;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
        this.methodName = methodName;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public int getPriority() {
        return priority;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?> getSubscriberClass() {
        return subscriberClass;
    }

    @Override
    public String toString() {
        return "SubscribedMethod{" +
                "method=" + method +
                ", eventType=" + eventType +
                ", threadMode=" + threadMode +
                ", priority=" + priority +
                '}';
    }
}
