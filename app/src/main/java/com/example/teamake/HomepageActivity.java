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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

public class HomepageActivity extends AppCompatActivity  {


    FirebaseAuth auth;
    FirebaseUser userLogged;
    TextView profileNameTV;

    ImageView imageViewProfile;
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
    private static final Integer UriCode = 0;

    private int permissionCheckRead = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main);
        profileNameTV = findViewById(R.id.profileNameTV);
        imageViewProfile = findViewById(R.id.imageViewMainPic);
        permissionCheckRead = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);


        auth = FirebaseAuth.getInstance();
        userLogged = auth.getCurrentUser();
        if(userLogged == null){
            Intent backToLogin =  new Intent(getApplicationContext(),MainActivity.class);
            startActivity(backToLogin);
            finish();
        }else{
            profileNameTV.setText(userLogged.getEmail());
        }

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

            Log.i(TAG,"Granted PEermission with alert dialog");
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }
    }

}

