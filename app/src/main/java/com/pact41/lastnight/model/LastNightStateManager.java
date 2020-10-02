package com.pact41.lastnight.model;

/**
 * Created by Paul-Elian on 16/02/2017.
 */

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import com.pact41.lastnight.R;
import com.pact41.lastnight.client_server.MyClient;

/**
 * Instance of the current app state
 */
public class LastNightStateManager {

    /* Preference file useful keys */
    private final String PREFERENCE_FILE_KEY = "com.pact41.lastnight.PREFERENCE_FILE_KEY";
    private final String CURRENT_USER_KEY = "com.pact41.lastnight.PREFERENCE_FILE_CURRENT_USER_KEY";
    private final String CURRENT_PARTY_NAME_KEY = "com.pact41.lastnight.PREFERENCE_FILE_CURRENT_PARTY_NAME_KEY";
    private final String ONLINE_MODE_KEY = "com.pact41.lastnight.PREFERENCE_FILE_ONLINE_MODE_KEY";
    private final String PARTY_IN_PROGRESS_KEY = "com.pact41.lastnight.PREFERENCE_FILE_PARTY_IN_PROGRESS_KEY";

    private SharedPreferences lastNightSharedPref;

    /** True if the user is currently at a party */
    private boolean partyInProgress;
    private String partyName;
    private String currentUser;

    /** Stores a reference to the object handling the connection to the server */
    private MyClient appConnection;

    private boolean onlineMode = true;

    public LastNightStateManager(Application appInstance)
    {
        appConnection = new MyClient();

        //Initializing the app state from the preference file
        lastNightSharedPref = appInstance.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        partyInProgress = lastNightSharedPref.getBoolean(PARTY_IN_PROGRESS_KEY, false);
        partyName = lastNightSharedPref.getString(CURRENT_PARTY_NAME_KEY, null);
        currentUser = lastNightSharedPref.getString(CURRENT_USER_KEY, null);
        onlineMode = lastNightSharedPref.getBoolean(ONLINE_MODE_KEY, true);
    }

    /**
     * Tells if a party has been joined by the user or not
     * @return "true" if the user is at a party, else "false"
     */
    public boolean isPartyInProgress()
    {
        return partyInProgress;
    }

    public void notifyJoiningEvent(String partyName)
    {
        this.partyInProgress = true;
        this.partyName = partyName;
        saveStateIntoPrefs();
    }

    public void notifyLeavingEvent() {
        this.partyInProgress = false;
        this.partyName = null;
        saveStateIntoPrefs();
    }

    public void notifyConnectionOf(String user){
        this.currentUser = user;
        saveStateIntoPrefs();
    }

    public String getCurrentUser(){
        return currentUser;
    }

    public String getCurrentPartyName(){
        return partyName;
    }

    public MyClient getClient() {
        return appConnection;
    }

    public void activateOfflineMode(){
        onlineMode = false;
        saveStateIntoPrefs();
    }

    public void disableOfflineMode(){
        onlineMode = true;
        saveStateIntoPrefs();
    }

    public boolean isOnlineModeActivated(){
        return onlineMode;
    }

    private void saveStateIntoPrefs()
    {
        //Gets an instance of an editor object which lets you write into the prefs file of the app
        SharedPreferences.Editor prefsEditor = lastNightSharedPref.edit();
        //Saving all changes into the object
        prefsEditor.putBoolean(PARTY_IN_PROGRESS_KEY, partyInProgress);
        prefsEditor.putString(CURRENT_PARTY_NAME_KEY, partyName);
        prefsEditor.putString(CURRENT_USER_KEY, currentUser);
        prefsEditor.putBoolean(ONLINE_MODE_KEY, onlineMode);
        //Writing changes into the static preferences file
        prefsEditor.apply();
    }
}
