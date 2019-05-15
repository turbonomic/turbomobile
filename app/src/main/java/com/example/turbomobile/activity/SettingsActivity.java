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

public class SettingsActivity extends AppCompatActivity {

    String cookie;
    String ip;
    TextView tvTurboVersion;
    TextView tvTurboLicense;
    TextView tvTurboInUseEntities;
    TextView tvTurboEdition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Information");
        tvTurboVersion = (TextView) findViewById(R.id.tvTurboVersion);
        tvTurboLicense = (TextView) findViewById(R.id.tvTurboLicense);
        tvTurboInUseEntities = (TextView) findViewById(R.id.tvTurboInUseEntities);
        tvTurboEdition = (TextView)findViewById(R.id.tvTurboEdition);

        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        getVersionData();
        getLicenseData();


    }
        private void getVersionData() {
            Request request = RequestFactory.getInstance(
                    ip,
                    "admin/versions",
                    "",
                    "GET",
                    cookie);


            OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
            //parse api
            //get details , version

            try {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String resp = response.body().string();
                        Log.e("RESP=", resp);
                        System.out.println("Response is" +resp);

                        final ObjectMapper mapper = new ObjectMapper();
                        final JsonNode json = mapper.readTree(resp);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String turboVersion = json.path("versionInfo").asText().substring(0,62);
                                String version = turboVersion.substring(0,35);
                                Log.e("VERSION",turboVersion);
                                tvTurboVersion.setText(version);
                            }
                        });


                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    private void getLicenseData() {
        Request request = RequestFactory.getInstance(
                ip,
                "license",
                "",
                "GET",
                cookie);



        OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
        //parse api
        //get details , version

        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String resp = response.body().string();
                    Log.e("RESP=", resp);
                    System.out.println("Response is" +resp);

                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String licensedEntities = json.path("numLicensedEntities").asText();
                            String numInUseEntities = json.path("numInUseEntities").asText();
                            String tvTurboType = json.path("edition").asText();

                            Log.e("LICENSED_ENTITIES",licensedEntities);
                            tvTurboLicense.setText(licensedEntities);
                            tvTurboInUseEntities.setText(numInUseEntities);
                            tvTurboEdition.setText(tvTurboType);
                            Log.e("NUM_ENTITIES_IN_USE",numInUseEntities);
                            Log.e("TYPE",tvTurboType);
                        }
                    });

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}








