package com.example.yolov8detect;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yolov8detect.database.UseDBHelper;
import com.example.yolov8detect.entiy.User;
import com.example.yolov8detect.utils.ToastUtil;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText_id;
    private EditText editText_pws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_id = findViewById(R.id.edittext_id);
        editText_pws = findViewById(R.id.edittext_pws);

        Button btn_login = findViewById(R.id.btn_login);
        Button btn_find_pws = findViewById(R.id.btn_find_pws);
        Button btn_register = findViewById(R.id.btn_register);

        btn_login.setOnClickListener(this);
        btn_find_pws.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        UseDBHelper mDB = UseDBHelper.getInstance(getApplicationContext());
        mDB.openReadLink();
        mDB.openWriteLink();
        String name = editText_id.getText().toString();
        String pws = editText_pws.getText().toString();
        // 登录
        if(v.getId() == R.id.btn_login){
            List<User> users = mDB.queryByName(name);
            if(users.size() == 0){
                ToastUtil.show(getApplicationContext(), "未注册");
            } else if (users.size() == 1) {
                if(pws.equals(users.get(0).pws) ){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("id", users.get(0).name);
                    intent.putExtra("phone", users.get(0).phone);
                    intent.putExtra("pws", users.get(0).pws);
                    startActivity(intent);
                }
                else {
                    ToastUtil.show(getApplicationContext(), "密码错误");
                }
            }
            // 找回密码
        } else if (v.getId() == R.id.btn_find_pws) {
            if(name.equals("")){
                ToastUtil.show(getApplicationContext(), "请输入账号");
            } else {
                List<User> users = mDB.queryByName(name);
                if(users.size() == 0){
                    ToastUtil.show(getApplicationContext(), "账号不存在，请重新输入");
                } else if (users.size() == 1) {
                    Intent intent = new Intent(getApplicationContext(), RefindPWS.class);
                    intent.putExtra("name", users.get(0).name);
                    startActivity(intent);
                }
            }
            // 注册
        } else if (v.getId() == R.id.btn_register) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        }
        mDB.closeLink();
    }
}