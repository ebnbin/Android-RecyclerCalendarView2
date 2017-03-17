package com.ebnbin.recyclercalendarview;

final class CalendarMonthEntity implements CalendarYearMonthEntity {
    /**
     * 年.
     */
    public final int year;
    /**
     * 月.
     */
    public final int month;

    /**
     * 月字符串.
     */
    public final String monthString;

    CalendarMonthEntity(int year, int month) {
        this.year = year;
        this.month = month;
        this.monthString = String.format(Util.getInstance().format_month, year, month);
    }

    @Override
    public int getItemType() {
        return ITEM_TYPE_MONTH;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public String getMonthString() {
        return monthString;
    }
}
