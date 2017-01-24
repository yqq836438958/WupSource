package qrom.component.config;

import com.example.wuptest.TestConstants;
import qrom.component.wup.QRomWupBaseConfig;

public class QRomWupConfig extends QRomWupBaseConfig {

	@Override
	public String getAppPackageName() {
		return TestConstants.PACKAGE_NAME;
	}

	@Override
	public boolean isRunTestForced() {
	    // true:强制切换到测试环境；false:默认正式环境
	    // 这里建议用常量控制，发布时该变量一定要设置为false，切换到正式环境
		return TestConstants.IS_WUP_RUN_TEST;
	}

}
