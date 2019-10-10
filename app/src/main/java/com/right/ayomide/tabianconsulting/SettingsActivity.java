package com.right.ayomide.tabianconsulting;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.right.ayomide.tabianconsulting.models.User;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";


    //private static final String DOMAIN_NAME = "skiplab.com.ng";
    private static final int GalleryPick = 1234;
    private StorageReference UserProfileImagesRef;

    private CircleImageView mProfileImage;
    private EditText etName, etPhone, etEmail, etPassword;
    private Button btnSave;
    private TextView change_password;
    private ProgressDialog mProgressDialog;

    //vars
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_settings );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Account Settings" );
        setSupportActionBar( toolbar );
        Log.d( TAG, "onCreate: started.");

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mProfileImage = findViewById( R.id.profile_image );
        etName = findViewById( R.id.etName );
        etPhone = findViewById( R.id.etPhone );
        etEmail = findViewById( R.id.etEmail );
        etPassword = findViewById( R.id.etPassword );
        btnSave = findViewById( R.id.btnSave );
        change_password = findViewById( R.id.change_password );

        verifyStoragePermissions();
        getAccountsData();

        btnSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d( TAG, "onClick: attempting to save settings." );

                //if current email does not equals what's in the EditText field then attempt to edit
                if (!etEmail.getText().toString().equals( FirebaseAuth.getInstance().getCurrentUser().getEmail() )){
                    //make sure the email & current password fields are field
                    if (!etEmail.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty())
                    {
                    /*
                    --------- Change email task -------
                     */

                            //Verify that user is changing to a company email address
                            //if (isValidDomain(etEmail.getText().toString())){}
                            editUserEmail();

                    } else {
                        Toast.makeText( SettingsActivity.this, "Email and Current password must be filled before saving", Toast.LENGTH_SHORT ).show();
                    }

                }
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                /*
                    -------Change Name--------
                */
                if (!etName.getText().toString().equals( "" )){
                    mProgressDialog = new ProgressDialog( SettingsActivity.this );
                    mProgressDialog.setMessage( "Loading..." );
                    mProgressDialog.show();

                    reference.child( getString( R.string.dbnode_users ) )
                            .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                            .child( getString( R.string.field_name ) )
                            .setValue( etName.getText().toString() );
                    mProgressDialog.dismiss();
                }

                /*
                    --------Change Phone Number--------
                 */
                if (!etPhone.getText().toString().equals( "" )){
                    reference.child( getString( R.string.dbnode_users ) )
                            .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                            .child( getString( R.string.field_phone ) )
                            .setValue( etPhone.getText().toString() );
                    mProgressDialog.dismiss();
                }

                Toast.makeText( SettingsActivity.this, "Saved", Toast.LENGTH_SHORT ).show();
            }
        } );


        change_password.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: sending password reset link");
                /*
                ------ Reset Password link --------
                 */
                sendResendPasswordLink();
            }
        } );

        mProfileImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStoragePermissions){
                    ToAccessPhotoGallery();
                }else{
                    verifyStoragePermissions();
                }
            }
        } );
    }

    private void ToAccessPhotoGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                final ProgressDialog mDialog = new ProgressDialog( this );
                mDialog.setMessage( "Uploading..." );
                mDialog.show();

                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImagesRef.child( FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                FirebaseDatabase.getInstance().getReference()
                                        .child( getString( R.string.dbnode_users ) )
                                        .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                                        .child( getString( R.string.field_profile_image ) )
                                        .setValue( downloadUrl ).addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                        }
                                    }
                                } );
                            }
                        } );
                    }
                } );
            }

        }
    }

    private void getAccountsData()
    {
        Log.d( TAG, "getUserAccountsData: getting the user's account information" );

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        /*
            ---------- Query method 1 --------------
         */
        Query query1 = reference.child( getString( R.string.dbnode_users ) )
                .orderByKey()
                .equalTo( FirebaseAuth.getInstance().getCurrentUser().getUid() );

        //orderByKey method will look for the key encapsulating the values of the object

        query1.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren() ){
                    User user = singleSnapshot.getValue(User.class);
                    Log.d( TAG, "onDataChange: (QUERY METHOD 1) found user: " + user.toString());

                    etName.setText( user.getName() );
                    etPhone.setText( user.getPhone() );
                    Picasso.with( getBaseContext() ).load(user.getProfile_image()).into( mProfileImage );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        /*
            ---------- Query method 2 --------------
         */
        Query query2 = reference.child( getString( R.string.dbnode_users ) )
                .orderByChild(getString( R.string.field_user_id ))
                .equalTo( FirebaseAuth.getInstance().getCurrentUser().getUid() );

        //orderByChild will look for the name of that field which would be user_id
        //therefore now we are looking for the user id in the field instead of in the key using the .equalTo method

        query2.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren() ){
                    User user = singleSnapshot.getValue(User.class);
                    Log.d( TAG, "onDataChange: (QUERY METHOD 2) found user: " + user.toString());

                    etName.setText( user.getName() );
                    etPhone.setText( user.getPhone() );
                    Picasso.with( getBaseContext() ).load(user.getProfile_image()).into( mProfileImage );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        /*
            ---------- Query method 3 --------------
         */
        Query query3 = reference.child( getString( R.string.dbnode_users ) )
                .orderByValue()
                .equalTo( FirebaseAuth.getInstance().getCurrentUser().getUid() );

        //orderByChild will look for the name of that field which would be user_id
        //therefore now we are looking for the user id in the field instead of in the key using the .equalTo method

        query3.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren() ){
                    User user = singleSnapshot.getValue(User.class);
                    Log.d( TAG, "onDataChange: (QUERY METHOD 3) found user: " + user.toString());

                    etName.setText( user.getName() );
                    etPhone.setText( user.getPhone() );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

        etEmail.setText( FirebaseAuth.getInstance().getCurrentUser().getEmail() );
    }


    private void editUserEmail()
    {
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers
        // such as googleAuthProvider or facebookAuthProvider

        mProgressDialog = new ProgressDialog( SettingsActivity.this );
        mProgressDialog.setMessage( "Loading..." );
        mProgressDialog.show();

        AuthCredential credential = EmailAuthProvider
                .getCredential( FirebaseAuth.getInstance().getCurrentUser().getEmail(), etPassword.getText().toString() );

        FirebaseAuth.getInstance().getCurrentUser().reauthenticate( credential )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Log.d( TAG, "onComplete: reauthenticate success" );

                            //make sure the domain is valid
                            //if(isValidDomain(etEmail.getText().toString())){}
                            //else{Toast.makeText(getBaseContext,"you must use company email"}


                            //////Check to see if the email is not already present in the database
                            FirebaseAuth.getInstance().fetchProvidersForEmail( etEmail.getText().toString() )
                                    .addOnCompleteListener( new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if (task.isSuccessful())
                                    {
                                        /////getProviders().size() will return size 1 if email ID is in use
                                        Log.d( TAG, "onComplete: RESULT: " + task.getResult().getProviders().size() );
                                        if (task.getResult().getProviders().size() == 1)
                                        {
                                            Log.d( TAG, "onComplete: That email is already in use." );
                                            mProgressDialog.dismiss();
                                            Toast.makeText( SettingsActivity.this, "That email is already in use", Toast.LENGTH_SHORT ).show();
                                        } else {
                                            Log.d( TAG, "onComplete: That email is available" );

                                            /////////////////////add new email
                                            FirebaseAuth.getInstance().getCurrentUser().updateEmail( etEmail.getText().toString() )
                                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                Log.d( TAG, "onComplete: User email address updated." );
                                                                Toast.makeText( SettingsActivity.this, "Updated email", Toast.LENGTH_SHORT ).show();
                                                                sendVerificationEmail();
                                                                FirebaseAuth.getInstance().signOut();
                                                                mProgressDialog.dismiss();
                                                            } else {
                                                                Log.d( TAG, "onComplete: Could not update email" );
                                                                Toast.makeText( SettingsActivity.this, "unable to update email", Toast.LENGTH_SHORT ).show();
                                                            }
                                                            mProgressDialog.dismiss();
                                                        }
                                                    } )
                                                    .addOnFailureListener( new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText( SettingsActivity.this, "Unable to update email", Toast.LENGTH_SHORT ).show();
                                                }
                                            } );
                                        }
                                    }
                                }
                            } );
                        } else {
                            Log.d( TAG, "onComplete: Incorrect password" );
                            Toast.makeText( SettingsActivity.this, "Incorrect password", Toast.LENGTH_SHORT ).show();
                            mProgressDialog.dismiss();
                        }
                    }
                } )
                .addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText( SettingsActivity.this, "Unable to update email", Toast.LENGTH_SHORT ).show();

            }
        } );
    }

    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            user.sendEmailVerification().addOnCompleteListener( new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText( SettingsActivity.this, "Sent verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText( SettingsActivity.this, "Couldn't send verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }
    }

    private void sendResendPasswordLink()
    {
        FirebaseAuth.getInstance().sendPasswordResetEmail( FirebaseAuth.getInstance().getCurrentUser().getEmail() )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Log.d( TAG, "onComplete: Password Reset Email sent" );
                            Toast.makeText( SettingsActivity.this, "Sent Password Reset Link to Email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d( TAG, "onComplete: No User Associated with that Email" );

                            Toast.makeText( SettingsActivity.this, "No User Associated with that Email",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } );
    }

    /**
     * General method for asking permissions. Can pass any array of permissions
     */
    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission( this.getApplicationContext(), permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( this.getApplicationContext(), permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission( this.getApplicationContext(), permissions[2] ) == PackageManager.PERMISSION_GRANTED){
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    SettingsActivity.this,
                    permissions,
                    GalleryPick
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);

        switch (requestCode){
            case GalleryPick:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d( TAG, "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0] );
                }
                break;
        }
    }
}
