package com.ebnbin.recyclercalendarview;

final class MonthEntity implements Entity {
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

    MonthEntity(int year, int month) {
        this.year = year;
        this.month = month;
        monthString = String.format(Util.getInstance().format_month, year, month);
    }

    @Override
    public int getItemType() {
        return ITEM_TYPE_MONTH;
    }
}
