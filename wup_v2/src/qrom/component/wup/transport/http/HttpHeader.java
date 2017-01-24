package qrom.component.wup.transport.http;

/*
 * Copyright (C) 2005-2010 TENCENT Inc.All Rights Reserved.
 * 
 * FileName：HttpHeader.java
 * 
 * Description：enum of http header name 
 * 
 * History：
 * 1.0 samuelmo Apr 7, 2010 Create
 */

public class HttpHeader {
    
    public static final class REQ {
        public static final String ACCEPT = "Accept";
        public static final String HOST = "Host";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String USER_AGENT = "User-Agent";
        public static final String REFERER = "Referer";
        public static final String RANGE = "Range";
        public static final String CONNECTION = "Connection";
        public static final String COOKIE = "Cookie";
        public static final String QCOOKIE = "QCookie";
        public static final String QUA = "Q-UA";
        public static final String QGUID = "Q-GUID";
        public static final String QAUTH = "Q-Auth";
        public static final String X_ONLINE_HOST = "x-online-host";
        
        /** wup压缩协议头 */
        public static final String QQ_S_ZIP = "QQ-S-ZIP";
        /** wup加密协议头 */
        public static final String QQ_S_ENCRYPT = "QQ-S-Encrypt";
    }

    public static final class RSP {
        /* version */
        /* status code */
        public static final String LOCATION = "Location";
        public static final String SERVER = "Server";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String CHARSET = "Charset";
        public static final String TRANSFER_ENCODING = "Transfer-Encoding";
        public static final String LAST_MODIFY = "Last-Modified";
        public static final String BYTE_RNAGES = "Byte-Ranges";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String CONNECTION = "Connection";
        public static final String CONTENT_RANGE = "Content-Range";
        public static final String CONTENT_DISPOSITION = "Content-Disposition";
        
        /** wup压缩协议头 */
        public static final String QQ_S_ZIP = "QQ-S-ZIP";
        /** wup加密协议头 */
        public static final String QQ_S_ENCRYPT = "QQ-S-Encrypt";
    }
    
    /** wup压缩协议 -- gzip */
    public static final String WUP_HEADER_GZIP_VALUE = "gzip";
    /** wup加密协议 --  mttecr2*/
    public static final String WUP_HEADER_ENCRYPT_VALUE = "mttecr2";
    // 请求头部内容类型
    public static final String CONTENT_TYPE = "application/multipart-formdata";
    
    public static final int HTTP_PORT = 80;
}
