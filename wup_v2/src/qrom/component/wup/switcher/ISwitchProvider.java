package qrom.component.wup.switcher;

/**
 * 开关的提供器
 * @author wileywang
 */
public interface ISwitchProvider {
	public boolean isAllClosed();
	
	public void setCloseAll(final boolean needClose);
	
	public void registerSwitchListener(ISwitchListener listener);
	public void unRegisterSwitchListener(ISwitchListener listener);
}
