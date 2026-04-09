package com.example.pet_care3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pet_care3.MainActivity;
import com.example.pet_care3.R;
import com.example.pet_care3.SigninActivity;
import com.example.pet_care3.SubmitAnimalActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    // FirebaseAuth 인스턴스를 가져옵니다.
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // 레이아웃에서 사용되는 위젯 변수들을 선언합니다.
    EditText etd_login_id, etd_login_pw;
    Button btn_login, btn_goto_signin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // XML 파일에서 정의한 위젯들과 연결합니다.
        etd_login_id = findViewById(R.id.etd_logid_id);
        etd_login_pw = findViewById(R.id.etd_login_pw);
        btn_login = findViewById(R.id.button_login);
        btn_goto_signin = findViewById(R.id.button_goto_signin);

        // 회원가입 화면으로 이동하는 버튼에 클릭 이벤트를 설정합니다.
        btn_goto_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
                startActivity(intent);
            }
        });

        // 로그인 버튼에 클릭 이벤트를 설정합니다.
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 입력된 이메일과 비밀번호를 가져옵니다.
                String id = etd_login_id.getText().toString();
                String pw = etd_login_pw.getText().toString();

                // FirebaseAuth를 사용하여 이메일과 비밀번호로 로그인을 시도합니다.
                mAuth.signInWithEmailAndPassword(id, pw)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // 로그인 작업이 성공적으로 완료되었는지 확인합니다.
                                if (task.isSuccessful()) {
                                    // 로그인이 성공하면 반려동물 등록 여부를 확인합니다.
                                    checkPetRegistration();
                                } else {
                                    // 로그인이 실패하면 사용자에게 오류 메시지를 표시합니다.
                                    Toast.makeText(LoginActivity.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // 반려동물 등록 여부를 확인하는 메소드입니다.
    private void checkPetRegistration() {
        // 현재 로그인된 사용자의 UID를 가져옵니다.
        String UID = mAuth.getCurrentUser().getUid();

        // FirebaseFirestore 인스턴스를 가져옵니다.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // "Users" 컬렉션에서 현재 사용자의 UID를 가진 문서의 "Pet" 하위 컬렉션을 가져옵니다.
        db.collection("Users").document(UID).collection("Pet").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // 작업이 성공적으로 완료되었는지 확인합니다.
                if (task.isSuccessful()) {
                    // 쿼리 스냅샷을 가져옵니다.
                    QuerySnapshot querySnapshot = task.getResult();
                    // 가져온 쿼리 스냅샷이 비어있지 않은지 확인합니다.
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // PET 컬렉션에 정보가 있는 경우, MainActivity로 이동합니다.
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // PET 컬렉션에 정보가 없는 경우, SubmitAnimalActivity로 이동합니다.
                        Intent intent = new Intent(LoginActivity.this, SubmitAnimalActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // 파이어스토어에서 데이터를 가져오는 도중 오류가 발생한 경우 사용자에게 오류 메시지를 표시합니다.
                    Toast.makeText(LoginActivity.this, "파이어스토어 오류", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
