package com.turbonomic.turbomobile.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.turbonomic.turbomobile.R;
import com.turbonomic.turbomobile.TargetDetails;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<TargetDetails> targetsList;
    private Context mContext;
    private String cookie;

    public RecyclerViewAdapter(Context mContext, ArrayList<TargetDetails> targetsList,String cookie) {
        this.targetsList = targetsList;
        this.mContext = mContext;
        this.cookie = cookie;
        Log.e("constructor",targetsList.toString());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_listitem,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        Log.e(TAG,"onBindViewHolder called.");
        viewHolder.txtTargetName.setText(targetsList.get(position).getDisplayName());
        int image = 0;
        if(targetsList.get(position).getType().toLowerCase().equals("azure")){
            image = R.drawable.azure;
        } else if(targetsList.get(position).getType().toLowerCase().equals("vcenter")){
            image = R.drawable.vcenter;
        } else if(targetsList.get(position).getType().toLowerCase().equals("pure")){
            image = R.drawable.pure;
        } else if(targetsList.get(position).getType().toLowerCase().equals("aws")){
            image = R.drawable.aws;
        } else if(targetsList.get(position).getType().toLowerCase().startsWith("cisco")){
            image = R.drawable.cisco;
        } else if(targetsList.get(position).getType().toLowerCase().equals("vmm")){
            image = R.drawable.vmm;
        } else if(targetsList.get(position).getType().toLowerCase().equals("hyper-v")){
            image = R.drawable.hyperv;
        } else if(targetsList.get(position).getType().toLowerCase().equals("vclouddirector")){
            image = R.drawable.vcd;
        } else if(targetsList.get(position).getType().toLowerCase().equals("cloudfoundry")){
            image = R.drawable.cloud_foundry_icon;
        } else if(targetsList.get(position).getType().toLowerCase().equals("tomcat")){
            image = R.drawable.tomcat_icon;
        } else if(targetsList.get(position).getType().toLowerCase().equals("sqlserver")){
            image = R.drawable.sqlserver_icon;
        } else {
            image = R.drawable.vcenter;
        }
        viewHolder.imgTargetLogo.setImageDrawable(ContextCompat.getDrawable(mContext,image));

        viewHolder.layoutListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something when we click on the list item (Target)
            }
        });

        /*
        viewHolder.layoutListItem.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "Long Click", Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return targetsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        ImageView imgTargetLogo;
        TextView txtTargetName;
        ConstraintLayout layoutListItem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTargetLogo = itemView.findViewById(R.id.imgTargetLogo);
            txtTargetName = itemView.findViewById(R.id.txtTargetName);
            layoutListItem = itemView.findViewById(R.id.layoutListItem);
            layoutListItem.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(),1,0,"Edit");
            menu.add(this.getAdapterPosition(),2,1,"Delete");

        }
    }
}
