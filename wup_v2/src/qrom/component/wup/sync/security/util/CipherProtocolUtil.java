package qrom.component.wup.sync.security.util;

import TRom.RomBaseInfo;
import TRom.SecPublicKeyReq;
import TRom.SecSessionReq;

public class CipherProtocolUtil {
    
    public static final String TAG = "CipherProtocolUtil";

    public static SecPublicKeyReq createGetPublicKeyRequest(RomBaseInfo romBaseInfo, String sApp,
            int iRootPubKeyVer) {
        SecPublicKeyReq keyReq = new SecPublicKeyReq();
        keyReq.stRomBaseInfo = romBaseInfo;
        keyReq.sApp = sApp;
        keyReq.iRootPubKeyVer = iRootPubKeyVer;

        return keyReq;
    }

    public static SecSessionReq createBeginSessionRequest(RomBaseInfo romBaseInfo, String sApp,
            byte[] vtGuid, int iAppPubKeyVer, byte[] vtEncryptedActiveKey) {
        SecSessionReq sessionReq = new SecSessionReq();
        sessionReq.stRomBaseInfo = romBaseInfo;
        sessionReq.vtGuid = romBaseInfo.vGUID;  // 为了兼容之前的版本而加上，如果不加则，createSecurityRequest会出现Null Pointer Exception(因为vtGuid为空)
        sessionReq.sApp = sApp;
        sessionReq.iAppPubKeyVer = iAppPubKeyVer;
        sessionReq.vtEncryptedActiveKey = vtEncryptedActiveKey;

        return sessionReq;
    }
}
