package com.pact41.lastnight.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.model.LastNightStateManager;

import static android.R.attr.checked;

public class LoginActivity extends NetworkActivity {
    private Button createAccount;

    private EditText login, password;
    private Button connection;
    private CheckBox offlineMode;
    public final static int CREATE_ACCOUNT_REQUEST = 0;

    LastNightStateManager stateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        stateManager = ((LastNight)getApplicationContext()).getAppStateManager();

        //Getting widgets' ID
        createAccount = (Button) findViewById(R.id.start_create_account);

        login = (EditText) findViewById(R.id.start_login);
        password = (EditText) findViewById(R.id.start_password);
        connection = (Button) findViewById(R.id.start_connection);
        offlineMode = (CheckBox) findViewById(R.id.start_offline_box);

        //To hide typed characters into the password EditText
        password.setTransformationMethod(new PasswordTransformationMethod());

        //Actions for buttons
        connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                password.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                if (!isEmptyEditText(login) && !isEmptyEditText(password)) {
                    new LoginTask().execute(login.getText().toString().trim(), password.getText().toString().trim());
                }
                else{
                    Toast toast= Toast.makeText(LoginActivity.this, R.string.start_null, Toast.LENGTH_LONG );
                    toast.show();
                    if (isEmptyEditText(login)) login.setBackgroundColor(getResources().getColor(R.color.colorError));
                    if (isEmptyEditText(password)) password.setBackgroundColor(getResources().getColor(R.color.colorError));
                }
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newAccountActivity = new Intent(LoginActivity.this, NewAccountActivity.class);
                startActivityForResult(newAccountActivity, CREATE_ACCOUNT_REQUEST); //Will have to use startActivityForResult
            }
        });

        //Determines the connection mode for the app behaviour. OfflineMode allows to use the app even if the server doesn't respond.
        offlineMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    stateManager.activateOfflineMode();
                else{
                    stateManager.disableOfflineMode();
                    getConnection().init(LoginActivity.this);
                }
            }
        });
    }

    @Override
    /* Used to get the result of a subscription */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CREATE_ACCOUNT_REQUEST){
            if(resultCode == RESULT_OK)
                Toast.makeText(LoginActivity.this, R.string.new_account_cheers, Toast.LENGTH_LONG).show();
        }
    }

    private final static boolean isEmptyEditText(EditText et) {
        if (et.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    /** Handles a login request to the server, and opens the menu if login has worked */
    private class LoginTask extends AsyncTask<String, Integer, Boolean>
    {
        private String login;

        public Boolean doInBackground(String... params){
            if(!(stateManager.isOnlineModeActivated()))
                return true;

            login = params[0];
            String password = params[1];
            return getConnection().login(login, password);
        }

        public void onPostExecute(Boolean loggedIn){
            if(loggedIn){
                stateManager.notifyConnectionOf(login);
                goToMainMenu();
            }
            else {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.start_login_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void goToMainMenu(){
        Intent myProfileActivity = new Intent(LoginActivity.this, MyProfileActivity.class);
        startActivity(myProfileActivity);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //To keep synchronization with connection mode on checkbox widget
        if(!stateManager.isOnlineModeActivated()){
            offlineMode.setChecked(true);
        }
    }
}
