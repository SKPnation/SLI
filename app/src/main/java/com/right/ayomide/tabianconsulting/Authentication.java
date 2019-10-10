package com.right.ayomide.tabianconsulting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Authentication extends AppCompatActivity {

    private static final String TAG = "Authentication";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    TextInputEditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvRegister, resendVerification;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_authentication );
        etEmail = findViewById( R.id.etEmail );
        etPassword = findViewById( R.id.etPassword );
        btnSignIn = findViewById( R.id.btnSignIn );
        tvRegister = findViewById( R.id.tvRegister );
        resendVerification = findViewById( R.id.tvVerification );

        //Now the listener will be actively listening for changes in the authentication state
        setupFirebaseAuth();

        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = new ProgressDialog( Authentication.this );
                mProgressDialog.setMessage( "Loading..." );
                mProgressDialog.show();

                //check for null valued editText fields
                if (!TextUtils.isEmpty( etEmail.getText().toString() ) &&
                        !TextUtils.isEmpty( etPassword.getText().toString() ))
                {
                    Log.d(TAG, "onClick: attempting to authenticate");

                    //Initiate Firebase Auth
                    FirebaseAuth.getInstance().signInWithEmailAndPassword( etEmail.getText().toString(),
                            etPassword.getText().toString() )
                            .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    mProgressDialog.dismiss();
                                }
                            } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText( Authentication.this, "Authentication failed", Toast.LENGTH_SHORT ).show();
                        }
                    } );

                } else {
                    mProgressDialog.dismiss();
                    etEmail.setError( "Both fields are required" );
                    etPassword.setError( "Both fields are required" );
                    etEmail.requestFocus();
                    etPassword.requestFocus();
                    return;
                }
            }
        } );

        tvRegister.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent( Authentication.this, RegisterActivity.class ) );
            }
        } );

        resendVerification.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationDialog();
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
                        Toast.makeText( Authentication.this, "Sent verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText( Authentication.this, "Couldn't send verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }
    }

    private void resendVerificationDialog()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder( this );

        LayoutInflater inflater = this.getLayoutInflater();
        View resend_verification_layout = inflater.inflate( R.layout.resend_verification_layout, null );
        dialog.setView( resend_verification_layout );

        final EditText confirmEmail = resend_verification_layout.findViewById( R.id.email );
        final EditText confirmPassword = resend_verification_layout.findViewById( R.id.password );
        final TextView cancel = resend_verification_layout.findViewById( R.id.cancel );
        TextView confirm = resend_verification_layout.findViewById( R.id.confirm );

        confirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!confirmEmail.getText().toString().isEmpty() && !confirmPassword.getText().toString().isEmpty())
                {
                    authenticateAndResendEmail(confirmEmail.getText().toString(), confirmPassword.getText().toString());
                } else {
                    confirmEmail.setError( "Both fields are required" );
                    confirmPassword.setError( "Both fields are required" );
                    confirmEmail.requestFocus();
                    confirmPassword.requestFocus();
                    return;
                }
            }

        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        } );

        dialog.show();
    }

    private void authenticateAndResendEmail(String email, String password)
    {
        mProgressDialog = new ProgressDialog( Authentication.this );
        mProgressDialog.setMessage( "Loading..." );
        mProgressDialog.show();

        AuthCredential credential = EmailAuthProvider.getCredential( email, password );
        FirebaseAuth.getInstance().signInWithCredential( credential ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Log.d( TAG, "onComplete: reauthenticate success.");
                    sendVerificationEmail();
                    FirebaseAuth.getInstance().signOut();
                    mProgressDialog.dismiss();
                }
            }
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText( getBaseContext(), "Invalid Credentials \nReset your password and try again",
                        Toast.LENGTH_SHORT ).show();
                mProgressDialog.dismiss();
            }
        } );
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
                        Toast.makeText( Authentication.this, "Authenticated with: "+ user.getEmail(),
                                Toast.LENGTH_SHORT ).show();

                        Intent intent = new Intent( Authentication.this, HomeActivity.class );
                        startActivity( intent );
                        finish();
                    }
                    else
                        {
                            Toast.makeText( Authentication.this, "Check your email for a Verification link "+ user.getEmail(),
                                    Toast.LENGTH_SHORT ).show();
                            FirebaseAuth.getInstance().signOut();
                        }

                } else {
                    Log.d( TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    //Everything you need to use the authStateListener Object
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener( mAuthListener );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener( mAuthListener );
    }
    //Everything you need to use the authStateListener Object
}
