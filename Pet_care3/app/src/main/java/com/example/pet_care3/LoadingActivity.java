package com.example.pet_care3;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);


        int selectedItemId = getIntent().getIntExtra("selectedItemId", R.id.home); // 디폴트값은 home

        // 일정 시간 후에 다음 화면으로 이동하는 코드
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 현재 화면 종료
                finish();

                // 로딩 액티비티로 이동
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                // 선택된 버튼의 ID를 전달 (4번 메뉴인 setting의 아이디 전달)
                intent.putExtra("selectedItemId", selectedItemId);
                startActivity(intent);
            }
        }, 7000);
    }
}
