package com.ebnbin.recyclercalendarview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
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
     * 默认结束年份.
     */
    public static final int YEAR_TO;
    /**
     * 默认结束月份.
     */
    public static final int MONTH_TO;

    static {
        int[] date = getDate();

        YEAR_TO = date[0];
        MONTH_TO = date[1];
    }

    /**
     * 返回一个长度为 3 的数组分别表示今天的年月日.
     */
    private static int[] getDate() {
        int[] date = new int[3];

        Calendar calendar = Calendar.getInstance();
        date[0] = calendar.get(Calendar.YEAR);
        date[1] = calendar.get(Calendar.MONTH) + 1;
        date[2] = calendar.get(Calendar.DATE);

        return date;
    }

    /**
     * 返回日历数据.
     */
    public static List<CalendarEntity> getCalendarData(int yearFrom, int monthFrom, int yearTo, int monthTo) {
        List<CalendarEntity> calendarData = new ArrayList<>();

        int weekOfFirstDayOfYearMonth = getWeekOfFirstDayOfYearMonth(yearFrom, monthFrom);
        for (int year = yearFrom; year <= yearTo; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == yearFrom && month < monthFrom || year == yearTo && month > monthTo) {
                    continue;
                }

                CalendarEntity yearMonthCalendarEntity = new CalendarEntity(year, month);
                calendarData.add(yearMonthCalendarEntity);

                for (int disabledDay = 0; disabledDay < weekOfFirstDayOfYearMonth; disabledDay++) {
                    CalendarEntity disabledDayCalendarEntity = new CalendarEntity(year, month,
                            CalendarEntity.DAY_DISABLED, false);
                    calendarData.add(disabledDayCalendarEntity);
                }

                int daysOfYearMonth = getDaysOfYearMonth(year, month);
                int lastSundayOfYearMonth = (daysOfYearMonth + weekOfFirstDayOfYearMonth - 1) / 7 * 7 -
                        weekOfFirstDayOfYearMonth + 1;
                for (int day = 1; day <= daysOfYearMonth; day++) {
                    boolean isLastSundayOfYearMonth = day == lastSundayOfYearMonth;
                    CalendarEntity dateCalendarEntity = new CalendarEntity(year, month, day,
                            isLastSundayOfYearMonth);
                    calendarData.add(dateCalendarEntity);
                }

                weekOfFirstDayOfYearMonth = (weekOfFirstDayOfYearMonth + daysOfYearMonth) % 7;
            }
        }

        CalendarEntity placeHolderCalendarEntity = new CalendarEntity();
        calendarData.add(placeHolderCalendarEntity);

        return calendarData;
    }

    /**
     * 最小年份.
     */
    public static final int MIN_YEAR = 1970;
    /**
     * 最大年份.
     */
    public static final int MAX_YEAR = 2037;

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
    private static int getWeekOfFirstDayOfYearMonth(int year, int month) {
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
            days += getDaysOfYearMonth(year, i);
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
    private static int getDaysOfYearMonth(int year, int month) {
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
}
