package com.example.yolov8detect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText register_name;
    private EditText register_phone;
    private EditText register_pws1;
    private EditText register_pws2;

    @Override
    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        register_name = findViewById(R.id.register_name);
        register_phone = findViewById(R.id.register_phone);
        register_pws1 = findViewById(R.id.register_pws1);
        register_pws2 = findViewById(R.id.register_pws2);

        Button register_btn_confirm = findViewById(R.id.register_btn_confirm);
        Button register_back = findViewById(R.id.register_back);

        register_btn_confirm.setOnClickListener(this);
        register_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        String name = register_name.getText().toString();
        String phone = register_phone.getText().toString();
        String pws1 = register_pws1.getText().toString();
        String pws2 = register_pws2.getText().toString();
        if (v.getId() == R.id.register_btn_confirm) {
            if (!name.equals("") && !phone.equals("") && !pws1.equals("") && !pws2.equals("")) {
                if (register_pws1.getText().toString().equals(register_pws2.getText().toString())) {
                    UseDBHelper mDB = UseDBHelper.getInstance(getApplicationContext());
                    mDB.openWriteLink();
                    mDB.openReadLink();
                    List<User> list = mDB.queryByName(name);
                    if(list.size() == 0){
                        long l = mDB.insert(new User(phone, pws1, name));
                        ToastUtil.show(getApplicationContext(), "注册成功");
                        startActivity(intent);
                    }
                    else {
                        ToastUtil.show(getApplicationContext(), "账号已存在，请重新注册");
                    }
//                    intent.putExtra("name", name);
//                    intent.putExtra("pws", pws1);
                    mDB.closeLink();

                }else {
                    ToastUtil.show(getApplicationContext(), "密码不一致");
                }
            }else {
                ToastUtil.show(getApplicationContext(), "资料不能为空");
            }
        } else if (v.getId() == R.id.register_back) {
            startActivity(intent);
        }
    }
}