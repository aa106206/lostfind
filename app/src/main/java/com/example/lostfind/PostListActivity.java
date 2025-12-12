package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable; // ★★★ TextWatcher를 위해 추가 ★★★
import android.text.TextWatcher;
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

    private PostAdapter adapter;
    private List<Post> allPostList; // ★★★ 1. 전체 게시물을 담을 원본 리스트 ★★★
    private List<Post> filteredPostList; // ★★★ 2. 필터링된 결과를 담을 리스트 ★★★
    private EditText searchEditText; // ★★★ 3. 검색창 변수 추가 ★★★
    private TabLayout tabLayout; // ★★★ TabLayout 변수 추가 ★★★

    // ★★★ 현재 선택된 탭을 저장할 변수 추가 (기본값: isfound) ★★★
    private String selectedTabType = "isfound";


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
        tabLayout = findViewById(R.id.tab_layout); // TabLayout 연결
        searchEditText = findViewById(R.id.search_edit_text);

        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        writePostButton.setOnClickListener(v -> {
            Intent intent = new Intent(PostListActivity.this, PostWriteActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allPostList = new ArrayList<>();
        filteredPostList = new ArrayList<>();

        adapter = new PostAdapter(this, filteredPostList);
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 선택된 탭의 위치(position)에 따라 selectedTabType 값 변경
                if (tab.getPosition() == 0) {
                    selectedTabType = "isfound"; // 첫 번째 탭: 찾은 게시판
                } else {
                    selectedTabType = "islost";  // 두 번째 탭: 찾는 게시판
                }
                // 탭이 변경되었으므로, 현재 검색어 기준으로 다시 필터링
                filter(searchEditText.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트 변경 전
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 변경될 때마다 filter() 메서드 호출
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트 변경 후
            }
        });

        // ### 데이터 로딩 함수 호출 ###
        loadPostData();
    }

    // ### loadPostData() 메서드를 Firebase에서 데이터 가져오도록 수정 ###
    private void loadPostData() {
        Query postsQuery = databaseReference.orderByChild("date");

        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allPostList.clear(); // 원본 리스트 비우기
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        allPostList.add(0, post); // 원본 리스트에 최신순으로 추가
                    }
                }

                // ★★★ 초기에 검색창이 비어있을 때의 전체 목록을 보여주기 위해 필터링 한번 실행 ★★★
                filter(searchEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostListActivity.this, "데이터 로딩 실패: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ★★★ 필터링을 수행하는 새로운 메서드 ★★★
    private void filter(String searchText) {
        filteredPostList.clear();

        // 1. 전체 원본 목록(allPostList)에서 반복
        for (Post post : allPostList) {
            // 2. 탭 조건 확인: 현재 선택된 탭(selectedTabType)과 게시물의 타입(post.getType())이 일치하는가?
            boolean isTabMatch = selectedTabType.equals(post.getType());

            // 3. 검색어 조건 확인
            boolean isSearchMatch;
            if (searchText.isEmpty()) {
                isSearchMatch = true; // 검색어가 없으면 항상 참
            } else {
                // 제목에 검색어가 포함되어 있는가? (대소문자 무시)
                isSearchMatch = post.getTitle().toLowerCase().contains(searchText.toLowerCase());
            }

            // 4. 두 조건이 모두 참(true)일 때만 필터링된 리스트에 추가
            if (isTabMatch && isSearchMatch) {
                filteredPostList.add(post);
            }
        }
        // 어댑터에 데이터 변경 알림
        adapter.notifyDataSetChanged();
    }
}
