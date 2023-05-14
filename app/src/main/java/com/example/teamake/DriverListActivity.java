package com.example.teamake;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;


public class DriverListActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference playersRef = db.collection("UserBasicInfo");
    private CollectionReference ridesRef = db.collection("Matches");
    DriversAdapter driversAdapter;
    RecyclerView driversViewList;
    ArrayList<UserItem> driversArrayList;
    HashMap<String,ArrayList<String>> mapDrivers = new HashMap<>();

    GoogleMap gMap;
    MarkerOptions marker;

    Button confirmLocationLookup;

    String UID_choosen,ride_choosen,nickname_choosen;
    int position_choosen,seats_choosen;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivers_list);


        driversViewList = findViewById(R.id.listPlayers);
        driversViewList.setHasFixedSize(true);
        driversViewList.setLayoutManager(new LinearLayoutManager(this));

        confirmLocationLookup = findViewById(R.id.confirmLocationLookup);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapDriver);

        mapFragment.getMapAsync(this);

        driversArrayList = new ArrayList<>();
//        driversAdapter = new DriversAdapter(driversArrayList);
//        driversViewList.setAdapter(driversAdapter);



        confirmLocationLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(UID_choosen != null) {
                    Intent intent = new Intent();
                    intent.putExtra("UID", UID_choosen);
                    intent.putExtra("position", position_choosen);
                    intent.putExtra("nickname", nickname_choosen);
                    intent.putExtra("seats", seats_choosen);
                    intent.putExtra("ride", ride_choosen);
                    setResult(444, intent);
                    finish();
                }else {
                    Toast.makeText(DriverListActivity.this, "SELECT AT LEAST 1 DRIVER", Toast.LENGTH_SHORT).show();
                }
            }
        });



        EventChangeListener();


    }

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
            }
        });



    }


    private void EventChangeListener(){

        //filter university

        Intent intent = getIntent();
        String University = intent.getStringExtra("University");
        String Date = intent.getStringExtra("Date");
        String Time = intent.getStringExtra("Time");


        Log.i("DriverListActivity", "Creating ride.. INFO:");
        Log.i("DriverListActivity", "Uni choosed = " + University);
        Log.i("DriverListActivity", "Date choosed = " + Date);
        Log.i("DriverListActivity", "Time choosed = " + Time);


        Query matchingRide = ridesRef
                .whereEqualTo("University", University)
                .whereEqualTo("Date", Date)
                .whereEqualTo("Time", Time)
                .whereGreaterThan("Seats",0);


        matchingRide.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().isEmpty()){

                    AlertDialog.Builder builder = new AlertDialog.Builder(DriverListActivity.this);

                    builder.setMessage("No driver or ride avaiable")
                            .setTitle("WARNING");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }


                if (task.isSuccessful()) {
                    Log.d("DriverListActivity task size", String.valueOf(task.getResult().size()));
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        //String nickname = document.getString("Nickname");
                        String rideId = document.getId();
                        String rideDriver = document.getString("Driver");
                        int freeSeats = (Integer) document.getLong("Seats").intValue();

                        ArrayList<String> tmpInfo = new ArrayList<>(3);
                        tmpInfo.add("Dummy");
                        tmpInfo.add(rideDriver);
                        tmpInfo.add(String.valueOf(freeSeats));
                        tmpInfo.add(rideId);
                        mapDrivers.put(rideDriver,tmpInfo);
                        Log.i("DriverList Activity","ADDED DRIVER to map: "+rideDriver);

                        //driversArrayList.add(PI);
                        //filterInfo.add(rideId);

                    }
                } else {
                    Log.d("DriverListActivity", "Error getting documents: ", task.getException());
                }

                if(!mapDrivers.isEmpty()) {
                    Query getNickname = playersRef
                            .whereIn(FieldPath.documentId(), new ArrayList<>(mapDrivers.keySet()));

                    getNickname.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.getResult().isEmpty()) {
                                Log.d("DriverListActivity", "size info users empty");
                                finish();
                            }
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String nickname = document.getString("Nickname");

                                    ArrayList<String> localInfo = new ArrayList<>();
                                    localInfo = mapDrivers.get(document.getId());
                                    localInfo.set(0, nickname);

                                    Log.d("DriverList info", "UID:" + document.getId() + " with info:" + localInfo);

                                    UserItem PI = new UserItem(R.drawable.baseline_person_24, localInfo.get(0), localInfo.get(1), Integer.valueOf(localInfo.get(2)));
                                    driversArrayList.add(PI);
                                }
                            } else {
                                Log.d("DriverListActivity", "Error getting documents INFO: ", task.getException());
                            }
                            buildRecyclerView();
                        }
                    });
                }
            }
        });

    }

    public void buildRecyclerView() {

        driversViewList.setHasFixedSize(true);
        driversAdapter = new DriversAdapter(driversArrayList);
        driversViewList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        driversViewList.setAdapter(driversAdapter);

        driversAdapter.setOnItemClickLister(new DriversAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                driversArrayList.get(position).setImageToPlayersPending();
                driversAdapter.notifyItemChanged(position);

                //ride id position


                String localUid = driversArrayList.get(position).getUID();
                String localNickname = driversArrayList.get(position).getNicknameText();
                int seats = driversArrayList.get(position).getFreeSeats();

                System.out.println(position);
                String rideId = mapDrivers.get(localUid).get(3);
                System.out.println(rideId);

                Log.i("DriverList","Inviting... ="+localUid + "with Nickname:"+localNickname+ " - seats:"+seats + "- Offers:"+rideId) ;

                Bundle extras = getIntent().getExtras();

                String localPosition = extras.get("position").toString();
                Log.i("DriverList",  "calling intent position: "+localPosition);

                UID_choosen = localUid;
                position_choosen = position;
                nickname_choosen = localNickname;
                seats_choosen = seats;
                ride_choosen = rideId;

                populateMarkerMap(rideId);

                //finish();
            }

            private void populateMarkerMap(String rideId) {

                ridesRef.whereEqualTo(FieldPath.documentId(),rideId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful() && !task.getResult().isEmpty()){
                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        GeoPoint geoDb = document.getGeoPoint("geoMarker");
                                        Log.i("DriverList","FOUND GEOPOINT" + geoDb);

                                        MarkerOptions mopt = new MarkerOptions();
                                        LatLng latLng = new LatLng(geoDb.getLatitude(),geoDb.getLongitude());
                                        mopt.position(latLng);
                                        mopt.title(latLng.latitude+ ":"+latLng.longitude);
                                        gMap.clear();

                                        //zoom level 10
                                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
                                        gMap.addMarker(mopt);

                                    }
                                }else {
                                    Log.d("DriverListActivity", "Error getting GEOPOINT ", task.getException());
                                }
                            }
                        });

            }
        });

    }



}
