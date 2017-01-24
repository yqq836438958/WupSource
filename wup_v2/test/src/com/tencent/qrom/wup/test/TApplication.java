package com.tencent.qrom.wup.test;

import qrom.component.wup.base.ContextHolder;
import android.app.Application;
import android.content.Context;

public class TApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ContextHolder.setApplicationContext(this);
	}
	
	@Override
	public void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		
	}
}
