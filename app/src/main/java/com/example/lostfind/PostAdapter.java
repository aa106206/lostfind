package com.example.lostfind;

import android.content.Context;
import android.content.Intent;
import android.location.Address; // ★★★ Address import 추가 ★★★
import android.location.Geocoder; // ★★★ Geocoder import 추가 ★★★
import android.util.Log; // 로그 확인을 위해 추가
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;
    private final Geocoder geocoder;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.geocoder = new Geocoder(context, Locale.KOREAN);
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

        if ("islost".equals(post.getType())) {
            // "찾는 글(isLost)"일 경우: 위치 정보 숨김
            holder.postLocation.setVisibility(View.GONE);
            holder.postLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        } else { // "찾은 글(isfound)"일 경우
            holder.postLocation.setVisibility(View.VISIBLE);
            holder.postLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0);

            // 위도, 경도 정보가 있는지 확인
            if (post.getLatitude() != 0 && post.getLongitude() != 0) {
                // Geocoder를 사용하여 위도/경도를 주소로 변환
                getAddressFromLatLng(post.getLatitude(), post.getLongitude(), holder.postLocation);
            } else {
                // 위치 정보가 없는 '찾은 글'일 경우
                holder.postLocation.setText("위치 정보 없음");
            }
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

    private void getAddressFromLatLng(double latitude, double longitude, TextView targetTextView) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // StringBuilder를 사용하여 null이 아닌 주소 정보만 안전하게 조합
                StringBuilder addressBuilder = new StringBuilder();

                // 시/도 (예: 서울특별시)
                if (address.getAdminArea() != null) {
                    addressBuilder.append(address.getAdminArea());
                }

                // 시/군/구 (예: 동작구) - AdminArea와 같지 않을 때만 추가
                if (address.getLocality() != null && !address.getLocality().equals(address.getAdminArea())) {
                    if (addressBuilder.length() > 0) addressBuilder.append(" ");
                    addressBuilder.append(address.getLocality());
                }

                // 동/읍/면/도로명 (예: 상도동)
                if (address.getThoroughfare() != null) {
                    if (addressBuilder.length() > 0) addressBuilder.append(" ");
                    addressBuilder.append(address.getThoroughfare());
                }

                targetTextView.setText(addressBuilder.toString());

            } else {
                targetTextView.setText("주소 정보 없음");
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Geocoder 사용 실패", e);
            targetTextView.setText("위치 변환 실패");
        }
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
