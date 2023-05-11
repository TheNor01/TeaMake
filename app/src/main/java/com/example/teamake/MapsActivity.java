package com.example.teamake;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap gMap;
    MarkerOptions marker;
    LatLng latLngClass = null;

    TextView welcomeLocation;
    Button confirMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.maps_main);

        confirMarker = findViewById(R.id.confirmMarker);
        welcomeLocation = findViewById(R.id.textViewLocation);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        confirMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(latLngClass==null) {
                    Toast.makeText(MapsActivity.this,"PLEASE PLACE 1 MARKER",Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("Lat", latLngClass.latitude);
                intent.putExtra("Lng",latLngClass.longitude);
                setResult(555, intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // es LatLng sydney = new LatLng(-33.852, 151.211);
                MarkerOptions mopt = new MarkerOptions();
                mopt.position(latLng);
                mopt.title(latLng.latitude+ ":"+latLng.longitude);
                gMap.clear();

                //zoom level 10
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
                gMap.addMarker(mopt);
                marker = mopt;
                latLngClass = latLng;
            }
        });



    }
}
