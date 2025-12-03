package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Toast를 위해 추가

import androidx.annotation.NonNull; // NonNull을 위해 추가
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

// Firebase 관련 클래스 import
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query; // 쿼리를 위해 추가
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostListActivity extends AppCompatActivity {

    private PostAdapter adapter; // com.example.lostfind.PostAdapter -> PostAdapter로 변경 (import 했으므로)
    private List<Post> postList;

    // ### Firebase 데이터베이스 참조 변수 선언 ###
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postlist);

        // ### Firebase 데이터베이스의 "posts" 경로를 참조 ###
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

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
        adapter = new PostAdapter(this, postList); // com.example.lostfind.PostAdapter -> PostAdapter
        recyclerView.setAdapter(adapter);

        // ### 데이터 로딩 함수 호출 ###
        loadPostData();
    }

    // ### loadPostData() 메서드를 Firebase에서 데이터 가져오도록 수정 ###
    private void loadPostData() {
        Query postsQuery = databaseReference.orderByChild("date");

        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(0, post); // 최신순으로 리스트에 추가
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터 로딩에 실패했을 경우 처리
                Toast.makeText(PostListActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
