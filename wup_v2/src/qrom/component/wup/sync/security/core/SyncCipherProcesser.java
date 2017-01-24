package qrom.component.wup.sync.security.core;

import java.security.PublicKey;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupCodecUtils;
import qrom.component.wup.apiv2.OutWrapper;
import qrom.component.wup.apiv2.RomBaseInfoBuilder;
import qrom.component.wup.apiv2.WupException;
import qrom.component.wup.apiv2.WupInterface;
import qrom.component.wup.apiv2.WupOption;
import qrom.component.wup.base.RunEnvType;
import qrom.component.wup.sync.security.StatisticListener;
import qrom.component.wup.sync.security.util.CipherKeyUtil;
import qrom.component.wup.sync.security.util.CipherProtocolUtil;
import qrom.component.wup.sync.security.util.RSAHelper;
import qrom.component.wup.sync.security.util.TEAHelper;
import TRom.RomBaseInfo;
import TRom.SecPublicKeyRsp;
import TRom.SecSessionRsp;
import TRom.SecurityStubAndroid;
import TRom.Securty_RetCode;
import android.content.Context;
import android.text.TextUtils;

public class SyncCipherProcesser {

    // Note: 这是hardcode形式的内置root公钥
    // 由openssl命令生成：
    // 1. openssl genrsa -out rsa_private_key.pem 2048
    // 2. openssl rsa -in rsa_private_key.pem -out rsa_public_key.pem -pubout
    // 正式环境key & version
    public static final String DEFAULT_ROOT_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAosgijveA5hjo3lWgXNn/" + "\r" +
                                   "0UB4eSkXhBtfxQYSqffYAHn2BrvljVQbxUKY5+uXopL3BD5Q+ySR/FGU8+iwbk/v" + "\r" + 
                                   "TrksbDTemkv8Tf94SGxmT1+wdsk+NAyoO3i31HVDOunxtbGB+yp/Yw82fAkRDCR1" + "\r" + 
                                   "OlXUGKawr4RXI91ovr5RmESMNtZ5MaxVCuR8eMmvtfpNnVHtu7tKs9gDwONTXh6h" + "\r" + 
                                   "sonjFtbj7WXS+Hw9a30qyuEF4EBz/nFjEkRMoOphh1asM7EA10dKkOfpzvURWqp3" + "\r" + 
                                   "I5Y3ZevEXKhi00yMt7JlfW51nzQF4jimLM+U2eX264j7JYiVxKvte4CN9edVi3sZ" + "\r" +
                                   "HQIDAQAB" + "\r";
    public static final int DEFAULT_ROOT_KEY_VERSION = 1;
    
    // 测试环境key & version
    public static final String DEFAULT_ROOT_KEY_TEST = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxw4lzGrh4OxSAw1QIKIi" + "\r" +
            "0sT+Mto0fDGNUsH2o9Z28qFCOHvzh2175ZJttbamACDZy9xKKgHOua4PIA1lrQSf" + "\r" + 
            "Pzs++Jn8RbiI0KTySN07Bs5PksLiKGh31qJhU9+cwwv8Uae+/297ITJl8Zfm5V5F" + "\r" + 
            "vGlX/YrnMKvBnNj75FhdsaAuVBMjuLUzx9ts40E/RsVlzdW4v3Onqs8KAGX4cpOG" + "\r" + 
            "QBN7RC9dvnQOjigLHowq+1MF+R0W8NYbX7kpgt5qCuFUvCctN5Ve3HfXnA4vUytq" + "\r" + 
            "CCvw1bXj/VvBTFdwiGe0s4J0gb5S5K8pLY95nQQ6AWZ/u1kUWzu9+ZYGgfpQGN5Y" + "\r" +
            "/QIDAQAB" + "\r";
    public static final int DEFAULT_ROOT_KEY_VERSION_TEST = 4;
    
    private static final String TAG = "SyncCipherProcesser";
    private static final String ROOT_STRING = "root";
    private static final String ROOT_VERSION_STRING = "root_version";
    private static final String DEFAULT_SIGN_ALGORITHM = "MD5withRSA";
    private static final String REG_PEM_PUBLIC_FILTER = "(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)";
//    private static final String REG_PEM_PRIVATE_FILTER = "(-+BEGIN RSA PRIVATE KEY-+\\r?\\n|-+END RSA PRIVATE KEY-+\\r?\\n?)";
    private static final int MAX_DO_LOOP_COUNT = 50;
    private static final String SERVANT_NAME = "romsecurity";
    
