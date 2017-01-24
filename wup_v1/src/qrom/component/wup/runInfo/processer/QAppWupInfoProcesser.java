package qrom.component.wup.runInfo.processer;

import qrom.component.wup.utils.QWupFileUtil;
import qrom.component.wup.utils.QWupLog;


public class QAppWupInfoProcesser extends QWupActivedInfoProcesser {   

    
    public QAppWupInfoProcesser() {
        super(QWupFileUtil.FILE_USER_WUP_INFO_APP, 
                QWupFileUtil.SD_USER_WUP_INFO_APP);
    	TAG = "QAppWupInfoProcesser";
    }
        
    @Override
    public long getRomId() {
        
        // app独立模式下无romid 
        
        return 0;
    }
                     
    @Override
    protected void sendBroadcastForWupBaseDataUpdate(int reqType) {
       QWupLog.i(TAG, "sendBroadcastForWupBaseDataUpdate-> app 单发模式 取消通知, reqType = " + reqType);
    }
}
