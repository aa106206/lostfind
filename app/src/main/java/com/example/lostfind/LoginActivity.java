package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lostfind.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> login());
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void login() {
        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // <<< 중요: 이메일 인증 여부 확인 >>>
//                            if (firebaseUser.isEmailVerified()) {
//                                // 이메일 인증 완료됨 -> 메인 화면으로 이동
//                                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent(this, MapsActivity.class); // 메인 액티비티로 이동
//                                startActivity(intent);
//                                finish();
//                            }
                            if (firebaseUser.isEmailVerified()) {

                                // ✅ 1️⃣ FCM 토큰 저장 (여기가 핵심)
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnSuccessListener(token -> {
                                            String uid = FirebaseAuth.getInstance().getUid();
                                            if (uid != null) {
                                                FirebaseDatabase.getInstance()
                                                        .getReference("users")
                                                        .child(uid)
                                                        .child("fcmToken")
                                                        .setValue(token);
                                            }
                                        });

                                // ✅ 2️⃣ 메인 화면 이동
                                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, MapsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            //여기까지 수정
                            else {
                                // 이메일 인증이 완료되지 않음
                                Toast.makeText(this, "이메일 인증을 완료해주세요.", Toast.LENGTH_LONG).show();
                                // 필요하다면 인증 메일 재전송 버튼 제공
                                // mAuth.signOut(); // 로그아웃 처리
                            }
                        }
                    } else {
                        // 로그인 실패
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



    }

}