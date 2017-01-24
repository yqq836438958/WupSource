package qrom.component.config;


import qrom.component.wup.QRomWupBaseConfig;

public class QRomWupConfig extends QRomWupBaseConfig {

	@Override
	public String getAppPackageName() {
		return "com.tencent.qrom.wup.test";
	}

	@Override
	public boolean isRunTestForced() {
		return true;
	}
	
}
