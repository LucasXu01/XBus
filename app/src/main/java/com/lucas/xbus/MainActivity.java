package com.lucas.xbus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lucas.annotations.Subscribe;
import com.lucas.annotations.ThreadMode;
import com.lucas.xbus.Event.PriorityEvent;
import com.lucas.xbus.Event.ViewEvent;
import com.lucas.xbus.Event.WorkEvent;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        long startTime = System.currentTimeMillis();
        Log.d(TAG, "onStart: startTime=" + startTime);
        registryByReflect();
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "onStart: endTime=" + endTime);
        Log.d(TAG, "onStart: collect all subscribe method cost " + (endTime - startTime));


        // 测试：发送WorkEvent
        findViewById(R.id.btn_send1).setOnClickListener(v -> XEventBus.getDefault().post(new WorkEvent(5)));

        // 测试：主线程发送ViewEvent
        findViewById(R.id.btn_send2).setOnClickListener(v-> XEventBus.getDefault().post(new ViewEvent("主线程测试文字")));

        // 测试：子线程发送ViewEvent
        findViewById(R.id.btn_send3).setOnClickListener(v-> new Thread() {
            @Override
            public void run() {
                super.run();
                XEventBus.getDefault().post(new ViewEvent("子线程测试文字"));
            }
        }.start());

        // 解注册bus
        findViewById(R.id.btn_send4).setOnClickListener(v-> XEventBus.getDefault().unregister(MainActivity.this));
        // 注册bus
        findViewById(R.id.btn_send5).setOnClickListener(v-> XEventBus.getDefault().register(MainActivity.this));
        // 事件优先级测试
        findViewById(R.id.btn_send6).setOnClickListener(v-> {
            XEventBus.getDefault().post(new PriorityEvent());
        });
    }

    @Subscribe(priority = 1)
    public void onEvent(final WorkEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Thread is " + Thread.currentThread().getName() + " Thread, WorkEvent num=" + event.getNum(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleView(final ViewEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Thread is " + Thread.currentThread().getName() + " Thread, ViewEvent text=" + event.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(priority = 1)
    public void onEventPriority1(final PriorityEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "priority = 1 ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(priority = 2)
    public void onEventPriority2(final PriorityEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "priority = 2 ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(priority = 3)
    public void onEventPriority3(final PriorityEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "priority = 3 ", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 方法一：以反射调用
     */
    public void registryByReflect(){
        XEventBus.getDefault().register(MainActivity.this);
    }

    /**
     * 方法二：APT方式调用
     */
    public void registryByApt(){
        AptMethodFinder aptMethodFinder = new AptMethodFinder();
        //注解处理器代码的模板类
//        AptMethodFinderTemplate aptMethodFinder = new AptMethodFinderTemplate();
        //注解处理调用方式
        XEventBus.builder().setMethodHandle(aptMethodFinder).build().register(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        XEventBus.getDefault().unregister(this);
    }


}
