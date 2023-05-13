package com.example.teamake;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;


public class DriverListActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference playersRef = db.collection("UserBasicInfo");
    private CollectionReference ridesRef = db.collection("Matches");
    DriversAdapter driversAdapter;
    RecyclerView driversViewList;
    ArrayList<UserItem> driversArrayList;
    HashMap<String,ArrayList<String>> mapDrivers = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivers_list);


        driversViewList = findViewById(R.id.listPlayers);
        driversViewList.setHasFixedSize(true);
        driversViewList.setLayoutManager(new LinearLayoutManager(this));

        driversArrayList = new ArrayList<>();
        driversAdapter = new DriversAdapter(driversArrayList);
        driversViewList.setAdapter(driversAdapter);

        EventChangeListener();


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
                        mapDrivers.put(rideDriver,tmpInfo);
                        Log.i("DriverList Activity","ADDED DRIVER to map: "+rideDriver);

                        //driversArrayList.add(PI);
                        //filterInfo.add(rideId);

                    }
                } else {
                    Log.d("DriverListActivity", "Error getting documents: ", task.getException());
                }

                Query getNickname = playersRef
                        .whereIn(FieldPath.documentId(),new ArrayList<String>(mapDrivers.keySet()));

                getNickname.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().isEmpty()) {
                            Log.d("DriverListActivity", "size info users empty");
                            finish();
                        }
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String nickname = document.getString("Nickname");

                                ArrayList<String> localInfo = new ArrayList<>();
                                localInfo = mapDrivers.get(document.getId());
                                localInfo.set(0,nickname);

                                Log.d("DriverList info", "UID:"+document.getId()+" with info:"+localInfo);

                                UserItem PI = new UserItem(R.drawable.baseline_person_24,localInfo.get(0),localInfo.get(1),Integer.valueOf(localInfo.get(2)));
                                driversArrayList.add(PI);
                            }
                        }else {
                            Log.d("DriverListActivity", "Error getting documents INFO: ", task.getException());
                        }
                        buildRecyclerView();
                    }
                });
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

                String localUid = driversArrayList.get(position).getUID();
                String localNickname = driversArrayList.get(position).getNicknameText();
                int seats = driversArrayList.get(position).getFreeSeats();

                Log.i("DriverList","Inviting... ="+localUid + "with Nickname:"+localNickname+ " - seats:"+seats) ;

                Bundle extras = getIntent().getExtras();

                String localPosition = extras.get("position").toString();
                Log.i("DriverList",  "calling intent position: "+localPosition);

                Intent intent = new Intent();
                intent.putExtra("UID", localUid);
                intent.putExtra("position",localPosition);
                intent.putExtra("nickname",localNickname);
                intent.putExtra("seats",seats);
                setResult(444, intent);
                finish();
            }
        });



    }

}
