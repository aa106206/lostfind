package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.lostfind.databinding.MyinfoBinding;

public class InfoActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        MyinfoBinding binding = MyinfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 결과가 OK이고, 데이터가 null이 아닌지 확인
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        // --- UserNameActivity에서 온 결과 처리 ---
                        // "newName" 키가 있는지 확인
                        if (result.getData().hasExtra("newName")) {
                            // Intent에서 "newName" 값을 꺼냄
                            String updatedName = result.getData().getStringExtra("newName");
                            // InfoActivity의 userName TextView를 업데이트
                            binding.userName.setText(updatedName);
                        }

                    }
                });

        binding.nameChangeButton.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, UserNameActivity.class);
            // 현재 화면에 표시된 이름을 Intent에 "currentName"이라는 키로 담아서 전달
            String currentName = binding.userName.getText().toString();
            intent.putExtra("currentName", currentName);
            activityResultLauncher.launch(intent);
        });

        binding.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.emailChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, UserEmailActivity.class));
            }
        });

        binding.idChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, UserIDActivity.class));
            }
        });

        binding.pwChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, UserPWActivity.class));
            }
        });

        binding.bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, BookmarkActivity.class));
            }
        });

        binding.pointshopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, PointShopActivity.class));
            }
        });

    }
}