package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.apiv2.RomBaseInfoBuilder;
import qrom.component.wup.apiv2.WupInterface;
import qrom.component.wup.base.RunEnv;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.guid.GuidProxy;
import qrom.component.wup.iplist.IPListClientByProvider;
import qrom.component.wup.iplist.SelectedIPPortResult;
import TRom.EIPType;
import TRom.E_ROM_DEVICE_TYPE;
import TRom.RomBaseInfo;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	
//	private IPListClientByProvider mIPListClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		QRomLog.d(TAG, "RunEnv=" + RunEnv.get().getEnvType());
		
		QRomLog.d(TAG, "GUID is " + WupInterface.getGuidStr());
		
		RomBaseInfo romBaseInfoCommon = new RomBaseInfoBuilder().build();
		
		RomBaseInfo romBaseInfoSelf = new RomBaseInfoBuilder() {
			@Override
			public void doBuild(RomBaseInfo romBaseInfo) {
				romBaseInfo.setEExtDataType(E_ROM_DEVICE_TYPE._ERDT_WATCH);
			}
		}.build();
		
//		mIPListClient = new IPListClientByProvider("com.tencent.qrom.tms.tcm");
//		
//		SelectedIPPortResult result = mIPListClient.selectIPPort(RunEnvType.IDC, EIPType.WUPPROXY
//				, ConnectInfoManager.get().getConnectInfo());
//		if (result == null) {
//			QRomLog.d(TAG, "selectIPPort result is null");
//		} else {
//			QRomLog.d(TAG, "node=" + result.getNode() + ", apnIndex=" + result.getApnIndex());
//		}
		QRomLog.d(TAG, "romBaseInfoCommon=" + romBaseInfoCommon + ", romBaseInfoSelf=" + romBaseInfoSelf);
	}
	
	@Override
	protected void onDestroy() {
//		mIPListClient = null;
		super.onDestroy();
	}
	
	

}
