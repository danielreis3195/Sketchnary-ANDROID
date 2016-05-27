package com.game.sketchnary.sketchnary.Main;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.game.sketchnary.sketchnary.Authentication.LoginActivity;
import com.game.sketchnary.sketchnary.Main.Room.Game.Play;
import com.game.sketchnary.sketchnary.Main.Room.Room;
import com.game.sketchnary.sketchnary.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import static com.game.sketchnary.sketchnary.Authentication.LoginActivity.IP_ADRESS;

public class MainMenuActivity extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener, FindGameFragment.OnHeadlineSelectedListener{
    public static ArrayList<Room> rooms = new ArrayList<Room>();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String status = (String)message.obj;
            if(status.equals("Refresh")){
                Fragment fragment = new FindGameFragment();
                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.contentFragment, fragment)
                        .commit();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //Making login on the game

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadGameRooms();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null){
            loadGameRooms();
        }

    }

    private void loadGameRooms() {
        final ProgressDialog progressDialog = new ProgressDialog(MainMenuActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading Rooms");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(){
            public void run(){
                rooms = makeRequest();
                Message message = mHandler.obtainMessage(1,"Refresh");
                message.sendToTarget();
                progressDialog.dismiss();
            }
        }.start();
    }

    private ArrayList<Room> makeRequest() {
        ArrayList<Room> res = new ArrayList<Room>();
        try {
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(getAssets().open("Keys/truststore.bks"), "123456".toCharArray());

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            URL url = new URL("https://" + IP_ADRESS + "/api/room/?rooms=all");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setConnectTimeout(5000);
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject rooms;


            rooms = new JSONObject(sb.toString());

            JSONArray array = rooms.getJSONArray("rooms");
            for (int i = 0; i < array.length(); i++) {
                String room = array.getJSONObject(i).getString("room");
                ArrayList<String> playersEmail = new ArrayList<String>();
                JSONArray players = array.getJSONObject(i).getJSONArray("players");
                for (int x = 0; x < players.length(); x++) {
                    String player_email = players.getJSONObject(x).getString(new Integer(x).toString());
                    playersEmail.add(player_email);
                }
                res.add(new Room(room,playersEmail));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return res;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_NewGame) {
            // Handle the camera action
            Fragment fragment = new FindGameFragment();
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.contentFragment, fragment)
                    .commit();
        } else if (id == R.id.nav_FindGame) {

        } else if (id == R.id.nav_Friends) {

        } else if (id == R.id.nav_GameOptions) {

        } else if (id == R.id.nav_Share) {

        } else if (id == R.id.nav_Settings) {

        }else if (id == R.id.nav_Logout) {
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onArticleSelected(int position) {
        System.out.println("oi");
        //tenho de iniciar uma nova atividade com os dados certos. Tenho de pedir o número de
        //jogadores presentes na sala e os dados deles.
        //MUDAR ISTO
        Intent intent = new Intent(this, Play.class);
        startActivity(intent);
        //ISTO ESTÀ BEM
       /* Intent intent = new Intent(this, RoomData.class);
        intent.putExtra("RoomNumber",item.roomNumber.toString());
        intent.putExtra("RoomCategory",item.roomCategory.toString());
        intent.putExtra("CurrentPlayers",item.currentPlayers.toString() );
        startActivity(intent);*/


    }
}
