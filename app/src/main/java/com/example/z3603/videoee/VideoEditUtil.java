package com.example.z3603.videoee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by z3603 on 2017/6/5.
 */

public class VideoEditUtil {

    public static final String VIDEO_THUMB_CACHE = "video.cache";
    public static final String FILE_EXTENSION = ".jpeg";


    public static List<VideoThumbBean> getVideoThumb(Context context, String videoPath) {
        List<VideoThumbBean> thumbs = new ArrayList<>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        int duration = Integer.parseInt(getVideoDuration(retriever)) / 1000;
        for (int i = 0; i < 10; i++) {
            VideoThumbBean thumb = getSavedFramePath(context, retriever, i * 1000 * 1000);
            thumbs.add(thumb);
        }
        return thumbs;
    }

    public static String getVideoDuration(MediaMetadataRetriever retriever) {
        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    }

    public static VideoThumbBean getSavedFramePath(Context context, MediaMetadataRetriever retriever, long timeUs) {
        VideoThumbBean videoThumbBean = new VideoThumbBean();
        Bitmap thumb = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        StringBuffer fileName = new StringBuffer(String.valueOf(System.currentTimeMillis()));
        fileName.append(FILE_EXTENSION);
        videoThumbBean.setPath(saveThumb(context, thumb, fileName.toString()));
        videoThumbBean.setTime(timeUs);
        return videoThumbBean;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, float targetWidth) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = targetWidth / width;
//        float scaleHeight = targetHeight / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);//等比缩放
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return newBitmap;
    }

    public static String getVideoThumbDir(Context context) {
        StringBuffer videoThumb = new StringBuffer(context.getCacheDir().getAbsolutePath());
        videoThumb.append(File.separator);
        videoThumb.append(VIDEO_THUMB_CACHE);
        File cacheFile = new File(videoThumb.toString());
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        return videoThumb.toString();
    }

    public static String saveThumb(Context context, Bitmap thumb, String fileName) {
        if (thumb == null) {
            return null;
        }
        File cacheBitmap = new File(getVideoThumbDir(context), fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheBitmap);
            thumb.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cacheBitmap.getAbsolutePath();
    }

    public static void deleteCache(Context context) {
        String cachePath = getVideoThumbDir(context);
        File file = new File(cachePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
