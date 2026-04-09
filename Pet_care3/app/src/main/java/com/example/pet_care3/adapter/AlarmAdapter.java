package com.example.pet_care3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pet_care3.R;
import com.example.pet_care3.model.AlarmDataModel;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<AlarmDataModel> dataList;

    // 생성자를 통해 데이터 리스트를 전달받음
    public AlarmAdapter(List<AlarmDataModel> dataList) {
        this.dataList = dataList;
    }

    // 뷰홀더 클래스 정의
    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmNameTextView;
        TextView alarmTimeTextView; // 알람 시간을 표시할 TextView 변수 추가

        public AlarmViewHolder(View itemView) {
            super(itemView);
            // 뷰홀더 안의 뷰들을 초기화
            alarmNameTextView = itemView.findViewById(R.id.routineName);
            alarmTimeTextView = itemView.findViewById(R.id.timeSelect); // 알람 시간을 표시할 TextView 초기화
        }
    }


    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 뷰홀더를 생성하고 반환
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmDataModel data = dataList.get(position);
        holder.alarmNameTextView.setText(data.getAlarmName());
        holder.alarmTimeTextView.setText(data.getAlarmTime());
    }




    @Override
    public int getItemCount() {
        // 데이터 리스트의 크기 반환
        return dataList.size();
    }
}
