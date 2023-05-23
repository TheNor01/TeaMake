package com.example.teamake;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LookupRideActivity extends AppCompatActivity {


    //populated by inviting
    ArrayList<UserItem> driverListToSend = new ArrayList<>();
    
    private RecyclerView mRecyclerViewList1;
    private DriversAdapter mAdapter1;

    Button sendRequestRide;
    TextView datePicker,timePickerView,choosedUniversity;

    int hour,minute;

    Map<String, Object> matchMap = new HashMap<>();
    ArrayList<String> driversUIDs;

    private DatePickerDialog.OnDateSetListener dateListener;

    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser userLogged;

    String rideToConfirm  = "";

    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),  new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Log.i("LookupRide -- Activity result CODE", String.valueOf(result.getResultCode()));

                    //Driver lookup
                    if(result.getResultCode() == 444) {
                        String UID,nickname,rideId;
                        int position;
                        int seats;
                        Intent data = result.getData();
                        if(data != null) {
                            System.out.println(data);
                            UID = data.getStringExtra("UID");
                            nickname = data.getStringExtra("nickname");
                            rideId = data.getStringExtra("ride");
                            seats = data.getIntExtra("seats",-1);
                            position = data.getIntExtra("position",-1);
                            
                            Log.i("LookupRide -- results Driver:",UID+" - at "+position+ " - Free seats:"+ seats + " - rideId: "+rideId);

                            driverListToSend.get(position).setNicknameToLooking(nickname);
                            driverListToSend.get(position).setUID(UID);
                            driverListToSend.get(position).setImageToPlayersPending();
                            driverListToSend.get(position).setSeats(seats);
                            rideToConfirm = rideId;

                            mAdapter1.notifyItemChanged(position);
                        }
                    }
                }
            });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.looking_for_ride);

        userLogged = auth.getCurrentUser();

        if(userLogged == null) {
            Intent backToLogin = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(backToLogin);
            finish();
        }

        datePicker = findViewById(R.id.tvDate);
        timePickerView = findViewById(R.id.tvTime);
        sendRequestRide = findViewById(R.id.sendRequestButton);
        choosedUniversity = findViewById(R.id.textViewUniversity);

        populateUniversity();


        buildRecyclerView();

        datePicker.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(LookupRideActivity.this,
                    android.R.style.Theme_DeviceDefault_Dialog_MinWidth,dateListener, year,month,day);
            dialog.show();
        });

        dateListener = (datePickerLocal, year, month, day) -> {
            Log.d("LookupRide", "onDateSet: dd/mm/yyy: " + day + "/" + month + "/" + year);
            month ++;  // from 0 to 11
            String date = day + "/" + month + "/" + year;
            datePicker.setText(date);
        };

        // TIME SECTION //
        timePickerView.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(LookupRideActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourSel, int minutesSel) {
                            hour = hourSel;
                            minute = minutesSel;

                            Log.d("LookupRide", hour + ":"+minute);
                            timePickerView.setText(hour+":"+minute);

                        }
                    },24,0,true);

            timePickerDialog.updateTime(hour,minute);
            timePickerDialog.show();
        });

        //send driver notification about new request
        sendRequestRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                driversUIDs = new ArrayList<>();
                for(UserItem pi : driverListToSend){
                    if(pi.getUID().equals("Dummy")){
                        Log.i("LookupRide","Rider: "+pi.getUID());
                        Toast.makeText(LookupRideActivity.this, "Select at least one ride", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else driversUIDs.add(pi.getUID());
                }

                modifyPassengersRide(rideToConfirm);
                sendPlayerNotification(rideToConfirm);

            }
        });

    }

    private void modifyPassengersRide(String rideToConfirm) {

        Map<String, String> passengers = new HashMap<>();
        passengers.put(userLogged.getUid(), "notAcceptedRide");

        FireDb.collection("Rides")
                .document(rideToConfirm)
                .update("Passengers."+userLogged.getUid(),"notAcceptedRide")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Log.i("LookupRide", "ADDED passengers to list");
                        }
                    }
                });
    }

    private void populateUniversity() {

        FireDb.collection("UserBasicInfo")
                .whereEqualTo(FieldPath.documentId(),userLogged.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String uniLogged = document.getString("University");
                                choosedUniversity.setText(uniLogged);
                                Log.i("Lookup", "university:"+ uniLogged, task.getException());
                            }
                        }
                        else {
                            Log.d("Lookup", "get university failed ", task.getException());
                        }

                    }
                });

    }


    public void buildRecyclerView() {
        mRecyclerViewList1 = findViewById(R.id.listPlayer1);
        mRecyclerViewList1.setHasFixedSize(true);
        mAdapter1 = new DriversAdapter(driverListToSend);

        mRecyclerViewList1.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        mRecyclerViewList1.setAdapter(mAdapter1);
        driverListToSend.add(new UserItem(R.drawable.baseline_group_add_24,"Driver","Dummy",-1));

        mAdapter1.setOnItemClickLister(new DriversAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                String choosedDate = datePicker.getText().toString();
                String choosedTime = timePickerView.getText().toString();

                if(choosedDate.isEmpty() || !choosedDate.matches(".*\\d.*") || choosedTime.isEmpty() || !choosedTime.matches(".*\\d.*") ){
                    Toast.makeText(LookupRideActivity.this, "Choose a valid date and time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                driverListToSend.get(position).setNicknameToLooking("Looking for..");
                mAdapter1.notifyItemChanged(position);

                Intent inviteDrivers = new Intent(getApplicationContext(), DriverListActivity.class);
                inviteDrivers.putExtra("position",position);
                inviteDrivers.putExtra("Date",choosedDate);
                inviteDrivers.putExtra("Time",choosedTime);
                inviteDrivers.putExtra("University",choosedUniversity.getText());
                activityResultLauncher.launch(inviteDrivers);

            }
        });
    }


    protected void sendPlayerNotification(String id_ride){
        for(String uid: driversUIDs) {
            if(uid.equals("Dummy")) {
                continue;
            }
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("id_ride", id_ride);
            localMap.put("UID", uid);
            localMap.put("status", "unread");
            localMap.put("passenger", userLogged.getUid());

            FireDb.collection("Notifications").add(localMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("LookupRide", "DocumentSnapshot Notification written with ID: " + documentReference.getId());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("LookupRide", "Error adding notifications ", e);
                        }
                    });
        }

        finish();
    }
}
