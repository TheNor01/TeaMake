package com.example.teamake;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {


    TextInputEditText edTextMail,edTextPw;
    Button registerButton;
    Toast toast;

    //Singleton mAuth firebase connection
    private FirebaseAuth mAuth;
    private Scene infoRegistration;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main);
        edTextMail = findViewById(R.id.emailEdit);
        edTextPw = findViewById(R.id.passwordEdit);


        int sceneRoot = R.id.registrationForm;
        infoRegistration = Scene.getSceneForLayout((ViewGroup) findViewById(sceneRoot),R.layout.register_main_scene_info,this);

        registerButton = findViewById(R.id.signUpButton);
        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, pw;
                email = edTextMail.getText().toString();
                pw = edTextPw.getText().toString();

                Log.i(TAG,"INFO: "+ email);
                Log.i(TAG,"INFO: "+pw);

                if(email.isEmpty() || pw.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"Please, Enter a valid email or password" , Toast.LENGTH_LONG).show();
                }else{
                    changeSceneToInfo(view);
                }
            }
        });

    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }


    public void changeSceneToInfo(View view){
        Log.i(TAG,"changing Scene to INFO");

        Intent sendInfoDb = new Intent(this,RegisterActivityInfo.class);
        sendInfoDb.putExtra("email",edTextMail.getText().toString());
        sendInfoDb.putExtra("password",edTextPw.getText().toString());
        startActivity(sendInfoDb, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        Transition fadeTransition = new Fade();
//        TransitionManager.go(infoRegistration,fadeTransition);
    }


}
