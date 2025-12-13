package com.example.lostfind;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.postTitle.setText(post.getTitle());

        if ("isfind".equals(post.getType())) {
            // post.type이 "isfound" (찾은 글)이면, 위치 TextView를 숨깁니다.
            holder.postLocation.setVisibility(View.GONE);
        } else {
            // 그 외의 경우 (예: "islost", 찾는 글)이면, 위치 TextView를 보여주고 텍스트를 설정합니다.
            holder.postLocation.setVisibility(View.VISIBLE);
            holder.postLocation.setText(post.getLocation());
        }

        if (post.getDate() instanceof Long) {
            long timestamp = (Long) post.getDate();
            holder.postDate.setText(formatTimestamp(timestamp));
        }

        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.postImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);

            intent.putExtra("POST_ID", post.getPostId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        // "yyyy.MM.dd" -> "yyyy.MM.dd HH:mm" 으로 변경
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        TextView postTitle;
        TextView postLocation;
        TextView postDate;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post_image);
            postTitle = itemView.findViewById(R.id.post_title);
            postLocation = itemView.findViewById(R.id.post_location);
            postDate = itemView.findViewById(R.id.post_date);
        }
    }
}
