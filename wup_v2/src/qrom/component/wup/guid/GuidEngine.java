package qrom.component.wup.guid;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants;
import qrom.component.wup.QRomWupConstants.BASEINFO_ERR_CODE;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.apiv2.RomBaseInfoBuilder;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.android.HandlerWorkRunner;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.net.IConnectInfoListener;
import qrom.component.wup.base.utils.ByteUtil;
import qrom.component.wup.base.utils.PhoneStatUtils;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.BaseInfoManager;
import qrom.component.wup.core.DispatcherFactory;
import qrom.component.wup.core.WupV1Support;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Request.RequestType;
import qrom.component.wup.framework.Response;
import qrom.component.wup.framework.core.IRequestCallback;
import qrom.component.wup.guid.storage.GuidStorageImpl;
import qrom.component.wup.guid.storage.IGuidStorage;
import qrom.component.wup.threads.StorageThread;
import TRom.ELOGINRET;
import TRom.LoginReq;
import TRom.LoginRsp;
import TRom.RomBaseInfo;

import com.qq.jce.wup.UniPacket;

/**
 * 负责Guid的拉取，缓存，同时进行login上报
 * @author wileywang
 *
 */
public class GuidEngine implements IRequestCallback, IConnectInfoListener {
	private static final String TAG = "GuidEngine";
	
	private static GuidEngine sInstance;
	
	/**
	 *  主动开启GuidEngine才能使用
	 */
	public static void startUp() {
		if (sInstance == null) {
			synchronized(GuidEngine.class) {
				if (sInstance == null) {
					sInstance = new GuidEngine(new HandlerWorkRunner(StorageThread.get()));
					EventBus.getDefault().post(new GuidEngineStartedEvent());
				}
			}
		}
	}
	
	public static GuidEngine get() {
		return sInstance;
	}
	
	private IWorkRunner mWorkRunner;  // Engine获取GUID上报的线程
	private IGuidStorage mGuidStorage;
	
	private boolean mIsRequesting;
	
	protected GuidEngine(IWorkRunner workRunner) {
		mGuidStorage = new GuidStorageImpl();
		mWorkRunner = workRunner;
		
		// 如果初始化后，发现GUID非法， 则为GUID未初始化，这个时候注册网络监听
		if (!QRomWupDataBuilder.isGuidValidate(mGuidStorage.getGuidBytes())) {
			ConnectInfoManager.get().registerConnectInfoListener(this);
		}
		
		// 初始化时主动上报
		requestLogin();
	}
	
	public byte[] getGuidBytes() {
		byte[] guidBytes = mGuidStorage.getGuidBytes();
		if (!QRomWupDataBuilder.isGuidValidate(guidBytes)) {
			requestLogin();
			return QRomWupConstants.WUP_DEFAULT_GUID;
		}
		return guidBytes;
	}
	
	public int requestLogin() {
		if (mIsRequesting) {
			return -1;
		}
		mWorkRunner.postWork(new Runnable() {
			@Override
			public void run() {
				doRequestLogin();
			}
		});
		
		return 9999; // 这个值是兼容WUPV1
	}
	
	private void doRequestLogin() {
		// 二次过滤，完全确保不会重复请求
		if (mIsRequesting) {
			return ;
		}
		
		RomBaseInfo romBaseInfo = new RomBaseInfoBuilder().build();
		if (StringUtil.isEmpty(romBaseInfo.getSQUA())) {
			QRomLog.e(TAG, "doRequestLogin() cancelled, not qua is set!");
			return ;
		}
		
		if (StringUtil.isEmpty(romBaseInfo.getSQIMEI())) {
            romBaseInfo.setSQIMEI(BASEINFO_ERR_CODE.QIME_REPORT_EMPTY_CODE);
        }
        
		if (romBaseInfo.getSQIMEI().startsWith(BASEINFO_ERR_CODE.QIME_ERR_CODE_SUFF)) {
			if (StringUtil.isEmpty(BaseInfoManager.get().getCurrentProcessName())) {
				romBaseInfo.setSQIMEI(romBaseInfo.getSQIMEI() + "_null");
			} else {
				romBaseInfo.setSQIMEI(romBaseInfo.getSQIMEI() + "_"
						+ BaseInfoManager.get().getCurrentProcessName());
			}
		}
		
		LoginReq loginReq = new LoginReq();
		String mac = PhoneStatUtils.getMacAddress(ContextHolder.getApplicationContextForSure());
		loginReq.setStBaseInfo(romBaseInfo);
		loginReq.setSMac(mac);
		
		Request req = new Request(
				QRomWupDataBuilder.createReqUnipackageV3("tromlogin", "login", "stLoginReq", loginReq)
				, RequestType.NORMAL_REQUEST);
		req.getRequestOption().setCallbackRunner(mWorkRunner);
		
		if (DispatcherFactory.getDefault().send(req, this) > 0) {
			mIsRequesting = true;
			QRomLog.d(TAG, "doRequestLogin send romBaseInfo= " + romBaseInfo + ", mac=" + mac);	
		}
	}
	
