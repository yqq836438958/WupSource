package qrom.component.wup.utils;

import qrom.component.wup.net.base.HttpHeader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;


public class ApnStatInfo {

    private static final String TAG = "ApnStatBroadcast";
    
    public static final int TYPE_UNKNOWN = 0x000;
    public static final int TYPE_NET = 0x001;
    public static final int TYPE_WAP = 0x002;    
    public static final int TYPE_WIFI = 0x004;
    public static final int TYPE_2G = 0x008;
    public static final int TYPE_3G = 0x010;
    public static final int TYPE_4G = 0x011;
    
    public static final String APN_UNKNOWN = "N/A";
    public static final String APN_NET = "Net";
    public static final String APN_WAP = "Wap";
    public static final String APN_WIFI = "Wlan";
    
    // 代理方式
    public static final byte PROXY_TYPE_CM = 0;
    public static final byte PROXY_TYPE_CT = 1;

    // 代理地址
    private static final String PROXY_CTWAP = "10.0.0.200";

    // APN 名称
    public static final String APN_CMWAP = "cmwap";
    public static final String APN_CMNET = "cmnet";
    public static final String APN_3GWAP = "3gwap";
    public static final String APN_3GNET = "3gnet";
    public static final String APN_UNIWAP = "uniwap";
    public static final String APN_UNINET = "uninet";
    public static final String APN_CTWAP = "ctwap";
    public static final String APN_CTNET = "ctnet";
    public static final String APN_777 = "#777";
    
    /**
     * 标准代理的网络类型索引
     */
    public static final int             PROXY_LIST_CMWAP   = 0;
    public static final int             PROXY_LIST_CMNET    = 1;
    public static final int             PROXY_LIST_UNWAP   = 2;
    public static final int             PROXY_LIST_UNNET    = 3;
    public static final int             PROXY_LIST_CTWAP    = 4;
    public static final int             PROXY_LIST_CTNET     = 5;
    public static final int             PROXY_LIST_WIFI        = 6;
    public static final int             PROXY_LIST_3GWAP   = 7;
    public static final int             PROXY_LIST_3GNET    = 8;
    public static final int             PROXY_LIST_4G    = 9;
    public static final int             PROXY_LIST_UNKOWN    = 99;

    /** 当前apn的信息 （networkInfo.getExtraInfo()）*/
    private static String M_APN_NAME = APN_UNKNOWN;
    /** 当前apn类型 -- 只记录wifi，net， wap，不细分具体net和wap的类型*/
    public static  int M_APN_TYPE = TYPE_WIFI;
    
    /** 代理地址 android.net.Proxy.getDefaultHost()*/
    private static String apnProxy = "";
    /** 代理端口 android.net.Proxy.getDefaultPort()*/
    private static int apnProt = HttpHeader.HTTP_PORT;
    /** 代理类型 */
    private static byte apnProxyType = PROXY_LIST_UNKOWN;
    private static boolean isUseProxy = false;

    /** 当前网络是否可用 networkInfo.isConnected() -- 需调用init更新当前状态 */
    private static boolean isConnected = false;
    
    /** 网络类型 -- wifi/2/3/4g*/
    private static int M_NET_TYPE = TYPE_UNKNOWN;
    
    /** 保存当前apn的额外信息 -- 统计使用 */
    public static String M_APN_EA_INFO = null;

