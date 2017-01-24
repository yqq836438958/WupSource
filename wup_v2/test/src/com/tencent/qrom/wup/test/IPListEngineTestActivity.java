package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.iplist.ApnIndexConvertor;
import qrom.component.wup.iplist.IPListEngine;
import TRom.EIPType;
import android.app.Activity;
import android.os.Bundle;

public class IPListEngineTestActivity extends Activity {
	private static final String TAG = IPListEngineTestActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		IPListEngine.startUp();
		
//		IPListEngine.get().updateIPList(RunEnvType.IDC, true);
		
		QRomLog.d(TAG,
				"selectIPPort=" + 
				IPListEngine.get().selectIPPort(RunEnvType.IDC, EIPType.WUPPROXY
						, ApnIndexConvertor.getApnIndex(ConnectInfoManager.get().getConnectInfo())));
	}
}
