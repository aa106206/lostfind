package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lostfind.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        binding.signupButton.setOnClickListener(v -> signup());

        binding.backButton.setOnClickListener(v -> {
            Intent intent=new Intent(this,StartActivity.class);
            startActivity(intent);
        });
    }


    private void signup() {
        String name = binding.signupName.getText().toString().trim();
        String email = binding.signupEmail.getText().toString().trim();
        String password1 = binding.signupPassword1.getText().toString().trim();
        String password2 = binding.signupPassword2.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password1.equals(password2)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password1.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password1)
                .addOnCompleteListener(this, (Task<AuthResult> task) -> {

                    if (task.isSuccessful()) {


                        String uid = mAuth.getCurrentUser().getUid();

                        User user = new User(uid, name, email);

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();

                                    // ④ 회원가입 후 이동
                                    startActivity(new Intent(this, StartActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "회원 DB 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    } else {
                        // 회원가입 실패
                        Toast.makeText(this, "회원가입 실패: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