    private Context mContext;
    private RunEnvType mEnvType;
    private PublicKey mRootKey;
    private int mRootKeyVersion;
    private SecPublicKeyRsp mPublicKeyResponse;
    private PublicKey mAppPublicKey;
    private int mAppPublicKeyVersion = 1;
    private byte[] mTeaKey;
    private String mSessionId;
    private boolean mStartSucceed = false;
    private long mStartTimeout = 0;
    private long mStartTime = 0;
    private long mStartDuration = 0;
    private boolean mTrustLocalKey = true;
    private int mDoLoopCount = 0;
    private String mCustomPackageName;
    private StatisticListener mStatListener;
    private SecurityStubAndroid mSecurityStubAndroid;

    public enum State {
        Init, InitComplete, GetRootKey, GetRootKeyComplete, GetPublicKey, GetPublicKeyComplete,
        VerifyRootKey, VerifyRootKeyComplete, VerifyPublicKey, VerifyPublicKeyComplete, 
        GenCommKey, GenCommKeyComplete, BeginSession, BeginSessionComplete, None
    }
    private State mNextState = State.Init;
    
    public interface StateChangedListener {
        public void onStateChanged(State oldState, State newState);
    }
    private StateChangedListener mStateChangedListener;

    public SyncCipherProcesser(Context context, RunEnvType envType) {
        assert (context != null);
        assert (envType != null);
        mContext = context;
        mEnvType = envType;
        mSecurityStubAndroid = new SecurityStubAndroid(SERVANT_NAME);
    }
    
    public void setStateChangedListener(StateChangedListener listener) {
        mStateChangedListener = listener;
    }
    
    public void clearStateChangedListener() {
        mStateChangedListener = null;
    }
    
    public boolean isReady() {
        return mStartSucceed;
    }
    
    public byte[] encrypt(byte[] data) {
        if (!mStartSucceed) {
            throw new IllegalStateException("Bad state: start is not succeed");
        }
        return QRomWupCodecUtils.teaEncode(mTeaKey, data);
    }
    
    public byte[] decrypt(byte[] data) {
        if (!mStartSucceed) {
            throw new IllegalStateException("Bad state: start is not succeed");
        }
        return QRomWupCodecUtils.teaDecode(mTeaKey, data);
    }
    
    public String getSessionId() {
        return mSessionId;
    }
    
    /**
     * 设置start的超时时间，单位：毫秒
     * @param timeout (0表示没有超时)
     */
    public void setTimeout(long timeout) {
        if (timeout < 0)
            return;
        mStartTimeout = timeout;
    }
    
    public void setPackageName(String packageName) {
        mCustomPackageName = packageName;
    }
    
    public String getPackageName() {
        if (TextUtils.isEmpty(mCustomPackageName)) {
            return "sync";  // 为兼容同步模块
        }
        return mCustomPackageName;
    }
    
    public void setStatisticListener(StatisticListener statListener) {
        mStatListener = statListener;
    }
    
    public boolean start() {
        mDoLoopCount = 0;
        mNextState = State.Init;
        boolean rv = false;
        do {
            State state = mNextState;
            mNextState = State.None;
            switch (state) {
                case Init:
                    rv = doInit();
                    break;
                case InitComplete:
                    rv = doInitComplete();
                    break;
                case GetRootKey:
                    rv = doGetRootKey();
                    break;
                case GetRootKeyComplete:
                    rv = doGetRootKeyCompelte();
                    break;
                case VerifyRootKey:
                    rv = doVerifyRootKey();
                    break;
                case VerifyRootKeyComplete:
                    rv = doVerifyRootKeyComplete();
                    break;
                case GetPublicKey:
                    rv = doGetPublicKey();
                    break;
                case GetPublicKeyComplete:
                    rv = doGetPublicKeyComplete();
                    break;
                case VerifyPublicKey:
                    rv = doVerifyPublicKey();
                    break;
                case VerifyPublicKeyComplete:
                    rv = doVerifyPublicKeyComplete();
                    break;
                case GenCommKey:
                    rv = doGenCommKey();
                    break;
                case GenCommKeyComplete:
                    rv = doGenCommKeyComplete();
                    break;
                case BeginSession:
                    rv = doBeginSession();
                    break;
                case BeginSessionComplete:
                    rv = doBeginSessionComplete();
                    break;
                default:
                    throw new IllegalStateException("No reached state");
            }
            if (mStateChangedListener != null) {
                mStateChangedListener.onStateChanged(state, mNextState);
            }
            mStartDuration = System.currentTimeMillis() - mStartTime;
            if (++mDoLoopCount > MAX_DO_LOOP_COUNT) {
                QRomLog.e(TAG, "start: mDoLoopCount over " + MAX_DO_LOOP_COUNT + ", current state is " + mNextState);
                return false;
            }
        } while (rv != false && mNextState != State.None && (mStartTimeout == 0 || mStartDuration < mStartTimeout));
        
        if (mStartTimeout != 0 && mStartDuration >= mStartTimeout) {
            QRomLog.e(TAG, "start failed: timeout(" + mStartTimeout + ")");
        }
        
        return mStartSucceed;
    }

