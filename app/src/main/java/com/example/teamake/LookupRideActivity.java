package com.example.teamake;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LookupRideActivity extends AppCompatActivity {

    ArrayList<UserItem> teamList1= new ArrayList<>();

    private RecyclerView mRecyclerViewList1;
    private DriversAdapter mAdapter1;

    Button sendRequestRide,lookingForRiders;
    TextView datePicker,timePickerView;

    Map<String, Object> matchMap = new HashMap<>();
    ArrayList<String> driversUIDs;

    private DatePickerDialog.OnDateSetListener dateListener;

    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();

    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),  new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Log.i("CreateMatch -- Activity result CODE", String.valueOf(result.getResultCode()));

                    if(result.getResultCode() == 444) {
                        String UID,nickname;
                        Integer position;
                        Integer team;
                        Intent data = result.getData();
                        if(data != null) {
                            System.out.println(data);
                            UID = data.getStringExtra("UID");
                            nickname = data.getStringExtra("nickname");
                            position = Integer.parseInt(data.getStringExtra("position"));


                            Log.i("CreateMatch -- results:",UID+" - at "+position+" TEAM "+team);

                            boolean isAlreadyPresent = teamList1.stream().anyMatch(o -> UID.equals(o.getUID()));

                            if(isAlreadyPresent ) Toast.makeText(LookupRideActivity.this, "User already invited", Toast.LENGTH_SHORT).show();

                            else {
                            teamList1.get(position).setNicknameToLooking(nickname);
                            teamList1.get(position).setUID(UID);
                            teamList1.get(position).setImageToPlayersPending();
                            mAdapter1.notifyItemChanged(position);

                            }
                        }
                    }
                }
            });





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_raid_main);

        countPlayers = findViewById(R.id.playersCount);
        datePicker = findViewById(R.id.tvDate);
        createMatchButton = findViewById(R.id.createMatchButton);


        buildRecyclerView();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,sports);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerSport.setAdapter(adapter);
        spinnerSport.setPrompt("Select your favorite Sport!");
        spinnerSport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String value = adapterView.getItemAtPosition(pos).toString();
                Toast.makeText(LookupRideActivity.this, "SELECTED:"+value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(LookupRideActivity.this,
                        android.R.style.Theme_DeviceDefault_Dialog_MinWidth,dateListener, year,month,day);
                dialog.show();
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePickerLocal,  int year, int month, int day) {
                Log.d("CreateMatch", "onDateSet: dd/mm/yyy: " + day + "/" + month + "/" + year);
                month ++;  // from 0 to 11
                String date = day + "/" + month + "/" + year;
                datePicker.setText(date);
            }
        };


        createMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int countPlayersValue = Integer.parseInt(countPlayers.getText().toString());
                String choosedSport = spinnerSport.getSelectedItem().toString();
                String choosedDate = datePicker.getText().toString();


                if(choosedSport.isEmpty()){
                    Toast.makeText(LookupRideActivity.this, "Choose a sport", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(choosedDate.isEmpty() || !choosedDate.matches(".*\\d.*")){
                    Toast.makeText(LookupRideActivity.this, "Choose a valid date", Toast.LENGTH_SHORT).show();
                    return;
                }



                Log.i("CreateMatch","Creating match.. INFO:");
                Log.i("CreateMatch","Players for team = "+countPlayersValue);
                Log.i("CreateMatch","Sport choosed = "+choosedSport);
                Log.i("CreateMatch","Date choosed = "+choosedDate);
                Log.i("CreateMatch","Team1 = "+teamList1.size());
                Log.i("CreateMatch","Team2 = "+teamList2.size());

                matchMap.put("Date", choosedDate);
                matchMap.put("Sport", choosedSport);
                matchMap.put("Status", "Pending");

                driversUIDs = new ArrayList<>();
                team2UIDs = new ArrayList<>();




                for(UserItem pi : teamList1){
                    if(pi.getUID().equals("Dummy")){
                        Log.i("CreateMatch","TEAM1"+pi.getUID());
                        Toast.makeText(LookupRideActivity.this, "Select all players", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else driversUIDs.add(pi.getUID());
                }

                for(UserItem pi : teamList2){
                    if(pi.getUID().equals("Dummy")){
                        Log.i("CreateMatch","TEAM2"+pi.getUID());
                        Toast.makeText(LookupRideActivity.this, "Select all players", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else team2UIDs.add(pi.getUID());
                }

                HashMap<String,ArrayList<String>> Players = new HashMap<>();

                //Build map UID : notAcceptedMatch

                for(int i = 0; i< driversUIDs.size(); i++) {
                    Players.put(driversUIDs.get(i),new ArrayList<>(Arrays.asList("team1", "notAcceptedMatch")));
                    Players.put(team2UIDs.get(i),new ArrayList<>(Arrays.asList("team2", "notAcceptedMatch")));
                }

                matchMap.put("Players",Players);

                FireDb.collection("Matches").add(matchMap)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("CreateMatch", "DocumentSnapshot MATCH written with ID: " + documentReference.getId());

                                sendPlayerNotification(documentReference.getId());
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("CreateMatch", "Error adding document", e);
                            }
                        });
            }
        });

    }


    public void insertItem(int position) {
        teamList1.add(position, new UserItem(R.drawable.baseline_group_add_24,"Player","Dummy"));
        mAdapter1.notifyItemInserted(position);
    }


    public void buildRecyclerView() {
        mRecyclerViewList1 = findViewById(R.id.listPlayer1);
        mRecyclerViewList1.setHasFixedSize(true);
        mAdapter1 = new DriversAdapter(teamList1);


        mAdapter1.setOnItemClickLister(new DriversAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                teamList1.get(position).setNicknameToLooking("Looking for..");
                mAdapter1.notifyItemChanged(position);


                Intent invitePlayers = new Intent(getApplicationContext(), DriverListActivity.class);
                invitePlayers.putExtra("position",position);
                invitePlayers.putExtra("team",1);
                activityResultLauncher.launch(invitePlayers);

            }
        });


        mRecyclerViewList1.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        mRecyclerViewList1.setAdapter(mAdapter1);

        teamList1.add(new UserItem(R.drawable.baseline_group_add_24,"Player","Dummy"));
    }


    protected void sendPlayerNotification(String id_match){
        ArrayList<String> allUID = new ArrayList<String>();
        allUID.addAll(driversUIDs);

        for(String uid: allUID) {
            HashMap<String, String> localMap = new HashMap<>();
            localMap.put("id_match", id_match);
            localMap.put("UID", uid);
            localMap.put("status", "unread");

            FireDb.collection("Notifications").add(localMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("CreateMatch", "DocumentSnapshot Notification written with ID: " + documentReference.getId());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("CreateMatch", "Error adding notifications ", e);
                        }
                    });
        }



    }
}
