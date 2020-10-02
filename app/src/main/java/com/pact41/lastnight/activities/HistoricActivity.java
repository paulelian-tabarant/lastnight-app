package com.pact41.lastnight.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.pact41.lastnight.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Robin on 26/01/2017.
 */

public class HistoricActivity extends MenuActivity {
    private ListView historic;
    private ArrayList<String> parties;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historic);

        historic=(ListView) findViewById(R.id.historic);
        parties = new ArrayList<>();

        //Permet d'accéder au profil de la soiréee
        historic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //The item index matches with the files list which is saved into the activity
                startPartyProfileActivity(false, parties.get(i));
            }
        });

        new FillPartyItemTask().execute();
    }

    @Override
    protected int getId(){
        return R.id.menu_item_reports;
    }

    private class FillPartyItemTask extends AsyncTask<String, Integer, SimpleAdapter> {
        @Override
        protected SimpleAdapter doInBackground(String... params) {
            ArrayList<HashMap<String,String>> list_of_parties= new ArrayList<HashMap<String,String>>();
            //Getting recap folders into external directory and listing all parties from them
            File[] files = getExternalFilesDir(null).listFiles();

            HashMap<String,String> map;
            for(int i=0; i < files.length; i++)
            {
                String fileName = files[i].getName();
                if(fileName.contains(stateManager.getCurrentUser())) {
                    int eventStartIndex = fileName.indexOf('_') + 1;
                    int eventEndIndex = fileName.indexOf('.');
                    String eventId = fileName.substring(eventStartIndex, eventEndIndex);
                    parties.add(eventId);
                    map = new HashMap<String, String>();
                    map.put("party_picture", String.valueOf(R.drawable.default_party_picture));
                    map.put("party_name", eventId);
                    map.put("party_date", getConnection().getEventHour(eventId));
                    map.put("party_place", getConnection().getEventPlace(eventId));
                    list_of_parties.add(map);
                }
            }
            SimpleAdapter historicAdapter = new SimpleAdapter (HistoricActivity.this, list_of_parties, R.layout.partyprofile,
                    new String[] {"party_picture", "party_name", "party_date", "party_place"}, new int[] {R.id.party_picture, R.id.party_name, R.id.party_date, R.id.party_place});
            return historicAdapter;
        }

        @Override
        protected void onPostExecute(SimpleAdapter historicAdapter) {
            historic.setAdapter(historicAdapter);
        }
    }
}