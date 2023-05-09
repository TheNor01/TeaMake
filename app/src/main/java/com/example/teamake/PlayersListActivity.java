package com.example.teamake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

public class PlayersListActivity extends AppCompatActivity {



    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference playersRef = db.collection("UserBasicInfo");
    DriversAdapter playersAdapter;
    RecyclerView playersViewList;
    ArrayList<UserItem> playersArrayList;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.players_list);


        playersViewList = findViewById(R.id.listPlayers);
        playersViewList.setHasFixedSize(true);
        playersViewList.setLayoutManager(new LinearLayoutManager(this));

        playersArrayList = new ArrayList<UserItem>();
        playersAdapter = new DriversAdapter(playersArrayList);
        playersViewList.setAdapter(playersAdapter);


        EventChangeListener();


    }


    private void EventChangeListener(){

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

                        UserItem PI = new UserItem(R.drawable.baseline_person_24,nickname,UID);
                        Log.i("PlayerList Activity","ADDED:"+PI.getUID());


                        playersArrayList.add(PI);
                    }

                }

                buildRecyclerView();

            }
        });

    }

    public void buildRecyclerView() {
        playersViewList = findViewById(R.id.listPlayers);

        playersViewList.setHasFixedSize(true);


        playersAdapter = new DriversAdapter(playersArrayList);
        playersViewList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        playersViewList.setAdapter(playersAdapter);

        playersAdapter.setOnItemClickLister(new DriversAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                playersArrayList.get(position).setImageToPlayersPending();
                playersAdapter.notifyItemChanged(position);

                String localUid = playersArrayList.get(position).getUID();
                String localNickname = playersArrayList.get(position).getNicknameText();

                Log.i("PlayersList","Inviting... ="+localUid);


                Bundle extras = getIntent().getExtras();

                String localPosition = extras.get("position").toString();
                String localTeam = extras.get("team").toString();
                Log.i("PlayersList",  "calling intent position: "+localPosition);


               // System.out.println(positionListView);



                Intent intent = new Intent();
                intent.putExtra("UID", localUid);
                intent.putExtra("position",localPosition);
                intent.putExtra("team",localTeam);
                intent.putExtra("nickname",localNickname);
                setResult(444, intent);
                finish();
            }
        });



    }

}
