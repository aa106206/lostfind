package com.example.lostfind;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.lostfind.databinding.NameChangeBinding;

public class UserNameActivity extends AppCompatActivity {
    private NameChangeBinding binding;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = NameChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        // 2. Intent에서 "currentName" 키로 저장된 현재 이름을 꺼냄
        String currentName = intent.getStringExtra("currentName");

        // 3. 이름이 정상적으로 전달되었다면 TextView에 텍스트 설정
        if (currentName != null && !currentName.isEmpty()) {
            binding.name.setText("현재 이름 : " + currentName);
        } else {
            binding.name.setText("현재 이름 : (알 수 없음)");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnSaveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = binding.editNewName.getText().toString().trim();

                if (newName.isEmpty()) {
                    binding.editNewName.setError("이름을 입력해주세요.");
                    return;
                }

                // 현재 이름과 동일한 경우
                if (newName.equals(currentName)) {
                    Toast.makeText(UserNameActivity.this, "현재 이름과 동일합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (currentUser != null) {
                    // Firebase Realtime Database의 "users" -> "UID" -> "name" 경로의 값을 업데이트
                    mDatabase.child("users").child(currentUser.getUid()).child("name").setValue(newName)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UserNameActivity.this, "이름이 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();

                                    // --- 결과 전달을 위한 코드 ---
                                    // 1. 결과를 담을 Intent 생성
                                    Intent resultIntent = new Intent();
                                    // 2. "newName"이라는 키로 수정된 이름을 Intent에 담기
                                    resultIntent.putExtra("newName", newName);
                                    // 3. 결과 코드와 데이터를 담은 Intent를 설정
                                    setResult(RESULT_OK, resultIntent);
                                    // 4. 현재 액티비티 종료
                                    finish();

                                } else {
                                    Toast.makeText(UserNameActivity.this, "이름 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}