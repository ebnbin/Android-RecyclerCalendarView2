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
    private PinnedHeaderRecyclerView mCalendarRecyclerView;

    private GridLayoutManager mCalendarLayoutManager;

    private List<CalendarEntity> mCalendarData;

    private CalendarAdapter mCalendarAdapter;

    /**
     * 如果为 false 则为单选, 否则为双选.
     */
    private boolean mDoubleSelected;

    /**
     * 最大双选数量.
     */
    private int mMaxDoubleSelectedCount;

    /**
     * 当前单选选中的 position.
     */
    private int mSingleSelectedPosition;

    /**
     * 当前双选选中的第一个 position.
     */
    private int mDoubleSelectedPositionA;
    /**
     * 当前双选选中的第二个 position.
     */
    private int mDoubleSelectedPositionB;

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

        mMaxDoubleSelectedCount = Util.MAX_DOUBLE_SELECTED_COUNT;

        int[] date = Util.getDate();
        setSelected(date[0], date[1], date[2]);
    }

    /**
     * 返回最大双选数量.
     */
    public int getMaxDoubleSelectedCount() {
        return mMaxDoubleSelectedCount;
    }

    /**
     * 返回是否双选.
     */
    public boolean isDoubleSelected() {
        return mDoubleSelected;
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

    /**
     * 设置最大双选数量, 并清除全部选中日期.
     */
    public void setMaxDoubleSelectedCount(int maxDoubleSelectedCount) {
        mMaxDoubleSelectedCount = maxDoubleSelectedCount;

        clearSelected();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        scrollToPosition(mScrollToPosition);
    }

    /**
     * 滚动到今天.
     */
    private void scrollToToday() {
        int[] date = Util.getDate();
        int position = Util.getPosition(mCalendarData, date[0], date[1], date[2]);
        scrollToPosition(position);
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

        int offset = calendarRecyclerViewMeasuredHeight / 2;
        mCalendarLayoutManager.scrollToPositionWithOffset(mScrollToPosition, offset);
        mScrollToPosition = -1;
    }

    /**
     * 设置是否双选, 并清除全部选中日期.
     */
    public void setDoubleSelected(boolean doubleSelected) {
        setDoubleSelect(doubleSelected, false);
    }

    /**
     * 设置是否双选, 并清除全部选中日期.
     *
     * @param scrollToPosition
     *         如果为 true 则滚动到今天, 默认为 false.
     */
    public void setDoubleSelect(boolean doubleSelect, boolean scrollToPosition) {
        mDoubleSelected = doubleSelect;

        clearSelected();

        if (scrollToPosition) {
            scrollToToday();
        }
    }

    /**
     * 设置单选日期并滚动到选中的 position.
     */
    public void setSelected(int year, int month, int day) {
        setSelected(year, month, day, true);
    }

    /**
     * 设置单选日期.
     *
     * @param scrollToPosition
     *         如果为 true 则滚动到选中的 position.
     */
    public void setSelected(int year, int month, int day, boolean scrollToPosition) {
        setDoubleSelected(false);

        int position = Util.getPosition(mCalendarData, year, month, day);

        setPositionSelected(position, CalendarEntity.SELECTED_SELECTED);
        mSingleSelectedPosition = position;

        mCalendarAdapter.notifyDataSetChanged();

        if (scrollToPosition) {
            scrollToPosition(position);
        }
    }

    /**
     * 设置双选日期并滚动到选中的开始 position.
     */
    public void setSelected(int fromYear, int fromMonth, int fromDay, int toYear, int toMonth, int toDay) {
        setSelected(fromYear, fromMonth, fromDay, toYear, toMonth, toDay, true);
    }

    /**
     * 设置双选日期.
     *
     * @param scrollToPosition
     *         如果为 true 则滚动到选中的开始 position.
     */
    public void setSelected(int fromYear, int fromMonth, int fromDay, int toYear, int toMonth, int toDay,
            boolean scrollToPosition) {
        setDoubleSelected(true);

        int fromPosition = Util.getPosition(mCalendarData, fromYear, fromMonth, fromDay);
        int toPosition = Util.getPosition(mCalendarData, toYear, toMonth, toDay);

        int selectedCount = 0;
        for (int i = fromPosition; i <= toPosition; i++) {
            if (mCalendarAdapter.getItem(i).dayEnabled) {
                ++selectedCount;
            }
        }
        if (selectedCount > mMaxDoubleSelectedCount) {
            onExceedMaxDoubleSelectedCount(selectedCount);
            return;
        }

        setPositionSelected(fromPosition, CalendarEntity.SELECTED_SELECTED);
        mDoubleSelectedPositionA = fromPosition;
        setPositionSelected(toPosition, CalendarEntity.SELECTED_SELECTED);
        mDoubleSelectedPositionB = toPosition;
        for (int i = fromPosition + 1; i < toPosition; i++) {
            setPositionSelected(i, CalendarEntity.SELECTED_RANGED);
        }

        mCalendarAdapter.notifyDataSetChanged();

        if (scrollToPosition) {
            scrollToPosition(fromPosition);
        }
    }

    /**
     * 清除全部选中日期.
     */
    public void clearSelected() {
        if (mSingleSelectedPosition != -1) {
            setPositionSelected(mSingleSelectedPosition, CalendarEntity.SELECTED_UNSELECTED);
            mSingleSelectedPosition = -1;
        }
        if (mDoubleSelectedPositionA != -1 && mDoubleSelectedPositionB != -1) {
            int fromPosition = Math.min(mDoubleSelectedPositionA, mDoubleSelectedPositionB);
            int toPosition = Math.max(mDoubleSelectedPositionA, mDoubleSelectedPositionB);
            for (int i = fromPosition; i <= toPosition; i++) {
                setPositionSelected(i, CalendarEntity.SELECTED_UNSELECTED);
            }
            mDoubleSelectedPositionA = -1;
            mDoubleSelectedPositionB = -1;
        } else {
            if (mDoubleSelectedPositionA != -1) {
                setPositionSelected(mDoubleSelectedPositionA, CalendarEntity.SELECTED_UNSELECTED);
                mDoubleSelectedPositionA = -1;
            }
            if (mDoubleSelectedPositionB != -1) {
                setPositionSelected(mDoubleSelectedPositionB, CalendarEntity.SELECTED_UNSELECTED);
                mDoubleSelectedPositionB = -1;
            }
        }

        mCalendarAdapter.notifyDataSetChanged();
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
                                clickDate(helper.getLayoutPosition(), item.year, item.month, item.day);
                            }
                        }
                    });

                    helper.setText(R.id.day, item.dayString);

                    break;
                }
            }
        }

        /**
         * 点击某日期.
         */
        private void clickDate(int position, int year, int month, int day) {
            if (mDoubleSelected) {
                // 双选.
                if (mDoubleSelectedPositionA == -1) {
                    // 两个都未选中.
                    if (mDoubleSelectedPositionB != -1) {
                        // 非正常情况, 容错代码.
                        setPositionSelected(mDoubleSelectedPositionB, CalendarEntity.SELECTED_UNSELECTED);
                        mDoubleSelectedPositionB = -1;
                    }
                    // 要选中第一个.
                    setPositionSelected(position, CalendarEntity.SELECTED_SELECTED);
                    mDoubleSelectedPositionA = position;
                    onDoubleFirstSelected(year, month, day);
                } else if (mDoubleSelectedPositionB == -1) {
                    // 已选中第一个.
                    if (position == mDoubleSelectedPositionA) {
                        // 要取消选中第一个.
                        setPositionSelected(position, CalendarEntity.SELECTED_UNSELECTED);
                        mDoubleSelectedPositionA = -1;
                        onDoubleFirstUnselected(year, month, day);
                    } else {
                        // 要选中第二个.
                        int fromPosition = Math.min(mDoubleSelectedPositionA, position);
                        int toPosition = Math.max(mDoubleSelectedPositionA, position);

                        int selectedCount = 0;
                        for (int i = fromPosition; i <= toPosition; i++) {
                            if (mCalendarAdapter.getItem(i).dayEnabled) {
                                ++selectedCount;
                            }
                        }

                        if (selectedCount <= mMaxDoubleSelectedCount) {
                            setPositionSelected(position, CalendarEntity.SELECTED_SELECTED);
                            mDoubleSelectedPositionB = position;

                            for (int i = fromPosition + 1; i < toPosition; i++) {
                                setPositionSelected(i, CalendarEntity.SELECTED_RANGED);
                            }
                            CalendarEntity fromCalendarEntity = mCalendarData.get(fromPosition);
                            CalendarEntity toCalendarEntity = mCalendarData.get(toPosition);
                            onDoubleSelected(fromCalendarEntity.year, fromCalendarEntity.month, fromCalendarEntity.day,
                                    toCalendarEntity.year, toCalendarEntity.month, toCalendarEntity.day,
                                    selectedCount);
                        } else {
                            onExceedMaxDoubleSelectedCount(selectedCount);
                        }
                    }
                } else {
                    // 两个都已选中.
                    int fromPosition = Math.min(mDoubleSelectedPositionA, mDoubleSelectedPositionB);
                    int toPosition = Math.max(mDoubleSelectedPositionA, mDoubleSelectedPositionB);
                    for (int i = fromPosition; i <= toPosition; i++) {
                        setPositionSelected(i, CalendarEntity.SELECTED_UNSELECTED);
                    }
                    mDoubleSelectedPositionB = -1;
                    setPositionSelected(position, CalendarEntity.SELECTED_SELECTED);
                    mDoubleSelectedPositionA = position;
                    onDoubleFirstSelected(year, month, day);
                }
            } else {
                if (mSingleSelectedPosition != position) {
                    // 要选中另一个.
                    if (mSingleSelectedPosition != -1) {
                        setPositionSelected(mSingleSelectedPosition, CalendarEntity.SELECTED_UNSELECTED);
                    }
                    setPositionSelected(position, CalendarEntity.SELECTED_SELECTED);
                    mSingleSelectedPosition = position;
                }

                onSingleSelected(year, month, day);
            }

            mCalendarAdapter.notifyDataSetChanged();
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
     * 设置某位置的选中状态.
     */
    private void setPositionSelected(int position, int selected) {
        CalendarEntity calendarEntity = mCalendarData.get(position);
        if (calendarEntity.dayEnabled) {
            calendarEntity.selected = selected;
        }
    }

    /**
     * 单选回调.
     */
    private void onSingleSelected(int year, int month, int day) {
        Toast.makeText(getContext(), String.format(Locale.getDefault(), "%d年%d月%d日", year, month, day),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选回调.
     */
    private void onDoubleSelected(int yearFrom, int monthFrom, int dayFrom, int yearTo, int monthTo, int dayTo,
            int count) {
        Toast.makeText(getContext(), String.format(Locale.getDefault(), "%d年%d月%d日 ~ %d年%d月%d日 %d天", yearFrom,
                monthFrom, dayFrom, yearTo, monthTo, dayTo, count), Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选选中第一个日期回调.
     */
    private void onDoubleFirstSelected(int year, int month, int day) {
        Toast.makeText(getContext(), String.format(Locale.getDefault(), "已选中 %d年%d月%d日", year, month, day),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选取消选中第一个日期回调.
     */
    private void onDoubleFirstUnselected(int year, int month, int day) {
        Toast.makeText(getContext(), String.format(Locale.getDefault(), "已取消选中 %d年%d月%d日", year, month, day),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 天数超过限制回调.
     *
     * @param count
     *         选中天数.
     */
    private void onExceedMaxDoubleSelectedCount(int count) {
        Toast.makeText(getContext(), String.format(Locale.getDefault(), "天数超过限制 %d天", count), Toast.LENGTH_SHORT)
                .show();
    }
}
