package com.oleksiykovtun.allted.soc.base;

/**
 * UTF-8-supporting media types for service methods
 */
public class MediaType {

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    public static final String JSON = javax.ws.rs.core.MediaType.APPLICATION_JSON + CHARSET_UTF_8;
    public static final String TEXT = javax.ws.rs.core.MediaType.TEXT_PLAIN + CHARSET_UTF_8;
    public static final String HTML = javax.ws.rs.core.MediaType.TEXT_HTML + CHARSET_UTF_8;

}
