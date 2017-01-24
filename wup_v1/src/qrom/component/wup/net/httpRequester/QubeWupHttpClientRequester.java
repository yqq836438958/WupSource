package qrom.component.wup.net.httpRequester;

import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.QRomWupRspExtraData;
import qrom.component.wup.net.QubeWupTask;
import qrom.component.wup.net.QubeWupTaskData;
import qrom.component.wup.net.base.HttpHeader;
import qrom.component.wup.net.base.QubeHttpPost;
import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;


public class QubeWupHttpClientRequester extends QubeWupHttpRequester{
	
    private static final String TAG = "QubeWupHttpClientTask";
	
    protected HttpPost mHttpPost;
    
    protected HttpParams mHttpParams;
    protected HttpClient mHttpClient;     

    public QubeWupHttpClientRequester(byte[] vGuid) {
    	super(vGuid);
    }
    
	protected ResponData doHttpRequest(QubeWupTaskData taskData) {

		QWupLog.trace(TAG, "doHttpRequest -> httpClient -- start url : " + taskData.mUrl 
		        + ", req timeout: " + taskData.mTimeout);
		
		mErrMsg = "";
		if (mHttpParams == null) {		    
		    mHttpParams = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(mHttpParams, (int) taskData.mTimeout);
		    HttpConnectionParams.setSoTimeout(mHttpParams, (int) taskData.mTimeout);
		}
		
		ResponData responData = null;
		try {

		    long startConnectTime = System.currentTimeMillis();
		    if (mHttpClient == null) {		        
		        // 新建HttpClient对象
		        mHttpClient = new DefaultHttpClient(mHttpParams);		        
		        mHttpPost = new QubeHttpPost(taskData.mUrl, mHttpClient.getParams()).openRequestConnect();
		        setRequestHeader(mHttpPost, taskData);
		    }
			
			taskData.mRspExtraData = new QRomWupRspExtraData();
            // 连接时间的差值
			taskData.mRspExtraData.mConnectTime = (int) (System.currentTimeMillis() - startConnectTime);
			// 请求数据长度
			taskData.mRspExtraData.mReqDataLen = taskData.mData == null ? 0 : taskData.mData.length;
			
			QWupLog.w(TAG, "doHttpRequest ->reqStart: mreqId = " + taskData.mReqId 
			        + ", reqDataLen = " + taskData.mRspExtraData.mReqDataLen  
			        + ", connectSubTime = " + taskData.mRspExtraData.mConnectTime );
			
			// 设置post http header
			// 封装数据
			mHttpPost.setEntity(new ByteArrayEntity(taskData.mData));
			
			long startSendTime = System.currentTimeMillis();
			HttpResponse response = mHttpClient.execute(mHttpPost);			
			// 发送请求及接收数据时间
			taskData.mRspExtraData.mSendTime =  (int) (System.currentTimeMillis() - startSendTime);
			QWupLog.w(TAG, "subSendTime = " + taskData.mRspExtraData.mSendTime);
			
			long startGetDatTime = System.currentTimeMillis();			
			responData = parseWupRespone(response);
			// 响应数据长度
			taskData.mRspExtraData.mRspDataLen = responData.mRspDatas == null ? 0 : responData.mRspDatas.length;
			// 获取数据时间
			taskData.mRspExtraData.mGetDataTime =  (int) (System.currentTimeMillis() - startGetDatTime);
			QWupLog.trace(TAG, "doHttpRequest ->reqEnd: mreqId = " + taskData.mReqId  
			        + ", rspDataLen = " + taskData.mRspExtraData.mRspDataLen
			        + ", subGetDataTime = " + taskData.mRspExtraData.mGetDataTime);
		}  catch (Exception e) {
			mErrCode = WUP_ERROR_CODE.WUP_TASK_ERR_EXCEPTION;
			mErrMsg = QWupStringUtil.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
			QWupLog.trace( TAG, e);
			// 为解决某些手机上有oom的异常，是手动触发gc
			checkOOM(e);
		}
		QWupLog.i(TAG, "doHttpRequest -> httpClient -- end");
		return responData;
	}
	    
	 
    /**
     * 设置http header
     * 
     * @param post
     */
    protected void setRequestHeader(HttpPost post, QubeWupTaskData taskData) {
        QWupLog.d(TAG, "setRequestHeader ");
        if (post == null) {
            return;
        }

        for (Entry<String, String> entry : DEFALUT_HEADER.entrySet()) {
            if (entry == null) {
                continue;
            }
            post.setHeader(entry.getKey(), entry.getValue());
        }

         /* wap下某些运营商需要添加该字段 */
		if (ApnStatInfo.M_APN_TYPE == ApnStatInfo.TYPE_WAP) { // 测试环境下不加host
			String host = QWupUrlUtil.parseUrlForHostPath(taskData.mUrl)[0];
			QWupLog.w(TAG, "X_ONLINE_HOST = " + host);
			post.setHeader(HttpHeader.REQ.X_ONLINE_HOST, host);
		} else {
			post.setHeader(HttpHeader.REQ.HOST, getProxyHost(taskData.mUrl));
		}

        if (taskData.mIsEncrpt) { // 需要加密
            post.setHeader(HttpHeader.REQ.QQ_S_ENCRYPT,
            		HttpHeader.WUP_HEADER_ENCRYPT_VALUE);
        }
    }

	    /**
		 * 解析wup返回数据
		 * @param response
		 * @return
		 * @throws Exception
		 */
		protected ResponData parseWupRespone(HttpResponse response) throws Exception {
			QWupLog.d(TAG, "parseWupRespone ");
			if (response == null) {
				return null;
			}
			
			ResponData responData = new ResponData();
			
			responData.mRspStatusCode = response.getStatusLine().getStatusCode();
			if (responData.mRspStatusCode == QubeWupTask.RESPONSE_OK) {
				responData.mRspDatas = EntityUtils.toByteArray(response.getEntity());
			}
			
			// 返回是否压缩  -- 标准压缩头  走wap的时候网关可能会主动压缩下
			Header header = response.getFirstHeader(HttpHeader.RSP.CONTENT_ENCODING);
	        if (header != null) {
	            responData.mRspContentEncoding = header.getValue();
	        }    
	     
	        // 自定义加密头
	        header = response.getFirstHeader(HttpHeader.RSP.QQ_S_ENCRYPT);
	        if (header != null) {
	        	responData.mRspQQEncrypt = header.getValue();
	        }
	        
	        // wup返回是否压缩 -- 自定义压缩头
	        header = response.getFirstHeader(HttpHeader.RSP.QQ_S_ZIP);
	        if (header != null) {
	        	responData.mRspQQZip = header.getValue();
	        }   
	        
	        // Content-Length
	        header = response.getFirstHeader(HttpHeader.RSP.CONTENT_LENGTH);
	        if (header != null) {
	        	responData.mRspContentLen = getLongValue(header.getValue());
	        }
	        return responData;
		}
		
	    /**
	     * 释放当前连接
	     */
	    public void cancelConnect() {
	        QWupLog.trace( TAG, " cancelConnect （超时强制取消线程） ");
	        try {
	            mHttpClient = null;
	            mHttpParams = null;
	            if (mHttpPost != null) {
	                QWupLog.d(TAG, "timeout cancelConnect");
	                QWupLog.trace( TAG, " abort （超时强制取消线程） ");
	                mHttpPost.abort();
	                mHttpPost = null;	                
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
