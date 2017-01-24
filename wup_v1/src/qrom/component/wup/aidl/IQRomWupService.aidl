package qrom.component.wup.aidl;

interface IQRomWupService {

/*
*根据数据类型获取对应数据
*/
byte[] getGuid();

/*
*根据数据类型获取对应数据
*/
byte[] getWupDataByType(int type);

/*
*发送login请求
*/
int doLogin();

/*
*发送获取iplist请求
*/
int doSendIpList();

}