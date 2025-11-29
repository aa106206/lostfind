package com.example.lostfind;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PopupBottomSheet extends BottomSheetDialogFragment {
    private String itemName;
    private String date;
    private String imageUrl;

    public PopupBottomSheet(String itemName, String date, String imageUrl) {
        this.itemName = itemName;
        this.date = date;
        this.imageUrl = imageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup, container, false);

        ImageView imageView = view.findViewById(R.id.imgView);
        TextView itemNameTextView = view.findViewById(R.id.titleText);
        TextView dateTextView = view.findViewById(R.id.dateText);

        itemNameTextView.setText(itemName);
        dateTextView.setText(date);
        Glide.with(requireContext()).load(imageUrl).into(imageView);

        return view;
    }

}