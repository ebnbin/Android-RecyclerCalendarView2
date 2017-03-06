package com.ebnbin.recyclercalendarview;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.Locale;

/**
 * 日历实体类.
 */
final class CalendarEntity implements MultiItemEntity {
    /**
     * 年月类型.
     */
    public static final int TYPE_YEAR_MONTH = 0;
    /**
     * 日期类型.
     */
    public static final int TYPE_DATE = 1;
    /**
     * 占位符类型.
     */
    public static final int TYPE_PLACEHOLDER = 2;

    /**
     * 非日期类型的日.
     */
    private static final int DAY_NOT_DATE = -1;
    /**
     * 无效的日.
     */
    public static final int DAY_DISABLED = 0;

    /**
     * 占位符类型的日期.
     */
    private static final int DATE_PLACEHOLDER = -2;

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
     * 类型.
     */
    public final int itemType;

    /**
     * 日是否有效.
     */
    public final boolean dayEnabled;

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
    public CalendarEntity(int year, int month) {
        this(year, month, DAY_NOT_DATE, DAY_NOT_DATE, null, false);
    }

    /**
     * 创建一个日期类型的日历实体.
     */
    public CalendarEntity(int year, int month, int day, int week, String festival, boolean isLastSundayOfYearMonth) {
        this.year = year;
        this.month = month;
        this.day = day;

        this.week = week;

        this.festival = festival;

        isToday = Util.isToday(this.year, this.month, this.day);
        isWeekend = this.week == 0 || this.week == 6;
        isFestival = !TextUtils.isEmpty(this.festival);

        itemType = this.day == DAY_NOT_DATE ? TYPE_YEAR_MONTH : TYPE_DATE;

        dayEnabled = itemType == TYPE_DATE && this.day != DAY_DISABLED;

        this.isLastSundayOfYearMonth = isLastSundayOfYearMonth;

        yearMonthString = String.format(Locale.getDefault(), "%d年%d月", this.year, this.month);
        if (this.dayEnabled) {
            if (isToday) {
                dayString = "今天";
            } else if (isFestival) {
                dayString = this.festival;
            } else {
                dayString = String.valueOf(this.day);
            }
        } else {
            dayString = "";
        }
    }

    /**
     * 创建一个占位符类型的日历实体.
     */
    public CalendarEntity() {
        year = DATE_PLACEHOLDER;
        month = DATE_PLACEHOLDER;
        day = DATE_PLACEHOLDER;

        isToday = false;
        week = DATE_PLACEHOLDER;
        festival = null;

        isWeekend = false;
        isFestival = false;

        itemType = TYPE_PLACEHOLDER;

        dayEnabled = false;

        isLastSundayOfYearMonth = false;

        yearMonthString = "";
        dayString = "";
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
