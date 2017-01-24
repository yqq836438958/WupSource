package qrom.component.wup.stat;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import qrom.component.wup.net.QRomWupStatUtils;
import qrom.component.wup.net.base.QubeHttpPost;
import qrom.component.wup.runInfo.QRomWupSharedpreferences;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;
import android.content.Context;

public class QRomNetCheck {

    private String TAG = "====QRomNetCheckWup";
    
    /** 默认检测的url 地址 */
    private static final String URL_CHECK = "http://3gimg.qq.com/trom_s/test/t.png";
    /** 默认检测url的flg */
    private static final String URL_CHECK_FLG = "u0"; 
    
    /** 检测标识前缀*/
    private static final String URL_PREFIX ="u";
    /** 检测标识前缀*/
    private static final String CHECKCNT_PREFIX ="t";
    /** 停止检测 */
    private static final String CHECK_STOP ="stop";
    /** 数据请求item分隔符 */
    private static final String CHECK_ITEM_SEPERATOR ="|";
    /** 数据分割符 -- 由于|是正则特殊字符，这里转意下*/
    private static final String CHECK_ITEM_FILTER ="\\|";
    /** 数据数值分隔符 */
    private static final String CHECK_VALUE_SEPERATOR =":";
    
    /** 一天请求的最大次数 */
    private static final int MAX_CHECK_CNT = 15;
    /** 下发一天请求的最大次数的上限 */
    private static final int CHECK_CNT_LIMIT = 50;
    /** 请求最小间隔*/
    private static final int REQUEST_MIN_INTERVAL = 5 * 60 * 1000;
//    private static final int REQUEST_MIN_INTERVAL = 10 * 1000;
    
    
    /** 返回数据为空 */
    private static final int SUCESS_CODE = 0;
    /** 返回数据为空 */
    private static final int ERR_CODE_RSP_DATAEMPTY = -102;
    /** 服务器返回非 200 */
    private static final int ERR_CODE_RSP_CODEERR = -103;
    /** 其他异常  */
    private static final int ERR_CODE_RSP_EXCEPTION = -105;
    /** 其他异常  */
    private static final int ERR_CODE_RSP_DATAERR = -196;
    
    private static String mUrl = null;
    private static String mFlg = null;
    private static int mMaxCnt = -1;
    private String mErrMsg = null;
    private int mErrCode = 0;
    
    public String checkServiceStat(Context context) {
        
        String checkStat = QRomWupSharedpreferences.getCheckFailStat(context);
        if (!QWupStringUtil.isEmpty(checkStat) && CHECK_STOP.equals(checkStat)) {  // 已关闭校验功能
            QWupLog.trace(TAG, "checkHttpServiceStat -> check wup fail stat is stop! ");
            return null;
        }
        String net = QRomWupStatUtils.getCurStatNetType(context);
        
        if (!isNeedSendCheckRequest(context)) {  // 不需要发送验证
            QWupLog.trace(TAG, "checkHttpServiceStat -> no need to send, cancel request! ");
            return null;
        }
        
        // 请求新服务器
        requestCheckService(context);
        if (mErrMsg != null) {
            mErrMsg.replace("_", "-");
        }
        
        if (mFlg != null) {
            mFlg.replace("_", "-");
        }
        
        String info = mErrCode + "_"  + mFlg +"_"+ mErrMsg + "_" + net;
        QWupLog.trace(TAG, "checkHttpServiceStat -> stat info: " + info);
        return info;
    }
    
