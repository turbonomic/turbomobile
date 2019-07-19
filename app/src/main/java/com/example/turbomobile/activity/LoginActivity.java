package com.example.turbomobile.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String token="", ip="", username="";
    private boolean isConnected=false;
    ProgressBar progressBar;
    Button btnLogin;
    EditText txtUsername, txtPassword, txtIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
        }

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();

        progressBar = findViewById(R.id.progressBar);
        txtUsername = findViewById(R.id.txtUser);
        txtPassword = findViewById(R.id.txtPass);
        txtIP = findViewById(R.id.txtIP);

        txtUsername.addTextChangedListener(watcher);
        txtPassword.addTextChangedListener(watcher);
        txtIP.addTextChangedListener(watcher);

        configureDoneButton();
    }

    @Override
    protected void onStop() {
        progressBar.setVisibility(View.GONE);
        super.onStop();
    }

    private Request createLoginRequest() {
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
        btnLogin = findViewById(R.id.btnDone);
        btnLogin.setEnabled(false);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                progressBar.setVisibility(View.VISIBLE);
                Request request = createLoginRequest();
                MyAsyncTask myTask = new MyAsyncTask(view);
                myTask.execute(request);
            }
        });
    }

    private final TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if ("".equals(txtIP.getText().toString().trim()) ||
                    "".equals(txtPassword.getText().toString().trim()) ||
                    "".equals(txtUsername.getText().toString().trim())) {
                btnLogin.setBackgroundColor(ContextCompat
                        .getColor(getApplicationContext(),R.color.colorGreenLight));
                btnLogin.setEnabled(false);
            } else {
                btnLogin.setBackgroundColor(ContextCompat
                        .getColor(getApplicationContext(),R.color.colorMainBg2));
                btnLogin.setEnabled(true);
            }
        }
    };

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
