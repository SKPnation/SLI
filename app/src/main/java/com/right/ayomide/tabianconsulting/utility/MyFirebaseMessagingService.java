package com.right.ayomide.tabianconsulting.utility;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.right.ayomide.tabianconsulting.AdminActivity;
import com.right.ayomide.tabianconsulting.Authentication;
import com.right.ayomide.tabianconsulting.ChatActivity;
import com.right.ayomide.tabianconsulting.ChatroomActivity;
import com.right.ayomide.tabianconsulting.HomeActivity;
import com.right.ayomide.tabianconsulting.R;
import com.right.ayomide.tabianconsulting.RegisterActivity;
import com.right.ayomide.tabianconsulting.SettingsActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";


    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";

        try {
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }catch (NullPointerException e){
            Log.e( TAG, "onMessageReceived: NullPointerException: " + e.getMessage() );
        }


        Log.d(TAG, "onMessageReceived: data: " + notificationData);
        Log.d(TAG, "onMessageReceived: notification body: " + notificationBody);
        Log.d(TAG, "onMessageReceived: notification title: " + notificationTitle);

        String identifyDataType = remoteMessage.getData().get(getString(R.string.data_type));
        //SITUATION: Application is in foreground then only send priority notificaitons such as an admin notification
        if(isApplicationInForeground()){
            if(identifyDataType.equals(getString(R.string.data_type_admin_broadcast))){
                //build admin broadcast notification
                String title = remoteMessage.getData().get(getString( R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                sendBroadcastNotification(title, message);
            }
        }

        //SITUATION: Application is in background or closed
        else if(!isApplicationInForeground()){
            if (identifyDataType.equals( getString( R.string.data_type_admin_broadcast ) )){
                //build admin broadcast notification

            }
            else if (identifyDataType.equals( getString( R.string.data_type_chat_message ) )){
                //build chat message notification
            }
        }
    }

    private void sendBroadcastNotification(String title, String message)
    {
        Log.d(TAG, "sendBroadcastNotification: building an admin broadcast notification");

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent(this, HomeActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setSmallIcon( R.drawable.sk_p_cropped )
                    .setLargeIcon( BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.sk_p_cropped))
                    .setSound( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION ) )
                    .setContentTitle( title )
                    .setContentText( message )
                    .setColor( getColor( R.color.colorPrimary ) )
                    .setAutoCancel(true);
        }

        builder.setContentIntent(notifyPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE);

        //Since Android Oreo, you need a Channel to send your notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);

            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

            mNotificationManager.notify(BROADCAST_NOTIFICATION_ID, builder.build());
    }

    private boolean isApplicationInForeground(){
        //check all the activities to see if any of them are running
        boolean isActivityRunning = HomeActivity.isActivityRunning
                || ChatActivity.isActivityRunning || AdminActivity.isActivityRunning
                || ChatroomActivity.isActivityRunning || Authentication.isActivityRunning
                || RegisterActivity.isActivityRunning || SettingsActivity.isActivityRunning;
        if(isActivityRunning) {
            Log.d(TAG, "isApplicationInForeground: application is in foreground.");
            return true;
        }
        Log.d(TAG, "isApplicationInForeground: application is in background or closed.");
        return false;
    }


}
