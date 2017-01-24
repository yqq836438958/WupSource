package qrom.component.wup.iplist;

import java.util.ArrayList;
import java.util.List;

import com.qq.jce.wup.UniPacket;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.QRomWupDataBuilder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.core.DispatcherFactory;
import qrom.component.wup.framework.Request;
import qrom.component.wup.framework.Response;
import qrom.component.wup.framework.Request.RequestType;
import qrom.component.wup.framework.core.IRequestCallback;
import qrom.component.wup.guid.GuidProxy;
import TRom.EIPType;
import TRom.IPListReq;
import TRom.IPListRsp;

/**
 *  封装IPList的抓取
 * @author wileywang
 *
 */
public class IPListFetcher implements IRequestCallback {
	
	public static interface IIPListRespCallback {
		public void onIPListResponse(
				IPListFetcher ipListFetcher, int errorCode, String errorMsg, IPListRsp resp);
	}
	
	private ConnectInfo mConnectInfo;
	private List<EIPType> mEIPTypes;
	private boolean mIsAll;
	private IIPListRespCallback mCallback;
	private IWorkRunner mWorkRunner; // 获取返回后的投递线程
	private RunEnvType mRequestEnvType;
	
	public IPListFetcher(
				ConnectInfo connectInfo
				, List<EIPType> eIPTypes
				, boolean bAll
				, IIPListRespCallback callback
				, RunEnvType requestEnvType
				, IWorkRunner workRunner) {
		if (connectInfo == null) {
			throw new IllegalArgumentException("connectInfo should not be null");
		}
		if (eIPTypes == null || eIPTypes.isEmpty()) {
			throw new IllegalArgumentException("eIPTypes should not be null or empty");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback should not be null");
		}
		if (requestEnvType == null) {
			throw new IllegalArgumentException("requestEnvType should not be null");
		}
		
		this.mConnectInfo = connectInfo;
		this.mEIPTypes = eIPTypes;
		this.mIsAll = bAll;
		this.mCallback = callback;
		this.mWorkRunner = workRunner;
		this.mRequestEnvType = requestEnvType;
	}
	
	public boolean start() {
		IPListReq req = new IPListReq();
		req.setVGUID(GuidProxy.get().getGuidBytes());
		req.setEApnType(JceNetUtil.getEAPNTYPE(mConnectInfo).value());
		req.setENetType(JceNetUtil.getNETTYPE(mConnectInfo).value());
		
		ArrayList<Integer> ipTypes = new ArrayList<Integer>();
		for (EIPType eIpType : mEIPTypes) {
			ipTypes.add(eIpType.value());
		}
		req.setVIPType(ipTypes);
		req.setBAll(mIsAll);
		
		UniPacket reqPacket = QRomWupDataBuilder.createReqUnipackageV3("tromlogin", "getIpList", "stIPListReq", req);
		Request request = new Request(reqPacket, RequestType.NORMAL_REQUEST);
		request.setRequestEnvType(mRequestEnvType);
		request.getRequestOption().setCallbackRunner(mWorkRunner);
		request.getRequestOption().setForceDefaultRoute(true);
		
		if (DispatcherFactory.getDefault().send(request, this) < 0) {
			return false;
		}
		
		return true;
	}
	
	public final List<EIPType> getEIPTypes() {
		return mEIPTypes;
	}
	
	public final boolean isAll() {
		return mIsAll;
	}
	
	public final RunEnvType getRequestEnvType() {
		return mRequestEnvType;
	}
	
	public final ConnectInfo getConnectInfo() {
		return mConnectInfo;
	}
	
	@Override
	public void onRequestFinished(long requestId, Request request, Response response) {
		if (response.getErrorCode() == 0) {
			UniPacket respPacket = QRomWupDataBuilder.getUniPacketV3(response.getResponseContent());
			if (respPacket == null) {
				mCallback.onIPListResponse(
						this, WUP_ERROR_CODE.WUP_INNER_ERROR
						, "IPList response content parse unipacket failed!", null);
				return ;
			}
			
			Integer result = respPacket.getByClass("", Integer.valueOf(0));
			if (result == null || result != 0) {
				mCallback.onIPListResponse(
						this, WUP_ERROR_CODE.WUP_INNER_ERROR
						, "IPList response server returns error! result=" + result, null);
				return ;
			}
			
			IPListRsp ipListResp = new IPListRsp();
			ipListResp = respPacket.getByClass("stIPListRsp", ipListResp);
			if (ipListResp == null) {
				mCallback.onIPListResponse(this, WUP_ERROR_CODE.WUP_INNER_ERROR
						, "IPList response server returns error! can not get stIPListRsp", null);
				return ;
			}
			
			mCallback.onIPListResponse(this, 0, "", ipListResp);
			
			return ;
		} 
		mCallback.onIPListResponse(this, response.getErrorCode(), response.getErrorMsg(), null);
	}
	

}
