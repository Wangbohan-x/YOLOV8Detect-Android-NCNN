package com.example.yolov8detect;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class YoloV8Ncnn {
    public native boolean Init(AssetManager mgr, int index);

    public class Obj
    {
        public float x;
        public float y;
        public float w;
        public float h;
        public String label;
        public float prob;
    }

    public native Obj[] Detect(Bitmap bitmap, boolean use_gpu, int index);

    static {
        System.loadLibrary("yolov8detect");
    }
}