    /**
     * 是否需要上报检测数据
     * @param context
     * @return
     */
    private boolean isNeedSendCheckRequest(Context context) {
        // 上次发送时间
        long lastSendCheckTime = QRomWupSharedpreferences.getCheckStatTime(context);
        long curTime = System.currentTimeMillis();
        // 上次发送的时间差
        long subTime = curTime - lastSendCheckTime;                
        if (subTime < REQUEST_MIN_INTERVAL && subTime > -REQUEST_MIN_INTERVAL) {
            QWupLog.trace(TAG, "checkHttpServiceStat ->isNeedSendCheckRequest :  send time not ok ");
            return false;
        }  
     
        // 初始化最大次数限制
        mMaxCnt = QRomWupSharedpreferences.getCheckStatMaxCnt(context);
        if (mMaxCnt <= 0) {  // 最大发送次数无效，使用默认次数
            mMaxCnt = MAX_CHECK_CNT;
        } else if (mMaxCnt > CHECK_CNT_LIMIT) {  // 超过指定最大上限，使用默认上限
            mMaxCnt = CHECK_CNT_LIMIT;
        }
        String lastDay = QWupStringUtil.getDateForYYYYMMDD(lastSendCheckTime);
        String curDay =  QWupStringUtil.getDateForYYYYMMDD(curTime);
        if (!lastDay.equals(curDay)) {  // 跨一个周期，计算器置0
            QRomWupSharedpreferences.setCheckStatCnt(context, 0);
            QWupLog.trace(TAG, "checkHttpServiceStat ->isNeedSendCheckRequest :  reset max cnt ");
        }
        // 发送的次数
        int sendCnt = QRomWupSharedpreferences.getCheckStatCnt(context);
        QWupLog.trace(TAG, "checkHttpServiceStat ->isNeedSendCheckRequest :  maxcnt = " + mMaxCnt + ", sendCnt = " + sendCnt);
        if (sendCnt >= mMaxCnt) {
            QWupLog.trace(TAG, "checkHttpServiceStat ->isNeedSendCheckRequest :  send cnt >= max!");
            return false;
        }
        
        return true;        
    }
    
