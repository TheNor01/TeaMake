package com.example.teamake;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivityInfo extends AppCompatActivity {


    TextInputEditText edNickname;
    Button storeInfoOnDb;
    Toast toast;

    //Singleton mAuth firebase connection
    private FirebaseAuth mAuth;
    private NumberPicker npAge;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main_scene_info);

        edNickname = findViewById(R.id.nicknameEdit);

        npAge = findViewById(R.id.agePicker);
        npAge.setMinValue(10);
        npAge.setMaxValue(80);
        npAge.setValue(15);
        npAge.setWrapSelectorWheel(true);

        storeInfoOnDb = findViewById(R.id.signUpButtonInfo);
        mAuth = FirebaseAuth.getInstance();

        storeInfoOnDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentCaller = getIntent();
                String email = intentCaller.getStringExtra("email");
                String pw = intentCaller.getStringExtra("password");

                Log.i(TAG,"INFO: "+ email);
                Log.i(TAG,"INFO: "+pw);
                Log.i(TAG,"INFO: "+npAge.getValue());
                Log.i(TAG,"INFO: "+edNickname.getText().toString());

                if(email.isEmpty() || pw.isEmpty()){
                    Toast.makeText(RegisterActivityInfo.this,"Please, Enter a valid email or password" , Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RegisterActivityInfo.this,"Storing all info users" , Toast.LENGTH_LONG).show();
//                    CreateUserDb(email,pw);
//                    Toast.makeText(RegisterActivity.this,"CREATED USER" , Toast.LENGTH_LONG).show();
//                    Intent intentProfile = new Intent(getApplicationContext(),HomepageActivity.class);
//                    startActivity(intentProfile);
//                    finish();
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

    private void CreateUserDb(String email,String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "createUserWithEmail:success");
//                                    FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivityInfo.this,"A new account has been created" , Toast.LENGTH_LONG).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivityInfo.this,"ERROR Creating account" , Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


}
