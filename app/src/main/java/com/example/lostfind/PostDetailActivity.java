package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

// NonNull 어노테이션을 위해 추가
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// 이미지 로딩을 위한 Glide 라이브러리 (build.gradle에 추가 필요)
import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ### Firestore에서 Realtime Database로 import 변경 ###
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostDetailActivity extends AppCompatActivity {

    private TextView postTitleDetail, userName, postTimestamp, lostItemName, lostItemLocation, postDescription;
    private ImageView profileImage, postImageDetail;
    private View locationTextView, mapLocationTextView, mapContainer;
    private Button editButton, deleteButton, sendMessageButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_detail);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        initializeViews();

        Intent intent = getIntent();
        postId = intent.getStringExtra("POST_ID");

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "오류: 게시물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 에러 발생 시 액티비티 종료
            return;
        }

        loadPostData();
    }

    private void initializeViews() {
        postTitleDetail = findViewById(R.id.post_title_detail);
        userName = findViewById(R.id.user_name);
        postTimestamp = findViewById(R.id.post_timestamp);
        lostItemName = findViewById(R.id.lost_item_name);
        lostItemLocation = findViewById(R.id.lost_item_location);
        postDescription = findViewById(R.id.post_description);
        profileImage = findViewById(R.id.profile_image);
        postImageDetail = findViewById(R.id.post_image_detail);
        locationTextView = findViewById(R.id.location_text_view);
        mapLocationTextView = findViewById(R.id.map_location_text_view);
        mapContainer = findViewById(R.id.map_container);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
        sendMessageButton = findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(v -> startChat());
    }

    private void loadPostData() {

        databaseReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 가져온 데이터를 Post.java 객체로 자동 변환
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        populateUI(post); // UI에 데이터 채우기
                        updateButtonVisibility(post.getAuthorId()); // 버튼 가시성 설정
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this, "오류: 게시물이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터 로딩에 실패했을 때
                Toast.makeText(PostDetailActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(Post post) {
        postTitleDetail.setText(post.getTitle());
        lostItemName.setText(post.getItemName());
        postDescription.setText(post.getDescription());
        // TODO: 작성자 이름, 프로필 이미지는 'users' DB에서 authorId로 가져와야 함 (지금은 임시 처리)
        // userName.setText(post.getAuthorId()); // 우선 UID로 표시
        // TODO: 타임스탬프(Long)를 "n분 전" 같은 형식의 문자열로 변환하여 표시해야 함.
        // postTimestamp.setText(...);

        // '습득(isfound)' 게시물일 경우에만 위치 정보 관련 뷰들을 보여줌
        if ("isfound".equalsIgnoreCase(post.getType())) {
            locationTextView.setVisibility(View.VISIBLE);
            lostItemLocation.setVisibility(View.VISIBLE);
            mapLocationTextView.setVisibility(View.VISIBLE);
            mapContainer.setVisibility(View.VISIBLE);
            lostItemLocation.setText(post.getLocation());
        } else {
            // '분실(islost)' 게시물일 경우 숨김
            locationTextView.setVisibility(View.GONE);
            lostItemLocation.setVisibility(View.GONE);
            mapLocationTextView.setVisibility(View.GONE);
            mapContainer.setVisibility(View.GONE);
        }

        // Glide 라이브러리를 사용하여 이미지 로드
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(this).load(post.getImageUrl()).into(postImageDetail);
        }
    }

    private void updateButtonVisibility(String authorId) {
        // 현재 로그인한 사용자가 글 작성자인지 확인
        if (currentUserId != null && currentUserId.equals(authorId)) {
            // 작성자 본인: 수정/삭제 버튼 보이기
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            sendMessageButton.setVisibility(View.GONE);
        } else {
            // 다른 사용자: 메시지 보내기 버튼 보이기
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            sendMessageButton.setVisibility(View.VISIBLE);
        }
    }

    private void startChat() {
        // 상대방(게시글 작성자)의 정보를 가져옵니다.
        databaseReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postSnapshot) {
                if (!postSnapshot.exists()) {
                    Toast.makeText(PostDetailActivity.this, "게시물 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Post post = postSnapshot.getValue(Post.class);
                if (post == null) return;

                String opponentUid = post.getAuthorId();
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (opponentUid == null || opponentUid.equals(myUid)) {
                    Toast.makeText(PostDetailActivity.this, "자기 자신과는 채팅할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 상대방의 이름을 가져오기 위해 'users' DB를 조회합니다.
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.child(opponentUid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                        String opponentName = nameSnapshot.getValue(String.class);
                        if (opponentName == null) {
                            opponentName = "상대방"; // 이름 정보가 없을 경우 기본값
                        }

                        // 채팅방 ID 생성 및 채팅방으로 이동
                        createOrGoToChatRoom(myUid, opponentUid, opponentName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PostDetailActivity.this, "상대방 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "데이터 접근에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrGoToChatRoom(String myUid, String opponentUid, String opponentName) {
        // 두 사용자의 UID를 정렬하여 고유한 채팅방 ID를 생성합니다.
        String chatRoomId;
        if (myUid.compareTo(opponentUid) > 0) {
            chatRoomId = myUid + "_" + opponentUid;
        } else {
            chatRoomId = opponentUid + "_" + myUid;
        }

        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chat_rooms").child(chatRoomId);

        // 채팅방이 이미 존재하는지 확인
        chatRoomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                // 채팅방이 없으면 새로 생성
                Map<String, Object> participants = new HashMap<>();
                participants.put(myUid, true);
                participants.put(opponentUid, true);

                Map<String, Object> roomData = new HashMap<>();
                roomData.put("participants", participants);
                roomData.put("lastMessage", "채팅방이 생성되었습니다.");
                roomData.put("lastMessageTimestamp", System.currentTimeMillis());
                chatRoomRef.setValue(roomData);

                // 각 사용자의 채팅방 목록에도 추가
                DatabaseReference userChatRoomsRef = FirebaseDatabase.getInstance().getReference("user_chat_rooms");
                userChatRoomsRef.child(myUid).child(chatRoomId).setValue(true);
                userChatRoomsRef.child(opponentUid).child(chatRoomId).setValue(true);
            }

            // 채팅방으로 이동
            Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
            intent.putExtra("chatRoomId", chatRoomId);
            intent.putExtra("opponentUserName", opponentName);
            startActivity(intent);
        });
    }



}