    private boolean doInit() {
        mStartSucceed = false;
        boolean result = false;
        try {
            if (mTrustLocalKey && CipherKeyUtil.isKeyExist(mContext, ROOT_STRING)) {
                mRootKey = CipherKeyUtil.loadPublicKey(mContext, ROOT_STRING);
                mRootKeyVersion = CipherKeyUtil.loadKeyVersion(mContext, ROOT_VERSION_STRING);
            } 
            
            if (!mTrustLocalKey 
                    || mRootKey == null
                    || mRootKeyVersion < 0) { // 本地没有保存root key || 从本地生成root key失败
            	if (mEnvType == RunEnvType.Gamma) {
                    QRomLog.d(TAG, "doInit use default root key(test): "
                            + DEFAULT_ROOT_KEY_TEST + ", version: "
                            + DEFAULT_ROOT_KEY_VERSION_TEST);
                    mRootKey = RSAHelper.getPublicKey(DEFAULT_ROOT_KEY_TEST);
                    mRootKeyVersion = DEFAULT_ROOT_KEY_VERSION_TEST;
                } else {
                    QRomLog.d(TAG, "doInit use default root key: "
                            + DEFAULT_ROOT_KEY + ", version: "
                            + DEFAULT_ROOT_KEY_VERSION);
                    mRootKey = RSAHelper.getPublicKey(DEFAULT_ROOT_KEY);
                    mRootKeyVersion = DEFAULT_ROOT_KEY_VERSION;
                }
            }
            
            mNextState = State.InitComplete;
            mStartTime = System.currentTimeMillis();
            mStartDuration = 0;
            result = true;
        } catch (Exception e) {
            QRomLog.e(TAG, e);
        }
        
        return result;
    }

    private boolean doInitComplete() {
        mNextState = State.GetPublicKey;
        if (mTrustLocalKey && CipherKeyUtil.isKeyExist(mContext, getAppPublicKeyString())) {
            mAppPublicKey = CipherKeyUtil.loadPublicKey(mContext, getAppPublicKeyString());
            mAppPublicKeyVersion = CipherKeyUtil.loadKeyVersion(mContext, getAppPublicKeyVersionString());
            if (mAppPublicKey != null && mAppPublicKeyVersion > 0) {
                mNextState = State.GenCommKey;
            }
        }
        return true;
    }
    
    private boolean doGetRootKey() {
        OutWrapper<SecPublicKeyRsp> outRsp = new OutWrapper<SecPublicKeyRsp>();
        try {
        	WupOption wupOption = new WupOption(WupOption.WupType.WUP_NORMAL_REQUEST);
        	wupOption.setRequestEnvType(mEnvType);
        	
            int retType = mSecurityStubAndroid.getPublicKey(CipherProtocolUtil.createGetPublicKeyRequest(
                    getRomBaseInfo(), "root", mRootKeyVersion), outRsp, wupOption);
            if (retType != Securty_RetCode._SEC_RC_OK) {
                QRomLog.e(TAG, "update root key error, code="+ retType + ", version=" + mRootKeyVersion);
                return reset();
            }
        } catch (WupException e) {
            QRomLog.e(TAG, "doGetRootKey: exception errorCode=" + e.getErrorCode()
                    + ", errorMsg=" + e.getErrorMsg());
            return false;
        }
        
        mPublicKeyResponse = outRsp.getOut();
        if (mPublicKeyResponse == null) {
            QRomLog.d(TAG, "parse root key response error");
            return false;
        }
        
        mNextState = State.GetRootKeyComplete;
        return true;
    }
    
