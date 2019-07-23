package com.turbonomic.turbomobile.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.turbonomic.turbomobile.R;
import com.turbonomic.turbomobile.RequestFactory;
import com.turbonomic.turbomobile.SSLCertificate;
import com.turbonomic.turbomobile.TargetDetails;
import com.turbonomic.turbomobile.adapter.RecyclerViewAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TargetsListActivity extends AppCompatActivity {

    private static final String TAG = "TargetsList Activity";
    private String cookie;
    private String ip;
    ArrayList<TargetDetails> targetsList = new ArrayList<>();

    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets_list);
        setTitle("Targets");
        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        fillTargetsData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_targets,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.btnAddTarget:
                Toast.makeText(this, "Add is clicked", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        switch (item.getItemId()){
            case 1:
                Toast.makeText(this, "Edit was clicked on item "+item.getGroupId(), Toast.LENGTH_SHORT).show();
                return true;
            case 2:
                Toast.makeText(this, "Delete was clicked on item "+item.getGroupId(), Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void initRecyclerView() {
        Log.e("After",targetsList.toString());
        recyclerView = findViewById(R.id.recyclerView_list);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this,targetsList,cookie);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fillTargetsData() {
        Request request = RequestFactory.getInstance(
                ip,
                "targets",
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

                            for (JsonNode targetNode : json){
                                String targetName = targetNode.path("displayName").asText();
                                String targetUUID = targetNode.path("uuid").asText();
                                String targetType = targetNode.path("type").asText();
                                Log.e("TARGET",targetType+"|"+targetName+"|"+targetUUID);
                                TargetDetails target = new TargetDetails(targetUUID,targetName,targetType);
                                targetsList.add(target);

                            }
                            Log.e("targetsList",targetsList.toString());
                            initRecyclerView();
                        }
                    });


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
