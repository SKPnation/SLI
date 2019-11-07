package com.right.ayomide.tabianconsulting.utility;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.right.ayomide.tabianconsulting.R;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "onTokenRefresh: Refreshed token: " + refreshedToken);

        //send the token to the database
        SendRegistrationToServer(refreshedToken);
    }

    private void SendRegistrationToServer(String token){
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child( getString( R.string.dbnode_users ) )
                .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                .child( getString( R.string.field_messaging_token ) )
                .setValue( token );
    }
}