    private boolean doGetRootKeyCompelte() {
        mNextState = State.VerifyRootKey;
        return true;
    }
    
    private boolean doVerifyRootKey() {
        String rootKeyString = mPublicKeyResponse.sPublicKey;
        if (rootKeyString == null) {
            return false;
        }
        
        boolean result = false;
        rootKeyString = rootKeyString.replaceAll(REG_PEM_PUBLIC_FILTER, "");
        try {
            if (RSAHelper.verifySignature(mPublicKeyResponse.sPublicKey.getBytes(), mRootKey,
                    DEFAULT_SIGN_ALGORITHM, mPublicKeyResponse.vtMd5Signature)) {
                mRootKey = RSAHelper.getPublicKey(rootKeyString);
                mRootKeyVersion = mPublicKeyResponse.iPubKeyVer;
                mNextState = State.VerifyRootKeyComplete;
                result = true;
                CipherKeyUtil.storeKey(mContext, ROOT_STRING, mRootKey);
                CipherKeyUtil.storeKeyVersion(mContext, ROOT_VERSION_STRING, mRootKeyVersion);
                if (mStatListener != null) {
                    mStatListener.onVerifyRootKeyResult(getPackageName(), true);
                }
            } else {
                QRomLog.e(TAG, "verify root key failed, get rootKeyString="
                        + rootKeyString + ", version="
                        + mPublicKeyResponse.iPubKeyVer);
                if (mStatListener != null) {
                    mStatListener.onVerifyRootKeyResult(getPackageName(), false);
                }
                result = reset();
            }
        } catch (Exception e) {
            QRomLog.e(TAG, e);
        }
        
        return result;
    }
    
    private boolean doVerifyRootKeyComplete() {
        mNextState = State.GetPublicKey;    // 根公钥更新后，重新更新应用公钥
        return true;
    }
    
    private boolean doGetPublicKey() {
        OutWrapper<SecPublicKeyRsp> outRsp = new OutWrapper<SecPublicKeyRsp>();
        try {
        	WupOption wupOption = new WupOption(WupOption.WupType.WUP_NORMAL_REQUEST);
        	wupOption.setRequestEnvType(mEnvType);
        	
            int retType = mSecurityStubAndroid.getPublicKey(CipherProtocolUtil.createGetPublicKeyRequest(
                    getRomBaseInfo(), getPackageName(), mRootKeyVersion), outRsp, wupOption);
            if (retType == Securty_RetCode._SEC_RC_ROOT_PUBKEY_EXPIRED) {
                QRomLog.w(TAG, "get public key error: root key expired, mRootKeyVersion=" + mRootKeyVersion);
                mNextState = State.GetRootKey;
                return true;
            } else if (retType != Securty_RetCode._SEC_RC_OK) {
                QRomLog.e(TAG, "get public key error, code="+ retType);
                return false;
            }
        } catch (WupException e) {
            QRomLog.e(TAG, "doGetPublicKey: exception errorCode=" + e.getErrorCode()
                    + ", errorMsg=" + e.getErrorMsg());
            return false;
        }
        
        mPublicKeyResponse = outRsp.getOut();
        if (mPublicKeyResponse == null) {
            QRomLog.d(TAG, "parse public key response error");
            return false;
        }
        
        mNextState = State.GetPublicKeyComplete;
        return true;
    }
    
    private boolean doGetPublicKeyComplete() {
        mNextState = State.VerifyPublicKey;
        return true;
    }
    
    private boolean doVerifyPublicKey() {
        String appPublicKeyString = mPublicKeyResponse.sPublicKey;
        if (appPublicKeyString == null) {
            return false;
        }
        
        boolean result = false;
        appPublicKeyString = appPublicKeyString.replaceAll(REG_PEM_PUBLIC_FILTER, "");
        try {
            if (RSAHelper.verifySignature(mPublicKeyResponse.sPublicKey.getBytes(), mRootKey,
                    DEFAULT_SIGN_ALGORITHM, mPublicKeyResponse.vtMd5Signature)) {
                mAppPublicKey = RSAHelper.getPublicKey(appPublicKeyString);
                mAppPublicKeyVersion = mPublicKeyResponse.iPubKeyVer;
                mNextState = State.VerifyPublicKeyComplete;
                result = true;
                if (mStatListener != null) {
                    mStatListener.onVerfiyAppKeyResult(getPackageName(), true);
                }
            } else {
                QRomLog.e(TAG, "verify public key failed");
                if (mStatListener != null) {
                    mStatListener.onVerfiyAppKeyResult(getPackageName(), false);
                }
                result = reset();
            }
        } catch (Exception e) {
            QRomLog.e(TAG, e);
        }
        
        return result;
    }
    
