package qrom.component.wup.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Patterns;

/*
 * Copyright (C) 2005-2010 TENCENT Inc.All Rights Reserved.
 * FileName：QubeUrlUtil.java
 * Description：url utility
 * History：
 * 1.0 samuelmo Apr 3, 2010 Create
 */

public final class QWupUrlUtil {

    // yorkhuang 2012-03-02
    // 重写了新的较为严格的正则表达式，为了解决若干中文以英文"."分隔开的非法URL被判断成合法，没有转SOSO而导致传输给服务器域名解析出错的问题
    private static Pattern mVALIDURL = Pattern
            .compile(
                    "((http://)?(\\w+[.])*|(www.))\\w+[.]"
                            + "([a-z]{2,4})?[[.]([a-z]{2,4})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)"
                            + "?([.][a-z]{2,4}+|/?)", Pattern.CASE_INSENSITIVE);
    private static Pattern mVALIDLOCALURL = Pattern.compile(
            "(.+)localhost(:)?(\\d)*/(.+)(\\.)(.+)", Pattern.CASE_INSENSITIVE);
    private static Pattern mVALIDMTTURL = Pattern.compile("mtt://(.+)",
            Pattern.CASE_INSENSITIVE);
    private static Pattern mVALIDQBEURL = Pattern.compile("qube://(.+)",
            Pattern.CASE_INSENSITIVE);
    private static Pattern mVALIDIPADDRESS = Pattern.compile(
            "(\\d){1,3}\\.(\\d){1,3}"
                    + "\\.(\\d){1,3}\\.(\\d){1,3}(:\\d{1,4})?(/(.*))?",
            Pattern.CASE_INSENSITIVE);

    public static final String LOCAL_FILE_PREFIX = "file://";

    private QWupUrlUtil() {
    }


    /**yyy
     * 判断URL是否是一个有效的格式
     */
    public static boolean isCandidateUrl(final String aUrl) {
        if (aUrl == null || aUrl.length() == 0) {
            return false;
        }

        if (QWupStringUtil.hasNotAscII(aUrl)) { // 含有中文
            return false;
        }

        String url = aUrl.trim();

        Matcher validUrl = mVALIDURL.matcher(url);
        Matcher validLocal = mVALIDLOCALURL.matcher(url);
        Matcher validIp = mVALIDIPADDRESS.matcher(url);
        Matcher validMtt = mVALIDMTTURL.matcher(url);
        Matcher validQbe = mVALIDQBEURL.matcher(url);

        return (validUrl.find() || validLocal.find() || validIp.find()
                || validMtt.find() || validQbe.find() || isLocalUrl(url));
    }

