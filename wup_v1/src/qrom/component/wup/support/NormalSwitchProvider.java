package qrom.component.wup.support;

import qrom.component.wup.utils.ListenerList;

/**
 * 普通的内存开关
 * @author wileywang
 *
 */
public class NormalSwitchProvider extends ListenerList<ISwitchListener> implements ISwitchProvider {
	private boolean mIsAllClosed = false;
	
	public NormalSwitchProvider() {
	}
	
	@Override
	public boolean isAllClosed() {
		return mIsAllClosed;
	}

	@Override
	public void setCloseAll(boolean needClose) {
		if (isAllClosed() == needClose) {
			return ;
		}
		mIsAllClosed = needClose;
		notifyListeners();
	}

	@Override
	public void registerSwitchListener(ISwitchListener listener) {
		this.registerListener(listener);
	}

	@Override
	public void unRegisterSwitchListener(ISwitchListener listener) {
		this.registerListener(listener);
	}
	
	@Override
	protected void onNotifyListener(ISwitchListener listener, Object... params) {
		listener.onSwitchChanged();
	}
	
}
