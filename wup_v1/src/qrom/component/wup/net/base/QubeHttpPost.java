package qrom.component.wup.net.base;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.params.HttpParams;

import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;


import android.content.Context;

public class QubeHttpPost {
	
	private static final String TAG = "QROM-HttpUrlConnecRequest";
	
	private HttpPost mHttpPost;
	
	private String mUrl;
	private HttpParams mHttpParams;
	
	/**
	 * 初始化QubeHttpPost
	 * @param url               连接url
	 * @param httpParams  HttpClient中的httpParams，
	 *                                   直连模式和非电信代理模式下可传入为空；
	 *                                   电信代理模式下若为空则走直连模式
	 */
	public QubeHttpPost(String url, HttpParams httpParams) {
		mUrl = url;
		mHttpParams = httpParams;
	}
	
	/**
	 * 获取http请求连接
	 *    若有代理使用ApnStatInfo中设定的代理（若调用是网络有变化及时刷新下）
	 * @throws Exception
	 */
	public HttpPost openRequestConnect() throws Exception {
		// 避免外部调用未及时改变网络状态，优先判断代理状态，优先使用直连
		boolean isUseProxy = ApnStatInfo.isUsedProxy();
		String proxy = isUseProxy ? ApnStatInfo.getProxyHost() : "";
		int port = isUseProxy ? ApnStatInfo.getProxyPort() : -1;
		return openRequestConnect(proxy, port, ApnStatInfo.getApnProxyType());
	}
	
	/**
	 * 获取http请求连接
	 *    若有代理使用ApnStatInfo中设定的代理（若调用是网络有变化及时刷新下）
	 *           -- 该接口用每次刷新网络状态
	 *    @param context 用于刷新网络
	 * @throws Exception
	 */
	public HttpPost openRequestConnect(Context context) throws Exception {
		ApnStatInfo.init(context);
		return openRequestConnect(ApnStatInfo.getProxyHost(), ApnStatInfo.getProxyPort(), ApnStatInfo.getApnProxyType());
	}
	
	/**
	 * 获取http请求连接
	 *     -- 若有代理则使用代理
	 * @param proxyHost  代理地址
	 * @param proxyPort  代理端口
	 * @param proxyType  代理类型，参考ApnStatInfo.PROXY_TYPE_CT 等
	 */
	public HttpPost openRequestConnect(String proxyHost, int proxyPort, int proxyType) {		
		
		if (QWupStringUtil.isEmpty(proxyHost)) {   // 无代理
			QWupLog.i(TAG, "proxy host is empty  -> direct connect");
			mHttpPost = new HttpPost(mUrl);
			return mHttpPost;
		} 
		
		// 有代理
		QWupLog.w(TAG, "isUsedProxy  proxyhost = " + proxyHost);
		if (ApnStatInfo.PROXY_TYPE_CT == proxyType)  { // 电信代理
			
			if (mHttpParams == null) {
				QWupLog.w(TAG, "PROXY_TYPE_CT -> mHttpParams is null, cann't set Proxy,  connect direct");
			} else {
				QWupLog.w(TAG, "PROXY_TYPE_CT -> " + proxyHost + " : " + proxyPort);
				HttpHost proxyHttpHost = new HttpHost(proxyHost, proxyPort);
				mHttpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);
			}
			
			mHttpPost = new HttpPost(mUrl);
		} else { // 其他代理

			String[] infos = QWupUrlUtil.parseUrlForHostPath(mUrl);
			if (!QWupUrlUtil.hasValidProtocal(proxyHost)) {
				proxyHost = "http://" + proxyHost;
			}
			String proxyUrl = proxyHost + infos[1];
			QWupLog.w(TAG, "isUsedProxy  other proxyUrl = " + proxyUrl);
			mHttpPost = new HttpPost(proxyUrl);       
			QWupLog.w(TAG, "ApnStatInfo.getProxyHost() = " + ApnStatInfo.getProxyHost());
			mHttpPost.setHeader(HttpHeader.REQ.X_ONLINE_HOST, infos[0]);
		}
		
		return mHttpPost;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HttpPost getHttpPost() {
		return mHttpPost;
	}
	
}
