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

import com.example.lostfind.databinding.ListBookmarkBinding;
import com.example.lostfind.databinding.ItemBookmarkBinding;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ListBookmarkBinding binding = ListBookmarkBinding.inflate(getLayoutInflater());
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
            list.add("게시글 제목 : " + i);
        }

        binding.recyclerBookmark.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerBookmark.setAdapter(new BookmarkActivity.MyAdapter(list));
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemBookmarkBinding itemBinding;

        public MyViewHolder(ItemBookmarkBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        private void bind(String text) {
            itemBinding.tvPostTitle.setText(text);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<BookmarkActivity.MyViewHolder> {
        private List<String> namelist;

        private MyAdapter(List<String> list) {
            this.namelist = list;
        }

        @NonNull
        @Override
        public BookmarkActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemBookmarkBinding itemBinding = ItemBookmarkBinding.inflate(getLayoutInflater());
            return new BookmarkActivity.MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull BookmarkActivity.MyViewHolder holder, int position) {
            String text = namelist.get(position);
            holder.bind(text);
        }

        @Override
        public int getItemCount() {
            return namelist.size();
        }
    }

}