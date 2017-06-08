package com.example.z3603.videoee;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by z3603 on 2017/6/7.
 */

public class RecyclerViewHolderView extends RelativeLayout {
    private static final String TAG = RecyclerViewHolderView.class.getName();
    private RecyclerView mContentView;
    private LinearLayoutManager mLayoutManager;

    public RecyclerViewHolderView(Context context) {
        super(context);
    }

    public RecyclerViewHolderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewHolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mContentView = (RecyclerView) getChildAt(0);
        }
    }

    private float mTouchX;
    private float mLeftOffset;
    private float mRightOffset;

    public void update(float left, float right) {
        this.mLeftOffset = left;
        this.mRightOffset = right;
    }

    public LinearLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = ev.getX();// 获取点击x坐标
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = ev.getX();
                float offsetX = currentX - mTouchX;
                Log.d(TAG, "mContentView.getChildAt(0).getLeft() = " + mContentView.getChildAt(0).getLeft() );
                Log.d(TAG, "mContentView.getLeft() = " + mContentView.getLeft());

                if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    if (offsetX > 0) {
                        if (mContentView.getLeft() <= mLeftOffset) {
                            // 移动布局
                            mContentView.layout((int) offsetX, mContentView.getTop(),
                                    mContentView.getRight(), mContentView.getBottom());
                            postInvalidateOnAnimation();
                        }
                    } else {
                        if (mContentView.getLeft() > 0) {
                            // 移动布局
                            Log.d(TAG, "offsetX = " + offsetX);
                            mContentView.layout((int) (mContentView.getLeft() + offsetX), mContentView.getTop(),
                                    mContentView.getRight(), mContentView.getBottom());
                            postInvalidateOnAnimation();
                        }
                    }
                }
//                if (offsetX > 0) {
//                    Log.d(TAG, "contentView.getLeft() = " + mContentView.getLeft());
//                    if (mContentView.getLeft() <= mLeftOffset) {
//                        // 移动布局
//                        mContentView.layout((int) offsetX, mContentView.getTop(),
//                                mContentView.getRight(), mContentView.getBottom());
//                        postInvalidateOnAnimation();
//                    }
//                } else {
//                    Log.d(TAG, "contentView.getRight() = " + mContentView.getRight());
//                    Log.d(TAG, "contentView.getLeft() = " + mContentView.getLeft());
//                    if (mContentView.getRight() >= mRightOffset) {
//                        // 移动布局
//                        mContentView.layout(mContentView.getLeft(), mContentView.getTop(),
//                                mContentView.getRight(), (int) (mContentView.getBottom() + offsetX));
//                        postInvalidateOnAnimation();
//                    }
//                }
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
