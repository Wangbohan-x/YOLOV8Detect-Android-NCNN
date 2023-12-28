package com.example.yolov8detect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDelete extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_delete);
        ImageView imageView = findViewById(R.id.image_delete_image);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView non = findViewById(R.id.non);

        AlertDialog.Builder builder = new AlertDialog.Builder(non.getContext());
        builder.setTitle("提示");
        builder.setMessage("是否删除:");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("ning", "删除");
                Intent intent = getIntent();
                long id = intent.getLongExtra("id", 0);
                Log.d("ning", "id="+id);
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


                int row = getContentResolver().delete(uri, null, null);
                Log.d("ning", "删除成功");
                Intent intent1 = new Intent(getApplicationContext(), ImageList.class);
                startActivity(intent1);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent1 = new Intent(getApplicationContext(), ImageList.class);
                startActivity(intent1);
            }
        });
        builder.show();



    }
}