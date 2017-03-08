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
    private PinnedHeaderRecyclerView mCalendarRecyclerView;

    private GridLayoutManager mCalendarLayoutManager;

    private List<CalendarEntity> mCalendarData;

    private CalendarAdapter mCalendarAdapter;

    private int[] mTodayDate;

    /**
     * 如果为 false 则为单选, 否则为双选.
     */
    private boolean mDoubleSelectedMode = false;

    /**
     * 最大双选数量.
     */
    private int mMaxDoubleSelectedCount = 0;

    /**
     * 当前选中的第一个 position.
     */
    private int mSelectedPositionA = -1;
    /**
     * 当前选中的第二个 position.
     */
    private int mSelectedPositionB = -1;

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

        mTodayDate = Util.getTodayDate();

        setMaxDoubleSelectedCount(Util.MAX_DOUBLE_SELECTED_COUNT);
        setDoubleSelectedMode(false);
        scrollToSelected();
    }

    //*****************************************************************************************************************
    // Getter, setter.

    /**
     * 返回最大双选数量.
     */
    public int getMaxDoubleSelectedCount() {
        return mMaxDoubleSelectedCount;
    }

    /**
     * 返回是否双选.
     */
    public boolean isDoubleSelectedMode() {
        return mDoubleSelectedMode;
    }

    /**
     * 设置最大双选数量, 并清除全部选中日期.
     */
    public void setMaxDoubleSelectedCount(int maxDoubleSelectedCount) {
        if (mMaxDoubleSelectedCount == maxDoubleSelectedCount) {
            return;
        }

        mMaxDoubleSelectedCount = maxDoubleSelectedCount;

        resetSelected(true);
    }

    //*****************************************************************************************************************
    // Double selected mode.

    /**
     * 设置是否双选, 并恢复默认选中日期.
     */
    public void setDoubleSelectedMode(boolean doubleSelectedMode) {
        setDoubleSelectedMode(doubleSelectedMode, true);
    }

    public void setDoubleSelectedMode(int[] date) {
        setDoubleSelectedMode(false, false);

        int position = Util.getPosition(mCalendarData, date);
        onPositionClick(position, true, false);
    }

    public void setDoubleSelectedMode(int[] dateFrom, int[] dateTo) {
        setDoubleSelectedMode(true, false);

        int fromPosition = Util.getPosition(mCalendarData, dateFrom);
        onPositionClick(fromPosition, false, false);
        int toPosition = Util.getPosition(mCalendarData, dateTo);
        onPositionClick(toPosition, true, false);
    }

    private void setDoubleSelectedMode(boolean doubleSelectedMode, boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode != doubleSelectedMode || mCalendarData == null) {
            mDoubleSelectedMode = doubleSelectedMode;

            mCalendarData = CalendarEntity.newCalendarData(getContext(), mDoubleSelectedMode, mTodayDate);
            mCalendarAdapter.setNewData(mCalendarData);
        }

        resetSelected(notifyDataSetChanged);
    }

    public void resetSelected() {
        resetSelected(true);
    }

    private void resetSelected(boolean notifyDataSetChanged) {
        if (mCalendarData == null) {
            mSelectedPositionA = -1;
            mSelectedPositionB = -1;

            return;
        }

        if (mDoubleSelectedMode) {
            unselectPositionAB();
        } else {
            selectPositionB(-1);
            int position = Util.getPosition(mCalendarData, mTodayDate);
            selectPositionA(position);
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 点击某日期.
     */
    private void onPositionClick(int position, boolean notifyDataSetChanged, boolean callback) {
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

    private int getPositionABSelectedCount(int positionA, int positionB) {
        if (positionA == -1 || positionB == -1) {
            return 0;
        }

        int fromPosition = Math.min(positionA, positionB);
        int toPosition = Math.max(positionA, positionB);

        int selectedCount = 0;
        for (int i = fromPosition; i <= toPosition; i++) {
            if (mCalendarAdapter.getItem(i).getItemType() == CalendarEntity.ITEM_TYPE_DAY) {
                ++selectedCount;
            }
        }

        return selectedCount;
    }

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
     * 设置某位置的选中状态.
     */
    private void setPositionSelected(int position, int selected) {
        CalendarEntity calendarEntity = mCalendarData.get(position);
        if (calendarEntity.getItemType() == CalendarEntity.ITEM_TYPE_DAY) {
            calendarEntity.selectedType = selected;
        }
    }

    //*****************************************************************************************************************
    // Callback.

    /**
     * 单选回调.
     */
    private void onSingleSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getItem(position);
        Toast.makeText(getContext(), calendarEntity.dateString, Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选回调.
     */
    private void onDoubleSelected(int positionFrom, int positionTo, int count) {
        CalendarEntity calendarEntityFrom = mCalendarAdapter.getItem(positionFrom);
        CalendarEntity calendarEntityTo = mCalendarAdapter.getItem(positionTo);
        Toast.makeText(getContext(), calendarEntityFrom.dateString + "~" + calendarEntityTo.dateString + "," + count,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选选中第一个日期回调.
     */
    private void onDoubleFirstSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getItem(position);
        Toast.makeText(getContext(), "已选中:" + calendarEntity.dateString, Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选取消第一个日期回调.
     */
    private void onDoubleFirstUnselected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getItem(position);
        Toast.makeText(getContext(), "已取消:" + calendarEntity.dateString, Toast.LENGTH_SHORT).show();
    }

    /**
     * 天数超过限制回调.
     *
     * @param count
     *         选中天数.
     */
    private void onExceedMaxDoubleSelectedCount(int count) {
        Toast.makeText(getContext(), "" + count, Toast.LENGTH_SHORT).show();
    }

    //*****************************************************************************************************************
    // 滚动.

    /**
     * 滚动到的 position, 如果为 -1 则不滚动.
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
        int position = Util.getPosition(mCalendarData, mTodayDate);
        scrollToPosition(position);
    }

    /**
     * 滚动到选中的 position, 如果没有选中的 position 则滚动到今天.
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
     * 滚动到指定的 position, 如果为 -1 则不滚动.
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
    private final class CalendarAdapter extends BaseMultiItemQuickAdapter<CalendarEntity, BaseViewHolder>
            implements PinnedHeaderRecyclerView.PinnedHeaderAdapter {
        public CalendarAdapter() {
            super(null);

            addItemType(CalendarEntity.ITEM_TYPE_MONTH, R.layout.item_year_month);
            addItemType(CalendarEntity.ITEM_TYPE_DAY, R.layout.item_date);
            addItemType(CalendarEntity.ITEM_TYPE_EMPTY_DAY, R.layout.item_empty_day);
            addItemType(CalendarEntity.ITEM_TYPE_DIVIDER, R.layout.item_divider);
        }

        @Override
        protected void convert(final BaseViewHolder helper, final CalendarEntity item) {
            switch (helper.getItemViewType()) {
                case CalendarEntity.ITEM_TYPE_MONTH: {
                    helper.setText(R.id.year_month, item.monthString);

                    break;
                }
                case CalendarEntity.ITEM_TYPE_DAY: {
                    helper.setText(R.id.day, item.dayString);
                    helper.setText(R.id.special, item.specialString);

                    helper.setTextColor(R.id.day, getResources().getColor(item.getTextColor()));
                    helper.setTextColor(R.id.special, getResources().getColor(item.getTextColor()));

                    helper.setBackgroundColor(R.id.root, getResources().getColor(item.getBackgroundColor()));

                    View rootView = helper.getView(R.id.root);
                    rootView.setEnabled(item.isEnabled);
                    rootView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.isEnabled) {
                                onPositionClick(helper.getLayoutPosition(), true, true);
                            }
                        }
                    });

                    break;
                }
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);

            Util.fullSpan(mCalendarLayoutManager, this, CalendarEntity.ITEM_TYPE_MONTH);
            Util.fullSpan(mCalendarLayoutManager, this, CalendarEntity.ITEM_TYPE_DIVIDER);
        }

        @Override
        public int getPinnedHeaderState(int position) {
            return getItem(position).isLastSundayOfMonth
                    ? PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_PUSHABLE
                    : PinnedHeaderRecyclerView.PinnedHeaderAdapter.STATE_VISIBLE;
        }

        @Override
        public void configurePinnedHeader(View pinnedHeaderView, int position) {
            TextView yearMonthTextView = (TextView) pinnedHeaderView.findViewById(R.id.year_month);
            yearMonthTextView.setText(getItem(position).monthString);
        }
    }
}
