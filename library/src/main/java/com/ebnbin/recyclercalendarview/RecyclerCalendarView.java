package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    /**
     * 开始年.
     */
    private final int mYearFrom;
    /**
     * 结束年.
     */
    private final int mMonthFrom;
    /**
     * 特殊天数.
     */
    private final int mSpecialCount;
    /**
     * 最大双选天数.
     */
    private final int mMaxDoubleSelectedCount;

    /**
     * 今天日期.
     */
    private int[] mTodayDate;

    private final List<CalendarEntity> mCalendarData = new ArrayList<>();

    private PinnedHeaderRecyclerView mCalendarRecyclerView;

    private GridLayoutManager mCalendarLayoutManager;

    private CalendarAdapter mCalendarAdapter;

    public RecyclerCalendarView(@NonNull Context context) {
        this(context, null);
    }

    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mYearFrom = getResources().getInteger(R.integer.year_from);
        mMonthFrom = getResources().getInteger(R.integer.month_from);
        mSpecialCount = getResources().getInteger(R.integer.special_count);
        mMaxDoubleSelectedCount = getResources().getInteger(R.integer.max_double_selected_count);

        mTodayDate = Util.getTodayDate();

        inflate(getContext(), R.layout.view_recycler_calendar, this);

        mCalendarRecyclerView = (PinnedHeaderRecyclerView) findViewById(R.id.calendar);

        mCalendarLayoutManager = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(mCalendarLayoutManager);

        mCalendarAdapter = new CalendarAdapter();
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);

        mCalendarRecyclerView.setPinnedHeaderView(R.layout.item_month);

        setDoubleSelectedMode(false);
        scrollToSelected();
    }

    //*****************************************************************************************************************
    // 选中模式.

    /**
     * 如果为 true 则为双选模式, 否则为单选模式.
     */
    private boolean mDoubleSelectedMode;

    /**
     * 当前选中的第一个位置.
     */
    private int mSelectedPositionA = -1;
    /**
     * 当前选中的第二个位置.
     */
    private int mSelectedPositionB = -1;

    /**
     * 返回是否为双选模式.
     */
    public boolean isDoubleSelectedMode() {
        return mDoubleSelectedMode;
    }

    /**
     * 设置是否为双选模式, 并重置选中日期.
     */
    public void setDoubleSelectedMode(boolean doubleSelectedMode) {
        setDoubleSelectedMode(doubleSelectedMode, true);
    }

    /**
     * 设置单选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] date) {
        setDoubleSelectedMode(false, false);

        clickPosition(getPosition(date), true, false);
    }

    /**
     * 设置双选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] dateFrom, int[] dateTo) {
        setDoubleSelectedMode(true, false);

        clickPosition(getPosition(dateFrom), false, false);
        clickPosition(getPosition(dateTo), true, false);
    }

    private void setDoubleSelectedMode(boolean doubleSelectedMode, boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode != doubleSelectedMode) {
            mDoubleSelectedMode = doubleSelectedMode;

            mCalendarData.clear();
        }

        if (mCalendarData.isEmpty()) {
            mCalendarData.addAll(CalendarEntity.newCalendarData(getContext(), mDoubleSelectedMode, mTodayDate,
                    mSpecialCount, mYearFrom, mMonthFrom));
        }

        resetSelected(notifyDataSetChanged);
    }

    /**
     * 重置选中日期.
     */
    public void resetSelected() {
        resetSelected(true);
    }

    private void resetSelected(boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode) {
            unselectPositionAB();
        } else {
            selectPositionB(-1);
            selectPositionA(getPosition(mTodayDate));
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 点击某位置.
     */
    private void clickPosition(int position, boolean notifyDataSetChanged, boolean callback) {
        if (mDoubleSelectedMode) {
            // 双选.
            if (mSelectedPositionA == -1) {
                // 两个都未选中.
                selectPositionB(-1);
                selectPositionA(position);
                if (callback) {
                    onDoubleFirstSelected(mSelectedPositionA);
                }
            } else if (mSelectedPositionB == -1) {
                // 已选中第一个.
                if (position == mSelectedPositionA) {
                    // 要取消选中第一个.
                    selectPositionA(-1);
                    if (callback) {
                        onDoubleFirstUnselected(position);
                    }
                } else {
                    // 要选中第二个.
                    int selectedCount = getPositionABSelectedCount(mSelectedPositionA, position);
                    if (selectedCount <= mMaxDoubleSelectedCount) {
                        selectPositionAB(mSelectedPositionA, position);
                        if (callback) {
                            onDoubleSelected(mSelectedPositionA, mSelectedPositionB, selectedCount);
                        }
                    } else {
                        if (callback) {
                            onExceedMaxDoubleSelectedCount(selectedCount);
                        }
                    }
                }
            } else {
                // 两个都已选中.
                unselectPositionAB();
                selectPositionA(position);
                if (callback) {
                    onDoubleFirstSelected(mSelectedPositionA);
                }
            }
        } else {
            selectPositionA(position);
            if (callback) {
                onSingleSelected(mSelectedPositionA);
            }
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置第一个位置.
     */
    private void selectPositionA(int position) {
        if (mSelectedPositionA == position) {
            return;
        }

        if (mSelectedPositionA != -1) {
            setPositionSelected(mSelectedPositionA, CalendarEntity.SELECTED_TYPE_UNSELECTED);
            mSelectedPositionA = -1;
        }

        if (position == -1) {
            return;
        }

        setPositionSelected(position, CalendarEntity.SELECTED_TYPE_SELECTED);
        mSelectedPositionA = position;
    }

    /**
     * 设置第二个位置.
     */
    private void selectPositionB(int position) {
        if (mSelectedPositionB == position) {
            return;
        }

        if (mSelectedPositionB != -1) {
            setPositionSelected(mSelectedPositionB, CalendarEntity.SELECTED_TYPE_UNSELECTED);
            mSelectedPositionB = -1;
        }

        if (position == -1) {
            return;
        }

        setPositionSelected(position, CalendarEntity.SELECTED_TYPE_SELECTED);
        mSelectedPositionB = position;
    }

    /**
     * 返回两个位置的选中天数.
     */
    private int getPositionABSelectedCount(int positionA, int positionB) {
        if (positionA == -1 || positionB == -1) {
            return 0;
        }

        int fromPosition = Math.min(positionA, positionB);
        int toPosition = Math.max(positionA, positionB);

        int selectedCount = 0;
        for (int i = fromPosition; i <= toPosition; i++) {
            if (mCalendarAdapter.getCalendarEntity(i).itemType == CalendarEntity.ITEM_TYPE_DAY) {
                ++selectedCount;
            }
        }

        return selectedCount;
    }

    /**
     * 取消双选选中.
     */
    private void unselectPositionAB() {
        if (mSelectedPositionA != -1 && mSelectedPositionB != -1) {
            for (int i = mSelectedPositionA; i <= mSelectedPositionB; i++) {
                setPositionSelected(i, CalendarEntity.SELECTED_TYPE_UNSELECTED);
            }

            mSelectedPositionA = -1;
            mSelectedPositionB = -1;

            return;
        }

        selectPositionA(-1);
        selectPositionB(-1);
    }

    /**
     * 双选选中.
     */
    private void selectPositionAB(int positionA, int positionB) {
        if (positionA == -1 || positionB == -1) {
            return;
        }

        int fromPosition = Math.min(positionA, positionB);
        int toPosition = Math.max(positionA, positionB);

        selectPositionA(fromPosition);
        selectPositionB(toPosition);

        for (int i = fromPosition + 1; i < toPosition; i++) {
            setPositionSelected(i, CalendarEntity.SELECTED_TYPE_RANGED);
        }
    }

    /**
     * 设置位置的选中状态.
     */
    private void setPositionSelected(int position, int selected) {
        CalendarEntity calendarEntity = mCalendarData.get(position);
        if (calendarEntity.itemType == CalendarEntity.ITEM_TYPE_DAY) {
            calendarEntity.selectedType = selected;
        }
    }

    /**
     * 返回指定日期的位置, 如果没找到则返回 -1.
     */
    private int getPosition(int[] date) {
        for (int position = 0; position < mCalendarData.size(); position++) {
            CalendarEntity calendarEntity = mCalendarData.get(position);
            if (calendarEntity.itemType == CalendarEntity.ITEM_TYPE_DAY
                    && Util.isDateEqual(calendarEntity.date, date)) {
                return position;
            }
        }

        return -1;
    }

    //*****************************************************************************************************************
    // 回调.

    /**
     * 单选回调.
     */
    private void onSingleSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
        Toast.makeText(getContext(), calendarEntity.dateString, Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选回调.
     */
    private void onDoubleSelected(int positionFrom, int positionTo, int dayCount) {
        CalendarEntity calendarEntityFrom = mCalendarAdapter.getCalendarEntity(positionFrom);
        CalendarEntity calendarEntityTo = mCalendarAdapter.getCalendarEntity(positionTo);
        Toast.makeText(getContext(), calendarEntityFrom.dateString + "~" + calendarEntityTo.dateString + "," + dayCount,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选选中第一个日期回调.
     */
    private void onDoubleFirstSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
        Toast.makeText(getContext(), "已选中:" + calendarEntity.dateString, Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选取消第一个日期回调.
     */
    private void onDoubleFirstUnselected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
        Toast.makeText(getContext(), "已取消:" + calendarEntity.dateString, Toast.LENGTH_SHORT).show();
    }

    /**
     * 超过最大双选天数回调.
     */
    private void onExceedMaxDoubleSelectedCount(int dayCount) {
        Toast.makeText(getContext(), "" + dayCount, Toast.LENGTH_SHORT).show();
    }

    //*****************************************************************************************************************
    // 滚动.

    /**
     * 滚动到的位置, 如果为 -1 则不滚动.
     */
    private int mScrollToPosition = -1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        scrollToPosition(mScrollToPosition);
    }

    /**
     * 滚动到今天.
     */
    public void scrollToToday() {
        scrollToPosition(getPosition(mTodayDate));
    }

    /**
     * 滚动到选中的位置, 如果没有选中的位置则滚动到今天.
     */
    public void scrollToSelected() {
        if (mDoubleSelectedMode && mSelectedPositionA != -1) {
            if (mSelectedPositionB == -1) {
                scrollToPosition(mSelectedPositionA);
            } else {
                scrollToPosition(Math.min(mSelectedPositionA, mSelectedPositionB));
            }
        } else if (!mDoubleSelectedMode && mSelectedPositionA != -1) {
            scrollToPosition(mSelectedPositionA);
        } else {
            scrollToToday();
        }
    }

    /**
     * 滚动到指定的位置, 如果为 -1 则不滚动.
     */
    private void scrollToPosition(int position) {
        mScrollToPosition = position;

        int calendarRecyclerViewMeasuredHeight = mCalendarRecyclerView.getMeasuredHeight();
        if (mScrollToPosition == -1 || calendarRecyclerViewMeasuredHeight == 0) {
            return;
        }

        int offset = calendarRecyclerViewMeasuredHeight / 2;
        mCalendarLayoutManager.scrollToPositionWithOffset(mScrollToPosition, offset);
        mScrollToPosition = -1;
    }

    //*****************************************************************************************************************
    // Adapter.

    /**
     * 日历 adapter.
     */
    private final class CalendarAdapter extends RecyclerView.Adapter
            implements PinnedHeaderRecyclerView.PinnedHeaderAdapter {
        private final LayoutInflater mLayoutInflater;

        CalendarAdapter() {
            mLayoutInflater = LayoutInflater.from(getContext());
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
                    dayViewHolder.itemView.setBackgroundColor(getResources().getColor(
                            calendarEntity.getBackgroundColor()));
                    dayViewHolder.itemView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (calendarEntity.isEnabled) {
                                clickPosition(layoutPosition, true, true);
                            }
                        }
                    });

                    dayViewHolder.dayTextView.setText(calendarEntity.dayString);
                    dayViewHolder.dayTextView.setTextColor(getResources().getColor(calendarEntity.getTextColor()));

                    dayViewHolder.specialTextView.setText(calendarEntity.specialString);
                    dayViewHolder.specialTextView.setTextColor(getResources().getColor(calendarEntity.getTextColor()));

                    break;
                }
            }
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
