package com.example.turbomobile.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SupplyChainActivity extends AppCompatActivity {

    String cookie,ip;
    private final String ENTITY_TYPE_VIRTUAL_MACHINE = "VirtualMachine";
    private final String ENTITY_TYPE_PHYSICAL_MACHINE = "PhysicalMachine";
    private final String ENTITY_TYPE_DATACENTER = "DataCenter";
    private final String ENTITY_TYPE_STORAGE = "Storage";
    private final String ENTITY_TYPE_APPLICATION = "Application";
    private final String ENTITY_TYPE_VIRTUAL_APPLICATION = "VirtualApplication";
    private final String ENTITY_TYPE_LOAD_BALANCER = "LoadBalancer";
    private final String ENTITY_TYPE_DATABASE_SERVER = "DatabaseServer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supply_chain);
        getSupportActionBar().hide();
        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        getSupplyChainData();
    }

    private void getSupplyChainData() {
        Request request = RequestFactory.getInstance(
                ip,
                "supplychains?environment_type=CLOUD&uuids=Market",
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
                    response.body().close();

                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button btnVM =  setDonutDetails(json,ENTITY_TYPE_VIRTUAL_MACHINE, R.id.btnVirtualMachine);
                            btnVM.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_VIRTUAL_MACHINE);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            Button btnVAPP = setDonutDetails(json,ENTITY_TYPE_VIRTUAL_APPLICATION, R.id.btnVirtualApplication);
                            btnVAPP.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_VIRTUAL_APPLICATION);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            Button btnLB = setDonutDetails(json,ENTITY_TYPE_LOAD_BALANCER, R.id.btnLoadBalancer);
                            btnLB.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_LOAD_BALANCER);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            Button btnAPP = setDonutDetails(json,ENTITY_TYPE_APPLICATION, R.id.btnApplication);
                            btnAPP.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_APPLICATION);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            Button btnDBS =  setDonutDetails(json,ENTITY_TYPE_DATABASE_SERVER, R.id.btnDatabaseServer);
                            btnDBS.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_DATABASE_SERVER);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            // TODO: Change to Zone
                            Button btnPM = setDonutDetails(json,ENTITY_TYPE_PHYSICAL_MACHINE, R.id.btnZone);
                            btnPM.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_PHYSICAL_MACHINE);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            // TODO: Change to Region
                            Button btnDC = setDonutDetails(json,ENTITY_TYPE_DATACENTER, R.id.btnRegion);
                            btnDC.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_DATACENTER);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });

                            // TODO: Change to Volume
                            Button btnST = setDonutDetails(json,ENTITY_TYPE_STORAGE, R.id.btnVolume);
                            btnST.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view){
                                    Intent i = new Intent(SupplyChainActivity.this,
                                            EntitiesAndActionsActivity.class );
                                    i.putExtra("EntityType",ENTITY_TYPE_STORAGE);
                                    i.putExtra("IP",ip);
                                    i.putExtra("Cookie",cookie);
                                    startActivity(i);
                                }
                            });
                        }
                    });


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Drawable getDonutColor(JsonNode health) {
        if (!health.path("Critical").asText().equals("")) {
            return getDrawable(R.drawable.sc_round_critical);
        } else if (!health.path("Major").asText().equals("")) {
            return getDrawable(R.drawable.sc_round_major);
        } else if (!health.path("Minor").asText().equals("")) {
            return getDrawable(R.drawable.sc_round_minor);
        }
        return getDrawable(R.drawable.sc_round);
    }

    private Button setDonutDetails(JsonNode json, String entityType, int buttonId) {
        String entitiesCount = json.path("seMap").path(entityType).path("entitiesCount").asText();
        Button button = findViewById(buttonId);
        button.setText(entitiesCount);
        JsonNode health = json.path("seMap").path(entityType).path("healthSummary");
        Drawable donut = getDonutColor(health);
        button.setBackground(donut);
        return button;
    }
}
