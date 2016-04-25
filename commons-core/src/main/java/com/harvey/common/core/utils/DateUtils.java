package com.harvey.common.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.harvey.common.core.config.Config;

public class DateUtils {

    public class DateFormats {

        public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String ISO_DATETIME_TIME_ZONE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
        public static final String ISO_DATETIME_NO_SECOND_FORMAT = "yyyy-MM-dd HH:mm";

        public static final String CN_DATETIME_FORMAT = "yyyy年MM月dd日HH时mm分ss秒";
        public static final String CN_SHORT_DATETIME_FORMAT = "yyyy年MM月dd日HH时mm分";

        public static final String ISO_DATETIME_MILLISECOND_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        public static final String ISO_DATETIME_MILLISECOND_TIME_ZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        public static final String ISO_DATETIME_NO_T_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String ISO_DATETIME_TIME_ZONE_NO_T_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
        public static final String ISO_DATETIME_NO_SECOND_NO_T_FORMAT = "yyyy-MM-dd HH:mm";

        public static final String ISO_DATETIME_MILLISECOND_NO_T_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        public static final String ISO_DATETIME_MILLISECOND_TIME_ZONE_NO_T_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

        public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
        public static final String CN_DATE_FORMAT = "yyyy年MM月dd日";
        public static final String ISO_DATE_TIME_ZONE_FORMAT = "yyyy-MM-ddZ";

        public static final String ISO_TIME_FORMAT = "'T'HH:mm:ss";
        public static final String ISO_TIME_MILLISECOND_FORMAT = "'T'HH:mm:ss.SSS";

        public static final String ISO_TIME_TIME_ZONE_FORMAT = "'T'HH:mm:ssZZ";
        public static final String ISO_TIME_MILLISECOND_TIME_ZONE_FORMAT = "'T'HH:mm:ss.SSSZ";

        public static final String ISO_TIME_NO_T_FORMAT = "HH:mm:ss";
        public static final String ISO_TIME_MILLISECOND_NO_T_FORMAT = "HH:mm:ss.SSS";
        public static final String CN_TIME_FORMAT = "HH时mm分ss秒";

        public static final String ISO_TIME_NO_T_TIME_ZONE_FORMAT = "HH:mm:ssZZ";
        public static final String ISO_TIME_MILLISECOND_NO_T_TIME_ZONE_FORMAT = "HH:mm:ss.SSSZ";

        public static final String SMTP_DATETIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
        public static final String SMTP_DATETIME_MILLISECOND_FORMAT = "EEE, dd MMM yyyy HH:mm:ss.SSS Z";

        public static final String ISO_MONTH_FORMAT = "yyyy-MM";
        public static final String CN_MONTH_FORMAT = "yyyy年MM月";
        
        public static final String US_DATETIME_TIME_ZONE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
    }

    public static final String[] DateFormatPatterns;
    static {
        List<String> patterns = new ArrayList<String>();
        for (Field field : DateFormats.class.getFields()) {
            if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())
                    && field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    patterns.add(field.get(null).toString());
                } catch (Exception ex) {
                }
            }
        }
        DateFormatPatterns = new String[patterns.size()];
        patterns.toArray(DateFormatPatterns);
    }
    
    public static final String DateFormatPattern = (String) CommonUtils.ifnull(Config.get("sys.DateFormatPattern"),DateFormats.ISO_DATE_FORMAT);
    public static final String DatetimeFormatPattern = (String) CommonUtils.ifnull(Config.get("sys.DatetimeFormatPattern"),DateFormats.ISO_DATETIME_FORMAT);

    public static Date parse(String source) throws ParseException {
        return parse(source, DateFormatPatterns);
    }
    

    public static Date parse(String source, String[] parsePatterns) throws ParseException {
        if ((source == null) || (parsePatterns == null)) {
            throw new IllegalArgumentException("Date and Patterns must not be null");
        }
        source = reformatTimezone(source);
        SimpleDateFormat parser = new SimpleDateFormat();
        ParsePosition pos = new ParsePosition(0);
        for (int i = 0; i < parsePatterns.length; ++i) {
            String pattern = parsePatterns[i];
            if(DateFormats.US_DATETIME_TIME_ZONE_FORMAT.equals(pattern)){
                pos.setIndex(0);
                return new SimpleDateFormat(pattern,Locale.US).parse(source,pos);
            }
            
            parser.applyPattern(pattern);
            pos.setIndex(0);
            
            Date date = parser.parse(source, pos);
            if ((date != null) && (pos.getIndex() == source.length())) {
                return date;
            }
        }
        return null;
    }

    
    private static String reformatTimezone(String str) {
        if(str.charAt(str.length() - 1) == 'Z'){
            StringBuilder sb = new StringBuilder(str);
            sb.setLength(str.length() - 1);
            sb.append("+0000");
            return sb.toString();
        }
        return str;
    }

    /**
     * Format the Date using pattern "yyyy-MM-dd"
     * 
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        if(date.getHours() == 0 && date.getMinutes() == 0 && date.getSeconds() == 0){
            return format(date, DateFormatPattern);  
        }
        return format(date,DatetimeFormatPattern);
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * Format the Date using pattern "yyyy-MM-dd HH:mm:ss"
     * 
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        return format(date, DatetimeFormatPattern);
    }

    /**
     * Format the Date using pattern "yyyy-MM"
     * 
     * @param date
     * @return
     */
    public static String formatMonth(Date date) {
        return format(date, DateFormats.ISO_MONTH_FORMAT);
    }

    /**
     * run test
     * 
     * @param args
     * @throws ParseException 
     */
    public static void main(String[] args) throws ParseException {
        outTest("2015-12-03T08:51:49.219Z");
        
        outTest("2015年1月8日00时31分44秒");
        outTest("2015年1月8日00时31分");
        outTest("2015年1月8日");
        outTest("2015年1月");
        outTest("00时31分44秒");

        outTest("2015-1-8T13:31:44");
        outTest("2015-1-8T13:31");
        outTest("T13:31:44");
        outTest("13:31:44");
        
        outTest("2015-1-8 13:31:44");
        outTest("2015-1-8 13:31");
        
        outTest("2015-1-8 13:31:44.576");
        
        outTest("2015-1-8 13:31:44+0800");
        outTest("2015-1-8 13:31:44-0800");
        
        outTest(new Date().toString());
    }

    private static void outTest(String dateStr) {
        try {

            System.out.println(String.format("Test:%s result:%s", dateStr, format(parse(dateStr),DateFormats.ISO_DATETIME_MILLISECOND_FORMAT)));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
