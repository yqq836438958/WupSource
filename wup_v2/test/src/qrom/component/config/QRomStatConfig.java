package qrom.component.config;

import qrom.component.statistic.QRomStatBaseConfig;

public class QRomStatConfig extends QRomStatBaseConfig {

	@Override
	public String getAppPackageName() {
		return "com.tencent.qrom.wup.test";
	}

	@Override
	public String getAppVersion() {
		return "0.2";
	}
	
}