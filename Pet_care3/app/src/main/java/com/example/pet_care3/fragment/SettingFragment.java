package com.example.pet_care3.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pet_care3.LoadingActivity;
import com.example.pet_care3.LoginActivity;
import com.example.pet_care3.MainActivity;
import com.example.pet_care3.R;
import com.example.pet_care3.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import android.net.Uri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class SettingFragment extends Fragment {

    ImageView setting_PetImage;
    TextView setting_PetName, setting_PetBirthday, setting_PetSex, setting_PetWeight;

    TextView tv_logout, tv_reviseProfile, tv_withdrawUser;
    CheckBox setting_PetNeutered;
    ImageButton imageButtonImageSelect;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    String before_name;





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setting_PetImage = view.findViewById(R.id.setting_PetImage);
        setting_PetName = view.findViewById(R.id.setting_PetName);
        setting_PetBirthday = view.findViewById(R.id.setting_PetBirthday);
        setting_PetWeight = view.findViewById(R.id.setting_PetWeight);
        setting_PetSex = view.findViewById(R.id.setting_PetSex);
        setting_PetNeutered = view.findViewById(R.id.setting_PetNeutered);

        tv_logout = view.findViewById(R.id.logout);
        tv_reviseProfile = view.findViewById(R.id.reviseProfile);
        tv_withdrawUser = view.findViewById(R.id.withdrawUser);







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
                        // 반려동물 정보 가져오기
                        String petName = document.getString(FirebaseID.pet_name);
                        String petSex = document.getString(FirebaseID.pet_sex);
                        String petWeight = Long.toString(document.getLong(FirebaseID.pet_weight));
                        boolean petNeutered = document.getBoolean(FirebaseID.pet_neutered);
                        long petBirthTimestamp = document.getLong(FirebaseID.pet_birthday_timestamp);

                        before_name = petName;


                        // 반려동물 정보를 TextView에 설정
                        setting_PetName.setText(petName);
                        setting_PetSex.setText(petSex);
                        setting_PetWeight.setText(petWeight);
                        setting_PetNeutered.setChecked(petNeutered);

                        // 생년월일 형식 변경
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                        String formattedDate = dateFormat.format(new Date(petBirthTimestamp));
                        setting_PetBirthday.setText(formattedDate);

                        // 반려동물 이미지 파일 이름 가져오기
                        String petImageFilename = document.getString(FirebaseID.pet_image_filename);

                        // 반려동물 이미지 파일 이름을 사용하여 Firebase Storage에서 이미지 불러오기
                        StorageReference imageRef = petImagesRef.child(petImageFilename);
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // 이미지 가져오기 성공
                                Picasso.get().load(uri).into(setting_PetImage); // Picasso를 사용하여 이미지 로드
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



        tv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FirebaseAuth 인스턴스에서 로그아웃 처리
                FirebaseAuth.getInstance().signOut();

                // 로그인 화면으로 이동하거나 다른 처리를 수행할 수 있음
                // 예시: LoginActivity로 이동
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish(); // 현재 화면 종료
            }
        });



        tv_reviseProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog.Builder를 사용하여 대화상자 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // 대화상자에 표시될 제목 설정
                builder.setTitle("프로필 수정");
                // 대화상자에 표시될 내용 설정
                builder.setMessage("반려동물의 정보를 수정하세요.");

                // 대화상자에 추가할 레이아웃을 inflate
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_animal, null);
                // 대화상자에 레이아웃 설정
                builder.setView(dialogView);

                // EditText, RadioButton, CheckBox 등의 뷰 찾기
                EditText editTextName = dialogView.findViewById(R.id.editText_pet_name);
                EditText editTextBirthday = dialogView.findViewById(R.id.editText_pet_birthday);
                EditText editTextWeight = dialogView.findViewById(R.id.editText_pet_weight);
                RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
                RadioButton radioButtonMale = dialogView.findViewById(R.id.radioButtonMale);
                RadioButton radioButtonFemale = dialogView.findViewById(R.id.radioButtonFemale);
                CheckBox checkBoxNeutered = dialogView.findViewById(R.id.setting_PetNeutered);

                // 대화상자에서 이미지 버튼 참조 가져오기
                imageButtonImageSelect = dialogView.findViewById(R.id.imageButtonImageSelect);

                // 이미지 버튼에 클릭 리스너 추가
                imageButtonImageSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 갤러리로부터 이미지를 선택하는 인텐트 실행
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                    }
                });




                // 사용자의 UID 가져오기
                String UID = mAuth.getCurrentUser().getUid();

                // 사용자 문서 참조
                DocumentReference userRef = db.collection("Users").document(UID);

                // Pet 컬렉션 참조
                CollectionReference petRef = userRef.collection("Pet");

                // Pet 문서 가져오기
                petRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Pet 컬렉션에 문서가 하나 이상 있는 경우
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                // 반려동물 정보 가져오기
                                String petName = document.getString(FirebaseID.pet_name);
                                String petBirthday = document.getString(FirebaseID.pet_birthday);
                                String petSex = document.getString(FirebaseID.pet_sex);
                                long petWeight = document.getLong(FirebaseID.pet_weight);
                                boolean petNeutered = document.getBoolean(FirebaseID.pet_neutered);
                                // 반려동물 문서의 ID를 이용하여 이미지 파일 이름을 가져옵니다.
                                String petImageFilename = document.getString(FirebaseID.pet_image_filename);

                                // Firebase Storage에서 이미지를 가져오는 참조 설정
                                StorageReference imageRef = petImagesRef.child(petImageFilename);

                                // 반려동물 이미지 파일 이름을 사용하여 Firebase Storage에서 이미지 불러오기
                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // 이미지 가져오기 성공
                                        Picasso.get().load(uri).into(imageButtonImageSelect); // Picasso를 사용하여 이미지 로드
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // 이미지 가져오기 실패
                                        Log.e("TAG", "이미지 가져오기 실패: " + e.getMessage());
                                    }
                                });

                                // 반려동물 정보를 EditText, RadioButton, CheckBox 등에 설정
                                editTextName.setText(petName);
                                editTextBirthday.setText(petBirthday);
                                editTextWeight.setText(String.valueOf(petWeight));
                                if (petSex.equals("남")) {
                                    radioButtonMale.setChecked(true);
                                } else {
                                    radioButtonFemale.setChecked(true);
                                }
                                checkBoxNeutered.setChecked(petNeutered);
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







                // EditText를 클릭했을 때 달력 다이얼로그가 나타나도록 설정
                editTextBirthday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 현재 날짜 가져오기
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                        // DatePickerDialog 생성 및 설정
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // 사용자가 선택한 날짜를 가져와서 EditText에 표시
                                String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDayOfMonth;
                                editTextBirthday.setText(selectedDate);
                            }
                        }, year, month, dayOfMonth);

                        // DatePickerDialog 보이기
                        datePickerDialog.show();
                    }
                });



                // 확인 버튼 설정
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 입력한 정보 가져오기
                        String name = editTextName.getText().toString().trim();
                        String birthday = editTextBirthday.getText().toString().trim();
                        double weight = Double.parseDouble(editTextWeight.getText().toString().trim());
                        boolean Neutered = checkBoxNeutered.isChecked();
                        String sex = radioButtonMale.isChecked() ? "남" : "여";
                        String petImageFilename = UUID.randomUUID().toString();







                        // 여기서 가져온 데이터를 사용하여 Firestore의 Pet 컬렉션의 필드 데이터에 적용
                        // 사용자 문서 참조
                        DocumentReference userRef = db.collection("Users").document(UID);

                        // Pet 컬렉션 참조
                        CollectionReference petRef = userRef.collection("Pet");

                        // 기존 반려동물 문서 삭제
                        petRef.document(before_name).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // 기존 문서 삭제 성공 후 새 문서 생성
                                        // 생일 문자열을 Date 객체로 변환하여 milliseconds로 변경
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        Date birthdayDate = null;
                                        try {
                                            birthdayDate = dateFormat.parse(birthday);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        long birthdayTimestamp = birthdayDate.getTime();

                                        // 새 반려동물 문서 생성
                                        petRef.document(name).set(new HashMap<String, Object>() {{
                                                    put(FirebaseID.pet_name, name);
                                                    put(FirebaseID.pet_birthday, birthday);
                                                    put(FirebaseID.pet_weight, weight);
                                                    put(FirebaseID.pet_sex, sex);
                                                    put(FirebaseID.pet_neutered, Neutered);
                                                    put(FirebaseID.pet_birthday_timestamp, birthdayTimestamp);
                                                    put(FirebaseID.pet_image_filename, petImageFilename);
                                                    // 기타 필요한 필드 추가
                                                }})
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // 새 문서 생성 성공
                                                        Toast.makeText(getActivity(), "반려동물 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();

                                                        // 이미지를 Firebase 스토리지에 업로드
                                                        uploadImageToStorage(selectedImageUri, UID, petImageFilename);


                                                        Intent intent = new Intent(getActivity(), LoadingActivity.class);
                                                        intent.putExtra("selectedItemId", R.id.setting); // 4번 메뉴인 setting의 아이디 전달
                                                        startActivity(intent);
                                                        getActivity().finish(); // 현재 액티비티 종료



                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // 새 문서 생성 실패
                                                        Toast.makeText(getActivity(), "반려동물 정보 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                                        Log.e("TAG", "Error creating document", e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // 기존 문서 삭제 실패
                                        Toast.makeText(getActivity(), "반려동물 정보 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        Log.e("TAG", "Error deleting document", e);
                                    }
                                });
                    }
                });




                // 취소 버튼 설정
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 취소 버튼을 눌렀을 때의 동작 구현
                        // 아무 동작 없음
                    }
                });

                // 대화상자 표시
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        tv_withdrawUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 확인 대화상자 표시
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("회원 탈퇴");
                builder.setMessage("정말로 회원을 탈퇴하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Firebase Authentication에서 현재 사용자 삭제
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // 사용자 삭제 성공
                                                // Firestore에서 사용자 관련 데이터 삭제
                                                deleteUserData();
                                            } else {
                                                // 사용자 삭제 실패
                                                Toast.makeText(getActivity(), "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 취소 버튼 클릭 시 아무 작업도 수행하지 않음
                    }
                });
                builder.show();
            }
        });

        return view;
    }



    // 이미지를 Firebase 스토리지에 업로드하는 메서드 정의
    private void uploadImageToStorage(Uri imageUri, String UID, String filename) {
        // 이미지를 업로드할 경로 설정
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference petImagesRef = storageRef.child("images").child(UID).child(filename);

        // 이미지 업로드
        petImagesRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // 이미지 업로드 성공
                        Log.d("TAG", "Image uploaded successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 이미지 업로드 실패
                        Log.e("TAG", "Error uploading image", e);
                    }
                });
    }


    // 사용자가 갤러리에서 이미지를 선택한 경우를 처리하는 부분
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // 선택한 이미지를 대화상자에 있는 이미지 버튼에 설정하기
            imageButtonImageSelect.setImageURI(imageUri);
            // 선택한 이미지 Uri 저장
            selectedImageUri = imageUri;
        }
    }


    // 회원 탈퇴 메서드
    private void deleteUserData() {
        String UID = mAuth.getCurrentUser().getUid();
        // Firebase Storage에서 사용자 이미지 데이터 삭제 후 Firestore에서 사용자 데이터 삭제
        deleteImageFromStorageThenDeleteUserData(UID);
    }

    // Firebase Storage에서 사용자 이미지 데이터 삭제 후 Firestore에서 사용자 데이터 삭제하는 메서드
    private void deleteImageFromStorageThenDeleteUserData(String UID) {
        // 이미지가 저장된 경로 설정
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("images").child(UID);
        // 해당 경로에 있는 이미지 데이터 모두 삭제
        imagesRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 이미지 데이터 삭제 성공
                        Log.d("TAG", "이미지 데이터 삭제 성공");
                        // Firestore에서 사용자 데이터 삭제
                        deleteUserDataFromFirestore(UID);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 이미지 데이터 삭제 실패
                        Log.e("TAG", "이미지 데이터 삭제 실패: " + e.getMessage());
                        // 실패한 경우에도 Firestore에서 사용자 데이터 삭제 시도
                        deleteUserDataFromFirestore(UID);
                    }
                });
    }

    // Firestore에서 사용자 데이터 삭제하는 메서드
    private void deleteUserDataFromFirestore(String UID) {
        // 사용자 문서 참조
        DocumentReference userRef = db.collection("Users").document(UID);
        // 사용자 문서 삭제
        userRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 사용자 데이터 삭제 성공
                        Toast.makeText(getActivity(), "회원 탈퇴되었습니다.", Toast.LENGTH_SHORT).show();
                        // Firestore에서 사용자 데이터 삭제가 완료된 후에 계정 삭제
                        deleteAccount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 사용자 데이터 삭제 실패
                        Toast.makeText(getActivity(), "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 계정 삭제 메서드
    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // 계정 삭제 성공
                                // 여기에서 필요한 추가 작업 수행
                            } else {
                                // 계정 삭제 실패
                                Toast.makeText(getActivity(), "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }






}