package com.example.lostfind;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lostfind.databinding.ActivityUserPwBinding; // XML 파일 이름에 맞게 수정 (예: activity_user_pw.xml)
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserPWActivity extends AppCompatActivity {

    private ActivityUserPwBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserPwBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // InfoActivity에서 전달받은 현재 사용자의 이메일
        String currentUserEmail = getIntent().getStringExtra("currentUserEmail");

        binding.btnSaveName.setOnClickListener(v -> { // 저장 버튼 ID가 'saveButton'이라고 가정
            String currentPassword = binding.currentPasswordEditText.getText().toString(); // 현재 비밀번호 EditText
            String newPassword = binding.newPasswordEditText.getText().toString();       // 새 비밀번호 EditText
            String confirmPassword = binding.confirmPasswordEditText.getText().toString(); // 새 비밀번호 확인 EditText

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
            if (user == null || currentUserEmail == null) {
                Toast.makeText(this, "사용자 정보가 올바르지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Firebase 비밀번호 변경 핵심 로직 ---
            // 1. 재인증을 위한 자격 증명(Credential) 생성 (현재 이메일 + 현재 비밀번호)
            AuthCredential credential = EmailAuthProvider.getCredential(currentUserEmail, currentPassword);

            // 2. 사용자 재인증
            user.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            // 3. 재인증 성공 시, 새 비밀번호로 업데이트
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(UserPWActivity.this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                            setResult(RESULT_OK); // 성공했다는 결과만 전달
                                            finish();
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

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
