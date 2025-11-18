package com.example.lostfind;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<ChatMessage> messageList;
    private final String currentUserName;
    private final SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.getDefault());

    // 뷰 타입을 구분하기 위한 상수
    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    public ChatAdapter(List<ChatMessage> messageList, String currentUserName) {
        this.messageList = messageList;
        this.currentUserName = currentUserName;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        // 메시지를 보낸 사람의 이름과 현재 사용자의 이름이 같으면 내 메시지로 판단
        if (message.getMessageUser().equals(currentUserName)) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 위에서 만든 item_chat_message.xml을 인플레이트
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage chatMessage = messageList.get(position);
        holder.bind(chatMessage, getItemViewType(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout; // 부모 레이아웃 참조 추가
        TextView messageText;
        TextView messageUser;
        TextView messageTime;

        MessageViewHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView; // xml의 최상위 레이아웃
            messageText = itemView.findViewById(R.id.message_text);
            messageUser = itemView.findViewById(R.id.message_user);
            messageTime = itemView.findViewById(R.id.message_time);
        }

        void bind(ChatMessage chatMessage, int viewType) {
            messageText.setText(chatMessage.getMessageText());
            messageUser.setText(chatMessage.getMessageUser());
            messageTime.setText(sdf.format(chatMessage.getMessageTime()));

            if (viewType == VIEW_TYPE_MY_MESSAGE) {
                // 내 메시지: 오른쪽 정렬
                rootLayout.setGravity(Gravity.END);
                messageText.setBackgroundResource(R.drawable.my_message_background);
                messageText.setTextColor(Color.WHITE);
                // 내 메시지는 사용자 이름을 숨길 수 있습니다.
                // messageUser.setVisibility(View.GONE);

            } else {
                // 상대방 메시지: 왼쪽 정렬
                rootLayout.setGravity(Gravity.START);
                messageText.setBackgroundResource(R.drawable.other_message_background);
                messageText.setTextColor(Color.BLACK);
                messageUser.setVisibility(View.VISIBLE);
            }
        }
    }
}
