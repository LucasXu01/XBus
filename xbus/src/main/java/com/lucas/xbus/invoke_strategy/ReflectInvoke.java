package com.lucas.xbus.invoke_strategy;

import android.util.Log;
import com.lucas.annotations.Subscribe;
import com.lucas.annotations.SubscribedMethod;
import com.lucas.annotations.Subscription;
import com.lucas.annotations.ThreadMode;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectInvoke extends MethodInvokeStrategy {
    private static final String TAG = "ReflectInvoke";
    /**
     * 获取这个订阅者类中所有的带{@link Subscribe}的方法
     *
     * @param subscriber 订阅者类，即通过register将this参数传过来的类，可以是activity、service、fragment、thread等。
     */
    @Override
    public List<SubscribedMethod> getAllSubscribedMethods(Object subscriber) {
        //记录订阅者方法参数
        List<SubscribedMethod> subscribedMethods = new ArrayList<>();
        Class<?> aClass = subscriber.getClass();
        //获取所有方法
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                if (parameterTypes.length > 1) {
                    throw new IllegalArgumentException("参数不能为空，且只能有一个参数");
                }
                Class<?> parameterType = parameterTypes[0];
                Log.d(TAG, "getAllSubscribedMethods: parameterType=" + parameterType.getName());
                Subscribe annotation = declaredMethod.getAnnotation(Subscribe.class);
                int priority = annotation.priority();
                ThreadMode threadMode = annotation.threadMode();
                SubscribedMethod subscribedMethod = new SubscribedMethod(declaredMethod, parameterType, threadMode, priority);
                Log.d(TAG, "getAllSubscribedMethods: subscribedMethod=" + subscribedMethod);
                subscribedMethods.add(subscribedMethod);
            }
        }
        return subscribedMethods;
    }

    @Override
    public void invokeMethod(Subscription subscription, final Object event) {
        final Object subscriber = subscription.getSubscriber();
        SubscribedMethod subscribedMethod = subscription.getSubscribedMethod();
        final Method method = subscribedMethod.getMethod();
        switch (subscribedMethod.getThreadMode()) {
            case POSTING:
                Log.d(TAG, "invokeMethod: ThreadMode=POSTING");
                invoke(method, subscriber, event);
                break;
            case MAIN:
                Log.d(TAG, "invokeMethod: ThreadMode=MAIN");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        invoke(method, subscriber, event);
                    }
                });
                break;
            case BACKGROUD:
                Log.d(TAG, "invokeMethod: ThreadMode=BACKGROUND");
                workHander.post(new Runnable() {
                    @Override
                    public void run() {
                        invoke(method, subscriber, event);
                    }
                });
                break;
        }

    }

    private void invoke(Method method, Object subscriber, Object event) {
        try {
            method.invoke(subscriber, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
