package com.example.lostfind;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.example.lostfind.databinding.NavigationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lostfind.databinding.ActivityMapsBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMapsBinding mapBinding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(mapBinding.getRoot());

        NavigationBinding naviBinding = NavigationBinding.bind(findViewById(R.id.nav_view));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.get().addOnSuccessListener(snapshot -> {
            String name = snapshot.child("name").getValue(String.class);
            String email = snapshot.child("email").getValue(String.class);
            naviBinding.userName.setText(name);
            naviBinding.userEmail.setText(email);
        });


        DrawerLayout drawerLayout = mapBinding.drawerLayout;
        mapBinding.naviBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END); // 네이게이션(오른쪽으로 드로어 열기)
        });

        naviBinding.goPostlist.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostListActivity.class);
            startActivity(intent);
        });

        naviBinding.goChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatRoomListActivity.class);
            startActivity(intent);
        });

        naviBinding.goMypost.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPostsActivity.class);
            startActivity(intent);
        });

        naviBinding.goMyinfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, InfoActivity.class);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng soongsil = new LatLng(37.494618, 126.959667);
        LatLng studentHall = new LatLng(37.496845, 126.956781);
        LatLng library = new LatLng(37.496306, 126.958539);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(library, 17));

//        mMap.addMarker(new MarkerOptions().position(soongsil).title("분실물보관소[정보과학관]"));
        mMap.addMarker(new MarkerOptions().position(studentHall).title("분실물보관소[학생회관406호]"));
        mMap.addMarker(new MarkerOptions().position(library).title("분실문보관소[도서관1층]"));

        //습득 게시물 마커 표시
        loadFoundPostsAndShowMarkers();
    }


    private void loadFoundPostsAndShowMarkers() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");

        ref.get().addOnSuccessListener(snapshot -> {

            for (DataSnapshot postSnap : snapshot.getChildren()) {

                // type이 isfound인 게시물만 지도에 표시
                String type = postSnap.child("type").getValue(String.class);
                if (type == null || !type.equals("isfound")) continue;

                // 제목
                String title = postSnap.child("itemName").getValue(String.class);

                // location 문자열 가져오기
                String locationStr = postSnap.child("location").getValue(String.class);

                if (locationStr == null || !locationStr.contains("위도")) {
                    Log.e("Maps", "위치 문자열 없음: " + postSnap.getKey());
                    continue;
                }

                try {
                    // "위도: 37.5595, 경도: 126.9691" → split
                    String[] parts = locationStr.split(","); // ["위도: 37.5595", " 경도: 126.9691"]

                    String latStr = parts[0].replace("위도:", "").trim();   // "37.5595"
                    String lngStr = parts[1].replace("경도:", "").trim();   // "126.9691"

                    double lat = Double.parseDouble(latStr);
                    double lng = Double.parseDouble(lngStr);

                    LatLng foundLoc = new LatLng(lat, lng);

                    // 지도에 마커 추가
                    mMap.addMarker(new MarkerOptions()
                            .position(foundLoc)
                            .title("습득물: " + title)
                    );

                } catch (Exception e) {
                    Log.e("Maps", "위치 파싱 실패: " + locationStr);
                }
            }

        }).addOnFailureListener(e -> {
            Log.e("Maps", "Firebase 불러오기 실패: " + e.getMessage());
        });
    }





}