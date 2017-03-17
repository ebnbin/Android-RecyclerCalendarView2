package com.ebnbin.recyclercalendarview;

import android.content.Context;

import java.util.Calendar;

/**
 * 工具类.
 */
final class Util {
    /**
     * 平年月份天数.
     */
    private static final int[] DAYS_OF_MONTHS = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * 返回某日期的星期.
     */
    public static int getWeek(int[] date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(date[0], date[1] - 1, date[2]);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
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
    public final int background_disabled;
    public final int text_day;
    public final int text_selected;
    public final int text_today;
    public final int text_disabled;

    public final String format_month;
    public final String format_date;

    private Util(Context context) {
        transparent = context.getResources().getColor(R.color.transparent);
        background_day = context.getResources().getColor(R.color.background_day);
        background_selected = context.getResources().getColor(R.color.background_selected);
        background_disabled = context.getResources().getColor(R.color.background_disabled);
        text_day = context.getResources().getColor(R.color.text_day);
        text_selected = context.getResources().getColor(R.color.text_selected);
        text_today = context.getResources().getColor(R.color.text_today);
        text_disabled = context.getResources().getColor(R.color.text_disabled);

        format_month = context.getString(R.string.format_month);
        format_date = context.getString(R.string.format_date);
    }
}
