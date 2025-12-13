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
        // 다른 화면에 갔다가 이 화면으로 돌아올 때마다 사용자 정보를 새로고침합니다.
        loadUserInfo();
    }

    // --- 추가: 사용자 정보를 불러오는 메소드 ---
    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // 로그인 되어있지 않은 경우 처리 (예: 로그인 화면으로 이동)
            // 이 예제에서는 간단히 리턴합니다.
            return;
        }

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                // naviBinding이 null이 아닐 때만 UI 업데이트
                if (naviBinding != null) {
                    naviBinding.userName.setText(name);
                    naviBinding.userEmail.setText(email);
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng soongsil = new LatLng(37.494618, 126.959667);
        LatLng studentHall = new LatLng(37.496845, 126.956781);
        LatLng library = new LatLng(37.496306, 126.958539);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(library, 17));

//        mMap.addMarker(new MarkerOptions().position(soongsil).title("분실물보관소[정보과학관]"));
        mMap.addMarker(new MarkerOptions()
                .position(studentHall).
                title("분실물보관소[학생회관406호]")
                .icon(createCircleMarker(R.drawable.studenthall, 40)));
        mMap.addMarker(new MarkerOptions()
                .position(library)
                .title("분실문보관소[도서관1층]")
                .icon(createCircleMarker(R.drawable.library, 40)));

        //습득 게시물 마커 표시
        loadFoundPostsAndShowMarkers();


        //popup을 위해서 마커에 클릭리스너 달기
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();

            if (tag instanceof DataSnapshot) {
                DataSnapshot snap = (DataSnapshot) tag;

                String postId = snap.getKey();
                if (postId == null) {
                    Log.e("MapsActivity", "Post ID가 null입니다.");
                    return true; // postId가 없으면 더 이상 진행하지 않음
                }

                // 2. 나머지 데이터들을 올바른 타입으로 가져옵니다.
                String authorId = snap.child("authorId").getValue(String.class);
                String itemName = snap.child("itemName").getValue(String.class);
                String imageUrl = snap.child("imageUrl").getValue(String.class);

                // 3. 날짜(date)는 Long 타입으로 가져와서 String으로 변환합니다.
                Object dateValue = snap.child("date").getValue();
                String dateString = ""; // 기본값은 빈 문자열
                if (dateValue instanceof Long) {
                    // SimpleDateFormat을 사용하여 원하는 형식의 문자열로 변환
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.KOREA);
                    dateString = sdf.format(new java.util.Date((Long) dateValue));
                }

                // 4. 생성자에 올바른 데이터를 전달합니다.
                PopupBottomSheet sheet = new PopupBottomSheet(postId, authorId, itemName, dateString, imageUrl);
                sheet.show(getSupportFragmentManager(), sheet.getTag());

            }

            return true; // 기본 동작(카메라 이동) 막기
        });


    }
    private void loadFoundPostsAndShowMarkers() {

        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // 1) 기존 습득물 마커 지우기
                for (Marker m : foundMarkers) {
                    m.remove();
                }
                foundMarkers.clear();

                // 2) 새 데이터로 마커 다시 그리기
                for (DataSnapshot postSnap : snapshot.getChildren()) {

                    // type이 isfound인 게시물만 지도에 표시
                    String type = postSnap.child("type").getValue(String.class);
                    if (type == null || !type.equals("isfound")) continue;

                    String title = postSnap.child("itemName").getValue(String.class);
//                    String date = postSnap.child("date").getValue(String.class);
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

                        Marker marker = mMap.addMarker(
                                new MarkerOptions()
                                        .position(foundLoc)
                                        .title(title)
                        );

                        // BottomSheet에서 쓰려고 tag로 postSnap 저장
                        marker.setTag(postSnap);

                        // 나중에 지우기 위해 리스트에 보관
                        foundMarkers.add(marker);

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

        // 1️⃣ Drawable → Bitmap
        Bitmap srcBitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(srcBitmap);
        drawable.setBounds(0, 0, sizePx, sizePx);
        drawable.draw(srcCanvas);

        // 2️⃣ 원형 Bitmap
        Bitmap output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // 🔹 이미지 원형 채우기
        Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        imagePaint.setShader(new BitmapShader(srcBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(radius, radius, radius, imagePaint);

        // 🔹 흰색 테두리
        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(4f);
        canvas.drawCircle(radius, radius, radius - 2f, strokePaint);

        return BitmapDescriptorFactory.fromBitmap(output);
    }




}