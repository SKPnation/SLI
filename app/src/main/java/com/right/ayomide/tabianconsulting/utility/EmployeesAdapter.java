package com.right.ayomide.tabianconsulting.utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.right.ayomide.tabianconsulting.AdminActivity;
import com.right.ayomide.tabianconsulting.Common.Common;
import com.right.ayomide.tabianconsulting.Interface.ItemClickListener;
import com.right.ayomide.tabianconsulting.R;
import com.right.ayomide.tabianconsulting.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.ViewHolder>{

    private static final String TAG = "EmployeesAdapter";

    private ArrayList<User> mUsers;
    private Context mContext;

    public EmployeesAdapter(ArrayList<User> users, Context context) {
        mUsers = users;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //inflate the custom layout
        View view = inflater.inflate(R.layout.layout_employee_listitem, viewGroup, false);

        //return a new holder instance
        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.with( mContext ).load( mUsers.get( position ).getProfile_image() ).into( holder.profileImage );
        holder.name.setText(mUsers.get(position).getName());
        holder.department.setText(mUsers.get(position).getDepartment());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView profileImage;
        public TextView name, department;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );

            profileImage = (ImageView) itemView.findViewById(R.id.profile_image);
            name = (TextView) itemView.findViewById(R.id.name);
            department = (TextView) itemView.findViewById(R.id.department);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: selected employee: " + mUsers.get(getAdapterPosition()));

                    //open a dialog for selecting a department
                    //((AdminActivity)mContext).setDepartmentDialog(mUsers.get(getAdapterPosition()));
                }
            });

        }
    }
}
