package com.ebnbin.recyclercalendarview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

/**
 * 日历 adapter.
 */
final class CalendarAdapter extends BaseMultiItemQuickAdapter<Entity, BaseViewHolder> {
    CalendarAdapter() {
        super(null);

        addItemType(Entity.ITEM_TYPE_MONTH, R.layout.item_month);
        addItemType(Entity.ITEM_TYPE_DAY, R.layout.item_day);
        addItemType(Entity.ITEM_TYPE_EMPTY_DAY, R.layout.item_empty_day);
    }

    @Override
    protected void convert(final BaseViewHolder helper, Entity item) {
        switch (helper.getItemViewType()) {
            case Entity.ITEM_TYPE_MONTH: {
                MonthEntity monthEntity = (MonthEntity) item;

                helper.setText(R.id.month, monthEntity.monthString);

                break;
            }
            case Entity.ITEM_TYPE_DAY: {
                final DayEntity dayEntity = (DayEntity) item;

                helper.getConvertView().setEnabled(dayEntity.isValid);
                helper.getConvertView().setBackgroundResource(dayEntity.getBackgroundResource());
                helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null && dayEntity.isValid) {
                            listener.onDayClick(helper.getLayoutPosition());
                        }
                    }
                });

                helper.setText(R.id.day, dayEntity.dayString);
                helper.setTextColor(R.id.day, dayEntity.getTextColor());

                break;
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        final GridLayoutManager.SpanSizeLookup oldSizeLookup = gridLayoutManager.getSpanSizeLookup();
        final int spanCount = gridLayoutManager.getSpanCount();

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int itemType = getItemViewType(position);
                if (itemType == Entity.ITEM_TYPE_MONTH) {
                    return spanCount;
                }

                if (oldSizeLookup != null) {
                    return oldSizeLookup.getSpanSize(position);
                }

                return 1;
            }
        });
    }

    //*****************************************************************************************************************
    // Listener.

    public Listener listener;

    static abstract class Listener {
        void onDayClick(int position) {
        }
    }
}
