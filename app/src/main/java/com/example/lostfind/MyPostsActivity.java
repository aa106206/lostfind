package com.example.lostfind;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private PostAdapter adapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        Button backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view_my_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(adapter);

        loadMyPosts();
    }

    private void loadMyPosts() {
        //postList.add(new Post("분실물 찾음", "학생회관", "2024-11-13", "https://picsum.photos/seed/1/200/200"));
        //postList.add(new Post("분실물 찾음", "중앙도서관", "2024-11-12", "https://picsum.photos/seed/2/200/200"));
        //postList.add(new Post("분실물 찾음", "정보섬", "2024-11-11", "https://picsum.photos/seed/3/200/200"));
    }
}
