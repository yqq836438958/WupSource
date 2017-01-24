package com.tencent.qrom.wup.test;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomQuaFactory;
import qrom.component.wup.apiv2.AsyncWupOption;
import qrom.component.wup.apiv2.IGuidListener;
import qrom.component.wup.apiv2.OutWrapper;
import qrom.component.wup.apiv2.WupException;
import qrom.component.wup.apiv2.WupInterface;
import qrom.component.wup.apiv2.WupOption;
import qrom.component.wup.apiv2.WupOption.WupType;
import qrom.component.wup.base.RunEnvType;
import TRom.GetKobeReq;
import TRom.GetKobeRsp;
import TRom.KobeshyTestStubAndroid;
import TRom.KobeshyTestStubAndroid.GetKobeResult;
import TRom.KobeshyTestStubAndroid.IGetKobeCallback;
import android.app.Activity;
import android.os.Bundle;

public class APIV2TestActivity extends Activity  implements IGuidListener {
	private static final String TAG = APIV2TestActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WupInterface.registerGuidListener(this);
//		
		
		
		QRomLog.d(TAG, "GUID=" + WupInterface.getGuidStr());
		
		QRomLog.d(TAG, "QUA=" + QRomQuaFactory.buildQua(getApplicationContext()));
		QRomLog.d(TAG, "Sys QUA=" + QRomQuaFactory.buildQuaForSysRom(getApplicationContext()));
		
//		asyncTest();
		
//		LogTicketProxy proxy = new LogTicketProxy();
//		try {
//			proxy.asyncGetTicket(new LogTicketProxyCallback() {
//
//				@Override
//				public void onTicketCallback(RunEnvType envType, GetTicketResult result) {
//					QRomLog.d(TAG, "envType=" + envType 
//							+ ", errorCode=" + result.getErrorCode() 
//							+ ", ret=" + result.getRet() + ", rsp=" + result.getRsp());
//				}
//
//				
//			});
//		} catch (WupException e) {
//			e.printStackTrace();
//		}
	}
	
	static int sCount = 0;
	
	public void asyncTest() {
		QRomLog.d("SpeedTest", "start time=" + System.currentTimeMillis());
		
		for (int threadIndex = 0; threadIndex < 10; ++threadIndex) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int index = 0; index < 1000; ++index) {
					GetKobeReq reqData = new GetKobeReq(1, "kobetest");
		
					AsyncWupOption option = new AsyncWupOption(WupType.WUP_ASYM_ENCRYPT_REQUEST, null);
			
					option.setCharset(WupOption.CHARSET_UTF8);  // 编码和解码字符， 默认UTF8
					option.setIsUseUniPacketV3(false);  // 采用UniPacket的V3编码， 不建议设置
//					option.setRetryTimes(1);  // 协议调用出错后，重试的次数， 默认不重试
					option.setTimeoutMs(30000); // 协议调用读超时时间, 默认60s
					
					option.setRequestEnvType(RunEnvType.IDC);
//					int random = new Random().nextInt();
//					if (index % 3 == 0) {
//						option.setRequestEnvType(RunEnvType.IDC); // 主动设置请求环境，一般不需要设置
//					} else if(index % 3 == 1){
//						option.setRequestEnvType(RunEnvType.Gamma);
//					} 
					option.setRequestPkgInfo("com.tencent.stat.test.app");
			
					try {
						new KobeshyTestStubAndroid("kobeTest").asyncGetKobe(reqData, new IGetKobeCallback() {
				
							@Override
							public void onGetKobeCallback(GetKobeResult result) {
								int returnsCount = 0;
								synchronized(this) {
									returnsCount = ++sCount;
								}
								QRomLog.d(TAG, "onGetKobeCallback result errorCode=" + result.getErrorCode() 
										+ ", requestId=" + result.getRequestId());
								QRomLog.d("SpeedTest", "onGetKobeCallback reqId=" + result.getRequestId()
										+ ", time=" + System.currentTimeMillis() + ", returnsCount=" + returnsCount);
								if (result.getErrorCode() == 0) {
									// 协议调用成功
									if (result.getRet() == 0) {
										// 后台返回码为0， 说明执行正确
										GetKobeRsp rsp = result.getRsp();
										QRomLog.d(TAG, "onGetKobeCallback rsp=" +  rsp.getSInfos());
									} else {
										QRomLog.d(TAG, "onGetKobeCallback server ret=" + result.getRet());
									}
							
								} else {
									QRomLog.d(TAG, "onGetKobeCallback wup failed!");
								}
							}
						}, option);
					} catch (WupException e) {
						e.printStackTrace();
					}
			
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		}
	}
	
	public void syncTest() {
		GetKobeReq reqData = new GetKobeReq(1, "kobetest");
		OutWrapper<TRom.GetKobeRsp> rspData = new OutWrapper<TRom.GetKobeRsp>();
		
		WupOption option = new WupOption(WupType.WUP_ASYM_ENCRYPT_REQUEST);
		option.setCharset(WupOption.CHARSET_UTF8);  // 编码和解码字符， 默认UTF8
		option.setIsUseUniPacketV3(true);  // 采用UniPacket的V3编码， 不建议设置
		option.setRetryTimes(1);  // 协议调用出错后，重试的次数， 默认不重试
		option.setTimeoutMs(30000); // 协议调用读超时时间, 默认60s
		option.setRequestEnvType(RunEnvType.IDC); // 主动设置请求环境，一般不需要设置
		
		try {
			int ret = new KobeshyTestStubAndroid("kobeTest").getKobe(reqData, rspData);
			QRomLog.d(TAG, "getKobe rsp=" +  rspData.getOut() + ", ret=" + ret);
		} catch (WupException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void onDestroy() {
		WupInterface.unRegisterGuidListener(this);
		
		super.onDestroy();
	}

	@Override
	public void onGuidChanged(byte[] vGuid) {
		QRomLog.d(TAG, "onGuidChanged GUID=" + WupInterface.getGuidStr());
	}
}
