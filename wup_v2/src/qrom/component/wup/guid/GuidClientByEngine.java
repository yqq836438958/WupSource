package qrom.component.wup.guid;

/**
 * 直接可以获取GuidEngine实例才能使用
 * @author wileywang
 *
 */
class GuidClientByEngine implements IGuidClient {

	@Override
	public byte[] getGuidBytes() {
		return GuidEngine.get().getGuidBytes();
	}

	@Override
	public void release() {
	}

	@Override
	public int requestLogin() {
		return GuidEngine.get().requestLogin();
	}

}
