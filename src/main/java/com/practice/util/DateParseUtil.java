package com.practice.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
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

    public static Date stringToDate(String source) {
        return getDate(source, DATE_STRICK);
    }

    public static Date stringToDateTime(String source) {
        return getDate(source, DATETIME_STRICK);
    }

    public static Date getDate(String source, String format) {
        try {
            return DateUtils.parseDateStrictly(source, format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer queryTodayWeekOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
}
