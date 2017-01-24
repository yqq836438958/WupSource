package com.example.wuptest;

import qrom.component.wup.QRomWupEnvironment;
import qrom.component.wup.QRomWupEnvironment.SwitchWorkMode;
import android.app.Application;

public class QubeWupTestApp extends Application {
    private static QubeWupTestApp sInstance = null;
    

    public static QubeWupTestApp getInstance() {
        return sInstance;
    }
    
    @Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	super.onCreate();
//    	QRomWupEnvironment.setSwitchWorkMode(SwitchWorkMode.WorkModeFile);
    	sInstance = this;
    }
    
}