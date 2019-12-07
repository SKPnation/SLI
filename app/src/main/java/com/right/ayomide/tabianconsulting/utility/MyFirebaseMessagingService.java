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
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
import com.right.ayomide.tabianconsulting.models.Chatroom;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    private int mNumPendingMessages = 0;

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

        //SITUATION: Application is in foreground then only send priority notifications such as an admin notification
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
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));

                sendBroadcastNotification(title, message);

            }
            else if (identifyDataType.equals( getString( R.string.data_type_chat_message ) )){
                //build chat message notification
                final String title = remoteMessage.getData().get(getString(R.string.data_title));
                final String message = remoteMessage.getData().get(getString(R.string.data_message));
                final String chatroomId = remoteMessage.getData().get(getString(R.string.data_chatroom_id));

                Log.d(TAG, "onMessageReceived: chatroom id: " + chatroomId);
                Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_chatrooms))
                        .orderByKey()
                        .equalTo(chatroomId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.getChildren().iterator().hasNext()){
                            DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();

                            Chatroom chatroom = new Chatroom();
                            Map<String, Object> objectMap = (HashMap<String, Object>) snapshot.getValue();

                            chatroom.setChatroom_id( objectMap.get( "chatroom_id" ).toString() );
                            chatroom.setChatroom_name( objectMap.get( "chatroom_name" ).toString() );
                            chatroom.setChatroom_code( objectMap.get( "chatroom_code" ).toString() );
                            chatroom.setCreator_id( objectMap.get( "creator_id" ).toString() );

                            Log.d(TAG, "onDataChange: chatroom: " + chatroom);

                            int numMessagesSeen = Integer.parseInt(snapshot
                                    .child(getString(R.string.field_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.field_last_message_seen))
                                    .getValue().toString());

                            int numMessages = (int) snapshot
                                    .child(getString(R.string.field_chatroom_messages)).getChildrenCount();

                            mNumPendingMessages = (numMessages - numMessagesSeen);
                            Log.d(TAG, "onDataChange: num pending messages: " + mNumPendingMessages);


                            sendChatmessageNotification(title, message, chatroom);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void sendChatmessageNotification(String title, String message, Chatroom chatroom)
    {
        Log.d(TAG, "sendChatmessageNotification: building a chatmessage notification");

        //get the notification id
        int notificationId = buildNotificationId(chatroom.getChatroom_id());

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_name));
        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(this, HomeActivity.class);
        // Sets the Activity to start in a new, empty task
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setSmallIcon(R.drawable.sk_p_cropped)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.sk_p_cropped))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(title)
                    .setContentText("New messages in " + chatroom.getChatroom_name())
                    .setColor(getColor(R.color.colorPrimary))
                    .setAutoCancel(true)
                    .setSubText(message)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("New messages in " + chatroom.getChatroom_name()).setSummaryText(message))
                    .setNumber(mNumPendingMessages);
    //				.setOnlyAlertOnce(true)
        }

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Since Android Oreo, you need a Channel to send your notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);

            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        mNotificationManager.notify(notificationId, builder.build());
    }

    private int buildNotificationId(String id)
    {
        Log.d(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(TAG, "buildNotificationId: id: " + id);
        Log.d(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }


    /**
      ----------Send broadcast message--------------
     */
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
