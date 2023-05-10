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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;



public class DriverListActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference playersRef = db.collection("UserBasicInfo");
    private CollectionReference ridesRef = db.collection("Matches");
    DriversAdapter driversAdapter;
    RecyclerView driversViewList;
    ArrayList<UserItem> driversArrayList;


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
                .whereEqualTo("University", University).whereEqualTo("Date", Date).whereEqualTo("Time", Time)
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
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String nickname = document.getString("Nickname");
                        String UID = document.getId();
                        int freeSeats = Integer.parseInt(document.getString("FreeSeats"));

                        UserItem PI = new UserItem(R.drawable.baseline_person_24,nickname,UID,freeSeats);
                        Log.i("DriverList Activity","ADDED DRIVER:"+PI.getUID());

                        driversArrayList.add(PI);
                    }
                } else {
                    Log.d("DriverListActivity", "Error getting documents: ", task.getException());
                }
                buildRecyclerView();

            }
        });

    }

    public void buildRecyclerView() {
        driversViewList = findViewById(R.id.listPlayers);

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

                Log.i("DriverList","Inviting... ="+localUid);


                Bundle extras = getIntent().getExtras();

                String localPosition = extras.get("position").toString();
                Log.i("DriverList",  "calling intent position: "+localPosition);



                Intent intent = new Intent();
                intent.putExtra("UID", localUid);
                intent.putExtra("position",localPosition);
                intent.putExtra("nickname",localNickname);
                setResult(444, intent);
                finish();
            }
        });



    }

}
