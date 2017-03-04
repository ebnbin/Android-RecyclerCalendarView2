package com.ebnbin.recyclercalendarview;

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

    /**
     * 类型.
     */
    public final int itemType;

    /**
     * 日是否有效.
     */
    public final boolean dayEnabled;

    /**
     * 年月字符串.
     */
    public final String yearMonthString;
    /**
     * 日字符串.
     */
    public final String dayString;
    /**
     * 日期字符串.
     */
    public final String dateString;

    /**
     * 创建一个年月类型的日历实体.
     */
    public CalendarEntity(int year, int month) {
        this(year, month, DAY_NOT_DATE);
    }

    /**
     * 创建一个日期类型的日历实体.
     */
    public CalendarEntity(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        itemType = this.day == DAY_NOT_DATE ? TYPE_YEAR_MONTH : TYPE_DATE;

        dayEnabled = itemType == TYPE_DATE && this.day != DAY_DISABLED;

        yearMonthString = String.format(Locale.getDefault(), "%d年%d月", this.year, this.month);
        dayString = this.dayEnabled ? String.valueOf(this.day) : "";
        dateString = String.format(Locale.getDefault(), "%s%d日", this.yearMonthString, this.day);
    }

    /**
     * 创建一个占位符类型的日历实体.
     */
    public CalendarEntity() {
        year = DATE_PLACEHOLDER;
        month = DATE_PLACEHOLDER;
        day = DATE_PLACEHOLDER;

        itemType = TYPE_PLACEHOLDER;

        dayEnabled = false;

        yearMonthString = "";
        dayString = "";
        dateString = "";
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
