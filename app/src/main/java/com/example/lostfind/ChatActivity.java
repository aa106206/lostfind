package com.example.lostfind;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

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
    private TextView chatUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        chatRoomId = getIntent().getStringExtra("chatRoomId");
        opponentUserName = getIntent().getStringExtra("opponentUserName");


        chatUserName = findViewById(R.id.chat_user_name);
        chatUserName.setText(opponentUserName);

        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Toast.makeText(this, "채팅방 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatMessagesRef = FirebaseDatabase.getInstance().getReference("chat_messages").child(chatRoomId);


        initializeViews();


        fetchMyUserNameAndStartChat();

        findViewById(R.id.btn_reward).setOnClickListener(v -> {
            giveRewardToOpponent();
        });

    }

    private void initializeViews() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(opponentUserName);
        }

        recyclerView = findViewById(R.id.list_of_messages);
        input = findViewById(R.id.input);
        fab = findViewById(R.id.fab_send);

        chatMessages = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        fab.setOnClickListener(v -> sendMessage());
    }

    private void fetchMyUserNameAndStartChat() {

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

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
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

    private void giveRewardToOpponent() {
        if (chatRoomId == null || currentUser == null) {
            Toast.makeText(this, "보상 정보를 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. chatRoomId에서 상대방의 UID를 추출합니다.
        // chatRoomId는 두 UID를 밑줄(_)로 연결한 형태 (예: uid1_uid2)
        String myUid = currentUser.getUid();
        String[] uids = chatRoomId.split("_");
        String opponentUid = null;

        if (uids.length == 2) {
            if (uids[0].equals(myUid)) {
                opponentUid = uids[1];
            } else {
                opponentUid = uids[0];
            }
        }

        if (opponentUid == null) {
            Toast.makeText(this, "상대방 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Firebase 'users' 경로에서 상대방의 데이터 참조를 가져옵니다.
        DatabaseReference opponentUserRef = FirebaseDatabase.getInstance().getReference("users").child(opponentUid);

        // 3. 트랜잭션을 사용하여 안전하게 포인트를 100 증가시킵니다.
        // 트랜잭션은 여러 사용자가 동시에 포인트를 변경하려고 할 때 데이터 충돌을 방지합니다.
        opponentUserRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                // 현재 포인트를 가져옵니다.
                Long currentPoint = mutableData.child("point").getValue(Long.class);

                if (currentPoint == null) {
                    // 'point' 필드가 없다면 100으로 새로 생성합니다.
                    currentPoint = 100L;
                } else {
                    // 기존 포인트에 100을 더합니다.
                    currentPoint += 100;
                }

                // 변경된 포인트 값을 데이터베이스에 다시 설정합니다.
                mutableData.child("point").setValue(currentPoint);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    // 트랜잭션이 성공적으로 완료되었을 때
                    Toast.makeText(ChatActivity.this, opponentUserName + "님에게 100 포인트를 지급했습니다!", Toast.LENGTH_LONG).show();
                    // 보상 버튼을 비활성화하여 중복 지급을 방지합니다.
                    findViewById(R.id.btn_reward).setEnabled(false);
                    findViewById(R.id.btn_reward).setAlpha(0.5f); // 버튼을 반투명하게 만들어 비활성화 상태를 시각적으로 표시
                } else {
                    // 트랜잭션 실패 시
                    Toast.makeText(ChatActivity.this, "포인트 지급에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    Log.e("ChatActivity", "포인트 지급 트랜잭션 실패: ", error != null ? error.toException() : null);
                }
            }
        });
    }

}