    /**
     * apn设置初始化
     */
    public static void init(Context context) {

    	if (context == null) {
    		return;
    	}
    	
        try {
            
            M_NET_TYPE = getNetType(context);
            
            QWupLog.d(TAG, "init start");
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();

            int type = -1;
            isUseProxy = false;
            M_APN_TYPE = TYPE_UNKNOWN;
            M_APN_NAME = APN_UNKNOWN;
            apnProxy = "";
            apnProt = -1;
        	apnProxyType = PROXY_TYPE_CM;
             	
            String extraInfo = null;
            if (networkInfo != null) {
            	QWupLog.d(TAG, "networkInfo : " + networkInfo);
                isConnected = networkInfo.isConnected();
                type = networkInfo.getType();
                extraInfo = networkInfo.getExtraInfo();
                if (extraInfo == null) {
                    M_APN_TYPE = TYPE_UNKNOWN;
                } else {
                    extraInfo = extraInfo.trim().toLowerCase();
                }
                M_APN_EA_INFO = extraInfo;
            } else {
                isConnected = false;
                QWupLog.d(TAG, "isConnected : " + isConnected);
                return;
            }

            if (type == ConnectivityManager.TYPE_WIFI) {
                M_APN_TYPE = TYPE_WIFI;
                M_APN_NAME = APN_WIFI;
                isUseProxy = false;
                apnProxy = "";
            	apnProxyType = PROXY_TYPE_CM;
//            	BSSID = getWifiBSSID(context);
            } else {
            	M_APN_NAME = extraInfo;
                // 判断是 wap 模式还是 net 模式
                if (extraInfo == null) {
                    M_APN_TYPE = TYPE_UNKNOWN;
                } else if (extraInfo.contains(APN_CMWAP)
                        || extraInfo.contains(APN_UNIWAP)
                        || extraInfo.contains(APN_3GWAP)
                        || extraInfo.contains(APN_CTWAP)) {
                    M_APN_TYPE = TYPE_WAP;
                } else if (extraInfo.contains(APN_CMNET)
                        || extraInfo.contains(APN_UNINET)
                        || extraInfo.contains(APN_3GNET)
                        || extraInfo.contains(APN_CTNET)) {
                    M_APN_TYPE = TYPE_NET;
                } else if (extraInfo.contains(APN_777)) {
                    M_APN_TYPE = TYPE_UNKNOWN;
                } else {
                    M_APN_TYPE = TYPE_UNKNOWN;
                }

                isUseProxy = false;
                if (isProxyMode(M_APN_TYPE)) {
                    apnProxy = android.net.Proxy.getDefaultHost();
                    apnProt = android.net.Proxy.getDefaultPort();

                    if (apnProxy != null) {
                        apnProxy = apnProxy.trim();
                    }

                    if (!QWupStringUtil.isEmpty(apnProxy)) {
                        isUseProxy = true;
                        M_APN_TYPE = TYPE_WAP;

                        // 判断是否电信代理
                        if (PROXY_CTWAP.equals(apnProxy)) {
                            apnProxyType = PROXY_TYPE_CT;
                        } else {
                            apnProxyType = PROXY_TYPE_CM;
                        }
                    } else {
                        isUseProxy = false;
                        M_APN_TYPE = TYPE_NET;
                        apnProxy = "";
						// #777非代理时为电信Net
						if (extraInfo != null && extraInfo.contains(APN_777)) {
							M_APN_NAME = APN_CTNET;
						}                        
                    }
                } else {  // 无代理
                	apnProxy = "";
                	apnProxyType = PROXY_TYPE_CM;
                }
            }

            if (M_APN_NAME == null) {
            	M_APN_NAME = APN_UNKNOWN;
            }

            StringBuilder builder = new StringBuilder();
            builder.append("apnType = ").append(M_APN_TYPE);
            builder.append("; apnName = ").append(M_APN_NAME);
            builder.append("; isUseProxy = ").append(isUseProxy);
            builder.append("; apnProxy = ").append(apnProxy);
            builder.append("; apnProt = ").append(apnProt);
            QWupLog.d(TAG, builder.toString());
            QWupLog.trace(TAG, builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        QWupLog.d(TAG, "init end");
    }
    
    /**
     * 获取apn类型 （缓存数据，如果要当前最新信息调用getApnType(Context context)）
     *   -- 当前apn类型 ； 只记录wifi，net， wap，不细分具体net和wap的类型
     * @return
     */
    public static int getApnType() {
    	return M_APN_TYPE;
    }
    
    /**
     * 获取当前网络apn类型 （会重新刷新当前网络状态）
     *    -- 当前apn类型 ； 只记录wifi，net， wap，不细分具体net和wap的类型
     * @param context
     * @return
     */
    public static int getApnType(Context context) {
    	init(context);
    	return M_APN_TYPE;
    }

    /**
     * 获取当前网络名称（缓存数据networkInfo.getExtraInfo()）
     *    
     * @return
     */
    public static String getApnName() {
    	return M_APN_NAME;
    }
    
    /**
     * 获取当前网络名称（networkInfo.getExtraInfo()）
     *     -- 会刷新当前网络状态
     * @return
     */
    public static String getApnName(Context context) {
    	init(context);
    	return M_APN_NAME;
    }
    
    /**
     * 判断网络是否连接 （获取缓存信息，及时更新可以调用init()方法）
     * 
     * @return
     */
    public static boolean isNetConnected() {
    	return isConnected;
    }
    
    /**
     * 判断网络是否连接 （获取缓存信息，及时更新可以调用init()方法）
     * -- 会刷新当前网络状态
     * @return
     */
    public static boolean isNetConnected(Context context) {
    	init(context);
    	return isConnected;
    }
    
    /**
     * 是否使用代理
     *   -- wap/unkonwn模式且代理地址不为空
     * @return
     */
    public static boolean isUsedProxy() {
        return isProxyMode(M_APN_TYPE) && isUseProxy && !QWupStringUtil.isEmpty(apnProxy);
    }
    
    /**
     * 获取代理地址
     * @return
     */
    public static String getProxyHost() {
        return apnProxy;
    }
    
    /**
     * 获取代理端口
     * @return
     */
    public static int getProxyPort() {
        return apnProt;
    }
    
    /**
     * 获取代理类型
     * @return
     */
    public static int getApnProxyType() {
    	return apnProxyType;
    }
    /**
     * 是否是2G网络模式
     */
    public static boolean is2GMode(Context context) {
        
        return getNetType(context) == TYPE_2G;
    }
    
    /**
     * 是否是wifi网络模式 (通过contex获取系统当前状态)
     * @param context
     * @return
     */
    public static boolean isWifiMode(Context context) {
        return getNetType(context) == TYPE_WIFI;
    }
    
    /**
     * 是否是wifi网络模式（通过init方法初始化并缓存的网络状态）
     * @return
     */
    public static boolean isWifiMode() {
        return M_APN_TYPE == TYPE_WIFI;
    }
    
    /**
     * 或当前网络类型
     *    -- 返回2G/3G/wifi类型
     */
    public static int getNetType(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return TYPE_UNKNOWN;
            }
            // getActiveNetworkInfo方法在部分机型上调用crash，这里catch下异常
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo == null) {
                return TYPE_UNKNOWN;
            }

            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            }

