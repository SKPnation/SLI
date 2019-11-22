package com.right.ayomide.tabianconsulting.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.right.ayomide.tabianconsulting.ChatActivity;
import com.right.ayomide.tabianconsulting.R;
import com.right.ayomide.tabianconsulting.models.Chatroom;
import com.right.ayomide.tabianconsulting.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ChatroomListAdapter extends ArrayAdapter<Chatroom> {

    private static final String TAG = "ChatroomListAdapter";

    private int mLayoutResource;
    private Context mContext;
    private LayoutInflater mInflater;

    public ChatroomListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Chatroom> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static class ViewHolder{
        TextView name, creatorName, numberMessages;
        ImageView mProfileImage, mTrash;
        Button leaveChat;
        RelativeLayout layoutContainer;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null)
        {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.creatorName = (TextView) convertView.findViewById(R.id.creator_name);
            holder.numberMessages = (TextView) convertView.findViewById(R.id.number_chatmessages);
            holder.mProfileImage = (ImageView) convertView.findViewById(R.id.profile_image);
            holder.mTrash = (ImageView) convertView.findViewById(R.id.icon_trash);
            holder.leaveChat = (Button) convertView.findViewById(R.id.leave_chat);
            holder.layoutContainer = (RelativeLayout) convertView.findViewById(R.id.layout_container);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            //set the chatroom name
            holder.name.setText(getItem(position).getChatroom_name());

            //set the number of chat messages
            final String chatMessagesString = String.valueOf(getItem(position).getChatroom_messages().size())
                    + " messages";
            holder.numberMessages.setText(chatMessagesString);

            //get the users details who created the chatroom
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(mContext.getString(R.string.dbnode_users))
                    .orderByKey()
                    .equalTo(getItem(position).getCreator_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot:  dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: Found chat room creator: "
                                + singleSnapshot.getValue(User.class).getName());
                        String createdBy = "created by " + singleSnapshot.getValue(User.class).getName();
                        holder.creatorName.setText(createdBy);
                        if (singleSnapshot.getValue(User.class).getProfile_image().isEmpty())
                        {
                            holder.mProfileImage.setImageDrawable( ContextCompat.getDrawable(mContext, R.drawable.ic_no_image));
                        }
                        else
                            {
                                Picasso.with( getContext() ).load( singleSnapshot.getValue(User.class).getProfile_image() ).into( holder.mProfileImage );
                            }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //delete chatroom
            holder.mTrash.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getItem( position ).getCreator_id().equals( FirebaseAuth.getInstance().getCurrentUser().getUid() )){
                        Log.d(TAG, "onClick: asking for permission to delete icon.");
                        ((ChatActivity)mContext).showDeleteChatroomDialog(getItem( position ).getChatroom_id());
                    } else {
                        Toast.makeText(mContext, "You didn't create this chatroom", Toast.LENGTH_SHORT).show();
                    }
                }
            } );

            holder.layoutContainer.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: navigating to chatroom");

                    AlertDialog.Builder builder= new AlertDialog.Builder(mContext, R.style.AlertDialog);
                    builder.setTitle("Enter Chatroom Code :");

                    final EditText chatroomCode = new EditText(mContext);

                    chatroomCode.setHint("\t\t\t\t e.g 0000 ");
                    builder.setView(chatroomCode);

                    builder.setPositiveButton( "ENTER", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String code = chatroomCode.getText().toString();

                            if(TextUtils.isEmpty(code))
                            {
                                Toast.makeText( mContext, "Please Enter Code", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                enterChatroom(code);
                            }
                        }
                    } );

                    builder.show();
                }

                private void enterChatroom(String code)
                {
                    if (code.equals( getItem( position ).getChatroom_code() ))
                    {
                        Log.d( TAG, "onClick: Welcome to " + getItem( position ).getChatroom_name() );
                        ((ChatActivity)mContext).joinChatroom(getItem(position));
                    }
                    else
                    {
                        Toast.makeText( mContext, "Wrong Code!!!", Toast.LENGTH_LONG ).show();
                    }
                }
            } );

        }catch (NullPointerException e){
            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
        }

        return convertView;
    }


}
