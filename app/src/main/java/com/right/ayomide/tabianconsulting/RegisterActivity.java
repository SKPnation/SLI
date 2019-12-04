package com.right.ayomide.tabianconsulting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.right.ayomide.tabianconsulting.models.User;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    //private static final String DOMAIN_NAME = "skiplab.com.ng";

    //widgets
    private EditText etEmail, etPassword, etConfirmPwd;
    private Button btnRegister;
    private ProgressDialog mProgressDialog;

    //vars
    public static boolean isActivityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        etEmail = findViewById( R.id.etEmail );
        etPassword = findViewById( R.id.etPassword );
        etConfirmPwd = findViewById( R.id.etConfirmPwd );
        btnRegister = findViewById( R.id.btnRegister );

        btnRegister.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = new ProgressDialog( RegisterActivity.this );
                mProgressDialog.setMessage( "Loading..." );
                mProgressDialog.show();

                //check for null valued editText fields
                if (!TextUtils.isEmpty( etEmail.getText().toString() ) &&
                        !TextUtils.isEmpty( etPassword.getText().toString() ) &&
                        !TextUtils.isEmpty( etConfirmPwd.getText().toString() ))
                {
                    //check if user has a company email address
                    //if (isValidDomain(etEmail.getText().toString())){}
                        //check if passwords match
                        if (etConfirmPwd.getText().toString().equals( etPassword.getText().toString() ))
                        {
                            mProgressDialog.dismiss();
                            registerNewEmail( etEmail.getText().toString(), etPassword.getText().toString() );
                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText( RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT ).show();
                        }
                        //mProgressDialog.dismiss();
                        //Toast.makeText( RegisterActivity.this, "Please Register with Company email", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressDialog.dismiss();
                    etEmail.setError( "All fields are required" );
                    etPassword.setError( "All fields are required" );
                    etConfirmPwd.setError( "All fields are required" );
                    etEmail.requestFocus();
                    etPassword.requestFocus();
                    etConfirmPwd.requestFocus();
                    return;
                }
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
                        Toast.makeText( RegisterActivity.this, "Sent verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText( RegisterActivity.this, "Couldn't send verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }
    }

    private void registerNewEmail(final String email, String password)
    {
        mProgressDialog = new ProgressDialog( RegisterActivity.this );
        mProgressDialog.setMessage( "Loading..." );
        mProgressDialog.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword( email, password ).addOnCompleteListener(
                RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                Log.d( TAG, "onComplete: onComplete: " + task.isSuccessful());

                if (task.isSuccessful())
                {
                    mProgressDialog.dismiss();
                    Log.d( TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser()
                            .getUid());

                    // Send email verification
                    sendVerificationEmail();

                    User user = new User();
                    user.setName( email.substring( 0, email.indexOf( "@" ) ) );
                    user.setPhone( "1" );
                    user.setProfile_image( "" );
                    user.setUser_id( FirebaseAuth.getInstance().getCurrentUser().getUid() );

                    FirebaseDatabase.getInstance().getReference()
                            .child( getString( R.string.dbnode_users ) )
                            .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                            .setValue( user ).addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity( new Intent( RegisterActivity.this, Authentication.class ) );
                        }
                    } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity( new Intent( RegisterActivity.this, Authentication.class ) );
                            task.getException().getMessage();
                            //Toast.makeText( RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT ).show();
                        }
                    } );

                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText( RegisterActivity.this, "Unable to Register", Toast.LENGTH_SHORT).show();
                }

            }
        } );
    }



    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

/*
    private boolean isValidDomain(String email)
    {
        Log.d(TAG, "isValidDomain: verifying email has correct domain: " + email);
        String domain = email.substring( email.indexOf( "@" )+1 ).toLowerCase();
        Log.d(TAG, "isValidDomain: users domain: " + domain);
        return domain.equals( DOMAIN_NAME );
    }
    */
}
