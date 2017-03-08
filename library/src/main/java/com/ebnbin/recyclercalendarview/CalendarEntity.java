package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 日历实体类.
 */
final class CalendarEntity {
    /**
     * 返回一个日历数据.
     */
    public static List<CalendarEntity> newCalendarData(Context context, boolean doubleSelectedMode, int[] todayDate,
            int specialCount, int yearFrom, int monthFrom) {
        List<CalendarEntity> calendarData = new ArrayList<>();

        int[] specialDateBefore = Util.addDate(todayDate, doubleSelectedMode ? 0 : specialCount);

        int yearTo = specialDateBefore[0];
        int monthTo = specialDateBefore[1];

        Map<int[], String> festivals = getFestivals(context);

        int week = Util.getWeek(new int[]{yearFrom, monthFrom, 1});
        int weekOfFirstDayOfMonth = week;

        for (int year = yearFrom; year <= yearTo; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == yearFrom && month < monthFrom || year == yearTo && month > monthTo) {
                    continue;
                }

                CalendarEntity monthCalendarEntity = new CalendarEntity(context, year, month, ITEM_TYPE_MONTH);
                calendarData.add(monthCalendarEntity);

                for (int emptyDay = 0; emptyDay < weekOfFirstDayOfMonth; emptyDay++) {
                    CalendarEntity emptyDayCalendarEntity = new CalendarEntity(context, year, month,
                            ITEM_TYPE_EMPTY_DAY);
                    calendarData.add(emptyDayCalendarEntity);
                }

                int daysOfMonth = Util.getDaysOfMonth(year, month);
                int lastSundayOfMonth = Util.getLastSundayOfMonth(daysOfMonth, weekOfFirstDayOfMonth);

                for (int day = 1; day <= daysOfMonth; day++) {
                    CalendarEntity dayCalendarEntity = new CalendarEntity(context, new int[]{year, month, day},
                            todayDate, specialDateBefore, festivals, week, lastSundayOfMonth, doubleSelectedMode);
                    calendarData.add(dayCalendarEntity);

                    week = Util.addWeek(week, 1);
                }

                weekOfFirstDayOfMonth = Util.addWeek(weekOfFirstDayOfMonth, daysOfMonth);
            }
        }

        CalendarEntity dividerCalendarEntity = new CalendarEntity();
        calendarData.add(dividerCalendarEntity);

        return calendarData;
    }

    /**
     * 读取 festival.json 文件并返回节日 map, key 为日期, value 为节日.
     */
    private static Map<int[], String> getFestivals(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream is = context.getResources().openRawResource(R.raw.festival);
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

        String rootKey = context.getString(R.string.festival_root_key);

        Map<int[], String> festivals = new ArrayMap<>();

        try {
            JSONObject rootJsonObject = new JSONObject(rootJsonString);
            JSONObject festivalJsonObject = rootJsonObject.getJSONObject(rootKey);
            Iterator<String> yearKeys = festivalJsonObject.keys();
            while (yearKeys.hasNext()) {
                String yearKey = yearKeys.next();
                int year = Integer.parseInt(yearKey);
                JSONObject yearJsonObject = festivalJsonObject.getJSONObject(yearKey);
                Iterator<String> monthKeys = yearJsonObject.keys();
                while (monthKeys.hasNext()) {
                    String monthKey = monthKeys.next();
                    int month = Integer.parseInt(monthKey);
                    JSONObject monthJsonObject = yearJsonObject.getJSONObject(monthKey);
                    Iterator<String> dayKeys = monthJsonObject.keys();
                    while (dayKeys.hasNext()) {
                        String dayKey = dayKeys.next();
                        int day = Integer.parseInt(dayKey);
                        String festival = monthJsonObject.getString(dayKey);
                        festivals.put(new int[]{year, month, day}, festival);
                    }
                }
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }

        return festivals;
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
     * 日期.
     */
    public final int[] date;
    /**
     * 年.
     */
    public final int year;
    /**
     * 月.
     */
    public final int month;
    /**
     * 日.
     */
    public final int day;

    /**
     * 特殊.
     */
    public final String special;
    /**
     * 节日.
     */
    public final String festival;

    /**
     * 星期.
     */
    public final int week;

    /**
     * 是否为今天.
     */
    public final boolean isToday;
    /**
     * 是否为现在.
     */
    public final boolean isPresent;
    /**
     * 是否为特殊.
     */
    public final boolean isSpecial;
    /**
     * 是否为可用.
     */
    public final boolean isEnabled;
    /**
     * 是否为节日.
     */
    public final boolean isFestival;
    /**
     * 是否为周末.
     */
    public final boolean isWeekend;

    /**
     * 是否为当前月的最后一个星期日.
     */
    public final boolean isLastSundayOfMonth;

    /**
     * 月字符串.
     */
    public final String monthString;
    /**
     * 日字符串.
     */
    public final String dayString;
    /**
     * 特殊字符串.
     */
    public final String specialString;
    /**
     * 日期字符串.
     */
    public final String dateString;

    /**
     * 选中类型.
     */
    public int selectedType;

    /**
     * 创建月类型或空白日类型的对象.
     */
    private CalendarEntity(Context context, int year, int month, int itemType) {
        this.itemType = itemType;
        this.date = null;
        this.year = year;
        this.month = month;
        this.day = 0;
        this.special = null;
        this.festival = null;
        this.week = -1;
        this.isToday = false;
        this.isPresent = false;
        this.isSpecial = false;
        this.isEnabled = false;
        this.isFestival = false;
        this.isWeekend = false;
        this.isLastSundayOfMonth = false;
        this.monthString = context.getString(R.string.month_string, year, month);
        this.dayString = null;
        this.specialString = null;
        this.dateString = null;
        this.selectedType = SELECTED_TYPE_UNSELECTED;
    }

    /**
     * 创建日类型的对象.
     */
    private CalendarEntity(Context context, int[] date, int[] todayDate, int[] specialDateBefore,
            Map<int[], String> festivals, int week, int lastSundayOfMonth, boolean doubleSelectedMode) {
        String festival = null;
        for (int[] key : festivals.keySet()) {
            if (Util.isDateEqual(key, date)) {
                festival = festivals.get(key);
            }
        }

        this.itemType = ITEM_TYPE_DAY;
        this.date = date;
        this.year = date[0];
        this.month = date[1];
        this.day = date[2];
        this.special = context.getString(R.string.special);
        this.festival = festival;
        this.week = week;
        this.isToday = Util.isDateEqual(date, todayDate);
        this.isPresent = Util.isDateBefore(date, todayDate, true);
        this.isSpecial = Util.isDateBetween(date, todayDate, specialDateBefore, false, true);
        this.isEnabled = isPresent || isSpecial;
        this.isFestival = !TextUtils.isEmpty(festival);
        this.isWeekend = week == 0 || week == 6;
        this.isLastSundayOfMonth = day == lastSundayOfMonth;
        this.monthString = context.getString(R.string.month_string, year, month);
        this.dayString = isToday ? context.getString(R.string.today) : isFestival ? festival : String.valueOf(day);
        this.specialString = isSpecial ? TextUtils.isEmpty(special) ? "" : special : null;
        this.dateString = context.getString(R.string.date_string, year, month, day);
        this.selectedType = doubleSelectedMode || !isToday ? SELECTED_TYPE_UNSELECTED : SELECTED_TYPE_SELECTED;
    }

    /**
     * 创建分隔线类型的对象.
     */
    private CalendarEntity() {
        this.itemType = ITEM_TYPE_DIVIDER;
        this.date = null;
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.special = null;
        this.festival = null;
        this.week = -1;
        this.isToday = false;
        this.isPresent = false;
        this.isSpecial = false;
        this.isEnabled = false;
        this.isFestival = false;
        this.isWeekend = false;
        this.isLastSundayOfMonth = false;
        this.monthString = null;
        this.dayString = null;
        this.specialString = null;
        this.dateString = null;
        this.selectedType = SELECTED_TYPE_UNSELECTED;
    }

    /**
     * 返回文字颜色.
     */
    public int getTextColor() {
        // 非日类型.
        if (itemType != ITEM_TYPE_DAY) {
            return android.R.color.transparent;
        }

        // 不可用.
        if (!isEnabled) {
            return R.color.text_disabled;
        }

        // 选中的.
        if (selectedType != SELECTED_TYPE_UNSELECTED) {
            return R.color.text_selected;
        }

        // 今天.
        if (isToday) {
            return R.color.text_today;
        }

        // 特殊.
        if (isSpecial) {
            return R.color.text_special;
        }

        // 节日.
        if (isFestival) {
            return R.color.text_festival;
        }

        // 周末.
        if (isWeekend) {
            return R.color.text_weekend;
        }

        // 默认.
        return R.color.text_day;
    }

    /**
     * 返回背景颜色.
     */
    public int getBackgroundColor() {
        // 非日类型.
        if (itemType != ITEM_TYPE_DAY) {
            return android.R.color.transparent;
        }

        // 不可用.
        if (!isEnabled) {
            return R.color.background_disabled;
        }

        // 选中类型.
        switch (selectedType) {
            case SELECTED_TYPE_SELECTED: {
                return R.color.background_selected;
            }
            case SELECTED_TYPE_RANGED: {
                return R.color.background_ranged;
            }
            default: {
                return R.color.background_day;
            }
        }
    }
}
