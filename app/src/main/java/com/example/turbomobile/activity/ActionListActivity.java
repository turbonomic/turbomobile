package com.example.turbomobile.activity;

import android.content.Intent;
import android.drm.DrmStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActionListActivity extends AppCompatActivity {

    private String cookie;
    private String ip;
    private String environmentType;
    private int criticalCounter = 0;
    private int majorCounter = 0;
    private int minorCounter = 0;
    private int pendingCounter = 0;
    private String actionUUID;
    ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();

    protected void  initList(){
        final SimpleAdapter sa= new SimpleAdapter(this, list, R.layout.layout_twolines, new String[] { "line1","line2" }, new int[] {R.id.line_a, R.id.line_b});
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions?order_by=severity&ascending=true",
                "{\"environmentType\":\"" + environmentType + "\"}",
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
                    Log.e("RESP=",resp);

                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);
                    runOnUiThread((new Runnable() {
                        @Override
                        public void run() {
                            list = populateList(json);
                            ((ListView)findViewById(R.id.list)).setAdapter(sa);
                        }
                    }));
                    response.close();
                }
            });

            ListView lv = (ListView)findViewById(R.id.list);
            lv.setClickable(true);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(parent.getContext(), ActionsListActivity.class);
                    String severity="";
                    if(position == 0){
                        severity = "CRITICAL";
                    } else if (position == 1) {
                        severity = "MAJOR";
                    } else {
                        severity = "MINOR";
                    }
                    intent.putExtra("Cookie",cookie);
                    intent.putExtra("IP",ip);
                    intent.putExtra("Severity",severity);
                    intent.putExtra("environmentType",environmentType);
                    intent.putExtra("ActionUUID",actionUUID);
                    startActivity(intent);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected ArrayList<HashMap<String,String>> populateList(JsonNode json){
        for (JsonNode targetNode : json) {
            JsonNode risk = targetNode.path("risk");
            String severity = risk.path("severity").asText();
            Log.e("SEVERITY", severity);
            if(targetNode.path("actionState").asText().toUpperCase().equals("PENDING_ACCEPT")){
                actionUUID = targetNode.path("uuid").asText();
            }
            switch (severity) {
                case "CRITICAL":
                    criticalCounter += 1;
                    break;
                case "MAJOR":
                    majorCounter += 1;
                    break;
                case "MINOR":
                    minorCounter += 1;
                    break;
                default:
                    pendingCounter += 1;
                    break;
            }
        }
        String[][] actions =
                {{"Critical",Integer.toString(criticalCounter)},
                        {"Major",Integer.toString(majorCounter)},
                        {"Minor",Integer.toString(minorCounter)}};
        HashMap<String,String> item;
        for(int i=0;i<actions.length;i++){
            item = new HashMap<String,String>();
            item.put( "line1", actions[i][0]);
            item.put( "line2", actions[i][1]);
            list.add( item );
        }
        return list;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);
        setTitle("Actions By Severity");
        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        environmentType = getIntent().getStringExtra("environmentType");
        initList();
    }
}
