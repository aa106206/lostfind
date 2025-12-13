package com.example.lostfind;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfind.databinding.ActivityUserPwBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserPWActivity extends AppCompatActivity {

    private ActivityUserPwBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserPwBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // 1. 현재 로그인된 사용자가 있는지 먼저 확인
        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnSaveName.setOnClickListener(v -> {
            String currentPassword = binding.currentPasswordEditText.getText().toString();
            String newPassword = binding.newPasswordEditText.getText().toString();
            String confirmPassword = binding.confirmPasswordEditText.getText().toString();

            // 입력 값 검증
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "새 비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- 핵심 수정 사항: FirebaseUser 객체에서 직접 이메일 가져오기 ---
            String userEmail = currentUser.getEmail();
            if (userEmail == null) {
                Toast.makeText(this, "사용자 이메일 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Firebase 비밀번호 변경 핵심 로직 ---
            // 1. 재인증을 위한 자격 증명(Credential) 생성 (가져온 이메일 + 현재 비밀번호)
            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);

            // 2. 사용자 재인증
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            // 3. 재인증 성공 시, 새 비밀번호로 업데이트
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(UserPWActivity.this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                            finish(); // 성공 시 액티비티 종료
                                        } else {
                                            Toast.makeText(UserPWActivity.this, "비밀번호 변경 실패: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // 재인증 실패 (대부분 현재 비밀번호가 틀린 경우)
                            Toast.makeText(UserPWActivity.this, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        binding.btnBack.setOnClickListener(view -> finish());
    }
}
