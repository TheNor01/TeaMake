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
import android.widget.Spinner;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateMatchActivity extends AppCompatActivity {

    ArrayList<PlayerItem> teamList1= new ArrayList<>();
    ArrayList<PlayerItem> teamList2= new ArrayList<>();

    private RecyclerView mRecyclerViewList1,mRecyclerViewList2;
    private PlayersAdapter mAdapter1,mAdapter2;

    String[] sports = {"Tennis","Basket","Soccer"};
    Spinner spinnerSport;
    Button addPlayerBtn,removePlayerBtn,createMatchButton;
    TextView countPlayers,datePicker;

    Map<String, Object> matchMap = new HashMap<>();
    ArrayList<String> team1UIDs,team2UIDs;

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
                            team = Integer.parseInt(data.getStringExtra("team"));


                            Log.i("CreateMatch -- results:",UID+" - at "+position+" TEAM "+team);

                            boolean isAlreadyPresent = teamList1.stream().anyMatch(o -> UID.equals(o.getUID()));
                            boolean isAlreadyPresent2 = teamList2.stream().anyMatch(o -> UID.equals(o.getUID()));

                            if(isAlreadyPresent || isAlreadyPresent2) Toast.makeText(CreateMatchActivity.this, "User already invited", Toast.LENGTH_SHORT).show();

                            else {
                                if (team == 1) {
                                    teamList1.get(position).setNicknameToLooking(nickname);
                                    teamList1.get(position).setUID(UID);
                                    teamList1.get(position).setImageToPlayersPending();
                                    mAdapter1.notifyItemChanged(position);
                                } else {
                                    teamList2.get(position).setNicknameToLooking(nickname);
                                    teamList2.get(position).setUID(UID);
                                    teamList2.get(position).setImageToPlayersPending();
                                    mAdapter2.notifyItemChanged(position);
                                }
                            }
                        }
                    }
                }
            });





    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_match_main);

        spinnerSport= findViewById(R.id.spinnerSport);
        addPlayerBtn= findViewById(R.id.addPlayerBtn);
        removePlayerBtn= findViewById(R.id.removePlayerBtn);
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
                Toast.makeText(CreateMatchActivity.this, "SELECTED:"+value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Integer currentValue = Integer.parseInt(countPlayers.getText().toString());
                currentValue++;
                countPlayers.setText(String.valueOf(currentValue));

                int position = Integer.parseInt(countPlayers.getText().toString());

                int scaledPosition = position-1;
                Log.i("CreateMatch","Sizeof list: " + mAdapter1.getItemCount());
                insertItem(scaledPosition);
                Log.i("CreateMatch","Sizeof list: " + mAdapter1.getItemCount());

            }
        });

        removePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer currentValue = Integer.parseInt(countPlayers.getText().toString());
                currentValue--;
                if(currentValue<1) {
                    Toast.makeText(CreateMatchActivity.this, "You cannot decrease anymore number of players", Toast.LENGTH_SHORT).show();
                    return;
                }
                countPlayers.setText(String.valueOf(currentValue));

                int position = Integer.parseInt(countPlayers.getText().toString());
                removeItem(position);

            }
        });


        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(CreateMatchActivity.this,
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
                    Toast.makeText(CreateMatchActivity.this, "Choose a sport", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(choosedDate.isEmpty() || !choosedDate.matches(".*\\d.*")){
                    Toast.makeText(CreateMatchActivity.this, "Choose a valid date", Toast.LENGTH_SHORT).show();
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

                team1UIDs = new ArrayList<>();
                team2UIDs = new ArrayList<>();




                for(PlayerItem pi : teamList1){
                    if(pi.getUID().equals("Dummy")){
                        Log.i("CreateMatch","TEAM1"+pi.getUID());
                        Toast.makeText(CreateMatchActivity.this, "Select all players", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else team1UIDs.add(pi.getUID());
                }

                for(PlayerItem pi : teamList2){
                    if(pi.getUID().equals("Dummy")){
                        Log.i("CreateMatch","TEAM2"+pi.getUID());
                        Toast.makeText(CreateMatchActivity.this, "Select all players", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else team2UIDs.add(pi.getUID());
                }

                matchMap.put("Team1",team1UIDs);
                matchMap.put("Team2",team2UIDs);

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
        teamList1.add(position, new PlayerItem(R.drawable.baseline_group_add_24,"Player","Dummy"));
        teamList2.add(position, new PlayerItem(R.drawable.baseline_group_add_24,"Player","Dummy"));
        mAdapter1.notifyItemInserted(position);
        mAdapter2.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        teamList1.remove(position);
        teamList2.remove(position);
        mAdapter1.notifyItemRemoved(position);
        mAdapter2.notifyItemRemoved(position);
    }

    public void buildRecyclerView() {
        mRecyclerViewList1 = findViewById(R.id.listPlayer1);
        mRecyclerViewList2 = findViewById(R.id.listPlayer2);

        mRecyclerViewList1.setHasFixedSize(true);
        mRecyclerViewList2.setHasFixedSize(true);


        mAdapter1 = new PlayersAdapter(teamList1);
        mAdapter2 = new PlayersAdapter(teamList2);


        mAdapter1.setOnItemClickLister(new PlayersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                teamList1.get(position).setNicknameToLooking("Looking for..");
                mAdapter1.notifyItemChanged(position);


                Intent invitePlayers = new Intent(getApplicationContext(),PlayersListActivity.class);
                invitePlayers.putExtra("position",position);
                invitePlayers.putExtra("team",1);
                //startActivity(invitePlayers);
                activityResultLauncher.launch(invitePlayers);

            }
        });

        mAdapter2.setOnItemClickLister(new PlayersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                teamList2.get(position).setNicknameToLooking("Looking for..");
                mAdapter2.notifyItemChanged(position);

                Intent invitePlayers = new Intent(getApplicationContext(),PlayersListActivity.class);
                invitePlayers.putExtra("position",position);
                invitePlayers.putExtra("team",2);
                //startActivity(invitePlayers);
                activityResultLauncher.launch(invitePlayers);
            }
        });


        mRecyclerViewList1.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mRecyclerViewList2.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        mRecyclerViewList1.setAdapter(mAdapter1);
        mRecyclerViewList2.setAdapter(mAdapter2);

        teamList1.add(new PlayerItem(R.drawable.baseline_group_add_24,"Player","Dummy"));
        teamList2.add(new PlayerItem(R.drawable.baseline_group_add_24,"Player","Dummy"));
    }


    protected void sendPlayerNotification(String id_match){
        ArrayList<String> allUID = new ArrayList<String>();
        allUID.addAll(team1UIDs);
        allUID.addAll(team2UIDs);

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
