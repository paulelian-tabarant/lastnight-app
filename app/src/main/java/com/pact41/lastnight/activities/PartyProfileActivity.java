package com.pact41.lastnight.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.model.LastNightStateManager;
import com.pact41.lastnight.services.SensorService;

import java.util.Calendar;

/**
 * Created by Robin on 26/01/2017.
 */

public class PartyProfileActivity extends NetworkActivity {

    private ImageView picture;
    private TextView name,date,place,hours_begin,hours_end,price;
    private Button sign_up=null;

    //Intent extra to stop the service when the party end hour is reached
    public static String TIME_REACHED_ACTION = "com.pact41.lastnight.intent.action.TIME_REACHED_ACTION";
    public static String SENSOR_SERVICE_PARTY_NAME_EXTRA = "com.pact41.lastnight.intent.action.SENSOR_SERVICE_PARTY_NAME_EXTRA";
    public static String SENSOR_SERVICE_USERNAME_EXTRA = "com.pact41.lastnight.intent.action.SENSOR_SERVICE_USERNAME_EXTRA";
    public static String SENSOR_SERVICE_CLIENT_EXTRA = "com.pact41.lastnight.intent.action.SENSOR_SERVICE_CLIENT_EXTRA";
    //Test constant field
    private String eventId;
    private Button recap=null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        RelativeLayout currentLayout = (RelativeLayout) RelativeLayout.inflate(this, R.layout.full_party_profile, null);

        picture=(ImageView) currentLayout.findViewById(R.id.full_party_pp);

        //Getting party name from the activity which has launched the party profile activity
        eventId = getIntent().getStringExtra(MenuActivity.PARTY_NAME);

        name=(TextView) currentLayout.findViewById(R.id.full_party_name);
        name.setText(eventId);

        date=(TextView) currentLayout.findViewById(R.id.full_party_date);

        place=(TextView) currentLayout.findViewById(R.id.full_party_place);

        hours_begin=(TextView) currentLayout.findViewById(R.id.full_party_hours_begin);

        hours_end=(TextView) currentLayout.findViewById(R.id.full_party_hours_end);

        price=(TextView) currentLayout.findViewById(R.id.full_party_priceedit);

        sign_up=(Button) currentLayout.findViewById(R.id.full_party_sign_up);
        recap=(Button) currentLayout.findViewById(R.id.full_party_recap);

        //Fills the widgets with the loaded event, from the server database
        new GetEventInfosTask().execute(eventId);

        //Removes "join" button from the layout if not needed
        if(!(getIntent().getBooleanExtra(MenuActivity.DISPLAY_JOIN_BUTTON, false)))
            currentLayout.removeView(sign_up);
        else{
            sign_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new JoinEventTask().execute(eventId);
                }
            });
        }

        //If we don't want to show the recap button
        if(!(getIntent().getBooleanExtra(MenuActivity.DISPLAY_RECAP_BUTTON, false))){
            currentLayout.removeView(recap);
        }
        else {
            //Will tell the other activity that we want to generate the party recap
            recap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }
        setContentView(currentLayout);
    }

    private class GetEventInfosTask extends AsyncTask<String, Integer, String[]>{
        @Override
        protected String[] doInBackground(String... params) {
            String eventId = params[0];
            String[] eventInfos = {
                    getConnection().getEventPlace(eventId),
                    getConnection().getEventHour(eventId),
                    String.valueOf(getConnection().getEventPrice(eventId))};
            return eventInfos;
        }

        @Override
        protected void onPostExecute(String[] eventInfos) {
            for(String info : eventInfos){
                if(info == null){
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
            }
            place.setText(eventInfos[0]);
            String fullDate = eventInfos[1];
            String day = fullDate.substring(0, 10);
            String startHour = fullDate.substring(11, 16);
            String endHour = fullDate.substring(17, 22);
            date.setText(day);
            hours_begin.setText(startHour);
            hours_end.setText(endHour);
            price.setText(eventInfos[2]);
        }
    }

    private class JoinEventTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String eventName = params[0];
            return getConnection().joinEvent(stateManager.getCurrentUser(), eventName);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                //Initialization of the intent to launch the sensor data capture service
                Intent sensorService = new Intent(PartyProfileActivity.this, SensorService.class);
                //Getting and setting when the service must be stopped
                Calendar calendar = Calendar.getInstance();

                String minute = hours_end.getText().toString().substring(3);
                String hour = hours_end.getText().toString().substring(0, 2);
                calendar.set(Calendar.MINUTE, Integer.valueOf(minute));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));

                //If this condition is verified, it means that the event ends the next day (ex. 23h --> 03h).
                //Then we must add a day to the date (we consider the event ends less than 24h after its beginning).
                if(calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
                        calendar.add(Calendar.DAY_OF_MONTH, 1);

                // Then we launch the service
                sensorService.putExtra(TIME_REACHED_ACTION, false);
                LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();
                stateManager.notifyJoiningEvent(name.getText().toString()); //Indicates the user has joined a party
                //We add extras which will be saved by the service to send score at each specified time interval.
                sensorService.putExtra(SENSOR_SERVICE_PARTY_NAME_EXTRA, stateManager.getCurrentPartyName());
                sensorService.putExtra(SENSOR_SERVICE_USERNAME_EXTRA, stateManager.getCurrentUser());
                startService(sensorService);
                // Then we set an alarm to stop the service when time is reached
                sensorService.putExtra(TIME_REACHED_ACTION, true);
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent.getService(PartyProfileActivity.this, 0, sensorService, 0));
                setResult(RESULT_OK); //Confirms that the user has joined the party
                Toast.makeText(PartyProfileActivity.this, "Vous avez bien rejoint la soirée. Elle se terminera à "
                        +calendar.get(Calendar.HOUR_OF_DAY)+"h"+calendar.get(Calendar.MINUTE)+".", Toast.LENGTH_LONG).show();
                finish();
            }
            else
                Toast.makeText(PartyProfileActivity.this, R.string.full_party_cant_join, Toast.LENGTH_LONG).show();
        }
    }
}
