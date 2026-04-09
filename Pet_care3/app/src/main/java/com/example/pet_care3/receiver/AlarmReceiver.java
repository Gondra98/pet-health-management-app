package com.example.pet_care3.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람이 울렸을 때 실행될 코드를 여기에 작성합니다.
        String alarmName = intent.getStringExtra("ALARM_NAME");
        Toast.makeText(context, "알람이 울립니다: " + alarmName, Toast.LENGTH_SHORT).show();
    }
}


