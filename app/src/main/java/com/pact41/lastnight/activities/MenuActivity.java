package com.pact41.lastnight.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.pact41.lastnight.R;
import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.model.LastNightStateManager;

import java.util.List;

public abstract class MenuActivity extends NetworkActivity {

    //Stores a reference to the app state
    protected LastNightStateManager stateManager;

    //Extra intents' useful keys
    public final static String DISPLAY_JOIN_BUTTON = "com.pact41.lastnight.intent.extra.DISPLAY_JOIN_BUTTON";
    //Indicates to the party profile activity that it can show the button to generate recap
    public final static String DISPLAY_RECAP_BUTTON = "com.pact41.lastnight.intent.action.DISPLAY_RECAP_BUTTON";
    public final static String PARTY_NAME = "com.pact41.lastnight.intent.extra.PARTY_NAME";
    public final static String RECAP_FILE_DIRECTORY = "com.pact41.lastnight.intent.extra.RECAP_FILE_DIRECTORY";

    //Requests for activities
    public final static int JOIN_PARTY_REQUEST = 0;
    public final static int GENERATE_RECAP_REQUEST = 2;
    private String recapFile;

    //Dialog to display an alert before quiting the app, if it is the last opened activity
    AlertDialog quitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        stateManager = ((LastNight) getApplicationContext()).getAppStateManager();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.menu_quit_alert_message)
                .setTitle(R.string.menu_quit_alert_title);
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quitDialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quitDialog.dismiss();
            }
        });
        quitDialog = builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        //Generates menu into the toolbar
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.appmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(getId() != item.getItemId()) {
            switch (item.getItemId()) {
                //Edit profile menu
                case R.id.menu_item_profile:
                    openProfileMenu();
                    break;
                case R.id.menu_item_current_party:
                    openCurrentPartyMenu();
                    break;
                case R.id.menu_item_friends:
                    openFriendListMenu();
                    break;
                case R.id.menu_item_reports:
                    openHistoryMenu();
                    break;
                case R.id.menu_item_organize:
                    openOrganizeMenu();
                    break;
                case R.id.menu_refresh_button:
                    refresh();
                    break;
            }
            return super.onOptionsItemSelected(item);
        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.menu_already_into_menu_message, item.getTitle()), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //Methods to tell the activity that we want to open one of the 5 menus
    protected void openProfileMenu(){
        Intent myProfileActivity = new Intent(MenuActivity.this, MyProfileActivity.class);
        startActivity(myProfileActivity);
        finish();
    }

    protected void openCurrentPartyMenu(){
        Intent joinPartyActivity;
        if(stateManager.isPartyInProgress())
            joinPartyActivity = new Intent(MenuActivity.this, CurrentPartyActivity.class);
        else
            joinPartyActivity = new Intent(MenuActivity.this, QRCodeScanActivity.class);
        startActivity(joinPartyActivity);
        finish();
    }

    protected void openFriendListMenu(){
        Intent friendListActivity= new Intent(MenuActivity.this, FriendlistActivity.class);
        startActivity(friendListActivity);
        finish();
    }

    protected void openHistoryMenu(){
        Intent historicActivity=new Intent(MenuActivity.this, HistoricActivity.class);
        startActivity(historicActivity);
        finish();
    }

    protected void openOrganizeMenu(){
        Intent organizeActivity=new Intent(MenuActivity.this, OrganizeActivity.class);
        startActivity(organizeActivity);
        finish();
    }

    //For instances which need to display a party profile

    /**
     * Starts a party profile activity
     * @param isJoinable Tells if the party can be joined, give "false" if you want to show a past event
     * @param partyName Give the party name
     */
    protected void startPartyProfileActivity(boolean isJoinable, String partyName)
    {
        Intent partyProfileActivity = new Intent(MenuActivity.this, PartyProfileActivity.class);
        partyProfileActivity.putExtra(DISPLAY_JOIN_BUTTON, isJoinable); //We have to generate a party profile to join it
        partyProfileActivity.putExtra(DISPLAY_RECAP_BUTTON, !isJoinable);  //We have to generate a party profile to see its recap
        partyProfileActivity.putExtra(PARTY_NAME, partyName);//Put file name here
        if(isJoinable)
            startActivityForResult(partyProfileActivity, JOIN_PARTY_REQUEST);
        else{
            if(partyName == null)
            {
                Toast.makeText(this, "Party recap file not found", Toast.LENGTH_SHORT).show();
            }
            else
            {
                recapFile = partyName;
                startActivityForResult(partyProfileActivity, GENERATE_RECAP_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //The activity wants to display a joinable party
        if(requestCode == JOIN_PARTY_REQUEST){
            if(resultCode == RESULT_OK){
                openCurrentPartyMenu(); //Will now open directly the page of the joined party
                finish(); //QR code scan must be unavailable once a party is joined
            }
            else
                Toast.makeText(this, R.string.full_party_not_found, Toast.LENGTH_LONG).show();
        }
        //The activity wants to display a party recap
        if(requestCode == GENERATE_RECAP_REQUEST)
            if(resultCode== RESULT_OK){
                Intent partyRecapActivity = new Intent(MenuActivity.this, PartyRecapActivity.class);
                partyRecapActivity.putExtra(RECAP_FILE_DIRECTORY, stateManager.getCurrentUser()+"_"+recapFile+".txt");
                partyRecapActivity.putExtra(PARTY_NAME,recapFile);
                startActivity(partyRecapActivity);
            }
    }

    /** Gives the matching item menu ID of the activity */
    protected abstract int getId();

    @Override
    public void onBackPressed() {
        List<ActivityManager.RunningTaskInfo> taskList = ((ActivityManager)getSystemService(ACTIVITY_SERVICE))
                .getRunningTasks(10);
        //Checks if the activity is the last opened into the app
        if(taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(this.getClass().getName())) {

            quitDialog.show();
            return; //We handle the back pressed with the result of the quitDialog in this case
        }
        super.onBackPressed();
    }
}
