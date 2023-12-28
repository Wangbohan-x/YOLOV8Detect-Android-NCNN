package com.example.yolov8detect.utils;



import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

public class PermissionUtil {
    // 检查多个权限，返回true表示完全启用，返回false表示未完全启用
    public static boolean checkPermission(Activity act, String[] permissions, int requestCode) {
        // 6.0之后才有动态管理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = PackageManager.PERMISSION_GRANTED;
            // 检查是否有权限未被允许
            for (String permission : permissions) {
                check = ContextCompat.checkSelfPermission(act, permission);
                if (check != PackageManager.PERMISSION_GRANTED) {
                    break;
                }
            }
            // 如果有未允许的权限，弹窗
            if (check != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(act, permissions, requestCode);
                return false;
            }
        }

        return true;
    }

    public static boolean checkGrant(int[] permissions) {
        if (permissions != null) {
            for (int grant : permissions) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}


