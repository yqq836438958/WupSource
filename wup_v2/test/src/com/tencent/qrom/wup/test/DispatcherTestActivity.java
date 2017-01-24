package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.core.DispatcherFactory;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Request.RequestType;
import qrom.component.wup.framework.Response;
import qrom.component.wup.framework.core.IRequestCallback;

import com.qq.jce.wup.UniPacket;

import TRom.GetKobeReq;
import TRom.GetKobeRsp;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class DispatcherTestActivity extends Activity implements ServiceConnection {
	private static final String TAG = DispatcherTestActivity.class.getSimpleName();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		testSingleRequest();
	}
	
	private int mTotalCount = 0;
	private int mSendTotalCount = 0;
	
	public void testSingleRequest() {
		int id = 1;
		String name = "kobe";
		GetKobeReq reqData = new GetKobeReq(id, name);
		
		UniPacket packet = QRomWupDataBuilder.createReqUnipackageV3("kobeTest", "getKobe", "req", reqData);
		
		Request request = new Request(packet, RequestType.NORMAL_REQUEST);
//		request.getRequestOption().setRetryTimes(3);
		
		long requestId = DispatcherFactory.getDefault().send(request,
				new IRequestCallback() {

					@Override
					public void onRequestFinished(long requestId,
							Request request, Response response) {
						++mTotalCount;
						QRomLog.d(TAG, "onRequestFinished " + mTotalCount + " finished!  requestId=" + requestId);
						if (response.getErrorCode() == 0) {

							GetKobeRsp rsp = new GetKobeRsp();
							rsp = (GetKobeRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(
											response.getResponseContent(),
											"rsp", rsp);
							if (null != rsp) {
								QRomLog.d(TAG, "onRequestFinished, sInfos=" + rsp.getSInfos());
							} else {
								QRomLog.d(TAG, "onRequestFinished parse unipacket failed!");
							}
							
						} else {
							QRomLog.d(TAG, "onRequestFinished error, errorCode="
											+ response.getErrorCode()
											+ ", errorMsg="
											+ response.getErrorMsg());
						}
					}
				});
		QRomLog.d(TAG, "send requestId=" + requestId);
	}
	
	
	public void testMultiRequest() {
		int id = 1;
		String name = "kobe";
		GetKobeReq reqData = new GetKobeReq(id, name);
		
		UniPacket packet = QRomWupDataBuilder.createReqUnipackageV3("kobeTest", "getKobe", "req", reqData);
		
		Request request = new Request(packet, RequestType.NORMAL_REQUEST);
		
		for (int index = 0; index < 10000; ++index) {

			long requestId = DispatcherFactory.getDefault().send(request,
					new IRequestCallback() {

						@Override
						public void onRequestFinished(long requestId,
								Request request, Response response) {
							++mTotalCount;
							QRomLog.d(TAG, "onRequestFinished " + mTotalCount + " finished!"  + ", sendTotalCount=" + mSendTotalCount
									 + ", requestId=" + requestId);
							if (response.getErrorCode() == 0) {

								GetKobeRsp rsp = new GetKobeRsp();
								rsp = (GetKobeRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(
												response.getResponseContent(),
												"rsp", rsp);
								if (null != rsp) {
//									QRomLog.d(TAG, "onRequestFinished, sInfos=" + rsp.getSInfos());
								} else {
//									QRomLog.d(TAG, "onRequestFinished parse unipacket failed!");
								}
								
							} else {
								QRomLog.d(TAG, "onRequestFinished error, errorCode="
												+ response.getErrorCode()
												+ ", errorMsg="
												+ response.getErrorMsg());
							}
						}
					});
			QRomLog.d(TAG, "send requestId=" + requestId);
			if (requestId > 0) {
				++mSendTotalCount;
			}
		}
	}


	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
	}


	@Override
	public void onServiceDisconnected(ComponentName name) {
	}
}
