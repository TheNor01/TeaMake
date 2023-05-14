package com.example.teamake;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivityInfo extends AppCompatActivity {


    TextInputEditText edNickname,edName,edSecondName;
    Button storeInfoOnDb;
    String uniCheck="";
    RadioGroup radioGroup;

    String email,pw;
    EditText phoneEdit ;

    //Singleton mAuth firebase connection
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();
    private final String collectionInfoUser = "UserBasicInfo";
    private NumberPicker npAge;
    public static final String TAG = "RegisterActivityInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main_scene_info);

        edNickname = findViewById(R.id.nicknameEdit);
        edName = findViewById(R.id.nameEdit);
        edSecondName = findViewById(R.id.second_nameEdit);
        phoneEdit = findViewById(R.id.phone);


        npAge = findViewById(R.id.agePicker);
        npAge.setMinValue(10);
        npAge.setMaxValue(80);
        npAge.setValue(15);
        npAge.setWrapSelectorWheel(true);

        radioGroup = findViewById(R.id.radioGroupUni);

        storeInfoOnDb = findViewById(R.id.signUpButtonInfo);


        storeInfoOnDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentCaller = getIntent();
                 email = intentCaller.getStringExtra("email");
                 pw = intentCaller.getStringExtra("password");

                Log.i(TAG,"INFO email: "+ email);
                Log.i(TAG,"INFO PW: "+ pw);
                Log.i(TAG,"INFO AGE: "+ npAge.getValue());
                Log.i(TAG,"INFO Nickname: "+ edNickname.getText().toString());
                Log.i(TAG,"INFO Name: "+ edName.getText().toString());
                Log.i(TAG,"INFO SecondName: "+ edSecondName.getText().toString());
                Log.i(TAG,"INFO phone: "+ phoneEdit.getText().toString());


                if(email.isEmpty() || pw.isEmpty()){
                    Toast.makeText(RegisterActivityInfo.this,"Please, Enter a valid email or password" , Toast.LENGTH_LONG).show();
                } else if (uniCheck.equals("")) {
                    Toast.makeText(RegisterActivityInfo.this,"Please, Pick a valid university" , Toast.LENGTH_LONG).show();
                }else{
                    Log.i(TAG,"Storing Db auth");
                    CreateUserDb(email,pw);
                    Toast.makeText(RegisterActivityInfo.this,"CREATED USER AuthDb" , Toast.LENGTH_LONG).show();
                    ////
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
                            String UID = task.getResult().getUser().getUid();
                            Log.i(TAG,"CREATED:" + UID);
                            CreateUserInfoDb(UID);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivityInfo.this,"ERROR Creating account" , Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void CreateUserInfoDb(String UID){
        HashMap<String,Object> userInfo = new HashMap<>();

        userInfo.put("Nickname",edNickname.getText().toString());
        userInfo.put("Age",npAge.getValue());
        userInfo.put("Name",edName.getText().toString());
        userInfo.put("Second Name",edSecondName.getText().toString());
        userInfo.put("Phone",phoneEdit.getText().toString());
        userInfo.put("University", uniCheck);
        FireDb.collection(collectionInfoUser).document(UID).set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User account deleted.");
                                        }
                                    }
                                });
                    }
                });
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_cittadella:
                if (checked){
                    Toast.makeText(this, "Selected"+((RadioButton) view).getText(), Toast.LENGTH_SHORT).show();
                    uniCheck = (String) ((RadioButton) view).getText();
                }
                break;
            case R.id.radio_unikore:
                if (checked){
                    Toast.makeText(this, "Selected"+((RadioButton) view).getText(), Toast.LENGTH_SHORT).show();
                    uniCheck = (String) ((RadioButton) view).getText();
                }
                break;
        }
    }
}
