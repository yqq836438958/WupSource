package qrom.component.wup.apiv2;

import qrom.component.wup.QRomWupConstants.BASEINFO_ERR_CODE;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.core.BaseInfoManager;
import qrom.component.wup.guid.GuidProxy;
import qrom.component.wup.statics.StatProxy;
import TRom.RomBaseInfo;

/**
 * 构建RomBaseInfo, 同时
 * @author wileywang
 *
 */
public class RomBaseInfoBuilder {
	
	public RomBaseInfoBuilder() {
	}
	
	public RomBaseInfo build() {
		RomBaseInfo romBaseInfo = new RomBaseInfo();
		
		romBaseInfo.setVGUID(GuidProxy.get().getGuidBytes());
		romBaseInfo.setSQUA(BaseInfoManager.get().getQua());
		romBaseInfo.setSIMEI(BaseInfoManager.get().getImei());
		romBaseInfo.setSLC(BaseInfoManager.get().getLC());
		
		romBaseInfo.setSQIMEI(StatProxy.get().getQImei());
		if (StringUtil.isEmpty(romBaseInfo.getSQIMEI()) || 
				romBaseInfo.getSQIMEI().startsWith(BASEINFO_ERR_CODE.QIME_ERR_CODE_SUFF)) {
			romBaseInfo.setSQIMEI(BaseInfoManager.get().getImei());
			if (StringUtil.isEmpty(romBaseInfo.getSQIMEI())) {
				romBaseInfo.setSQIMEI(BASEINFO_ERR_CODE.QIME_REPORT_EMPTY_CODE);
			}
		}
		
		romBaseInfo.setSPackName(ContextHolder.getApplicationContextForSure().getPackageName());
		
		doBuild(romBaseInfo);
		return romBaseInfo;
	}
	
	protected void doBuild(RomBaseInfo romBaseInfo) {
	}
	
}
