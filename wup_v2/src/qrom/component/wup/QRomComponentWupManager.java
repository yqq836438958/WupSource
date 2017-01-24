package qrom.component.wup;

import java.util.List;
import java.util.concurrent.Semaphore;

import qrom.component.wup.apiv2.IGuidListener;
import qrom.component.wup.apiv2.RomBaseInfoBuilder;
import qrom.component.wup.apiv2.WupInterface;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.RunEnv;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.event.Subscriber;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.BaseInfoManager;
import qrom.component.wup.core.DispatcherFactory;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Request.RequestType;
import qrom.component.wup.framework.Response;
import qrom.component.wup.framework.core.IRequestCallback;
import qrom.component.wup.guid.GuidProxy;
import qrom.component.wup.guid.GuidUpdateEvent;
import qrom.component.wup.iplist.IPListProxy;
import qrom.component.wup.iplist.SelectedIPPortResult;
import qrom.component.wup.threads.TempThreadPool;
import TRom.EIPType;
import TRom.RomBaseInfo;

import android.R.bool;
import android.content.Context;

import com.qq.jce.wup.UniPacket;

/**
 * 个组件使用wup的接口类
 * @author sukeyli
 *
 */
@Deprecated
public abstract class QRomComponentWupManager implements IGuidListener {
    
    protected String TAG = "QRomComponentWupManager";
    /** 处理wup请求的对象 */
    
    public QRomComponentWupManager() {
    }
    
    /**
     * 加载wup附属配置
     *    -- 如：切换测试环境配置文件
     * @param context
     * @return
     */
    public boolean loadWupEtcInfo(Context context) {
    	return false;
    }
    
    /**
     * 获取配置文件中环境变量
     * @return int wup运行环境变量,1:测试环境 0正式环境
     */
    public int getWupEtcWupEnviFlg() {
    	return RunEnv.get().getNetEnvValue();
    }
    
    /**
     * 启动wup模块（走rom方式，通过push模块获取guid及iplist）
     *   
     * @param context
     * 
     */
    public synchronized void startup(Context context) {
    	startup(context, 0);
    	
    	WupInterface.registerGuidListener(this);
    }
    /**
     * 启动wup模块制定功能  
     *   
     * @param context
     * @param startMode   QubeWupConstants.RUN_MODE_ROM_DEFAULT/RUN_MODE_APP_ALL等
     * 
     */
    public synchronized void startup(Context context, int startMode) {
        ContextHolder.setApplicationContext(context);
        
        
    }
    
    /**
     * 设置QRomId （romId请从tsf登录sdk中获取设置）
     * @param romId
     */
//    public void setQRomId(long romId) {
//        mWupBaseManager.setQRomId(romId);
//    }
    
    // 异步请求的回调实现
    private class AsyncRequestCallback implements IRequestCallback {
    	private int mFromModelType;
    	private int mOperType;
    	private QRomWupReqExtraData mReqExtraData;
    	
    	public AsyncRequestCallback(int fromModelType, int operType
    			, QRomWupReqExtraData reqExtraData) {
    		mFromModelType = fromModelType;
    		mOperType = operType;
    		mReqExtraData = reqExtraData;
    	}
    	
		@Override
		public void onRequestFinished(final long requestId, final Request request, final Response response) {
			TempThreadPool.get().post(new Runnable() {
				@Override
				public void run() {
					QRomWupRspExtraData rspExtraData = new QRomWupRspExtraData();
					if (response.getErrorCode() == 0) {
						onReceiveAllData(mFromModelType, (int)requestId, mOperType, mReqExtraData
								, rspExtraData, request.getServiceName(), response.getResponseContent());
					} else {
						onReceiveError(mFromModelType, (int)requestId, mOperType, mReqExtraData
								, rspExtraData, request.getServiceName(), response.getErrorCode(), response.getErrorMsg());
					}
				}
				
			});
		}
    }
    
    // 同步请求的回调实现
    private class SyncRequestCallback implements IRequestCallback {
    	
    	private Semaphore mSemaphore;
    	private byte[] mResponseData;
    	
    	public SyncRequestCallback() {
    		mSemaphore = new Semaphore(0);
    	}
    	
