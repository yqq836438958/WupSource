package qrom.component.wup.apiv2;

/**
 *  WUP暴露的状态相关的接口
 * @author wileywang
 *
 */
public interface IGuidListener {
	public void onGuidChanged(byte[] vGuid);
}
