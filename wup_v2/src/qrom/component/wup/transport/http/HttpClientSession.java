package qrom.component.wup.transport.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import qrom.component.log.QRomLog;
import qrom.component.wup.QRomWupConstants.WUP_ERROR_CODE;
import qrom.component.wup.base.net.ApnType;
import qrom.component.wup.base.net.ConnectInfo;
import qrom.component.wup.base.net.NetType;
import qrom.component.wup.base.utils.StringUtil;
import qrom.component.wup.base.utils.TEACoding;
import qrom.component.wup.base.utils.ZipUtils;

/**
 *  使用HttpClient实现的HttpSession
 * @author wileywang
 *
 */
public class HttpClientSession extends HttpSession {
	private static final String TAG = HttpClientSession.class.getSimpleName();
	
	private HttpPost mHttpPost;
	
	private HttpRouteInfo mSelectedRouteInfo;
	
	// HttpClient的实现，这几个时间可能不太准确， 连接的获取被屏蔽
	private long mConnectTimeMs = 0;
	private long mSendTimeMs = 0;
	private long mReadTimeMs = 0;
	
	public HttpClientSession(long sessionId
			, SessionRequest sessionRequest
			, IHttpRouteChooser routeChooser
			, ICallback callback) {
		super(sessionId, sessionRequest, routeChooser, callback);
	}
	
	public long getSessionId() {
		return mSessionId;
	}
	
	private void prepareHttpPost(HttpParams httpParams, HttpRouteInfo httpRouteInfo) throws IOException {
		// 代理设置
		if (httpRouteInfo.getConnectInfo() != null) {
			if (StringUtil.isEmpty(httpRouteInfo.getConnectInfo().getProxyHost())) {
				mHttpPost = new HttpPost(httpRouteInfo.getHttpAddr().toUrlStr());
			} else {
				if (httpRouteInfo.getConnectInfo().getProxyType() == ConnectInfo.PROXY_TYPE_CT) {
					// 电信代理
					HttpHost proxyHttpHost = new HttpHost(httpRouteInfo.getConnectInfo().getProxyHost()
							, httpRouteInfo.getConnectInfo().getProxyPort());
					httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);

					mHttpPost = new HttpPost(httpRouteInfo.getHttpAddr().toUrlStr());
				} else {
					mHttpPost = new HttpPost(httpRouteInfo.getConnectInfo().getHttpProxyUrl());
					mHttpPost.setHeader(HttpHeader.REQ.X_ONLINE_HOST, httpRouteInfo.getHttpAddr().getHttpHost());
				}
			}

			if (httpRouteInfo.getConnectInfo().getApnType() == ApnType.APN_TYPE_WAP) {
				mHttpPost.setHeader(HttpHeader.REQ.X_ONLINE_HOST, httpRouteInfo.getHttpAddr().getHttpHost());
			} else {
				mHttpPost.setHeader(HttpHeader.REQ.HOST, httpRouteInfo.getHttpAddr().getHttpHost());
			}
		}
		
		// 所有wup请求的公共头部
		mHttpPost.setHeader(HttpHeader.REQ.CONTENT_TYPE, HttpHeader.CONTENT_TYPE);
		mHttpPost.setHeader(HttpHeader.REQ.QGUID, mSessionRequest.getGuid());
		mHttpPost.setHeader(HttpHeader.REQ.ACCEPT_ENCODING, HttpHeader.WUP_HEADER_GZIP_VALUE);
		mHttpPost.setHeader(HttpHeader.REQ.QQ_S_ZIP,  HttpHeader.WUP_HEADER_GZIP_VALUE);
		
		if (mSessionRequest.getUserHeaders() != null) {
			for (Entry<String, String> entry : mSessionRequest.getUserHeaders().entrySet()) {
				mHttpPost.setHeader(entry.getKey(), entry.getValue());
			}
		}
		
		// POST内容和加密处理
		byte[] postContent = ZipUtils.gZip(mSessionRequest.getPostData());
		
		if (mSessionRequest.isQQEncrypt()) {
			postContent = new TEACoding(QQ_ENCRYPT_BYTES_KEY).encode(postContent);
			mHttpPost.setHeader(HttpHeader.REQ.QQ_S_ENCRYPT, HttpHeader.WUP_HEADER_ENCRYPT_VALUE);
		}
		
