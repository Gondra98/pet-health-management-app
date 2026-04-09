package com.example.pet_care3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pet_care3.R;

import java.util.ArrayList;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {

    // 데이터 목록을 보유하는 ArrayList
    private ArrayList<String> routines;
    private OnItemLongClickListener longClickListener; // 롱클릭 리스너 선언

    // 롱클릭 리스너 인터페이스 정의
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    // 롱클릭 리스너 설정 메서드
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    // 생성자: 데이터 목록을 받아옴
    public CheckListAdapter(ArrayList<String> routines) {
        this.routines = routines;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 뷰 홀더 생성 및 레이아웃 연결
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_checklist, parent, false);
        return new ViewHolder(view); // 여기서 ViewHolder를 올바르게 인스턴스화합니다
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 지정된 위치의 데이터를 뷰 홀더의 뷰에 바인딩
        String routine = routines.get(position);
        holder.routineName.setText(routine);

        // 아이템 롱클릭 이벤트 처리
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(position);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        // 데이터 목록의 전체 항목 수 반환
        return routines.size();
    }

    // 뷰 홀더 클래스: RecyclerView의 각 항목에 대한 뷰를 보유함
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routineName;
        CheckBox checkBox;

        // 생성자: 뷰 홀더의 뷰 초기화
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            routineName = itemView.findViewById(R.id.routineName);
            checkBox = itemView.findViewById(R.id.chk_routine);
        }
    }
}

