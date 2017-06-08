package com.example.z3603.videoee;

import android.graphics.Bitmap;

/**
 * Created by z3603 on 2017/6/5.
 */

public class VideoThumbBean {

    private String path;
    private long time;
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoThumbBean that = (VideoThumbBean) o;
        if (time != that.time) return false;
        else return true;
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "VideoThumbBean{" +
                "path='" + path + '\'' +
                ", time=" + time +
                '}';
    }
}
