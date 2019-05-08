package com.example.turbomobile.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String token="";
    private String ip="";
    private String username="";
    private boolean isConnected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        configureDoneButton();
    }

    private void setToken() {
        TextView txtUsername = findViewById(R.id.txtUser);
        TextView txtPassword = findViewById(R.id.txtPass);
        TextView txtIP = findViewById(R.id.txtIP);
        username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        ip = txtIP.getText().toString();
        Request request = RequestFactory.getInstance(
                ip,
                "login?username="+username+"&password="+password,
                "",
                "POST");
        handleResponse(request);

    }

    private void handleResponse(Request request) {
        OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String cookie = response.header("Set-Cookie");
                    token = cookie;
                    Log.e("Token=",token);
                    isConnected=true;
                    response.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureDoneButton() {
        Button btn = (Button) findViewById(R.id.btnDone);
        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                setToken();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(isConnected) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("Cookie", token);
                    intent.putExtra("IP", ip);
                    intent.putExtra("Username",username);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials, please try again", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
