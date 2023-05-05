package com.lucas.xbus;

import com.lucas.annotations.MethodHandle;

public class XBusBuilder {
    MethodHandle methodHandle;

    public XBusBuilder setMethodHandle(MethodHandle aptInvoke){
        this.methodHandle = aptInvoke;
        return this;
    }

    public XEventBus build(){
        XEventBus xEventBus = new XEventBus(this);
        if(XEventBus.instance == null){
            XEventBus.instance = xEventBus;
        }
        return xEventBus;
    }
}