	// 不要在这个函数里面使用getGuidBytes方法， 当第一次guid拉取时，又获取guid的问题
	@Override
	public void onRequestFinished(long requestId, Request request, Response response) {
		mIsRequesting = false;
		if (response.getErrorCode() == 0) {
			UniPacket rspPacket = QRomWupDataBuilder.getUniPacketV3(response.getResponseContent());
			if (rspPacket == null) {
				QRomLog.e(TAG, "onRequestFinished, but parse login unipacket failed!");
				return ;
			}
			
			Integer result = rspPacket.getByClass("", Integer.valueOf(0));
			if (result == null) {
				QRomLog.e(TAG, "onRequestFinished, but parse result null!");
				return ;
			} else {
				if (result != 0) {
					if (result == ELOGINRET._E_QUA_ERROR) {
						QRomLog.e(TAG, "onRequestFinished, qua config error, please check!!!");
						return ;
					} else if (result == ELOGINRET._E_QUA_SN_UNCONF) {
						QRomLog.e(TAG, "onRequestFinished, qua sn not config, please goto the webadmin to config!!!");
						return ;
					}
					QRomLog.e(TAG, "onRequestFinished, but get server error=" + result);
					return ;
				}
			}
			
			LoginRsp loginRsp = new LoginRsp();
			loginRsp = rspPacket.getByClass("stLoginRsp", loginRsp);
	        if (loginRsp == null) {
	        	QRomLog.e(TAG, "onRequestFinished, but parse loginRsp failed!");
	        	return ;
	        }
	        
	        if (QRomWupDataBuilder.isGuidValidate(loginRsp.getVGUID())) {
	        	if (!ByteUtil.isEquals(mGuidStorage.getGuidBytes(), loginRsp.getVGUID())) {
	        		QRomLog.i(TAG, "onRequestFinished, Guid Changed! Old Guid="
	        					+ StringUtil.byteToHexString(mGuidStorage.getGuidBytes())
	        					+ ", New Guid=" + StringUtil.byteToHexString(loginRsp.getVGUID()));
	        		onGuidUpdates(loginRsp.getVGUID());
	        	} else {
	        		QRomLog.i(TAG, "onRequestFinished, Guid Not Changed!");
	        	}
			}
		} else {
			QRomLog.e(TAG, "onRequestFinished, errorCode=" + response.getErrorCode() + ", errorMsg=" + response.getErrorMsg());
		}
	}
	
	private void onGuidUpdates(byte[] newGuidBytes) {
		mGuidStorage.updateGuidBytes(newGuidBytes);
		
		ConnectInfoManager.get().unRegisterConnectInfoListener(this);
		EventBus.getDefault().post(new GuidUpdateEvent(newGuidBytes));
		WupV1Support.get().sendBroadcastForWupBaseDataUpdate(WUP_DATA_TYPE.WUP_DATA_GUID);
	}

	@Override
	public void onConnectInfoReload() {
		if (!QRomWupDataBuilder.isGuidValidate(mGuidStorage.getGuidBytes())
			&& ConnectInfoManager.get().getConnectInfo().isConnected()) {
			requestLogin();
		}
	}

	@Override
	public void onReceiveNetworkChanged() {
	}
}
