package com.example.yolov8detect.utils;


import android.content.Context;

public class Utils {
    public static int dio2px(Context context, float dpValue){
        // 获取当前手机的像素密度
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
