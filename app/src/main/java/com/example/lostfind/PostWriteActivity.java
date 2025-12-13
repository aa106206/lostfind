package com.example.lostfind;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView; // ImageView import 추가
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView; // ScrollView를 위해 import 추가
import android.view.MotionEvent;

import androidx.activity.result.ActivityResultLauncher; // ActivityResultLauncher import 추가
import androidx.activity.result.contract.ActivityResultContracts; // ActivityResultContracts import 추가
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Glide import 추가
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage; // Firebase Storage import 추가
import com.google.firebase.storage.StorageReference; // StorageReference import 추가
import com.google.firebase.storage.UploadTask; // UploadTask import 추가
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


//

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID; // 고유 파일 이름 생성을 위해 추가

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostWriteActivity extends AppCompatActivity implements OnMapReadyCallback{
    public interface OnResultListener {
        void onSuccess(String result);
        void onError(String error);
    }
    // --- 뷰 변수들 ---
    private Button back_Button;
    private EditText postTitleDetail, lostItemName, postDescription, lostItemLocation;
    private CheckBox checkboxIsFound;
    private Button buttonAddPhoto, submitPostButton;
    private ImageView postImageDetail; // ★★★ 이미지뷰 변수 추가 ★★★
    private FrameLayout mapContainer;
    private View locationTextView, mapLocationTextView;
    private ScrollView mainScrollView;
    private View transparentView;

    // --- Firebase 관련 변수들 ---
    private DatabaseReference databaseReference;
    private StorageReference storageReference; // ★★★ Firebase Storage 참조 변수 추가 ★★★
    private FirebaseAuth mAuth;
    private String currentUserId;

    // --- 이미지 데이터 관련 변수 ---
    private Uri imageUri = null; // ★★★ 선택된 이미지의 Uri를 저장할 변수 ★★★
    private ActivityResultLauncher<Intent> galleryLauncher; // ★★★ 갤러리 결과를 처리할 런처 ★★★

    // --- 지도 관련 변수 추가 ---
    private GoogleMap mMap;
    private Marker currentMarker; // 현재 찍혀있는 마커를 저장할 변수
    private LatLng selectedLatLng = null;
    // ★★★ 수정 모드 관련 변수 추가 ★★★
    private boolean isEditMode = false;
    private String editPostId = null;

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

        // ★★★ 수정 모드인지 확인하고, postId를 가져옴 ★★★
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);
        if (isEditMode) {
            editPostId = intent.getStringExtra("postId");
        }

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // 준비가 완료되면 onMapReady가 호출됨
        }

        // --- 클릭 리스너 설정 ---
        setupClickListeners();
        // ★★★ 스크롤뷰와 투명 뷰에 터치 리스너 설정 ★★★
        setupMapTouchListener();
        if (isEditMode && editPostId != null) {
            // 제목을 "글 수정"으로 변경
            TextView toolbarTitle = findViewById(R.id.toolbar_title_text); // XML에 ID가 있다면 사용
            if(toolbarTitle != null) toolbarTitle.setText("글 수정");

            loadPostForEdit();
        }
    }
    private void loadPostForEdit() {
        databaseReference.child(editPostId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        // 가져온 데이터로 UI 채우기
                        postTitleDetail.setText(post.getTitle());
                        lostItemName.setText(post.getItemName());
                        postDescription.setText(post.getDescription());

                        // '습득' 여부 체크
                        if ("isfound".equals(post.getType())) {
                            checkboxIsFound.setChecked(true);
                            // 위도, 경도 정보가 있다면 LatLng 객체로 변환하고 지도에 마커 표시
                            if (post.getLatitude() != 0 && post.getLongitude() != 0) {
                                selectedLatLng = new LatLng(post.getLatitude(), post.getLongitude());
                                // EditText에 위치 정보 표시
                                String locationText = String.format("lat: %.4f, lng: %.4f", selectedLatLng.latitude, selectedLatLng.longitude);
                                lostItemLocation.setText(locationText);
                                // 지도가 준비되었다면 마커를 그림
                                if (mMap != null) {
                                    updateMapMarker();
                                }
                            }
                        }

                        // 이미지 표시
                        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                            Glide.with(PostWriteActivity.this).load(post.getImageUrl()).into(postImageDetail);
                            // imageUri는 null이지만, 사용자가 새 이미지를 선택하지 않으면 기존 URL을 재사용해야 함
                        }
                    }
                } else {
                    Toast.makeText(PostWriteActivity.this, "수정할 게시물을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostWriteActivity.this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateMapMarker() {
        if (mMap == null || selectedLatLng == null) return;
        if (currentMarker != null) currentMarker.remove();
        currentMarker = mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("선택한 위치"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
    }

    private void initializeViews() {
        mainScrollView = findViewById(R.id.scrollView);
        transparentView = findViewById(R.id.transparent_view);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // 지도가 준비되면 GoogleMap 객체를 전역 변수에 할당

        // 기본 카메라 위치 설정 (예: 서울)
        LatLng seoul = new LatLng(37.496349, 126.957454);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f));

        Toast.makeText(this, "습득한 위치를 지도에서 한번만 탭하세요.", Toast.LENGTH_SHORT).show();

        if(isEditMode && selectedLatLng != null) {
            updateMapMarker();
        }

        // ★★★ 지도 클릭 리스너 설정 ★★★
        mMap.setOnMapClickListener(latLng -> {
            // 1. 선택된 위도/경도를 전역 변수에 저장
            selectedLatLng = latLng;

            // 2. 이전에 찍힌 마커가 있다면 제거
            if (currentMarker != null) {
                currentMarker.remove();
            }

            // 3. 새로운 위치에 마커 추가
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("선택한 위치");
            currentMarker = mMap.addMarker(markerOptions);

            // 4. 위치 정보를 EditText에도 간략히 표시
            String locationText = String.format("lat: %.4f, lng: %.4f", latLng.latitude, latLng.longitude);
            lostItemLocation.setText(locationText);
        });
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
            findViewById(R.id.map_wrapper).setVisibility(visibility); // ID로 직접 접근
            if (locationTextView != null) locationTextView.setVisibility(visibility);
            if (mapLocationTextView != null) mapLocationTextView.setVisibility(visibility);
        });
        lostItemLocation.setFocusable(false);
    }



    @SuppressLint("ClickableViewAccessibility")
    private void setupMapTouchListener() {
        transparentView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 손가락이 닿는 순간, ScrollView가 터치 이벤트를 가로채지 못하게 함
                    // (지도에 터치 이벤트를 전달하기 위함)
                    mainScrollView.requestDisallowInterceptTouchEvent(true);
                    return false; // 터치 이벤트를 계속 받기 위해 false 반환

                case MotionEvent.ACTION_UP:
                    // 손가락을 떼는 순간, 다시 ScrollView가 터치 이벤트를 가로챌 수 있게 함
                    mainScrollView.requestDisallowInterceptTouchEvent(false);
                    return true; // 터치 이벤트를 여기서 끝내기 위해 true 반환

                default:
                    return true;
            }
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
            // 1. 새 이미지를 선택한 경우: 항상 이미지 업로드 진행
            uploadImageAndPost(title, itemName, description, location);
        } else if (isEditMode) {
            // 2. 수정 모드이고, 새 이미지를 선택 안 한 경우: 기존 이미지 URL 재사용
            databaseReference.child(editPostId).child("imageUrl").get().addOnSuccessListener(dataSnapshot -> {
                String existingImageUrl = dataSnapshot.exists() ? dataSnapshot.getValue(String.class) : "";
                uploadPostDetails(title, itemName, description, location, existingImageUrl);
            });
        } else {
            // 3. 새 글 작성이고, 이미지를 선택 안 한 경우
            uploadPostDetails(title, itemName, description, location, "");
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
        String postId;

        if (isEditMode) {
            // 수정 모드: 기존 postId 사용
            postId = editPostId;
        } else {
            // 새 글 작성 모드: 새로운 postId 생성
            postId = databaseReference.push().getKey();
        }
        if (postId == null) {
            Toast.makeText(this, "오류: 게시물 ID를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFound = checkboxIsFound.isChecked();
        String type = isFound ? "isfound" : "islost";

        Post newPost = new Post(postId, title, itemName, location, description, imageUrl, currentUserId, type);
//
        if (isFound && selectedLatLng != null) {
            newPost.setLatitude(selectedLatLng.latitude);
            newPost.setLongitude(selectedLatLng.longitude);
        }

        databaseReference.child(postId)
                .setValue(newPost)
                .addOnCompleteListener(task -> {

                    Log.d("Gemini", "setValue onComplete 호출됨");

                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        Log.e("Gemini", "게시물 저장 실패", e);

                        Toast.makeText(
                                PostWriteActivity.this,
                                "게시물 정보 업로드 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    // ★★★ Gemini 분석 및 매칭 로직 (조건부) ★★★
                    if (!isEditMode && isFound && imageUri != null) {

                        Log.d("Gemini", "수정하고 있어요3 (Gemini 분석 시작)");

                        analyzeFoundPostToGemini(
                                title,
                                itemName,
                                location,
                                description,
                                imageUri,
                                new OnResultListener() {

                                    @Override
                                    public void onSuccess(String res) {
                                        Log.d("Gemini", "수정하고 있어요4 (Gemini 성공)");

                                        try {
                                            JSONObject json = extractGeminiJsonOnly(res);
                                            saveGeminiResultToFirebase(postId, json);
                                            startMatchingFlow(postId);
                                        } catch (Exception e) {
                                            Log.e("Gemini", "JSON 파싱 오류", e);
                                        }

                                        runOnUiThread(() -> {
                                            Toast.makeText(
                                                    PostWriteActivity.this,
                                                    "게시물이 등록되었습니다.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            finish();
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("Gemini", "Gemini 오류: " + error);

                                        runOnUiThread(() -> {
                                            Toast.makeText(
                                                    PostWriteActivity.this,
                                                    "게시물은 등록되었으나 AI 분석이 실패했습니다.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            finish();
                                        });
                                    }
                                }
                        );

                    } else {
                        // 수정 모드 / 분실물 / 이미지 없음 → AI 없이 종료
                        Log.d("Gemini", "수정하고 있어요6 (AI 미실행)");

                        String message = isEditMode
                                ? "게시물이 수정되었습니다."
                                : "게시물이 등록되었습니다.";

                        Toast.makeText(
                                PostWriteActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    }
                });
    }

    private void analyzeFoundPostToGemini(String title, String itemName, String location,String description, Uri imageUri, OnResultListener listener) {
//        String apiKey = BuildConfig.GEMINI_API_KEY;
        String apiKey="AIzaSyCQvTfmmo_dZXngI5yYG8otAVO3_4KYuTM";

        OkHttpClient client = new OkHttpClient();

        try {
            // 1. 이미지 Base64 변환
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            // 2. 프롬프트
            String prompt =
                    "You are an AI that extracts structured information from lost & found posts.\n\n" +
                            "Analyze the following 'found item post' and extract 4 fields:\n\n" +
                            "1. found_location\n" +
                            "2. item_name\n" +
                            "3. item_features\n" +
                            "4. image_features\n\n" +
                            "Return JSON ONLY:\n" +
                            "{\n" +
                            "  \"found_location\": \"\",\n" +
                            "  \"item_name\": \"\",\n" +
                            "  \"item_features\": \"\",\n" +
                            "  \"image_features\": \"\"\n" +
                            "}\n\n" +
                            "POST TEXT:\n" + title + "\n" + itemName+"\n" + location+"\n" + description;

            // 3. JSON Request body
            JSONObject requestBody = new JSONObject();

            JSONArray contents = new JSONArray();
            JSONObject partsObj = new JSONObject();
            JSONArray parts = new JSONArray();

            // Text part
            parts.put(new JSONObject().put("text", prompt));

            // Image part
            parts.put(new JSONObject()
                    .put("inline_data",
                            new JSONObject()
                                    .put("mime_type", "image/jpeg")
                                    .put("data", base64Image)
                    )
            );

            partsObj.put("parts", parts);
            contents.put(partsObj);
            requestBody.put("contents", contents);

            // 4. Build request
            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .build();

            // 5. Execute
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    listener.onSuccess(res);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onError(e.getMessage());
                }
            });

        } catch (Exception e) {
            listener.onError(e.getMessage());
        }
    }

private JSONObject extractGeminiJsonOnly(String res) throws Exception {
    JSONObject root = new JSONObject(res);
    JSONArray candidates = root.getJSONArray("candidates");
    JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
    JSONArray parts = content.getJSONArray("parts");

    String jsonText = parts.getJSONObject(0).getString("text");

    // 1) 앞/뒤에 붙은 ```json 또는 ``` 제거
    jsonText = jsonText.replace("```json", "")
            .replace("```", "")
            .trim();

    // 2) 줄바꿈 제거
    if (jsonText.startsWith("\n")) jsonText = jsonText.substring(1).trim();

    // 3) 이제 진짜 JSON으로 변환
    return new JSONObject(jsonText);
}

    private void saveGeminiResultToFirebase(String postId, JSONObject json) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts").child(postId).child("gemini");

        Map<String, Object> map = new HashMap<>();
        map.put("found_location", json.optString("found_location"));
        map.put("item_name", json.optString("item_name"));
        map.put("item_features", json.optString("item_features"));
        map.put("image_features", json.optString("image_features"));

        ref.setValue(map);
    }

    private void startMatchingFlow(String postId) {
        // Step1: Lost 게시글 전체 불러오기
        GeminiLostPostLoader.loadLostPosts(new GeminiLostPostLoader.OnLostPostsLoaded() {
            @Override
            public void onSuccess(List<Post> lostPosts) {
                // Step2: Found 게시글의 Gemini JSON 불러오기
                loadFoundGeminiJson(postId, lostPosts);
            }
            @Override
            public void onError(String error) {
                Log.e("Match", "Lost load error: " + error);
            }
        });
    }


    private void loadFoundGeminiJson(String postId, List<Post> lostPosts) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("posts")
                .child(postId)
                .child("gemini");

        ref.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Log.e("Match", "Gemini JSON 없음");
                return;
            }

            try {
                JSONObject foundJson = new JSONObject((Map) snapshot.getValue());

                // Step3: GeminiMatcher 호출
                requestMatching(foundJson, lostPosts);

            } catch (Exception e) {
                Log.e("Match", "Found JSON parse error: " + e.getMessage());
            }

        }).addOnFailureListener(e -> {
            Log.e("Match", "Gemini load error: " + e.getMessage());
        });
    }

    private void requestMatching(JSONObject foundJson, List<Post> lostPosts) {

        GeminiMatcher.matchFoundWithLost(foundJson, lostPosts, new GeminiMatcher.MatchCallback() {
            @Override
            public void onSuccess(JSONArray matches) {

                Log.d("Match", "매칭 결과: " + matches.toString());

                // FCM 알림은 여기서 호출 예정
                try {
                    for (int i = 0; i < matches.length(); i++) {
                        JSONObject obj = matches.getJSONObject(i);

                        String matchedPostId = obj.getString("lostPostId");  // ✔ 추출 완료
                        Log.d("Match", "매칭된 postId = " + matchedPostId);

                        // 매칭된 게시글의 작성자에게 알림 보내기
                        notifyMatchedUser(matchedPostId);
                    }
                } catch (Exception e) {
                    Log.e("Match", "매칭 처리 오류: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                Log.e("Match", "매칭 오류: " + error);
            }
        });
    }

    private void notifyMatchedUser(String postId) {

        FirebaseDatabase.getInstance().getReference("posts")
                .child(postId)
                .child("authorId")
                .get()
                .addOnSuccessListener(snap -> {

                    String userId = snap.getValue(String.class);  // 분실글 작성자 UID

                    if (userId != null) {

                        // Cloud Functions가 FCM을 보내기 위한 데이터 생성
                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("notifications")
                                .child(userId)
                                .push();

                        Map<String, Object> map = new HashMap<>();
                        map.put("title", "습득물과 매칭되었습니다!");
                        map.put("body", "당신이 잃어버린 물건이 발견된 것 같아요.");

                        ref.setValue(map);  // ← Functions 트리거됨! (sendMatchNotification 실행)
                    }

                });
    }



}
