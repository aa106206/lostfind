package com.example.lostfind;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRoomList;
    private DatabaseReference mDatabase;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        recyclerView = findViewById(R.id.chat_room_recyclerview);
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(this, chatRoomList);
        recyclerView.setAdapter(adapter);

        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadChatRooms();

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
        });
    }

    private void loadChatRooms() {
        // 내가 참여중인 채팅방 ID 목록을 가져온다.
        mDatabase.child("user_chat_rooms").child(currentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomList.clear();
                for (DataSnapshot chatRoomIdSnapshot : snapshot.getChildren()) {
                    String chatRoomId = chatRoomIdSnapshot.getKey();
                    if (chatRoomId != null) {
                        fetchChatRoomInfo(chatRoomId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatRoomListActivity.this, "채팅방 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchChatRoomInfo(String chatRoomId) {
        // 채팅방 ID를 이용해 채팅방 상세 정보를 가져온다.
        mDatabase.child("chat_rooms").child(chatRoomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatRoom room = snapshot.getValue(ChatRoom.class);
                if (room != null) {
                    room.setChatRoomId(chatRoomId);

                    // 상대방 정보 찾기
                    Map<String, Boolean> participants = (Map<String, Boolean>) snapshot.child("participants").getValue();
                    if (participants != null) {
                        for (String uid : participants.keySet()) {
                            if (!uid.equals(currentUserUid)) {
                                mDatabase.child("users").child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                                        String opponentName = nameSnapshot.getValue(String.class);
                                        room.setOpponentUserName(opponentName != null ? opponentName : "상대방");

                                        // 중복 체크 후 리스트에 추가
                                        boolean isExisting = false;
                                        for (int i = 0; i < chatRoomList.size(); i++) {
                                            if (chatRoomList.get(i).getChatRoomId().equals(chatRoomId)) {
                                                chatRoomList.set(i, room);
                                                isExisting = true;
                                                break;
                                            }
                                        }
                                        if (!isExisting) {
                                            chatRoomList.add(room);
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
