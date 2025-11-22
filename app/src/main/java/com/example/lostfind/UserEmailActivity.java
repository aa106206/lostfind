package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// ViewBinding, Firebase 관련 클래스 import
import com.example.lostfind.databinding.ActivityUserEmailBinding; // XML 파일에 맞는 바인딩 클래스
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserEmailActivity extends AppCompatActivity {

    private ActivityUserEmailBinding binding; // XML 파일 이름이 activity_user_email.xml 이라고 가정
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // InfoActivity로부터 현재 이메일 주소를 받아옴
        String currentEmail = getIntent().getStringExtra("currentUserEmail");
        if (currentEmail == null) {
            Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 저장 버튼 클릭 이벤트
        binding.btnSaveName.setOnClickListener(v -> {
            String password = binding.currentPasswordEditText.getText().toString();
            String newEmail = binding.editNewName.getText().toString().trim();

            // 입력값 유효성 검사
            if (password.isEmpty()) {
                binding.currentPasswordEditText.setError("현재 비밀번호를 입력해주세요.");
                return;
            }
            if (newEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                binding.editNewName.setError("올바른 이메일 형식을 입력해주세요.");
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            // --- 재인증 및 이메일 변경 로직 ---
            // 1. 재인증을 위한 자격 증명(Credential) 생성 (현재 이메일 + 입력받은 비밀번호)
            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);

            // 2. 사용자 재인증
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    // 3. 재인증 성공 시, 이메일 변경 절차 진행
                    user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // 4. Realtime Database의 이메일도 함께 변경
                            mDatabase.child("users").child(user.getUid()).child("email").setValue(newEmail)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(UserEmailActivity.this, "확인 메일을 보냈습니다. 새 이메일을 확인해주세요.", Toast.LENGTH_LONG).show();

                                            // 5. InfoActivity로 변경된 이메일 결과 전달
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("newEmail", newEmail);
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        }
                                    });
                        } else {
                            // 이메일 변경 요청 실패 (예: 이미 사용 중인 이메일)
                            Toast.makeText(UserEmailActivity.this, "이메일 변경 요청 실패: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // 재인증 실패 (대부분 비밀번호가 틀린 경우)
                    Toast.makeText(UserEmailActivity.this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
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
