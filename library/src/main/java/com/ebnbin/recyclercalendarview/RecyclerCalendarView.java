package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    private RecyclerView mCalendarRecyclerView;

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

        inflate(getContext(), R.layout.view_recycler_calendar, this);

        mCalendarRecyclerView = (RecyclerView) findViewById(R.id.calendar);

        mCalendarLayoutManager = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(mCalendarLayoutManager);

        mCalendarAdapter = new CalendarAdapter();
        mCalendarAdapter.listener = new CalendarAdapter.Listener() {
            @Override
            void onDayClick(int position) {
                super.onDayClick(position);

                selectPosition(position, true);
            }
        };
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);
    }

    //*****************************************************************************************************************
    // 选中.

    /**
     * 当前选中的位置.
     */
    private int mSelectedPosition = -1;

    /**
     * 设置年月范围并更新数据.
     */
    public void setRange(int yearFrom, int monthFrom, int yearTo, int monthTo) {
        if (!Util.isRangeValid(yearFrom, monthFrom, yearTo, monthTo)) {
            return;
        }

        List<Entity> data = Util.newCalendarData(yearFrom, monthFrom, yearTo, monthTo);
        mCalendarAdapter.setNewData(data);

        mSelectedPosition = -1;
    }

    /**
     *  选中某日期.
     */
    public void selectDate(int[] date) {
        if (!Util.isDateValid(date)) {
            return;
        }

        selectPosition(getPosition(date), false);
    }

    /**
     * 选中某位置.
     */
    private void selectPosition(int position, boolean callback) {
        if (mSelectedPosition == position) {
            return;
        }

        if (mSelectedPosition != -1) {
            setPositionSelected(mSelectedPosition, false);
            mSelectedPosition = -1;
        }

        if (position == -1) {
            return;
        }

        setPositionSelected(position, true);
        mSelectedPosition = position;

        if (callback) {
            onSelected(position);
        }
    }

    /**
     * 设置位置的选中状态.
     */
    private void setPositionSelected(int position, boolean selected) {
        Entity entity = mCalendarAdapter.getItem(position);

        if (!(entity instanceof DayEntity)) {
            return;
        }

        DayEntity dayEntity = (DayEntity) entity;

        if (dayEntity.selected == selected) {
            return;
        }

        dayEntity.selected = selected;
        mCalendarAdapter.notifyItemChanged(position);
    }

    /**
     * 返回指定日期的位置, 如果没找到则返回 -1.
     */
    private int getPosition(int[] date) {
        for (int position = 0; position < mCalendarAdapter.getItemCount(); position++) {
            Entity entity = mCalendarAdapter.getItem(position);
            if (entity instanceof DayEntity && Util.isDateEqual(((DayEntity) entity).date, date)) {
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
     * 滚动到选中的位置, 如果没有选中的位置则滚动到今天, 如果今天不存在则不滚动.
     */
    public void scrollToSelected() {
        if (mSelectedPosition == -1) {
            int todayPosition = getPosition(Util.getTodayDate());
            if (todayPosition != -1) {
                scrollToPosition(todayPosition);
            }
        } else {
            scrollToPosition(mSelectedPosition);
        }
    }

    /**
     * 滚动到指定的位置, 如果为 -1 则不滚动.
     */
    private void scrollToPosition(int position) {
        mScrollToPosition = position;

        if (mScrollToPosition == -1) {
            return;
        }

        int calendarRecyclerViewMeasuredHeight = mCalendarRecyclerView.getMeasuredHeight();

        if (calendarRecyclerViewMeasuredHeight == 0) {
            return;
        }

        int offset = calendarRecyclerViewMeasuredHeight / 2;
        mCalendarLayoutManager.scrollToPositionWithOffset(mScrollToPosition, offset);
        mScrollToPosition = -1;
    }

    //*****************************************************************************************************************
    // 监听.

    /**
     * 监听器.
     */
    public final List<Listener> listeners = new ArrayList<>();

    /**
     * 回调.
     */
    private void onSelected(int position) {
        Entity entity = mCalendarAdapter.getItem(position);

        if (!(entity instanceof DayEntity)) {
            return;
        }

        DayEntity dayEntity = (DayEntity) entity;
        int[] date = dayEntity.date;

        for (Listener listener : listeners) {
            listener.onSelected(date);
        }
    }

    /**
     * 回调监听器.
     */
    public static abstract class Listener {
        public void onSelected(int[] date) {
        }
    }
}
