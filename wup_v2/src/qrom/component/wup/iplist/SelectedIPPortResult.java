package qrom.component.wup.iplist;

import qrom.component.wup.base.RunEnvType;
import TRom.EIPType;

/**
 *  被选中的IPList的描述
 * @author wileywang
 *
 */
public class SelectedIPPortResult {
	private IPPortNodeInfo mNodeInfo;  // 选中的节点
	private EIPType mIPType;   // 对应的IP类型
	private RunEnvType mEnvType;  // 对应的环境类型
	
	private int mApnIndex; // 选中结果对应的APN索引
	
	private String mBssid; // 先预留, 目前是按照ApnIndex进行命中
	private String mExtraData;
	
	public SelectedIPPortResult(IPPortNodeInfo node
			, EIPType ipType
			, RunEnvType envType
			, int apnIndex) {
		this(node, ipType, envType, apnIndex, "");
	}
	
	public SelectedIPPortResult(IPPortNodeInfo nodeInfo
			, EIPType ipType
			, RunEnvType envType
			, int apnIndex
			, String bssid) {
		this.mNodeInfo = nodeInfo;
		this.mIPType = ipType;
		this.mEnvType = envType;
		this.mApnIndex = apnIndex;
		this.mBssid = bssid;
	}
	
	public IPPortNodeInfo getNodeInfo() {
		return mNodeInfo;
	}
	
	public EIPType getIPType() {
		return mIPType;
	}
	
	public RunEnvType getEnvType() {
		return mEnvType;
	}
	
	public int getApnIndex() {
		return mApnIndex;
	}
	
	public String getBssid() {
		return mBssid;
	}
	
	public String getExtraData() {
		return mExtraData;
	}
	
	public SelectedIPPortResult setExtraData(String extraData) {
		this.mExtraData = extraData;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(64);
		builder.append("SelectedIPPortResult[");
		builder.append("mNodeInfo=");
		builder.append(mNodeInfo);
		builder.append(", mIPType=");
		builder.append(mIPType);
		builder.append(", mEnvType=");
		builder.append(mEnvType);
		builder.append(", mApnIndex=");
		builder.append(mApnIndex);
		builder.append(", mBssid=");
		builder.append(mBssid);
		builder.append(", mExtraData=");
		builder.append(mExtraData);
		builder.append("]");
		return builder.toString();
	}
}
