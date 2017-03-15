package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * 工具类.
 */
final class Util {
    /**
     * 平年月份天数.
     */
    private static final int[] DAYS_OF_MONTHS = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * 1970 年 1 月 1 日的星期.
     */
    private static final int WEEK_OF_19700101 = 4;

    /**
     * 返回某日期的星期.
     */
    public static int getWeek(int[] date) {
        return (getDaysFrom19700101(date) + WEEK_OF_19700101) % 7;
    }

    /**
     * 返回某年某月某日距离 1970 年 1 月 1 日的天数.
     */
    private static int getDaysFrom19700101(int[] date) {
        int days = 0;

        for (int i = 1970; i < date[0]; i++) {
            days += getDaysOfYear(i);
        }

        for (int i = 1; i < date[1]; i++) {
            days += getDaysOfMonth(date[0], i);
        }

        days += date[2] - 1;

        return days;
    }

    /**
     * 返回某年的天数.
     */
    private static int getDaysOfYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    /**
     * 返回某年某月的天数.
     */
    public static int getDaysOfMonth(int year, int month) {
        return month == 2 && isLeapYear(year) ? 29 : DAYS_OF_MONTHS[month - 1];
    }

    /**
     * 返回某年是否为闰年.
     */
    private static boolean isLeapYear(int year) {
        return year % 4 == 0;
    }

    /**
     * 返回今天日期.
     */
    public static int[] getTodayDate() {
        Calendar calendar = Calendar.getInstance();

        int[] todayDate = new int[3];
        todayDate[0] = calendar.get(Calendar.YEAR);
        todayDate[1] = calendar.get(Calendar.MONTH) + 1;
        todayDate[2] = calendar.get(Calendar.DATE);

        return todayDate;
    }

    /**
     * 计算日期.
     */
    public static int[] addDate(int[] date, int add) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(date[0], date[1] - 1, date[2]);
        calendar.add(Calendar.DATE, add);

        int[] newDate = new int[3];
        newDate[0] = calendar.get(Calendar.YEAR);
        newDate[1] = calendar.get(Calendar.MONTH) + 1;
        newDate[2] = calendar.get(Calendar.DATE);

