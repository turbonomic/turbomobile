package com.turbonomic.turbomobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.turbonomic.turbomobile.R;
import com.turbonomic.turbomobile.RequestFactory;
import com.turbonomic.turbomobile.SSLCertificate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EntitiesListActivity extends AppCompatActivity {

    private String cookie;
    private String ip;
    public String uuidGroup;
    private ArrayAdapter adapter;
    public final static String EntityUUID = "EntityUUID";

    private static ArrayList<String> entityNames = new ArrayList<String>();
    private static ArrayList<String> entityUUIDS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Entities");
        setContentView(R.layout.activity_entities_list);
        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        uuidGroup = getIntent().getStringExtra(GroupsActivity.group_ID);

        fillEntitiesData();

        //Waiting for API to load the data before displaying
        try {
            Thread.sleep(2000);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, entityNames);

        ListView listView = (ListView) findViewById(R.id.entityNames);
        listView.setAdapter(adapter);

        //Click on any entity item and it takes to display its metrics
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(EntitiesListActivity.this, EntityDetailsActivity.class );
                String entityUuid = entityUUIDS.get((int)id);
                i.putExtra(EntityUUID, entityUuid);
                i.putExtra("IP",ip);
                i.putExtra("Cookie",cookie);
                startActivity(i);
            }
        });
    }

    private void fillEntitiesData() {
        Request request = RequestFactory.getInstance(
                ip,
                "groups/"+uuidGroup+"/members",
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

                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            for (JsonNode entityNode : json){
                                String entityName = entityNode.path("displayName").asText();
                                String entityUuid = entityNode.path("uuid").asText();
                                Log.e("VM",entityName+"|"+entityUuid);
                                entityNames.add(entityName);
                                entityUUIDS.add(entityUuid);
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
