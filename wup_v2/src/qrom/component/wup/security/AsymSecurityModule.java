package qrom.component.wup.security;

import java.util.HashMap;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.base.utils.ZipUtils;
import qrom.component.wup.framework.IModule;
import qrom.component.wup.framework.IModule.IModuleProcessor;
import qrom.component.wup.framework.IReqModule;
import qrom.component.wup.framework.IRespModule;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Request.RequestType;
import qrom.component.wup.statics.StatProxy;
import qrom.component.wup.sync.security.AsymCipherManager;
import TRom.SECPROXY_RETCODE;
import TRom.SecureReq;
import TRom.SecureRsp;

import com.qq.jce.wup.UniPacket;

/**
 *  非对称加密模块
 * @author wileywang
 *
 */
public class AsymSecurityModule implements IAsymSessionCallback {
	private static final String TAG = AsymSecurityModule.class.getSimpleName();
	
	private static final String USER_SESSION_ID_KEY = "asym_session_id";
	
	private IWorkRunner mWorkRunner; 
	private AsymSessionProcessor mAsymSessionProcessor;
	private AsymCipherManager mAsymCipherManager;
	
	private static class AsymSessionEntry {
		private long mRequestId;
		private IModuleProcessor<IReqModule.Param> mModuleProcessor;
		private IReqModule.Param mRequestParam;
		
		public AsymSessionEntry(long requestId
				, IModuleProcessor<IReqModule.Param> moduleProcessor
				, IReqModule.Param requestParam) {
			this.mRequestId = requestId;
			this.mModuleProcessor = moduleProcessor;
			this.mRequestParam = requestParam;
		}
		
		public long getRequestId() {
			return mRequestId;
		}
		
		public IModuleProcessor<IReqModule.Param> getModuleProcessor() {
			return mModuleProcessor;
		}
		
		public IReqModule.Param getRequestParam() {
			return mRequestParam;
		}
	}
	
	private Map<Long, AsymSessionEntry> mAsymSessionEntries;
	
	private IReqModule mReqModule =  new IReqModule() {
		@Override
		public Result onProcess(long reqId, IReqModule.Param param,
				IModuleProcessor<IReqModule.Param> processor) {
			if (param.getRequest().getRequestType() != RequestType.ASYM_ENCRPT_REQUEST) {
				return IModule.RESULT_OK;
			}
			
			AsymSessionEntry sessionEntry = new AsymSessionEntry(reqId, processor, param);
			mAsymSessionEntries.put(reqId, sessionEntry);
			mAsymSessionProcessor.getSession(reqId, getReqAppPkgName(param.getRequest())
					, param.getRequest().getRequestEnvType()
					, mWorkRunner, AsymSecurityModule.this);
			
			return IModule.RESULT_PENDING;
		}

		@Override
		public void cancel(long reqId) {
			mAsymSessionEntries.remove(reqId);
		}

		@Override
		public String getModuleName() {
			return AsymSecurityModule.class.getName() + "_ReqModule";
		}
	};
	
	private IRespModule mRespModule = new IRespModule() {

		@Override
		public Result onProcess(
				long reqId,
				IRespModule.Param param,
				IModuleProcessor<IRespModule.Param> processor) {
			if (param.getRequest().getRequestType() != RequestType.ASYM_ENCRPT_REQUEST) {
				return IModule.RESULT_OK;
			}
			
			if (param.getResponse().getErrorCode() == 0) {
				// 进行解码
				try {
					String sessionId = (String)param.getRequest().getUserData().getData(USER_SESSION_ID_KEY);
					UniPacket proxyPkg = QRomWupDataBuilder.getuniPacket(param.getResponse().getResponseContent(), null);
					int rspCode = (Integer)proxyPkg.get("");
					if (rspCode == SECPROXY_RETCODE._SP_RC_OK) {
						SecureRsp secureRsp = (SecureRsp) proxyPkg.get("stRsp");
						
						byte[] decryptDatas = mAsymCipherManager.decrypt(
								sessionId, secureRsp.getVRealRsp(), param.getRequest().getRequestEnvType());
						if (decryptDatas == null) {
							param.getResponse().setErrorCode(WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_RSP_DECCRYPT_FAILE);
							param.getResponse().setResponseContent(null);
							param.getResponse().setErrorMsg("asym decrypt response failed!");
							return IModule.RESULT_OK;
						}
						
						if (secureRsp.getBZip()) {
							decryptDatas = ZipUtils.unGzip(decryptDatas);
						}
						
						param.getResponse().setResponseContent(decryptDatas);
						
						return IModule.RESULT_OK;
					} 
					
					int errorCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_RSP_CODE_FAILE;
					String errorMsg = "";
				        // 请求错误
					switch (rspCode) {
					case SECPROXY_RETCODE._SP_RC_SESSION_OUTDATE: // session过期，请重新beginSession
						// 重置session通道
						mAsymCipherManager.notifySessionInvalid(
								getReqAppPkgName(param.getRequest()), param.getRequest().getRequestEnvType());
						errorMsg = "rsp code: session timeout";
						// 对于不重试的请求，请求重试
						if (param.getRequest().getRequestOption().getRetryTimes() <= 0) {
							param.getRequest().getRequestOption().setRetryTimes(1);
						}
						break;
					case SECPROXY_RETCODE._SP_RC_UNREGIST_PACKAGE: // 未注册的包名 [需要告知后台配置包名]
						errorMsg = "rsp code: unregister package";
						break;
					case SECPROXY_RETCODE._SP_RC_INVALID_SERVANT: // servant非法  [需要后台配置]
						errorMsg = "rsp code: servant invalid";
						break;

					case SECPROXY_RETCODE._SP_RC_DECRYPT_FAILED: // 解码失败
						errorMsg = "rsp code: server decrypt failed";
						break;
					case SECPROXY_RETCODE._SP_RC_SESSION_PARAM_ERROR: // session参数有误 [包名对应错误]
						errorMsg = "rsp code: session param invalid";
						break;
					case SECPROXY_RETCODE._SP_RC_SERVER_ERR:
						errorMsg = "rsp code: SERVER_ERR";
						break;
					case SECPROXY_RETCODE._SP_RC_INVALID_GUID: // guid 不合法
						errorMsg = "rsp code: guid invalid";
						break;
					case SECPROXY_RETCODE._SP_BUS_SERVER_FAILED: // 业务调用失败
						errorCode = WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_BUS_SERVICE_FAILE;
						errorMsg = "rsp code: _SP_BUS_SERVER_FAILED";
						break;
					case SECPROXY_RETCODE._SP_RC_ZIP_FAILED: // 服务器解压失败
						errorMsg = "rsp code: _SP_RC_ZIP_FAILED";
						break;
					default:
						break;
					}
					
					param.getResponse().setResponseContent(null);
					param.getResponse().setErrorCode(errorCode);
					param.getResponse().setSubErrorCode(rspCode);
					param.getResponse().setErrorMsg(errorMsg);
					
					return IModule.RESULT_OK;
				} catch (Throwable e) {
					QRomLog.e(TAG, e.getMessage(), e);
					
					param.getResponse().setResponseContent(null);
					param.getResponse().setErrorCode(WUP_ERROR_CODE.WUP_INNER_ERROR);
					param.getResponse().setErrorMsg(e.getMessage());
					
					return IModule.RESULT_OK;
				}
			}
			return IModule.RESULT_OK;
		}

		@Override
		public void cancel(long reqId) {
		}

		@Override
		public String getModuleName() {
			return AsymSecurityModule.class.getName() + "_RespModule";
		}
	};
	
	
	public AsymSecurityModule(IWorkRunner workRunner) {
		this.mWorkRunner = workRunner;
		this.mAsymCipherManager = new AsymCipherManager(ContextHolder.getApplicationContextForSure());
		this.mAsymCipherManager.setStatisticListener(StatProxy.get());
		this.mAsymSessionProcessor = new AsymSessionProcessor(mAsymCipherManager);
		this.mAsymSessionEntries = new HashMap<Long, AsymSessionEntry>();
	}
	
