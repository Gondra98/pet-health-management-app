package com.example.pet_care3;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pet_care3.utility.FirebaseID;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SubmitAnimalActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1; // 카메라로 사진 촬영 요청 코드
    private static final int REQUEST_IMAGE_GALLERY = 2; // 갤러리에서 이미지 선택 요청 코드
    private ImageButton imageButtonImageSelect; // 이미지 선택 버튼
    private EditText editTextPetBirthday, editTextPetName, editTextPetWeight; // 애완동물 생일, 이름, 몸무게 입력 필드
    private CheckBox checkBoxPetNeutered; // 중성화 여부 체크박스
    private RadioGroup radioGroupPetSex; // 애완동물 성별 라디오 그룹
    private String petImageFileName; // 애완동물 이미지 파일명

    private FirebaseFirestore db; // Firestore 데이터베이스 인스턴스
    private FirebaseStorage mStorage = FirebaseStorage.getInstance(); // Firebase Storage 인스턴스
    private Uri selectedImageUri; // 선택된 이미지의 Uri

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_animal);

        // 뷰 초기화
        editTextPetName = findViewById(R.id.editText_pet_name);
        editTextPetWeight = findViewById(R.id.editText_animal_weight);
        editTextPetBirthday = findViewById(R.id.editText_animal_birthday);
        checkBoxPetNeutered = findViewById(R.id.setting_PetNeutered);
        radioGroupPetSex = findViewById(R.id.radioGroup);

        imageButtonImageSelect = findViewById(R.id.imageButtonImageSelect);
        imageButtonImageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchGalleryIntent();
            }
        });

        editTextPetBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button buttonSubmit = findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImageToFirebase(selectedImageUri);
            }
        });
    }

    // DatePickerDialog를 통해 날짜를 선택하는 메서드
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        editTextPetBirthday.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDayOfMonth);
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    // 갤러리에서 이미지를 선택하는 메서드
    private void dispatchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }


    // 갤러리에서 선택된 이미지 처리하는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageButtonImageSelect.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Firebase에 이미지 업로드하는 메서드
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String folderPath = "images/" + userId + "/";
        petImageFileName = UUID.randomUUID().toString();
        String fileUrl = folderPath + petImageFileName;
        StorageReference imageRef = storageRef.child(fileUrl);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                submitAnimalInfo();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SubmitAnimalActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 생일을 타임스탬프로 변환하는 메서드
    private long convertBirthdateToTimestamp(String birthdate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(birthdate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 애완동물 정보를 Firebase에 제출하는 메서드
    private void submitAnimalInfo() {
        String petName = editTextPetName.getText().toString();
        String petBirthday = editTextPetBirthday.getText().toString();
        double petWeight = Double.parseDouble(editTextPetWeight.getText().toString());
        boolean petNeutered = checkBoxPetNeutered.isChecked();
        int selectedRadioButtonId = radioGroupPetSex.getCheckedRadioButtonId();
        View selectedRadioButton = radioGroupPetSex.findViewById(selectedRadioButtonId);
        String petSex = "";
        if (selectedRadioButton != null && selectedRadioButton instanceof RadioButton) {
            petSex = ((RadioButton) selectedRadioButton).getText().toString();
        }

        long pet_birth_timestamp = convertBirthdateToTimestamp(petBirthday);

        Map<String, Object> petInfo = new HashMap<>();
        petInfo.put(FirebaseID.pet_name, petName);
        petInfo.put(FirebaseID.pet_sex, petSex);
        petInfo.put(FirebaseID.pet_birthday, petBirthday);
        petInfo.put(FirebaseID.pet_weight, petWeight);
        petInfo.put(FirebaseID.pet_neutered, petNeutered);
        petInfo.put(FirebaseID.pet_image_filename, petImageFileName);
        petInfo.put(FirebaseID.pet_birthday_timestamp, pet_birth_timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("Users").document(userId).collection("Pet").document(petName).set(petInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(SubmitAnimalActivity.this, "애완동물 정보가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SubmitAnimalActivity.this, LoadingActivity.class);
                            startActivity(intent); // 다음 화면으로 이동


                            finish();   // 현재 액티비티 종료
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SubmitAnimalActivity.this, "애완동물 정보 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(SubmitAnimalActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}

