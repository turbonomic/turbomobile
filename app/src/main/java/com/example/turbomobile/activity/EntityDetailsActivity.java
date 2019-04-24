package com.example.turbomobile.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.example.turbomobile.TargetDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EntityDetailsActivity extends AppCompatActivity {

    private String cookie;
    private String entityUUID;
    private String ip;
    private TextView txtEntityName,txtEntityState,txtEntitySeverity,txtEntityTarget,txtEntityVmem,txtEntityVcpu,txtEntityIOThroughput,txtEntityNetThroughput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_details);
        cookie = getIntent().getStringExtra("Cookie");
        entityUUID = getIntent().getStringExtra(EntitiesListActivity.EntityUUID);
        ip = getIntent().getStringExtra("IP");
        txtEntityName = (TextView) findViewById(R.id.txtEntityName);
        txtEntityState = (TextView) findViewById(R.id.txtEntityState);
        txtEntitySeverity = (TextView) findViewById(R.id.txtEntitySeverity);
        txtEntityTarget = (TextView) findViewById(R.id.txtEntityTarget);
        txtEntityVmem = (TextView) findViewById(R.id.txtEntityVmem);
        txtEntityVcpu = (TextView) findViewById(R.id.txtEntityVcpu);
        txtEntityIOThroughput = (TextView) findViewById(R.id.txtEntityIOThroughput);
        txtEntityNetThroughput = (TextView) findViewById(R.id.txtEntityNetThroughput);
        getEntityDetails();
        getEntityStats();
    }

    private void getEntityDetails() {
        Request request = RequestFactory.getInstance(
                ip,
                "entities/"+entityUUID,
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
                            txtEntityName.setText(json.path("displayName").asText());
                            txtEntitySeverity.setText(json.path("severity").asText());
                            txtEntityState.setText(json.path("state").asText());
                            txtEntityTarget.setText(json.path("discoveredBy").path("displayName").asText());
                            //txtEntityVmem.setText(json.path("").asText());
                            //txtEntityVcpu.setText(json.path("").asText());
                        }
                    });


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getEntityStats() {
        Request request = RequestFactory.getInstance(
                ip,
                "entities/"+entityUUID+"/stats",
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
                            try {
                                JsonNode statistics =  json.get(0).path("statistics");
                                for (JsonNode stat : statistics){
                                    String name = stat.path("name").asText();
                                    String value;
                                    if(name.toLowerCase().equals("vmem")){
                                        value= stat.path("capacity").path("total").asText();
                                        Double vmem = Double.parseDouble(value);
                                        vmem = vmem / 1024;
                                        vmem = vmem / 1024;
                                        long lvmem = Math.round(vmem);
                                        txtEntityVmem.setText(Long.toString(lvmem)+ " GB");
                                    }
                                    if(name.toLowerCase().equals("vcpu")){
                                        value= stat.path("capacity").path("total").asText();
                                        Double vcpu = Double.parseDouble(value);
                                        vcpu = vcpu / 1024;
                                        Long lvcpu = Math.round(vcpu);
                                        txtEntityVcpu.setText(Long.toString(lvcpu)+" GHz");
                                    }
                                    if(name.toLowerCase().equals("iothroughput")){
                                        value= stat.path("capacity").path("total").asText();
                                        Double io = Double.parseDouble(value);
                                        io = io / 10000000;
                                        //Long lio = Math.round(io);

                                        txtEntityIOThroughput.setText(Double.toString(io)+" GB/s");
                                    }
                                    if(name.toLowerCase().equals("netthroughput")){
                                        value= stat.path("capacity").path("total").asText();
                                        Double net = Double.parseDouble(value);
                                        net = net / 10000;
                                        Long lnet = Math.round(net);
                                        txtEntityNetThroughput.setText(Long.toString(lnet)+ " MB/s");
                                    }
                                }

                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
