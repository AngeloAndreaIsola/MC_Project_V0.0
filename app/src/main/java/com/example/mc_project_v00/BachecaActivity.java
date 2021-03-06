package com.example.mc_project_v00;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;
import com.example.mc_project_v00.database.AppExecutors;
import com.example.mc_project_v00.database.DatabaseClient;
import com.example.mc_project_v00.database.PostContentImage;
import com.example.mc_project_v00.database.PostRoomDatabase;
import com.google.gson.JsonObject;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BachecaActivity extends AppCompatActivity implements OnListClickListener {

    private static final String TAG = "BachecaActivity";
    private String sidString = null; 
    private BachecaAdapter adapter;
    private Context context = null;
    private Boolean togle = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bacheca);
        Log.d(TAG, "On Create");

        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("ACCORDO");

        SharedPreferences preferences = getSharedPreferences("User preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        sidString = preferences.getString("sid", null);
        adapter = new BachecaAdapter(this, this);
        context = this;


        //PULISCE TUTTO IL DB NEL CASO SERVA
        /*
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                PostRoomDatabase postRoomDatabase = DatabaseClient.getInstance(context).getPostRoomDatabase();
                postRoomDatabase.clearAllTables();
            }
        });

         */





        //CONTROLLA CHE SIA IL PRIMO ACCESSO DELL'UTENTE

        if (preferences.getBoolean("firstLogin", true)) {

            Log.d(TAG, "sid: " + preferences.getString("sid",null));

            register();

            Log.d(TAG, "sid: " + preferences.getString("sid",null));

            editor.putBoolean("firstLogin", false);
            editor.apply();

        } else if (preferences.getString("sid",null) != null){
            //sidString = preferences.getString("sid", null);
            Log.d(TAG, "sid: " + preferences.getString("sid",null));
            UserData.sid = preferences.getString("sid",null);
            try {
                saveProfile();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            refreshWall();
        }

    }




    private void register() {
        ComunicationController ccBacheca = new ComunicationController(this);
        ccBacheca.register(response -> {
            try {
                saveSID_inSharedPreferences(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            if (error instanceof NoConnectionError){
                AlertDialog.Builder failRegistrationDialog = new AlertDialog.Builder(context);
                failRegistrationDialog.setTitle("Nessuna connessione, registrazione fallita");
                failRegistrationDialog.setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        register();
                    }
                });
                failRegistrationDialog.setNegativeButton("Cancella",null);
                failRegistrationDialog.show();
            }else {
                AlertDialog.Builder failRegistrationDialog = new AlertDialog.Builder(context);
                failRegistrationDialog.setTitle("Registrazione fallita");
                failRegistrationDialog.setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        register();
                    }
                });
                failRegistrationDialog.setNegativeButton("Cancella",null);
                failRegistrationDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bacheca, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.refresh:
                Toast.makeText(context, "Ricarica canali", Toast.LENGTH_SHORT).show();
                refreshWall();
                break;

            case R.id.addChannel:
                AlertDialog.Builder channelDialog = new AlertDialog.Builder(context);
                channelDialog.setTitle("Aggiungi nuovo canale: ");
                final EditText channelName = new EditText(context);
                channelName.setInputType(InputType.TYPE_CLASS_TEXT);
                channelDialog.setView(channelName);
                channelDialog.setPositiveButton("Aggingi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String channel= channelName.getText().toString();
                        Log.d(TAG, channel);
                        addChannelAndRefresh(channel);
                    }
                });
                channelDialog.setNegativeButton("Cancella",null);
                channelDialog.show();
                break;

            case R.id.settings:
                startActivity(new Intent(BachecaActivity.this, SettingsActivity.class));
                break;


            case R.id.prefered_filter:
                Log.d(TAG, "Clik su filtro preferiti");

        }


        return super.onOptionsItemSelected(item);
    }


    private void showWall(JSONObject response) throws JSONException {
        Log.d(TAG, "request correct: "+ response.toString());

        //colleghiamo model e dapter
        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        //BachecaAdapter adapter = new BachecaAdapter(this, this);
        rv.setAdapter(adapter);

        BachecaModel.getInstance().addAndSortData(response);
        adapter.notifyDataSetChanged();
    }


    private void reportErrorToUsers(VolleyError error){
        Log.d(TAG, "Errore richiesta: " + error.toString());
        Toast.makeText(this,"Errore richiesta: " + error.toString(), Toast.LENGTH_LONG).show();
    }

    public void saveSID_inSharedPreferences(JSONObject response) throws JSONException {
        SharedPreferences preferences = getSharedPreferences("User preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putString("sid", response.getString("sid"));
            editor.apply();
        } catch (JSONException e) {
            Log.d(TAG, "parsing failed");
        }
        Log.d(TAG, "request correct: "+ response.toString());
        Log.d(TAG, "request saved: "+ preferences.getAll().toString());

        sidString = preferences.getString("sid", null);

        UserData.sid = preferences.getString("sid", null);

        saveProfile();
        refreshWall();
    }

    private void saveProfile() throws JSONException {
        ComunicationController comunicationController = new ComunicationController(this);
        comunicationController.getProfile(UserData.sid, responseProfile -> {
            try {
                UserData.pversion = responseProfile.getInt("pversion");
                UserData.picture = responseProfile.getString("picture");
                UserData.uid = responseProfile.getString("uid");
                UserData.username = responseProfile.getString("name");

                Log.d(TAG, "Profilo salvato:  Sid=" + UserData.sid + " Uid=" + UserData.uid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> reportErrorToUsers(error));

    }

    @Override
    public void onListClick(int position) throws JSONException {
        Log.d("RecycleViewExample", "From Main Activity: " + position);
        String nomeCanale = BachecaModel.getInstance().getChannelFromList(position);
        Intent i = new Intent(BachecaActivity.this, CanaleActivity.class);
        i.putExtra("nomeCanale", nomeCanale);
        i.putExtra("position", position);
        startActivity(i);

    }

    private void addChannelAndRefresh(String cTitle) {
        ComunicationController ccBacheca = new ComunicationController(context);
        if (cTitle.length() >= 20){
            Toast.makeText(context, "Il nome del canale non può essere piu lungo  o uguale a 20 caratteri", Toast.LENGTH_SHORT).show();
        }else {
            try {
                ccBacheca.addChannel(sidString, cTitle, response -> refreshWall() , error -> reportErrorToUsers(error));
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshWall(){
        ComunicationController ccBacheca = new ComunicationController(context);
        try {
            ccBacheca.getWall(sidString, response -> {
                try {
                    showWall(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                if (error instanceof NoConnectionError){
                    AlertDialog.Builder noConnectionDialog = new AlertDialog.Builder(context);
                    noConnectionDialog.setTitle("Nessuna connessione");
                    noConnectionDialog.setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refreshWall();
                        }
                    });
                    noConnectionDialog.setNegativeButton("Cancella",null);
                    noConnectionDialog.show();
                }else {
                    Toast.makeText(this,"Errore richiesta: " + error.toString(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }
}










