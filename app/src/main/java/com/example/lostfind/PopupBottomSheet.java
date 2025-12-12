package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PopupBottomSheet extends BottomSheetDialogFragment {
    private String postId;
    private String authorId;
    private String itemName;
    private String date;
    private String imageUrl;
    View view;

    public PopupBottomSheet(String postId, String authorId, String itemName, String date, String imageUrl) {
        this.postId=postId;
        this.authorId = authorId;
        this.itemName = itemName;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.popup, container, false);

        ImageView imageView = view.findViewById(R.id.imgView);
        TextView itemNameTextView = view.findViewById(R.id.titleText);
        TextView dateTextView = view.findViewById(R.id.dateText);

        itemNameTextView.setText(itemName);
        dateTextView.setText(date);
        Glide.with(requireContext()).load(imageUrl).into(imageView);  //popup 이미지로딩 부분

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button goPostBtn = view.findViewById(R.id.button);
        ImageButton messageBtn = view.findViewById(R.id.button2);


        // 게시글 상세로 이동
        goPostBtn.setOnClickListener(v -> {
            Log.d("PopupBottomSheet", "#########게시글이동버튼1#########");
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra("POST_ID", postId);
            Log.d("PopupBottomSheet", "#########게시글이동버튼2#########");
            startActivity(intent);
            Log.d("PopupBottomSheet", "#########게시글이동버튼3#########");
            dismiss();
        });

        // 쪽지 보내기
        messageBtn.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            // 1. 로그인 상태 확인
            if (currentUser == null) {
                Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String myUid = currentUser.getUid();
            String opponentUid = authorId; // 생성자에서 전달받은 게시물 작성자 ID

            // 2. 자기 자신에게 쪽지를 보내는 경우 방지
            if (opponentUid == null || opponentUid.equals(myUid)) {
                Toast.makeText(getContext(), "자기 자신에게는 쪽지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. 상대방의 이름을 가져옵니다. (ChatActivity 상단에 표시하기 위함)
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(opponentUid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                    String opponentName = nameSnapshot.getValue(String.class);
                    if (opponentName == null || opponentName.isEmpty()) {
                        opponentName = "상대방"; // 이름 정보가 없을 경우 기본값
                    }

                    // 4. 고유한 채팅방 ID를 생성합니다. (startChat()과 동일한 방식)
                    String chatRoomId;
                    if (myUid.compareTo(opponentUid) > 0) {
                        chatRoomId = myUid + "_" + opponentUid;
                    } else {
                        chatRoomId = opponentUid + "_" + myUid;
                    }

                    DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chat_rooms").child(chatRoomId);

                    // 5. 채팅방 존재 여부를 확인하고, 없으면 생성합니다.
                    final String finalOpponentName = opponentName; // 람다식 내부에서 사용하기 위해 final로 선언
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

                        // 6. 모든 정보가 준비된 후, 채팅방으로 이동합니다.
                        if (getActivity() != null) {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("chatRoomId", chatRoomId); // ChatActivity가 이 ID를 사용하도록 설정
                            intent.putExtra("opponentUserName", finalOpponentName); // 상대방 이름을 전달
                            startActivity(intent);
                            dismiss(); // BottomSheet 닫기
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "상대방 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}