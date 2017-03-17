package com.ebnbin.recyclercalendarview;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 日历实体类.
 */
final class CalendarEntity {
    /**
     * 返回一个日历数据.
     */
    public static List<CalendarEntity> newCalendarData(boolean doubleSelectedMode) {
        List<CalendarEntity> calendarData = new ArrayList<>();

        int[] specialDateBefore = Util.addDate(Util.getTodayDate(),
                doubleSelectedMode ? 0 : Util.getInstance().special_count);
        int yearTo = specialDateBefore[0];
        int monthTo = specialDateBefore[1];

        int weekOfFirstDayOfMonth = Util.getWeek(
                new int[]{Util.getInstance().year_from, Util.getInstance().month_from, 1});

        for (int year = Util.getInstance().year_from; year <= yearTo; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == Util.getInstance().year_from && month < Util.getInstance().month_from
                        || year == yearTo && month > monthTo) {
                    continue;
                }

                calendarData.add(new CalendarEntity(ITEM_TYPE_MONTH, doubleSelectedMode, new int[]{year, month, 0}));

                for (int emptyDay = 0; emptyDay < weekOfFirstDayOfMonth; emptyDay++) {
                    calendarData.add(new CalendarEntity(ITEM_TYPE_EMPTY_DAY, doubleSelectedMode,
                            new int[]{year, month, 0}));
                }

                int daysOfMonth = Util.getDaysOfMonth(year, month);

                for (int day = 1; day <= daysOfMonth; day++) {
                    calendarData.add(new CalendarEntity(ITEM_TYPE_DAY, doubleSelectedMode,
                            new int[]{year, month, day}));
                }

                weekOfFirstDayOfMonth = Util.addWeek(weekOfFirstDayOfMonth, daysOfMonth);
            }
        }

        calendarData.add(new CalendarEntity(ITEM_TYPE_DIVIDER, doubleSelectedMode, new int[3]));

        return calendarData;
    }

    //*****************************************************************************************************************
    // 实体.

    /**
     * 月类型.
     */
    public static final int ITEM_TYPE_MONTH = 0;
    /**
     * 日类型.
     */
    public static final int ITEM_TYPE_DAY = 1;
    /**
     * 空白日类型.
     */
    public static final int ITEM_TYPE_EMPTY_DAY = 2;
    /**
     * 分隔线类型.
     */
    public static final int ITEM_TYPE_DIVIDER = 3;

    /**
     * 未选中的.
     */
    public static final int SELECTED_TYPE_UNSELECTED = 0;
    /**
     * 已选中的.
     */
    public static final int SELECTED_TYPE_SELECTED = 1;
    /**
     * 选中范围的.
     */
    public static final int SELECTED_TYPE_RANGED = 2;

    /**
     * 类型.
     */
    public final int itemType;

    /**
     * 如果为 true 则为双选模式, 否则为单选模式.
     */
    public final boolean doubleSelectedMode;

    /**
     * 日期.
     */
    public final int[] date;

    /**
     * 选中类型
     */
    public int selectedType;

    private CalendarEntity(int itemType, boolean doubleSelectedMode, int[] date) {
        this.itemType = itemType;
        this.doubleSelectedMode = doubleSelectedMode;
        this.date = date;

        selectedType = this.doubleSelectedMode || !isToday() ? SELECTED_TYPE_UNSELECTED : SELECTED_TYPE_SELECTED;
    }

    /**
     * 节日.
     */
    public String getFestival() {
        if (itemType != ITEM_TYPE_DAY) {
            return null;
        }

        String festival = null;
        Map<Integer, Map<Integer, Map<Integer, String>>> festivals = Util.getInstance().festivals;
        if (festivals.get(date[0]) != null && festivals.get(date[0]).get(date[1]) != null) {
            festival = festivals.get(date[0]).get(date[1]).get(date[2]);
        }
        return festival;
    }

    /**
     * 星期.
     */
    public int getWeek() {
        if (itemType != ITEM_TYPE_DAY) {
            return -1;
        }

        return Util.getWeek(date);
    }

    /**
     * 是否为今天.
     */
    public boolean isToday() {
        if (itemType != ITEM_TYPE_DAY) {
            return false;
        }

        return Util.isDateEqual(date, Util.getTodayDate());
    }

    /**
     * 是否为现在.
     */
    public boolean isPresent() {
        if (itemType != ITEM_TYPE_DAY) {
            return false;
        }

        return Util.isDateBefore(date, Util.getTodayDate(), true);
    }

    /**
     * 是否为可用.
     */
    public boolean isEnabled() {
        if (itemType != ITEM_TYPE_DAY) {
            return false;
        }

        return isPresent() || !TextUtils.isEmpty(getSpecialString());
    }

    /**
     * 是否为周末.
     */
    public boolean isWeekend() {
        if (itemType != ITEM_TYPE_DAY) {
            return false;
        }

        int week = getWeek();
        return week == 0 || week == 6;
    }

    /**
     * 是否为当前月的最后一个星期日.
     */
    public boolean isLastSundayOfMonth() {
        if (itemType != ITEM_TYPE_DAY) {
            return false;
        }

        return date[2] == Util.getLastSundayOfMonth(date[0], date[1]);
    }

    /**
     * 月字符串.
     */
    public String getMonthString() {
        if (itemType != ITEM_TYPE_MONTH && itemType != ITEM_TYPE_DAY && itemType != ITEM_TYPE_EMPTY_DAY) {
            return null;
        }

        return String.format(Util.getInstance().format_month, date[0], date[1]);
    }

    /**
     * 日字符串.
     */
    public String getDayString() {
        if (itemType != ITEM_TYPE_DAY) {
            return null;
        }

        String festival = getFestival();
        return isToday() ? Util.getInstance().today
                : !TextUtils.isEmpty(festival) ? festival : String.valueOf(date[2]);
    }

    /**
     * 特殊字符串.
     */
    public String getSpecialString() {
        if (itemType != ITEM_TYPE_DAY) {
            return null;
        }

        int[] todayDate = Util.getTodayDate();
        int[] specialDateBefore = Util.addDate(todayDate, doubleSelectedMode ? 0 : Util.getInstance().special_count);
        return Util.isDateBetween(date, todayDate, specialDateBefore, false, true) ? Util.getInstance().special : "";
    }

    /**
     * 返回文字颜色.
     */
    public int getTextColor() {
        // 非日类型.
        if (itemType != ITEM_TYPE_DAY) {
            return Util.getInstance().transparent;
        }

        // 不可用.
        if (!isEnabled()) {
            return Util.getInstance().text_disabled;
        }

        // 选中的.
        if (selectedType != SELECTED_TYPE_UNSELECTED) {
            return Util.getInstance().text_selected;
        }

        // 今天.
        if (isToday()) {
            return Util.getInstance().text_today;
        }

        // 特殊.
        if (!TextUtils.isEmpty(getSpecialString())) {
            return Util.getInstance().text_special;
        }

        // 节日.
        if (!TextUtils.isEmpty(getFestival())) {
            return Util.getInstance().text_festival;
        }

        // 周末.
        if (isWeekend()) {
            return Util.getInstance().text_weekend;
        }

        // 默认.
        return Util.getInstance().text_day;
    }

    /**
     * 返回背景颜色.
     */
    public int getBackgroundColor() {
        // 非日类型.
        if (itemType != ITEM_TYPE_DAY) {
            return Util.getInstance().transparent;
        }

        // 不可用.
        if (!isEnabled()) {
            return Util.getInstance().background_disabled;
        }

        // 选中类型.
        switch (selectedType) {
            case SELECTED_TYPE_SELECTED: {
                return Util.getInstance().background_selected;
            }
            case SELECTED_TYPE_RANGED: {
                return Util.getInstance().background_ranged;
            }
            default: {
                return Util.getInstance().background_day;
            }
        }
    }
}
