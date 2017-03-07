package com.ebnbin.recyclercalendarview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

/**
 * 工具类.
 */
final class Util {
    /**
     * 默认开始年份.
     */
    public static final int YEAR_FROM = 2011;
    /**
     * 默认开始月份.
     */
    public static final int MONTH_FROM = 1;
    /**
     * 默认最大双选数量.
     */
    public static final int MAX_DOUBLE_SELECTED_COUNT = 90;
    /**
     * 默认特殊日期天数.
     */
    public static final int SPECIAL_DAYS = 15;

    /**
     * 返回指定日期在 calendarData 中的 position, 如果没找到则返回 -1.
     */
    public static int getPosition(List<CalendarEntity> calendarData, int[] date) {
        for (int position = 0; position < calendarData.size(); position++) {
            CalendarEntity calendarEntity = calendarData.get(position);
            if (isDateEqual(new int[]{calendarEntity.year, calendarEntity.month, calendarEntity.day}, date)) {
                return position;
            }
        }

        return -1;
    }

    /**
     * 平年月份天数.
     */
    private static final int[] DAYS_OF_MONTHS = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * 1970 年 1 月 1 日的星期.
     */
    private static final int WEEK_OF_19700101 = 4;

    /**
     * 返回某年某月 1 日的星期.
     */
    public static int getWeekOfFirstDayOfMonth(int year, int month) {
        return (getDaysFrom19700101(year, month, 1) + WEEK_OF_19700101) % 7;
    }

    /**
     * 返回某年某月某日距离 1970 年 1 月 1 日的天数.
     */
    private static int getDaysFrom19700101(int year, int month, int day) {
        int days = 0;

        for (int i = 1970; i < year; i++) {
            days += getDaysOfYear(i);
        }

        for (int i = 1; i < month; i++) {
            days += getDaysOfMonth(year, i);
        }

        days += day - 1;

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
     * Copy from https://github.com/oubowu/PinnedSectionItemDecoration.
     */
    public static void fullSpan(RecyclerView recyclerView, final RecyclerView.Adapter adapter,
            final int pinnedHeaderType) {
        // 如果是网格布局，这里处理标签的布局占满一行
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup oldSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter.getItemViewType(position) == pinnedHeaderType) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (oldSizeLookup != null) {
                        return oldSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
        }
    }

    public static int[] getTodayDate() {
        return getDateFromToday(0);
    }

    public static int[] getDateFromToday(int add) {
        int[] todayDate = new int[3];

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, add);

        todayDate[0] = calendar.get(Calendar.YEAR);
        todayDate[1] = calendar.get(Calendar.MONTH) + 1;
        todayDate[2] = calendar.get(Calendar.DATE);

        return todayDate;
    }

    public static boolean isDateAfter(int[] thisDate, int[] date, boolean canEqual) {
        return thisDate[0] > date[0]
                || thisDate[0] == date[0] && thisDate[1] > date[1]
                || thisDate[0] == date[0] && thisDate[1] == date[1]
                && (canEqual ? thisDate[2] >= date[2] : thisDate[2] > date[2]);
    }

    public static boolean isDateBefore(int[] thisDate, int[] date, boolean canEqual) {
        return thisDate[0] < date[0]
                || thisDate[0] == date[0] && thisDate[1] < date[1]
                || thisDate[0] == date[0] && thisDate[1] == date[1]
                && (canEqual ? thisDate[2] <= date[2] : thisDate[2] < date[2]);
    }

    public static boolean isDateBetween(int[] thisDate, int[] dateAfter, int[] dateBefore, boolean canEqualAfter,
            boolean canEqualBefore) {
        return isDateAfter(thisDate, dateAfter, canEqualAfter) && isDateBefore(thisDate, dateBefore, canEqualBefore);
    }

    public static boolean isDateEqual(int[] thisDate, int[] date) {
        return thisDate[0] == date[0] && thisDate[1] == date[1] && thisDate[2] == date[2];
    }

    public static int getLastSundayOfMonth(int daysOfMonth, int weekOfFirstDayOfMonth) {
        return (daysOfMonth + weekOfFirstDayOfMonth - 1) / 7 * 7 - weekOfFirstDayOfMonth + 1;
    }

    public static int getWeek(int current, int add) {
        return (current + add) % 7;
    }
}
