package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lostfind.databinding.NameChangeBinding; // NameChangeBinding 사용 확인
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserNameActivity extends AppCompatActivity {
    private NameChangeBinding binding;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private String currentName; // 현재 이름을 저장할 멤버 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = NameChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 시스템 바 UI 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase 인스턴스 초기화
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- 핵심 수정 사항: DB에서 직접 현재 이름을 불러오기 ---
        loadAndDisplayCurrentName();

        // 저장 버튼 클릭 리스너
        binding.btnSaveName.setOnClickListener(view -> {
            String newName = binding.editNewName.getText().toString().trim();

            if (newName.isEmpty()) {
                binding.editNewName.setError("이름을 입력해주세요.");
                return;
            }

            // 현재 이름과 동일한 경우 (멤버 변수와 비교)
            if (newName.equals(currentName)) {
                Toast.makeText(UserNameActivity.this, "현재 이름과 동일합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase DB의 이름 업데이트
            updateNameToDatabase(newName);
        });

        // 뒤로가기 버튼 클릭 리스너
        binding.btnBack.setOnClickListener(view -> finish());
    }

    /**
     * Firebase DB에서 현재 사용자 이름을 가져와 화면에 표시하는 메소드
     */
    private void loadAndDisplayCurrentName() {
        binding.name.setText("현재 이름 : 로딩 중..."); // 사용자에게 로딩 중임을 알림
        mDatabase.child("users").child(currentUser.getUid()).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentName = snapshot.getValue(String.class); // 멤버 변수에 저장
                            binding.name.setText("현재 이름 : " + currentName);
                        } else {
                            binding.name.setText("현재 이름 : (정보 없음)");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.name.setText("현재 이름 : (로딩 실패)");
                        Toast.makeText(UserNameActivity.this, "이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 새로운 이름을 Firebase DB에 저장하는 메소드
     * @param newName 새로 변경할 이름
     */
    private void updateNameToDatabase(String newName) {
        mDatabase.child("users").child(currentUser.getUid()).child("name").setValue(newName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserNameActivity.this, "이름이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        // InfoActivity의 onResume()에서 최신 정보를 로드하므로
                        // 별도의 결과 전달 없이 액티비티를 종료합니다.
                        finish();
                    } else {
                        Toast.makeText(UserNameActivity.this, "이름 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
