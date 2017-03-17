package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    /**
     * 今天日期.
     */
    private int[] mTodayDate;

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

        Util.init(getContext());

        mTodayDate = Util.getTodayDate();

        inflate(getContext(), R.layout.view_recycler_calendar, this);

        mCalendarRecyclerView = (PinnedHeaderRecyclerView) findViewById(R.id.calendar);

        mCalendarLayoutManager = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(mCalendarLayoutManager);

        mCalendarAdapter = new CalendarAdapter();
        mCalendarAdapter.setOnDayClickListener(new CalendarAdapter.OnDayClickListener() {
            @Override
            void onDayClick(int position) {
                super.onDayClick(position);

                clickPosition(position, true, true);
            }
        });
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);

        mCalendarRecyclerView.setPinnedHeaderView(R.layout.item_month);

        int[] todayDate = Util.getTodayDate();
        setDoubleSelectedMode(false, todayDate[0], todayDate[1], todayDate[0], todayDate[1]);
        scrollToSelected();
    }

    //*****************************************************************************************************************
    // 选中模式.

    /**
     * 当前选中的第一个位置.
     */
    private int mSelectedPositionA = -1;

    /**
     * 设置是否为双选模式, 并重置选中日期.
     */
    public void setDoubleSelectedMode(int yearFrom, int monthFrom, int yearTo, int monthTo) {
        setDoubleSelectedMode(true, yearFrom, monthFrom, yearTo, monthTo);
    }

    /**
     * 设置单选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] date, int yearFrom, int monthFrom, int yearTo, int monthTo) {
        setDoubleSelectedMode(false, yearFrom, monthFrom, yearTo, monthTo);

        setDate(date);
    }

    private void setDoubleSelectedMode(boolean notifyDataSetChanged, int yearFrom, int monthFrom, int yearTo,
            int monthTo) {
        mCalendarAdapter.setNewData(Util.newCalendarData(mTodayDate, yearFrom, monthFrom, yearTo, monthTo));

        resetSelected(notifyDataSetChanged);
    }

    /**
     * 重置选中日期.
     */
    public void resetSelected() {
        resetSelected(true);
    }

    private void resetSelected(boolean notifyDataSetChanged) {
        selectPositionA(getPosition(mTodayDate));

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    public void setDate(int[] date) {
        clickPosition(getPosition(date), true, false);
    }

    /**
     * 点击某位置.
     */
    private void clickPosition(int position, boolean notifyDataSetChanged, boolean callback) {
        selectPositionA(position);
        if (callback) {
            onSingleSelected(mSelectedPositionA);
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
            setPositionSelected(mSelectedPositionA, false);
            mSelectedPositionA = -1;
        }

        if (position == -1) {
            return;
        }

        setPositionSelected(position, true);
        mSelectedPositionA = position;
    }

    /**
     * 设置位置的选中状态.
     */
    private void setPositionSelected(int position, boolean selected) {
        CalendarEntity calendarEntity = mCalendarAdapter.getItem(position);
        if (calendarEntity.getItemType() == CalendarEntity.ITEM_TYPE_DAY) {
            ((CalendarDayEntity) calendarEntity).selected = selected;
        }
    }

    /**
     * 返回指定日期的位置, 如果没找到则返回 -1.
     */
    private int getPosition(int[] date) {
        for (int position = 0; position < mCalendarAdapter.getItemCount(); position++) {
            CalendarEntity calendarEntity = mCalendarAdapter.getItem(position);
            if (calendarEntity.getItemType() == CalendarEntity.ITEM_TYPE_DAY
                    && Util.isDateEqual(((CalendarDayEntity) calendarEntity).date, date)) {
                return position;
            }
        }

        return -1;
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
        if (mSelectedPositionA != -1) {
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
    // 回调.

    /**
     * 单选回调.
     */
    private void onSingleSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getItem(position);
        Toast.makeText(getContext(), Util.getDateString(((CalendarDayEntity) calendarEntity).date), Toast.LENGTH_SHORT)
                .show();
    }
}
