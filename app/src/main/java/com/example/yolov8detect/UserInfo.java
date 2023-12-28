package com.example.yolov8detect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.yolov8detect.database.UseDBHelper;
import com.example.yolov8detect.entiy.User;
import com.example.yolov8detect.utils.ToastUtil;

import java.util.List;

public class UserInfo extends AppCompatActivity {

    private User user = new User();
    private EditText userInfo_name;
    private EditText userInfo_phone;
    private EditText userInfo_pws;
    private UseDBHelper mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // 获取用户信息
        Intent intent = getIntent();
        user.name = intent.getStringExtra("id");
        user.phone = intent.getStringExtra("phone");
        user.pws = intent.getStringExtra("pws");
        mDB = UseDBHelper.getInstance(getApplicationContext());
        mDB.openReadLink();
        mDB.openWriteLink();
        List<User> list = mDB.queryByName(user.name);

        userInfo_name = findViewById(R.id.UserInfo_name);
        userInfo_phone = findViewById(R.id.UserInfo_phone);
        userInfo_pws = findViewById(R.id.UserInfo_pws);

        userInfo_name.setText(list.get(0).name);
        userInfo_phone.setText(list.get(0).phone);
        userInfo_pws.setText(list.get(0).pws);

        final boolean[] cover = {true};
        ImageView UserInfo_icon = findViewById(R.id.UserInfo_icon);
        UserInfo_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cover[0]){
                    UserInfo_icon.setImageResource(R.drawable.uncovered);
                    userInfo_pws.setTransformationMethod(null);
                    cover[0] = false;
                }else {
                    UserInfo_icon.setImageResource(R.drawable.covered);
                    userInfo_pws.setTransformationMethod(new PasswordTransformationMethod());
                    cover[0] = true;
                }
            }
        });




//        Button UserInfo_edit = findViewById(R.id.UserInfo_modify);
//        UserInfo_edit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                userInfo_name.setEnabled(true);
////                userInfo_phone.setEnabled(true);
////                userInfo_pws.setEnabled(true);
//                UserInfo_icon.setImageResource(R.drawable.uncovered);
//                userInfo_pws.setTransformationMethod(new PasswordTransformationMethod());
//                cover[0] = false;
//            }
//        });
        Button UserInfo_modify = findViewById(R.id.UserInfo_modify);
        UserInfo_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.id = list.get(0).id;
                user.name = userInfo_name.getText().toString();
                user.phone = userInfo_phone.getText().toString();
                user.pws = userInfo_pws.getText().toString();
                mDB.Update(user);
//                userInfo_name.setEnabled(false);
//                userInfo_phone.setEnabled(false);
//                userInfo_pws.setEnabled(false);
                ToastUtil.show(getApplicationContext(), "修改成功");

            }
        });

        Button UserInfo_out = findViewById(R.id.UserInfo_out);
        UserInfo_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
            }
        });
    }

}