package com.ebnbin.recyclercalendarview;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
     * 是否为今天.
     */
    public static boolean isToday(int[] date) {
        return isDateEqual(date, getTodayDate());
    }

    /**
     * 范围是否有效.
     */
    public static boolean isRangeValid(int yearFrom, int monthFrom, int yearTo, int monthTo) {
        return yearFrom >= 1970
                && yearFrom <= 2037
                && monthFrom >= 1
                && monthFrom <= 12
                && yearTo <= 2037
                && monthTo <= 12
                && (yearTo > yearFrom || yearTo == yearFrom && monthTo >= monthFrom);
    }

    /**
     * 日期是否有效.
     */
    public static boolean isDateValid(int[] date) {
        return date != null
                && date.length == 3
                && date[0] >= 1970
                && date[0] <= 2037
                && date[1] >= 1
                && date[1] <= 12
                && date[2] >= 1
                && date[2] <= getDaysOfMonth(date[0], date[1]);
    }

    //*****************************************************************************************************************
    // Entity.

    /**
     * 返回一个日历数据.
     */
    public static List<Entity> newCalendarData(int yearFrom, int monthFrom, int yearTo, int monthTo) {
        List<Entity> calendarData = new ArrayList<>();

        int weekOfFirstDayOfMonth = Util.getWeek(new int[]{yearFrom, monthFrom, 1});

        for (int year = yearFrom; year <= yearTo; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == yearFrom && month < monthFrom || year == yearTo && month > monthTo) {
                    continue;
                }

                Entity monthEntity = new MonthEntity(year, month);
                calendarData.add(monthEntity);

                for (int emptyDay = 0; emptyDay < weekOfFirstDayOfMonth; emptyDay++) {
                    Entity emptyDayEntity = new EmptyDayEntity();
                    calendarData.add(emptyDayEntity);
                }

                int daysOfMonth = Util.getDaysOfMonth(year, month);
                for (int day = 1; day <= daysOfMonth; day++) {
                    Entity dayEntity = new DayEntity(new int[]{year, month, day});
                    calendarData.add(dayEntity);
                }

                weekOfFirstDayOfMonth = Util.addWeek(weekOfFirstDayOfMonth, daysOfMonth);
            }
        }

        return calendarData;
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

    public final int text_day;
    public final int text_selected;
    public final int text_today;
    public final int text_disabled;

    public final String format_month;

    private Util(Context context) {
        text_day = context.getColor(R.color.text_normal);
        text_selected = context.getColor(R.color.text_selected);
        text_today = context.getColor(R.color.text_today);
        text_disabled = context.getColor(R.color.text_disabled);

        format_month = context.getString(R.string.format_month);
    }
}
