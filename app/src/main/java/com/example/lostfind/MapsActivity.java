package com.example.lostfind;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.bumptech.glide.Glide;
import com.example.lostfind.databinding.NavigationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lostfind.databinding.ActivityMapsBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private NavigationBinding naviBinding;


    private DatabaseReference postsRef;
    private ValueEventListener postsListener;
    private final List<Marker> foundMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMapsBinding mapBinding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(mapBinding.getRoot());

        naviBinding = NavigationBinding.bind(findViewById(R.id.nav_view));
        loadUserInfo();

        DrawerLayout drawerLayout = mapBinding.drawerLayout;
        mapBinding.naviBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END); // 네이게이션(오른쪽으로 드로어 열기)
        });

        naviBinding.goPostlist.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, PostListActivity.class);
            startActivity(intent);
        });

        naviBinding.goChat.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, ChatRoomListActivity.class);
            startActivity(intent);
        });

        naviBinding.goMypost.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, MyPostsActivity.class);
            startActivity(intent);
        });

        naviBinding.goMyinfo.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, InfoActivity.class);
            startActivity(intent);
        });

        naviBinding.goStart.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, StartActivity.class);
            startActivity(intent);
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (naviBinding != null) {
                naviBinding.userName.setText("로그인 필요");
                naviBinding.userEmail.setText("로그인 정보가 없습니다.");
            }
            return;
        }

        user.reload().addOnCompleteListener(reloadTask -> {
            if (reloadTask.isSuccessful()) {
                FirebaseUser freshUser = FirebaseAuth.getInstance().getCurrentUser();
                if (freshUser != null) {
                    String uid = freshUser.getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
                    ref.get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);

                            if (naviBinding != null) {
                                naviBinding.userName.setText(name);
                            }
                        }
                    });

                    if (naviBinding != null) {
                        naviBinding.userEmail.setText(freshUser.getEmail());
                    }
                }
            } else {
                Log.e("MapsActivity", "사용자 정보 reload 실패", reloadTask.getException());
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        LatLng soongsil = new LatLng(37.494618, 126.959667);
        LatLng studentHall = new LatLng(37.496845, 126.956781);
        LatLng library = new LatLng(37.496306, 126.958539);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(library, 17));

        mMap.addMarker(new MarkerOptions()
                .position(studentHall).
                title("분실물보관소[학생회관406호]")
                .icon(createCircleMarker(R.drawable.studenthall, 40)));
        mMap.addMarker(new MarkerOptions()
                .position(library)
                .title("분실문보관소[도서관1층]")
                .icon(createCircleMarker(R.drawable.library, 40)));

        loadFoundPostsAndShowMarkers();

        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();

            if (tag instanceof DataSnapshot) {
                DataSnapshot snap = (DataSnapshot) tag;

                String postId = snap.getKey();
                if (postId == null) {
                    Log.e("MapsActivity", "Post ID가 null입니다.");
                    return true;
                }


                String authorId = snap.child("authorId").getValue(String.class);
                String itemName = snap.child("itemName").getValue(String.class);
                String imageUrl = snap.child("imageUrl").getValue(String.class);


                Object dateValue = snap.child("date").getValue();
                String dateString = ""; // 기본값은 빈 문자열
                if (dateValue instanceof Long) {

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA);
                    dateString = sdf.format(new java.util.Date((Long) dateValue));
                }

                PopupBottomSheet sheet = new PopupBottomSheet(postId, authorId, itemName, dateString, imageUrl);
                sheet.show(getSupportFragmentManager(), sheet.getTag());

                return true;
            }

            return false;
        });


    }
    private void loadFoundPostsAndShowMarkers() {

        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for (Marker m : foundMarkers) {
                    m.remove();
                }
                foundMarkers.clear();


                for (DataSnapshot postSnap : snapshot.getChildren()) {


                    String type = postSnap.child("type").getValue(String.class);
                    if (type == null || !type.equals("isfound")) continue;

                    String title = postSnap.child("itemName").getValue(String.class);

                    Long date = snapshot.child("date").getValue(Long.class);

                    String imageUrl = postSnap.child("imageUrl").getValue(String.class);

                    String locationStr = postSnap.child("location").getValue(String.class);
                    if (locationStr == null || !locationStr.contains("lat")) {
                        Log.e("Maps", "위치 문자열 없음: " + postSnap.getKey());
                        continue;
                    }

                    try {
                        String[] parts = locationStr.split(",");
                        String latStr = parts[0].replace("lat:", "").trim();
                        String lngStr = parts[1].replace("lng:", "").trim();

                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);

                        LatLng foundLoc = new LatLng(lat, lng);

                        addFoundItemMarker(
                                foundLoc,
                                title,
                                imageUrl,
                                postSnap
                        );


                        Log.d("Maps", "분실물 위치 표시 완료: " + lat + ", " + lng);

                    } catch (Exception e) {
                        Log.e("Maps", "위치 파싱 실패: " + locationStr);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Maps", "Firebase listen 실패: " + error.getMessage());
            }
        };

        postsRef.addValueEventListener(postsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postsRef != null && postsListener != null) {
            postsRef.removeEventListener(postsListener);
        }
    }



    //실험실
    private BitmapDescriptor createCircleMarker(int drawableResId, int sizeDp) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableResId);
        if (drawable == null) return null;

        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * density);
        float radius = sizePx / 2f;


        Bitmap srcBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(srcBitmap);
        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(srcCanvas);


        Bitmap output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);


        Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imagePaint.setShader(new BitmapShader(srcBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(radius, radius, radius, imagePaint);


        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.RED);
        strokePaint.setStrokeWidth(4f);
        canvas.drawCircle(radius, radius, radius - 2f, strokePaint);

        return BitmapDescriptorFactory.fromBitmap(output);
    }

    private void addFoundItemMarker(
            LatLng position,
            String title,
            String imageUrl,
            DataSnapshot postSnap
    ) {
        int sizeDp = 36;
        int borderColor = Color.BLUE;

        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(
                            @NonNull Bitmap resource,
                            com.bumptech.glide.request.transition.Transition<? super Bitmap> transition
                    ) {

                        BitmapDescriptor icon =
                                createCircleMarkerFromBitmap(resource, sizeDp, borderColor);

                        Marker marker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(position)
                                        .title(title)
                                        .icon(icon)
                        );

                        if (marker != null) {
                            marker.setTag(postSnap);
                            foundMarkers.add(marker);
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {}
                });
    }


    private BitmapDescriptor createCircleMarkerFromBitmap(
            Bitmap src,
            int sizeDp,
            int borderColor
    ) {
        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * density);
        float radius = sizePx / 2f;

        Bitmap scaled = Bitmap.createScaledBitmap(src, sizePx, sizePx, true);

        Bitmap output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imagePaint.setShader(
                new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        );
        canvas.drawCircle(radius, radius, radius, imagePaint);

        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(borderColor);
        strokePaint.setStrokeWidth(4f);
        canvas.drawCircle(radius, radius, radius - 2f, strokePaint);

        return BitmapDescriptorFactory.fromBitmap(output);
    }



}