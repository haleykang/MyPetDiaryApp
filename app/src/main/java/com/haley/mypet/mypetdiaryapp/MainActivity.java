package com.haley.mypet.mypetdiaryapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartClick(View v) {
        // 단순 페이지 이동
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        // 페이드 인 페이드 아웃 화면 전환
        // 주의 ! startActivity 후 에 실행 할 것
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();

    }
}
