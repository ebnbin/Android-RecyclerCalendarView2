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
     * 默认最大双选数量.
     */
    public static final int MAX_DOUBLE_SELECTED_COUNT = 90;

    /**
     * 默认特殊日期天数.
     */
    public static final int SPECIAL_DAYS = 15;
    /**
     * 默认特殊日期字符串.
     */
    public static final String SPECIAL_STRING = "预售";

    /**
     * 返回一个长度为 3 的数组分别表示今天的年月日.
     */
    public static int[] getDate() {
        int[] date = new int[3];

        Calendar calendar = Calendar.getInstance();
        date[0] = calendar.get(Calendar.YEAR);
        date[1] = calendar.get(Calendar.MONTH) + 1;
        date[2] = calendar.get(Calendar.DATE);

        return date;
    }

    /**
     * 日期计算.
     *
     * @param days
     *         增加天数.
     *
     * @return 计算后的日期.
     */
    public static int[] addDate(int year, int month, int day, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.add(Calendar.DATE, days);

        int[] date = new int[3];
        date[0] = calendar.get(Calendar.YEAR);
        date[1] = calendar.get(Calendar.MONTH);
        date[2] = calendar.get(Calendar.DATE);

        return date;
    }

    /**
     * 返回某日期是否为今天.
     */
    public static boolean isToday(int year, int month, int day) {
        int[] date = getDate();
        return year == date[0] && month == date[1] && day == date[2];
    }

    /**
     * 返回某日期是否是未来日期.
     */
    public static boolean isFuture(int year, int month, int day) {
        return isFuture(year, month, day, 0);
    }

    /**
     * 返回某日期是否是未来日期, 并在一定天数及以内.
     *
     * @param within
     *         天数范围.
     */
    public static boolean isFuture(int year, int month, int day, int within) {
        int[] date = getDate();
        int todayDaysFrom19700101 = getDaysFrom19700101(date[0], date[1], date[2]);
        int futureDaysFrom19700101 = getDaysFrom19700101(year, month, day);

        if (within <= 0) {
            return futureDaysFrom19700101 > todayDaysFrom19700101;
        } else {
            return futureDaysFrom19700101 > todayDaysFrom19700101
                    && futureDaysFrom19700101 <= todayDaysFrom19700101 + within;
        }
    }

    /**
     * 返回指定年月日的 position.
     */
    public static int getPosition(List<CalendarEntity> calendarData, int year, int month, int day) {
        for (int position = 0; position < calendarData.size(); position++) {
            CalendarEntity calendarEntity = calendarData.get(position);
            if (calendarEntity.year == year && calendarEntity.month == month && calendarEntity.day == day) {
                return position;
            }
        }

        return -1;
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
    public static int getWeekOfFirstDayOfYearMonth(int year, int month) {
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
    public static int getDaysOfYearMonth(int year, int month) {
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
