package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
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

        inflate(getContext(), R.layout.recycler_calendar_view, this);

        mCalendarRecyclerView = (PinnedHeaderRecyclerView) findViewById(R.id.calendar);

        mCalendarLayoutManager = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(mCalendarLayoutManager);

        mCalendarAdapter = new CalendarAdapter(getContext());
        mCalendarAdapter.setOnDayClickListener(new CalendarAdapter.OnDayClickListener() {
            @Override
            void onDayClick(int position) {
                super.onDayClick(position);

                clickPosition(position, true, true);
            }
        });
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);

        mCalendarRecyclerView.setPinnedHeaderView(R.layout.recycler_calendar_item_month);

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

        if (Util.isDateValid(date)) {
            clickPosition(getPosition(date), true, false);
        }
    }

    /**
     * 设置双选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] dateFrom, int[] dateTo) {
        setDoubleSelectedMode(true, false);

        if (Util.isDateValid(dateFrom) && Util.isDateValid(dateTo)) {
            clickPosition(getPosition(dateFrom), false, false);
            clickPosition(getPosition(dateTo), true, false);
        }
    }

    private void setDoubleSelectedMode(boolean doubleSelectedMode, boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode != doubleSelectedMode) {
            mDoubleSelectedMode = doubleSelectedMode;

            mCalendarAdapter.setCalendarData(null);
        }

        if (mCalendarAdapter.getCalendarData().isEmpty()) {
            mCalendarAdapter.setCalendarData(CalendarEntity.newCalendarData(mDoubleSelectedMode));
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
            selectPositionA(getPosition(Util.getTodayDate()));
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();

            // React Native 渲染问题.
            mCalendarRecyclerView.scrollBy(0, 1);
            mCalendarRecyclerView.scrollBy(0, -1);
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
                    if (selectedCount <= Util.getInstance().max_double_selected_count) {
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

            // React Native 渲染问题.
            mCalendarRecyclerView.scrollBy(0, 1);
            mCalendarRecyclerView.scrollBy(0, -1);
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
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarData().get(position);
        if (calendarEntity.itemType == CalendarEntity.ITEM_TYPE_DAY) {
            calendarEntity.selectedType = selected;
        }
    }

    /**
     * 返回指定日期的位置, 如果没找到则返回 -1.
     */
    private int getPosition(int[] date) {
        for (int position = 0; position < mCalendarAdapter.getCalendarData().size(); position++) {
            CalendarEntity calendarEntity = mCalendarAdapter.getCalendarData().get(position);
            if (calendarEntity.itemType == CalendarEntity.ITEM_TYPE_DAY
                    && Util.isDateEqual(calendarEntity.date, date)) {
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
        scrollToPosition(getPosition(Util.getTodayDate()));
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
    // 回调.

    public Listener listener;

    /**
     * 单选回调.
     */
    private void onSingleSelected(int position) {
//        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
//        Toast.makeText(getContext(), Util.getDateString(calendarEntity.date), Toast.LENGTH_SHORT).show();
        if (listener != null) {
            listener.onSingleSelected(mCalendarAdapter.getCalendarEntity(position).date);
        }
    }

    /**
     * 双选回调.
     */
    private void onDoubleSelected(int positionFrom, int positionTo, int dayCount) {
//        CalendarEntity calendarEntityFrom = mCalendarAdapter.getCalendarEntity(positionFrom);
//        CalendarEntity calendarEntityTo = mCalendarAdapter.getCalendarEntity(positionTo);
//        Toast.makeText(getContext(), Util.getDateString(calendarEntityFrom.date) + "~" +
//                Util.getDateString(calendarEntityTo.date) + "," + dayCount, Toast.LENGTH_SHORT).show();

        if (listener != null) {
            listener.onDoubleSelected(mCalendarAdapter.getCalendarEntity(positionFrom).date,
                    mCalendarAdapter.getCalendarEntity(positionTo).date, dayCount);
        }
    }

    /**
     * 双选选中第一个日期回调.
     */
    private void onDoubleFirstSelected(int position) {
//        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
//        Toast.makeText(getContext(), "已选中:" + Util.getDateString(calendarEntity.date), Toast.LENGTH_SHORT).show();

        if (listener != null) {
            listener.onDoubleFirstSelected(mCalendarAdapter.getCalendarEntity(position).date);
        }
    }

    /**
     * 双选取消第一个日期回调.
     */
    private void onDoubleFirstUnselected(int position) {
//        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
//        Toast.makeText(getContext(), "已取消:" + Util.getDateString(calendarEntity.date), Toast.LENGTH_SHORT).show();

        if (listener != null) {
            listener.onDoubleFirstUnselected(mCalendarAdapter.getCalendarEntity(position).date);
        }
    }

    /**
     * 超过最大双选天数回调.
     */
    private void onExceedMaxDoubleSelectedCount(int dayCount) {
//        Toast.makeText(getContext(), "" + dayCount, Toast.LENGTH_SHORT).show();

        if (listener != null) {
            listener.onExceedMaxDoubleSelectedCount(dayCount);
        }
    }

    public static abstract class Listener {
        public void onSingleSelected(int[] date) {
        }

        /**
         * 双选回调.
         */
        public void onDoubleSelected(int[] dateFrom, int[] dateTo, int dayCount) {
        }

        /**
         * 双选选中第一个日期回调.
         */
        public void onDoubleFirstSelected(int[] date) {
        }

        /**
         * 双选取消第一个日期回调.
         */
        public void onDoubleFirstUnselected(int[] date) {
        }

        /**
         * 超过最大双选天数回调.
         */
        public void onExceedMaxDoubleSelectedCount(int dayCount) {
        }
    }
}
