package com.ebnbin.recyclercalendarview;

final class EmptyDayEntity implements Entity {
    @Override
    public int getItemType() {
        return ITEM_TYPE_EMPTY_DAY;
    }
}
