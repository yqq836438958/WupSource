package qrom.component.wup.base;

/**
 *  环境类型的描述
 * @author wileywang
 *
 */
public enum RunEnvType {
	Gamma(1),  // 测试环境
	IDC(2);     // 正式环境
	
	private int mValue;
	RunEnvType(int value) {
		mValue = value;
	}
	public int value() {
		return mValue;
	}
	
	public static RunEnvType from(int value) {
		if (value == RunEnvType.Gamma.value()) {
			return RunEnvType.Gamma;
		} else if (value == RunEnvType.IDC.value()) {
			return RunEnvType.IDC;
		}
		return null;
	}
}
