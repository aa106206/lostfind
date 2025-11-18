package com.example.lostfind;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostWriteActivity extends AppCompatActivity {

    private EditText postTitleDetail;
    private EditText lostItemName;
    private EditText postDescription;
    private CheckBox checkboxIsFound;
    private EditText lostItemLocation;
    private FrameLayout mapContainer;
    private Button submitPostButton;
    private View locationTextView;
    private View mapLocationTextView;

    // Firebase 관련 변수 선언
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_write);

        // Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // 로그인되지 않은 사용자는 글을 쓸 수 없음. 로그인 화면으로 보낼 수도 있음.
            Toast.makeText(this, "글을 작성하려면 로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish(); // 현재 액티비티 종료
            return;
        }
        currentUserId = currentUser.getUid(); // 현재 로그인한 사용자의 UID 가져오기

        // XML의 뷰(View)들과 자바 변수 연결
        initializeViews();

        // '습득' 체크박스 상태에 따라 위치 입력란 보이기/숨기기
        checkboxIsFound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            lostItemLocation.setVisibility(visibility);
            mapContainer.setVisibility(visibility);
            if (locationTextView != null) locationTextView.setVisibility(visibility);
            if (mapLocationTextView != null) mapLocationTextView.setVisibility(visibility);
        });

        // '작성 완료' 버튼 클릭 이벤트 처리
        submitPostButton.setOnClickListener(v -> {
            uploadPost(); // 게시물 업로드 함수 호출
        });
    }

    private void initializeViews() {
        postTitleDetail = findViewById(R.id.post_title_detail);
        lostItemName = findViewById(R.id.lost_item_name);
        postDescription = findViewById(R.id.post_description);
        checkboxIsFound = findViewById(R.id.checkbox_is_found);
        lostItemLocation = findViewById(R.id.lost_item_location);
        mapContainer = findViewById(R.id.map_container);
        submitPostButton = findViewById(R.id.submit_post_button);
        locationTextView = findViewById(R.id.location_text_view);
        mapLocationTextView = findViewById(R.id.map_location_text_view);
    }

    private void uploadPost() {
        // 1. 사용자가 입력한 텍스트 가져오기
        String title = postTitleDetail.getText().toString().trim();
        String itemName = lostItemName.getText().toString().trim();
        String description = postDescription.getText().toString().trim();
        String location = lostItemLocation.getText().toString().trim();
        boolean isFound = checkboxIsFound.isChecked();

        // 2. ★★★ 필수 입력 항목 유효성 검사 (이 부분이 추가되었습니다) ★★★
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(itemName) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "제목, 분실물 이름, 상세 설명을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return; // 함수를 여기서 종료하여 더 이상 진행하지 않음
        }
        if (isFound && TextUtils.isEmpty(location)) {
            Toast.makeText(this, "습득 장소를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return; // 함수를 여기서 종료
        }

        // 3. Firebase에 저장할 고유 ID 생성
        String postId = databaseReference.push().getKey();
        if (postId == null) {
            Toast.makeText(this, "오류: 게시물 ID를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = isFound ? "isfound" : "islost";
        // TODO: 이미지 업로드 기능 추가 시 imageUrl을 실제 URL로 변경해야 함. 지금은 임시로 비워둠.
        String imageUrl = "";

        // 4. Post 객체 생성
        Log.d("PostWriteActivity", "currentUserId: " + currentUserId);
        Log.d("PostWriteActivity", "postId: " + postId);
        Post newPost = new Post(postId, title, itemName, location, description, imageUrl, currentUserId, type);

        // 5. Firebase Realtime Database에 데이터 업로드
        databaseReference.child(postId).setValue(newPost)
                .addOnSuccessListener(aVoid -> {
                    // 업로드 성공
                    Toast.makeText(PostWriteActivity.this, "게시물이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 게시물 목록 화면 등으로 돌아가기 위해 현재 화면 종료
                })
                .addOnFailureListener(e -> {
                    // 업로드 실패
                    Toast.makeText(PostWriteActivity.this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

