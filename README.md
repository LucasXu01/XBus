# 一 简介

XBus 是一个轻量级的事件总线库，旨在提供简单、高效、易用的事件通信机制。我们的设计目标如下：

1. 易用性：XBus 的 API 设计简洁明了，易于集成和使用。
2. 高性能：提供过反射和注解处理器（APT）生成订阅者方法索引，后者可提高事件查找和调用的性能。
3. 可扩展性：XEventBus 具有良好的可扩展性，可以根据需求添加更多功能，如优先级控制、延时处理等。

# 二 快速使用：

## 2.1、根build.gradle中添加仓库来源地址

```java
allprojects {
    repositories {
        ...
        maven {
            url 'https://lucasxu01.github.io/maven-repository/'
        }
        
    }
}
```

## 2.2、app项目级别build.gradle中添加依赖

```java
    implementation 'com.lucas:xbus:1.0.0'
    implementation 'com.lucas:xbus-annotations:1.0.0'
    annotationProcessor 'com.lucas:xbus-apt-processor:1.0.0'
```

## 2.3、使用：

### 2.3.1 Antivity的onCreate方法中注册bus：

```java
XEventBus.getDefault().register(MainActivity.this);
```

### 2.3.2 定义一个自己的Event事件：

```java
public class WorkEvent {
    private int num;

    public WorkEvent(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
```

### 2.3.3 对应的Activity中注册方法

```java
    @Subscribe(priority = 1)
    public void onEvent(final WorkEvent event) {
         Log.e(TAG, "onEvent: " + " Thread, WorkEvent num=" + event.getNum());
    }
```

### 2.3.4 发送事件进行调用

```java
XEventBus.getDefault().post(new WorkEvent(5))
```

# 其他功能

若想使用apt方式代替注解，可在bus注册时这样注册：

```java
AptMethodFinder aptMethodFinder = new AptMethodFinder();
XEventBus.builder().setMethodHandle(aptMethodFinder).build().register(this);
```

具体使用可参看demo源码；或者体验demo，请点击此处下载 [demo.apk](apkdemo/demo.apk)