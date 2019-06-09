package com.example.turbomobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.turbomobile.R;
import com.example.turbomobile.SearchDetails;
import com.example.turbomobile.activity.EntitiesListActivity;
import com.example.turbomobile.activity.EntityDetailsActivity;
import com.example.turbomobile.activity.MainActivity;

import java.util.ArrayList;

public class RecyclerViewSearchAdapter extends RecyclerView.Adapter<RecyclerViewSearchAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewSearchAdapter";
    private Context mContext;
    private String cookie;
    private String ip;

    private ArrayList<SearchDetails> results;

    public RecyclerViewSearchAdapter(Context mContext,
                                     ArrayList<SearchDetails> results,
                                     String cookie,
                                     String ip) {
        this.mContext = mContext;
        this.results = results;
        this.cookie = cookie;
        this.ip = ip;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_searchlistitem, viewGroup,false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.textView.setText(results.get(position).getDisplayName());
        viewHolder.layoutSearchListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EntityDetailsActivity.class);
                intent.putExtra("Cookie", cookie);
                intent.putExtra("IP", ip);
                intent.putExtra(EntitiesListActivity.EntityUUID, results.get(position).getUuid());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        ConstraintLayout layoutSearchListItem;;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.search_result_name);
            layoutSearchListItem = itemView.findViewById(R.id.layout_search_item_list);
            layoutSearchListItem.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            return;
        }
    }
}
