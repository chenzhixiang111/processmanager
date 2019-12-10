package com.makepower.processmanager.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author czx
 * @Description
 * @Version 2019-10-28 14:59
 */
public class DateTimeUtils {
    /**
     * 将时间字符串转为date
     * @param dateStr
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static Date string2date(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateStr);
    }

    /**
     * 获取当天的日期
     * @return yyyy-MM-dd格式的时间,只有日期，不带时分秒
     */
    public static String today(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
}