		@Override
		public void onRequestFinished(long requestId, Request request,
				Response response) {
			if (response.getErrorCode() == 0) {
				mResponseData = response.getResponseContent();
			}
			mSemaphore.release();
		}
		
		public void waitResponse() {
			try {
				mSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public byte[] getResponseData() {
			return mResponseData;
		}
    	
    }
            
    /**
     *   发送wup请求
     *   
     * @param fromModelType  请求的模块标识
     * @param operType            请求操作标识
     * @param reqPacket           请求数据
     * @return  int                      <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     */
    public int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket) {
    	return requestWupNoRetry(fromModelType, operType, reqPacket, 0);
    }
    
    /**
     * 
     *   发送wup请求
     *   
     * @param fromModelType  请求的模块标识
     * @param operType            请求操作标识
     * @param reqPacket           请求数据
     * @param timeout              请求超时时间（单位毫秒）
     * @return  int                      <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     */
    public int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket, long timeout) {
    	return requestWupNoRetry(fromModelType, operType, reqPacket, null, timeout);
    }
    
    /**
     *   发送wup请求
     *   
     * @param fromModelType  请求的模块标识
     * @param operType            请求操作标识
     * @param reqPacket           请求数据
     * @param extraData           请求附带数据（会在回包接口中原样返回）
     * @param timeout              请求超时（单位毫秒）
     * @return  int                      <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     */
    public int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket, QRomWupReqExtraData extraData, long timeout) {
        return requestWupNoRetry(fromModelType, operType,reqPacket,extraData,timeout,false);
    }

    public int requestWupNoRetry(int fromModelType, int operType, UniPacket reqPacket, QRomWupReqExtraData extraData, long timeout, boolean isRequestEncrypt) {
        if (timeout >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("timeout should not >= Integer.MAX_VALUE");
        }

        Request request = new Request(reqPacket, RequestType.NORMAL_REQUEST);
        if (timeout > 0) {
            request.getRequestOption().setTimeoutMills((int)timeout);
        }
        request.getRequestOption().setIsEncryptRequest(isRequestEncrypt);
        return (int) DispatcherFactory.getDefault().send(request
                        , new AsyncRequestCallback(fromModelType, operType, extraData));
    }
    /**
     *   发送wup请求
     *   
     * @param fromModelType  请求的模块标识
     * @param operType            请求操作标识
     * @param reqPacket           请求数据
     * @param timeout              请求超时（单位毫秒）
     * @return  int                      <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     */
    @Deprecated
    public int requestWupNoRetry(int fromModelType, int operType, byte[] reqPacketBytes, long timeout) {
    	return requestWupNoRetry(fromModelType, operType, reqPacketBytes, null, "reqPacket_bytes", timeout);
    }
    
    /**
     *   发送wup请求
     *   
     * @param fromModelType  请求的模块标识
     * @param operType            请求操作标识
     * @param reqPacketBytes   请求数据
     * @param extraData            额外数据
     * @param serviceName       请求服务的服务名（用于确认是否加密数据，默认加密）
     * @param timeout               请求超时（单位毫秒）
     * @return  int                      <=0 ： 请求未发送，> 0 : 请求发送，等待回包
     */
    @Deprecated
    public int requestWupNoRetry(int fromModelType, int operType, byte[] reqPacketBytes, 
            QRomWupReqExtraData extraData, String serviceName, long timeout) {
    	return requestWupNoRetry(fromModelType, operType, reqPacketBytes, extraData, serviceName, "", timeout);
    }    
    
    /**
     * 
     * @param fromModelType  请求的模块标识
     * @param operType            请求操作标识
     * @param reqPacketBytes   请求数据
     * @param extraData            额外数据
     * @param serviceName       请求服务的服务名（用于确认是否加密数据，默认加密）
     * @param functionName     请求服务的function
     * @param timeout               请求超时（单位毫秒）
     * @return
     */
    @Deprecated
    public int requestWupNoRetry(int fromModelType, int operType, byte[] reqPacketBytes, 
            QRomWupReqExtraData extraData, String serviceName, String functionName, long timeout) {
    	if (reqPacketBytes == null) {
    		throw new IllegalArgumentException("reqPacketBytes should not be null");
    	}
    	if (timeout >= Integer.MAX_VALUE) {
    		throw new IllegalArgumentException("timeout should not >= Integer.MAX_VALUE");
    	}
    	
    	Request request = new Request(reqPacketBytes, serviceName, functionName, RequestType.NORMAL_REQUEST);
    	if (timeout > 0) {
    		request.getRequestOption().setTimeoutMills((int)timeout);
    	}
    	
    	return (int)DispatcherFactory.getDefault().send(request
    					, new AsyncRequestCallback(fromModelType, operType, extraData));
    }
    
    /**
     * 非对称加密wup请求
     * @param fromModel  请求的模块标识
     * @param operType     请求操作标识
     * @param reqPkg         请求数据包
     * @return
     */
    public int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg) {
    	return requestAsymEncryptWup(fromModel, operType, reqPkg, null);
    }
    
    /**
     * 非对称加密wup请求
     * @param fromModel  请求的模块标识
     * @param operType     请求操作标识
     * @param reqPkg         请求数据包
     * @param extra            额外数据
     * @return
     */
    public int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg, QRomWupReqExtraData extra) {
    	return requestAsymEncryptWup(fromModel, operType, reqPkg, extra, 0);
    }
    
    /**
     * 非对称加密wup请求
     * @param fromModel  请求的模块标识
     * @param operType     请求操作标识
     * @param reqPkg         请求数据包
     * @param timeout        超时时间（单位毫秒）
     * @return
     */
    public int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg, long timeout) {
    	return requestAsymEncryptWup(fromModel, operType, reqPkg, null, timeout);
    }
    
    /**
     * 非对称加密wup请求
     * @param fromModel  请求的模块标识
     * @param operType     请求操作标识
     * @param reqPkg         请求数据包
     * @param extra             附带数据
     * @param timeout        超时时间（单位毫秒）
     * @return
     */
    public int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg, QRomWupReqExtraData extra, long timeout) {
    	return requestAsymEncryptWup(fromModel, operType, reqPkg, extra, timeout, null);
    }
    
    /**
     * 非对称加密wup请求
     * @param fromModel  请求的模块标识
     * @param operType     请求操作标识
     * @param reqPkg         请求数据包
     * @param extra             附带数据
     * @param timeout        超时时间（单位毫秒）
     * @param appPkgInfo  指定app的信息 （一般为app的包名）
     * @return
     */
    protected int requestAsymEncryptWup(int fromModel, int operType, UniPacket reqPkg, 
            QRomWupReqExtraData extra, long timeout, String appPkgInfo) {
    	if (reqPkg == null) {
    		throw new IllegalArgumentException("reqPkg should not be null");
    	}
    	if (timeout >= Integer.MAX_VALUE) {
    		throw new IllegalArgumentException("timeout should not >= Integer.MAX_VALUE");
    	}
    	
    	Request request = new Request(reqPkg, RequestType.ASYM_ENCRPT_REQUEST);
    	request.getRequestOption().setIsEncryptRequest(true);
    	if (timeout > 0) {
    		request.getRequestOption().setTimeoutMills((int)timeout);
    	}
    	if (appPkgInfo != null) {
    		request.setAppPkgInfo(appPkgInfo);
    	}
    	
    	return (int)DispatcherFactory.getDefault().send(request
					, new AsyncRequestCallback(fromModel, operType, extra));
    }
    
    /**
     * 发送同步非对策加密请求
     * @param reqPkg           请求数据包
     * @param appPkgName 请求app的标识
     * @return
     */
    public byte[] sendSynAsymEncryptRequest(UniPacket reqPkg, String appPkgName) {
    	return sendSynAsymEncryptRequest(reqPkg, appPkgName, 0);
    }
    
    /**
     * 发送同步非对策加密请求 
     * @param reqPkg           请求数据包
     * @param appPkgName 请求app的标识
     * @param timeout          超时时间（单位毫秒）
     * @return
     */
    public byte[] sendSynAsymEncryptRequest(UniPacket reqPkg, String appPkgName, long timeout) {
    	if (reqPkg == null) {
    		throw new IllegalArgumentException("reqPkg should not be null");
    	}
    	if (timeout >= Integer.MAX_VALUE) {
    		throw new IllegalArgumentException("timeout should not >= Integer.MAX_VALUE");
    	}
    	
    	Request request = new Request(reqPkg, RequestType.ASYM_ENCRPT_REQUEST);
    	request.getRequestOption().setIsEncryptRequest(true);
    	if (timeout > 0) {
    		request.getRequestOption().setTimeoutMills((int)timeout);
    	}
    	if (appPkgName != null) {
    		request.setAppPkgInfo(appPkgName);
    	}
    	
    	SyncRequestCallback callback = new SyncRequestCallback();
    	if (DispatcherFactory.getDefault().send(request, callback) <= 0) {
    		return null;
    	}
    	callback.waitResponse();
    	return callback.getResponseData();
    }
    
    
    /**
     * 同步发送wup请求
     *    -- 该方法会阻塞调用线程，直到网络数据返回
     *    -- 非ui线程才能调用
     * @param pkt  请求的协议数据
     * @return  byte[] 服务器返回数据
     */
    public byte[] sendSynWupRequest(UniPacket reqPkg) {
    	return sendSynWupRequest(reqPkg, 0);
    }
    
    /**
     * 同步发送wup请求
     *    -- 该方法会阻塞调用线程，直到网络数据返回 
     * @param pkt          请求的协议数据
     * @param timeout   超时时间  
     *       （该值为网络连接的超时，部分手机上在假网络时该值无效，系统自动使用默认值；
     *       若请求对时间敏感，请自己另设计时器，或调用异步接口处理网络请求,如requestWup(...)接口）
     * @return  byte[] 服务器返回数据
     */
    public byte[] sendSynWupRequest(UniPacket reqPkg, long timeout) {  
    	if (reqPkg == null) {
    		throw new IllegalArgumentException("reqPkg should not be null");
    	}
    	if (timeout >= Integer.MAX_VALUE) {
    		throw new IllegalArgumentException("timeout should not >= Integer.MAX_VALUE");
    	}
    	
    	Request request = new Request(reqPkg, RequestType.NORMAL_REQUEST);
    	if (timeout > 0) {
    		request.getRequestOption().setTimeoutMills((int)timeout);
    	}
    	
    	SyncRequestCallback callback = new SyncRequestCallback();
    	if (DispatcherFactory.getDefault().send(request, callback) <= 0) {
    		return null;
    	}
    	callback.waitResponse();
    	return callback.getResponseData();
    }
    
    
    /**
     * 取消wup请求
     * @param reqId
     * @return
     */
    public boolean cancelTask(int reqId) {
    	DispatcherFactory.getDefault().cancel(reqId);
    	return true;
    }
    /**
     * wup工程内部使用的获取guid方法
     *    -- 避免外部重写该方法导致错误
     * @return
     */
    private String getGUIDStrForInner() {
        return StringUtil.byteToHexString(getGUIDBytesForInner());
    }
    
    /**
     * wup工程内部使用的获取guid方法
     *    -- 避免外部重写该方法导致错误
     * @return
     */
    private byte[] getGUIDBytesForInner() {
    	return GuidProxy.get().getGuidBytes();
    }
    
    /**
     *   外部对象调用获取guid
     * @return  byte[]
     */
    public byte[] getGUIDBytes() {
        return getGUIDBytesForInner();
    }
    
    /**
     *   外部对象调用获取guid
     * @return  String guid的16进制字符串
     */
    public String getGUIDStr() {
        return getGUIDStrForInner();
    }
    
    /**
     * 获取app的qua
     *   -- 默认请在asset目录下配置build_config.ini文件
     * @return
     */
    public String getQua() {
    	return BaseInfoManager.get().getQua();
    }
        
    /**
     * 获取登录后的QRomId <p>
     *    -- 若要获取到romid 请配置权限： "android.permission.READ_SETTINGS"
     * @return
     */
    @Deprecated
    public long getQRomId() {
        return 0;
    }
    
    /**
     * 获取当前wup的socket代理地址
     * @return  List<String>
     */
    public List<String> getCurSocketProxyList() {
    	return WupInterface.getCurWupSocketIPList();
    }
    
    /**
     * 获取当前wup的http代理地址
     * @return  List<String>
     */
    public List<String> getCurApnProxyList() {
    	return WupInterface.getCurWupSocketIPList();
    }
    
    /**
     * 主动发起获取guid请求 <p>
     *    -- 仅在主动管理guid模式及guid不合法时才发送请求
     * @return int 具体返回码请看 LOGIN_RSP_CODE，> 100则请求发送，其他延时或取消发送
     */
    public int requestGuid() {
    	return -1;
    }
    
    /**
     * 主动发起获取iplist 请求<p>
     *      -- 检测 a. 发送频率是否频繁；
     *                  b. 缓存是否是否超时；
     *                  c.  是否亮屏；
     *      仅当亮屏且缓存超时时才发起请求
     *    
     * @return int 参考QRomWupConstants.LOGIN_RSP_CODE
     */
    public int requestIpList() {
    	return -1;
    }
    
    /**
     * 强制发送iplist请求<br>
     * （请确认确实需要更新iplist才调用，如后台下发iplist更新消息时, 默认情况下sdk会自动更新iplist;<br>
     *       普通情况下请调用requestIpList接口）<p>
     *    -- 忽略缓存超时时间（仅检测请求发送是否频繁，是否在亮屏下）
     * @return
     */
    public int sendForceIplistIgnoreCacheTimeout() {
    	return -1;
    }
        
    /**
     * 发起login <p>
     *    -- 目前仅当前的tcm host和app单发模式才执行操作，其他模式下均不执行，<br>
     *        现在login命令字仅下发给rom host，且login服务仅记录最后一次login的app信息，暂只针对host（by sukeyli 2015.2.2）<br>
     *    -- 仅在需要login的时候发起，调用时间请与对应产品确认
     * @return reqId 返回码同requestGuid
     */
    public synchronized int doLogin() {
        return GuidProxy.get().doLogin();
    }
    
    /**
     * wup请求是否发向测试环境
     * @return boolean 
     */
    public boolean isWupReqSendTest() {
    	return RunEnv.get().getEnvType() == RunEnvType.Gamma;
    }
    
    /**
     * 获取RomBaseInfo
     *    -- 注意：返回对的RomBaseInfo是进程唯一的，即同一个进程中所有wupManager共享
     *        
     * @return  RomBaseInfo
     */
    public RomBaseInfo getRomBaseInfo() {
    	return new RomBaseInfoBuilder() {
    		protected void doBuild(RomBaseInfo romBaseInfo) {
    			updataBaseInfo(romBaseInfo);
    		}
    	}.build();
    }
    
    // 新版本兼容updateBaseInfo
    public void updataBaseInfo(RomBaseInfo baseInfo) {
    }
    
    /**
     * 获取长连接代理地址
     * @return
     */
    public String getWupProxySocketAddress() {
    	SelectedIPPortResult result = IPListProxy.get().selectIPPort(RunEnv.get().getEnvType()
    			, EIPType.WUPSOCKET, ConnectInfoManager.get().getConnectInfo());
    	if (result == null) {
    		return null;
    	}
    	
    	return result.getNodeInfo().getNode().toUrlString();
    }
    
    /**
     * wup模块发生异常
     * @param type       异常类型
     * @param e            exception信息
     * @param desc       二外信息描述
     */
    public void onWupErrCatched(int type, Throwable e, String desc) {
    }
    
    public void sendWupDataChangeMsg(int dataFlg) {
    }
    
    /**
     * 重新加下wup相关数据
     */
    public void reloadWupInfo() {
    }
    
    /**
     * 释放所有资源 <p>
     *    -- 释放后，需重新startup后才能使用
     */
    public void release() {
    	WupInterface.unRegisterGuidListener(this);
    }
    
    @Subscriber
    public void onGuidBytesUpdateEvent(GuidUpdateEvent event) {
    	onGuidChanged(event.getNewGuidBytes());
    }
    
    
    /**
     * wup请求完成--数据接收成功
     * 
     * @param fromModelType
     * @param reqId
     * @param operType
     * @param wupReqExtraData
     * @param wupRspExtraData
     * @param serviceName
     * @param response
     */
    public abstract void onReceiveAllData(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response);
    
    /**
     * wup请求失败 -- 数据接收失败
     * 
     * @param fromModelType
     * @param reqId
     * @param operType
     * @param wupReqExtraData
     * @param wupRspExtraData
     * @param serviceName
     * @param errorCode
     * @param description
     */
    public abstract void onReceiveError(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, int errorCode, String description);
    /**
     * 同步新guid 
     * 
     * @param vGuid
     */
    public abstract void onGuidChanged(byte[] vGuid);
    
    
}
