package com.example.wuptest;

import java.util.List;

import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.QRomWupEnvironment;
import qrom.component.wup.aidl.IQRomWupService;
import qrom.component.wup.sysImpl.QWupRomSysProxyerImpl;
import qrom.component.wup.utils.QWupStringUtil;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.wuptest.QubeWupTestActivity.WupCallBack;
import com.tencent.qubewupbasis.R;


/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.example.HelloJni.HelloJniTest \
 * com.example.HelloJni.tests/android.test.InstrumentationTestRunner
 */
public class QubeWupTestActivity extends Activity implements ServiceConnection{
    
    private static String TAG = "====";
    /*
     * 这里会holder住context，
     * 尽量传入app的context
     */
    DemoWupManager testDemo = null;
    
    boolean mIsBinder = false;
    
    IQRomWupService mRomWupService;
    
    static WupCallBack mCallBack;
    
    EditText editText;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.tencent.qubewupbasis.R.layout.testwup_main);
//		startService(new Intent(this, TestService.class));
		mCallBack = new WupCallBack();
		// 绑定service
//		bindTestService();
		
		
		byte[] guid = getWupManager().getGUIDBytes();
		Log.i(TAG, "guid = " + QRomWupDataBuilder.byteToHexString(guid));
		editText = (EditText) findViewById(R.id.editText);
		// 测试同步发送wup请求接口
		testWupSynReq();
		
		Test.testReqWup();
		
//		Log.i("====", "qua = " + QRomQuaFactory.buildQua(this) + ", sdk int = " + Build.VERSION.SDK_INT);
	}
	
	private void testWupSynReq() {
	 
	    Button buttonMain = (Button) findViewById(R.id.button1);
	    buttonMain.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {

//                UniPacket pkt = QubeWupDataBuidler.createLoginReqData(getWupManager().getUserInfo());
//                getWupManager().sendSynWupRequest(pkt);
                
//                getWupManager().requestGuid();
                getWupManager().doLogin();
                byte[] guid = getWupManager().getGUIDBytes();
                Log.i("====", "guid = " + QWupStringUtil.byteToHexString(guid));
//                long romId = getWupManager().getQRomId();
//                Log.i("====", "romId = " + romId);
                Log.i("====", "BRAND = " + Build.BRAND);             
                List<String> iplist = getWupManager().getCurApnProxyList();
                Log.i("====", "iplist = " + iplist);  
                
                bindTestService();
                // 异步发送加密请求
//                RomGetAccountReq getAccountReq = new RomGetAccountReq();
//                getAccountReq.uiRomId = 100028;
//                final UniPacket packet = QRomWupDataBuilder.createReqUnipackageV3("romaccount", "getAccountInfo", "stReq", getAccountReq);
////                int reqId = getWupManager().requestAsymEncryptWup(5, 5, packet);
////                Log.w("====", "requestAsymEncryptWup -> reqid = " + reqId);
//                
//                new Thread() {
//                    public void run() {
//                        byte[] rsp = getWupManager().sendSynAsymEncryptRequest(packet, TestConstants.PACKAGE_NAME);
//                        RomGetAccountRsp getAccountRsp= (RomGetAccountRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(
//                                rsp, "stRsp", new RomGetAccountRsp());
//                        Log.d("====", "getAccountRsp = " + getAccountRsp);
//                    };
//                }.start();
                
            }
        });
	    
	       Button buttonThread = (Button) findViewById(R.id.button2);
	       buttonThread.setOnClickListener(new OnClickListener() {
	            
	            @Override
	            public void onClick(View v) {
//	                getWupManager().requestIpList();
//	                List<String> info = getWupManager().getCurApnProxyList();
//	                Log.i("====", "ip info = " + info);
//	                String qua = QRomQuaFactory.buildQuaForSysRom(getApplicationContext());
//	                Log.i("====", "sys qua info = " + qua);
//	                int reqid = getWupManager().requestIpList();
//	                Log.i("====", "requestIpList = " + reqid);
//	                testReqWup();
//	                Intent intent = new Intent(TestConstants.PACKAGE_NAME+ ".qrom.intent.action.wup.logsdk.getLogTicket");
//	                // 请求的包名
//	                intent.putExtra("app_pkgName", TestConstants.PACKAGE_NAME);
//                    // 请求超时时间
//                    intent.putExtra("ticket_timeout", 30000);
//                    // 请求的id
//                    intent.putExtra("report_resId", 5);
//	                sendBroadcast(intent);
	            	QRomWupEnvironment.getInstance(QubeWupTestActivity.this).setCloseAll(
	            			!QRomWupEnvironment.getInstance(QubeWupTestActivity.this).isAllClosed());
	            }
	        });
	}

//    private String getSysProp(String cmd) throws Exception{
//        
//        java.lang.Process process   = Runtime.getRuntime().exec(cmd);
//        InputStreamReader inputStreamReader = new   InputStreamReader(process.getInputStream()); 
//        char[] buf = new char[3];
//        int readLen = 0;
//        StringBuilder strb = new StringBuilder();;
//        while ((readLen = inputStreamReader.read(buf)) != -1) {
//            strb.append(buf, 0, readLen);
//        }
//      
//       return strb.toString();
//    }
//    
    public int WUP_DEMO_TEST = 0;
        
//    private void test() {
//        int modeType = DemoWupManager.WUP_MODEL_TEST_DEMO;
//        int operType = DemoWupManager.WUP_OPER_TEST_DEMO;
//        UniPacket packet = getWupDemoData();
//        
//        int reqId = getWupManager().requestWupNoRetry(modeType, operType, packet);
//        if (reqId > 0) {
//            Log.d("TEST", "demo request is senging, reqId = " + reqId);
//        }
//    }
	
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
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    unBindTestServic();
	    
	    if (testDemo != null) {
	        testDemo.release();
	        testDemo = null;
	    }
	    
	}
	
	private void bindTestService() {
	    if (!mIsBinder) {	        
	        Intent service = new Intent(this, TestService.class);
	        bindService(service, this, Context.BIND_AUTO_CREATE);
	        mIsBinder = true;
	    }
	}
	
	private void unBindTestServic() {
	 
	    if (mIsBinder) {	        
	        mRomWupService = null;
	        unbindService(this);
	        mIsBinder = false;
	    }
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
	    mRomWupService = IQRomWupService.Stub.asInterface(service);
	    // 
	    QWupRomSysProxyerImpl.getInstance().setTestProxyWupService(mRomWupService);
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
	    
	}
	
    
    public class WupCallBack {
        
        public void onWupSucess(int operType, byte[] rspData) {
            /*
             * 这里通过返回的operType区分是哪个请求的数据
             * 
             */
            if (operType == DemoWupManager.WUP_OPER_TEST_DEMO) {  // 测试用wup请求
                // 解包 rspData
                // ... 将rspData 解析成unipackage 转换成所需要的数据
            }
        }
    }
    
	
}
