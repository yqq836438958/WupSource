package qrom.component.wup.sysImpl;

import qrom.component.wup.aidl.IQRomWupService;
import qrom.component.wup.aidl.QRomWupService;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.utils.QWupLog;
import android.content.Context;
import android.os.IBinder;

public class QWupRomSysProxyerImpl {
    
    private String TAG = "QRomWupBinderProxyer";
    
    private boolean isInTest = false; 
    
    private static QWupRomSysProxyerImpl mInstance;
    private QRomWupService mWupServiceForRom;
    /**测试用底层wup 信息service对象代理 */
    private IQRomWupService mTestProxyWupService;
    
    public static QWupRomSysProxyerImpl getInstance() {
        if (mInstance == null) {
            mInstance = new QWupRomSysProxyerImpl();
        }
        
        return mInstance;
    }
    
    public void startUp() {
        
    }
    /**
     * framework层使用的获取wupService接口
     * @return
     */
    public QRomWupService getRomWupServiceInFramework(Context context) {
        // 初始化wup manager
        
        if (mWupServiceForRom == null) {
            mWupServiceForRom = new QRomWupService();
        }
        // 初始化
        QRomWupImplEngine.getInstance().initBase(context);
        QRomWupImplEngine.getInstance().getSdkInnerWupManger();
        return mWupServiceForRom;
    }
    
    public void registerWupRomSys(Context context) {
        // 初始化
        QRomWupImplEngine.getInstance().initBase(context);
        // 启动默认wup sdk
        QRomWupImplEngine.getInstance().getSdkInnerWupManger();
    }
    
    public void refreshRomWupServiceInFramework(Context context) {
        mWupServiceForRom = null;
    }
    
    /**
     * wup sdk中使用，获取系统framework层通讯接口
     * @param context
     * @return
     */
    public IQRomWupService getQRomBinderServiceForSdk(Context context) {
        
        if (isInTest && mTestProxyWupService != null) {
            return mTestProxyWupService;
        }
        IBinder binder = null;
        try {
            binder = (IBinder)context.getSystemService("qrom_framework_wup");
            if (binder == null) {
                QWupLog.trace(TAG, "getQRomBinderServiceForSdk -> qrom_framework_wup : getSystemService is null" );
                return null;
            }
            // 获取qrom系统的wup service
            IQRomWupService qRomWupService = IQRomWupService.Stub.asInterface(binder);
            return qRomWupService; 
        } catch (Exception e) {
            QWupLog.w(TAG, "getQRomBinderServiceForSdk", e);
            QWupLog.reportBinderUseErr("getBinder->" + e + " : " + e.getMessage());
        }
        return null;
    }
    

    public void setTestProxyWupService(IQRomWupService testProxy) {
        mTestProxyWupService = testProxy;        
    }

}
