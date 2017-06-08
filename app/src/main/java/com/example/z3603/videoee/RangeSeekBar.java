package com.example.z3603.videoee;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by z3603 on 2017/6/3.
 */

public class RangeSeekBar extends View {

    /**
     * 控件touchDown时，实在左border还是在右boeder
     */
    private static final int LEFT_BORDER = -1;
    private static final int RIGHT_BORDER = -2;
    private static final int NONE_BORDER = -3;

    private float mDefaultCropLength;
    private int mBorderWidth;
    private int mMainHeight;
    private int mCropRectBorder;
    private float mTouchedX;
    private int mWidth = 0;
    private int mHeight = 0;

    private Bitmap mBitmapBorderLeft;
    private Bitmap mBitmapBorderRight;
    //    private Bitmap mBitmapMaskLeft;
//    private Bitmap mBitmapMaskRight;
    private NinePatchDrawable mDrawableMaskLeft;
    private NinePatchDrawable mDrawableMaskRight;
    private NinePatchDrawable mDrawableMaskCenter;

    private Paint mMainPanit;
    private Paint mCropRectPaint;

    private float mLeftBorderPosition;
    private float mRightBorderPosition;
    private float mCropRectLength;

    private OnCropRectBorderChangedListener mCropRectBorderChangedListener;


    public RangeSeekBar(Context context) {
        super(context);
        init();
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //裁剪框左右边框图片
        mBitmapBorderLeft = BitmapFactory.decodeResource(getResources(), R.mipmap.crop_rect_border);
        //裁剪框图片宽度
        int borderWidth = mBitmapBorderLeft.getWidth();
        //裁剪框图片高度
        int borderHeight = mBitmapBorderLeft.getHeight();
        int newWidth = ImageUtils.dip2px(getContext(), 11);
        int newHeight = ImageUtils.dip2px(getContext(), 55);
        //边框宽度复制到全局
        mBorderWidth = newWidth;
        mMainHeight = newHeight;
        mCropRectBorder = ImageUtils.dip2px(getContext(), 4);
        //横向缩放比
        float scaleWidth = newWidth * 1.0f / borderWidth;
        //纵向缩放比
        float scaleHeight = newHeight * 1.0f / borderHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //创建图片缩放后的图片
        mBitmapBorderLeft = Bitmap.createBitmap(mBitmapBorderLeft, 0, 0, borderWidth, borderHeight, matrix, true);
        //裁剪边框右边同左边一致
        mBitmapBorderRight = mBitmapBorderLeft;
        mMainPanit = new Paint(Paint.ANTI_ALIAS_FLAG);

        mDrawableMaskLeft = (NinePatchDrawable) getContext().getResources().getDrawable(R.mipmap.overlay_black);
        mDrawableMaskRight = mDrawableMaskLeft;
        mDrawableMaskCenter = (NinePatchDrawable) getContext().getResources().getDrawable(R.mipmap.overlay_crop);

        mCropRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCropRectPaint.setStyle(Paint.Style.FILL);
        //白色画笔
        mCropRectPaint.setColor(Color.parseColor("#ffffff"));
    }

    public void loadParams(float minCropRectLength, int defaultCropLength) {
        mCropRectLength = defaultCropLength;
        mDefaultCropLength = minCropRectLength;
        mLeftBorderPosition = 0;
        mRightBorderPosition = defaultCropLength;
    }

    public OnCropRectBorderChangedListener getCropRectBorderChangedListener() {
        return mCropRectBorderChangedListener;
    }

    public void setCropRectBorderChangedListener(OnCropRectBorderChangedListener cropRectBorderChangedListener) {
        this.mCropRectBorderChangedListener = cropRectBorderChangedListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 300;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = 120;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画左边border
        canvas.drawBitmap(mBitmapBorderLeft, mLeftBorderPosition, 0, mMainPanit);
        //画右边border
        canvas.drawBitmap(mBitmapBorderRight, mRightBorderPosition, 0, mMainPanit);

        //画左边遮罩
        int leftMaskStart = 0;
        int leftMaskEnd = (int) mLeftBorderPosition;
        if (leftMaskEnd - leftMaskStart > 0) {
            Rect rect = new Rect();
            rect.set(leftMaskStart, 0, leftMaskEnd, mMainHeight);
            mDrawableMaskLeft.setBounds(rect);
            mDrawableMaskLeft.draw(canvas);
        }
        //画右边遮罩
        int rightMaskStart = (int) (mRightBorderPosition + mBorderWidth);
        int rightMaskEnd = getWidth();
        if (getWidth() - rightMaskStart > 0) {
            Rect rect = new Rect();
            rect.set(rightMaskStart, 0, getWidth(), mMainHeight);
            mDrawableMaskRight.setBounds(rect);
            mDrawableMaskRight.draw(canvas);
        }

        //画中间遮罩
        int centerMaskStart = (int) (mLeftBorderPosition + mBorderWidth);
        int centerMaskEnd = (int) mRightBorderPosition;
        Rect rect = new Rect();
        rect.set(centerMaskStart, 0, centerMaskEnd, mMainHeight);
        mDrawableMaskCenter.setBounds(rect);
        mDrawableMaskCenter.draw(canvas);

        //画中间矩形
        float cropRectStart = mLeftBorderPosition + mBorderWidth;
        float cropRectEnd = mRightBorderPosition;
        float cropRectTop = 0;
        float cropRectBottom = mMainHeight;
        RectF mainCropRect = new RectF();
        mainCropRect.set(cropRectStart, cropRectTop, cropRectEnd, cropRectBottom);
        mCropRectPaint.setStyle(Paint.Style.STROKE);
        mCropRectPaint.setStrokeWidth(mCropRectBorder);
        canvas.drawRect(mainCropRect, mCropRectPaint);
    }

