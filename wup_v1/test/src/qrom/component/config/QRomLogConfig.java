package qrom.component.config;


import qrom.component.log.QRomLogBaseConfig;

import com.example.wuptest.TestConstants;

/**
 * Title: QRomLogConfig
 * Package: qrom.component.log
 * Author: interzhang
 * Date: 14-3-18 下午3:57
 * Version: v1.0
 */
public class QRomLogConfig extends QRomLogBaseConfig {

	@Override
	public int getLogMode() {
		// TODO Auto-generated method stub
		return QRomLogBaseConfig.LOG_BOTH;
	}

	@Override
	public String getPackageName() {
		// TODO Auto-generated method stub
		return TestConstants.PACKAGE_NAME;
	}

}
