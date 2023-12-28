package com.example.yolov8detect;

import static android.app.usage.UsageEvents.Event.NONE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.yolov8detect.databinding.ActivityMainBinding;
import com.example.yolov8detect.entiy.MyAppInfo;
import com.example.yolov8detect.entiy.User;
import com.example.yolov8detect.utils.BitmapUtil;
import com.example.yolov8detect.utils.GetScreenUtil;
import com.example.yolov8detect.utils.ToastUtil;
import com.example.yolov8detect.utils.TouchUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'yolov8detect' library on application startup.
    static {
        System.loadLibrary("yolov8detect");
    }


    private ActivityMainBinding binding;
    private static final int SELECT_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> mResultLauncher;
    private ImageView imageView;
    private Bitmap bitmap = null;
    private Bitmap yourSelectedImage = null;
    private Bitmap showImage = null;

    private YoloV8Ncnn yolov8ncnn = new YoloV8Ncnn();
    private TextView text1;

    // 手指滑动部分
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // 第一个按下的手指的点
    private PointF startPoint = new PointF();
    // 两个按下的手指的触摸点的中点
    private PointF midPoint = new PointF();
    // 初始的两个手指按下的触摸点的距离
    private float oriDis = 1f;
    private int scaledWidth;
    private int scaledHeight;
    private RadioButton rb_person;
    private RadioButton rb_sheep;
    private RadioButton rb_sticks;

    private User user = new User();
    private int index;
    private Button item1,item2,item3;
    private boolean mIsMenuOpen =false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        // 获取用户信息
        Intent intent = getIntent();
        user.name = intent.getStringExtra("id");
        user.phone = intent.getStringExtra("phone");
        user.pws = intent.getStringExtra("pws");


//        RadioGroup RG_btn = findViewById(R.id.RG_btn);
//        RG_btn.child


        rb_person = findViewById(R.id.detect_person);
        rb_sheep = findViewById(R.id.detect_sheep);
        rb_sticks = findViewById(R.id.detect_sticks);
        index = -1;
        if(rb_sticks.isChecked()){
            index = 0;
        } else if (rb_person.isChecked()) {
            index = 1;
        } else if (rb_sheep.isChecked()) {
            index = 2;
        }

        // 初始化模型
