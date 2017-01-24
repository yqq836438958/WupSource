package com.example.wuptest;

import java.util.ArrayList;

import qrom.component.wup.runInfo.WupAppProtocolBuilder;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QRomWupDataBuilderImpl;
import qrom.component.wup.utils.QWupLog;
import TRom.EIPType;

import com.qq.jce.wup.UniPacket;

public class Test {
	private static final String TAG = "wup_test";
	public static void testReqWup() {
        // 设置请求类型
        ArrayList<Integer> typeList = new ArrayList<Integer>(2);
        typeList.add(EIPType._WUPPROXY);
        
        boolean bAll = false;
    
        // 当前网络类型
        int apnType = QRomWupDataBuilderImpl.getApnType(ApnStatInfo.getCurApnProxyIndex());
        int netType = QRomWupDataBuilderImpl.getNetTypeOfService(ApnStatInfo.getNetType());
        
        UniPacket packet = WupAppProtocolBuilder.createIpListReqData(DemoWupManager.getInstance().getGUIDBytes(), 
                typeList, apnType, netType, bAll);
        
        int reqId = DemoWupManager.getInstance().requestWupNoRetry(10, DemoWupManager.WUP_OPER_TEST_DEMO, packet);

        QWupLog.i(TAG, "testReqWup-> reqId = " + reqId);
	}
}
