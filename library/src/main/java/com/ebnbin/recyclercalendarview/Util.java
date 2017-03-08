package com.ebnbin.recyclercalendarview;

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
     * 返回某月的最后一个星期日的日期.
     */
    public static int getLastSundayOfMonth(int daysOfMonth, int weekOfFirstDayOfMonth) {
        return (daysOfMonth + weekOfFirstDayOfMonth - 1) / 7 * 7 - weekOfFirstDayOfMonth + 1;
    }
}
