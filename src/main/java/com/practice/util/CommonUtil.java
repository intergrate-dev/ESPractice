package com.practice.util;

import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CommonUtil {
    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static String listToString(List<String> list) {
        StringBuffer buffer = new StringBuffer();
        list.stream().forEach(t -> {
            buffer.append(t).append(",");
        });
        return buffer.substring(0, buffer.length() - 1);
    }



    public static void main(String[] args) {
        String monday = null;
        String sunday = null;
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.setTime(today);
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            cal.add(Calendar.DAY_OF_MONTH, -7);
        } else {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        sunday = new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime()).concat("23:59:59");
        cal.add(Calendar.DAY_OF_MONTH, -6);
        monday = new SimpleDateFormat("yyyy-MM-dd ").format(cal.getTime()).concat("00:00:00");
        logger.info("monday: {}, sunday: {}", monday, sunday);
    }

}
