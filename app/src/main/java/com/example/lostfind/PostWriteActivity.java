package com.example.lostfind;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView; // ImageView import 추가
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // ActivityResultLauncher import 추가
import androidx.activity.result.contract.ActivityResultContracts; // ActivityResultContracts import 추가
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Glide import 추가
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage; // Firebase Storage import 추가
import com.google.firebase.storage.StorageReference; // StorageReference import 추가
import com.google.firebase.storage.UploadTask; // UploadTask import 추가

import java.util.UUID; // 고유 파일 이름 생성을 위해 추가

public class PostWriteActivity extends AppCompatActivity {

    // --- 뷰 변수들 ---
    private Button back_Button;
    private EditText postTitleDetail, lostItemName, postDescription, lostItemLocation;
    private CheckBox checkboxIsFound;
    private Button buttonAddPhoto, submitPostButton;
    private ImageView postImageDetail; // ★★★ 이미지뷰 변수 추가 ★★★
    private FrameLayout mapContainer;
    private View locationTextView, mapLocationTextView;

    // --- Firebase 관련 변수들 ---
    private DatabaseReference databaseReference;
    private StorageReference storageReference; // ★★★ Firebase Storage 참조 변수 추가 ★★★
    private FirebaseAuth mAuth;
    private String currentUserId;

    // --- 이미지 데이터 관련 변수 ---
    private Uri imageUri = null; // ★★★ 선택된 이미지의 Uri를 저장할 변수 ★★★
    private ActivityResultLauncher<Intent> galleryLauncher; // ★★★ 갤러리 결과를 처리할 런처 ★★★

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_write);

        // --- Firebase 인스턴스 초기화 ---
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        storageReference = FirebaseStorage.getInstance().getReference("post_images"); // "post_images" 폴더에 저장

        // --- 사용자 로그인 상태 확인 ---
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "글을 작성하려면 로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        // --- 뷰 초기화 ---
        initializeViews();

        // ★★★ 갤러리 런처 초기화: 갤러리에서 이미지를 선택하면 실행될 콜백 설정 ★★★
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // 이미지를 성공적으로 선택했을 때
                        imageUri = result.getData().getData(); // 선택한 이미지의 Uri 저장
                        // Glide를 사용하여 선택한 이미지를 ImageView에 표시
                        Glide.with(this).load(imageUri).into(postImageDetail);
                    }
                }
        );

        // --- 클릭 리스너 설정 ---
        setupClickListeners();
    }

    private void initializeViews() {
        back_Button = findViewById(R.id.back_button);
        postTitleDetail = findViewById(R.id.post_title_detail);
        lostItemName = findViewById(R.id.lost_item_name);
        postDescription = findViewById(R.id.post_description);
        checkboxIsFound = findViewById(R.id.checkbox_is_found);
        lostItemLocation = findViewById(R.id.lost_item_location);
        buttonAddPhoto = findViewById(R.id.button_add_photo);
        submitPostButton = findViewById(R.id.submit_post_button);
        postImageDetail = findViewById(R.id.post_image_detail); // ImageView 연결
        mapContainer = findViewById(R.id.map_container);
        locationTextView = findViewById(R.id.location_text_view);
        mapLocationTextView = findViewById(R.id.map_location_text_view);
    }

    private void setupClickListeners() {
        back_Button.setOnClickListener(v -> onBackPressed());

        // ★★★ '사진 추가' 버튼 클릭 시 갤러리 열기 ★★★
        buttonAddPhoto.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        submitPostButton.setOnClickListener(v -> uploadPost());

        checkboxIsFound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            lostItemLocation.setVisibility(visibility);
            mapContainer.setVisibility(visibility);
            if (locationTextView != null) locationTextView.setVisibility(visibility);
            if (mapLocationTextView != null) mapLocationTextView.setVisibility(visibility);
        });
    }

    private void uploadPost() {
        String title = postTitleDetail.getText().toString().trim();
        String itemName = lostItemName.getText().toString().trim();
        String description = postDescription.getText().toString().trim();
        String location = lostItemLocation.getText().toString().trim();

        // --- 유효성 검사 ---
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(itemName) || TextUtils.isEmpty(description)) {
            Toast.makeText(this, "제목, 분실물 이름, 상세 설명을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ★★★ 이미지가 선택되었을 경우에만 업로드 진행 ★★★
        if (imageUri != null) {
            uploadImageAndPost(title, itemName, description, location);
        } else {
            // 이미지가 없는 경우, 바로 게시물 텍스트 정보만 업로드
            uploadPostDetails(title, itemName, description, location, ""); // 이미지 URL은 빈 문자열로 전달
        }
    }

    // ★★★ 이미지 업로드와 게시물 정보 업로드를 함께 처리하는 메서드 ★★★
    private void uploadImageAndPost(String title, String itemName, String description, String location) {
        // 고유한 파일 이름 생성 (중복 방지)
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageReference.child(fileName);

        // 이미지 업로드 시작
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 이미지 업로드 성공 시, 다운로드 URL 가져오기
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // 다운로드 URL과 함께 게시물 텍스트 정보 업로드
                        uploadPostDetails(title, itemName, description, location, imageUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ★★★ 최종적으로 Realtime Database에 모든 정보를 저장하는 메서드 ★★★
    private void uploadPostDetails(String title, String itemName, String description, String location, String imageUrl) {
        String postId = databaseReference.push().getKey();
        if (postId == null) {
            Toast.makeText(this, "오류: 게시물 ID를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFound = checkboxIsFound.isChecked();
        String type = isFound ? "isfound" : "islost";

        Post newPost = new Post(postId, title, itemName, location, description, imageUrl, currentUserId, type);

        databaseReference.child(postId).setValue(newPost)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PostWriteActivity.this, "게시물이 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PostWriteActivity.this, "게시물 정보 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
