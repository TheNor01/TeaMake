package com.example.teamake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextInputEditText edTextMail,edTextPw;
    Button loginButton;
    Button registerButton;
    ProgressBar pgLoad;

    private static final String TAG = "MainActivity";


    //Singleton mAuth firebase connection
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edTextMail = findViewById(R.id.emailEdit);
        edTextPw = findViewById(R.id.passwordEdit);
        pgLoad.setVisibility(View.INVISIBLE);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG,"Switching to Register Activity");
                Intent intentRegister = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intentRegister);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, pw;
                email = edTextMail.getText().toString();
                pw = edTextPw.getText().toString();

                if(email.isEmpty() || pw.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please, Enter a valid email or password" , Toast.LENGTH_LONG).show();
                }
                else LoginUserDb(email,pw);
            }
        });

    }



    private void LoginUserDb(String email,String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Authentication completed successfully.", Toast.LENGTH_SHORT).show();
                            Intent intentProfile = new Intent(getApplicationContext(),HomepageActivity.class);
                            startActivity(intentProfile);
                            //finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}