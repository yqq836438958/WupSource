package qrom.component.wup.aidl;

import qrom.component.wup.QRomWupConstants.WUP_DATA_TYPE;
import qrom.component.wup.runInfo.QRomWupImplEngine;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupSdkConstants.IPLIST_REQ_TYPE;
import android.os.RemoteException;

public class QRomWupService extends IQRomWupService.Stub {

    @Override
    public byte[] getGuid() throws RemoteException {
        return getWupDataByType(WUP_DATA_TYPE.WUP_DATA_GUID);
    }
    
    @Override
    public byte[] getWupDataByType(int type) throws RemoteException {
        byte[] rspDatas = QRomWupImplEngine.getInstance().getWupRunTimeManager().getWupDataForAidl(type);
        QWupLog.i("QRomWupService", "getWupDataByType-> data len = " + (rspDatas == null ? -1 : rspDatas.length));
        return rspDatas;
    }

    @Override
    public int doLogin() throws RemoteException {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager().doLogin();
    }

    @Override
    public int doSendIpList() throws RemoteException {
        return QRomWupImplEngine.getInstance().getWupRunTimeManager()
                .requestIpList(IPLIST_REQ_TYPE.IPLIST_REQ_NORMAL);
    }

}
