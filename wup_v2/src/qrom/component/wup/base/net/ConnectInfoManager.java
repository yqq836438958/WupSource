package qrom.component.wup.base.net;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import qrom.component.log.QRomLog;
import qrom.component.wup.base.ContextHolder;
import qrom.component.wup.base.utils.ListenerList;
import qrom.component.wup.base.utils.StringUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * 网络管理器
 * @author wileywang
 */
public class ConnectInfoManager extends BroadcastReceiver {
	private static final String TAG = ConnectInfoManager.class.getSimpleName();
	
	private static final long LOAD_INFO_TIME_PERIOD_SLOW = 5 * 60 * 1000; 
	private static final long LOAD_INFO_TIME_PERIOD_FAST = 10 * 1000;
	
	private static ConnectInfoManager sInstance;
	public static ConnectInfoManager get() {
		if (sInstance == null) {
			synchronized(ConnectInfoManager.class) {
				if (sInstance == null) {
					sInstance = new ConnectInfoManager(
								ContextHolder.getApplicationContextForSure());
				}
			}
		}
		return sInstance;
	}
	
	private Context mApplicationContext;
	private ConnectInfo mCurrentConnectInfo;
	private ListenerList<IConnectInfoListener> mListenerList = new ListenerList<IConnectInfoListener>() {
		@Override
		protected void onNotifyListener(IConnectInfoListener listener,
				Object... params) {
			if (params != null && params.length > 0) {
				listener.onConnectInfoReload();
			} else {
				listener.onReceiveNetworkChanged();
			}
		}
		
	};
	
	// 做一个定时器，每隔一段时间就去
	private Timer mLoadTimer; 
	
