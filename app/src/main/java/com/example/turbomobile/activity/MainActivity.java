package com.example.turbomobile.activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView onPremActionsCard, cloudActionsCard, targetsCard, searchCard, groupsCard, settingsCard;
    private LinearLayout onPremActionsLayout, cloudActionsLayout;
    private TextView criticalOnPremActionsText, criticalCloudActionsText, welcomeTextView;
    private String cookie, ip, username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onPremActionsLayout = findViewById(R.id.layoutOnPremActions);
        cloudActionsLayout = findViewById(R.id.layoutCloudActions);

        criticalOnPremActionsText = findViewById(R.id.txtCriticalOnPremActions);
        criticalCloudActionsText = findViewById(R.id.txtCriticalCloudActions);
        welcomeTextView = findViewById(R.id.txtWelcome);

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
        username = getIntent().getStringExtra("Username");

        String welcomeText = "Welcome, <b>"+username+"</b><br>Turbonomic Instance <b>" + ip+"</b>";
        welcomeTextView.setText(Html.fromHtml(welcomeText));

        fillOnPremCriticalActionsView();
        fillCloudCriticalActionsView();

    }

    private void fillOnPremCriticalActionsView() {
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions/stats",
                "{\"environmentType\":\"ONPREM\",\"riskSeverityList\":[\"CRITICAL\"]}",
                "POST",
                cookie);

        OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String resp = response.body().string();
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Double actionsCount = 0.0;
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                final JsonNode statistics = json.get(0).path("statistics");
                                for (JsonNode statistic : statistics) {
                                    if (statistic.path("name").asText().equals("numActions")) {
                                        actionsCount = Double.parseDouble(statistic.path("value").asText());
                                    }
                                }
                            }
                            if(actionsCount > 0) {
                                Long actions = Math.round(actionsCount);
                                onPremActionsLayout.setBackgroundColor(getResources().getColor(R.color.colorCritical));
                                criticalOnPremActionsText.setVisibility(View.VISIBLE);
                                criticalOnPremActionsText.setText(actions.toString());
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillCloudCriticalActionsView() {
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions/stats",
                "{\"environmentType\":\"CLOUD\",\"riskSeverityList\":[\"CRITICAL\"]}",
                "POST",
                cookie);

        OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String resp = response.body().string();
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Double actionsCount = 0.0;
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                final JsonNode statistics = json.get(0).path("statistics");
                                for (JsonNode statistic : statistics) {
                                    if (statistic.path("name").asText().equals("numActions")) {
                                        actionsCount = Double.parseDouble(statistic.path("value").asText());
                                    }
                                }
                            }
                            if(actionsCount > 0) {
                                Long actions = Math.round(actionsCount);
                                cloudActionsLayout.setBackgroundColor(getResources().getColor(R.color.colorCritical));
                                criticalCloudActionsText.setVisibility(View.VISIBLE);
                                criticalCloudActionsText.setText(actions.toString());
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.cvCloudActions:
                intent = new Intent(this, ActionListActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                intent.putExtra("environmentType", "CLOUD");
                startActivity(intent);
                break;
            case R.id.cvOnPremActions:
                intent = new Intent(this, ActionListActivity.class);
                intent.putExtra("Cookie",cookie);
                intent.putExtra("IP",ip);
                intent.putExtra("environmentType", "ONPREM");
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
