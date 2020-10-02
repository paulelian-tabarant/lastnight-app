package com.pact41.lastnight.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.client_server.MyClient;
import com.pact41.lastnight.model.LastNightStateManager;

/* Extend this class on all activities needing network connection */
public abstract class NetworkActivity extends AppCompatActivity {

    protected MyClient client;
    LastNightStateManager stateManager;

    private ProgressDialog connectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stateManager = ((LastNight)getApplicationContext()).getAppStateManager();
        client = stateManager.getClient();
        connectionDialog = new ProgressDialog(this);
        connectionDialog.setMessage(getResources().getString(R.string.network_connection_processing));
        connectionDialog.setCanceledOnTouchOutside(false); //Avoids being canceled by user
    }

    protected MyClient getConnection(){
            return client;
    }

    public void notifyConnectionAttempt(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionDialog.show();
            }
        });
    }

    public void notifyConnectionResult(final boolean success){
        //We launch the method into the client connection thread so we need to indicate runOnUiThread for Toast messages
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(success){
                    Toast.makeText(NetworkActivity.this, R.string.network_connection_success, Toast.LENGTH_SHORT).show();
                    refresh();
                }
                else
                    Toast.makeText(NetworkActivity.this, R.string.network_connection_error, Toast.LENGTH_SHORT).show();
            }
        });
        if(connectionDialog != null) connectionDialog.dismiss();
    }

    public void notifyNetworkUnavailibility() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NetworkActivity.this, R.string.network_connection_unavailable, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onResume(){
        super.onResume();
        if(stateManager.isOnlineModeActivated())
            client.init(this);
    }

    protected void refresh(){
        finish();
        overridePendingTransition( 0, 0);
        startActivity(getIntent());
        overridePendingTransition( 0, 0);
    }
}
