package qrom.component.wup.net.base;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import qrom.component.wup.utils.ApnStatInfo;
import qrom.component.wup.utils.QWupLog;
import qrom.component.wup.utils.QWupStringUtil;
import qrom.component.wup.utils.QWupUrlUtil;


import android.content.Context;

public class QubeHttpUrlConnection {
	private static final String TAG = "QROM-HttpUrlConnecRequest";
	private String mUrl;
	private String mHost;
	
	HttpURLConnection urlConn = null;
	
	public QubeHttpUrlConnection(String url) {
		mUrl = url;
	}

	
	/**
	 * 获取http请求连接
	 *    若有代理使用ApnStatInfo中设定的代理（若调用时网络有变化及时刷新下ApnStatInfo中信息）
	 * @throws Exception
	 */
	public HttpURLConnection openRequestConnect() throws Exception {
		// 避免外部调用未及时改变网络状态，优先判断代理状态，优先使用直连
		boolean isUseProxy = ApnStatInfo.isUsedProxy();
		String proxy = isUseProxy ? ApnStatInfo.getProxyHost() : "";
		int port = isUseProxy ? ApnStatInfo.getProxyPort() : -1;
		return openRequestConnect(proxy, port, ApnStatInfo.getApnProxyType());
	}
	
	/**
	 * 获取http请求连接
	 *    -- 该接口用每次刷新网络状态
	 *    @param context 用于刷新网络
	 * @throws Exception
	 */
	public HttpURLConnection openRequestConnect(Context context) throws Exception {
		ApnStatInfo.init(context);
		return openRequestConnect(ApnStatInfo.getProxyHost(), ApnStatInfo.getProxyPort(), ApnStatInfo.getApnProxyType());
	}
	
	
	/**
	 * 获取http请求连接
	 *     -- 若有代理则使用代理
	 * @param proxyHost  代理地址
	 * @param proxyPort  代理端口
	 * @param proxyType  代理类型，参考ApnStatInfo.PROXY_TYPE_CT 等
	 * @return HttpURLConnection
	 * @throws Exception
	 */
	public HttpURLConnection openRequestConnect(String proxyHost, int proxyPort, int proxyType) throws Exception{
		
		URL url = null;
		
		if (QWupStringUtil.isEmpty(proxyHost)) {  // 不走代理
			url = QWupUrlUtil.toURL(mUrl);
			urlConn = (HttpURLConnection) url.openConnection();
			QWupLog.i(TAG, "connect direct, don't use proxy");
			return urlConn;
		}
		
		if (ApnStatInfo.PROXY_TYPE_CT == proxyType) {  // 电信代理
			
			Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
			url = QWupUrlUtil.toURL(mUrl);
			QWupLog.w(TAG, "isUsedProxy  PROXY_TYPE_CT proxyHost = " + proxyHost + " port = " + proxyPort);
			urlConn = (HttpURLConnection) url.openConnection(proxy);
		} else {  // 其他代理
			String[] urlInfo = QWupUrlUtil.parseUrlForHostPath(mUrl);
			mHost = urlInfo[0];
			
			if (!QWupUrlUtil.hasValidProtocal(proxyHost)) {
				proxyHost = "http://" + proxyHost;
			}
			String proxyUrl = proxyHost + urlInfo[1];
			QWupLog.w(TAG, "isUsedProxy  other proxyUrl = " + proxyUrl);
			// 构造一个URL对象
			url = new URL(proxyUrl);
			// 使用HttpURLConnection打开连接
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestProperty(HttpHeader.REQ.X_ONLINE_HOST, mHost);
		}		
		return urlConn;
    }	
	
	public HttpURLConnection getHttpUrlConnection() {
		return urlConn;
	}

}
