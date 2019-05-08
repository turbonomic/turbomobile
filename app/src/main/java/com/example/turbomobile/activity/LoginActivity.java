package com.example.turbomobile.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String token="";
    private String ip="";
    private String username="";
    private boolean isConnected=false;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progressBar);
        configureDoneButton();
    }

    @Override
    protected void onStop() {
        progressBar.setVisibility(View.GONE);
        super.onStop();
    }

    private Request createLoginRequest() {
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
        return request;
    }

    private void handleResponse(Request request) {
        OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
        try (Response response =  client.newCall(request).execute()){
            token = response.header("Set-Cookie");
            Log.e("Token=",token);
            isConnected=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureDoneButton() {
        Button btn = (Button) findViewById(R.id.btnDone);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                progressBar.setVisibility(View.VISIBLE);
                Request request = createLoginRequest();
                MyAsyncTask myTask = new MyAsyncTask(view);
                myTask.execute(request);
            }
        });
    }

    public class MyAsyncTask extends AsyncTask<Request,Void,Boolean>{

        private View view;

        public MyAsyncTask(View view) {
            this.view = view;
        }

        @Override
        protected Boolean doInBackground(Request... requests) {
            handleResponse(requests[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                if (isConnected) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("Cookie", token);
                    intent.putExtra("IP", ip);
                    intent.putExtra("Username", username);
                    startActivity(intent);
                } else {
                    Snackbar snack = Snackbar.make(view,"Invalid credentials, please try again",
                            Snackbar.LENGTH_LONG);
                    snack.getView().setBackgroundColor(ContextCompat
                            .getColor(view.getContext(),R.color.colorCritical));
                    snack.show();
                }
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
