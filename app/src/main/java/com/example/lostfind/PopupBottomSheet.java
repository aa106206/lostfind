package com.example.lostfind;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PopupBottomSheet extends BottomSheetDialogFragment {
    private String postId;
    private String itemName;
    private String date;
    private String imageUrl;
    View view;

    public PopupBottomSheet(String postId, String itemName, String date, String imageUrl) {
        this.postId=postId;
        this.itemName = itemName;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.popup, container, false);

        ImageView imageView = view.findViewById(R.id.imgView);
        TextView itemNameTextView = view.findViewById(R.id.titleText);
        TextView dateTextView = view.findViewById(R.id.dateText);

        itemNameTextView.setText(itemName);
        dateTextView.setText(date);
        Glide.with(requireContext()).load(imageUrl).into(imageView);  //popup 이미지로딩 부분

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button goPostBtn = view.findViewById(R.id.button);
        ImageButton messageBtn = view.findViewById(R.id.button2);


        // 게시글 상세로 이동
        goPostBtn.setOnClickListener(v -> {
            Log.d("PopupBottomSheet", "#########게시글이동버튼1#########");
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra("POST_ID", postId);
            Log.d("PopupBottomSheet", "#########게시글이동버튼2#########");
            startActivity(intent);
            Log.d("PopupBottomSheet", "#########게시글이동버튼3#########");
            dismiss();
        });

        // 쪽지 보내기
        messageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChatRoom.class);
            intent.putExtra("postId", postId);
            startActivity(intent);
            dismiss();
        });

    }
}