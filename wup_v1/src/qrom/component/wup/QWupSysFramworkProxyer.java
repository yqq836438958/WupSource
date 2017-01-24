package qrom.component.wup;

import qrom.component.wup.aidl.QRomWupService;
import qrom.component.wup.sysImpl.QWupRomSysProxyerImpl;
import android.content.Context;

/**
 * wupsdk内置到framwork中，相关接口代理层
 *    -- 非framwork层逻辑，勿调用该类相关接口
 * @author sukeyli
 *
 */
public class QWupSysFramworkProxyer {
    /**
     * framework层使用的获取wupService接口
     * @return
     */
    public static QRomWupService getRomWupServiceInFramework(Context context) {
        return QWupRomSysProxyerImpl.getInstance().getRomWupServiceInFramework(context);
    }
}
