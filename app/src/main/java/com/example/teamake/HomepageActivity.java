package com.example.teamake;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.Set;


//Todo
// 1) Build service as listener notification
// 2) cache img


public class HomepageActivity extends AppCompatActivity  {


    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final StorageReference FireStorage = FirebaseStorage.getInstance().getReference();
    private final FirebaseFirestore FireDb = FirebaseFirestore.getInstance();

    private CollectionReference Notifications = FireDb.collection("Notifications");
    private CollectionReference Rides = FireDb.collection("Matches");

    FirebaseUser userLogged;
    TextView profileNameTV, universityTv, offerRide, lookingForRide, myRides;

    Button logoutBtn;

    ImageView imageViewProfile;

    // NOTIFICATIONS PENDING MATCHES
    ArrayList<RideItem> listInvitePending;
    RecyclerView invitesRidePending;
    RidesAdapter iAdapter;
    private static final String TAG = "HomepageActivity";
    private static final Integer MY_PERMISSIONS_REQUEST_READ_MEDIA = 0;
    private final String collectionInfoUser = "UserBasicInfo";

    private int permissionCheckRead = 0 ;

    HashMap<String,String> linkingNotificationRides = new HashMap<>();

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



    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main);


        profileNameTV = findViewById(R.id.profileNameTV);
        lookingForRide = findViewById(R.id.textRideReceiveTv);
        offerRide = findViewById(R.id.textRideOfferTv);
        myRides = findViewById(R.id.myRidesTv);
        universityTv = findViewById(R.id.myUniversity);
        imageViewProfile = findViewById(R.id.imageViewMainPic);
        logoutBtn = findViewById(R.id.buttonLogout);

        invitesRidePending = findViewById(R.id.pendingInvites);
        listInvitePending = new ArrayList<>();
        invitesRidePending.setHasFixedSize(true);
        //listMatchPending.add(new RideItem("xxx", R.drawable.baseline_sports_basketball_24, "basket", "22", 0, 0, R.drawable.baseline_check_24));
        invitesRidePending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        iAdapter = new RidesAdapter(listInvitePending);
        invitesRidePending.setAdapter(iAdapter);

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
                        String university = task.getResult().get("University").toString();


                        Log.i(TAG,"LOGGED USER NICK:"+nickname);
                        profileNameTV.setText(nickname);
                        universityTv.setText(university);

                        //Add sync task -- login sergio -- but it shows Kamado
                    } else {
                        Log.d(TAG, "Error getting documents University : ", task.getException());
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

        offerRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCreateRide= new Intent(getApplicationContext(), CreateRideActivity.class);
                startActivity(intentCreateRide);
            }
        });

        myRides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentViewRide= new Intent(getApplicationContext(), RidesActivity.class);
                startActivity(intentViewRide);
            }
        });

        lookingForRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentViewRide= new Intent(getApplicationContext(), LookupRideActivity.class);
                startActivity(intentViewRide);
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

    

    protected  void PopulateRecyclerView(ArrayList<String> invitesRide){

        Log.i(TAG,"DISPLAYING ALL PENDING REQUEST RIDE  FOR: "+userLogged.getUid());
        Log.i(TAG, String.valueOf(invitesRide.size()) + invitesRide);
        if(!invitesRide.isEmpty()) {
            Rides
                    .whereIn(FieldPath.documentId(), invitesRide)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String status = document.getString("Status");
                                    HashMap<String,ArrayList<String>> passengers = ((HashMap<String,ArrayList<String>> ) document.get("Passengers"));
                                    boolean isPlayerInvited = false;

                                    List<String> passengersKeys = new ArrayList<>(passengers.keySet());

                                    //if (passengersKeys.contains(userLogged.getUid())) isPlayerInvited = true;
                                    if (status.equals("Pending")) {

                                        for(String localPass : passengersKeys){
                                            if(!localPass.equals("NULL")) {
                                                RideItem RI = CreateRideEntry(document, localPass);
                                                //RideItem RI = new RideItem(document.getId(), imageToUse, sport, date, -1, -1, R.drawable.baseline_check_24);
                                                listInvitePending.add(RI);
                                            }
                                        }


                                    }
                                }
                                //LinkAdapterToList();
                                iAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }

                    });
        }
    }


    protected void ManageOnClickMatch(){
        iAdapter.setOnItemClickLister(new RidesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String rideId = listInvitePending.get(position).getRideID();
                String passengerId = listInvitePending.get(position).getPassenger();
                Log.i(TAG,"Accepting ride adapter: "+ rideId);
                Rides
                        .whereEqualTo(FieldPath.documentId(),rideId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                HashMap<String,String> passengersToUpdate = new HashMap<>();
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        passengersToUpdate = ((HashMap<String, String>) document.get("Passengers"));
                                        passengersToUpdate.put(passengerId, "Accepted");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                                ModifyMatchPassengers(passengersToUpdate,rideId,position);
                            }
                        });
            }
        });
    }

    private void ModifyStatusPassenger(HashMap<String,String> playerToUpdate) {
        //String props = playerToUpdate.get(userLogged.getUid());
        //props.set(1, "Accepted");
        playerToUpdate.put(userLogged.getUid(),"Accepted");
        Log.i(TAG,"Setting map changes for: "+userLogged.getUid()+"--"+ playerToUpdate.get(userLogged.getUid()));
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
        if(userLogged == null) {
            Intent backToLogin = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(backToLogin);
            finish();
        }
        buildViewMatches();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(userLogged == null) {
            Intent backToLogin = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(backToLogin);
            finish();
        }
        //RidesManager.CheckForAllConfirmedPassengers();
    }


    private void buildViewMatches() {
        Log.i(TAG,"ON RESUME , Checking new notifications");
        ArrayList<String> newRides = new ArrayList<>();
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
                                String localRide = document.getString("id_ride");

                                if(!localUID.equals(userLogged.getUid()) && !localStatus.equals("unread")) continue;

                                if(!linkingNotificationRides.containsKey(localRide)) {
                                    Log.i(TAG,"ADDING NEW Ride: "+localRide);
                                    Log.i(TAG,"ADDING NEW Notification: "+localNotification);

                                    linkingNotificationRides.put(localRide,localNotification);
                                    newRides.add(document.getString("id_ride"));
                                }
                            }
                        }
                        Log.d(TAG, "new rides: " + newRides);
                        Log.i(TAG, String.valueOf(newRides.size()));
                        if(!newRides.isEmpty()) {
                            Log.i(TAG, "calling populate recy for : " + userLogged.getUid());
                            PopulateRecyclerView(newRides);
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    protected void ModifyMatchPassengers(HashMap<String,String> mapToUpload, String rideID, int position){
        Rides.document(rideID).update("Passengers",mapToUpload).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                String notificationToRemove = null;

                if (task.isSuccessful()) {
                    Log.i(TAG,"UPDATED passengers map of ride: "+rideID);
                    notificationToRemove = linkingNotificationRides.get(rideID);
                }
                else {
                    Log.d(TAG, "get failed with ", task.getException());
                }

                if(notificationToRemove != null) RemoveNotificationFromDbList(rideID,notificationToRemove,position);

                List<String> key = new ArrayList<>(mapToUpload.keySet());
                RidesManager.CheckForAllConfirmedPassengers(key.get(0),rideID);
            }
        });
    }


    protected RideItem CreateRideEntry(QueryDocumentSnapshot document,String pass){

        Log.i(TAG,"Found rideID: "+document.getId());

        String date = document.getString("Date");
        String time = document.getString("Time");
        String University = document.getString("University");

        return new RideItem(document.getId(), R.drawable.baseline_school_24, University, date, time, pass, R.drawable.baseline_check_24);

    }


    protected void RemoveNotificationFromDbList(String matchID,String notificationToRemove,int position){
        Notifications.document(notificationToRemove)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Notification: "+ notificationToRemove +" successfully deleted!");
                        linkingNotificationRides.remove(matchID);
                        listInvitePending.remove(position);
                        iAdapter.notifyItemChanged(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });


    }

}

