package com.right.ayomide.tabianconsulting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.right.ayomide.tabianconsulting.models.ChatMessage;
import com.right.ayomide.tabianconsulting.models.Chatroom;
import com.right.ayomide.tabianconsulting.models.User;
import com.right.ayomide.tabianconsulting.utility.ChatroomListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private ListView mListView;

    //Create new chatroom layout
    private EditText mChatroomName;

    //vars
    private ArrayList<Chatroom> mChatrooms;
    private ChatroomListAdapter mAdapter;
    private DatabaseReference mChatroomReference;
    public static boolean isActivityRunning;
    private HashMap<String, String> mNumChatroomMessages;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Chat Rooms" );
        setSupportActionBar( toolbar );

        mListView = findViewById( R.id.listView );

        getChatrooms();

        FloatingActionButton fab = (FloatingActionButton) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewGroup();
            }
        } );
    }

    private void CreateNewGroup()
    {
        AlertDialog.Builder builder= new AlertDialog.Builder(ChatActivity.this, R.style.AlertDialog);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialog_new_chatroom = inflater.inflate( R.layout.dialog_new_chatroom, null );
        builder.setView( dialog_new_chatroom );

        mChatroomName = (EditText) dialog_new_chatroom.findViewById(R.id.input_chatroom_name);

        builder.setPositiveButton( "Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {

                mProgressDialog = new ProgressDialog( ChatActivity.this );
                mProgressDialog.setMessage( "Creating..." );
                mProgressDialog.show();

                if (!mChatroomName.getText().toString().equals( "" )){
                    Log.d(TAG, "onClick: creating new chat room");

                    mChatroomReference = FirebaseDatabase.getInstance().getReference();
                    //get the new chatroom unique id
                    String chatroomId = mChatroomReference
                            .child(getString(R.string.dbnode_chatrooms))
                            .push().getKey();

                    //create the chatroom
                    Chatroom chatroom = new Chatroom();
                    chatroom.setChatroom_name(mChatroomName.getText().toString());
                    chatroom.setCreator_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    chatroom.setChatroom_id(chatroomId);

                    //insert the new chatroom into the database
                    mChatroomReference
                            .child(getString(R.string.dbnode_chatrooms))
                            .child(chatroomId)
                            .setValue(chatroom);

                    //create a unique id for the message
                    String messageId = mChatroomReference
                            .child(getString(R.string.dbnode_chatrooms))
                            .push().getKey();

                    //insert the first message into the chatroom
                    ChatMessage message = new ChatMessage();

                    message.setMessage("Welcome to the new chatroom!");
                    message.setTimestamp(getTimestamp());
                    mChatroomReference
                            .child(getString(R.string.dbnode_chatrooms))
                            .child(chatroomId)
                            .child(getString(R.string.field_chatroom_messages))
                            .child(messageId)
                            .setValue(message);
                    mProgressDialog.dismiss();
                    Toast.makeText(ChatActivity.this, "Chatroom saved to database", Toast.LENGTH_SHORT).show();
                    getChatrooms();

                }else {
                    Toast.makeText(ChatActivity.this, "name required", Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }

                /*
                RootRef.child( getString( R.string.dbnode_chatrooms ) )
                        .child( etChatroomName.getText().toString() )
                        .setValue( "" ).addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, etChatroomName.getText().toString() + " group is created successfully", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }

                    }
                } );
                */

            }
        } );

        builder.show();
    }


    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Britain/London"));
        return sdf.format(new Date());
    }

    private void getChatrooms()
    {
        Log.d( TAG, "getChatrooms: retrieving chatrooms from firebase database." );
        mChatrooms = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child( getString(R.string.dbnode_chatrooms ) );
        query.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found chatroom: "
                            + singleSnapshot.getValue());

                    Chatroom chatroom = new Chatroom();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    chatroom.setChatroom_id( objectMap.get( "chatroom_id" ).toString() );
                    chatroom.setChatroom_name( objectMap.get( "chatroom_name" ).toString() );
                    chatroom.setCreator_id( objectMap.get( "creator_id" ).toString() );

                    //chatroom.setChatroom_id(singleSnapshot.getValue(Chatroom.class).getChatroom_id());
                    //chatroom.setSecurity_level(singleSnapshot.getValue(Chatroom.class).getSecurity_level());
                    //chatroom.setCreator_id(singleSnapshot.getValue(Chatroom.class).getCreator_id());
                    //chatroom.setChatroom_name(singleSnapshot.getValue(Chatroom.class).getChatroom_name());

                    //get the chatroom messages
                    ArrayList<ChatMessage> messagesList = new ArrayList<>();
                    for (DataSnapshot snapshot: singleSnapshot
                            .child( "chatroom_messages" ).getChildren())
                    {
                        ChatMessage message = new ChatMessage();
                        message.setTimestamp(snapshot.getValue(ChatMessage.class).getTimestamp());
                        message.setUser_id(snapshot.getValue(ChatMessage.class).getUser_id());
                        message.setMessage(snapshot.getValue(ChatMessage.class).getMessage());
                        messagesList.add(message);
                    }
                    chatroom.setChatroom_messages(messagesList);
                    mChatrooms.add(chatroom);

                }
                setupChatroomList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //..
            }
        } );
    }

    private void setupChatroomList()
    {
        Log.d(TAG, "setupChatroomList: setting up chatroom listview");
        mAdapter = new ChatroomListAdapter(ChatActivity.this, R.layout.layout_chatroom_listitem, mChatrooms);
        mListView.setAdapter(mAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called.");
        checkAuthenticationState();
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

    private void checkAuthenticationState(){
        Log.d(TAG, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d(TAG, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(ChatActivity.this, Authentication.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d(TAG, "checkAuthenticationState: user is authenticated.");
        }
    }

    public void showDeleteChatroomDialog(final String chatroom_id)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( ChatActivity.this );
        alertDialog.setTitle( "Are you sure you want to delete this chatroom?" );

        alertDialog.setPositiveButton( "DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child( "chatrooms" )
                        .child( chatroom_id )
                        .removeValue();
                getChatrooms();
            }
        } );

        alertDialog.setNegativeButton( "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );

        alertDialog.show();
    }

    public void joinChatroom(final Chatroom chatroom)
    {
        //make sure the chatroom exists before joining
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_chatrooms)).orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    if(objectMap.get(getString(R.string.field_chatroom_id)).toString()
                            .equals(chatroom.getChatroom_id())){
                        Log.d(TAG, "onItemClick: selected chatroom: " + chatroom.getChatroom_id());

                        //add user to the list of users who have joined the chatroom
                        //addUserToChatroom(chatroom);

                        //navigate to the chatoom
                        Intent intent = new Intent(ChatActivity.this, ChatroomActivity.class);
                        intent.putExtra(getString(R.string.intent_chatroom), chatroom);
                        startActivity(intent);
                    }
                }
                //getChatrooms();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
/*
    private void addUserToChatroom(Chatroom chatroom)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child(getString(R.string.dbnode_chatrooms))
                .child(chatroom.getChatroom_id())
                .child(getString(R.string.field_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_last_message_seen))
                .setValue(mNumChatroomMessages.get(chatroom.getChatroom_id()));
    } */
}
