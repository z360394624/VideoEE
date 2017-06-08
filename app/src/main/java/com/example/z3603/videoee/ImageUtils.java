package com.example.z3603.videoee;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by z3603 on 2017/5/27.
 */

public class ImageUtils {

    public static void loadRemoteImage(Activity activity, ImageView imageView, String url) {
        Glide.with(activity).load(url).into(imageView);
    }

    public static void loadLocalImage(final Activity activity, final ImageView imageView, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] bytes = baos.toByteArray();
        Glide.with(activity).load(bytes).into(imageView);
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadLocalImage(final Activity activity, final ImageView imageView, String localUri) {
        File image = new File(localUri);
        Glide.with(activity).load(image).into(imageView);
    }

    public static int dip2px(Context context, int dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((float) dip * scale + 0.5F);
    }

    public static void stopAll(final Activity activity) {
        Glide.with(activity).onDestroy();
    }

}
