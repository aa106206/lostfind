package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lostfind.databinding.MyinfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InfoActivity extends AppCompatActivity {

    private MyinfoBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = MyinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 시스템 바 여백 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 버튼 클릭 리스너 설정
        setupButtonClickListeners();

        // ActivityResultLauncher 로직은 이름 변경처럼 즉시 결과를 받아오는 경우에만
        // 유용하므로, 이메일/비밀번호 변경에서는 더 이상 사용하지 않습니다.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 핵심: 이 화면이 다시 포커스를 얻을 때마다 항상 최신 사용자 정보를 불러옵니다.
        // 이메일 인증 후 돌아왔을 때, 변경된 이메일이 이 로직을 통해 반영됩니다.
        loadLatestUserData();
    }

    /**
     * Firebase로부터 최신 사용자 정보를 가져와 화면을 갱신하는 메소드.
     */
    private void loadLatestUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            // 필요하다면 로그인 화면으로 이동하는 로직 추가
            // startActivity(new Intent(this, LoginActivity.class));
            // finish();
            return;
        }

        // 로딩 중임을 사용자에게 알리기 (선택 사항)
        binding.userName.setText("로딩 중...");
        binding.userEmail.setText("로딩 중...");

        // 중요: 서버로부터 최신 사용자 정보를 강제로 새로고침합니다.
        // 이메일 인증 상태나 변경된 이메일 주소를 반영하기 위해 필수적입니다.
        currentUser.reload().addOnCompleteListener(reloadTask -> {
            if (reloadTask.isSuccessful()) {
                // reload에 성공하면, mAuth 인스턴스는 최신 사용자 정보를 담게 됩니다.
                FirebaseUser freshUser = mAuth.getCurrentUser();
                if (freshUser != null) {
                    // 1. 최신 이메일 정보를 화면에 표시
                    binding.userEmail.setText(freshUser.getEmail());

                    // 2. 데이터베이스에서 최신 이름 정보를 가져와 화면에 표시
                    mDatabase.child("users").child(freshUser.getUid()).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String name = snapshot.getValue(String.class);
                                        binding.userName.setText(name);
                                    } else {
                                        binding.userName.setText("이름 정보 없음");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    binding.userName.setText("이름 로딩 실패");
                                    Log.w("InfoActivity", "DB에서 이름 로딩 실패", error.toException());
                                }
                            });
                }
            } else {
                // reload 실패 (예: 네트워크 문제, 사용자 세션 만료 등)
                Log.e("InfoActivity", "사용자 정보 reload 실패", reloadTask.getException());
                Toast.makeText(this, "세션이 만료되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                // 필요 시 로그인 화면으로 이동
            }
        });
    }

    /**
     * 모든 버튼의 클릭 이벤트를 설정하는 메소드.
     */
    private void setupButtonClickListeners() {
        binding.emailChangeButton.setOnClickListener(v -> {
            // 이제 Intent로 아무 정보도 넘겨주지 않습니다. UserEmailActivity가 직접 가져갑니다.
            startActivity(new Intent(InfoActivity.this, UserEmailActivity.class));
        });

        binding.pwChangeButton.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, UserPWActivity.class));
        });

        binding.nameChangeButton.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, UserNameActivity.class));
        });

        binding.imageButton.setOnClickListener(v -> finish()); // 뒤로가기

        binding.bookmarkButton.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, BookmarkActivity.class));
        });

        binding.pointshopButton.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, PointShopActivity.class));
        });
    }

    // 기존의 loadUserData 메소드는 loadLatestUserData로 통합되었으므로 삭제합니다.
    // 기존의 onResume 내부의 loadLatestUserData 호출은 중복이므로 삭제합니다.
}
