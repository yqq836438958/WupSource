package qrom.component.wup;


public interface IQubeWupNotifyCallback {
    /**
     * wup请求完成--数据接收成功
     * 
     * @param fromModelType
     * @param reqId
     * @param operType
     * @param wupReqExtraData
     * @param wupRspExtraData
     * @param serviceName
     * @param response
     */
    public void onReceiveAllData(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, byte[] response);
    
    /**
     * wup请求失败 -- 数据接收失败
     * 
     * @param fromModelType
     * @param reqId
     * @param operType
     * @param wupReqExtraData
     * @param wupRspExtraData
     * @param serviceName
     * @param errorCode
     * @param description
     */
    public void onReceiveError(int fromModelType, int reqId, int operType,
            QRomWupReqExtraData wupReqExtraData, 
            QRomWupRspExtraData wupRspExtraData, String serviceName, int errorCode, String description);
       
    /**
     * 同步新guid 
     * 
     * @param vGuid
     */
    public void onGuidChanged(byte[] vGuid);
    
    /**
     * 更新RomBaseInfo基本协议
     * @param baseInfo RomBaseInfo
     */
//    public void updataBaseInfo(RomBaseInfo baseInfo);
    
    
    /**
     * 通知数据改变
     * 
     * @param dataFlg 数据类型
     */
    public void sendWupDataChangeMsg(int dataFlg);
    
    /**
     * wup模块发送错误
     * @param e
     * @param desc
     */
    public void onWupErrCatched(int type, Throwable e, String desc);    

}
