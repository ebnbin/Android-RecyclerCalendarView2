package com.ebnbin.recyclercalendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 固定 header 的 recyclerView.
 */
class PinnedHeaderRecyclerView extends RecyclerView {
    private View mPinnedHeaderView;

    private int mPinnedHeaderViewWidth;
    private int mPinnedHeaderViewHeight;

    private boolean mPinnedHeaderViewVisible;

    private PinnedHeaderAdapter mPinnedHeaderAdapter;

    public PinnedHeaderRecyclerView(Context context) {
        super(context);

        init();
    }

    public PinnedHeaderRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PinnedHeaderRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        setFadingEdgeLength(0);
    }

    /**
     * 设置 pinnedHeaderView.
     *
     * @param layoutResId
     *         如果为 0 则设置 pinnedHeaderView 为 null.
     */
    public void setPinnedHeaderView(int layoutResId) {
        if (layoutResId == 0) {
            mPinnedHeaderView = null;
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            mPinnedHeaderView = layoutInflater.inflate(layoutResId, this, false);
        }

        requestLayout();
    }

    /**
     * Adapter 必须实现接口 PinnedHeaderAdapter.
     */
    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        mPinnedHeaderAdapter = (PinnedHeaderAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mPinnedHeaderView != null) {
            measureChild(mPinnedHeaderView, widthMeasureSpec, heightMeasureSpec);

            mPinnedHeaderViewWidth = mPinnedHeaderView.getMeasuredWidth();
            mPinnedHeaderViewHeight = mPinnedHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mPinnedHeaderView != null) {
            mPinnedHeaderView.layout(0, 0, mPinnedHeaderViewWidth, mPinnedHeaderViewHeight);

            invalidatePinnedHeaderView();
        }
    }

    private void invalidatePinnedHeaderView() {
        if (mPinnedHeaderView == null || mPinnedHeaderAdapter == null) {
            return;
        }

        int position = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        int state = mPinnedHeaderAdapter.getPinnedHeaderState(position);

        switch (state) {
            case PinnedHeaderAdapter.STATE_GONE: {
                mPinnedHeaderViewVisible = false;

                break;
            }
            case PinnedHeaderAdapter.STATE_VISIBLE: {
                mPinnedHeaderViewVisible = true;

                if (mPinnedHeaderView.getTop() != 0) {
                    mPinnedHeaderView.layout(0, 0, mPinnedHeaderViewWidth, mPinnedHeaderViewHeight);
                }

                mPinnedHeaderAdapter.configurePinnedHeader(mPinnedHeaderView, position);

                break;
            }
            case PinnedHeaderAdapter.STATE_PUSHABLE: {
                mPinnedHeaderViewVisible = true;

                int firstViewBottom = getChildAt(0).getBottom();
                int y = mPinnedHeaderViewHeight < firstViewBottom ? 0 : firstViewBottom - mPinnedHeaderViewHeight;
                if (mPinnedHeaderView.getTop() != y) {
                    mPinnedHeaderView.layout(0, y, mPinnedHeaderViewWidth, mPinnedHeaderViewHeight + y);
                }

                mPinnedHeaderAdapter.configurePinnedHeader(mPinnedHeaderView, position);

                break;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mPinnedHeaderViewVisible && mPinnedHeaderView != null) {
            drawChild(canvas, mPinnedHeaderView, getDrawingTime());
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        invalidatePinnedHeaderView();
    }

    /**
     * PinnedHeaderRecyclerView 的 adapter 必须实现这个接口.
     */
    interface PinnedHeaderAdapter {
        /**
         * 始终隐藏.
         */
        int STATE_GONE = 0;
        /**
         * 始终显示.
         */
        int STATE_VISIBLE = 1;
        /**
         * 可推动.
         */
        int STATE_PUSHABLE = 2;

        /**
         * 根据返回值确定对应位置的 pinnedHeaderView 的状态.
         *
         * @return STATE_GONE, STATE_VISIBLE 或 STATE_PUSHABLE.
         */
        int getPinnedHeaderState(int position);

        /**
         * 配置对应位置的 pinnedHeaderView.
         */
        void configurePinnedHeader(View pinnedHeaderView, int position);
    }
}