	protected ConnectInfoManager(Context context) {
		mApplicationContext = context.getApplicationContext();
		loadConnectInfo();
		
		long loadInfoPeriodMs = LOAD_INFO_TIME_PERIOD_SLOW;
		try {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(NetActions.ACTION_NET_CHANGED);
			mApplicationContext.registerReceiver(this, intentFilter);
		} catch (Throwable e) {
			// 广播注册的保护吗，防止注册过程crash, 但是这个时候网络状况就不能及时更新了
			// 加快更新网络连接信息的频率，增加准确性(保守策略)
			QRomLog.e(TAG, e.getMessage(), e);
			loadInfoPeriodMs = LOAD_INFO_TIME_PERIOD_FAST;
		}
		
		// 主要是为了保证，在网络广播丢失的情况下，进程还是能够从一段时间恢复当前网咯状况的信息
		mLoadTimer = new Timer();
		mLoadTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				loadConnectInfo();
			}
		}, loadInfoPeriodMs, loadInfoPeriodMs);
	}
	
	public ConnectInfo getConnectInfo() {
		return mCurrentConnectInfo;
	}
	
	public void registerConnectInfoListener(IConnectInfoListener listener) {
		mListenerList.registerListener(listener);
	}
	
	public void unRegisterConnectInfoListener(IConnectInfoListener listener) {
		mListenerList.unregisterListener(listener);
	}
	
	@SuppressWarnings("deprecation")
	private synchronized void loadConnectInfo() {
		long loadTimestartMs = System.currentTimeMillis();
		try {
			 ConnectivityManager manager = (ConnectivityManager) mApplicationContext
	                    .getSystemService(Context.CONNECTIVITY_SERVICE);
			 
			 NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			 ConnectInfo connectInfo = new ConnectInfo(getNetType(networkInfo));
			 
			 if (!connectInfo.isConnected()) {
				 mCurrentConnectInfo = connectInfo; 
				 return ;
			 } 
			 
			 String extraInfo = networkInfo.getExtraInfo();
			 if (extraInfo == null) {
				 connectInfo.setApnType(ApnType.APN_TYPE_UNKNOWN);
			 } else {
				 extraInfo = extraInfo.trim().toLowerCase(Locale.getDefault());
				 connectInfo.setExtraInfo(extraInfo);
			 }
			 
			if (connectInfo.getNetType() == NetType.NET_WIFI) {
				connectInfo.setApnType(ApnType.APN_TYPE_WIFI);
				connectInfo.setApnName(ConnectInfo.APN_NAME_WIFI);
				connectInfo.setBssid(getWifiBSSID());
			} else {
				connectInfo.setApnName(extraInfo);

				if (StringUtil.isEmpty(extraInfo)) {
					connectInfo.setApnType(ApnType.APN_TYPE_UNKNOWN);
				} else if (extraInfo.contains(ConnectInfo.APN_CMWAP)
						|| extraInfo.contains(ConnectInfo.APN_UNIWAP)
						|| extraInfo.contains(ConnectInfo.APN_3GWAP)
						|| extraInfo.contains(ConnectInfo.APN_CTWAP)) {
					connectInfo.setApnType(ApnType.APN_TYPE_WAP);
				} else if (extraInfo.contains(ConnectInfo.APN_CMNET)
						|| extraInfo.contains(ConnectInfo.APN_UNINET)
						|| extraInfo.contains(ConnectInfo.APN_3GNET)
						|| extraInfo.contains(ConnectInfo.APN_CTNET)) {
					connectInfo.setApnType(ApnType.APN_TYPE_NET);
				} else if (extraInfo.contains(ConnectInfo.APN_777)) {
					connectInfo.setApnType(ApnType.APN_TYPE_UNKNOWN);
				} else {
					connectInfo.setApnType(ApnType.APN_TYPE_UNKNOWN);
				}
				
				if (connectInfo.getApnType() == ApnType.APN_TYPE_UNKNOWN
						|| connectInfo.getApnType() == ApnType.APN_TYPE_WAP) {
                    String apnProxy = android.net.Proxy.getDefaultHost();
                    if (apnProxy != null) {
                    	connectInfo.setProxyHost(apnProxy.trim());
                    }
                    connectInfo.setProxyPort(android.net.Proxy.getDefaultPort());

                    if (!StringUtil.isEmpty(connectInfo.getProxyHost())) {
                    	connectInfo.setIsUseProxy(true);
                    	
                        connectInfo.setApnType(ApnType.APN_TYPE_WAP);
                        // 判断是否电信代理
                        if (ConnectInfo.PROXY_CTWAP_HOST.equals(connectInfo.getProxyHost())) {
                        	connectInfo.setProxyType(ConnectInfo.PROXY_TYPE_CT);
                        } 
                    } else {
                    	connectInfo.setApnType(ApnType.APN_TYPE_NET);
						// #777非代理时为电信Net
						if (extraInfo != null && extraInfo.contains(ConnectInfo.APN_777)) {
							connectInfo.setApnName(ConnectInfo.APN_CTNET);
						}
                    }
                } 
				
				if (StringUtil.isEmpty(connectInfo.getApnName())) {
					connectInfo.setApnName(ConnectInfo.APN_NAME_UNKNOWN);
				}
			}
			
			mCurrentConnectInfo = connectInfo; // 注意，这里交换一定要最后执行,从而可以确保线程安全，原子化狡猾可以保证读不加锁
		} catch (Throwable e) {
			QRomLog.e(TAG, e.getMessage(), e);
			mCurrentConnectInfo = new ConnectInfo(NetType.NET_NO); // 无法获取网络状况，当无网络对待
		} finally {
			QRomLog.i(TAG, "loadConnectInfo timeEscape=" + (System.currentTimeMillis() - loadTimestartMs)
					+ "ms, currentConnectInfo=" + mCurrentConnectInfo);
			mListenerList.notifyListeners("reload");
		}
	}
	
	private NetType getNetType(NetworkInfo activeNetworkInfo) {
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			return NetType.NET_NO;
		}
		
		int type = activeNetworkInfo.getType();
        if (type == ConnectivityManager.TYPE_WIFI) {
            return NetType.NET_WIFI;
        }

        if (type == ConnectivityManager.TYPE_MOBILE) {
            int subType = activeNetworkInfo.getSubtype();
            switch (subType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return NetType.NET_2G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NetType.NET_4G;
            default:
                return NetType.NET_3G;
            }
        }
        
        return NetType.NET_UNKNOWN;
	}
	
	public String getWifiBSSID() {
        try {
            WifiManager manager = (WifiManager) mApplicationContext.getSystemService(Context.WIFI_SERVICE);
            if (manager != null) {
                // crash上报 getConnectionInfo方法 部分手机抛出异常
                // （java.lang.IllegalArgumentException: BTTPDJBUJOH is not a constant in class android.net.wifi.SupplicantState）
                WifiInfo wifiInfo = manager.getConnectionInfo();
                String bssid = null;
                if (wifiInfo != null) {
                    bssid = wifiInfo.getBSSID();
                }
                return bssid; 
            }
        } catch (Throwable e) {
            QRomLog.e(TAG, "getWifiBSSID-> err msg: " + e.getMessage(), e);
        }
		
		return "";
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (NetActions.ACTION_NET_CHANGED.equals(intent.getAction())) {
			loadConnectInfo();
			mListenerList.notifyListeners();
		}
	}
}