    boolean isPressedLeft = false;
    boolean isPressedRight = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex;// 记录点击点的index
        float touchedX = event.getX();
        int border = NONE_BORDER;
//        if (range == NONE_BORDER) {
//            Log.d("onTouchEvent","something wrong");
//            return super.onTouchEvent(event);
//        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("onTouchEvent", "ACTION_DOWN");
                isPressedLeft = false;
                isPressedRight = false;
                mTouchedX = event.getX();
                border = isBorderRange(touchedX);
                if (border == LEFT_BORDER) {
                    isPressedLeft = true;
                    Log.d("onTouchEvent", "LEFT_BORDER");
                } else if (border == RIGHT_BORDER) {
                    isPressedRight = true;
                    Log.d("onTouchEvent", "RIGHT_BORDER");
                } else {
                    Log.d("onTouchEvent", "all false");
                    isPressedLeft = false;
                    isPressedRight = false;
                }
                if (mCropRectBorderChangedListener != null) {
                    mCropRectBorderChangedListener.onCropRectBorderChanged(border, mLeftBorderPosition + mBorderWidth, mRightBorderPosition, MotionEvent.ACTION_DOWN);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("onTouchEvent", "ACTION_MOVE");
                Log.d("onTouchEvent", "isPressedLeft = " + isPressedLeft + "; isPressedRight = " + isPressedRight);
//                if (Math.abs(event.getX() - mTouchedX) <= 50) {
//                    return super.onTouchEvent(event);
//                }
                if (isPressedLeft) {
                    mLeftBorderPosition = event.getX();
                    if (mRightBorderPosition - mLeftBorderPosition - mBorderWidth < mDefaultCropLength) {
                        //到达极限值后重赋值为极限值
                        mLeftBorderPosition = mRightBorderPosition - mBorderWidth - mDefaultCropLength;
                    }
                    Log.d("onTouchEvent", "draw left");
                    invalidate();
                } else if (isPressedRight) {
                    mRightBorderPosition = event.getX();
                    if (mRightBorderPosition - mLeftBorderPosition - mBorderWidth < mDefaultCropLength) {
                        //到达极限值后重赋值为极限值
                        mRightBorderPosition = mLeftBorderPosition + mBorderWidth + mDefaultCropLength;
                    } else if (mRightBorderPosition + mBorderWidth > getMeasuredWidth()) {
                        //移动到最右边后，不能再移动
                        mRightBorderPosition = getMeasuredWidth() - mBorderWidth;
                    }
                    Log.d("onTouchEvent", "draw right");
                    invalidate();
                }
                mCropRectLength = mRightBorderPosition - mLeftBorderPosition - mBorderWidth;
                if (mCropRectBorderChangedListener != null) {
                    mCropRectBorderChangedListener.onCropRectBorderChanged(border, mLeftBorderPosition + mBorderWidth, mRightBorderPosition, MotionEvent.ACTION_MOVE);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCropRectBorderChangedListener != null) {
                    mCropRectBorderChangedListener.onCropRectBorderChanged(border, mLeftBorderPosition + mBorderWidth, mRightBorderPosition, MotionEvent.ACTION_UP);
                }
                break;
        }
        if (isPressedLeft || isPressedRight) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * 按下的点是否在图片范围内
     *
     * @param touchX
     * @return
     */
    private int isBorderRange(float touchX) {
        // 当前触摸点X坐标-最小值图片中心点在屏幕的X坐标之差<=最小点图片的宽度的一般
        // 即判断触摸点是否在以最小值图片中心为原点，宽度一半为半径的圆内。
        Log.d("isBorderRange", "touchX = " + touchX);
        Log.d("isBorderRange", "mLeftBorderPosition = " + mLeftBorderPosition);
        Log.d("isBorderRange", "mRightBorderPosition = " + mRightBorderPosition);
        Log.d("isBorderRange", "mBorderWidth = " + mBorderWidth);
        if (touchX >= mLeftBorderPosition && touchX <= mLeftBorderPosition + mBorderWidth) {
            return LEFT_BORDER;
        } else if (touchX >= mRightBorderPosition && touchX <= mRightBorderPosition + mBorderWidth) {
            return RIGHT_BORDER;
        } else {
            return NONE_BORDER;
        }
    }

    public interface OnCropRectBorderChangedListener {
        void onCropRectBorderChanged(int border, float leftPosition, float rightPosition, int action);
    }
}
