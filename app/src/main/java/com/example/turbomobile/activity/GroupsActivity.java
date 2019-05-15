package com.example.turbomobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupsActivity extends AppCompatActivity {

    private String cookie;
    private String ip;
    private ArrayAdapter adapter;
    public final static String group_ID = "com.example.turbomobile.activity._ID";

    private static ArrayList<String> groupsNames = new ArrayList<>();
    private static ArrayList<String> groupsUUIDS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setTitle("Groups");
        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");

        //Populate the groups name and uuid
        fillGroupsData();

        //Waiting for API to load the data before displaying
        try {
            Thread.sleep(2000);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, groupsNames);

        ListView listView = (ListView) findViewById(R.id.groupsNames);
        listView.setAdapter(adapter);

        //Click on any group item and it takes to display its entities
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(GroupsActivity.this, EntitiesListActivity.class);
                String groupUuid = groupsUUIDS.get((int)id);
                i.putExtra(group_ID, groupUuid);
                i.putExtra("IP",ip);
                i.putExtra("Cookie",cookie);
                startActivity(i);
            }
        });
    }

    private void fillGroupsData() {
        Request request = RequestFactory.getInstance(
                ip,
                "groups/GROUP-MyGroups/members",
                "",
                "GET",
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
                    Log.e("RESP=",resp);

                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            for (JsonNode groupNode : json){
                                String groupName = groupNode.path("displayName").asText();
                                String groupUUID = groupNode.path("uuid").asText();
                                Log.e("GROUP",groupName+"|"+groupUUID);
                                groupsNames.add(groupName);
                                groupsUUIDS.add(groupUUID);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}