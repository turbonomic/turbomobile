package com.example.turbomobile.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.example.turbomobile.SearchDetails;
import com.example.turbomobile.adapter.RecyclerViewSearchAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    ArrayList<SearchDetails> results = new ArrayList<>();;
    private String cookie;
    private String ip;
    private RecyclerView recyclerView;
    private static final String TAG = "Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SearchView searchView = findViewById(R.id.search_field);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                fillSearchResultsData(query);
                initRecyclerView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Do nothing unless they submit
                return false;
            }
        });
    }

    private void initRecyclerView() {
        RecyclerViewSearchAdapter adapter = new RecyclerViewSearchAdapter(this, results, cookie, ip);
        recyclerView = findViewById(R.id.search_results);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fillSearchResultsData(String query) {
        // Clear state of previous search results
        results.clear();
        // URL encode the query
        String encodedQuery = query;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String error = String.format("Error encoding query '%s'. Response: '%s'",
                    query, e.getMessage());
            Log.e(TAG, error);
        }
        Request request = RequestFactory.getInstance(
                ip,
                "search?detail_type=compact&disable_hateoas=true&q=" + encodedQuery,
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
                    Log.d("RESP=", resp);

                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (JsonNode resultNode : json) {
                                String resultName = resultNode.path("displayName").asText();
                                String resultUUID = resultNode.path("uuid").asText();
                                Log.d("SEARCH", resultName + "|" + resultUUID);
                                results.add(new SearchDetails(resultUUID, resultName));

                            }
                            Log.d("resultsList", results.toString());
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