		mHttpPost.setEntity(new ByteArrayEntity(postContent));
	}
	
	private RespData parseRespData(HttpResponse httpResponse) throws IOException {
		RespData respData = new RespData();
		
		respData.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		if (respData.isStatusHttpOK()) {
			
			// 返回是否压缩  -- 标准压缩头  走wap的时候网关可能会主动压缩下
			Header header = httpResponse.getFirstHeader(HttpHeader.RSP.CONTENT_ENCODING);
	        if (header != null) {
	        	respData.setContentEncoding(header.getValue());
	        }    
	     
	        // 自定义加密头
	        header = httpResponse.getFirstHeader(HttpHeader.RSP.QQ_S_ENCRYPT);
	        if (header != null) {
	        	respData.setQQEncrypt(header.getValue());
	        }
	        
	        // wup返回是否压缩 -- 自定义压缩头
	        header = httpResponse.getFirstHeader(HttpHeader.RSP.QQ_S_ZIP);
	        if (header != null) {
	        	respData.setQQZip(header.getValue());
	        }   
	        
	        // Content-Length
	        header = httpResponse.getFirstHeader(HttpHeader.RSP.CONTENT_LENGTH);
	        if (header != null) {
	        	respData.setContentLength(Long.valueOf(header.getValue()));
	        }

	        byte[] contentBytes = EntityUtils.toByteArray(httpResponse.getEntity());
	        
	        if (contentBytes == null || contentBytes.length <= 0) {
	        	return respData;
	        }
	        
	        // 先解标准的Gzip，主要是运营商有可能截获，又进一步压缩
	        if (respData.isContentEncodingGzip()) {
	        	contentBytes = ZipUtils.unGzip(contentBytes);
	        }
	        
	        if (respData.isQQEncrypt()) {
	        	contentBytes = new TEACoding(QQ_ENCRYPT_BYTES_KEY).decode(contentBytes);
	        }
	        
	        if (respData.isQQZip()) {
	        	contentBytes = ZipUtils.unGzip(contentBytes);
	        }
	        
	        respData.setResponseContent(contentBytes);
		}
		
		return  respData;
	}
	
	@Override
	public void doExecute() {
		try {
			long startConnectTime = System.currentTimeMillis();
			
			// 网络地址选择器, 获取对应的网络信息， 选择对应的地址
			mSelectedRouteInfo = mRouteChooser.selectRouteInfo();
			if (mSelectedRouteInfo == null) {
				onFinished(null, WUP_ERROR_CODE.WUP_INNER_ERROR, "no http route info is choosed!");
				return ;
			}
			if (mSelectedRouteInfo.getHttpAddr() == null) {
				onFinished(null, WUP_ERROR_CODE.WUP_INNER_ERROR, "http route info has no HttpAddr!");
				return ;
			}
			
			HttpParams httpParams = new BasicHttpParams();
			NetType netType = (mSelectedRouteInfo.getConnectInfo() != null) 
								? mSelectedRouteInfo.getConnectInfo().getNetType() : null;
			if (netType == NetType.NET_WIFI) {
				HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUTMS_IN_WIFI);
			} else if (netType == NetType.NET_3G || netType == NetType.NET_4G) {
				HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUTMS_IN_34G);
			} else {
				HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUTMS_IN_2G);
			}
			HttpConnectionParams.setSoTimeout(httpParams, mSessionRequest.getSessionTimeoutMs());
			
			QRomLog.d(TAG, "HttpSession start mSessionId=" + mSessionId 
							+ ", connectInfo=" + mSelectedRouteInfo.getConnectInfo() 
							+ ", httpAddr=" + mSelectedRouteInfo.getHttpAddr()
							+ ", envType=" + mSelectedRouteInfo.getEnvType());
			
			HttpClient client = new DefaultHttpClient(httpParams);
			prepareHttpPost(httpParams, mSelectedRouteInfo);
			
			try {
				long startSendTime = System.currentTimeMillis();
				mConnectTimeMs = startSendTime - startConnectTime;
				
				HttpResponse httpResponse = client.execute(mHttpPost);
				
				long startReadTime = System.currentTimeMillis();
				mSendTimeMs = startReadTime - startSendTime;
				
				RespData respData = parseRespData(httpResponse);
				
				mReadTimeMs = System.currentTimeMillis() - startReadTime;
				
				if (!respData.isStatusHttpOK()) {
					onFinished(null, WUP_ERROR_CODE.WUP_TASK_ERR_SERVICE_RSPCODE
							, "server error! statusCode=" + respData.getStatusCode());
					return ;
				}
				
				if (respData.getResponseContent() == null || respData.getResponseContent().length <= 0) {
					onFinished(null, WUP_ERROR_CODE.WUP_TASK_ERR_RSPDATA_EMPTY, "server response is empty");
					return ;
				}
				
				onFinished(respData, 0, "");
				return ;
			} catch (ConnectException e) {
				QRomLog.e(TAG, e.getMessage(), e);
				onFinished(null, WUP_ERROR_CODE.WUP_CONNECTED_FAILED, e.getMessage());
				reportNetworkError(WUP_ERROR_CODE.WUP_CONNECTED_FAILED);
				return ;
			} catch (ConnectTimeoutException e) {
				QRomLog.e(TAG, e.getMessage(), e);
				onFinished(null, WUP_ERROR_CODE.WUP_CONNECTION_TIMEOUT, e.getMessage());
				reportNetworkError(WUP_ERROR_CODE.WUP_CONNECTION_TIMEOUT);
				return ;
			} catch (SocketTimeoutException e) {
				QRomLog.e(TAG, e.getMessage(), e);
				onFinished(null, WUP_ERROR_CODE.WUP_READ_TIMEOUT, e.getMessage());
				reportNetworkError(WUP_ERROR_CODE.WUP_READ_TIMEOUT);
				return ;
			} catch (Exception e) {
				QRomLog.e(TAG, e.getMessage(), e);
				onFinished(null, WUP_ERROR_CODE.WUP_NETWORK_ERROR, e.getMessage());
				reportNetworkError(WUP_ERROR_CODE.WUP_NETWORK_ERROR);
				return ;
			}
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
			onFinished(null, WUP_ERROR_CODE.WUP_INNER_ERROR, e.getMessage());
		}
	}
	
	private void reportNetworkError(int errorCode) {
		if (mSelectedRouteInfo != null) {
			mSelectedRouteInfo.getRouteChooser().reportNetworkError(mSelectedRouteInfo, errorCode);
		}
	}

	@Override
	public HttpRouteInfo getHttpRouteInfo() {
		return mSelectedRouteInfo;
	}

	@Override
	public long getSessionConnectTimeMs() {
		return mConnectTimeMs;
	}

	@Override
	public long getSessionSendTimeMs() {
		return mSendTimeMs;
	}

	@Override
	public long getSessionReadTimeMs() {
		return mReadTimeMs;
	}

	@Override
	protected void doCancel() {
		if (mHttpPost != null) {
			mHttpPost.abort();
		}
	}
	
}
