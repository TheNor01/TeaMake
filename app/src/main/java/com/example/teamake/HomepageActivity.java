package com.example.teamake;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


//Todo
// 1) Build service as listener notification
// 2) cache img


public class HomepageActivity extends AppCompatActivity  {


    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();

    private  final StorageReference FireStorage = FirebaseStorage.getInstance().getReference();
    private CollectionReference Notifications = FireDb.collection("Notifications");
    private CollectionReference Matches = FireDb.collection("Matches");

    FirebaseUser userLogged;
    TextView profileNameTV,sportNameTv,createMatchTv,myStats;

    Button logoutBtn;

    ImageView imageViewProfile;

    // NOTIFICATIONS PENDING MATCHES
    ArrayList<MatchItem> listMatchPending;
    RecyclerView matchesPendingView;
    MatchesAdapter mAdapter;
    //

    HashMap<String,String> linkingNotificationMatches = new HashMap<>();

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
                            String referenceStr = "profileImages/"+userLogged.getUid()+"_profileImage.jpg";
                            StorageReference profileRef = FireStorage.child(referenceStr);
                            UploadTask uploadTask = profileRef.putFile(uri);

                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.i(TAG,"Data loaded = "+referenceStr);
                                }
                            });

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
        myStats = findViewById(R.id.statsTv);
        sportNameTv = findViewById(R.id.bestSports);
        imageViewProfile = findViewById(R.id.imageViewMainPic);
        logoutBtn = findViewById(R.id.buttonLogout);

        matchesPendingView = findViewById(R.id.pendingMatches);
        listMatchPending = new ArrayList<>();
        matchesPendingView.setHasFixedSize(true);
        //listMatchPending.add(new MatchItem("xxx", R.drawable.baseline_sports_basketball_24, "basket", "22", 0, 0, R.drawable.baseline_check_24));
        matchesPendingView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mAdapter = new MatchesAdapter(listMatchPending);
        matchesPendingView.setAdapter(mAdapter);

        permissionCheckRead = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);

        userLogged = auth.getCurrentUser();
        if(userLogged == null){
            Intent backToLogin =  new Intent(getApplicationContext(),MainActivity.class);
            startActivity(backToLogin);
            finish();
        }else{
            Log.i(TAG,"LOGGED USER:"+userLogged.getUid());
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


            String referenceStr = "profileImages/"+userLogged.getUid()+"_profileImage.jpg";
            StorageReference profileRef = FireStorage.child(referenceStr);

            String profileImg = userLogged.getUid()+"_profileImage.jpg";
            try {
                File localFile = File.createTempFile(profileImg, "jpg");
                profileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG,"Local file created");
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        imageViewProfile.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i(TAG,"ERROR Download file");
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            //buildRecyclerView();
            //LinkAdapterToList();
            ManageOnClickMatch();

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG,"Logging out");
                    auth.signOut();
                    Intent backToLogin = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(backToLogin);
                    finish();
                }
            });
        }

        createMatchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCreateMatch= new Intent(getApplicationContext(),CreateMatchActivity.class);
                startActivity(intentCreateMatch);
            }
        });

        myStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentViewMatch= new Intent(getApplicationContext(),MatchesActivity.class);
                startActivity(intentViewMatch);
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

                                String status = document.getString("status");
                                if(status.equals("unread")) {
                                    String localIdMatch = document.getString("id_match");
                                    String notificationID = document.getId();
                                    Log.i(TAG, "DOC ID:"+notificationID + " => ADDING ID MATCH: " + localIdMatch);
                                    matchesToDisplayDb.add(localIdMatch);

                                    //match-notify
                                    linkingNotificationMatches.put(localIdMatch,notificationID);
                                    System.out.println("Size matches:"+ matchesToDisplayDb.size());

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
                                    HashMap<String,ArrayList<String>> players;
                                    players = ((HashMap<String,ArrayList<String>> ) document.get("Players"));
                                    boolean isPlayerInvited = false;

                                    if (players.containsKey(userLogged.getUid())) isPlayerInvited = true;
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

                                        //MatchItem MI = new MatchItem(document.getId(), imageToUse, sport, date, -1, -1, R.drawable.baseline_check_24);
                                        MatchItem MI = new MatchItem(document.getId(), imageToUse, document.getId(), date, -1, -1, R.drawable.baseline_check_24);
                                        listMatchPending.add(MI);

                                    }
                                }
                                //LinkAdapterToList();
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }

                    });
        }
    }

    protected void LinkAdapterToList() {
        Log.i(TAG,"SIZE Pending matches: " + listMatchPending.size());
        //mAdapter.notifyDataSetChanged();
        ManageOnClickMatch();
    }

        // disconnect two parts

    protected void ManageOnClickMatch(){
        mAdapter.setOnItemClickLister(new MatchesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String matchID = listMatchPending.get(position).getMatchID();
                Log.i(TAG,"Accepting match adapter"+matchID);
                Matches.document(matchID)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                HashMap<String,ArrayList<String>> playerToUpdate = new HashMap<>();
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                        playerToUpdate = ((HashMap<String,ArrayList<String>> ) document.get("Players"));
                                        if(playerToUpdate.containsKey(userLogged.getUid())){

                                            ArrayList<String> props = playerToUpdate.get(userLogged.getUid());
                                            props.set(1, "Accepted");
                                            playerToUpdate.put(userLogged.getUid(),props);
                                            Log.i(TAG,"Setting map changes for: "+userLogged.getUid()+"--"+playerToUpdate.get(userLogged.getUid()));
                                        }

                                       // HashMap<String,String> team1
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }

                                if(playerToUpdate.isEmpty()) {
                                    System.err.println("Map is empty");
                                }
                                else{
                                    ModifyMatchPlayers(playerToUpdate,matchID,position);
                                }
                            }
                        });
            }
        });
    }


    protected void ModifyMatchPlayers(HashMap<String,ArrayList<String>> mapToUpload,String matchID,int position){
        Matches.document(matchID).update("Players",mapToUpload).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                String notificationToRemove = null;

                if (task.isSuccessful()) {
                    Log.i(TAG,"UPDATED Match id"+matchID);
                     notificationToRemove = linkingNotificationMatches.get(matchID);
                }
                else {
                    Log.d(TAG, "get failed with ", task.getException());
                }

                if(notificationToRemove != null) RemoveNotificationFromDbList(matchID,notificationToRemove,position);

            }
        });
    }


    protected void RemoveNotificationFromDbList(String matchID,String notificationToRemove,int position){
        Notifications.document(notificationToRemove)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Notification"+ notificationToRemove +" successfully deleted!");
                        linkingNotificationMatches.remove(matchID);
                        listMatchPending.remove(position);
                        mAdapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"ON START");
        //LinkAdapterToList();
    }



    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG,"ON PAUSE");
        ArrayList<String> newMatches = new ArrayList<>();
        Notifications
                .whereEqualTo("UID", userLogged.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String localUID = document.getString("UID");
                                String localStatus = document.getString("status");
                                String localNotification = document.getId();
                                String localMatch = document.getString("id_match");

                                if(!localUID.equals(userLogged.getUid()) && !localStatus.equals("unread")) continue;

                                if(!linkingNotificationMatches.containsKey(localMatch)) {
                                    Log.i(TAG,"ADDING NEW Match"+localMatch);
                                    Log.i(TAG,"ADDING NEW Notification"+document.getString("id_match"));

                                    linkingNotificationMatches.put(localMatch,localNotification);
                                    newMatches.add(document.getString("id_match"));
                                }
                            }

                        }
                        Log.d(TAG, "new matches: " + newMatches);
                        Log.i(TAG, String.valueOf(newMatches.size()));
                        if(!newMatches.isEmpty()) {
                            Log.i(TAG, "calling populate v2 for : " + userLogged.getUid());
                            PopulateRecyclerView(newMatches);
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
        }


        /*
    protected  void PopulateRecyclerView(ArrayList<String> matchesToDisplayDb){

        Log.i(TAG,"DISPLAYING ALL PENDING MATCHES  V2 FOR: "+userLogged.getUid());
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
                                    HashMap<String,ArrayList<String>> players;
                                    players = ((HashMap<String,ArrayList<String>> ) document.get("Players"));
                                    boolean isPlayerInvited = false;

                                    if (players.containsKey(userLogged.getUid())) isPlayerInvited = true;
                                    if (status.equals("Pending") && isPlayerInvited) {

                                        Log.i(TAG,"Found NEW matchID: "+document.getId());

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

                                        //MatchItem MI = new MatchItem(document.getId(), imageToUse, sport, date, -1, -1, R.drawable.baseline_check_24);
                                        MatchItem MI = new MatchItem(document.getId(), imageToUse, document.getId(), date, -1, -1, R.drawable.baseline_check_24);
                                        listMatchPending.add(MI);

                                    }
                                }
                                mAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }

                    });
        }
    }

         */




}

