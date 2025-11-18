package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class PostListActivity extends AppCompatActivity {

    private com.example.lostfind.PostAdapter adapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postlist);

        Button backButton = findViewById(R.id.back_button);
        Button writePostButton = findViewById(R.id.write_post_button);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        EditText searchEditText = findViewById(R.id.search_edit_text);

        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        writePostButton.setOnClickListener(v -> {
            Intent intent = new Intent(PostListActivity.this, PostWriteActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new com.example.lostfind.PostAdapter(this, postList);
        recyclerView.setAdapter(adapter);

        loadPostData();
    }

    private void loadPostData() {

        //postList.add(new Post("분실물 찾음", "학생회관", "2024-11-13", "https://picsum.photos/seed/1/200/200"));
        //postList.add(new Post("분실물 찾음", "중앙도서관", "2024-11-12", "https://picsum.photos/seed/2/200/200"));
        //postList.add(new Post("분실물 찾음", "정보섬", "2024-11-11", "https://picsum.photos/seed/3/200/200"));

    }
}