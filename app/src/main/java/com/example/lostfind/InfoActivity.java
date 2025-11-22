package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lostfind.databinding.MyinfoBinding; // myinfo.xml에 대한 ViewBinding 클래스
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InfoActivity extends AppCompatActivity {

    private MyinfoBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. ViewBinding 초기화 (myinfo.xml과 연결)
        binding = MyinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 시스템 바 여백 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 3. 현재 로그인된 사용자 정보 로드
        if (currentUser != null) {
            // 로그인 정보가 있으면 Realtime Database에서 추가 정보를 가져와 화면에 표시
            loadUserData(currentUser);
        } else {
            // 로그인 정보가 없으면 로그 출력 및 로그인 화면으로 이동 (필요 시)
            Log.w("InfoActivity", "사용자가 로그인되어 있지 않아, 로그인 화면으로 이동합니다.");
            // startActivity(new Intent(InfoActivity.this, LoginActivity.class));
            // finish();
        }

        // 4. 다른 액티비티에서 보낸 결과를 처리하는 콜백 설정
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 작업이 성공했고(RESULT_OK), 데이터가 담겨있는지 확인
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // UserEmailActivity에서 보낸 "newEmail" 데이터가 있다면
                        if (data.hasExtra("newEmail")) {
                            String updatedEmail = data.getStringExtra("newEmail");
                            // InfoActivity의 userEmail TextView를 즉시 업데이트
                            binding.userEmail.setText(updatedEmail);
                        }

                        // UserNameActivity에서 보낸 "newName" 데이터가 있다면
                        if (data.hasExtra("newName")) {
                            String updatedName = data.getStringExtra("newName");
                            binding.userName.setText(updatedName);
                        }
                    }
                });

        // 5. 각 버튼 클릭 리스너 설정


        binding.emailChangeButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, UserEmailActivity.class);
            String currentEmail = binding.userEmail.getText().toString();
            if (!currentEmail.isEmpty()) {
                intent.putExtra("currentUserEmail", currentEmail);
                activityResultLauncher.launch(intent);
            } else {
                Toast.makeText(this, "사용자 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });


        binding.pwChangeButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, UserPWActivity.class);
            String currentEmail = binding.userEmail.getText().toString();
            // 비밀번호 변경 시 재인증에 현재 이메일이 필요합니다.
            if (!currentEmail.isEmpty()) {
                intent.putExtra("currentUserEmail", currentEmail);
                activityResultLauncher.launch(intent);
            } else {
                Toast.makeText(this, "사용자 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });


        // 다른 버튼들에 대한 리스너도 여기에 추가합니다...
        binding.nameChangeButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, UserNameActivity.class);
            intent.putExtra("currentName", binding.userName.getText().toString());
            activityResultLauncher.launch(intent);
        });

        binding.imageButton.setOnClickListener(v -> finish()); // 뒤로가기 버튼

        binding.bookmarkButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, BookmarkActivity.class);
            startActivity(intent);
        });

        binding.pointshopButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, PointShopActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 로그인된 사용자의 정보를 가져와 화면(TextView)에 표시하는 메소드
     * @param currentUser 현재 로그인된 FirebaseUser 객체
     */
    private void loadUserData(FirebaseUser currentUser) {
        // 우선, Firebase Auth에서 직접 이메일을 가져와 표시 (가장 정확)
        String email = currentUser.getEmail();
        binding.userEmail.setText(email);

        // 추가로 Realtime Database에 저장된 '이름' 같은 정보를 가져옴
        mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 데이터베이스에서 'name' 필드의 값을 가져옴
                    String name = dataSnapshot.child("name").getValue(String.class);
                    binding.userName.setText(name);

                    // 만약 데이터베이스의 이메일도 Auth 정보와 동기화하고 싶다면 여기서 한번 더 설정할 수 있습니다.
                    // String dbEmail = dataSnapshot.child("email").getValue(String.class);
                    // binding.userEmail.setText(dbEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("InfoActivity", "loadUserData:onCancelled", databaseError.toException());
            }
        });
    }
}
