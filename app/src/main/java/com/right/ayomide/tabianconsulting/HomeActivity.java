package com.right.ayomide.tabianconsulting;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Skiplab Innovation" );
        setSupportActionBar( toolbar );

        setupFirebaseAuth();

        getUserDetails();

        //setUserDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void setUserDetails()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUdates = new UserProfileChangeRequest.Builder()
                .setDisplayName( "Ayomide Ajayi" )
                .setPhotoUri(Uri.parse( "https://scontent.flos4-1.fna.fbcdn.net/v/t1.0-1/c0.0.320.320a/p320x320/64362122_103676957577336_8018274818564554752_n.jpg?_nc_cat=109&_nc_oc=AQltaIEDHJulpwMR5WzJCsRNzLtx0hKtkspHq7HfIyTSYFCDeVtCLNChWazUw0dv63g&_nc_ht=scontent.flos4-1.fna&oh=8a287f810bc997d87dd26533946c60bb&oe=5E3B1AA5" ))
                .build();

        user.updateProfile( profileUdates ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Log.d( TAG, "onComplete: User profile updated");

                    getUserDetails();
                }
            }
        } );
    }

    private void getUserDetails()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            String uid = user.getUid();
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            String properties = "uid: " + uid + "\n" +
                    "name: " + name + "\n" +
                    "email: " + email + "\n" +
                    "photourl " + photoUrl;

            Log.d( TAG, "getUserDetails: \n" + properties );
        }
    }

    private void checkAuthenticationState()
    {
        Log.d( TAG, "checkAuthenticationState: check authentication state." );

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null)
        {
            Log.d( TAG, "checkAuthenticationState: user is null, navigating back to login screen" );

            Intent intent = new Intent( HomeActivity.this, Authentication.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity( intent );
            finish();
        } else {
            Log.d( TAG, "checkAuthenticationState: user is authenticated." );
        }
    }

    private void setupFirebaseAuth()
    {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null)
                {
                    if (user.isEmailVerified())
                    {
                        Log.d( TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                        Toast.makeText( HomeActivity.this, "Authenticated with: "+ user.getEmail(),
                                Toast.LENGTH_SHORT ).show();
                    }
                    else
                    {
                        Toast.makeText( HomeActivity.this, "Check your email for a Verification link "+ user.getEmail(),
                                Toast.LENGTH_SHORT ).show();
                        FirebaseAuth.getInstance().signOut();
                    }

                } else {
                    Log.d( TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        super.onOptionsItemSelected( item );

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent( HomeActivity.this, Authentication.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity( intent );
            finish();
        }
        if(item.getItemId() == R.id.action_settings){
            startActivity( new Intent( this, SettingsActivity.class ) );
        }

        return true;
    }
}
