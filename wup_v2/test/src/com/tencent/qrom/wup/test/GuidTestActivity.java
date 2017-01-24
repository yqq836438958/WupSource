package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.guid.GuidProxy;
import android.app.Activity;
import android.os.Bundle;

public class GuidTestActivity extends Activity {
	private static final String TAG = GuidTestActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		QRomLog.d(TAG, "guid=" + StringUtil.byteToHexString(GuidProxy.get().getGuidBytes()));
	}
}
