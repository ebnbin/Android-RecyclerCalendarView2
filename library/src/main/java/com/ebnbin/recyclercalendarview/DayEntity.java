package com.ebnbin.recyclercalendarview;

final class DayEntity implements Entity {
    /**
     * 日期.
     */
    public final int[] date;

    /**
     * 是否为今天.
     */
    public final boolean isToday;
    /**
     * 是否为有效.
     */
    public final boolean isValid;

    /**
     * 日字符串.
     */
    public final String dayString;

    /**
     * 是否选中.
     */
    public boolean selected;

    DayEntity(int[] date) {
        this.date = date;
        isToday = Util.isToday(this.date);
        isValid = true;
        dayString = String.valueOf(date[2]);
        selected = false;
    }

    @Override
    public int getItemType() {
        return ITEM_TYPE_DAY;
    }

    /**
     * 返回文字颜色.
     */
    public int getTextColor() {
        // 选中的.
        if (selected) {
            return Util.getInstance().text_selected;
        }

        // 今天.
        if (isToday) {
            return Util.getInstance().text_today;
        }

        // 无效的.
        if (!isValid) {
            return Util.getInstance().text_disabled;
        }

        // 默认.
        return Util.getInstance().text_day;
    }

    /**
     * 返回背景资源 id.
     */
    public int getBackgroundResource() {
        // 选中的.
        if (selected) {
            return R.drawable.background_day_selected;
        }

        // 无效的.
        if (!isValid) {
            return R.drawable.background_day_invalid;
        }

        return 0;
    }
}
