package com.example.teamake;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomepageActivity extends AppCompatActivity  {


    FirebaseAuth auth;
    FirebaseUser userLogged;
    TextView profileNameTV;


    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main);
        profileNameTV = findViewById(R.id.profileNameTV);


        auth = FirebaseAuth.getInstance();
        userLogged = auth.getCurrentUser();
        if(userLogged == null){
            Intent backToLogin =  new Intent(getApplicationContext(),MainActivity.class);
            startActivity(backToLogin);
            finish();
        }else{
            profileNameTV.setText(userLogged.getEmail());
        }

    }
}