        return newDate;
    }

    /**
     * 计算星期.
     */
    public static int addWeek(int week, int add) {
        return (week + add) % 7;
    }

    /**
     * 是否在某日期之后.
     */
    public static boolean isDateAfter(int[] thisDate, int[] date, boolean canEqual) {
        return thisDate[0] > date[0]
                || thisDate[0] == date[0] && thisDate[1] > date[1]
                || thisDate[0] == date[0] && thisDate[1] == date[1]
                && (canEqual ? thisDate[2] >= date[2] : thisDate[2] > date[2]);
    }

    /**
     * 是否在某日期之前.
     */
    public static boolean isDateBefore(int[] thisDate, int[] date, boolean canEqual) {
        return thisDate[0] < date[0]
                || thisDate[0] == date[0] && thisDate[1] < date[1]
                || thisDate[0] == date[0] && thisDate[1] == date[1]
                && (canEqual ? thisDate[2] <= date[2] : thisDate[2] < date[2]);
    }

    /**
     * 是否在两个日期之间.
     */
    public static boolean isDateBetween(int[] thisDate, int[] dateAfter, int[] dateBefore, boolean canEqualAfter,
            boolean canEqualBefore) {
        return isDateAfter(thisDate, dateAfter, canEqualAfter) && isDateBefore(thisDate, dateBefore, canEqualBefore);
    }

    /**
     * 是否与某日期相等.
     */
    public static boolean isDateEqual(int[] thisDate, int[] date) {
        return thisDate[0] == date[0] && thisDate[1] == date[1] && thisDate[2] == date[2];
    }

    /**
     * 返回日期是否有效.
     */
    public static boolean isDateValid(int[] date) {
        return isDateBetween(date, new int[]{getInstance().year_from, getInstance().month_from, 1}, getTodayDate(),
                true, true);
    }

    /**
     * 返回某月的最后一个星期日的日期.
     */
    public static int getLastSundayOfMonth(int daysOfMonth, int weekOfFirstDayOfMonth) {
        return (daysOfMonth + weekOfFirstDayOfMonth - 1) / 7 * 7 - weekOfFirstDayOfMonth + 1;
    }

    /**
     * 返回日期字符串.
     */
    public static String getDateString(int[] date) {
        return String.format(getInstance().format_date, date[0], date[1], date[2]);
    }

    //*****************************************************************************************************************
    // Cache.

    private static Util sInstance;

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new Util(context);
        }
    }

    public static Util getInstance() {
        return sInstance;
    }

    public final int transparent;
    public final int background_day;
    public final int background_selected;
    public final int background_ranged;
    public final int background_disabled;
    public final int text_day;
    public final int text_selected;
    public final int text_today;
    public final int text_special;
    public final int text_festival;
    public final int text_weekend;
    public final int text_disabled;

    public final int year_from;
    public final int month_from;
    public final int special_count;
    public final int max_double_selected_count;

    public final String special;
    public final String today;
    public final String format_month;
    public final String format_date;
    private final String key_festival;

    public final Map<Integer, Map<Integer, Map<Integer, String>>> festivals;

    private Util(Context context) {
        transparent = context.getResources().getColor(R.color.transparent);
        background_day = context.getResources().getColor(R.color.background_day);
        background_selected = context.getResources().getColor(R.color.background_selected);
        background_ranged = context.getResources().getColor(R.color.background_ranged);
        background_disabled = context.getResources().getColor(R.color.background_disabled);
        text_day = context.getResources().getColor(R.color.text_day);
        text_selected = context.getResources().getColor(R.color.text_selected);
        text_today = context.getResources().getColor(R.color.text_today);
        text_special = context.getResources().getColor(R.color.text_special);
        text_festival = context.getResources().getColor(R.color.text_festival);
        text_weekend = context.getResources().getColor(R.color.text_weekend);
        text_disabled = context.getResources().getColor(R.color.text_disabled);

        year_from = context.getResources().getInteger(R.integer.year_from);
        month_from = context.getResources().getInteger(R.integer.month_from);
        special_count = context.getResources().getInteger(R.integer.special_count);
        max_double_selected_count = context.getResources().getInteger(R.integer.max_double_selected_count);

        special = context.getString(R.string.special);
        today = context.getString(R.string.today);
        format_month = context.getString(R.string.format_month);
        format_date = context.getString(R.string.format_date);
        key_festival = context.getString(R.string.key_festival);

        festivals = getFestivals(context);
    }

    /**
     * 读取 recycler_calendar_festival.json 文件并返回节日 map.
     */
    private Map<Integer, Map<Integer, Map<Integer, String>>> getFestivals(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.recycler_calendar_festival);
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(is));
            String string;
            while ((string = br.readLine()) != null) {
                stringBuilder.append(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rootJsonString = stringBuilder.toString();

        Map<Integer, Map<Integer, Map<Integer, String>>> festivals = new ArrayMap<>();

        try {
            JSONObject rootJsonObject = new JSONObject(rootJsonString);
            JSONObject festivalJsonObject = rootJsonObject.getJSONObject(key_festival);
            Iterator<String> yearKeys = festivalJsonObject.keys();
            while (yearKeys.hasNext()) {
                String yearKey = yearKeys.next();
                int year = Integer.parseInt(yearKey);
                Map<Integer, Map<Integer, String>> monthMap = new ArrayMap<>();
                JSONObject yearJsonObject = festivalJsonObject.getJSONObject(yearKey);
                Iterator<String> monthKeys = yearJsonObject.keys();
                while (monthKeys.hasNext()) {
                    String monthKey = monthKeys.next();
                    int month = Integer.parseInt(monthKey);
                    Map<Integer, String> dayMap = new ArrayMap<>();
                    JSONObject monthJsonObject = yearJsonObject.getJSONObject(monthKey);
                    Iterator<String> dayKeys = monthJsonObject.keys();
                    while (dayKeys.hasNext()) {
                        String dayKey = dayKeys.next();
                        int day = Integer.parseInt(dayKey);
                        String festival = monthJsonObject.getString(dayKey);
                        dayMap.put(day, festival);
                    }
                    monthMap.put(month, dayMap);
                }
                festivals.put(year, monthMap);
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }

        return festivals;
    }
}
