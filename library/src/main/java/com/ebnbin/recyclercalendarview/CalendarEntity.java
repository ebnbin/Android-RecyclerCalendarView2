package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;

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
final class CalendarEntity implements MultiItemEntity {
    /**
     * 返回一个日历数据.
     */
    public static List<CalendarEntity> newCalendarData(Context context, boolean doubleSelectedMode, int[] todayDate) {
        List<CalendarEntity> calendarData = new ArrayList<>();

        int[] specialDateBefore = Util.addDate(todayDate, doubleSelectedMode ? 0 : Util.SPECIAL_DAYS);

        int yearFrom = Util.YEAR_FROM;
        int monthFrom = Util.MONTH_FROM;
        int yearTo = specialDateBefore[0];
        int monthTo = specialDateBefore[1];

        Map<int[], String> festivals = getFestivals(context);

        int week = Util.getWeekOfFirstDayOfMonth(yearFrom, monthFrom);
        int weekOfFirstDayOfMonth = week;

        for (int year = yearFrom; year <= yearTo; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == yearFrom && month < monthFrom || year == yearTo && month > monthTo) {
                    continue;
                }

                createMonthCalendarEntity(calendarData, context, year, month);

                for (int emptyDay = 0; emptyDay < weekOfFirstDayOfMonth; emptyDay++) {
                    createEmptyDayCalendarEntity(calendarData, context, year, month);
                }

                int daysOfMonth = Util.getDaysOfMonth(year, month);
                int lastSundayOfMonth = Util.getLastSundayOfMonth(daysOfMonth, weekOfFirstDayOfMonth);

                for (int day = 1; day <= daysOfMonth; day++) {
                    createDayCalendarEntity(calendarData, context, year, month, day, todayDate, specialDateBefore,
                            festivals, week, lastSundayOfMonth, doubleSelectedMode);

                    week = Util.getWeek(week, 1);
                }

                weekOfFirstDayOfMonth = Util.getWeek(weekOfFirstDayOfMonth, daysOfMonth);
            }
        }

        createDividerCalendarEntity(calendarData);

        return calendarData;
    }

    /**
     * 返回一个月类型的日历实体对象.
     */
    private static void createMonthCalendarEntity(List<CalendarEntity> calendarData, Context context, int year,
            int month) {
        int itemType = ITEM_TYPE_MONTH;

        int day = 0;

        String special = null;
        String festival = null;

        int week = -1;

        boolean isToday = false;
        boolean isPresent = false;
        boolean isSpecial = false;
        boolean isEnabled = false;
        boolean isFestival = false;
        boolean isWeekend = false;

        boolean isLastSundayOfMonth = false;

        String monthString = context.getString(R.string.month_string, year, month);
        String dayString = null;
        String specialString = null;
        String dateString = null;

        int selectedType = SELECTED_TYPE_UNSELECTED;

        CalendarEntity monthCalendarEntity = new CalendarEntity(itemType, year, month, day, special, festival, week,
                isToday, isPresent, isSpecial, isEnabled, isFestival, isWeekend, isLastSundayOfMonth, monthString,
                dayString, specialString, dateString, selectedType);
        calendarData.add(monthCalendarEntity);
    }

    /**
     * 返回一个日类型的日历实体对象.
     */
    private static void createDayCalendarEntity(List<CalendarEntity> calendarData, Context context, int year,
            int month, int day, int[] todayDate, int[] specialDateBefore, Map<int[], String> festivals, int week,
            int lastSundayOfMonth, boolean doubleSelectedMode) {
        int[] date = new int[]{year, month, day};

        int itemType = ITEM_TYPE_DAY;

        String special = context.getString(R.string.special);
        String festival = null;
        for (int[] key : festivals.keySet()) {
            if (Util.isDateEqual(key, date)) {
                festival = festivals.get(key);
            }
        }

        boolean isToday = Util.isDateEqual(date, todayDate);
        boolean isPresent = Util.isDateBefore(date, todayDate, true);
        boolean isSpecial = Util.isDateBetween(date, todayDate, specialDateBefore, false, true);
        boolean isEnabled = isPresent || isSpecial;
        boolean isFestival = !TextUtils.isEmpty(festival);
        boolean isWeekend = week == 0 || week == 6;

        boolean isLastSundayOfMonth = day == lastSundayOfMonth;

        String monthString = context.getString(R.string.month_string, year, month);
        String dayString = isToday ? context.getString(R.string.today) : isFestival ? festival : String.valueOf(day);
        String specialString = isSpecial ? TextUtils.isEmpty(special) ? "" : special : null;
        String dateString = context.getString(R.string.date_string, year, month, day);

        int selectedType = doubleSelectedMode || !isToday ? SELECTED_TYPE_UNSELECTED : SELECTED_TYPE_SELECTED;

        CalendarEntity dayCalendarEntity = new CalendarEntity(itemType, year, month, day, special, festival, week,
                isToday, isPresent, isSpecial, isEnabled, isFestival, isWeekend, isLastSundayOfMonth, monthString,
                dayString, specialString, dateString, selectedType);
        calendarData.add(dayCalendarEntity);
    }

    /**
     * 返回一个空白日类型的日历实体对象.
     */
    private static void createEmptyDayCalendarEntity(List<CalendarEntity> calendarData, Context context, int year,
            int month) {
        int itemType = ITEM_TYPE_EMPTY_DAY;

        int day = 0;

        String special = null;
        String festival = null;

        int week = -1;

        boolean isToday = false;
        boolean isPresent = false;
        boolean isSpecial = false;
        boolean isEnabled = false;
        boolean isFestival = false;
        boolean isWeekend = false;

        boolean isLastSundayOfMonth = false;

        String monthString = context.getString(R.string.month_string, year, month);
        String dayString = null;
        String specialString = null;
        String dateString = null;

        int selectedType = SELECTED_TYPE_UNSELECTED;

        CalendarEntity emptyDayCalendarEntity =  new CalendarEntity(itemType, year, month, day, special, festival,
                week, isToday, isPresent, isSpecial, isEnabled, isFestival, isWeekend, isLastSundayOfMonth,
                monthString, dayString, specialString, dateString, selectedType);
        calendarData.add(emptyDayCalendarEntity);
    }

    /**
     * 返回一个分割线类型的日历实体对象.
     */
    private static void createDividerCalendarEntity(List<CalendarEntity> calendarData) {
        int itemType = ITEM_TYPE_DIVIDER;

        int year = 0;
        int month = 0;
        int day = 0;

        String special = null;
        String festival = null;

        int week = -1;

        boolean isToday = false;
        boolean isPresent = false;
        boolean isSpecial = false;
        boolean isEnabled = false;
        boolean isFestival = false;
        boolean isWeekend = false;

        boolean isLastSundayOfMonth = false;

        String monthString = null;
        String dayString = null;
        String specialString = null;
        String dateString = null;

        int selectedType = SELECTED_TYPE_UNSELECTED;

        CalendarEntity dividerCalendarEntity = new CalendarEntity(itemType, year, month, day, special, festival, week,
                isToday, isPresent, isSpecial, isEnabled, isFestival, isWeekend, isLastSundayOfMonth, monthString,
                dayString, specialString, dateString, selectedType);
        calendarData.add(dividerCalendarEntity);
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

    private CalendarEntity(int itemType, int year, int month, int day, String special, String festival, int week,
            boolean isToday, boolean isPresent, boolean isSpecial, boolean isEnabled, boolean isFestival,
            boolean isWeekend, boolean isLastSundayOfMonth, String monthString, String dayString,
            String specialString, String dateString, int selectedType) {
        this.itemType = itemType;
        this.year = year;
        this.month = month;
        this.day = day;
        this.special = special;
        this.festival = festival;
        this.week = week;
        this.isToday = isToday;
        this.isPresent = isPresent;
        this.isSpecial = isSpecial;
        this.isEnabled = isEnabled;
        this.isFestival = isFestival;
        this.isWeekend = isWeekend;
        this.isLastSundayOfMonth = isLastSundayOfMonth;
        this.monthString = monthString;
        this.dayString = dayString;
        this.specialString = specialString;
        this.dateString = dateString;
        this.selectedType = selectedType;
    }

    @Override
    public int getItemType() {
        return itemType;
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
            return R.color.disabled_text;
        }

        // 选中的.
        if (selectedType != SELECTED_TYPE_UNSELECTED) {
            return R.color.selected_text;
        }

        // 今天.
        if (isToday) {
            return R.color.unselected_today_text;
        }

        // 特殊.
        if (isSpecial) {
            return R.color.unselected_special_text;
        }

        // 节日.
        if (isFestival) {
            return R.color.unselected_festival_text;
        }

        // 周末.
        if (isWeekend) {
            return R.color.unselected_weekend_text;
        }

        // 默认.
        return R.color.unselected_text;
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
            return R.color.unselected_background;
        }

        // 选中类型.
        switch (selectedType) {
            case SELECTED_TYPE_SELECTED: {
                return R.color.selected_background;
            }
            case SELECTED_TYPE_RANGED: {
                return R.color.ranged_background;
            }
            default: {
                return R.color.unselected_background;
            }
        }
    }
}
