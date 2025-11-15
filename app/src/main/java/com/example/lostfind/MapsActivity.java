package com.example.lostfind;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;

import com.example.lostfind.databinding.NavigationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lostfind.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMapsBinding mapBinding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(mapBinding.getRoot());

        NavigationBinding naviBinding = NavigationBinding.inflate(getLayoutInflater());
        setContentView(mapBinding.getRoot());

        DrawerLayout drawerLayout = mapBinding.drawerLayout;
        mapBinding.naviBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END); // 네이게이션(오른쪽으로 드로어 열기)
        });

        naviBinding.goPostlist.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostListActivity.class);
            startActivity(intent);
        });

        naviBinding.goMyinfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, InfoActivity.class);
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

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(soongsil, 20));

        mMap.addMarker(new MarkerOptions().position(soongsil).title("정보섬"));
    }
}