package com.example.turbomobile.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.turbomobile.R;
import com.example.turbomobile.RequestFactory;
import com.example.turbomobile.SSLCertificate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActionsListActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static Map<String,String> pendingActionsMap,recommendedActionsMap,acceptedActionsMap;
    private String ip, cookie, severity, environmentType;
    private static ArrayAdapter pendingActionsadapter, recommendedActionsAdapter, acceptedActionsAdapter;
    private final String NO_ACTIONS = "No actions found";

    @Override
    protected void onStop() {
        pendingActionsMap = null;
        recommendedActionsMap = null;
        acceptedActionsMap = null;
        pendingActionsadapter = null;
        recommendedActionsAdapter = null;
        acceptedActionsAdapter = null;
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions_list);

        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        severity = getIntent().getStringExtra("Severity");
        environmentType = getIntent().getStringExtra("environmentType");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    fillPendingActionsList();
                } else if (tab.getPosition() == 1) {
                    fillRecommendedActionsList();
                } else {
                    fillAcceptedActionsList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // Fill the first tab. The other tabs will be filled once clicked on them.
        fillPendingActionsList();
    }

    private void setupPendingActionsListView() {
        ListView pendingActionsListView = findViewById(R.id.pendingActionsListView);
        final List<String> actionsListDetails = new ArrayList<String>(pendingActionsMap.values());

        if(actionsListDetails.size() > 0) {
            final List<String> actionsListUUIDs = new ArrayList<String>(pendingActionsMap.keySet());
            pendingActionsadapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsListDetails);
            pendingActionsListView.setAdapter(pendingActionsadapter);

            pendingActionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(ActionsListActivity.this, ActionDetails.class );
                    String ActionUUID = actionsListUUIDs.get(position);
                    i.putExtra("ActionUUID", ActionUUID);
                    i.putExtra("IP",ip);
                    i.putExtra("Cookie",cookie);
                    i.putExtra("Executable",true);
                    startActivity(i);
                }
            });
        } else {
            actionsListDetails.add(NO_ACTIONS);
            pendingActionsadapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsListDetails);
            pendingActionsListView.setAdapter(pendingActionsadapter);
        }
    }
    private void fillPendingActionsList() {
        if(pendingActionsMap !=null && pendingActionsMap.size() > 0){
            setupPendingActionsListView();
            return;
        }
        pendingActionsMap = new HashMap<>();
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions",
                "{\"environmentType\": \""+environmentType+"\",\"actionStateList\": [\"PENDING_ACCEPT\"],\"riskSeverityList\": [\""+severity+"\"]}",
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
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Double actionsCount = 0.0;
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                for (JsonNode action : json) {
                                    String details = action.path("details").asText();
                                    if (details != null && !details.isEmpty()) {
                                        String uuid = action.path("uuid").asText();
                                        //Log.e("Details",details);
                                        pendingActionsMap.put(uuid, details);
                                    }
                                }
                            }
                            setupPendingActionsListView();
                        }
                    });
                    response.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecommendedActionsListView(){
        ListView recommendedActionsListView = findViewById(R.id.recommendedActionsListView);
        List<String> actionsList = new ArrayList<String>(recommendedActionsMap.values());
        if(actionsList.size() > 0) {
            final List<String> actionsListUUIDs = new ArrayList<String>(recommendedActionsMap.keySet());
            recommendedActionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsList);
            recommendedActionsListView.setAdapter(recommendedActionsAdapter);

            recommendedActionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(ActionsListActivity.this, ActionDetails.class );
                    String ActionUUID = actionsListUUIDs.get(position);
                    i.putExtra("ActionUUID", ActionUUID);
                    i.putExtra("IP",ip);
                    i.putExtra("Cookie",cookie);
                    i.putExtra("Executable",false);
                    startActivity(i);
                }
            });
        } else {
            actionsList.add(NO_ACTIONS);
            recommendedActionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsList);
            recommendedActionsListView.setAdapter(recommendedActionsAdapter);
        }
    }
    private void fillRecommendedActionsList() {
        if(recommendedActionsMap !=null && recommendedActionsMap.size() > 0){
            setupRecommendedActionsListView();
            return;
        }
        recommendedActionsMap = new HashMap<>();
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions",
                "{\"environmentType\": \""+environmentType+"\",\"actionStateList\": [\"RECOMMENDED\"],\"riskSeverityList\": [\""+severity+"\"]}",
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
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Double actionsCount = 0.0;
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                for (JsonNode action : json) {
                                    String details = action.path("details").asText();
                                    if (details != null && !details.isEmpty()) {
                                        String uuid = action.path("uuid").asText();
                                        //Log.e("Details",details);
                                        recommendedActionsMap.put(uuid, details);
                                    }
                                }
                            }
                            setupRecommendedActionsListView();
                        }
                    });
                    response.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupAcceptedActionsListView() {
        ListView acceptedActionsListView = findViewById(R.id.acceptedActionsListView);
        List<String> actionsList = new ArrayList<String>(acceptedActionsMap.values());
        if(actionsList.size() > 0) {
            final List<String> actionsListUUIDs = new ArrayList<String>(acceptedActionsMap.keySet());
            acceptedActionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsList);
            acceptedActionsListView.setAdapter(acceptedActionsAdapter);
            acceptedActionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(ActionsListActivity.this, ActionDetails.class );
                    String ActionUUID = actionsListUUIDs.get(position);
                    i.putExtra("ActionUUID", ActionUUID);
                    i.putExtra("IP",ip);
                    i.putExtra("Cookie",cookie);
                    i.putExtra("Executable",false);
                    startActivity(i);
                }
            });
        } else {
            actionsList.add("No actions found");
            acceptedActionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsList);
            acceptedActionsListView.setAdapter(acceptedActionsAdapter);
        }
    }

    private void fillAcceptedActionsList() {
        if(acceptedActionsMap !=null && acceptedActionsMap.size() > 0){
            setupAcceptedActionsListView();
            return;
        }
        acceptedActionsMap = new HashMap<>();
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions",
                "{\"startTime\": \"2019-05-06T00:00:00+00:00\",\"environmentType\": \""+environmentType+"\",\"actionStateList\": [\"ACCEPTED\",\"FAILED\",\"SUCCEEDED\"],\"riskSeverityList\": [\""+severity+"\"]}",
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
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("ACCEPTED",resp);
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                for (JsonNode action : json) {
                                    String details = action.path("details").asText();
                                    if (details != null && !details.isEmpty()) {
                                        String uuid = action.path("uuid").asText();
                                        acceptedActionsMap.put(uuid, details);
                                    }
                                }
                            }
                            setupAcceptedActionsListView();
                        }
                    });
                    response.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PendingActionsFragment extends Fragment {

        public PendingActionsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pending_actions_list, container, false);
            return rootView;
        }
    }
    public static class RecommendedFragment extends Fragment {

        public RecommendedFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_recommended_actions_list, container, false);
            return rootView;
        }
    }
    public static class AcceptedActionsFragment extends Fragment {

        public AcceptedActionsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_accepted_actions_list, container, false);
            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == 0) {
                return new PendingActionsFragment();
            } else if (position == 1) {
                return new RecommendedFragment();
            } else if (position == 2)  {
                return new AcceptedActionsFragment();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}
