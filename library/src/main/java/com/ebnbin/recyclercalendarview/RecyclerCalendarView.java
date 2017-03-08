package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private PinnedHeaderRecyclerView mCalendarRecyclerView;

    private GridLayoutManager mCalendarLayoutManager;

    private CalendarAdapter mCalendarAdapter;

    private List<CalendarEntity> mCalendarData;

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

        int position = Util.getPosition(mCalendarData, date);
        clickPosition(position, true, false);
    }

    /**
     * 设置双选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] dateFrom, int[] dateTo) {
        setDoubleSelectedMode(true, false);

        int fromPosition = Util.getPosition(mCalendarData, dateFrom);
        clickPosition(fromPosition, false, false);
        int toPosition = Util.getPosition(mCalendarData, dateTo);
        clickPosition(toPosition, true, false);
    }

    private void setDoubleSelectedMode(boolean doubleSelectedMode, boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode != doubleSelectedMode || mCalendarData == null) {
            mDoubleSelectedMode = doubleSelectedMode;

            mCalendarData = CalendarEntity.newCalendarData(getContext(), mDoubleSelectedMode, mTodayDate,
                    mSpecialCount, mYearFrom, mMonthFrom);
            mCalendarAdapter.setNewData(mCalendarData);
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
            if (mCalendarAdapter.getItem(i).getItemType() == CalendarEntity.ITEM_TYPE_DAY) {
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
        if (calendarEntity.getItemType() == CalendarEntity.ITEM_TYPE_DAY) {
            calendarEntity.selectedType = selected;
        }
    }

    //*****************************************************************************************************************
    // 回调.

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
    private void onDoubleSelected(int positionFrom, int positionTo, int dayCount) {
        CalendarEntity calendarEntityFrom = mCalendarAdapter.getItem(positionFrom);
        CalendarEntity calendarEntityTo = mCalendarAdapter.getItem(positionTo);
        Toast.makeText(getContext(), calendarEntityFrom.dateString + "~" + calendarEntityTo.dateString + "," + dayCount,
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
        int position = Util.getPosition(mCalendarData, mTodayDate);
        scrollToPosition(position);
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
    private final class CalendarAdapter extends BaseMultiItemQuickAdapter<CalendarEntity, BaseViewHolder>
            implements PinnedHeaderRecyclerView.PinnedHeaderAdapter {
        CalendarAdapter() {
            super(null);

            addItemType(CalendarEntity.ITEM_TYPE_MONTH, R.layout.item_month);
            addItemType(CalendarEntity.ITEM_TYPE_DAY, R.layout.item_day);
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
                                clickPosition(helper.getLayoutPosition(), true, true);
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
