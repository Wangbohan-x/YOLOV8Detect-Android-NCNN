package com.example.yolov8detect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yolov8detect.database.UseDBHelper;
import com.example.yolov8detect.entiy.User;
import com.example.yolov8detect.utils.ToastUtil;

import java.util.List;

public class RefindPWS extends AppCompatActivity implements View.OnClickListener {

    private EditText re_find_pws1;
    private EditText re_find_pws2;
    private String name;

    @Override
    @SuppressLint({"MissingInflatedId", "LocalSuppress", "ResourceAsColor"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refind_pws);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        EditText re_find_id = findViewById(R.id.re_find_id);
        re_find_id.setText(name);
        re_find_id.setTextColor(R.color.black);
        re_find_pws1 = findViewById(R.id.re_find_pws1);
        re_find_pws2 = findViewById(R.id.re_find_pws2);

        Button re_find_btn = findViewById(R.id.re_find_btn);
        Button re_find_back = findViewById(R.id.re_find_back);

        re_find_btn.setOnClickListener(this);
        re_find_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        UseDBHelper mDB = UseDBHelper.getInstance(getApplicationContext());
        mDB.openReadLink();
        mDB.openWriteLink();
        if(v.getId() == R.id.re_find_btn){
            if(re_find_pws1.getText().toString().equals(re_find_pws2.getText().toString()) && !re_find_pws1.getText().toString().equals(null)){
                List<User> list = mDB.queryByName(name);
                list.get(0).pws = re_find_pws1.getText().toString();
                mDB.Update(list.get(0));
                ToastUtil.show(getApplicationContext(), "重置成功");
                mDB.closeLink();
                startActivity(intent);
                finish();
            }else {
                ToastUtil.show(getApplicationContext(), "密码不一致");
            }
        } else if (v.getId() == R.id.re_find_back) {
            mDB.closeLink();
            startActivity(intent);
            finish();
        }

    }
}