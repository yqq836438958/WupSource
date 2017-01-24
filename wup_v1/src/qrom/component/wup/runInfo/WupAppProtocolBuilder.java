package qrom.component.wup.runInfo;

import java.util.ArrayList;

import qrom.component.wup.QRomWupDataBuilder;

import TRom.IPListReq;
import TRom.LoginReq;
import TRom.RomBaseInfo;
import TRom.SecureReq;
import TRom.log.GetTicketReq;

import com.qq.jce.wup.UniPacket;

public class WupAppProtocolBuilder {

    //--------------------新协议报文----------------
    /**
     * 创建获取ip列表请求
     * @param guid
     * @param typeList
     * @param apnType  参考OPT.EAPNTYPE
     * @param apnName  apn描述信息
     * @param bAll           获取所有iplist
     * @return
     */
    public static UniPacket createIpListReqData(byte[] guid, ArrayList<Integer> typeList, 
            int apnType, int netType, boolean bAll) {
        IPListReq ipListReq = new IPListReq();
        ipListReq.vGUID = guid;
        ipListReq.vIPType = typeList;
        ipListReq.eApnType = apnType;
        ipListReq.eNetType = netType;
        ipListReq.bAll = bAll;
        
        return QRomWupDataBuilder.createReqUnipackageV3("tromlogin",
                "getIpList", "stIPListReq", ipListReq);
    }

    /**
     * 创建获取loginReq报文请求
     * @param baseInfo
     * @return
     */
    public static UniPacket createLoginReqData(RomBaseInfo baseInfo, String mac, String processName) {
        LoginReq loginReq = new LoginReq();
        loginReq.stBaseInfo = baseInfo;
        loginReq.sMac = mac;
//        loginReq.sProcess = processName;
        return QRomWupDataBuilder.createReqUnipackageV3("tromlogin",
                "login", "stLoginReq", loginReq);
    }
    
    
    public static UniPacket createAsymEncryptProxyReqData(String session, byte[] guid, String pkgName, byte[] realDatas, boolean isZip) {
        
        SecureReq secureReq = new SecureReq();
        secureReq.sSessionId = session;
        secureReq.vGUID = guid;
        secureReq.sPackageName = pkgName;
        secureReq.vRealReq = realDatas;
        secureReq.bZip = isZip;
        
        return QRomWupDataBuilder.createReqUnipackage("secprx", "secureCall", "stReq", secureReq);        
    }

    public static UniPacket creatLogSdkGetTicketReqData(RomBaseInfo baseInfo) {
        GetTicketReq getTicketReq = new GetTicketReq();
        getTicketReq.stRomBaseInfo = baseInfo;
        return QRomWupDataBuilder.createReqUnipackageV3("treport", "getTicket", "req", getTicketReq);        
    }
}
