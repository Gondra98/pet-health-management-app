package com.example.pet_care3.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pet_care3.R;
import com.example.pet_care3.adapter.CheckListAdapter;
import com.example.pet_care3.utility.FirebaseID;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import android.util.Log;


public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    CheckListAdapter adapter;
    ArrayList<String> routines;

    FloatingActionButton checklistAdd_Button;

    TextView petNameTextView, petSexTextView, petAgeTextView;
    ImageView petImageView;


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 화면 레이아웃을 인플레이트하여 View 객체 생성
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        petNameTextView = view.findViewById(R.id.home_petname);
        petSexTextView = view.findViewById(R.id.home_petsex);
        petAgeTextView = view.findViewById(R.id.home_petage);

        petImageView = view.findViewById(R.id.home_petimage);

        // 사용자 UID 가져오기
        String UID = mAuth.getCurrentUser().getUid();

        // 사용자 문서 참조
        DocumentReference userRef = db.collection("Users").document(UID);

        // Pet 컬렉션 참조
        CollectionReference petRef = userRef.collection("Pet");


        // 사용자의 UID와 일치하는 폴더 내 'pet' 하위 컬렉션에 접근
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference petImagesRef = storageRef.child("images").child(UID);


        // 반려동물 데이터 가져오기
        petRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Pet 컬렉션에 문서가 하나 이상 있는 경우
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        // 반려동물 이름, 성별, 출생 타임스탬프 가져오기
                        String petName = document.getString(FirebaseID.pet_name);
                        String petSex = document.getString(FirebaseID.pet_sex);
                        long petBirthTimestamp = document.getLong(FirebaseID.pet_birthday_timestamp);

                        // 나이 계산
                        long currentTimestamp = System.currentTimeMillis();
                        long ageInMillis = currentTimestamp - petBirthTimestamp;
                        long ageInYears = ageInMillis / (1000L * 60 * 60 * 24 * 365);

                        // 가져온 데이터를 TextView에 설정
                        petNameTextView.setText(petName);
                        petSexTextView.setText(petSex);
                        petAgeTextView.setText(String.valueOf(ageInYears) + "살"); // 나이 출력

                        // 반려동물의 이미지 파일 이름 가져오기
                        String petImageFilename = document.getString(FirebaseID.pet_image_filename);



                        // 반려동물 이미지 파일 이름을 사용하여 Firebase Storage에서 이미지 불러오기
                        StorageReference imageRef = petImagesRef.child(petImageFilename);
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // 이미지 가져오기 성공
                                Picasso.get().load(uri).into(petImageView); // Picasso를 사용하여 이미지 로드
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 이미지 가져오기 실패
                                Log.e("TAG", "이미지 가져오기 실패: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    // Pet 컬렉션에 문서가 없는 경우
                    Toast.makeText(getActivity(), "반려동물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 실패한 경우
                Toast.makeText(getActivity(), "반려동물 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                Log.d("TAG", e.toString());
            }
        });




        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView);
        // RecyclerView에 LinearLayoutManager 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 데이터 목록 생성
        routines = new ArrayList<>();
        routines.add("양치");
        routines.add("운동");
        routines.add("독서");

        // 어댑터 생성 및 RecyclerView에 설정
        adapter = new CheckListAdapter(routines);

        // 아이템 롱클릭 리스너 설정
        adapter.setOnItemLongClickListener(new CheckListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                showLongClickDialog(position);
            }
        });

        recyclerView.setAdapter(adapter);

        // 플로팅 액션 버튼 초기화
        checklistAdd_Button = view.findViewById(R.id.checklistAdd_Button);

        // 플로팅 액션 버튼 클릭 리스너 설정
        checklistAdd_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        return view;
    }






    // 새로운 항목 추가 대화상자 표시 메서드
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("새로운 항목 추가");

        // 대화상자에 EditText 추가
        final EditText input = new EditText(getActivity());
        builder.setView(input);

        // 확인 버튼 설정
        builder.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newItem = input.getText().toString().trim();
                if (!newItem.isEmpty()) {
                    routines.add(newItem);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), "항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 취소 버튼 설정
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // 대화 상자 표시
        builder.show();
    }

    // 항목 삭제 대화상자 표시 메서드
    private void showLongClickDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("항목 삭제");
        builder.setMessage("정말로 삭제하시겠습니까?");

        // 확인 버튼 설정
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                routines.remove(position);
                adapter.notifyItemRemoved(position);
                dialog.dismiss();
            }
        });

        // 취소 버튼 설정
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // 대화 상자 표시
        builder.show();
    }
}
