package com.example.teamake;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


// Classe usata per creare per una offerta di passaggio, input: data, time, uni, e posti disponibili,

public class CreateRideActivity extends AppCompatActivity {


    String[] universities = {"Cittadella Catania","Benedettini Catania","Unikore"};
    Spinner spinnerUni;
    Button addPlayerBtn,removePlayerBtn,createMatchButton;
    TextView countSeats,datePicker,timePickerView;

    int hour,minute;

    Map<String, Object> matchMap = new HashMap<>();
    ArrayList<String> invitedUsersUID;

    private DatePickerDialog.OnDateSetListener dateListener;

    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser userLogged;
    
    


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        userLogged = auth.getCurrentUser();

        if(userLogged == null) {
            Intent backToLogin = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(backToLogin);
            finish();
        }

        setContentView(R.layout.create_raid_main);

        spinnerUni= findViewById(R.id.spinnerUniversity);
        addPlayerBtn= findViewById(R.id.addSeatBtn);
        removePlayerBtn= findViewById(R.id.removeSeatBtn);
        countSeats = findViewById(R.id.playersCount);
        datePicker = findViewById(R.id.tvDate);
        timePickerView = findViewById(R.id.tvTime);
        createMatchButton = findViewById(R.id.createRideButton);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, universities);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerUni.setAdapter(adapter);
        spinnerUni.setPrompt("Select your Destination");
        spinnerUni.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String value = adapterView.getItemAtPosition(pos).toString();
                Toast.makeText(CreateRideActivity.this, "SELECTED UNI: "+value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Integer currentValue = Integer.parseInt(countSeats.getText().toString());
                currentValue++;
                countSeats.setText(String.valueOf(currentValue));

            }
        });

        removePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer currentValue = Integer.parseInt(countSeats.getText().toString());
                currentValue--;
                if(currentValue<1) {
                    Toast.makeText(CreateRideActivity.this, "You cannot decrease anymore number of seats ", Toast.LENGTH_SHORT).show();
                    return;
                }
                countSeats.setText(String.valueOf(currentValue));

            }
        });


        // DATE SECTION //

        datePicker.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(CreateRideActivity.this,
                    android.R.style.Theme_DeviceDefault_Dialog_MinWidth,dateListener, year,month,day);
            dialog.show();
        });

        dateListener = (datePickerLocal, year, month, day) -> {
            Log.d("CreateRide", "onDateSet: dd/mm/yyy: " + day + "/" + month + "/" + year);
            month ++;  // from 0 to 11
            String date = day + "/" + month + "/" + year;
            datePicker.setText(date);
        };


        // TIME SECTION //


        timePickerView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              TimePickerDialog timePickerDialog = new TimePickerDialog(CreateRideActivity.this,
                      new TimePickerDialog.OnTimeSetListener() {
                      @SuppressLint("SetTextI18n")
                      @Override
                      public void onTimeSet(TimePicker timePicker, int hourSel, int minutesSel) {
                          hour = hourSel;
                          minute = minutesSel;

                          timePickerView.setText(hour+":"+minute);

                      }
                  },24,0,true);

              timePickerDialog.updateTime(hour,minute);
              timePickerDialog.show();
          }
        });


        createMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int countSeatsValue = Integer.parseInt(countSeats.getText().toString());
                String choosedUniversity = spinnerUni.getSelectedItem().toString();
                String choosedDate = datePicker.getText().toString();
                String choosedTime = timePickerView.getText().toString();


                if (choosedUniversity.isEmpty()) {
                    Toast.makeText(CreateRideActivity.this, "Choose at least one university", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (choosedDate.isEmpty() || !choosedDate.matches(".*\\d.*") || choosedTime.isEmpty() || !choosedTime.matches(".*\\d.*")) {
                    Toast.makeText(CreateRideActivity.this, "Choose a valid date or time", Toast.LENGTH_SHORT).show();
                    return;
                }


                //PREPARE INSERT INTO//
                Log.i("CreateRide", "Creating ride.. INFO:");
                Log.i("CreateRide", "Seat for ride = " + countSeatsValue);
                Log.i("CreateRide", "Uni choosed = " + choosedUniversity);
                Log.i("CreateRide", "Date choosed = " + choosedDate);
                Log.i("CreateRide", "Time choosed = " + choosedTime);


                invitedUsersUID = new ArrayList<>();
                for(int i=0;i<countSeatsValue;i++){
                    invitedUsersUID.add("NULL");
                }


                HashMap<String,String> Passengers  = new HashMap<>();

                //Build map UID : notAcceptedMatch

                for (int i = 0; i < invitedUsersUID.size(); i++) {
                    Passengers .put("NULL", "notAcceptedRide");
                }

                matchMap.put("Date", choosedDate);
                matchMap.put("Time", choosedTime);
                matchMap.put("University", choosedUniversity);
                matchMap.put("Driver",userLogged.getUid() );
                matchMap.put("Status", "Pending");
                matchMap.put("Passengers", Passengers );
                //matchMap.put("MarkerStartingLocation", location );

                FireDb.collection("Matches").add(matchMap)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("CreateRide", "DocumentSnapshot MATCH written with ID: " + documentReference.getId());
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("CreateRide", "Error adding document", e);
                            }
                        });
            }
        });

    }

}
