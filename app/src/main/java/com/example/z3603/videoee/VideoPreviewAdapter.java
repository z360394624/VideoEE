package com.example.z3603.videoee;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by z3603 on 2017/6/7.
 */

public class VideoPreviewAdapter extends RecyclerView.Adapter<VideoPreviewAdapter.MyViewHolder> {

    private static final String TAG = VideoPreviewAdapter.class.getName();
    private LayoutInflater mInflater;
    private List<String> mData = new ArrayList<>();

    private View view;
    private Context mContext;
    private MediaMetadataRetriever mRetriever;
    private long mVideoLength;
    private float mItemWidth;
    private int mItemCount;
    private int mItemDuration;
    private List<VideoThumbBean> mVideoThumbs = new ArrayList<>();

    public VideoPreviewAdapter(Context context, MediaMetadataRetriever retriever, long duration) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mRetriever = retriever;
        this.mVideoLength = duration;
        if (mVideoLength > 10000) {
            mItemCount = (int) (mVideoLength / 1000);
            mItemDuration = 1000;
        } else {
            mItemCount = 10;
            mItemDuration = (int) (mVideoLength / 10);
        }
        Log.d(TAG, "mItemCount = " + mItemCount);
        Log.d(TAG, "mVideoLength = " + mVideoLength);
    }

    public void setItemWidth(float itemWidth) {
        this.mItemWidth = itemWidth;
    }

    public void clearData() {
        if (mVideoThumbs != null && mVideoThumbs.size() > 0) {
            mVideoThumbs.clear();
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recycler_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view, mItemWidth);
        Log.d("adapter", "onCreateViewHolder mItemWidth = " + mItemWidth);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (holder.imageView != null) {
            holder.imageView.setImageBitmap(null);
        }
        Flowable<VideoThumbBean> frameFinder = Flowable.create(new FlowableOnSubscribe<VideoThumbBean>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<VideoThumbBean> e) throws Exception {
                int time = position * mItemDuration * 1000;
                Log.d("adapter", "start get bitmap time = " + time);
                VideoThumbBean thumb = contains(mVideoThumbs, time);
                if (thumb == null) {
                    thumb = new VideoThumbBean();
                    long start = System.currentTimeMillis();
                    Bitmap bitmap = mRetriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST);
                    int scaleWidth = ImageUtils.dip2px(mContext, 20);
                    Bitmap scaledBitmap = VideoEditUtil.scaleBitmap(bitmap, scaleWidth);
                    Log.d("adapter", "cost time = " + (System.currentTimeMillis() - start));
                    thumb.setTime(time);
                    String fileName = String.valueOf(System.currentTimeMillis());
                    String path = VideoEditUtil.saveThumb(mContext, scaledBitmap, fileName);
                    mVideoThumbs.add(thumb);
                    thumb.setPath(path);
                }
                e.onNext(thumb);
            }
        }, BackpressureStrategy.DROP);
        FlowableSubscriber<VideoThumbBean> subscriber = new FlowableSubscriber<VideoThumbBean>() {
            @Override
            public void onSubscribe(@NonNull Subscription s) {
                s.request(Long.MAX_VALUE);
                holder.setSwitcher(s);
            }

            @Override
            public void onNext(VideoThumbBean bitmap) {
                ImageUtils.loadLocalImage((Activity) mContext, holder.imageView, bitmap.getPath());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        };
        frameFinder.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        holder.setCanLoad(false);
        holder.getSwitcher().cancel();
        Log.d("adapter", "holder cancel show");
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    private VideoThumbBean contains(List<VideoThumbBean> thumbs, long time) {
        VideoThumbBean t = null;
        if (thumbs == null || thumbs.size() == 0) {
            return null;
        }
        for (VideoThumbBean bean : thumbs) {
            if (bean.getTime() == time) {
                t = bean;
                break;
            }
        }
        return t;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        private boolean canLoad;

        private Subscription switcher;

        public MyViewHolder(View view, float itemWidth) {
            super(view);
            canLoad = true;
            imageView = (ImageView) view.findViewById(R.id.item_image);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = (int) itemWidth;
            imageView.setLayoutParams(params);
        }

        public boolean canLoad() {
            return canLoad;
        }

        public void setCanLoad(boolean canLoad) {
            this.canLoad = canLoad;
        }

        public Subscription getSwitcher() {
            return switcher;
        }

        public void setSwitcher(Subscription switcher) {
            this.switcher = switcher;
        }
    }

}