package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.transport.http.DefaultHttpSessionPool;
import qrom.component.wup.transport.http.HttpAddr;
import qrom.component.wup.transport.http.HttpClientSession;
import qrom.component.wup.transport.http.HttpRouteInfo;
import qrom.component.wup.transport.http.HttpSession;
import qrom.component.wup.transport.http.HttpSession.SessionRequest;
import qrom.component.wup.transport.http.HttpSession.SessionResponse;
import qrom.component.wup.transport.http.IHttpRouteChooser;
import qrom.component.wup.transport.http.IHttpSessionPool;
import TRom.GetKobeReq;
import TRom.GetKobeRsp;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.qq.jce.wup.UniPacket;

public class HttpSessionTestActivity extends Activity implements IHttpRouteChooser {
	private static final String TAG = HttpSessionTestActivity.class.getSimpleName();
	
	IHttpSessionPool mSessionPool;
	ConnectInfoManager mConnectInfoManager;
	
	Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSessionPool = new DefaultHttpSessionPool();
		mHandler = new Handler();
		
		testHttpSessionOne();
	}
	
	private void testHttpSessionOne() {
		
		int id = 1;
		String name = "kobe";
		GetKobeReq reqData = new GetKobeReq(id, name);
		
		UniPacket packet = QRomWupDataBuilder.createReqUnipackageV3("kobeTest", "getKobe", "req", reqData);
		
		SessionRequest request = new SessionRequest();
		request.setGuid("1284cf7026883f6ffc25bda2b2a13d4f");
		request.setSessionTimeout(60*1000);
		request.setPostData(packet.encode());
		
		HttpSession httpSession = new HttpClientSession(1, request, this, new HttpSession.ICallback() {
			
			@Override
			public void onSessionResponse(HttpSession httpSession,
					SessionResponse sessionResponse) {
				if (sessionResponse.getErrorCode() == 0) {
					GetKobeRsp rsp = new GetKobeRsp();
					rsp = (GetKobeRsp)QRomWupDataBuilder.parseWupResponseByFlgV3(
							sessionResponse.getRespData().getResponseContent(), "rsp", rsp);
					if (null != rsp) {
						QRomLog.d(TAG, "onSessionResponse, sInfos=" + rsp.getSInfos());
					} else {
						QRomLog.d(TAG, "onSessionResponse parse unipacket failed!");
					}
					
				} else {
					QRomLog.d(TAG, "onSessionResponse error, errorCode="
								+ sessionResponse.getErrorCode() + ", errorMsg=" + sessionResponse.getErrorMsg());
				}
				
			}
		});
		
		mSessionPool.postHttpSession(httpSession);
	}
	
	@Override
	public void onDestroy() {
		mSessionPool.destroy();
		
		super.onDestroy();
	}

	@Override
	public HttpRouteInfo selectRouteInfo() {
		return new HttpRouteInfo(mConnectInfoManager.getConnectInfo()
				, new HttpAddr("w.html5.qq.com", 8080),  RunEnvType.IDC, this);
	}

	@Override
	public void reportNetworkError(HttpRouteInfo routeInfo, int errorCode) {
	}
}
