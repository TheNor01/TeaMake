package com.example.teamake;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
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


    //we check for a ride with driver X if the new accepted users is the last one.
    //If so, the ride is Ready

    // fare in modo che questa logica venga spostata nel main
    protected static void CheckForAllConfirmedPassengers(String rideID){

        if(auth.getCurrentUser() == null){
            return;
        }
        Log.i("Rmanager","CheckForAllConfirmedRides");
        Log.i("Rmanager user",userLogger.getUid());
        Log.i("Rmanager ride id",rideID);

        //String queryTerm = "Players."+auth.getCurrentUser().getUid()+".exists";

        Matches
                //.whereArrayContains("Players",auth.getCurrentUser().getUid()) //problem
                .whereEqualTo(FieldPath.documentId(), rideID)
                .whereEqualTo("Driver",userLogger.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        HashMap<String,String> passengers;

                        if(task.getResult().isEmpty())
                        {
                            Log.i("Rmanager","EMPTY RESULTS");
                        }
                        else {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.i("Rmanager Doc", document.getId());
                                if (document.getString("Status").equals("Confirmed")) continue;

                                passengers = ((HashMap<String, String>) document.get("Passengers"));
                                int seats = document.getLong("Seats").intValue();
                                Log.i("Rmanager", "rideID" + document.getId() + " Size of passengers: " + passengers.keySet().size());

                                int counter = 0;
                                for (String UID : passengers.keySet()) {
                                    String props = passengers.get(UID);
                                    if (props.equals("Accepted")) {
                                        counter++;
                                    }
                                }
                                if (counter == seats) {
                                    Log.i("Rmanager", "RIDE IS READY TO GO");
                                    SetReadyMatch(document.getId());
                                }
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
