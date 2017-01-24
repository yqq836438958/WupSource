package com.example.wuptest;

import qrom.component.wup.QRomComponentWupManager;
import qrom.component.wup.QRomWupReqExtraData;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.utils.QWupLog;
import TRom.RomBaseInfo;
import android.content.Context;
import android.util.Log;

import com.qq.jce.wup.UniPacket;

public class DemoWupManager extends QRomComponentWupManager {

	public static final String TAG = "====DemoWupManager";
   
    private static DemoWupManager mDemoManager;
    
    /** 测试用模块 */
    public static final int WUP_MODEL_TEST_DEMO = 1;
    /** 测试用wup请求操作 */
    public static final int WUP_OPER_TEST_DEMO = 1;
    
    public DemoWupManager() {
        super();
    }   
    
    
    public static DemoWupManager getInstance() {
        if (mDemoManager == null) {
            mDemoManager = new DemoWupManager();
        }
        
        return mDemoManager;
    }
    
          
    public void startWup(Context context) {
        
        /*
         * 由于wup会监听网络状态，会holder住context
         * 在应用退出时一定要调用release释放资源，（请确定不再使用wup模块时 release）
         * 
         *   release 后若要再使用wup，请重新初始化并调用startup
         *   
         *   建议一个进程中使用一个wupMange 管理wup请求
         * 
         */
        super.startup(context);
        

        Log.i("====", "qua = " + getQua());
    }
    
    @Override
    public void updataBaseInfo(RomBaseInfo baseInfo) {
        
        /*
         * 设置qimei 从统计sdk中获取 
         * 
         * 注意：
         *     1.  获取qimei需要先接入统计, 这里仅用统计接口举例，统计sdk使用方式请参考统计说明
         *     2.   baseInfo对象据是进程公用的wup协议数据， 请勿随意修属性值
         *     3.  接口详细说明，请参考wup sdk使用文档
         */
        // 初次使用统计时，请先初始化，这里仅接口说明
//        baseInfo.sQIMEI = QStatExecutor.getQIMEI();
    }
    
    @Override
    public void onGuidChanged(byte[] vGuid) {
        /*
         * guid发送变化，如：全新安装时获取到guid会回调该方法
         *    -- 若无需要对guid变化敏感的操作，可忽略该回调
         */
    }
    
    public void release() {
        QWupLog.trace(TAG, " release () 释放资源" );
        /*
         * 由于wup会监听网络状态，会holder住context
         * 这里一定要释放资源
         */
        super.release();
    }
   
    
    public void onReceiveAllData(int fromModelType, int reqId, int operType,
    		QRomWupReqExtraData wupReqExtraData, QRomWupRspExtraData rspExtraData, 
    		String serviceName, byte[] response) {
        Log.i(TAG, "test onReceiveAllData -> modeType = " + fromModelType + ", operType = " + operType);
        /*
         * 该接口是处理所有wup返回数据的 （只有reqId > 0 的请求才会回调该接口）
         * -- 只要接收到服务器的正常回包都会回调到这个接口里来
         * 通过 reqId 标识请求的唯一性
         * 
         * 通过fromModelType 和 operType 判断请求是来自哪个模块 
         */        
        
    	switch (fromModelType) {
        case WUP_MODEL_TEST_DEMO:  // 测试模块发起的wup请求返回
            // 这里可以将数据转发给QubeWupTestActivity中的对象进行处理
            // 这里只是为了方便测试所以直接QubeWupTestActivity中的callback处理回包
            // 请根据具体情况处理对应请求
            QubeWupTestActivity.mCallBack.onWupSucess(operType, response);
            break;
        case 5:  // 测试用
//            RomGetAccountRsp getAccountRsp= (RomGetAccountRsp) QRomWupDataBuilder.parseWupResponseByFlgV3(
//                    response, "stRsp", new RomGetAccountRsp());
//            Log.d(TAG, "getAccountRsp = " + getAccountRsp);
            break;
        default:   
            // TODO
            break;
        }

    }
 
    
    public void onReceiveError(int fromModelType, int reqId, int operType,
    		QRomWupReqExtraData wupReqExtraData, QRomWupRspExtraData wupRspExtraData,
    		String serviceName, int errorCode, String description) {
        
        Log.i(TAG, "test onReceiveError -> modeType = " + fromModelType + ", operType = " + operType 
                + ", errcode = " + errorCode + ", errMsg = " + description);
        
        /*
         * 
         * 处理wup失败的请求 （只有reqId > 0 的请求才会回调该接口）
         * 
         */
        
    	switch (fromModelType) {
        case WUP_MODEL_TEST_DEMO:  // 测试发起的wup请求返回
            break;
        default:  // 传递其他进程wup数据
            // TODO
            break;
        }
    	
    }
    
    
    /**
     *   发送wup请求
     *   
     * @param fromModelType  请求的模块标识 
     * @param operType            请求操作标识
     * @param reqPacket           请求数据
     * @return  int                      <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     *                                          （仅当reqId > 0的情况才会有成功或失败的回调）
     */
    public int requestWupNoRetry(int fromModelType, int operType,
            UniPacket reqPacket) {
        return super.requestWupNoRetry(fromModelType, operType, reqPacket);
    }
    
    
    @Override
    public int requestWupNoRetry(int fromModelType, int operType,
            UniPacket reqPacket, QRomWupReqExtraData extraData, long timeout) {
                
        return super.requestWupNoRetry(fromModelType, operType, reqPacket, extraData,
                timeout);
    }
    
    
}
