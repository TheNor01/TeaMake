package com.example.teamake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;



public class DriverListActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference playersRef = db.collection("UserBasicInfo");
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

        playersRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {


                if(error != null){
                    Log.e("Firestore error", error.getMessage());
                }

                for(DocumentChange dc : value.getDocumentChanges()){
                    if(dc.getType() == DocumentChange.Type.ADDED){

                        DocumentSnapshot doc =  dc.getDocument();

                        String nickname = doc.getString("Nickname");
                        String UID = doc.getId();
                        int freeSeats = Integer.parseInt(doc.getString("FreeSeats"));

                        UserItem PI = new UserItem(R.drawable.baseline_person_24,nickname,UID,freeSeats);
                        Log.i("DriverList Activity","ADDED:"+PI.getUID());

                        driversArrayList.add(PI);
                    }

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
                String localTeam = extras.get("team").toString();
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
