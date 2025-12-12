package com.example.lostfind;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookmarkActivity extends AppCompatActivity {

    private static final String TAG = "BookmarkActivity";

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> bookmarkedPosts;
    private TextView emptyTextView;
    private ValueEventListener bookmarkListener;
    private DatabaseReference bookmarkRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.bookmark_recyclerview);
        emptyTextView = findViewById(R.id.empty_bookmark_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookmarkedPosts = new ArrayList<>();
        postAdapter = new PostAdapter(this, bookmarkedPosts);
        recyclerView.setAdapter(postAdapter);

        setupBookmarkListener();
    }

    private void setupBookmarkListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String currentUserId = currentUser.getUid();
        bookmarkRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(currentUserId);

        // 북마크 목록에 변경이 생길 때마다 실시간으로 데이터를 다시 불러옴
        bookmarkListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> postIds = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    postIds.add(dataSnapshot.getKey());
                }
                Collections.reverse(postIds); // 최신 북마크가 위로 오도록
                fetchPosts(postIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "북마크 ID 로딩 실패", error.toException());
            }
        };
        bookmarkRef.addValueEventListener(bookmarkListener);
    }

    private void fetchPosts(List<String> postIds) {
        bookmarkedPosts.clear();
        if (postIds.isEmpty()) {
            updateUI();
            return;
        }

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        for (String postId : postIds) {
            postsRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Post post = snapshot.getValue(Post.class);
                        if (post != null) {
                            post.setPostId(snapshot.getKey()); // Post 객체에 ID 설정
                            bookmarkedPosts.add(post);
                        }
                    }
                    // 모든 조회가 끝난 후 UI 업데이트 (안정성을 위해 개수 비교)
                    if (bookmarkedPosts.size() >= postIds.size()) {
                        updateUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "게시물 로딩 실패: " + postId, error.toException());
                }
            });
        }
    }

    private void updateUI() {
        if (bookmarkedPosts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
        postAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 리스너를 제거하여 메모리 누수 방지
        if (bookmarkRef != null && bookmarkListener != null) {
            bookmarkRef.removeEventListener(bookmarkListener);
        }
    }
}
