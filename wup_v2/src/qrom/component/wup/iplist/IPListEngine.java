package qrom.component.wup.iplist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.IWorkRunner;
import qrom.component.wup.base.RunEnv;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.base.android.HandlerWorkRunner;
import qrom.component.wup.base.event.EventBus;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.ConnectInfoManager;
import qrom.component.wup.base.net.IConnectInfoListener;
import qrom.component.wup.base.net.NetType;
import qrom.component.wup.base.utils.ThreadResultHandler;
import qrom.component.wup.core.WupV1Support;
import qrom.component.wup.iplist.node.IPListNode;
import qrom.component.wup.iplist.node.IPPortNode;
import qrom.component.wup.iplist.node.IPRootNode;
import qrom.component.wup.iplist.node.IPTypeNode;
import TRom.EIPType;
import TRom.IPListRsp;
import TRom.JoinIPInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;

/**
 *  负责主动抓取IpList, 更新IpList，以及缓存IpList的
 *  
 *  注意: 逻辑和数据维护在工作线程, 暴露的API需通过跨线程调用来完成
 * @author wileywang
 *
 */
public class IPListEngine extends BroadcastReceiver 
		implements IPListFetcher.IIPListRespCallback, IConnectInfoListener {
	private static final String TAG = "IPListEngine";
	
	// push 强制更新IPList
	private static final String ACTION_PUSH_UPDATE_IPLIST = "qrom.component.push.action.updateIplist";
	
	private static IPListEngine sInstance;
	private static HandlerThread sIPListEngineThread;
	public static void startUp() {
		if (sInstance == null) {
			synchronized(IPListEngine.class) {
				if (sInstance == null) {
					sIPListEngineThread = new HandlerThread("IPListEngineThread");
					sIPListEngineThread.start();
					
					sInstance = new IPListEngine(
							new HandlerWorkRunner(sIPListEngineThread));
					EventBus.getDefault().post(new IPListEngineStartedEvent());
				}
			}
		}
	}
	
	public static IPListEngine get() {
		return sInstance;
	}
	
	private IWorkRunner mWorkRunner;
	private Map<Integer, IPListStorage> mIpListStorageMap;
	
	private Map<Integer, Boolean> mEnvIsInUpdateIPListMap;
	
	public IPListEngine(IWorkRunner workRunner) {
		mWorkRunner = workRunner;
		mIpListStorageMap = new HashMap<Integer, IPListStorage>();
		mEnvIsInUpdateIPListMap = new HashMap<Integer, Boolean>();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_PUSH_UPDATE_IPLIST);
		ContextHolder.getApplicationContextForSure().registerReceiver(this, intentFilter);
		
		ConnectInfoManager.get().registerConnectInfoListener(this);
	}
	
	public List<IPListNode> copyAllIPListNode(final RunEnvType envType, final EIPType ipType) {
		if (envType == null || ipType == null) {
			return null;
		}
		
		return new ThreadResultHandler<List<IPListNode>>(mWorkRunner) {

			@Override
			public void fillResult() {
				mResult = doCopyAllIPListNode(envType, ipType);
			}

		}.getResult();
	}
	
	public IPListNode copyIPListNode(final RunEnvType envType
			, final EIPType ipType
			, final int apnIndex) {
		if (envType == null || ipType == null) {
			return null;
		}
		
		return new ThreadResultHandler<IPListNode>(mWorkRunner) {

			@Override
			public void fillResult() {
				IPListNode ipListNode = getIPListNode(envType, ipType, apnIndex);
				if (ipListNode != null) {
					mResult = new IPListNode(ipListNode);
				}
			}

		}.getResult();
	}
	
	
	
	/**
	 *  选取接入点
	 * @param envType
	 * @param ipType
	 * @return
	 */
	public IPPortNodeInfo selectIPPort(final RunEnvType envType, final EIPType ipType, final int apnIndex) {
		if (ipType == null || envType == null) {
			return null;
		}
		
		return new ThreadResultHandler<IPPortNodeInfo>(mWorkRunner){

			@Override
			public void fillResult() {
				mResult = doSelectIPPort(envType, ipType, apnIndex);
			}
			
		}.getResult();
	}
	
	private IPPortNodeInfo doSelectIPPort(RunEnvType envType, EIPType ipType, int apnIndex) {
		checkInWorkerThread();
		
		IPListNode ipListNode = getIPListNode(envType, ipType, apnIndex);
		if (ipListNode == null) {
			doUpdateIPList(envType, false, false);
			return null;
		}
		
		IPPortNode preferIPPortNode = ipListNode.getPreferIPPortNode();
		if (preferIPPortNode == null) {
			return null;
		}
		
		// 注意，为了保证整个IPList的节点树不会因为线程间转移而被误用，这里利用拷贝来保证线程安全
		return new IPPortNodeInfo(
				 new IPPortNode(preferIPPortNode)
				, ipListNode.getIPPortList().size()
				, ipListNode.getPreferNodeIndex()
				, ipListNode.getClientIP());
	}
	
	public void updateIPList(final RunEnvType envType, final boolean isForceUpdate, final boolean isShouldUpdateAll) {
		if (envType == null) {
			return ;
		}
		
		if (mWorkRunner.getThread() == Thread.currentThread()) {
			doUpdateIPList(envType, isForceUpdate, isShouldUpdateAll);
			return ;
		}
		
		mWorkRunner.postWork(new Runnable() {

			@Override
			public void run() {
				doUpdateIPList(envType, isForceUpdate, isShouldUpdateAll);
			}
			
		});
	}
	
	private List<IPListNode> doCopyAllIPListNode(RunEnvType envType, EIPType ipType) {
		IPTypeNode ipTypeNode = getIPListStorage(envType).getIPRootNode().getIPTypeNode(ipType);
		if (ipTypeNode == null) {
			return null;
		}
		
		return ipTypeNode.copyAllIPListNodes();
	}
	
	public void reportError(final SelectedIPPortResult result, final int errorCode) {
		if (result == null) {
			return ;
		}
		
		if (mWorkRunner.getThread() == Thread.currentThread()) {
			doReportError(result, errorCode);
			return ;
		}
		
		mWorkRunner.postWork(new Runnable() {
			@Override
			public void run() {
				doReportError(result, errorCode);
			}
		});
	}
	
	private void doReportError(SelectedIPPortResult result, int errorCode) {
		if (errorCode == WUP_ERROR_CODE.WUP_READ_TIMEOUT) {
			// 读超时表示接入点是联通的， 很可能是后台数据量大，或者当时网络速度慢，本身和后台的关系并不大
			return ;
		}
		
		IPListNode ipListNode = getIPListNode(
				result.getEnvType(), result.getIPType(), result.getApnIndex());
		if (ipListNode == null) {
			return ;
		}
		
		int targetIndex = -1;
		for (int index = 0; index < ipListNode.getIPPortList().size(); index++) {
			IPPortNode ipPortNode = ipListNode.getIPPortList().get(index);
			if (ipPortNode.equals(result.getNodeInfo().getNode())) {
				targetIndex = index;
				break;
			}
		}
		
		if (targetIndex != ipListNode.getPreferNodeIndex()) {
			// 最佳接入点已经被切换 
			return ;
		}
		
		// 第一个和第二个接入点，输入最佳接入点, 错误重试次数为两次
		int preferNodeIndex = ipListNode.getPreferNodeIndex();
		if (preferNodeIndex == 0 || preferNodeIndex == 1) {
			// 非网络连接失败，并且是较好的接入点, 这个时候尝试复用
			if (errorCode != WUP_ERROR_CODE.WUP_CONNECTED_FAILED && ipListNode.getPreferNodeErrorTimes() < 3) {
				ipListNode.setPreferNodeErrorTimes(ipListNode.getPreferNodeErrorTimes() + 1);
				return ;
			}
		}
		
		// 切换接入点
		ipListNode.setPreferNodeIndex(++preferNodeIndex);
		ipListNode.setPreferNodeErrorTimes(0);
		
		// 切换完成后，发现无可用接入点, 则更新IPList
		if (preferNodeIndex >= ipListNode.getIPPortList().size()) {
			updateIPList(result.getEnvType(), true, false);
			
			// 同时尝试把第一个接入点，切换第一个接入点
			ipListNode.setPreferNodeIndex(0);
		}
		
	}
	
	
	private IPListStorage getIPListStorage(RunEnvType envType) {
		IPListStorage ipListStorage = mIpListStorageMap.get(envType.value());
		if (ipListStorage == null) {
			ipListStorage = new IPListStorage(envType);
			mIpListStorageMap.put(envType.value(), ipListStorage);
		}
		return ipListStorage;
	}
	
	private IPListNode getIPListNode(RunEnvType envType, EIPType ipType, int apnIndex) {
		IPTypeNode ipTypeNode = getIPListStorage(envType).getIPRootNode().getIPTypeNode(ipType);
		if (ipTypeNode == null) {
			return null;
		}
		
		IPListNode ipListNode = ipTypeNode.getApnNode(apnIndex);
		if (ipListNode == null) {
			return null;
		}
		
		return ipListNode;
	}

	private void checkInWorkerThread() {
		if (mWorkRunner.getThread() != Thread.currentThread()) {
			throw new IllegalStateException("please call this in worker thread");
		}
	}
	
	private void doUpdateIPList(RunEnvType envType, boolean isForceUpdate, boolean isShouldUpdateAll) {
		checkInWorkerThread();
		
		Boolean isEnvUpdateIPList = mEnvIsInUpdateIPListMap.get(envType.value());
		if (isEnvUpdateIPList != null && isEnvUpdateIPList.booleanValue() && !isForceUpdate) {
			QRomLog.d(TAG, "doUpdateIPList called, but envType=" + envType + " is InUpdating");
			return ;
		}
		
		QRomLog.d(TAG, "doUpdateIPList started! envType=" + envType 
							+ ", isForceUpdate=" + isForceUpdate + ", isShouldUpdateAll=" + isShouldUpdateAll);
		
		boolean isAll = false;
		ConnectInfo connectInfo = ConnectInfoManager.get().getConnectInfo();
		
		if (!isForceUpdate) {
			if (!connectInfo.isConnected()) {
				QRomLog.d(TAG, "doUpdateIPlist end! Network Not Connected!");
				return ;
			}
		
			// 检测更新时间
			IPTypeNode ipTypeNode = getIPListStorage(envType).getIPRootNode().getIPTypeNode(EIPType.WUPPROXY);
			if (ipTypeNode != null) {
				IPListNode ipListNode = ipTypeNode.getApnNode(ApnIndexConvertor.getApnIndex(connectInfo));
				if (ipListNode != null) {
					long timeInterval = (System.currentTimeMillis()/1000) - ipListNode.getLastUpdateTimestamp();
					if (timeInterval < getIPListUpdateTimeInterval(envType, connectInfo.getNetType())) {
						QRomLog.d(TAG, "doUpdateIPList end! TimeInterval is in cache time!");
						return ;
					}
				}
			}
			
		}
		
		// wifi下全量更新数据
		if (connectInfo.getNetType() == NetType.NET_WIFI) {
			isAll = true;
		}
		
		// 全部强制全量更新，则一定全量更新，
		if (isShouldUpdateAll) {
			isAll = true;
		}
		
		sendIpListRequest(envType, connectInfo, isAll);
	}
	
	private void sendIpListRequest(RunEnvType envType
			, ConnectInfo connectInfo
			, boolean isAll) {
		List<EIPType> ipTypes = new ArrayList<EIPType>();
		ipTypes.add(EIPType.WUPPROXY);
		ipTypes.add(EIPType.WUPSOCKET);
		
		if (new IPListFetcher(connectInfo, ipTypes, isAll, this, envType, mWorkRunner).start()) {
			mEnvIsInUpdateIPListMap.put(envType.value(), Boolean.TRUE);
		}
	}
	
	/**
	 * WIFI下刷新频率增大，有助于提高准确率
	 * 
	 *  获取更新时间的间隔配置
	 */
	private long getIPListUpdateTimeInterval(RunEnvType runEnvType, NetType netType) {
		if (runEnvType != RunEnvType.IDC) {
			// 非正式环境，刷新IPList拉长到一周
			return 7* 24 * 3600;
		}
		
		if (netType == NetType.NET_WIFI) {
			return 3600;
		}
		return 10 * 3600;
	}

	@Override
	public void onIPListResponse(IPListFetcher ipListFetcher, int errorCode, String errorMsg, IPListRsp resp) {
		QRomLog.d(TAG, "onIPListResponse envType=" + ipListFetcher.getRequestEnvType()
				+ ", errorCode=" + errorCode + ", errorMsg=" + errorMsg + ", iplistRsp=" + resp);
		mEnvIsInUpdateIPListMap.remove(ipListFetcher.getRequestEnvType().value());
		if (resp != null) {
			IPListStorage ipListStorage = getIPListStorage(ipListFetcher.getRequestEnvType());
			
			ipListStorage.edit();
			IPRootNode ipRootNode = ipListStorage.getIPRootNode();
			for (JoinIPInfo joinIPInfo : resp.getVJoinIPInfo()) {
				EIPType ipType = EIPType.convert(joinIPInfo.eIPType);
				if (ipType == null) {
					continue;
				}
				
				IPTypeNode ipTypeNode = ipRootNode.getIPTypeNode(ipType);
				if (ipTypeNode == null) {
					ipTypeNode = new IPTypeNode(ipType);
					ipRootNode.updateIpTypeNode(ipType, ipTypeNode);
				}
				
				int apnIndex = ApnIndexConvertor.getApnIndex(joinIPInfo);
				
				IPListNode ipListNode = new IPListNode(apnIndex);
				ipListNode.setClientIP(resp.getSClientIp());
				ipListNode.setLastUpdatTimestamp(System.currentTimeMillis()/1000);
				for (String ipPort : joinIPInfo.getVIPList()) {
					IPPortNode ipPortNode = IPPortNode.parse(ipPort);
					if (ipPortNode != null) {
						ipListNode.getIPPortList().add(ipPortNode);
					}
				}
				if (ipListNode.getIPPortList().size() > 0) {
					ipListNode.setPreferNodeIndex(0);
				}
				
				ipTypeNode.updateApnNode(apnIndex, ipListNode);
			}
			
			ipListStorage.commit();
			
			EventBus.getDefault().post(new IPListUpdateEvent(ipListFetcher.getRequestEnvType()));
			WupV1Support.get().sendBroadcastForWupBaseDataUpdate(WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		QRomLog.d(TAG, "onReceive broadcast action=" + action);
		if (ACTION_PUSH_UPDATE_IPLIST.equalsIgnoreCase(action)){
			// push 强制更新IPList, 全部全量更新
			updateIPList(RunEnv.get().getEnvType(), true, true);
		}
	}

	@Override
	public void onConnectInfoReload() {
	}

	@Override
	public void onReceiveNetworkChanged() {
		updateIPList(RunEnvType.IDC, false, false);
		updateIPList(RunEnvType.Gamma, false, false);
	}
	
}
