package com.lucas.xbus;

import com.lucas.annotations.MethodHandle;
import com.lucas.annotations.SubscribedMethod;
import com.lucas.annotations.Subscription;
import com.lucas.annotations.ThreadMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 此为APT生成的模板
// 以此为参考写生成代码，实际中不需要关注此类
public class AptMethodFinderTemplate implements MethodHandle {

    private static final Map<Object, List<SubscribedMethod>> aptMap = new HashMap<>();

    static {
        aptMap.put(com.lucas.xbus.MainActivity.class, findMethodsInMainActivity());
    }

    @Override
    public List<SubscribedMethod> getAllSubscribedMethods(Object subscriber) {
        return aptMap.get(subscriber);
    }

    @Override
    public void invokeMethod(Subscription subscription, Object event) {

    }

    private static List<SubscribedMethod> findMethodsInMainActivity(){
        List<SubscribedMethod> subscribedMethods = new ArrayList<>();
        subscribedMethods.add(new SubscribedMethod(com.lucas.xbus.MainActivity.class, com.lucas.xbus.Event.WorkEvent.class, ThreadMode.POSTING, 0, "onEvent"));
        subscribedMethods.add(new SubscribedMethod(com.lucas.xbus.MainActivity.class, com.lucas.xbus.Event.ViewEvent.class, ThreadMode.MAIN, 0, "handleView"));
        return subscribedMethods;
    }
}
