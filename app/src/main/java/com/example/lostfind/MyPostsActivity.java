package com.example.lostfind;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.Toast; // Toast를 위해 추가

import androidx.annotation.NonNull; // NonNull을 위해 추가
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private PostAdapter adapter;
    private List<Post> postList;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        ImageButton backButton = findViewById(R.id.back_button);

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
        // ### "posts" 경로에서 authorId 필드가 현재 내 ID와 같은 데이터만 조회하는 쿼리 생성 ###
        Query myPostsQuery = databaseReference.orderByChild("authorId").equalTo(currentUserId);

        // 생성한 쿼리에 리스너를 붙여 데이터 가져오기
        myPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear(); // 기존 목록을 비워 중복 로딩 방지
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        // 최신 글이 위로 오게 하려면 리스트의 맨 앞에 추가
                        postList.add(0, post);
                    }
                }
                adapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터 로딩 실패 시 처리
                Toast.makeText(MyPostsActivity.this, "내 게시물 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
