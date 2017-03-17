package com.ebnbin.recyclercalendarview;

final class CalendarDayEntity implements CalendarYearMonthEntity {
    /**
     * 日期.
     */
    public final int[] date;

    /**
     * 是否为今天.
     */
    public final boolean isToday;
    /**
     * 是否为可用.
     */
    public final boolean isEnabled;

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
     * 是否选中.
     */
    public boolean selected;

    CalendarDayEntity(int[] date, int[] todayDate, int lastSundayOfMonth) {
        this.date = date;
        this.isToday = Util.isDateEqual(date, todayDate);
        this.isEnabled = Util.isDateBefore(date, todayDate, true);
        this.isLastSundayOfMonth = date[2] == lastSundayOfMonth;
        this.monthString = String.format(Util.getInstance().format_month, date[0], date[1]);
        this.dayString = String.valueOf(date[2]);
        this.selected = isToday;
    }

    @Override
    public int getItemType() {
        return ITEM_TYPE_DAY;
    }

    @Override
    public int getYear() {
        return date[0];
    }

    @Override
    public int getMonth() {
        return date[1];
    }

    @Override
    public String getMonthString() {
        return monthString;
    }

    /**
     * 返回文字颜色.
     */
    public int getTextColor() {
        // 不可用.
        if (!isEnabled) {
            return Util.getInstance().text_disabled;
        }

        // 选中的.
        if (selected) {
            return Util.getInstance().text_selected;
        }

        // 今天.
        if (isToday) {
            return Util.getInstance().text_today;
        }

        // 默认.
        return Util.getInstance().text_day;
    }

    /**
     * 返回背景颜色.
     */
    public int getBackgroundColor() {
        // 不可用.
        if (!isEnabled) {
            return Util.getInstance().background_disabled;
        }

        // 选中的.
        if (selected) {
            return Util.getInstance().background_selected;
        }

        return Util.getInstance().background_day;
    }
}
