package com.example.lostfind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage chatMessage = messageList.get(position);
        holder.messageText.setText(chatMessage.getMessageText());
        String meta = chatMessage.getMessageUser() + " - " + sdf.format(chatMessage.getMessageTime());
        holder.messageMeta.setText(meta);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageMeta;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(android.R.id.text1);
            messageMeta = itemView.findViewById(android.R.id.text2);
        }
    }
}
