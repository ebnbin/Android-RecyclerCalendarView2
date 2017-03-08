package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 日历 adapter.
 */
final class CalendarAdapter extends RecyclerView.Adapter implements PinnedHeaderRecyclerView.PinnedHeaderAdapter {
    private final Context mContext;

    private final GridLayoutManager mCalendarLayoutManager;

    private final LayoutInflater mLayoutInflater;

    private final List<CalendarEntity> mCalendarData = new ArrayList<>();

    private OnDayClickListener mOnDayClickListener;

    CalendarAdapter(Context context, GridLayoutManager calendarLayoutManager) {
        mContext = context;

        mCalendarLayoutManager = calendarLayoutManager;

        mLayoutInflater = LayoutInflater.from(mContext);
    }

    /**
     * 不为 null, 可能为 empty.
     */
    public List<CalendarEntity> getCalendarData() {
        return mCalendarData;
    }

    /**
     * 如果为 null 则清空数据.
     */
    public void setCalendarData(List<CalendarEntity> calendarData) {
        mCalendarData.clear();

        if (calendarData != null) {
            mCalendarData.addAll(calendarData);
        }
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CalendarEntity.ITEM_TYPE_MONTH: {
                return new MonthViewHolder(mLayoutInflater.inflate(R.layout.item_month, parent, false));
            }
            case CalendarEntity.ITEM_TYPE_DAY: {
                return new DayViewHolder(mLayoutInflater.inflate(R.layout.item_day, parent, false));
            }
            case CalendarEntity.ITEM_TYPE_EMPTY_DAY: {
                return new EmptyDayViewHolder(mLayoutInflater.inflate(R.layout.item_empty_day, parent, false));
            }
            case CalendarEntity.ITEM_TYPE_DIVIDER: {
                return new DividerViewHolder(mLayoutInflater.inflate(R.layout.item_divider, parent, false));
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        final int layoutPosition = holder.getLayoutPosition();
        final CalendarEntity calendarEntity = getCalendarEntity(holder.getLayoutPosition());

        switch (viewType) {
            case CalendarEntity.ITEM_TYPE_MONTH: {
                MonthViewHolder monthViewHolder = (MonthViewHolder) holder;

                monthViewHolder.monthTextView.setText(calendarEntity.monthString);

                break;
            }
            case CalendarEntity.ITEM_TYPE_DAY: {
                DayViewHolder dayViewHolder = (DayViewHolder) holder;

                dayViewHolder.itemView.setEnabled(calendarEntity.isEnabled);
                dayViewHolder.itemView.setBackgroundColor(getColor(calendarEntity.getBackgroundColor()));
                dayViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnDayClickListener != null && calendarEntity.isEnabled) {
                            mOnDayClickListener.onDayClick(layoutPosition);
                        }
                    }
                });

                dayViewHolder.dayTextView.setText(calendarEntity.dayString);
                dayViewHolder.dayTextView.setTextColor(getColor(calendarEntity.getTextColor()));

                dayViewHolder.specialTextView.setText(calendarEntity.specialString);
                dayViewHolder.specialTextView.setTextColor(getColor(calendarEntity.getTextColor()));

                break;
            }
        }
    }

    private int getColor(int resId) {
        return mContext.getResources().getColor(resId);
    }

    @Override
    public int getItemCount() {
        return mCalendarData.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        final GridLayoutManager.SpanSizeLookup oldSizeLookup = mCalendarLayoutManager.getSpanSizeLookup();
        final int spanCount = mCalendarLayoutManager.getSpanCount();

        mCalendarLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
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
    public int getItemViewType(int position) {
        return getCalendarEntity(position).itemType;
    }

    public CalendarEntity getCalendarEntity(int position) {
        return mCalendarData.get(position);
    }

    @Override
    public int getPinnedHeaderState(int position) {
        return getCalendarEntity(position).isLastSundayOfMonth
                ? PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_PUSHABLE
                : PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View pinnedHeaderView, int position) {
        TextView yearMonthTextView = (TextView) pinnedHeaderView.findViewById(R.id.month);
        yearMonthTextView.setText(getCalendarEntity(position).monthString);
    }

    //*****************************************************************************************************************
    // Listener.

    static abstract class OnDayClickListener {
        void onDayClick(int position) {
        }
    }

    //*****************************************************************************************************************
    // ViewHolder.

    private static final class MonthViewHolder extends RecyclerView.ViewHolder {
        public final TextView monthTextView;

        MonthViewHolder(View itemView) {
            super(itemView);

            monthTextView = (TextView) itemView.findViewById(R.id.month);
        }
    }

    private static final class DayViewHolder extends RecyclerView.ViewHolder {
        public final TextView dayTextView;
        public final TextView specialTextView;

        DayViewHolder(View itemView) {
            super(itemView);

            dayTextView = (TextView) itemView.findViewById(R.id.day);
            specialTextView = (TextView) itemView.findViewById(R.id.special);
        }
    }

    private static final class EmptyDayViewHolder extends RecyclerView.ViewHolder {
        EmptyDayViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static final class DividerViewHolder extends RecyclerView.ViewHolder {
        DividerViewHolder(View itemView) {
            super(itemView);
        }
    }
}
