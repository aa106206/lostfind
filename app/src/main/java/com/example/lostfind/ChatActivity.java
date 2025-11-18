package com.example.lostfind;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

    // --- 에러 수정: 필요한 변수들을 모두 선언합니다. ---
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> chatMessages;
    private EditText input;
    private Button fab;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private String currentUserName = "Anonymous"; // 기본값 설정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- 에러 수정: findViewById 호출을 올바르게 배치합니다. ---
        recyclerView = findViewById(R.id.list_of_messages);
        input = findViewById(R.id.input);
        fab = findViewById(R.id.fab_send);

        chatMessages = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 전송 버튼 클릭 리스너 설정
        fab.setOnClickListener(view -> sendMessage());

        // 사용자 이름을 먼저 가져온 후, 어댑터를 설정하고 메시지를 표시합니다.
        fetchUserName();
    }

    private void fetchUserName() {
        // 현재 사용자의 이름을 'users' DB에서 가져옵니다.
        mDatabase.child("users").child(currentUser.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    currentUserName = name;
                }

                // <<< 중요: 사용자 이름을 가져온 후 어댑터를 초기화하고 RecyclerView에 설정합니다. >>>
                adapter = new ChatAdapter(chatMessages, currentUserName);
                recyclerView.setAdapter(adapter);

                // 메시지 목록을 불러옵니다.
                displayChatMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "사용자 이름을 불러오는 데 실패했습니다. 'Anonymous'로 참여합니다.", Toast.LENGTH_SHORT).show();

                // 실패하더라도 기본 이름으로 채팅을 시작합니다.
                adapter = new ChatAdapter(chatMessages, currentUserName);
                recyclerView.setAdapter(adapter);
                displayChatMessages();
            }
        });
    }

    // --- 에러 수정: sendMessage 메소드를 완성합니다. ---
    private void sendMessage() {
        String messageText = input.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // ChatMessage 객체를 생성합니다 (보낸사람 이름 포함)
            ChatMessage chatMessage = new ChatMessage(messageText, currentUserName);

            // Firebase Realtime Database의 "chat" 경로에 메시지를 push합니다.
            mDatabase.child("chat").push().setValue(chatMessage)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 메시지 전송 성공 시 입력 필드 초기화
                            input.setText("");
                        } else {
                            Toast.makeText(ChatActivity.this, "메시지 전송 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // --- 에러 수정: displayChatMessages 메소드를 완성합니다. ---
    private void displayChatMessages() {
        mDatabase.child("chat").addChildEventListener(new ChildEventListener() {
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