            if (type == ConnectivityManager.TYPE_MOBILE) {
                int subType = networkInfo.getSubtype();
                switch (subType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return TYPE_2G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return TYPE_4G;
                default:
                    return TYPE_3G;
                }
            }

            return TYPE_WIFI;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return TYPE_UNKNOWN;
    }
    
    /**
     * 是否代理模式
     * @param type
     * @return
     */
    private static boolean isProxyMode(int type) {
        return type == TYPE_WAP || type == TYPE_UNKNOWN;
    }

    /**
     * 获取apn名字
     *   -- 只区分wify，net，wap
     * @param type 网络类型 （TYPE_WAP/ TYPE_NET/APN_WIFI/ TYPE_UNKNOWN）
     * @return
     */
    public static String getApnName(int type) {
        switch (type) {
        case TYPE_WAP:
             return APN_WAP; //保存apn的详细名字
        case TYPE_NET:
            return APN_NET;
        case TYPE_WIFI:
            return APN_WIFI;
        case TYPE_UNKNOWN:
            return APN_UNKNOWN;
        default:
            return APN_UNKNOWN;
        }
    }
    

    /**
     * 获取缓存中当前apn名字
     *   -- 只区分wify，联通/移动/电信 net，联通/移动/电信 wap
     *   -- 若网络变化先调用init()更新当前网络类型
     * @return
     */
	public static String getCurApnDetailName() {

		String resName = APN_UNKNOWN;

		if (M_APN_NAME == null) {
			M_APN_NAME = APN_UNKNOWN;
		}
		switch (ApnStatInfo.M_APN_TYPE) {
		case ApnStatInfo.TYPE_WIFI:
			resName = APN_WIFI;
			break;

		case ApnStatInfo.TYPE_WAP:
			if (M_APN_NAME.contains(ApnStatInfo.APN_CMWAP)) {
				resName = ApnStatInfo.APN_CMWAP;
				break;
			} else if (M_APN_NAME.contains(ApnStatInfo.APN_CTWAP)) {
				resName = ApnStatInfo.APN_CTWAP;
				break;
			} else if (M_APN_NAME.contains(ApnStatInfo.APN_UNIWAP)){
				resName = ApnStatInfo.APN_UNIWAP;
				break;
			} else if (M_APN_NAME.contains(ApnStatInfo.APN_3GWAP)) {
				resName = ApnStatInfo.APN_3GWAP;
			} else {
				resName = M_APN_NAME;
				break;
			}
		case ApnStatInfo.TYPE_NET:
			if (M_APN_NAME.contains(ApnStatInfo.APN_CMNET)) {
				resName = ApnStatInfo.APN_CMNET;
				break;
			} else if (M_APN_NAME.contains(ApnStatInfo.APN_CTNET)) {
				resName = ApnStatInfo.APN_CTNET;
				break;
			} else if (M_APN_NAME.contains(APN_UNINET)) {
				resName = ApnStatInfo.APN_UNINET;
				break;
			} else if (M_APN_NAME.contains(ApnStatInfo.APN_3GNET)) {
				resName = ApnStatInfo.APN_3GNET;
			}
			break;
		default:
			resName = "";
			break;
		}

		return resName;
	}
    
    /**
     * 获取缓存中当前apn Proxy索引
     * -- 若网络变化先调用init()更新当前网络类型
     * @return
     */
    public static int getCurApnProxyIndex() {
    	
    	if (M_APN_NAME == null) {
    		M_APN_NAME = "";
    	}
        
        switch(M_APN_TYPE) {
        case ApnStatInfo.TYPE_WIFI:
            return ApnStatInfo.PROXY_LIST_WIFI;
            
        case ApnStatInfo.TYPE_WAP:
            if (M_APN_NAME.contains(ApnStatInfo.APN_CMWAP)) {
                return ApnStatInfo.PROXY_LIST_CMWAP;
            } else if (M_APN_NAME.contains(ApnStatInfo.APN_CTWAP)) {
                return ApnStatInfo.PROXY_LIST_CTWAP;
            } else if (M_APN_NAME.contains(ApnStatInfo.APN_UNIWAP)) {
                return ApnStatInfo.PROXY_LIST_UNWAP;
            } else if (M_APN_NAME.contains(ApnStatInfo.APN_3GWAP)) {
            	return ApnStatInfo.PROXY_LIST_3GWAP;
            } else {
            	 return ApnStatInfo.PROXY_LIST_CTWAP;
            }
        case ApnStatInfo.TYPE_NET:
            if (M_APN_NAME.contains(ApnStatInfo.APN_CMNET)) {
                return ApnStatInfo.PROXY_LIST_CMNET;
            } else if (M_APN_NAME.contains(ApnStatInfo.APN_CTNET)) {
                return ApnStatInfo.PROXY_LIST_CTNET;
            } else if (M_APN_NAME.contains(ApnStatInfo.APN_UNINET)) {
                return ApnStatInfo.PROXY_LIST_UNNET;
            } else if (M_APN_NAME.contains(ApnStatInfo.APN_3GNET)) {
				return ApnStatInfo.PROXY_LIST_3GNET;
			} else {
				return ApnStatInfo.PROXY_LIST_CTNET;
            }
        default:
            return ApnStatInfo.PROXY_LIST_UNKOWN;
        }        
    }
	/**
	 * 获取WIFI的BSSID
	 */
	public static String getWifiBSSID(Context context) {
		if (context == null) {
			return null;
		}

        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (manager != null) {
                // crash上报 getConnectionInfo方法 部分手机抛出异常
                // （java.lang.IllegalArgumentException: BTTPDJBUJOH is not a constant in class android.net.wifi.SupplicantState）
                WifiInfo wifiInfo = manager.getConnectionInfo();
                String bssid = null;
                if (wifiInfo != null) {
                    bssid = wifiInfo.getBSSID();
                }
                QWupLog.trace(TAG, "getWifiBSSID-> bssid=" + bssid);
                return bssid; 
            }
        } catch (Throwable e) {
            QWupLog.w(TAG, "getWifiBSSID-> err msg: " + e + ", " + e.getMessage());
        }
		
		return null;
	}
	
	/**
	 * 获取当前网络类型
	 * @return NET_WIFI/2G/3G/4G
	 */
	public static int getNetType() {
	    return M_NET_TYPE;
	}
}
