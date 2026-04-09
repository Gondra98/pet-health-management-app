package com.example.pet_care3;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pet_care3.adapter.ContentsPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;
    private ContentsPagerAdapter contentsPagerAdapter;

    private long backPressedTime = 0;
    private static final long FINISH_INTERVAL_TIME = 2000; // 2초 내에 뒤로가기 버튼을 두 번 눌러야 앱이 종료됨

    // onCreate 메서드 내에 추가
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 초기화
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager2 = findViewById(R.id.viewPager);

        // ViewPager2 어댑터 설정
        contentsPagerAdapter = new ContentsPagerAdapter(this);
        viewPager2.setAdapter(contentsPagerAdapter);

        // BottomNavigationView 아이템 선택 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            // 아이템 ID 가져오기
            int itemId = item.getItemId();
            // itemId에 따라서 ViewPager2의 현재 아이템 설정
            if (itemId == R.id.home) {
                viewPager2.setCurrentItem(0, true);
                return true;
            } else if (itemId == R.id.alarm) {
                viewPager2.setCurrentItem(1, true);
                return true;
            } else if (itemId == R.id.health) {
                viewPager2.setCurrentItem(2, true);
                return true;
            } else if (itemId == R.id.setting) {
                viewPager2.setCurrentItem(3, true);
                return true;
            }
            return false;
        });

        // ViewPager2 페이지 변경 리스너 설정
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // ViewPager2 페이지가 변경될 때 BottomNavigationView의 선택 항목 변경
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });

        // 이전 액티비티(로딩 액티비티)에서 전달받은 아이템 ID를 확인하여 설정(4번) 버튼 선택
        int selectedItemId = getIntent().getIntExtra("selectedItemId", R.id.home);
        bottomNavigationView.setSelectedItemId(selectedItemId);
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            // 어플 완전 종료
            finishAffinity();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}



