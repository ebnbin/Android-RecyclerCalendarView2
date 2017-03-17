package com.ebnbin.recyclercalendarview;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * 日历实体类.
 */
interface Entity extends MultiItemEntity {
    /**
     * 月类型.
     */
    int ITEM_TYPE_MONTH = 0;
    /**
     * 日类型.
     */
    int ITEM_TYPE_DAY = 1;
    /**
     * 空白日类型.
     */
    int ITEM_TYPE_EMPTY_DAY = 2;
}