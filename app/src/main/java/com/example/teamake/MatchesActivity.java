package com.example.teamake;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MatchesActivity extends AppCompatActivity {


    //Singleton mAuth firebase connection
    private FirebaseAuth mAuth;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapterData;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<MatchItem> matchesList= new ArrayList<>();


    //Create a match

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.matches_main);

        recyclerView = findViewById(R.id.matchesPlayed);

        // get from db at the time
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        matchesList.add(new MatchItem("1234",R.drawable.baseline_sports_soccer_24,"Soccer","2020-02-01",10,20,R.drawable.baseline_check_24));
        matchesList.add(new MatchItem("12345",R.drawable.baseline_sports_tennis_24,"Tennis","2020-02-01",10,20,R.drawable.baseline_check_24));
        matchesList.add(new MatchItem("123456",R.drawable.baseline_sports_basketball_24,"Basket","2020-02-01",10,20,R.drawable.baseline_check_24));


        adapterData = new MatchesAdapter(matchesList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterData);




    }


}
