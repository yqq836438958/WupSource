package qrom.component.config;

import qrom.component.statistic.QRomStatBaseConfig;

import com.example.wuptest.TestConstants;

public class QRomStatConfig extends QRomStatBaseConfig {

	@Override
	public String getAppPackageName() {
		/**		  
		 * 此处应返回当前使用统计sdk的应用的包名
		 */
		return TestConstants.PACKAGE_NAME;
	}


}
