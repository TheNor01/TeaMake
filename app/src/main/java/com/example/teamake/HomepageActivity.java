package com.example.teamake;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomepageActivity extends AppCompatActivity  {


    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();
    private CollectionReference Notifications = FireDb.collection("Notifications");
    private CollectionReference Matches = FireDb.collection("Matches");

    FirebaseUser userLogged;
    TextView profileNameTV;
    TextView sportNameTv;
    TextView createMatchTv;
    ImageView imageViewProfile;


    // NOTIFICATIONS PENDING MATCHES
    ArrayList<MatchItem> listMatchPending;
    RecyclerView matchesPendingView;
    MatchesAdapter mAdapter;
    //

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    Log.i(TAG,String.valueOf(result.getResultCode()));

                    if(result.getResultCode() == RESULT_OK) {
                        Uri uri;
                        Intent data = result.getData();
                        if(data != null) {
                            uri = data.getData();
                            Log.i(TAG,uri.toString());
                            imageViewProfile.setImageURI(uri);
                        }
                    }
                }
    });

    private static final String TAG = "HomepageActivity";
    private static final Integer MY_PERMISSIONS_REQUEST_READ_MEDIA = 0;
    private final String collectionInfoUser = "UserBasicInfo";

    private int permissionCheckRead = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main);


        profileNameTV = findViewById(R.id.profileNameTV);
        createMatchTv = findViewById(R.id.createMatchTv);
        sportNameTv = findViewById(R.id.bestSports);
        imageViewProfile = findViewById(R.id.imageViewMainPic);

        matchesPendingView = findViewById(R.id.pendingMatches);
        listMatchPending = new ArrayList<>();

        permissionCheckRead = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);

        userLogged = auth.getCurrentUser();
        Log.i(TAG,"LOGGED USER:"+userLogged.getUid());
        if(userLogged == null){
            Intent backToLogin =  new Intent(getApplicationContext(),MainActivity.class);
            startActivity(backToLogin);
            finish();
        }else{
            //Getting info about logger users
            FireDb.collection(collectionInfoUser).document(userLogged.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String nickname = task.getResult().get("Nickname").toString();
                        List<String> sports = (List<String>) task.getResult().get("Sports");

                        String sportsAsString =  sports.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(","));

                        Log.i(TAG,"LOGGED USER NICK:"+nickname);
                        profileNameTV.setText(nickname);
                        sportNameTv.setText(sportsAsString);

                        //Add sync task -- login sergio -- but it shows Kamado
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });

            buildRecyclerView();
        }

        createMatchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCreateMatch= new Intent(getApplicationContext(),CreateMatchActivity.class);
                startActivity(intentCreateMatch);
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create an instance of the
                // intent of the type image

                Intent iGallery = new Intent();
                iGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                //default uri will be DCIM
                File uriToLoad = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                iGallery.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);


                //setResult(11,iGallery); maybe it is called from other activity

                if (permissionCheckRead == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomepageActivity.this, "You have already granted this permission!", Toast.LENGTH_SHORT).show();
                }
                else if (permissionCheckRead == PackageManager.PERMISSION_DENIED){
                    GetPermissionMediaStorage();
                }

                activityResultLauncher.launch(iGallery);


                // pass the constant to compare it
                // with the returned requestCode
            }
        });

    }

    private void GetPermissionMediaStorage() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because you are changing your profile picture")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomepageActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

            Log.i(TAG,"Granted Permission with alert dialog");
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }
    }

    public void buildRecyclerView() {

        String loggedUserUID = userLogged.getUid();
        Log.i(TAG,"GETTING ALL NOTIFICATION MATCHES FOR: "+loggedUserUID);


        ArrayList<String> matchesToDisplayDb = new ArrayList<>();

        Notifications
                .whereEqualTo("UID", loggedUserUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                System.out.println(document.getId());
                                String status = document.getString("status");
                                if(status.equals("unread")) {
                                    String localId = document.getString("id_match");
                                    Log.i(TAG, document.getId() + " => " + localId + "  ADDING");
                                    matchesToDisplayDb.add(localId);
                                    System.out.println(matchesToDisplayDb.size());

                                }
                            }
                            PopulateRecyclerView(matchesToDisplayDb);
                        } else {
                            Log.i(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    protected  void PopulateRecyclerView(ArrayList<String> matchesToDisplayDb){

        Log.i(TAG,"DISPLAYING ALL PENDING MATCHES  FOR: "+userLogged.getUid());
        Log.i(TAG, String.valueOf(matchesToDisplayDb.size()));
        if(!matchesToDisplayDb.isEmpty()) {
            Matches
                    .whereIn(FieldPath.documentId(), matchesToDisplayDb)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String status = document.getString("Status");
                                    ArrayList<String> team1, team2;
                                    team1 = (ArrayList<String>) document.get("Team1");
                                    team2 = (ArrayList<String>) document.get("Team2");

                                    boolean isPlayerInvited = false;
                                    if (team1.contains(userLogged.getUid()) || team2.contains(userLogged.getUid())) isPlayerInvited = true;
                                    if (status.equals("Pending") && isPlayerInvited) {

                                        Log.i(TAG,"Found matchID: "+document.getId());

                                        String date = document.getString("Date");
                                        String sport = document.getString("Sport");
                                        Integer imageToUse;
                                        switch (sport) {
                                            case "Tennis":
                                                imageToUse = R.drawable.baseline_sports_tennis_24;
                                                break;
                                            case "Soccer":
                                                imageToUse = R.drawable.baseline_sports_soccer_24;
                                                break;
                                            case "Basket":
                                                imageToUse = R.drawable.baseline_sports_basketball_24;
                                                break;
                                            default:
                                                imageToUse = R.drawable.baseline_group_add_24;
                                        }

                                        MatchItem MI = new MatchItem(document.getId(), imageToUse, sport, date, -1, -1, R.drawable.baseline_check_24);
                                        listMatchPending.add(MI);

                                    }
                                }
                                LinkAdapterToList();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    protected void LinkAdapterToList(){
        System.out.println("SIZE REC VIEW: "+listMatchPending.size());

        matchesPendingView.setHasFixedSize(true);

        //listMatchPending.add(new MatchItem("xxx", R.drawable.baseline_sports_basketball_24, "basket", "22", 0, 0, R.drawable.baseline_check_24));

        mAdapter = new MatchesAdapter(listMatchPending);
        matchesPendingView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        matchesPendingView.setAdapter(mAdapter);

        mAdapter.setOnItemClickLister(new MatchesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String matchID = listMatchPending.get(position).getMatchID();
                Log.i(TAG,"Accepting match id"+matchID);
            }
        });

    }

}

