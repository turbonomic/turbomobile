package com.example.turbomobile.activity;

import android.os.AsyncTask;;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.example.turbomobile.dto.ActionDTO;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActionDetails extends AppCompatActivity {

    private String cookie;
    private String ip;
    private boolean executable;
    private TextView txtActionDetails,txtActionMode,txtActionType,txtActionState,txtActionRisk;
    private Button btnAccept;
    private ActionDTO actionDTO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_details);
        setTitle("Action Details");

        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        executable = getIntent().getBooleanExtra("Executable",false);
        actionDTO = (ActionDTO) getIntent().getSerializableExtra("ActionDTO");

        btnAccept = findViewById(R.id.btnAccept);

        btnAccept.setVisibility(executable ? View.VISIBLE : View.INVISIBLE);
        txtActionDetails = (TextView) findViewById(R.id.txtActionDetails);
        txtActionMode = (TextView) findViewById(R.id.txtActionMode);
        txtActionType = (TextView) findViewById(R.id.txtActionType);
        txtActionState = (TextView) findViewById(R.id.txtActionState);
        txtActionRisk = (TextView) findViewById(R.id.txtActionRisk);

        setActionDetails();

        configureAcceptButton();
    }

    private Request createAcceptRequest() {
        Request request = RequestFactory.getInstance(
                ip,
                "actions/"+actionDTO.getUuid()+"?accept=true",
                "",
                "POST",
                cookie);
        return request;
    }

    private Boolean handleResponse(Request request) {
        OkHttpClient client = SSLCertificate.getUnsafeOkHttpClient();
        try (Response response =  client.newCall(request).execute()){
            final String resp = response.body().string();
           return (resp.equalsIgnoreCase("true")) ? true :false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void configureAcceptButton() {
        btnAccept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                Request request = createAcceptRequest();
                MyAsyncTask myTask = new MyAsyncTask(view);
                myTask.execute(request);
                btnAccept.setBackgroundColor(ContextCompat
                        .getColor(view.getContext(),R.color.colorMainBg));
                btnAccept.setEnabled(false);
            }
        });
    }

    private void setActionDetails() {
        txtActionDetails.setText(actionDTO.getDetails());
        txtActionMode.setText(actionDTO.getMode());
        txtActionType.setText(actionDTO.getType());
        txtActionState.setText(actionDTO.getState());
        txtActionRisk.setText(actionDTO.getRiskDescription());
    }

    private class MyAsyncTask extends AsyncTask<Request,Void,Boolean> {

        private View view;

        public MyAsyncTask(View view) {
            this.view = view;
        }

        @Override
        protected Boolean doInBackground(Request... requests) {
            return handleResponse(requests[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                Snackbar.make(view,"The action was accepted and now being executed",
                        Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar snack = Snackbar.make(view,
                        "A problem happened whe the action was accepted",
                        Snackbar.LENGTH_LONG);
                snack.getView().setBackgroundColor(ContextCompat
                        .getColor(view.getContext(),R.color.colorCritical));
                snack.show();
            }
        }
    }

}
