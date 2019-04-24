package com.example.turbomobile.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.example.turbomobile.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView onPremActionsCard, cloudActionsCard, targetsCard, searchCard, groupsCard, settingsCard;

    private String cookie;
    private String ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onPremActionsCard = (CardView) findViewById(R.id.cvOnPremActions);
        cloudActionsCard = (CardView) findViewById(R.id.cvCloudActions);
        targetsCard = (CardView) findViewById(R.id.cvTargets);
        searchCard = (CardView) findViewById(R.id.cvSearch);
        groupsCard = (CardView) findViewById(R.id.cvGroups);
        settingsCard = (CardView) findViewById(R.id.cvSettings);

        onPremActionsCard.setOnClickListener(this);
        cloudActionsCard.setOnClickListener(this);
        targetsCard.setOnClickListener(this);
        searchCard.setOnClickListener(this);
        groupsCard.setOnClickListener(this);
        settingsCard.setOnClickListener(this);

        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.cvCloudActions:
                intent = new Intent(this, ActionListActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                startActivity(intent);
                break;
            case R.id.cvOnPremActions:
                intent = new Intent(this, ActionListActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                startActivity(intent);
                break;
            case R.id.cvGroups:
                intent = new Intent(this, GroupsActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                startActivity(intent);
                break;
            case R.id.cvSearch:
                intent = new Intent(this, SearchActivity.class);
                intent.putExtra("Cookie", cookie);
                intent.putExtra("IP", ip);
                startActivity(intent);
                break;
            case R.id.cvTargets:
                intent = new Intent(this, TargetsListActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                startActivity(intent);
                break;
            case R.id.cvSettings:
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                startActivity(intent);
                break;
        }

    }
}
