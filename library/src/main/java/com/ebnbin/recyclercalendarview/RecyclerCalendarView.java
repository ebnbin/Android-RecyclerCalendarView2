package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    private PinnedHeaderRecyclerView mCalendarPinnedHeaderRecyclerView;

    private CalendarAdapter mCalendarAdapter;

    public RecyclerCalendarView(@NonNull Context context) {
        super(context);

        init();
    }

    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_recycler_calendar, this);

        mCalendarPinnedHeaderRecyclerView = (PinnedHeaderRecyclerView) findViewById(R.id.calendar);

        RecyclerView.LayoutManager layout = new GridLayoutManager(getContext(), 7);
        mCalendarPinnedHeaderRecyclerView.setLayoutManager(layout);

        mCalendarAdapter = new CalendarAdapter();
        mCalendarPinnedHeaderRecyclerView.setAdapter(mCalendarAdapter);

        mCalendarPinnedHeaderRecyclerView.setPinnedHeaderView(R.layout.item_year_month);

        setYearMonthRange(Util.YEAR_FROM, Util.MONTH_FROM, Util.YEAR_TO, Util.MONTH_TO);
    }

    /**
     * 设置年月范围. 年份 1970 ~ 2037, 月份 1 ~ 12, to 日期必须大于 from 日期.
     */
    public void setYearMonthRange(int yearFrom, int monthFrom, int yearTo, int monthTo) {
        if (yearFrom < Util.MIN_YEAR || yearFrom > Util.MAX_YEAR
                || monthFrom < 1 || monthFrom > 12
                || yearTo < yearFrom
                || yearTo == yearFrom && monthTo < monthFrom) {
            return;
        }

        List<CalendarEntity> data = Util.getCalendarData(yearFrom, monthFrom, yearTo, monthTo);
        mCalendarAdapter.setNewData(data);
    }

    /**
     * 日历 adapter.
     */
    private final class CalendarAdapter extends BaseMultiItemQuickAdapter<CalendarEntity, BaseViewHolder>
            implements PinnedHeaderRecyclerView.PinnedHeaderAdapter {
        public CalendarAdapter() {
            super(null);

            addItemType(CalendarEntity.TYPE_YEAR_MONTH, R.layout.item_year_month);
            addItemType(CalendarEntity.TYPE_DATE, R.layout.item_date);
            addItemType(CalendarEntity.TYPE_PLACEHOLDER, R.layout.item_placeholder);
        }

        @Override
        protected void convert(BaseViewHolder helper, final CalendarEntity item) {
            switch (helper.getItemViewType()) {
                case CalendarEntity.TYPE_YEAR_MONTH: {
                    helper.setText(R.id.year_month, item.yearMonthString);

                    break;
                }
                case CalendarEntity.TYPE_DATE: {
                    helper.getView(R.id.root).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.dayEnabled) {
                                Toast.makeText(getContext(), item.dateString, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    helper.setText(R.id.day, item.dayString);

                    break;
                }
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);

            Util.fullSpan(recyclerView, this, CalendarEntity.TYPE_YEAR_MONTH);
            Util.fullSpan(recyclerView, this, CalendarEntity.TYPE_PLACEHOLDER);
        }

        @Override
        public int getPinnedHeaderState(int position) {
            return getItem(position).isLastSundayOfYearMonth
                    ? PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_PUSHABLE
                    : PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_VISIBLE;
        }

        @Override
        public void configurePinnedHeader(View pinnedHeaderView, int position) {
            int itemType = getItemViewType(position);
            if (itemType == CalendarEntity.TYPE_YEAR_MONTH || itemType == CalendarEntity.TYPE_DATE) {
                TextView yearMonthTextView = (TextView) pinnedHeaderView.findViewById(R.id.year_month);
                yearMonthTextView.setText(getItem(position).yearMonthString);
            }
        }
    }
}
