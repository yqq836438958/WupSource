package qrom.component.wup;

/**
 * wup请求返回的额外数据
 *   -- 目前仅有统计时常使用 （20131015）
 * @author sukeyli
 *
 */
public class QRomWupRspExtraData {

    public int mConnectTime = -1;
    public int mSendTime = -1;
    public int mGetDataTime = -1;
    public int mReqDataLen = -1;
    public int mRspDataLen = -1;
}
