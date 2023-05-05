package com.lucas.annotations;


import java.util.List;

public interface MethodHandle {

    public List<SubscribedMethod> getAllSubscribedMethods(Object subscriber);

    public void invokeMethod(Subscription subscription, Object event);

}
