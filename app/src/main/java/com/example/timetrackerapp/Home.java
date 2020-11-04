package com.example.timetrackerapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.timetrackerapp.adapters.EventAdapter;
import com.example.timetrackerapp.adapters.RecyclerViewEventsAdapter;
import com.example.timetrackerapp.common.Common;
import com.example.timetrackerapp.model.EventsWeek;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";
    private GoogleSignInClient mGoogleSignInClient;
    private TextView titleHome;
    private BarChart barChart;
    private RecyclerView recyclerView;
    private RecyclerViewEventsAdapter recyclerViewEventsAdapter;
    private ArrayList doInBackGroundResults = new ArrayList();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        barChart = (BarChart) findViewById(R.id.barchart);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_events);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        titleHome = (TextView)findViewById(R.id.home_title);

        //Configure sign-in to request the user;s ID, email address, and basic profile. ID and basic profile are included in DEFAULT SIGN IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .requestEmail()
                .build();

        //Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                handleSignInResult(task);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_logout:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void printBarCharHome(ArrayList eventData){
        BarDataSet bardataset = new BarDataSet((ArrayList<BarEntry>)eventData.get(0), "");

        BarData data = new BarData(Common.LABELS, bardataset);
        barChart.setData(data); // set the data and list of labels into chart
        bardataset.setBarSpacePercent(50f);

        //barChart.setDescription("Set Bar Chart Description Here");  // set the description
        bardataset.setColors((int [])eventData.get(1));

        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getLegend().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getContentDescription();
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#FFFFFF"));
        barChart.setDescription("");
        barChart.animateY(5000);

    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idTokenString = account.getIdToken();
            String authCode = account.getServerAuthCode();

            new ThreadAdapter().execute(idTokenString, authCode);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(Home.this, "Failed from Home", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signOut(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Home.this, "Successfully signed out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Home.this, Login.class));
                        finish();
                    }
                });
    }

    private class ThreadAdapter extends AsyncTask<String, Void, ArrayList> {
        private ProgressDialog pdia;
        private String accessToken;
        private EventAdapter eventAdapter = new EventAdapter();

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected ArrayList doInBackground(String... strings) {

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    // Specify the CLIENT_ID of the app that accesses the backend:
                    .setAudience(Collections.singletonList(getString(R.string.server_client_id)))
                    // Or, if multiple clients access the backend:
                    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                    .build();

            // Exchange auth code for access token
            GoogleTokenResponse tokenResponse = null;
            try {
                //System.out.println("Token " + strings[0] + " \n " +  strings[1]);
                tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance(),
                        "https://oauth2.googleapis.com/token",
                        getString(R.string.server_client_id),
                        getString(R.string.client_secret),
                        strings[1],
                        "")  // Specify the same redirect URI that you use with your web
                        // app. If you don't have a web version of your app, you can
                        // specify an empty string.
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            accessToken = tokenResponse.getAccessToken();
            Events events;
            try {
                // Build a new authorized API client service.
                Calendar service = new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), new GoogleCredential().setAccessToken(accessToken))
                        .build();

                // List the next 10 events from the primary calendar.
                DateTime now = new DateTime(System.currentTimeMillis());
                LocalDateTime sevenDays = LocalDateTime.now().plusDays(Common.DAYS.length);//))+1
                Date convertedDate = Date.from(sevenDays.atZone(ZoneId.systemDefault()).toInstant());
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                DateTime oneWeek = new DateTime(convertedDate);
                titleHome.setText("Events o Week\n From (" + dateFormat.format(LocalDateTime.now()) + ") Until (" +
                        dateFormat.format(sevenDays) + ")");

                events = service.events().list("primary")
                        .setMaxResults(99)
                        .setTimeMin(now)
                        .setTimeMax(oneWeek)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();

                HashMap<String, EventsWeek> map = eventAdapter.getEventsInMap(events);
                doInBackGroundResults = eventAdapter.getDailyEventHoursSpent(map);
                doInBackGroundResults.add(map);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return doInBackGroundResults;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(Home.this);
            pdia.setMessage("Loading...");
            pdia.show();
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            if (result.size() >= 1) {
                printBarCharHome(result);
                // specify an adapter with the result to show*/
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerViewEventsAdapter = new RecyclerViewEventsAdapter(result, Home.this);
                recyclerView.setAdapter(recyclerViewEventsAdapter);

                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @SuppressLint("StaticFieldLeak")
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onRefresh() {
                        if (swipeRefreshLayout.isRefreshing()) {
                            new AsyncTask<Void, Void, ArrayList>() {
                                @Override
                                protected ArrayList doInBackground(Void... voids) {
                                    ArrayList<Object> arrayList = new ArrayList<>();
                                    Events events = null;

                                    // Build a new authorized API client service.
                                    Calendar service = new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), new GoogleCredential().setAccessToken(accessToken))
                                            .build();

                                    // List the next 10 events from the primary calendar.
                                    DateTime now = new DateTime(System.currentTimeMillis());
                                    LocalDateTime sevenDays = LocalDateTime.now().plusDays(Common.DAYS.length);//i had plus one +1
                                    Date convertedDate = Date.from(sevenDays.atZone(ZoneId.systemDefault()).toInstant());
                                    DateTime oneWeek = new DateTime(convertedDate);
                                    try {
                                        events = service.events()
                                                .list("primary")
                                                .setMaxResults(99)
                                                .setTimeMin(now)
                                                .setTimeMax(oneWeek)
                                                .setOrderBy("startTime")
                                                .setSingleEvents(true)
                                                .execute();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    HashMap<String, EventsWeek> items = eventAdapter.getEventsInMap(events);
                                    arrayList = eventAdapter.getDailyEventHoursSpent(items);
                                    arrayList.add(items);

                                    return arrayList;
                                }

                                @Override
                                protected void onPostExecute(ArrayList arrayList) {
                                    printBarCharHome(arrayList);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    recyclerViewEventsAdapter = new RecyclerViewEventsAdapter(arrayList, Home.this);
                                    recyclerView.setAdapter(recyclerViewEventsAdapter);
                                    super.onPostExecute(arrayList);
                                }
                            }.execute();
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        pdia.dismiss();
                    }
                });
                pdia.dismiss();

            }else {
                pdia.dismiss();
                recyclerViewEventsAdapter.notifyDataSetChanged();
                Toast.makeText(Home.this, "Empty Calendar", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
    }
}