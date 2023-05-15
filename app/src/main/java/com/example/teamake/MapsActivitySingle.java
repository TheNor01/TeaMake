package com.example.teamake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivitySingle extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap gMap;
    MarkerOptions marker;
    LatLng latLngClass = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.maps_single_main);


        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapSingle);

        mapFragment.getMapAsync(this);

        Log.i("SINGLE MAIN MAP","Lookup intent");
        Intent intent = getIntent();
        Double Lat = intent.getDoubleExtra("Lat",0.00);
        Double Lng = intent.getDoubleExtra("Lng",0.00);

        if(Lng!=0.00 && Lat != 0.00){
            latLngClass = new LatLng(Lat,Lng);
        }else {
            Log.i("SINGLE MAIN MAP","Lookup intent EMPTY");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        MarkerOptions mopt = new MarkerOptions();
        mopt.position(latLngClass);
        gMap.clear();

        //zoom level 10
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngClass,14));
        gMap.addMarker(mopt);
        marker = mopt;
    }
}
