package com.lt.springstarter.util;

import java.text.SimpleDateFormat;

public class Datetime {

    public static Long timestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static Long ms() {
        return System.currentTimeMillis();
    }

    public static String datetime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ms());
    }

    public static String format(Long timestamp, String format) {
        return new SimpleDateFormat(format).format(timestamp * 1000);
    }
}
