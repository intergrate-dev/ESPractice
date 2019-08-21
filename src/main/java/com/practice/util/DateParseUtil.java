package com.practice.util;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateParseUtil {

    private static Logger logger = LoggerFactory.getLogger(DateParseUtil.class);

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
        try {
            return DateUtils.parseDate(source, DATETIME_STRICK);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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

    public static JSONArray lastWeekMondayToSunday() {
        JSONArray dateRange = new JSONArray();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
        String monday = null;
        String sunday = null;
        Calendar cal = Calendar.getInstance();
        setEndCalendar(cal);
        sunday = format.format(cal.getTime()).concat("23:59:59");
        cal.add(Calendar.DAY_OF_MONTH, -6);
        monday = format.format(cal.getTime()).concat("00:00:00");
        logger.info("monday: {}, sunday: {}", monday, sunday);
        dateRange.add(monday);
        dateRange.add(sunday);
        return dateRange;
    }

    private static void setEndCalendar(Calendar cal) {
        Date today = new Date();
        cal.setTime(today);
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            cal.add(Calendar.DAY_OF_MONTH, -7);
        } else {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
    }

    public static List<String> datesOfLastWeek() {
        List<String> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        setEndCalendar(cal);
        Date endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        dates.add(DateParseUtil.dateToString(cal.getTime()));
        while (endDate.after(cal.getTime())) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            dates.add(DateParseUtil.dateToString(cal.getTime()));
        }
        return dates;
    }

    public static void main(String[] args) {
        /*String str = "2019-08-18 16:41:00";
        Date date = DateParseUtil.stringToDateTime(str);
        logger.info("--------------------- date: {} ----------------", DateParseUtil.dateTimeToString(date));*/
        /*List<String> publish = new ArrayList<>();
        publish.add("22");
        publish.add(0, "11");
        publish.add(2, "33");

        logger.info("------------------------ publish, : {} --------------------", publish.toString());*/
        List<String> dates = datesOfLastWeek();
        logger.info("------------------------ main, dates: {} --------------------", dates.toString());
    }
}
