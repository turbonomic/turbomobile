package com.example.turbomobile.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

public class ActionDetails extends AppCompatActivity {

    private String cookie;
    private String ip;
    private String actionUUID;
    private TextView txtActionDetails,txtActionMode,txtActionType,txtActionState,txtActionRisk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_details);

        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        actionUUID = getIntent().getStringExtra("ActionUUID");

        txtActionDetails = (TextView) findViewById(R.id.txtActionDetails);
        txtActionMode = (TextView) findViewById(R.id.txtActionMode);
        txtActionType = (TextView) findViewById(R.id.txtActionType);
        txtActionState = (TextView) findViewById(R.id.txtActionState);
        txtActionRisk = (TextView) findViewById(R.id.txtActionRisk);
        getActionDetails();
    }
    private void getActionDetails() {
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions/"+actionUUID,
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
                            txtActionDetails.setText(json.path("details").asText());
                            txtActionMode.setText(json.path("actionMode").asText());
                            txtActionType.setText(json.path("actionType").asText());
                            txtActionState.setText(json.path("actionState").asText());
                            txtActionRisk.setText(json.path("risk").path("description").asText());
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