    private boolean doVerifyPublicKeyComplete() {
        CipherKeyUtil.storeKey(mContext, getAppPublicKeyString(), mAppPublicKey);
        CipherKeyUtil.storeKeyVersion(mContext, getAppPublicKeyVersionString(), mAppPublicKeyVersion);
        mNextState = State.GenCommKey;
        return true;
    }
    
    private boolean doGenCommKey() {
        boolean result = false;
        mTeaKey = TEAHelper.getRandomKey(16);
        if (mTeaKey != null && mTeaKey.length == 16) {
            result = true;
            mNextState = State.GenCommKeyComplete;
        }
        return result;
    }
    
    private boolean doGenCommKeyComplete() {
        mNextState = State.BeginSession;
        return true;
    }
    
    private boolean doBeginSession() {
        byte[] guid = null;
        if (getRomBaseInfo() == null) {
            QRomLog.e(TAG, "begin session: getRomBaseInfo is null");
            return false;
        }
        guid = WupInterface.getGuidBytes();
        if (guid == null) {
            QRomLog.e(TAG, "begin session: guid is null");
            return false;
        }
        
        OutWrapper<SecSessionRsp> outRsp = new OutWrapper<SecSessionRsp>();
        try {
        	WupOption wupOption = new WupOption(WupOption.WupType.WUP_NORMAL_REQUEST);
        	wupOption.setRequestEnvType(mEnvType);
        	
            int retType = mSecurityStubAndroid.beginSession(CipherProtocolUtil
                    .createBeginSessionRequest(getRomBaseInfo(), getPackageName(),
                            guid, mAppPublicKeyVersion,
                            RSAHelper.encrypt(mTeaKey, mAppPublicKey)), outRsp, wupOption);
            if (retType == Securty_RetCode._SEC_RC_APP_PUBKEY_EXPIRED) {
                QRomLog.w(TAG, "begin session: app public key expired");
                mNextState = State.GetPublicKey;
                return true;
            } else if (retType != Securty_RetCode._SEC_RC_OK) {
                QRomLog.e(TAG, "begin session failed, code="+ retType);
                return reset();
            }
        } catch (WupException e) {
            QRomLog.e(TAG, "doBeginSession: exception errorCode=" + e.getErrorCode()
                    + ", errorMsg=" + e.getErrorMsg());
            return false;
        }
        
        SecSessionRsp sessionRsp = outRsp.getOut();
        if (sessionRsp == null) {
            QRomLog.e(TAG, "parse begin session response failed");
            return false;
        }
        if (TextUtils.isEmpty(sessionRsp.sSessionId)) {
            QRomLog.e(TAG, "begin session reponse session id id null");
            return false;
        }
        mSessionId = sessionRsp.sSessionId;
        mNextState = State.BeginSessionComplete;
        return true;
    }
    
    private boolean doBeginSessionComplete() {
        mStartSucceed = true;
        return true;
    }
    
    private boolean reset() {
        QRomLog.w(TAG, "reset, before reset current rootKey=" + mRootKey
                    + ", rootKeyVersion=" + mRootKeyVersion
                    + ", publicKey=" + mAppPublicKey
                    + ", publicKeyVersion" + mAppPublicKeyVersion);
        CipherKeyUtil.removeKey(mContext,  ROOT_STRING);
        CipherKeyUtil.removeKey(mContext, ROOT_VERSION_STRING);
        CipherKeyUtil.removeKey(mContext, getAppPublicKeyString());
        CipherKeyUtil.removeKey(mContext, getAppPublicKeyVersionString());
        mNextState = State.Init;
        mTrustLocalKey = false;
        return true;
    }
    
    private RomBaseInfo getRomBaseInfo() {
        return new RomBaseInfoBuilder().build();
    }
    
    private String getAppPublicKeyString() {
        return "app_public_key_" + getPackageName();
    }
    
    private String getAppPublicKeyVersionString() {
        return "app_public_key_version_" + getPackageName();
    }
}
