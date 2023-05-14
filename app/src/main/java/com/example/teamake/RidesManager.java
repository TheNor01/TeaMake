package com.example.teamake;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class RidesManager {

    private static final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final FirebaseUser userLogger = auth.getCurrentUser();

    private static CollectionReference Matches = FireDb.collection("Matches");



    // fare in modo che questa logica venga spostata nel main
    protected static void CheckForAllConfirmedPassengers(String keyPass){

        if(auth.getCurrentUser() == null){
            return;
        }
        Log.i("Rmanager","CheckForAllConfirmedRides");
        Log.i("Rmanager user",userLogger.getUid());

        //String queryTerm = "Players."+auth.getCurrentUser().getUid()+".exists";
        String queryTerm = "Passengers."+keyPass;

        Matches
                //.whereArrayContains("Players",auth.getCurrentUser().getUid()) //problem
                .orderBy(queryTerm)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        HashMap<String,String> passengers;

                        if(queryDocumentSnapshots.isEmpty()) Log.i("Rmanager","EMPTY RESULTS");

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                            Log.i("Rmanager Doc",document.getId());
                            if(document.getString("Status").equals("Confirmed")) continue;

                            passengers = ((HashMap<String,String> ) document.get("Passengers"));
                            int seats = document.getLong("Seats").intValue();
                            Log.i("Rmanager","rideID"+document.getId()+" Size of passengers: "+passengers.keySet().size());

                            int counter=0;
                            for(String UID: passengers.keySet()){
                                String props = passengers.get(UID);
                                if(props.equals("Accepted")){
                                    counter++;
                                }
                            }
                            if(counter==seats){
                                Log.i("Rmanager","RIDE IS READY TO GO");
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
                        Log.d("Rmanager", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Rmanager", "Error updating document", e);
                    }
                });
    }


}
