package com.example.teamake;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class CreateMatchActivity extends AppCompatActivity {

    ArrayList<PlayerItem> teamList1= new ArrayList<>();
    ArrayList<PlayerItem> teamList2= new ArrayList<>();

    private RecyclerView mRecyclerViewList1,mRecyclerViewList2;
    private PlayersAdapter mAdapter1,mAdapter2;
    private RecyclerView.LayoutManager mLayoutManager;

    String[] sports = {"Tennis","Basket","Soccer"};
    Spinner spinnerSport;
    Button addPlayerBtn,removePlayerBtn;
    TextView countPlayers,datePicker;

    private DatePickerDialog.OnDateSetListener dateListener;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_match_main);

        spinnerSport= findViewById(R.id.spinnerSport);
        addPlayerBtn= findViewById(R.id.addPlayerBtn);
        removePlayerBtn= findViewById(R.id.removePlayerBtn);
        countPlayers = findViewById(R.id.playersCount);
        datePicker = findViewById(R.id.tvDate);


        buildRecyclerView();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,sports);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerSport.setAdapter(adapter);
        spinnerSport.setPrompt("Select your favorite Sport!");
        spinnerSport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String value = adapterView.getItemAtPosition(pos).toString();
                Toast.makeText(CreateMatchActivity.this, "SELECTED:"+value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addPlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Integer currentValue = Integer.parseInt(countPlayers.getText().toString());
                currentValue++;
                countPlayers.setText(String.valueOf(currentValue));

                int position = Integer.parseInt(countPlayers.getText().toString());

                int scaledPosition = position-1;
                Log.i("CreateMatch","Sizeof list: " + mAdapter1.getItemCount());
                insertItem(scaledPosition);
                Log.i("CreateMatch","Sizeof list: " + mAdapter1.getItemCount());

            }
        });

        removePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer currentValue = Integer.parseInt(countPlayers.getText().toString());
                currentValue--;
                if(currentValue<1) {
                    Toast.makeText(CreateMatchActivity.this, "You cannot decrease anymore number of players", Toast.LENGTH_SHORT).show();
                    return;
                }
                countPlayers.setText(String.valueOf(currentValue));

                int position = Integer.parseInt(countPlayers.getText().toString());
                removeItem(position);

            }
        });


        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(CreateMatchActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar,dateListener, year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePickerLocal,  int year, int month, int day) {
                Log.d("CreateMatch", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);
                String date = month + "/" + day + "/" + year;
                datePicker.setText(date);
            }
        };


    }
    public void insertItem(int position) {
        teamList1.add(position, new PlayerItem(R.drawable.baseline_group_add_24,"Player"));
        teamList2.add(position, new PlayerItem(R.drawable.baseline_group_add_24,"Player"));
        mAdapter1.notifyItemInserted(position);
        mAdapter2.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        teamList1.remove(position);
        teamList2.remove(position);
        mAdapter1.notifyItemRemoved(position);
        mAdapter2.notifyItemRemoved(position);
    }

    public void buildRecyclerView() {
        mRecyclerViewList1 = findViewById(R.id.listPlayer1);
        mRecyclerViewList2 = findViewById(R.id.listPlayer2);

        mRecyclerViewList1.setHasFixedSize(true);
        mRecyclerViewList2.setHasFixedSize(true);


        mAdapter1 = new PlayersAdapter(teamList1);
        mAdapter2 = new PlayersAdapter(teamList2);


        mAdapter1.setOnItemClickLister(new PlayersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                teamList1.get(position).setNicknameToLooking("Looking for..");
                mAdapter1.notifyItemChanged(position);
            }
        });

        mAdapter2.setOnItemClickLister(new PlayersAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                teamList2.get(position).setNicknameToLooking("Looking for..");
                mAdapter2.notifyItemChanged(position);
            }
        });


        mRecyclerViewList1.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mRecyclerViewList2.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        mRecyclerViewList1.setAdapter(mAdapter1);
        mRecyclerViewList2.setAdapter(mAdapter2);

        teamList1.add(new PlayerItem(R.drawable.baseline_group_add_24,"Player"));
        teamList2.add(new PlayerItem(R.drawable.baseline_group_add_24,"Player"));
    }
}
