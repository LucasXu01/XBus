package com.lucas.xbus.invoke_strategy;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.lucas.annotations.MethodHandle;

public abstract class MethodInvokeStrategy implements MethodHandle {
    private static final String TAG = "MethodInvokeStrategy";

    private static HandlerThread handlerThread = new HandlerThread("workThread");

    protected static Handler mainHandler;

    protected static Handler workHander;

    public MethodInvokeStrategy() {
        handlerThread.start();
        mainHandler = new Handler(Looper.getMainLooper());
        workHander = new Handler(handlerThread.getLooper());
    }
}
