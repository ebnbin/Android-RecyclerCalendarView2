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
import android.widget.Toast;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.oushangfeng.pinnedsectionitemdecoration.utils.FullSpanUtil;

import java.util.List;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    private RecyclerView mCalendarRecyclerView;

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

        mCalendarRecyclerView = (RecyclerView) findViewById(R.id.calendar);

        RecyclerView.LayoutManager layout = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(layout);

        RecyclerView.ItemDecoration decor = new PinnedHeaderItemDecoration.Builder(CalendarEntity.TYPE_YEAR_MONTH)
                .enableDivider(true).setDividerId(R.drawable.divider).create();
        mCalendarRecyclerView.addItemDecoration(decor);

        mCalendarAdapter = new CalendarAdapter();
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);

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
    private final class CalendarAdapter extends BaseMultiItemQuickAdapter<CalendarEntity, BaseViewHolder> {
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

            FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, CalendarEntity.TYPE_YEAR_MONTH);
            FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, CalendarEntity.TYPE_PLACEHOLDER);
        }
    }
}
