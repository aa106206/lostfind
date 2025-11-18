package com.example.lostfind;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRoomList;
    private final Context context;

    public ChatRoomAdapter(Context context, List<ChatRoom> chatRoomList) {
        this.context = context;
        this.chatRoomList = chatRoomList;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);
        holder.bind(chatRoom);
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage, timestamp;

        ChatRoomViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.message_timestamp);
        }

        void bind(ChatRoom chatRoom) {
            userName.setText(chatRoom.getOpponentUserName());
            lastMessage.setText(chatRoom.getLastMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.getDefault());
            timestamp.setText(sdf.format(chatRoom.getLastMessageTimestamp()));

            // 채팅방 클릭 시 해당 채팅방으로 이동
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatRoomId", chatRoom.getChatRoomId());
                intent.putExtra("opponentUserName", chatRoom.getOpponentUserName());
                context.startActivity(intent);
            });
        }
    }
}
