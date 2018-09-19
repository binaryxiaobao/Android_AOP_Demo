package com.bob.www.testdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bob.www.testdemo.aop.IgnoreFastClick;

public class MainActivity extends AppCompatActivity {
    private Button mTestBtn;
    private int number = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestBtn = findViewById(R.id.btn_test);
        mTestBtn.setOnClickListener(new View.OnClickListener() {

            @IgnoreFastClick
            @Override
            public void onClick(View v) {
                Log.d("AOP", "----------------"+number++);
            }
        });
    }
}