    /**
     * @return True iff the remote url is valid.
     */
    public static boolean isLocalUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return isFileUrl(url);
    }

    /**
     * @return True iff the url is an file: url.
     */
    public static boolean isFileUrl(String url) {
        return (null != url) && (url.length() > 6)
                && url.substring(0, 7).equalsIgnoreCase(LOCAL_FILE_PREFIX);
    }


    /**yyy
     * 判断URL是否有一个有效的协议头
     */
    public static boolean hasValidProtocal(final String aUrl) {
        if (aUrl == null || aUrl.length() == 0) {
            return false;
        }
        String url = aUrl.trim().toLowerCase();

        int pos1 = url.indexOf("://");
        int pos2 = url.indexOf('.');

        // 检测"wap.fchgame.com/2/read.jsp?url=http://www.zaobao.com/zg/zg.shtml"类型网址
        if (pos1 > 0 && pos2 > 0 && pos1 > pos2) {
            return false;
        }

        return url.contains("://");
    }

    /**yyy
     * 根据输入，得到一个有效URL 如果输入无法被解析为一个URL，返回NULL
     */
    public static String resolvValidUrl(final String aUrl) {
        if (aUrl == null || aUrl.length() == 0) {
            return null;
        }

        String tmpUrl = aUrl.trim();

        // bug9422306：当url是“data:Image/jpg;base64,/9j/4AAQSkZJRgABAgAAZABkA...”这种
        // 字符串形式的图片数据时，作特殊处理，否则会发生ANR
        if (tmpUrl.length() > 11
                && tmpUrl.substring(0, 11).toLowerCase()
                        .startsWith("data:image/")) {
            return tmpUrl;
        }

        /*
         * 原逻辑这里会现在用系统规则过滤下url，在levon a780上调用系统匹配规则导致anr，
         * 匹配逻辑修改为，先用本地规则匹配，若匹配失败再用系统规则效验 -- sukeyli 20130617
         */
        
        if (isCandidateUrl(tmpUrl)) {  // 用本地规则匹配url
            if (!hasValidProtocal(tmpUrl)) {
                tmpUrl = "http://" + tmpUrl;
            }

            try {
                if (tmpUrl.startsWith("tencent://")
                        || tmpUrl.startsWith("qube://")
                        || tmpUrl.startsWith("file://")) {
                    return tmpUrl;
                } else {
                    new URL(tmpUrl).toString();
                    return tmpUrl;
                }
            } 
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
        	
        	// 用系统规则匹配，避免本地规则有部分url匹配失败，导致误判
        	boolean isUrl = Patterns.WEB_URL.matcher(tmpUrl).matches();
        	if (isUrl) {  // 系统解析是合法url返回
        		if (!hasValidProtocal(tmpUrl)) {
        			tmpUrl = "http://" + tmpUrl;
        		}
        		return tmpUrl;
        	}
        }
        return null;
    }

    /**yyy
     * 判断是否有可用网络
     * 
     * @return
     */
    public static boolean isNetWorkConnected(Context context) {
        if (context == null) {
            return false;
        }

        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                
                NetworkInfo info = manager.getActiveNetworkInfo();                
                if (info != null && info.isConnected()) {
                    return true;
                }
            }            
            return false;
        } catch (Exception e) {
            
            e.printStackTrace();
        }
        // 异常情况下默认网络ok
        return true;
    }
    
	/**
	 * 解析当前url的host和path信息
	 *    -- host：为"://"到第一个'/'之间的信息，path为'/'之后的部分
	 * @param strTargetUrl
	 * @return String[2]; 0 -- host信息， 1-- path信息
	 */
	public static String[] parseUrlForHostPath(String strTargetUrl) {
		
		String host = "";
		String path = "";
		if (!QWupStringUtil.isEmpty(strTargetUrl)) {			
			int hostIndex = strTargetUrl.indexOf("://") + 3;
			if (hostIndex < 0) {
				hostIndex = 0;
			}
			int pathIndex = strTargetUrl.indexOf('/', hostIndex);
			if (pathIndex < 0) {
				host = strTargetUrl.substring(hostIndex);
				path = "";
			} else {
				host = strTargetUrl.substring(hostIndex, pathIndex);
				path = strTargetUrl.substring(pathIndex);			
			}
		}
		
		if (QWupStringUtil.isEmpty(path)) {
			path = "/";
		}		
		return new String[] {host, path };
	}
	
    /**
     * string url to URL
     * 
     * @param url
     * @return
     */
    public static URL toURL(String url) throws MalformedURLException {
        URL uRL = new URL(url);

        // 有个别 URL 在 path 和 querystring 之间缺少 / 符号，需补上
        if (uRL.getPath() == null || "".equals(uRL.getPath())) {
            if (uRL.getFile() != null && uRL.getFile().startsWith("?")) {
                // 补斜杠符号
                int idx = url.indexOf('?');
                if (idx != -1) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(url.substring(0, idx));
                    sb.append('/');
                    sb.append(url.substring(idx));

                    uRL = new URL(sb.toString());

                    // System.out.println("toURL : " + _URL.toString());
                }
            }

            // 分支走到这里，没有path也没有file，证明为一个没有/的host，例如:
            // http://m.cnbeta.com(注意：后面没有/)
            if (uRL.getFile() == null || "".equals(uRL.getFile())) {
                StringBuilder sb = new StringBuilder();
                sb.append(url);
                sb.append("/");

                uRL = new URL(sb.toString());
            }

        }
        return uRL;
    }
    
}
