package com.example.teamake;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PlayersListActivity extends AppCompatActivity {


    TextView inviteView,nicknameView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference playersRef = db.collection("UserBasicInfo");

    PlayersAdapter playersAdapter;
    RecyclerView playersViewList;
    ArrayList<PlayerItem> playersArrayList;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.players_list);


        playersViewList = findViewById(R.id.listPlayers);
        playersViewList.setHasFixedSize(true);
        playersViewList.setLayoutManager(new LinearLayoutManager(this));

        playersArrayList = new ArrayList<PlayerItem>();

        playersAdapter = new PlayersAdapter(playersArrayList);

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

                        PlayerItem PI = new PlayerItem(R.drawable.baseline_person_24,nickname);
                        playersArrayList.add(PI);
                    }

                }

                playersAdapter.notifyDataSetChanged();



            }
        });


    }

}
