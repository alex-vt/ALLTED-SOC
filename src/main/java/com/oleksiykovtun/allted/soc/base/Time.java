package com.oleksiykovtun.allted.soc.base;

import java.text.SimpleDateFormat;

/**
 * Common time formatter
 */
public class Time {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";

    public static String getFormatted(long timeMillis) {
        return new SimpleDateFormat(DATE_TIME_FORMAT).format(timeMillis) + " UTC";
    }

    public static String getSeconds(long timeMillis) {
        return timeMillis / 1000 + " s";
    }

}