//        boolean ret_init = yolov8ncnn.Init(getAssets(), rb_person.isChecked());
        boolean ret_init = yolov8ncnn.Init(getAssets(), index);
        if (!ret_init) {
            Log.e("MainActivity", "yolov8ncnn Init failed");
        }
        text1 = (TextView) findViewById(R.id.showText);
        imageView = (ImageView) findViewById(R.id.imageView);

        rb_sticks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = 0;
                yolov8ncnn.Init(getAssets(), 0);
            }
        });
        rb_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = 1;
                yolov8ncnn.Init(getAssets(), 1);
            }
        });
        rb_sheep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = 2;                                                                                                                                                                       
                yolov8ncnn.Init(getAssets(), 2);
            }
        });



        // 选取图片
        Button buttonImage = (Button) findViewById(R.id.buttonImage);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });


        Button takePhoto = findViewById(R.id.take_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 动态申请权限
                // 创建一个File对象，用于保存摄像头拍下的图片，这里把图片命名为output_image.jpg
                // 并将它存放在手机SD卡的应用关联缓存目录下
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                // 对照片的更换设置
                try {
                    // 如果上一次的照片存在，就删除
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    // 创建一个新的文件
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 如果Android版本大于等于7.0
                if (Build.VERSION.SDK_INT >= 24) {
                    // 将File对象转换成一个封装过的Uri对象
                    imageUri = FileProvider.getUriForFile(MainActivity.this, "yolov8.fileprovider", outputImage);
                    Log.d("CV", outputImage.toString() + "手机系统版本高于Android7.0");
                } else {
                    // 将File对象转换为Uri对象，这个Uri标识着output_image.jpg这张图片的本地真实路径，Uri.fromFile是个过时的代码
                    Log.d("CV", outputImage.toString() + "手机系统版本低于Android7.0");
                    imageUri = Uri.fromFile(outputImage);
                }
                //相机拍照后进行裁剪
                // 动态申请权限
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO);
                } else {
                    // 启动相机程序
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    // 指定图片的输出地址为imageUri
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, TAKE_PHOTO);
                }
            }
        });


        // 进行检测，并且返回展示
        Button buttonDetect = (Button) findViewById(R.id.buttonDetect);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage == null)
                    return;

                imageView.setBackground(null);

                YoloV8Ncnn.Obj[] objects = yolov8ncnn.Detect(yourSelectedImage, false, index);
                Log.d("ning", "检测结束");

                showObjects(objects);
                Log.d("ning", "展示结束");
            }
        });

        // 读取
        /*Button buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ImageList.class);
                startActivity(intent);
            }
        });*/

        //保存
        Button buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                if (bitmapDrawable != null) {
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    saveToSystemGallery(getApplicationContext(), bitmap);
                    ToastUtil.show(getApplicationContext(), "保存成功");
                } else {
                    ToastUtil.show(getApplicationContext(), "无法保存");
                }
            }
        });

        // 用户信息
        /*Button buttonUerInfo = (Button) findViewById(R.id.buttonUerInfo);
        buttonUerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), UserInfo.class);
                intent1.putExtra("id", user.name);
                intent1.putExtra("phone", user.phone);
                intent1.putExtra("pws", user.pws);
                startActivity(intent1);
            }
        });*/

        // 放大图片
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bigImageLoader(bitmap);
            }
        });



        //先重写 setOnLongClickListener()
        // 手指滑动事件
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();
                Log.d("ning", "onTouch: x= " + x + "y=" + y);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        //单点触控
                        matrix.set(view.getImageMatrix());
                        savedMatrix.set(matrix);
                        startPoint.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //多点触控
                        oriDis = TouchUtil.distance(event);
                        if (oriDis > 10f) {
                            savedMatrix.set(matrix);
                            midPoint = TouchUtil.midPoint(event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 手指滑动事件
                        if (mode == DRAG) {
                            // 是一个手指拖动
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - startPoint.x, event.getY()
                                    - startPoint.y);
                            Log.d("ning", "mode=" + mode);
                        } else if (mode == ZOOM) {
                            // 两个手指滑动
                            float newDist = TouchUtil.distance(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oriDis;
                                matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                                Log.d("ning", "mode=" + mode);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        // 手指放开事件
                        mode = NONE;
                        Log.d("ning", "手指松开");
                        break;
                }
                view.setImageMatrix(matrix);
                return true;
            }

        });

        Button menu = findViewById(R.id.menu);
        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        item3 = findViewById(R.id.item3);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsMenuOpen){
                    mIsMenuOpen = true;
                    oPenMenu();
                }else{
                    mIsMenuOpen = false;
                    closeMenu();
                }
            }
        });
        item1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent1 = new Intent(getApplicationContext(),LoginActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
            }
        });
        item2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent2 = new Intent(getApplicationContext(),ImageList.class);
                startActivity(intent2);
            }
        });
        item3.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent3 = new Intent(getApplicationContext(), UserInfo.class);
                intent3.putExtra("id", user.name);
                intent3.putExtra("phone", user.phone);
                intent3.putExtra("pws", user.pws);
                startActivity(intent3);
            }
        });

    }


    private void oPenMenu() {
        doAnimateOpen(item1, 0, 3, 300);
        doAnimateOpen(item2, 1, 3, 300);
        doAnimateOpen(item3, 2, 3, 300);
    }
    private void doAnimateOpen(View view, int index, int total, int radius) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        double degree = Math.toRadians(90) / (total - 1) * index;
        int translationX = -(int) (radius * Math.sin(degree));
        int translationY = -(int) (radius * Math.cos(degree));
        AnimatorSet set = new AnimatorSet();
        //包括平移 缩放 透明 动画
        set.playTogether(ObjectAnimator.ofFloat(view, "translationX", 0, translationX),
                ObjectAnimator.ofFloat(view, "translationY", 0, translationY),
                ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f),
                ObjectAnimator.ofFloat(view, "alpha", 0f, 1f));
        set.setDuration(800).start();
    }

    private void closeMenu() {
        doAnimateClose(item1,0,3,300);
        doAnimateClose(item2,1,3,300);
        doAnimateClose(item3,2,3,300);
    }

    private void doAnimateClose(View view, int index, int total, int radius) {
        if (view.getVisibility()!=View.VISIBLE){
            view.setVisibility(View.VISIBLE);
        }
        double degree = Math.PI * index / ((total - 1) * 2);
        int translationX = -(int)(radius*Math.sin(degree));
        int translationY = -(int)(radius*Math.cos(degree));
        AnimatorSet set = new AnimatorSet();
        //包括平移 缩放 透明 动画
        set.playTogether(ObjectAnimator.ofFloat(view, "translationX", translationX,0),
                ObjectAnimator.ofFloat(view, "translationY", translationY,0),
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.1f),
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f));
        set.setDuration(800).start();
    }




    private void bigImageLoader(Bitmap bitmap) {
        final Dialog dialog = new Dialog(this);
        ImageView image = new ImageView(getApplicationContext());
        image.setImageBitmap(bitmap);
        dialog.setContentView(image);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }


    private void saveToSystemGallery(Context context, Bitmap bitmap) {
        File dir = new File(Environment.getExternalStorageDirectory(), "MyAlbums");
        if (!dir.exists()) {
            dir.mkdir();
        }
        String filename = System.currentTimeMillis() + MyAppInfo.getAppName() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");


        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            OutputStream out = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showObjects(YoloV8Ncnn.Obj[] objects) {

        if (objects == null) {
            imageView.setImageBitmap(bitmap);
            text1.setText("识别失败");
            return;
        }
        text1.setText("识别成功");
        // draw objects on bitmap
        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int[] num = new int[3];
        String[] classes = {"sticks", "person", "sheep"};

        for (int i = 0; i < num.length; i++) {
            num[i] = 0;
        }

        final int[] colors = new int[]{
                Color.rgb(54, 67, 244),
                Color.rgb(99, 30, 233),
                Color.rgb(176, 39, 156),
                Color.rgb(183, 58, 103),
                Color.rgb(181, 81, 63),
                Color.rgb(243, 150, 33),
                Color.rgb(244, 169, 3),
                Color.rgb(212, 188, 0),
                Color.rgb(136, 150, 0),
                Color.rgb(80, 175, 76),
                Color.rgb(74, 195, 139),
                Color.rgb(57, 220, 205),
                Color.rgb(59, 235, 255),
                Color.rgb(7, 193, 255),
                Color.rgb(0, 152, 255),
                Color.rgb(34, 87, 255),
                Color.rgb(72, 85, 121),
                Color.rgb(158, 158, 158),
                Color.rgb(139, 125, 96)
        };

        Canvas canvas = new Canvas(rgba);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.WHITE);
        textpaint.setTextSize(35);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++) {
            paint.setColor(colors[i % 19]);
            if (objects[i].label.equals(classes[0])  && rb_sticks.isChecked()) {
                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);
                num[0]++;
            }
            if (objects[i].label.equals(classes[1])  && rb_person.isChecked()) {
                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);
                num[1]++;
            }
            if (objects[i].label.equals(classes[2])  && rb_sheep.isChecked()) {
                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);
                num[2]++;
            }
        }
        showImage = BitmapUtil.scaleBitmap(rgba, scaledWidth, scaledHeight);
        Canvas canvas_text = new Canvas(showImage);
        String text = "";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateformat.format(System.currentTimeMillis());

        for (int i = 0; i < num.length; i++) {
            if (num[i] != 0) {
                text += classes[i] + " : " + num[i] + " ";
                text += " 日期：" + dateStr;
            }
            canvas_text.drawText(text, 10, 100, textpaint);
        }

        imageView.setImageBitmap(showImage);
        text1.setText(text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_IMAGE:
                if (resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                    try {
                        bitmap = decodeUri(selectedImage);

                        yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        // 缩放
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        int scaled = 0;
                        scaledWidth = GetScreenUtil.getWidth(getApplicationContext());
                        scaled = scaledWidth / width;
                        scaledHeight = height * scaled;


                        Matrix matrix1 = new Matrix();
                        matrix1.postScale((float) scaledWidth, (float) scaledHeight);

                        showImage = BitmapUtil.scaleBitmap(bitmap, scaledWidth, scaledHeight);

                        imageView.setImageBitmap(showImage);
                    } catch (FileNotFoundException e) {
                        Log.e("MainActivity", "FileNotFoundException");
                        return;
                    }
                }
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));// 将图片解析成Bitmap对象
                        yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                super.onActivityResult(requestCode, resultCode, data);
                break;
            default:
                break;
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 640;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        // Rotate according to EXIF
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            Log.e("MainActivity", "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}