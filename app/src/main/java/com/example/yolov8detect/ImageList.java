package com.example.yolov8detect;


import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.yolov8detect.entiy.ImageInfo;
import com.example.yolov8detect.entiy.MyAppInfo;
import com.example.yolov8detect.utils.GetScreenUtil;
import com.example.yolov8detect.utils.PermissionUtil;
import com.example.yolov8detect.utils.ToastUtil;
import com.example.yolov8detect.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageList extends AppCompatActivity {

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQUEST_CODE = 1;

    private Button msLoadImgBtn;

    // 存储图片信息
    private List<ImageInfo> mImageList = new ArrayList<>();
    private GridLayout msGridList;

    private List<String> mImageId = new ArrayList<>();
    private Map<String, Long> mMap = new HashMap<String, Long>();
    private String[] path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_list);

//        msLoadImgBtn = findViewById(R.id.MS_load_img_btn);
        msGridList = findViewById(R.id.MS_Image_list);

        if (PermissionUtil.checkPermission(this, PERMISSIONS, PERMISSION_REQUEST_CODE)) {
            // 加载图片列表到mImageList中
            loadImageList();
            Log.d("Ning", "loadImage结束");
            // 展示图片
            showImageList();
            Log.d("Ning", "showImage结束");
        }
//        imageClick();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        msGridList.removeAllViews();
        mImageId.clear();
        mMap.clear();
        loadImageList();
        showImageList();
    }

    @Override
    // 回调方法，判断是否授权
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE &&
                PermissionUtil.checkGrant(grantResults)) {
            // 加载图片列表到mImageList中
            loadImageList();
            // 展示图片
            showImageList();
        } else {
            ToastUtil.show(this, "授权失败");
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }


    private void showImageList() {

        msGridList.removeAllViews();
        int num = 0;
        Log.d("ning", mImageList.size()+"");
        for (String image : mImageId) {
            // image -> imageview ->gridlayout
            Log.d("ning", "开始读取" + image);
            ImageView msIv = new ImageView(this);
            Bitmap bitmap = BitmapFactory.decodeFile(image);
            msIv.setImageBitmap(bitmap);

            // 设置mImage的点击事件
            msIv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    bigImageLoader(bitmap);
                }
            });


//            int finalNum = num;
            msIv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ImageDelete.class);
                    intent.putExtra("id", mMap.get(image));
                    startActivity(intent);
                    return false;
                }
            });

            // 设置缩放
            msIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            // 宽高
//            int px = Utils.dio2px(this, GetScreenUtil.getWidth(getApplicationContext()));
            int px = GetScreenUtil.getWidth(getApplicationContext()) / 4;
            px -= 5;
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(px, px);
            msIv.setLayoutParams(params);

            int padding = Utils.dio2px(this, 5);
            msIv.setPadding(padding, padding, padding, padding);



            msGridList.addView(msIv);
            num++;

        }
    }

    //点击事件
//    private void imageClick(){
//        int children = msGridList.getChildCount();
//        Log.d("ning", "children="+children);
//
//        for(int i = 0; i < children; i++){
//
//            View imageDelete = msGridList.getChildAt(i);
//
//            int finalI = i;
//            imageDelete.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(msGridList.getContext());
////                    builder.setTitle("提示");
////                    builder.setMessage("是否删除:"+ finalImage);
//                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            int index = finalI;
//                            mImageId.remove(index);
////                            int row = getContentResolver().delete(finalUri, null, null);
////                            Log.d("ning", "删除成功");
////                            loadImageList();
//                            showImageList();
//                        }
//                    });
//                    builder.setNegativeButton("取消", null);
//                    builder.show();
//                    return false;
//                }
//            });
//        }
//
//    }

    private void bigImageLoader(Bitmap bitmap)  {
        final Dialog dialog = new Dialog(this);
        ImageView image = new ImageView(getApplicationContext());
        image.setImageBitmap(bitmap);
        dialog.setContentView(image);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.cancel();
            }
        });
    }

    @SuppressLint("Range")
    // 加载图片
    private void loadImageList() {

        // 查询的列
        String[] columns = new String[]{
                MediaStore.Images.Media._ID,    //编号
                MediaStore.Images.Media.TITLE,  //标题
                MediaStore.Images.Media.SIZE,   //大小
                MediaStore.Images.Media.DATA,   //路径
        };
        // 加载应用中的图片 MediaStore
        // 数据库查询
//        String
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                "_id DESC"
        );

        int count = 0;
        if (cursor != null) {
            mImageList.clear();
            ImageInfo imageInfo = new ImageInfo();
            while (cursor.moveToNext()) {
                // 获取图片信息
                imageInfo.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                imageInfo.name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
                imageInfo.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                imageInfo.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                if (imageInfo.path.contains(MyAppInfo.getAppName())){
                    count++;
                    mImageList.add(imageInfo);
                    Log.d("ning", "image" + imageInfo.toString());
                    mImageId.add(imageInfo.path);
                    mMap.put(imageInfo.path, imageInfo.id);
                }
            }
        }
    }
}