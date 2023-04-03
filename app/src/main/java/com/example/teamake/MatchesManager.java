package com.example.teamake;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchesManager {

    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();


    protected void CheckForAllConfirmedPlayers(){
        FireDb.collection("Matches")
                .whereEqualTo("Status","Pending")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            HashMap<String, ArrayList<String>> players;

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                players = ((HashMap<String,ArrayList<String>> ) document.get("Players"));

                                //HOW many players?

                                Log.i("Mmanager","Size of players"+players.keySet().size());

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
                    }
                });
    }

    protected void SetReadyMatch(String documentToChange){
        FireDb.collection("Matches").document(documentToChange)
                .update("status", "Confirmed")
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
