package com.example.lostfind;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GeminiLostPostLoader extends AppCompatActivity {
    public interface OnLostPostsLoaded {
        void onSuccess(List<Post> posts);
        void onError(String error);
    }


    public static void loadLostPosts(OnLostPostsLoaded listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");

        ref.orderByChild("type").equalTo("islost")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Post> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Post p = child.getValue(Post.class);
                            if (p != null) list.add(p);
                        }
                        listener.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onError(error.getMessage());
                    }
                });
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_gemini_lost_post_loader);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
}