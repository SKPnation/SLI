package com.right.ayomide.tabianconsulting.utility;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.right.ayomide.tabianconsulting.AdminActivity;
import com.right.ayomide.tabianconsulting.Common.Common;
import com.right.ayomide.tabianconsulting.Interface.ItemClickListener;
import com.right.ayomide.tabianconsulting.R;

public class EmployeeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener
{

    public ImageView profileImage;
    public TextView name, department;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public EmployeeViewHolder(@NonNull View itemView) {
        super( itemView );

        profileImage = (ImageView) itemView.findViewById(R.id.profile_image);
        name = (TextView) itemView.findViewById(R.id.name);
        department = (TextView) itemView.findViewById(R.id.department);

        itemView.setOnClickListener( this );
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick( view, getAdapterPosition(), false );
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "Select the action" );

        contextMenu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
