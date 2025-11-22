package com.example.lostfind;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    // --- 변수 선언 ---
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages;
    private EditText input;
    private Button fab;
    private DatabaseReference chatMessagesRef; // 메시지 경로 참조로 이름 변경
    private FirebaseUser currentUser;
    private String currentUserName = "Anonymous"; // 내 이름
    private String chatRoomId; // 현재 채팅방 ID
    private String opponentUserName; // 상대방 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Intent로부터 데이터 받기 ---
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        opponentUserName = getIntent().getStringExtra("opponentUserName");

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Toast.makeText(this, "채팅방 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Firebase 사용자 정보 및 DB 경로 설정 ---
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 메시지들이 저장된 경로를 참조
        chatMessagesRef = FirebaseDatabase.getInstance().getReference("chat_messages").child(chatRoomId);

        // --- UI 초기화 ---
        initializeViews();

        // --- 내 이름 가져오기 및 채팅 시작 ---
        fetchMyUserNameAndStartChat();
    }

    private void initializeViews() {
        // 액션바(타이틀바)에 상대방 이름 표시
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(opponentUserName);
        }

        recyclerView = findViewById(R.id.list_of_messages);
        input = findViewById(R.id.input);
        fab = findViewById(R.id.fab_send);

        chatMessages = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 전송 버튼 클릭 리스너
        fab.setOnClickListener(v -> sendMessage());
    }

    private void fetchMyUserNameAndStartChat() {
        // 내 이름을 'users' DB에서 가져옵니다.
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(currentUser.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    currentUserName = name;
                }

                // 어댑터를 초기화하고 RecyclerView에 설정합니다.
                adapter = new ChatAdapter(chatMessages, currentUserName);
                recyclerView.setAdapter(adapter);

                // 메시지 목록을 불러옵니다.
                displayChatMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "사용자 이름을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                // 실패하더라도 채팅은 계속 진행합니다.
                adapter = new ChatAdapter(chatMessages, currentUserName);
                recyclerView.setAdapter(adapter);
                displayChatMessages();
            }
        });
    }

    private void sendMessage() {
        String messageText = input.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // ChatMessage 객체를 생성합니다 (보낸사람 이름은 '내 이름'으로)
            ChatMessage chatMessage = new ChatMessage(messageText, currentUserName);

            // 1. 'chat_messages/{chatRoomId}' 경로에 새로운 메시지를 저장합니다.
            chatMessagesRef.push().setValue(chatMessage)
                    .addOnSuccessListener(aVoid -> input.setText("")) // 성공 시 입력창 초기화
                    .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "메시지 전송 실패", Toast.LENGTH_SHORT).show());

            // 2. 'chat_rooms/{chatRoomId}' 경로의 마지막 메시지 정보를 업데이트합니다.
            DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("chat_rooms").child(chatRoomId);
            roomRef.child("lastMessage").setValue(messageText);
            roomRef.child("lastMessageTimestamp").setValue(System.currentTimeMillis());
        }
    }

    private void displayChatMessages() {
        // chatMessagesRef 경로에 있는 메시지들을 실시간으로 가져옵니다.
        chatMessagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                if (chatMessage != null) {
                    chatMessages.add(chatMessage);
                    // 어댑터에 아이템이 추가되었음을 알리고, 마지막 위치로 스크롤합니다.
                    adapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "메시지를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
