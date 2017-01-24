package com.tencent.qrom.wup.test;

import java.util.ArrayList;
import java.util.List;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.iplist.IPListFetcher;
import TRom.EIPType;
import TRom.IPListRsp;
import android.app.Activity;
import android.os.Bundle;

public class IPListFetcherTestActivity extends Activity implements IPListFetcher.IIPListRespCallback {
	private static final String TAG = IPListFetcherTestActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		List<EIPType> eIPTypes = new ArrayList<EIPType>();
		eIPTypes.add(EIPType.WUPPROXY);
//		eIPTypes.add(EIPType.WUPSOCKET);
		
		new IPListFetcher(ConnectInfoManager.get().getConnectInfo()
				, eIPTypes
				, true
				, this
				, RunEnvType.Gamma
				, null).start();
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onIPListResponse(IPListFetcher ipListFetcher, int errorCode, String errorMsg, IPListRsp resp) {
		QRomLog.d(TAG, "onIPListResponse errorCode=" + errorCode + ", errorMsg=" + errorMsg
				 + ", resp=" + resp + ", isAll=" + ipListFetcher.isAll());
	}
}
