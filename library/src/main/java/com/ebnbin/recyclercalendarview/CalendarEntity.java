package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 日历实体类.
 */
final class CalendarEntity implements MultiItemEntity {
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
        int[] date = Util.getDate();

        YEAR_TO = date[0];
        MONTH_TO = date[1];
    }

    /**
     * 返回日历数据.
     */
    public static List<CalendarEntity> getCalendarData(Context context, boolean doubleSelected) {
        if (doubleSelected) {
            return getCalendarData(context, YEAR_FROM, MONTH_FROM, YEAR_TO, MONTH_TO);
        }

        int[] todayDate = Util.getDate();
        int[] toDate = Util.addDate(todayDate[0], todayDate[1], todayDate[2], Util.SPECIAL_DAYS);

        int yearTo = toDate[0];
        int monthTo = toDate[1];

        return getCalendarData(context, YEAR_FROM, MONTH_FROM, yearTo, monthTo);
    }

    /**
     * 返回日历数据.
     */
    private static List<CalendarEntity> getCalendarData(Context context, int yearFrom, int monthFrom, int yearTo,
            int monthTo) {
        List<CalendarEntity> calendarData = new ArrayList<>();

        Map<Integer, Map<Integer, Map<Integer, String>>> festivals = getFestivals(context);

        int week = Util.getWeekOfFirstDayOfYearMonth(yearFrom, monthFrom);
        int weekOfFirstDayOfYearMonth = week;
        for (int year = yearFrom; year <= yearTo; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == yearFrom && month < monthFrom || year == yearTo && month > monthTo) {
                    continue;
                }

                CalendarEntity yearMonthCalendarEntity = new CalendarEntity(year, month);
                calendarData.add(yearMonthCalendarEntity);

                for (int disabledDay = 0; disabledDay < weekOfFirstDayOfYearMonth; disabledDay++) {
                    CalendarEntity emptyDayCalendarEntity = new CalendarEntity(TYPE_EMPTY_DAY);
                    calendarData.add(emptyDayCalendarEntity);
                }

                int daysOfYearMonth = Util.getDaysOfYearMonth(year, month);
                int lastSundayOfYearMonth = (daysOfYearMonth + weekOfFirstDayOfYearMonth - 1) / 7 * 7 -
                        weekOfFirstDayOfYearMonth + 1;
                for (int day = 1; day <= daysOfYearMonth; day++) {
                    boolean isLastSundayOfYearMonth = day == lastSundayOfYearMonth;

                    String festival = "";
                    if (festivals.containsKey(year)
                            && festivals.get(year).containsKey(month)
                            && festivals.get(year).get(month).containsKey(day)) {
                        festival = festivals.get(year).get(month).get(day);
                    }

                    CalendarEntity dateCalendarEntity = new CalendarEntity(year, month, day, week, festival,
                            isLastSundayOfYearMonth);
                    calendarData.add(dateCalendarEntity);

                    week = (week + 1) % 7;
                }

                weekOfFirstDayOfYearMonth = (weekOfFirstDayOfYearMonth + daysOfYearMonth) % 7;
            }
        }

        CalendarEntity dividerCalendarEntity = new CalendarEntity(TYPE_DIVIDER);
        calendarData.add(dividerCalendarEntity);

        return calendarData;
    }

    /**
     * 返回节日.
     */
    private static Map<Integer, Map<Integer, Map<Integer, String>>> getFestivals(Context context) {
        Map<Integer, Map<Integer, Map<Integer, String>>> festivals = new ArrayMap<>();

        try {
            JSONObject rootJsonObject = new JSONObject(getFestivalJsonString(context));
            JSONObject festivalJsonObject = rootJsonObject.getJSONObject("festival");
            Iterator<String> yearKeys = festivalJsonObject.keys();
            while (yearKeys.hasNext()) {
                String yearKey = yearKeys.next();
                JSONObject yearJsonObject = festivalJsonObject.getJSONObject(yearKey);
                Iterator<String> monthKeys = yearJsonObject.keys();

                Map<Integer, Map<Integer, String>> monthMap = new ArrayMap<>();
                while (monthKeys.hasNext()) {
                    String monthKey = monthKeys.next();
                    JSONObject monthJsonObject = yearJsonObject.getJSONObject(monthKey);
                    Iterator<String> dayKeys = monthJsonObject.keys();

                    Map<Integer, String> dayMap = new ArrayMap<>();
                    while (dayKeys.hasNext()) {
                        String dayKey = dayKeys.next();
                        String festival = monthJsonObject.getString(dayKey);

                        int day = Integer.parseInt(dayKey);
                        dayMap.put(day, festival);
                    }

                    int month = Integer.parseInt(monthKey);
                    monthMap.put(month, dayMap);
                }

                int year = Integer.parseInt(yearKey);
                festivals.put(year, monthMap);
            }
        } catch (NumberFormatException | JSONException e) {
            e.printStackTrace();
        }

        return festivals;
    }

    /**
     * 读取 festival.json 文件为字符串.
     */
    private static String getFestivalJsonString(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.festival);
        BufferedReader br;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String string;
            while ((string = br.readLine()) != null) {
                stringBuilder.append(string);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    //*****************************************************************************************************************

    /**
     * 年月类型.
     */
    public static final int TYPE_YEAR_MONTH = 0;
    /**
     * 日期类型.
     */
    public static final int TYPE_DATE = 1;
    /**
     * 空白日期类型.
     */
    public static final int TYPE_EMPTY_DAY = 2;
    /**
     * 分隔符类型.
     */
    public static final int TYPE_DIVIDER = 3;

    /**
     * 非日期类型的日.
     */
    private static final int DAY_NOT_DATE = -1;

    public final int year;
    public final int month;
    public final int day;

    public final int week;

    /**
     * 节日.
     */
    public final String festival;

    /**
     * 是否为今天.
     */
    public final boolean isToday;
    /**
     * 是否为周末.
     */
    public final boolean isWeekend;
    /**
     * 是否为节日.
     */
    public final boolean isFestival;
    /**
     * 是否为现在的日期.
     */
    public final boolean isPresent;
    /**
     * 是否为特殊的日期.
     */
    public final boolean isSpecial;

    /**
     * 类型.
     */
    public final int itemType;

    /**
     * 是否为当前年月的最后一个星期日.
     */
    public final boolean isLastSundayOfYearMonth;

    /**
     * 年月字符串.
     */
    public final String yearMonthString;
    /**
     * 日字符串.
     */
    public final String dayString;

    /**
     * 未选中的.
     */
    public static final int SELECTED_UNSELECTED = 0;
    /**
     * 已选中的.
     */
    public static final int SELECTED_SELECTED = 1;
    /**
     * 选中范围的.
     */
    public static final int SELECTED_RANGED = 2;

    /**
     * 选中类型.
     */
    public int selected;

    /**
     * 创建一个年月类型的日历实体.
     */
    private CalendarEntity(int year, int month) {
        this.year = year;
        this.month = month;
        day = DAY_NOT_DATE;

        week = DAY_NOT_DATE;

        festival = "";

        isToday = false;
        isWeekend = false;
        isFestival = false;
        isPresent = false;
        isSpecial = false;

        itemType = TYPE_YEAR_MONTH;

        isLastSundayOfYearMonth = false;

        yearMonthString = String.format(Locale.getDefault(), "%d年%d月", this.year, this.month);
        dayString = "";
    }

    /**
     * 创建一个日期类型的日历实体.
     */
    private CalendarEntity(int year, int month, int day, int week, String festival, boolean isLastSundayOfYearMonth) {
        this.year = year;
        this.month = month;
        this.day = day;

        this.week = week;

        this.festival = festival;

        isToday = Util.isToday(this.year, this.month, this.day);
        isWeekend = this.week == 0 || this.week == 6;
        isFestival = !TextUtils.isEmpty(this.festival);
        isPresent = !Util.isFuture(this.year, this.month, this.day);
        isSpecial = Util.isFuture(this.year, this.month, this.day, Util.SPECIAL_DAYS);

        itemType = this.day == DAY_NOT_DATE ? TYPE_YEAR_MONTH : TYPE_DATE;

        this.isLastSundayOfYearMonth = isLastSundayOfYearMonth;

        yearMonthString = String.format(Locale.getDefault(), "%d年%d月", this.year, this.month);
        if (isToday) {
            dayString = "今天";
        } else if (isFestival) {
            dayString = this.festival;
        } else {
            dayString = String.valueOf(this.day);
        }
    }

    /**
     * 创建一个月份占位符类型的日历实体.
     */
    private CalendarEntity(int placeHolderType) {
        year = DAY_NOT_DATE;
        month = DAY_NOT_DATE;
        day = DAY_NOT_DATE;

        week = DAY_NOT_DATE;

        festival = "";

        isToday = false;
        isWeekend = false;
        isFestival = false;
        isPresent = false;
        isSpecial = false;

        itemType = placeHolderType;

        isLastSundayOfYearMonth = false;

        yearMonthString = "";
        dayString = "";
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
