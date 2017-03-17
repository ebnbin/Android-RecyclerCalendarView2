package com.ebnbin.recyclercalendarview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

/**
 * 日历 adapter.
 */
final class CalendarAdapter extends BaseMultiItemQuickAdapter<CalendarEntity, BaseViewHolder>
        implements PinnedHeaderRecyclerView.PinnedHeaderAdapter {
    private OnDayClickListener mOnDayClickListener;

    CalendarAdapter() {
        super(null);

        addItemType(CalendarEntity.ITEM_TYPE_MONTH, R.layout.item_month);
        addItemType(CalendarEntity.ITEM_TYPE_DAY, R.layout.item_day);
        addItemType(CalendarEntity.ITEM_TYPE_EMPTY_DAY, R.layout.item_empty_day);
        addItemType(CalendarEntity.ITEM_TYPE_DIVIDER, R.layout.item_divider);
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    @Override
    protected void convert(final BaseViewHolder helper, CalendarEntity item) {
        switch (helper.getItemViewType()) {
            case CalendarEntity.ITEM_TYPE_MONTH: {
                CalendarMonthEntity monthEntity = (CalendarMonthEntity) item;

                helper.setText(R.id.month, monthEntity.monthString);

                break;
            }
            case CalendarEntity.ITEM_TYPE_DAY: {
                final CalendarDayEntity dayEntity = (CalendarDayEntity) item;

                helper.getConvertView().setEnabled(dayEntity.isEnabled);
                helper.getConvertView().setBackgroundColor(dayEntity.getBackgroundColor());
                helper.getConvertView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnDayClickListener != null && dayEntity.isEnabled) {
                            mOnDayClickListener.onDayClick(helper.getLayoutPosition());
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
                if (itemType == CalendarEntity.ITEM_TYPE_MONTH || itemType == CalendarEntity.ITEM_TYPE_DIVIDER) {
                    return spanCount;
                }
                if (oldSizeLookup != null) {
                    return oldSizeLookup.getSpanSize(position);
                }
                return 1;
            }
        });
    }

    @Override
    public int getPinnedHeaderState(int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType != CalendarEntity.ITEM_TYPE_DAY) {
            return PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_VISIBLE;
        }

        return ((CalendarDayEntity) getItem(position)).isLastSundayOfMonth
                ? PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_PUSHABLE
                : PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View pinnedHeaderView, int position) {
        CalendarEntity calendarEntity = getItem(position);
        if (!(calendarEntity instanceof CalendarYearMonthEntity)) {
            return;
        }

        CalendarYearMonthEntity calendarYearMonthEntity = (CalendarYearMonthEntity) calendarEntity;

        TextView yearMonthTextView = (TextView) pinnedHeaderView.findViewById(R.id.month);
        yearMonthTextView.setText(calendarYearMonthEntity.getMonthString());
    }

    //*****************************************************************************************************************
    // Listener.

    static abstract class OnDayClickListener {
        void onDayClick(int position) {
        }
    }
}
