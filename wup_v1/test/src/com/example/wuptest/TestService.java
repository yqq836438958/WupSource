package com.example.wuptest;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupEnvironment;
import qrom.component.wup.QRomWupEnvironment.SwitchWorkMode;
import qrom.component.wup.aidl.QRomWupService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

public class TestService extends Service{
    
    private String TAG = "TestService";
    private QRomWupService mQRomWupService = new QRomWupService();


    @Override
    public void onCreate() {
        super.onCreate();
        
        QRomLog.i(TAG, "===onCreate===");
        String guidStr = getWupManager().getGUIDStr();
        Test.testReqWup();
        Log.i("====", "onCreate -> pid = " + Process.myPid() + ", guid = " + guidStr);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        QRomLog.i(TAG, "===onDestroy===");
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        String guidStr = getWupManager().getGUIDStr();
        
        Log.i("====", "onStart -> pid = " + Process.myPid() + ", guid = " + guidStr);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mQRomWupService;
    }
    
    DemoWupManager testDemo = null;
    public DemoWupManager getWupManager() {

        /**
         * wup模块需要一个回调，启动wup最好用一个单例对象管理所有请求
         * 
         * 非特殊情况下，不要初始化2个wup对象
         */
        if (testDemo == null) {
            testDemo = DemoWupManager.getInstance();
            // startup一次就可以
            testDemo.startWup(getApplicationContext());         
        }
        return testDemo;        
    }
}
