package com.turbonomic.turbomobile.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.turbonomic.turbomobile.R;
import com.turbonomic.turbomobile.RequestFactory;
import com.turbonomic.turbomobile.SSLCertificate;
import com.turbonomic.turbomobile.dto.ActionDTO;
import com.turbonomic.turbomobile.dto.EntityDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EntitiesAndActionsActivity extends AppCompatActivity {

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

    private String ip, cookie, entityType;
    private static ArrayList<ActionDTO> actionsList;
    private static ArrayList<EntityDTO> entitiesList;
    private static ArrayAdapter actionsAdapter,entitiesAdapter;
    private final String NO_ACTIONS = "No actions found";
    private final String NO_ENTITIES = "No entities found";
    private final String ENVIRONMENT_TYPE = "CLOUD";
    private TabLayout tabLayout;

    @Override
    protected void onDestroy() {
        actionsList = null;
        entitiesList = null;
        actionsAdapter = null;
        entitiesAdapter = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entities_and_actions);

        cookie = getIntent().getStringExtra("Cookie");
        ip = getIntent().getStringExtra("IP");
        entityType = getIntent().getStringExtra("EntityType");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.actions_entities_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sc_tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {
                    fillEntitiesList();
                } else {
                    fillActionsList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        fillEntitiesList();

    }

    private void setupEntitiesListView(){
        ListView entitiesListView = findViewById(R.id.scEntitiesListView);
        tabLayout = findViewById(R.id.sc_tabs);
        tabLayout.getTabAt(0).setText("ENTITIES ("+entitiesList.size()+")");
        if(entitiesList.size() > 0) {
            ArrayList<String> entitiesNames = new ArrayList<>();
            for(EntityDTO entityDTO : entitiesList) {
                entitiesNames.add(entityDTO.getDisplayName());
            }
            entitiesAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, entitiesNames);
            entitiesListView.setAdapter(entitiesAdapter);

            entitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(EntitiesAndActionsActivity.this,
                            EntityDetailsActivity.class );
                    i.putExtra("EntityUUID",entitiesList.get(position).getUuid());
                    i.putExtra("IP",ip);
                    i.putExtra("Cookie",cookie);
                    startActivity(i);
                }
            });
        } else {
            ArrayList<String> emptyList = new ArrayList<>();
            emptyList.add(NO_ENTITIES);
            entitiesAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, emptyList);
            entitiesListView.setAdapter(entitiesAdapter);
        }
    }

    private void fillEntitiesList() {
        if(entitiesList !=null && entitiesList.size() > 0){
            setupEntitiesListView();
            return;
        }
        Request request = RequestFactory.getInstance(
                ip,
                "search?types="+entityType+
                        "&environment_type="+ENVIRONMENT_TYPE+
                        "&order_by=name&ascending=true",
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
                    final ObjectMapper mapper = new ObjectMapper();
                    final JsonNode json = mapper.readTree(resp);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            entitiesList = new ArrayList<>();
                            Log.e("Entites",resp);
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                for (JsonNode entity : json) {
                                    String displayName = entity.path("displayName").asText();
                                    if (displayName != null && !displayName.isEmpty()) {
                                        EntityDTO entityDTO = setEntityInfo(entity);
                                        entitiesList.add(entityDTO);
                                    }
                                }
                            }
                            setupEntitiesListView();
                        }
                    });
                    response.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupActionsListView(){
        ListView recommendedActionsListView = findViewById(R.id.scActionsListView);
        tabLayout = findViewById(R.id.sc_tabs);
        tabLayout.getTabAt(1).setText("ACTIONS ("+actionsList.size()+")");
        if(actionsList.size() > 0) {
            ArrayList<String> actionsDetails = new ArrayList<>();
            for(ActionDTO actionDTO : actionsList) {
                actionsDetails.add(actionDTO.getDetails());
            }
            actionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, actionsDetails);
            recommendedActionsListView.setAdapter(actionsAdapter);

            recommendedActionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(EntitiesAndActionsActivity.this, ActionDetails.class );
                    i.putExtra("ActionDTO",actionsList.get(position));
                    i.putExtra("IP",ip);
                    i.putExtra("Cookie",cookie);
                    i.putExtra("Executable",false);
                    startActivity(i);
                }
            });
        } else {
            ArrayList<String> emptyList = new ArrayList<>();
            emptyList.add(NO_ACTIONS);
            actionsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.activity_listview, emptyList);
            recommendedActionsListView.setAdapter(actionsAdapter);
        }
    }

    private EntityDTO setEntityInfo(JsonNode entity){
        EntityDTO entityDTO = new EntityDTO();
        entityDTO.setUuid(entity.path("uuid").asText());
        entityDTO.setState(entity.path("state").asText());
        entityDTO.setClassName(entity.path("className").asText());
        entityDTO.setDisplayName(entity.path("displayName").asText());
        entityDTO.setSeverity(entity.path("severity").asText());
        entityDTO.setDiscoveredBy(entity.path("discoveredBy").path("displayName").asText());
        return entityDTO;
    }

    private ActionDTO setActionInfo(JsonNode action){
        ActionDTO actionDTO = new ActionDTO();
        actionDTO.setUuid(action.path("uuid").asText());
        actionDTO.setDetails(action.path("details").asText());
        actionDTO.setMode(action.path("actionMode").asText());
        actionDTO.setState(action.path("actionState").asText());
        actionDTO.setType(action.path("actionType").asText());
        actionDTO.setId(action.path("actionID").asText());
        actionDTO.setRiskDescription(action.path("risk")
                .path("description").asText());
        return actionDTO;
    }

    private void fillActionsList() {
        if(actionsList !=null && actionsList.size() > 0){
            setupActionsListView();
            return;
        }
        Request request = RequestFactory.getInstance(
                ip,
                "markets/Market/actions?order_by=severity&ascending=true",
                "{\"relatedEntityTypes\": [ \""+entityType+"\" ],\"environmentType\": \""+ENVIRONMENT_TYPE+"\"}",
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
                            actionsList = new ArrayList<>();
                            if (!"[]".equals(resp)) {
                                // If API response was not an empty list
                                for (JsonNode action : json) {
                                    String details = action.path("details").asText();
                                    if (details != null && !details.isEmpty()) {
                                        ActionDTO actionDTO  = setActionInfo(action);
                                        actionsList.add(actionDTO);
                                    }
                                }
                            }
                            setupActionsListView();
                        }
                    });
                    response.close();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class EntitiesFragment extends Fragment {

        public EntitiesFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sc_entities_list, container, false);
            return rootView;
        }
    }
    public static class ActionsFragment extends Fragment {

        public ActionsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_sc_actions_list, container, false);
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
            if(position == 0) {
                return new EntitiesFragment();
            } else if (position == 1) {
                return new ActionsFragment();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
