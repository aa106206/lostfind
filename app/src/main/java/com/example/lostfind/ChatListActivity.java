package com.example.lostfind;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfind.databinding.ItemBookmarkBinding;
import com.example.lostfind.databinding.ListChatBinding;
import com.example.lostfind.databinding.ItemChatBinding;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ListChatBinding binding = ListChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        List<String> list = new ArrayList<>();
        for (int i = 0; i  < 20; i++) {
            list.add("채팅 : " + i);
        }

        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerChat.setAdapter(new ChatListActivity.MyAdapter(list));
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemChatBinding itemBinding;

        public MyViewHolder(ItemChatBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        private void bind(String text) {
            itemBinding.tvSenderName.setText(text);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<ChatListActivity.MyViewHolder> {
        private List<String> list;

        private MyAdapter(List<String> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ChatListActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemChatBinding itemBinding = ItemChatBinding.inflate(getLayoutInflater());
            return new ChatListActivity.MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatListActivity.MyViewHolder holder, int position) {
            String text = list.get(position);
            holder.bind(text);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}