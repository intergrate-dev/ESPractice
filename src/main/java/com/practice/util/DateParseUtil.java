package com.practice.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

public class DateParseUtil {


    public static final String DATE_STRICK = "yyyy-MM-dd";
    public static final String DATETIME_STRICK = "yyyy-MM-dd hh:mm:ss";
    public static final String DATE_OBLIQUE = "yyyy/MM/dd";

    public static String dateToString(Date date, String format) {
        if (date == null) {
            return null;
        }
        if (StringUtils.isEmpty(format)) {
            format = DATE_STRICK;
        }
        return DateFormatUtils.format(date, format);
    }

    public static String dateToString(Date date) {
        if (date == null) {
            return null;
        }
        return dateToString(date, DATE_STRICK);
    }

    public static String dateTimeToString(Date date) {
        if (date == null) {
            return null;
        }
        return dateToString(date, DATETIME_STRICK);
    }

}
