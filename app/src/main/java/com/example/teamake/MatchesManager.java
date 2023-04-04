package com.example.teamake;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MatchesManager {

    private static final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static CollectionReference Matches = FireDb.collection("Matches");


    protected static void CheckForAllConfirmedPlayers(){

        Log.i("Mmanager","CheckForAllConfirmedPlayers");
        Log.i("Mmanager",auth.getCurrentUser().getUid());

        //String queryTerm = "Players."+auth.getCurrentUser().getUid()+".exists";
        String queryTerm = "Players."+auth.getCurrentUser().getUid();

        Matches
                //.whereArrayContains("Players",auth.getCurrentUser().getUid()) //problem
                .orderBy(queryTerm)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        HashMap<String,ArrayList<String>> players;

                        if(queryDocumentSnapshots.isEmpty()) Log.i("Mmanager","EMPTY RESULTS");

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                            Log.i("Mmanager Doc",document.getId());
                            if(document.getString("Status").equals("Confirmed")) continue;

                            players = ((HashMap<String,ArrayList<String>> ) document.get("Players"));
                            Log.i("Mmanager","MatchID"+document.getId()+"Size of players: "+players.keySet().size());

                            //HOW many players?

                            int counter=0;
                            for(String UID: players.keySet()){
                                ArrayList<String> props = players.get(UID);
                                if(props.get(1).equals("Accepted")){
                                    counter++;
                                }
                            }
                            if(counter==players.keySet().size()){
                                Log.i("Mmanager","MATCH IS READY TO PLAY");
                                SetReadyMatch(document.getId());
                            }
                        }
                    }
                });
    }

    protected static void SetReadyMatch(String documentToChange){
        FireDb.collection("Matches").document(documentToChange)
                .update("Status", "Confirmed")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Mmanager", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Mmanager", "Error updating document", e);
                    }
                });
    }


}
