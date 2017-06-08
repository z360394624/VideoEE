package com.example.z3603.videoee;

import android.animation.ValueAnimator;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LinearLayoutManager 现行管理器，支持横向、纵向。
 * GridLayoutManager 网格布局管理器
 * StaggeredGridLayoutManager 瀑布就式布局管理器
 */
public class MainActivity extends AppCompatActivity implements RangeSeekBar.OnCropRectBorderChangedListener {

    private static final String TAG = MainActivity.class.getName();

    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLayoutManager;

    private String mPath;

    private VideoPreviewAdapter mAdapter;
    private MediaMetadataRetriever mMetadataRetriever;
    private long mVideoDuration;

    private RangeSeekBar mSeekBar;
    private VideoView mVideoPreview;
    private ImageView mVideoPreviewIndicator;
    private ValueAnimator animator;

    private float mDisplayWidth;
    private float mDisplayHeight;
    private float mItemWidth;
    private long mItemDuration;
    private int mItemCount;
    private float mRecyclerViewLength;
    private int mSeekStart;
    private int mSeekStop;
    private float mLeftPosition;
    private float mRightPosition;
    private Button mCropComplete;

    private static final int UPDATE_DELAY = 200;
    private static final int UPDATE_CURRENT_DURATION = 1000;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CURRENT_DURATION:
                    int currentDuration = mVideoPreview.getCurrentPosition();
                    Log.d(TAG, "currentDuration = " + currentDuration);
                    if (mVideoPreview.isPlaying()) {
                        if (currentDuration > mSeekStop) {
                            mVideoPreview.seekTo(mSeekStart);
                            mVideoPreview.start();
                            startAnima();
                        }
                        mHandler.sendEmptyMessageDelayed(UPDATE_CURRENT_DURATION, UPDATE_DELAY);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;

        mVideoPreview = (VideoView) findViewById(R.id.preview_videoview);

        mRecyclerView = (RecyclerView) findViewById(R.id.preview_seek_bar);
        mCropComplete = (Button) findViewById(R.id.btn_complete);
        mSeekBar = (RangeSeekBar) findViewById(R.id.see_bar);
        mVideoPreviewIndicator = (ImageView) findViewById(R.id.video_edit_indicator);
        buildLayoutManager();
        mPath = Environment.getExternalStorageDirectory() + "/2.mp4";
//        mPath = Environment.getExternalStorageDirectory() + "/3.mp4";

        mMetadataRetriever = new MediaMetadataRetriever();
        mMetadataRetriever.setDataSource(mPath);
        mVideoDuration = Long.parseLong(getVideoLength());
        Log.d(TAG, "mVideoDuration = " + mVideoDuration);


        mAdapter = new VideoPreviewAdapter(this, mMetadataRetriever, mVideoDuration);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mRecyclerListener);

        int realWidth = (int) (mDisplayWidth - ImageUtils.dip2px(this, 11) * 2);
        int defaultPosition = (int) (mDisplayWidth - ImageUtils.dip2px(this, 11));
        mSeekStart = 0;
        mItemWidth = realWidth / 10;
        mItemCount = mAdapter.getItemCount();
        mRecyclerViewLength = mItemCount * mItemWidth;
        mLeftPosition = 0;
        mRightPosition = defaultPosition;
        if (mVideoDuration < 10000) {
            mItemDuration = mVideoDuration / 10;
            int minCropRectWidth = (int) (mItemWidth * 1000 / mItemDuration);
            mSeekBar.loadParams(minCropRectWidth, defaultPosition);
            mSeekStop = (int) mVideoDuration;
        } else {
            mSeekBar.loadParams(mItemWidth, defaultPosition);
            mSeekStop = 10000;
        }
        mSeekBar.setCropRectBorderChangedListener(this);
        mAdapter.setItemWidth(mItemWidth);

        mVideoPreview.setVideoPath(mPath);

        mVideoPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //开始更新播放进度
                mHandler.sendEmptyMessage(UPDATE_CURRENT_DURATION);
            }
        });
        mVideoPreview.start();
        startAnima();
        Log.d(TAG, "mSeekStart = " + mSeekStart);
        Log.d(TAG, "mSeekStop = " + mSeekStop);
        mVideoPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //重复播放
                mVideoPreview.start();
                startAnima();
            }
        });

        mCropComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClipUtil.clipVideo(MainActivity.this, mPath, mSeekStart/1000, mSeekStop/1000);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "解码错误，剪切失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        ImageUtils.stopAll(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mMetadataRetriever != null) {
            mMetadataRetriever.release();
            mMetadataRetriever = null;
        }
        if (mAdapter != null) {
            mAdapter.clearData();
            mAdapter = null;
        }
        VideoEditUtil.deleteCache(this);
        super.onDestroy();
    }

    private RecyclerView.OnScrollListener mRecyclerListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    //videoPlay
                    mSeekStart = (int) ((getScrollX() + mLeftPosition) / mRecyclerViewLength * mVideoDuration);
                    mSeekStop = (int) ((getScrollX() + mRightPosition) / mRecyclerViewLength * mVideoDuration);
                    mVideoPreview.seekTo(mSeekStart);
                    mVideoPreview.start();
                    startAnima();
                    mHandler.sendEmptyMessage(UPDATE_CURRENT_DURATION);
                    Log.d(TAG, "mSeekStart = " + mSeekStart);
                    Log.d(TAG, "mSeekStop = " + mSeekStop);
                    break;
                default:
                    //videoPause
                    mVideoPreview.pause();
                    animator.cancel();
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        }
    };


    private void startAnima() {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoPreviewIndicator.getLayoutParams();
        animator = ValueAnimator
                .ofInt((int)mLeftPosition, (int)mRightPosition)
                .setDuration(mSeekStop - mSeekStart + UPDATE_DELAY);
        animator.setInterpolator(new LinearInterpolator());
        Log.d(TAG, "startAnima mLeftPosition = " + mLeftPosition);
        Log.d(TAG, "startAnima mRightPosition = " + mRightPosition);
        Log.d(TAG, "startAnima mSeekStart = " + mSeekStart);
        Log.d(TAG, "startAnima mSeekStop = " + mSeekStop);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.leftMargin = (int) animation.getAnimatedValue();
                mVideoPreviewIndicator.setLayoutParams(params);
            }
        });
        animator.start();
    }

    private String getVideoLength() {
        return mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    }

    /**
     * 水平滑动了多少px
     *
     * @return int px
     */
    private int getScrollX() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        return (int) (position * mItemWidth - firstVisibleChildView.getLeft());
    }

    private int getItemWidth() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        return firstVisibleChildView.getWidth();
    }

    public int getVideoDegree() {
        int degree = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String degreeStr = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (!TextUtils.isEmpty(degreeStr)) {
                degree = Integer.valueOf(degreeStr);
            }
        }
        return degree;
    }

    private void buildLayoutManager() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onCropRectBorderChanged(int border, float leftPosition, float rightPosition, int action) {
        float rightPadding = 0;
        float itemWidth = getItemWidth();
        float l = 0;
        float r = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //videoPause
                mVideoPreview.pause();
                animator.cancel();
                break;
            case MotionEvent.ACTION_MOVE:
                //videoPause
                mVideoPreview.pause();
                animator.cancel();
                break;
            case MotionEvent.ACTION_UP:
                //videoPlay
                mLeftPosition = leftPosition;
                mRightPosition = rightPosition;
                mSeekStart = (int) ((getScrollX() + leftPosition) / mRecyclerViewLength * mVideoDuration);
                mSeekStop = (int) ((getScrollX() + rightPosition) / mRecyclerViewLength * mVideoDuration);
                mVideoPreview.seekTo(mSeekStart);
                mVideoPreview.start();
                startAnima();
                mHandler.sendEmptyMessage(UPDATE_CURRENT_DURATION);
                break;
        }
    }
}
