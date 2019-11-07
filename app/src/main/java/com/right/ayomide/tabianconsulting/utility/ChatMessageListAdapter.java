package com.right.ayomide.tabianconsulting.utility;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.right.ayomide.tabianconsulting.R;
import com.right.ayomide.tabianconsulting.models.ChatMessage;
import com.right.ayomide.tabianconsulting.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 9/18/2017.
 */

public class ChatMessageListAdapter extends ArrayAdapter<ChatMessage> {

    private static final String TAG = "ChatMessageListAdapter";

    private int mLayoutResource;
    private Context mContext;

    public ChatMessageListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ChatMessage> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
    }

    public static class ViewHolder{
        TextView message, name;
        ImageView mProfileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.message = convertView.findViewById(R.id.message);
            holder.name = convertView.findViewById( R.id.user_name );
            holder.mProfileImage = convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
            holder.name.setText( "" );
            holder.message.setText("");
        }

        try{
            //set the message
            holder.message.setText(getItem(position).getMessage());
            holder.name.setText( getItem( position ).getName() );
            if (!getItem( position ).getProfile_image().isEmpty()) {

                //we only load image if prev. URL and current URL do not match, or tag is null
                Picasso.with( getContext() ).load( getItem( position ).getProfile_image() ).into( holder.mProfileImage );
                holder.mProfileImage.setTag(getItem(position).getProfile_image());
            }
            else {
                holder.mProfileImage.setImageResource( R.drawable.ic_android );
            }

        }catch (NullPointerException e){
            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
        }

        return convertView;
    }

}

















