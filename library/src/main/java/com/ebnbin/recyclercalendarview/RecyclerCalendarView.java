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
import java.util.Locale;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    /**
     * 单选类型.
     */
    public static final int SELECT_SINGLE = 0;
    /**
     * 双选类型.
     */
    public static final int SELECT_DOUBLE = 1;

    private PinnedHeaderRecyclerView mCalendarRecyclerView;

    private GridLayoutManager mCalendarLayoutManager;

    private List<CalendarEntity> mCalendarData;

    private CalendarAdapter mCalendarAdapter;

    /**
     * 如果为 false 则为单选类型, 否则为双选类型.
     */
    private boolean mDoubleSelect;

    /**
     * 当前单选选中 position.
     */
    private int mSingleSelectedPosition;

    /**
     * 滚动到的 position, 如果为 -1 则不滚动.
     */
    private int mScrollToPosition = -1;

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

        mCalendarRecyclerView = (PinnedHeaderRecyclerView) findViewById(R.id.calendar);

        mCalendarLayoutManager = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(mCalendarLayoutManager);

        mCalendarAdapter = new CalendarAdapter();
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);

        mCalendarRecyclerView.setPinnedHeaderView(R.layout.item_year_month);

        setYearMonthRange(Util.YEAR_FROM, Util.MONTH_FROM, Util.YEAR_TO, Util.MONTH_TO);

        int[] date = Util.getDate();
        singleSelect(date[0], date[1], date[2]);
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

        mCalendarData = Util.getCalendarData(yearFrom, monthFrom, yearTo, monthTo);
        mCalendarAdapter.setNewData(mCalendarData);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        scrollToPosition(mScrollToPosition);
    }

    /**
     * 滚动到指定的 position.
     *
     * @param scrollToPosition
     *         如果为 -1 则不滚动.
     */
    private void scrollToPosition(int scrollToPosition) {
        mScrollToPosition = scrollToPosition;

        int calendarRecyclerViewMeasuredHeight = mCalendarRecyclerView.getMeasuredHeight();
        if (mScrollToPosition == -1 || calendarRecyclerViewMeasuredHeight == 0) {
            return;
        }

        if (mDoubleSelect) {
        } else {
            int offset = calendarRecyclerViewMeasuredHeight / 2;
            mCalendarLayoutManager.scrollToPositionWithOffset(mScrollToPosition, offset);
            mScrollToPosition = -1;
        }
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
            addItemType(CalendarEntity.TYPE_PLACEHOLDER, R.layout.view_divider);
        }

        @Override
        protected void convert(final BaseViewHolder helper, final CalendarEntity item) {
            switch (helper.getItemViewType()) {
                case CalendarEntity.TYPE_YEAR_MONTH: {
                    helper.setText(R.id.year_month, item.yearMonthString);

                    break;
                }
                case CalendarEntity.TYPE_DATE: {
                    helper.getView(R.id.root).setEnabled(item.dayEnabled);

                    switch (item.selected) {
                        case CalendarEntity.SELECTED_UNSELECTED: {
                            helper.setBackgroundColor(R.id.root,
                                    getResources().getColor(R.color.unselected_background));
                            helper.setTextColor(R.id.day, getResources().getColor(R.color.unselected_text));
                            break;
                        }
                        case CalendarEntity.SELECTED_SELECTED: {
                            helper.setBackgroundColor(R.id.root, getResources().getColor(R.color.selected_background));
                            helper.setTextColor(R.id.day, getResources().getColor(R.color.selected_text));
                            break;
                        }
                        case CalendarEntity.SELECTED_RANGED: {
                            helper.setBackgroundColor(R.id.root, getResources().getColor(R.color.ranged_background));
                            helper.setTextColor(R.id.day, getResources().getColor(R.color.selected_text));
                            break;
                        }
                    }

                    helper.getView(R.id.root).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.dayEnabled) {
                                if (mDoubleSelect) {
                                } else {
                                    singleSelect(helper, item);
                                }
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

    /**
     * 点击 item 单选.
     */
    private void singleSelect(BaseViewHolder helper, CalendarEntity item) {
        internalSingleSelect(helper.getLayoutPosition(), item.year, item.month, item.day, true);
    }

    /**
     * 指定年月日单选.
     */
    public void singleSelect(int year, int month, int day) {
        int position = Util.getPosition(mCalendarData, year, month, day);
        internalSingleSelect(position, year, month, day, false);

        scrollToPosition(position);
    }

    /**
     * 内部单选方法.
     *
     * @param callback
     *         是否回调.
     */
    private void internalSingleSelect(int position, int year, int month, int day, boolean callback) {
        if (mSingleSelectedPosition != position) {
            mCalendarData.get(mSingleSelectedPosition).selected = CalendarEntity.SELECTED_UNSELECTED;
            mSingleSelectedPosition = position;
        }
        mCalendarData.get(position).selected = CalendarEntity.SELECTED_SELECTED;

        if (callback) {
            onSingleSelected(year, month, day);
        }

        mCalendarAdapter.notifyDataSetChanged();
    }

    /**
     * 单选回调.
     */
    private void onSingleSelected(int year, int month, int day) {
        Toast.makeText(getContext(), String.format(Locale.getDefault(), "%d年%d月%d日", year, month, day),
                Toast.LENGTH_SHORT).show();
    }
}
