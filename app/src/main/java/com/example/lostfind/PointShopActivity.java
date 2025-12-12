package com.example.lostfind;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostfind.databinding.PointshopBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PointShopActivity extends AppCompatActivity {

    private static final String TAG = "PointShopActivity";
    private PointshopBinding binding;

    private DatabaseReference userRef; // user의 전체 경로 참조
    private ValueEventListener pointValueEventListener;
    private long currentUserPoint = 0; // 현재 포인트를 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = PointshopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        loadUserPoint();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.btnBack.setOnClickListener(view -> finish());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<ShopItem> shopItemList = new ArrayList<>();
        shopItemList.add(new ShopItem(R.drawable.gift1, "문화상품권 5천원권", 5000));
        shopItemList.add(new ShopItem(R.drawable.gift2, "스타벅스 아메리카노", 4500));
        shopItemList.add(new ShopItem(R.drawable.gift3, "편의점 상품권 5천원권", 5000));
        shopItemList.add(new ShopItem(R.drawable.gift4, "배달의민족 1만원 쿠폰", 10000));

        PointShopAdapter adapter = new PointShopAdapter(shopItemList, this::onItemClick);
        binding.productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.productsRecyclerView.setAdapter(adapter);
    }

    // 아이템 클릭 시 호출될 메소드
    private void onItemClick(ShopItem item) {
        // 포인트가 부족하면 확인 다이얼로그조차 띄우지 않음
        if (currentUserPoint < item.getPrice()) {
            Toast.makeText(this, "포인트가 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        // 구매 확인 다이얼로그를 띄우는 메소드 호출
        showPurchaseConfirmDialog(item);
    }

    // ▼▼▼▼▼ 1. 구매 확인 다이얼로그를 보여주는 새로운 메소드 ▼▼▼▼▼
    private void showPurchaseConfirmDialog(ShopItem item) {
        // AlertDialog.Builder를 사용하여 다이얼로그 생성
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("상품 구매") // 다이얼로그 제목
                .setMessage("'"+ item.getTitle() + "' 상품을 정말로 구매하시겠습니까?") // 메시지
                .setIcon(R.drawable.reward) // 아이콘 설정 (선물상자 아이콘 예시)
                .setPositiveButton("예", (dialog, which) -> {
                    // '예' 버튼을 눌렀을 때만 포인트 차감 로직 실행
                    proceedWithPurchase(item);
                })
                .setNegativeButton("아니오", null) // '아니오' 버튼은 아무 동작 없이 다이얼로그를 닫음
                .show();
    }

    // ▼▼▼▼▼ 2. 기존 onItemClick의 로직을 담당하는 새로운 메소드 ▼▼▼▼▼
    private void proceedWithPurchase(ShopItem item) {
        // Firebase Transaction을 사용하여 안전하게 포인트 차감
        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long point = mutableData.child("point").getValue(Long.class);
                if (point == null || point < item.getPrice()) {
                    // 트랜잭션 도중 포인트가 부족해진 경우 중단
                    return Transaction.abort();
                }

                // 포인트 차감
                long newPoint = point - item.getPrice();
                mutableData.child("point").setValue(newPoint);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(PointShopActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Point transaction failed.", error.toException());
                } else if (committed) {
                    // 트랜잭션 성공 시 기존 구매 완료 다이얼로그 표시
                    Toast.makeText(PointShopActivity.this, item.getTitle() + " 구매 성공!", Toast.LENGTH_SHORT).show();
                    showPurchaseDialog(item);
                } else {
                    // 트랜잭션 실패 (그 사이 다른 기기에서 포인트를 써서 부족해진 경우 등)
                    Toast.makeText(PointShopActivity.this, "포인트가 부족하여 구매에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // 구매 완료 다이얼로그 표시
    private void showPurchaseDialog(ShopItem item) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_purchase_complete);

        // 다이얼로그 배경을 투명하게, 둥근 모서리 배경이 보이도록 설정
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView purchasedImage = dialog.findViewById(R.id.iv_purchased_item);
        TextView purchasedTitle = dialog.findViewById(R.id.tv_purchased_item_name);
        Button closeButton = dialog.findViewById(R.id.btn_dialog_close);
        Button saveButton = dialog.findViewById(R.id.btn_dialog_save_image);


        purchasedImage.setImageResource(item.getImageResId());
        purchasedTitle.setText(item.getTitle());

        saveButton.setOnClickListener(v -> {
            saveImageToGallery(item);
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void loadUserPoint() {
        pointValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long pointValue = snapshot.child("point").getValue(Long.class);
                currentUserPoint = (pointValue != null) ? pointValue : 0; // 현재 포인트 업데이트

                String formattedPoint = NumberFormat.getNumberInstance(Locale.US).format(currentUserPoint);
                binding.tvCurrentPoint.setText(formattedPoint + " P");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "포인트 정보를 불러오는 데 실패했습니다.", error.toException());
            }
        };
        userRef.addValueEventListener(pointValueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && pointValueEventListener != null) {
            userRef.removeEventListener(pointValueEventListener);
        }
    }

    private void saveImageToGallery(ShopItem item) {
        // 1. 리소스 이미지를 비트맵으로 변환
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), item.getImageResId());
        OutputStream fos; // 파일을 쓰기 위한 스트림

        try {
            // 2. 안드로이드 버전에 따라 저장 방식 분기
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 안드로이드 10 (API 29) 이상
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, item.getTitle() + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                // ▼▼▼▼▼ 수정된 부분 ▼▼▼▼▼
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
                // 안드로이드 9 (API 28) 이하
                // ▼▼▼▼▼ 수정된 부분 ▼▼▼▼▼
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File image = new File(imagesDir, item.getTitle() + ".jpg");
                fos = new FileOutputStream(image);

                // 갤러리 앱에 즉시 보이도록 미디어 스캔 요청
                MediaScannerConnection.scanFile(this, new String[]{image.getPath()}, null, null);
            }

            // 3. 비트맵을 JPEG 파일로 압축하여 저장
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                Toast.makeText(this, "갤러리에 이미지를 저장했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                throw new IOException("파일 스트림을 열 수 없습니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    interface OnItemClickListener {
        void onItemClick(ShopItem item);
    }

    class PointShopAdapter extends RecyclerView.Adapter<PointShopAdapter.ShopViewHolder> {

        private final List<ShopItem> items;
        private final OnItemClickListener listener;

        public PointShopAdapter(List<ShopItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_item, parent, false);
            return new ShopViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
            ShopItem currentItem = items.get(position);
            holder.bind(currentItem, listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ShopViewHolder extends RecyclerView.ViewHolder {
            ImageView itemImage;
            TextView itemTitle;
            TextView itemDescription;

            public ShopViewHolder(@NonNull View itemView) {
                super(itemView);
                itemImage = itemView.findViewById(R.id.itemImage);
                itemTitle = itemView.findViewById(R.id.itemTitle);
                itemDescription = itemView.findViewById(R.id.itemDescription);
            }

            public void bind(final ShopItem item, final OnItemClickListener listener) {
                itemImage.setImageResource(item.getImageResId());
                itemTitle.setText(item.getTitle());
                String priceText = NumberFormat.getNumberInstance(Locale.US).format(item.getPrice()) + " P";
                itemDescription.setText(priceText);

                // 아이템 뷰 전체에 클릭 리스너 설정
                itemView.setOnClickListener(v -> listener.onItemClick(item));
            }
        }
    }
}