	public IReqModule getReqModule() {
		return mReqModule;
	}
	
	public IRespModule getRespModule() {
		return mRespModule;
	}

	@Override
	public void onAsymSessionCallback(long requestId, String sessionId, int errorCode, String errorMsg) {
		AsymSessionEntry sessionEntry = mAsymSessionEntries.remove(requestId);
		if (sessionEntry == null) {
			QRomLog.d(TAG, "onAsymSessionCallback session entry not exists for requestId=" + requestId + ", may be cancelled");
			return ;
		}
		
		if (errorCode != 0) {
			// 加密失败，取消发送流程
			sessionEntry.getModuleProcessor().cancelProcess(
					mReqModule, sessionEntry.getRequestId()
					, sessionEntry.getRequestParam(), errorCode, errorMsg);
			return ;
		}
		
		try {
			Request request = sessionEntry.getRequestParam().getRequest();
			
			// 进行加密数据的处理
			byte[] transportData = request.getTransportData();
			if (transportData == null) {
				transportData = request.getPacketEncodeData();
			}
			
			boolean isNeedZip = true;
			if (isNeedZip) {
				transportData = ZipUtils.gZip(transportData);
			}
			
			byte[] encrytTransportData = mAsymCipherManager.encrypt(
					sessionId, transportData, request.getRequestEnvType());
			if (encrytTransportData == null) {
				sessionEntry.getModuleProcessor().cancelProcess(mReqModule
						, sessionEntry.getRequestId()
						, sessionEntry.getRequestParam()
						, WUP_ERROR_CODE.WUP_TASK_ERR_SESSION_ENCRYPT
						, "encrpt session failed!");
				return;
			}

			SecureReq secureReq = new SecureReq();
			secureReq.setSSessionId(sessionId);
			secureReq.setVGUID(request.getGuidBytes());
			secureReq.setSPackageName(getReqAppPkgName(request));
			secureReq.setVRealReq(encrytTransportData);
			secureReq.setBZip(isNeedZip);

			UniPacket proxyPkg = QRomWupDataBuilder.createReqUnipackage("secprx", "secureCall", "stReq", secureReq);
			proxyPkg.setRequestId((int) sessionEntry.getRequestId());

			request.setTransportData(proxyPkg.encode());
			request.getUserData().putData(USER_SESSION_ID_KEY, sessionId);
			sessionEntry.getModuleProcessor().continueProcess(
					mReqModule, sessionEntry.getRequestId(), sessionEntry.getRequestParam());

		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
			sessionEntry.getModuleProcessor().cancelProcess(
					mReqModule, sessionEntry.getRequestId()
					, sessionEntry.getRequestParam(), WUP_ERROR_CODE.WUP_INNER_ERROR, e.getMessage());
		}
	}
	
	private String getReqAppPkgName(Request request) {
		if (!StringUtil.isEmpty(request.getAppPkgInfo())) {
			return request.getAppPkgInfo();
		} else {
			return ContextHolder.getApplicationContextForSure().getPackageName();
		}
	}
}
