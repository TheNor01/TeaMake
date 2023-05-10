package com.example.teamake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RidesActivity extends AppCompatActivity {


    //Singleton mAuth firebase connection
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    FirebaseUser userLogged;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapterData;
    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();

    private CollectionReference Rides = FireDb.collection("Matches");

    ArrayList<RideItem> ridesList = new ArrayList<>();
    private static final String TAG = "RidesActivity";


    //Create a match

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rides_main);

        recyclerView = findViewById(R.id.ridesConfirmed);

        // get from db at the time
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        adapterData = new RidesAdapter(ridesList);

        recyclerView.setAdapter(adapterData);

        userLogged = mAuth.getCurrentUser();
        if(userLogged == null){
            Intent backToLogin =  new Intent(getApplicationContext(),MainActivity.class);
            startActivity(backToLogin);
            finish();
        }else{
            PopulateRecyclerView();
        }


    }

    protected  void PopulateRecyclerView(){

        String queryTerm = "USERS ."+mAuth.getCurrentUser().getUid();

        Log.i(TAG,"DISPLAYING ALL CONFIRMED RIDES  FOR: "+userLogged.getUid());
            Rides
                    .orderBy(queryTerm)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                if(task.getResult().isEmpty()){
                                    RideItem MI = new RideItem("dummy", R.drawable.baseline_school_24, "dummy", "2023/04/02", "00:00", R.drawable.baseline_info_24, R.drawable.baseline_notifications_24);
                                    ridesList.add(MI);
                                }
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String status = document.getString("Status"); //status match

                                    if (status.equals("Confirmed")) {

                                        Log.i(TAG,"FOUND RIDE CONFIRMED: "+document.getId());
                                        RideItem RI = CreateRide(document);
                                        //RideItem MI = new RideItem(document.getId(), imageToUse, sport, date, -1, -1, R.drawable.baseline_check_24);
                                        ridesList.add(RI);
                                        Log.i(TAG,"SIZE CONFIRMED: "+ ridesList.size());
                                    }
                                }
                                adapterData.notifyDataSetChanged();

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }

                    });
    }

    protected RideItem CreateRide(QueryDocumentSnapshot document){

        String date = document.getString("Date");
        String time = document.getString("Time");
        String destinationUniversity = document.getString("University");
        int locationInfo = Integer.parseInt(document.getString("MarkerStartingLocation"));

        return new RideItem(document.getId(), R.drawable.baseline_school_24, destinationUniversity, date, time, R.drawable.baseline_info_24, R.drawable.baseline_info_24);

    }



}
