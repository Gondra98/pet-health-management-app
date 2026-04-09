package com.example.pet_care3.fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pet_care3.adapter.AlarmAdapter;
import com.example.pet_care3.model.AlarmDataModel;
import com.example.pet_care3.R;
import com.example.pet_care3.receiver.AlarmReceiver;
import com.github.angads25.toggle.widget.DayNightSwitch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmFragment extends Fragment {

    private List<AlarmDataModel> alarmList;
    private AlarmAdapter alarmAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_alarm, container, false);

        alarmList = new ArrayList<>();
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmAdapter = new AlarmAdapter(alarmList);
        recyclerView.setAdapter(alarmAdapter);



        root.findViewById(R.id.AlarmAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAlarmDialog();
            }
        });

        return root;
    }

    // 알람 추가 다이얼로그 표시 메서드
    private void showAddAlarmDialog() {
        // 다이얼로그 생성 및 설정
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_alarm, null);
        final EditText nameInput = dialogView.findViewById(R.id.nameInput);
        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        builder.setView(dialogView)
                .setTitle("알람 추가")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String alarmName = nameInput.getText().toString();
                        int hour, minute;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            hour = timePicker.getHour();
                            minute = timePicker.getMinute();
                        } else {
                            hour = timePicker.getCurrentHour();
                            minute = timePicker.getCurrentMinute();
                        }
                        String alarmTime = String.format("%02d:%02d", hour, minute);

                        // 알람을 설정합니다.
                        setAlarm(hour, minute, alarmName);

                        // 알람 데이터를 추가하고 어댑터 갱신
                        AlarmDataModel alarmDataModel = new AlarmDataModel(alarmName, alarmTime);
                        alarmList.add(alarmDataModel);
                        alarmAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // 다이얼로그 표시
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // 알람 설정 메서드
    private void setAlarm(int hour, int minute, String alarmName) {
        Context context = getContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_NAME", alarmName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // 알람 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canScheduleExactAlarms = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                canScheduleExactAlarms = alarmManager.canScheduleExactAlarms();
            }
            if (canScheduleExactAlarms) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                // 정확한 알람 예약이 불가능한 경우 처리
                // 사용자에게 알림을 표시하거나 대체 옵션을 제공할 수 있습니다.
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

}