    /**
     * 请求检测服务器相关http状态
     * 
     * @return String 返回应统计上报的信息
     */
    private void requestCheckService(Context context) {
        
        try {
            mUrl = QWupUrlUtil.resolvValidUrl(mUrl);
            if (QWupStringUtil.isEmpty(mUrl)) {  // 检测默认url
                mUrl = URL_CHECK;
                mFlg = URL_CHECK_FLG;
            }
            // 增加发送计数
            int sendCnt = QRomWupSharedpreferences.getCheckStatCnt(context);
            QRomWupSharedpreferences.setCheckStatCnt(context, sendCnt+1);
            // 更新发送时间
            QRomWupSharedpreferences.setCheckStatTime(context, System.currentTimeMillis());
            
            // 开始发送请求
            BasicHttpParams mHttpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(mHttpParams, 5000);
            HttpConnectionParams.setSoTimeout(mHttpParams, 5000);
            
            // 新建HttpClient对象
            DefaultHttpClient mHttpClient = new DefaultHttpClient(mHttpParams);               
            HttpPost  mHttpPost = new QubeHttpPost(mUrl, mHttpClient.getParams()).openRequestConnect();
            HttpResponse response = mHttpClient.execute(mHttpPost);     
            int rspCode = response.getStatusLine().getStatusCode();
            if (rspCode != 200) {  // 返回非200返回码
                mErrCode = ERR_CODE_RSP_CODEERR;
                mErrMsg = "rspCode: " + rspCode;
                return;
            }
            
            byte[] rspDatas= EntityUtils.toByteArray(response.getEntity());
            if (rspDatas == null || rspDatas.length == 0) {
                mErrCode = ERR_CODE_RSP_DATAEMPTY;
                mErrMsg = "rspdata is empty";
                return;
            }
            
            String rspCheckInfo = new String(rspDatas);
//            String rspCheckInfo = "u:3gimg.qq.com/trom_s/test/t.png";
            String[] checkItems;
            String rspStr = null;
            String checkCntStr = null;
            QWupLog.trace(TAG, "requestCheckService -> rspCheckInfo: " + rspCheckInfo);
            if (rspCheckInfo.contains(CHECK_ITEM_SEPERATOR)) { // 多个item 被'|'分割
                QWupLog.trace(TAG, "requestCheckService -> rsp has more item ");
                checkItems = rspCheckInfo.split(CHECK_ITEM_FILTER);
                if (checkItems == null || checkItems.length == 0) {  // 数据格式错误
                    mErrCode = ERR_CODE_RSP_DATAERR;
                    mErrMsg = "rspdata checkinfo items >1 is err : ";
                    return;
                }
                int size = checkItems.length;
                QWupLog.trace(TAG, "requestCheckService -> checkItems size: " + size);
                for (int i = 0; i < size; i++) {
                    if (QWupStringUtil.isEmpty(checkItems[i])) {
                        continue;
                    }
                    if (checkItems[i].startsWith(URL_PREFIX)) {  // 找到url的前缀
                        rspStr = checkItems[i];
                        continue;
                    }
                    if (checkItems[i].startsWith(CHECKCNT_PREFIX)) {  
                        checkCntStr = checkItems[i];
                        continue;
                    }
                }
            } else {
                rspStr = rspCheckInfo;
            }

            String[] infos = rspStr == null ? null : rspStr.split(CHECK_VALUE_SEPERATOR);
            if (infos == null || infos.length < 2) {  // 数据格式错误
                mErrCode = ERR_CODE_RSP_DATAERR;
                mErrMsg = "rspdata format is err : " + rspStr;
                return;
            }
            
            String flg = infos[0] == null ? "na" : infos[0];
            String rspInfo = infos[1];
            if (rspInfo != null && CHECK_STOP.equalsIgnoreCase(rspInfo.trim())) {  // 停止check上报
                QRomWupSharedpreferences.setCheckFailStat(context, CHECK_STOP);
                mUrl = null;
                mFlg =  flg;
                mErrCode = SUCESS_CODE;
                mErrMsg = "stop";
                return;
            } 
            
            rspInfo = QWupUrlUtil.resolvValidUrl(infos[1]);
            if (QWupStringUtil.isEmpty(flg) || QWupStringUtil.isEmpty(rspInfo) 
                    || !flg.startsWith(URL_PREFIX) ) {  // 返回url有问题,为空或前缀不是约定格式
                mErrCode = ERR_CODE_RSP_DATAERR;
                mErrMsg = "rspdata is not url, info : " +  rspStr;
                return;
            }
            
            mUrl = rspInfo;
            mFlg = flg;
            mErrCode = SUCESS_CODE;
            mErrMsg = "ok";            
            QWupLog.trace(TAG, "requestCheckService -> sucess new murl: " + mUrl + ", flg: " + mFlg);
            
            // 解析最大次数的信息
            if (!QWupStringUtil.isEmpty(checkCntStr)) {
                QWupLog.trace(TAG, "requestCheckService -> rsp has cnt info.");
                // 最大次数参数不为空
                String[] cntInfo = checkCntStr.split(CHECK_VALUE_SEPERATOR);
                if (cntInfo != null && cntInfo.length == 2) {
                    String cntStr = cntInfo[1] == null ? "" : cntInfo[1].trim();
                    if (!QWupStringUtil.isEmpty(cntStr)) {
                        mMaxCnt = Integer.valueOf(cntInfo[1], 10);
                        if (mMaxCnt > CHECK_CNT_LIMIT) {  // 大于最大限制
                            mMaxCnt = CHECK_CNT_LIMIT;
                        }
                        QRomWupSharedpreferences.setCheckStatMaxCnt(context, mMaxCnt);
                        QWupLog.trace(TAG, "requestCheckService -> max cnt: " + mMaxCnt);
                    }
                } else {
                    QWupLog.trace(TAG, "requestCheckService -> cnt info is err ");
                }
            }
        
            
        } catch (Throwable e) {
            mErrCode = ERR_CODE_RSP_EXCEPTION;
            String msg = e.getMessage();
            mErrMsg = QWupStringUtil.isEmpty(msg) ? "err:" + e.toString() : msg;
        }
    }
}
