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
        // "posts" 경로의 데이터를 "timestamp" 필드를 기준으로 정렬하여 가져오는 쿼리 생성
        // 이렇게 하면 최신순으로 데이터를 받아올 준비가 됩니다.
        Query postsQuery = databaseReference.orderByChild("timestamp");

        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear(); // 기존 목록을 비워 중복 로딩 방지
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        // 최신 글이 목록의 맨 위로 오게 하려면 리스트의 0번째 인덱스에 추가
                        postList.add(0, post);
                    }
                }
                adapter.notifyDataSetChanged(); // 어댑터에 데이터가 변경되었음을 알림
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터 로딩에 실패했을 경우 처리
                Toast.makeText(PostListActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
