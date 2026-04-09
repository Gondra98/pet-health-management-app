package com.example.pet_care3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pet_care3.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    // FirebaseAuth 및 FirebaseFirestore 인스턴스 생성
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    // 레이아웃에서 사용되는 뷰들을 위한 변수 선언
    EditText etd_signin_id, etd_signin_pw, etd_signin_check_pw;
    Button btn_signin, btn_goto_login;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // 레이아웃에서 아이디 및 비밀번호 입력을 위한 EditText와 버튼을 찾아서 변수에 할당
        etd_signin_id = findViewById(R.id.etd_signin_id);
        etd_signin_pw = findViewById(R.id.etd_signin_pw);
        etd_signin_check_pw = findViewById(R.id.etd_signin_check_pw);
        btn_signin = findViewById(R.id.button_signin);
        btn_goto_login = findViewById(R.id.button_goto_login);

        // 회원가입 버튼 클릭 시 이벤트 처리
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText에서 아이디, 비밀번호 및 비밀번호 확인 값을 가져옴
                String id = etd_signin_id.getText().toString();
                String pw = etd_signin_pw.getText().toString();
                String check_pw = etd_signin_check_pw.getText().toString();

                // 입력된 아이디, 비밀번호 및 비밀번호 확인이 비어있지 않은지 확인
                if (id != null && !id.isEmpty() && pw != null && !pw.isEmpty() && check_pw != null && !check_pw.isEmpty()) {
                    // 비밀번호와 비밀번호 확인이 일치하는지 확인
                    if (pw.equals(check_pw)) {
                        // FirebaseAuth를 사용하여 사용자 생성
                        mAuth.createUserWithEmailAndPassword(id, pw)
                                .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // 사용자가 성공적으로 생성된 경우
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                String uid = user.getUid();
                                                // 사용자 정보를 HashMap에 저장
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put(FirebaseID.UID, uid);
                                                userMap.put(FirebaseID.id, id);
                                                userMap.put(FirebaseID.password, pw);
                                                // Firestore에 사용자 정보 저장
                                                mStore.collection("Users").document(uid).set(userMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    // 회원가입 성공 메시지 표시
                                                                    Toast.makeText(SigninActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                                                    // 로그인 액티비티로 이동
                                                                    Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    // Firestore에 사용자 정보 저장 실패 시 메시지 표시
                                                                    Toast.makeText(SigninActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            // 사용자 생성 실패 시 메시지 표시
                                            Toast.makeText(SigninActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        // 비밀번호와 비밀번호 확인이 일치하지 않을 경우 메시지 표시
                        Toast.makeText(SigninActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 아이디 또는 비밀번호가 입력되지 않은 경우 메시지 표시
                    Toast.makeText(SigninActivity.this, "아이디 또는 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
