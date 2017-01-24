package qrom.component.wup.runInfo.processer;

import qrom.component.wup.QRomQuaFactory;
import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;

/**
 * 集成到TROM系统framework源码中的wup数据处理模式
 * @author sukeyli
 *
 */
public class QRomSysWupInfoProcesser extends QRomWupInfoProcesser {

    private QRomSysReceiver mReceiver;
    
    public QRomSysWupInfoProcesser() {
        super();
        TAG = "QRomSysWupInfoProcesser";
    }
    
    @Override
    public void startUp(Context context) {
        super.startUp(context);
        if (mReceiver == null) {
            mReceiver = new QRomSysReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_REQDATAS);
            context.registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public long getRomId() {
        return getRomIdFromTsf();
    }
    
    @Override
    public String getQua(Context context) {
        String qua = QRomQuaFactory.buildQua(context);
        return qua;
    }
    
    @Override
    protected boolean isRequestGuidByMainProcess(int reqType) {
        // TODO 通过aidl将请求转到主进程处理
        return false;
    }
    
    private void onProcessRspDatas(int reqType, String pkg) {
        QWupLog.i(TAG, "onProcessRspDatas-> reqType = " + reqType + ", pkg = " + pkg);
        Context context = getContext();
        if (context == null) {
            QWupLog.w(TAG, "onProcessRspDatas-> context null");
            return;
        }
        byte[] datas = null;
        byte[] extras= null;
        // iplist数据 针对请求app发送
        String rspAction = pkg + QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_IPDATAS;
        try {
            switch (reqType) {
            
            case WUP_DATA_TYPE.WUP_DATA_GUID:
                datas = getGUIDBytes();
                // guid 所有app都更新最新的guid
                rspAction = QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_RSPDATAS;
                break;
            case WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW:  // iplist
                datas = QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(
                        WUP_DATA_TYPE.WUP_DATA_IPLIST_NEW);
                extras =  QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(
                        WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI);
                break;
                
            case WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI:  // wifi iplist
                extras =  QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(
                        WUP_DATA_TYPE.WUP_DATA_IPLIST_WIFI);
                break;
                
            case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_NEW: // socket iplists
                datas = QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(
                        WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_NEW);
                extras =  QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(
                        WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI);
                break;
            case WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI:  // wifi socket
                extras =  QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(
                        WUP_DATA_TYPE.WUP_DATA_SOCKET_IPLIST_WIFI);
                break;

            default:
                break;
            }
            
            if (datas == null && extras == null) {
                QWupLog.w(TAG, "onProcessRspDatas-> not support Type");
                return;
            }
            QWupLog.i(TAG, "onProcessRspDatas-> action: " + rspAction);
            QWupLog.v(TAG, "onProcessRspDatas-> reqType = " + reqType
                    + ", datasLen = " + (datas == null ? -1 : datas.length)
                    + ", extraLen = " + (extras == null ? -1 : extras.length));
            Intent intent = new Intent(rspAction);
            intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, reqType);
            intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_PKG, pkg);
            intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_DATAS, datas);
            intent.putExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_EXTRA, extras);
            context.sendBroadcast(intent);
            
        } catch (Throwable e) {
           QWupLog.w(TAG, "onProcessRspDatas", e);
        }
    }
    
    @Override
    public boolean handleMessage(Message msg) {
       
        int what = msg.what;
        
        switch (what) {
        case MSG_WUP_ROMSYS_BROAD_REQ_WUPDATE:  // 更新wup数据
            int reqType = msg.arg1;
            String pkg = (String) msg.obj;
            onProcessRspDatas(reqType, pkg);
            break;

        default:
            break;
        }
        
        return  super.handleMessage(msg);
    }
    
    class QRomSysReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            if (QWupSdkConstants.ACTION_WUP_SDK_ROMSYS_REQDATAS.equals(action)) {  // app 请求数据
                // 请求数据类型
                int reqType = intent.getIntExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_TYPE, -1);
                // 请求包名
                String pkgName = intent.getStringExtra(QWupSdkConstants.ACTION_WUP_SDK_PARM_FLG_PKG);
                QWupLog.d(TAG, "QRomSysReceiver-> ACTION_WUP_SDK_ROMSYS_RSPDATAS: pkg = " 
                        + pkgName +", reqType = " + reqType);
                sendMsg(MSG_WUP_ROMSYS_BROAD_REQ_WUPDATE, reqType, pkgName, 0);
            }
        }
    }
}
