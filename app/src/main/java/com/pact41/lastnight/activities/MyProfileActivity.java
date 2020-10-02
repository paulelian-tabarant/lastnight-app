package com.pact41.lastnight.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.model.LastNightStateManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Robin on 26/01/2017.
 */

public class MyProfileActivity extends MenuActivity {

    private ImageView picture;
    private TextView last_name;
    private TextView first_name;
    private ListView last_party;
    private Button bottomButton;
    private ArrayList<LinkedHashMap<String,String>> list_of_parties;

    private TextView userNameView, mailAddressView;

    private String friendUser;

    public final static int EDIT_PROFILE_REQUEST = 1;

    private class PictureTask extends AsyncTask<String, Integer, Bitmap>{
        public Bitmap doInBackground(String... params){
            String myId=params[0];
            return getConnection().getProfilePhoto(myId);
        }

        public void onPostExecute(Bitmap bmp){
            picture.setImageBitmap(bmp);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();
        String currentUser=stateManager.getCurrentUser();

        picture=(ImageView) findViewById(R.id.my_profile_picture);

        last_name=(TextView) findViewById(R.id.my_profile_last_name);

        first_name=(TextView) findViewById(R.id.my_profile_first_name);

        userNameView = (TextView)findViewById(R.id.my_profile_username);
        mailAddressView = (TextView)findViewById(R.id.my_profile_mail_address);

        friendUser = getIntent().getStringExtra(FriendlistActivity.FRIEND_NAME_EXTRA);
        bottomButton=(Button) findViewById(R.id.my_profile_modify);

        //Case when we load the activity for a friend user : the button's aim is to delete a friend
        if(friendUser != null) {
            setTitle("Profil de " + friendUser);

            //The bottom button text is set to "modify" by default, so we must refresh its content
            bottomButton.setText(R.string.my_profile_delete);
            bottomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DeleteFriendTask().execute(friendUser);
                }
            });
        }
        //Case when we load the activity for the current user : the button's aim is to edit his profile
        else
        {
            bottomButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent editProfile=new Intent(MyProfileActivity.this,EditProfileActivity.class);
                    startActivityForResult(editProfile,EDIT_PROFILE_REQUEST);
                }
            });
        }

        last_party=(ListView) findViewById(R.id.my_profile_lastparty);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==EDIT_PROFILE_REQUEST){
            if (resultCode==RESULT_OK)
                Toast.makeText(MyProfileActivity.this,R.string.edit_profile_confirmed,Toast.LENGTH_LONG).show();
            else
                Toast.makeText(MyProfileActivity.this,R.string.edit_profile_failed,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //We don't generate the taskbar for friends
        if(friendUser != null)
            return true;
        return super.onCreateOptionsMenu(menu);
    }

    protected void onResume(){
        super.onResume();
        LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();
        String currentUser=stateManager.getCurrentUser();

        if(friendUser == null) { //Case when the activity is launched for the current user and not one for his friends
            new NameTask().execute(currentUser);
            new PictureTask().execute(currentUser);
            new LastPartyInfosTask().execute();
            //Permet d'accéder au profil de la soiréee
            last_party.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //The item index matches with the files list which is saved into the activity
                    String partyName=(new ArrayList<String>(list_of_parties.get(0).values())).get(1);
                    startPartyProfileActivity(false, partyName );
                }
            });
        }
        else{
            TextView lastPartyTitle = (TextView)findViewById(R.id.my_profile_lastpartytext);
            ((RelativeLayout)lastPartyTitle.getParent()).removeView(lastPartyTitle);
            new NameTask().execute(friendUser);
            new PictureTask().execute(friendUser);
        }
    }

    private class NameTask extends AsyncTask<String, Integer, String[]>{
        public String[] doInBackground(String... params){
            String myId=params[0];
            String[] name= {myId,
                            getConnection().getUserFstNme(myId),
                            getConnection().getUserLstNme(myId),
                            getConnection().getUserEmail(myId)};
            return name;
        }

        @Override
        public void onPostExecute(String[] name){
            userNameView.setText(name[0]);
            first_name.setText(name[1]);
            last_name.setText(name[2]);
            mailAddressView.setText(name[3]);
        }
    }

    private class LastPartyInfosTask extends AsyncTask<String, Integer, String[]>{
        @Override
        protected String[] doInBackground(String... params) {
            File[] recapFiles = getExternalFilesDir(null).listFiles();

            //Getting files concerning the current user
            List<File> userRecapFiles = new ArrayList<>();
            for (File file : recapFiles){
                if(file.getName().contains(stateManager.getCurrentUser()))
                    userRecapFiles.add(file);
            }
            if(userRecapFiles.size() == 0) return null;

            File lastModifiedFile = userRecapFiles.get(0);
            for (int i = 1; i < userRecapFiles.size(); i++) {
                if (lastModifiedFile.lastModified() < userRecapFiles.get(i).lastModified()) {
                    lastModifiedFile = recapFiles[i];
                }
            }
            String lastEventName = lastModifiedFile.getName();
            lastEventName = lastEventName.substring(
                    lastEventName.indexOf("_") + 1,
                    lastEventName.indexOf("."));

            String[] lastEventInfos = new String[3];
            lastEventInfos[0] = lastEventName;
            lastEventInfos[1] = getConnection().getEventHour(lastEventName);
            lastEventInfos[2] = String.valueOf(getConnection().getEventPlace(lastEventName));
            return lastEventInfos;
        }

        @Override
        protected void onPostExecute(String[] lastEventInfos) {
            if(lastEventInfos == null) return;

            list_of_parties= new ArrayList<LinkedHashMap<String,String>>();
            LinkedHashMap<String,String>  map = new LinkedHashMap<String,String>();

            map.put("party_picture",String.valueOf(R.drawable.default_party_picture));
            map.put("party_name",lastEventInfos[0]);
            map.put("party_date",lastEventInfos[1]);
            map.put("party_place",lastEventInfos[2]);
            list_of_parties.add(map);

            SimpleAdapter historicadapter = new SimpleAdapter (MyProfileActivity.this, list_of_parties, R.layout.partyprofile,
                    new String[] {"party_picture", "party_name", "party_date", "party_place"}, new int[] {R.id.party_picture,R.id.party_name, R.id.party_date, R.id.party_place});

            last_party.setAdapter(historicadapter);
        }
    }

    /** Deletes the friend given into the first argument of the execute() command */
    private class DeleteFriendTask extends AsyncTask<String, Integer, Boolean> {

        private String addingSuccessMsg, addingFailureMsg;

        @Override
        protected Boolean doInBackground(String... friends) {
            String friendToDelete = friends[0];
            addingSuccessMsg = getResources().getString(R.string.my_profile_delete_confirmation, friendToDelete);
            addingFailureMsg = getResources().getString(R.string.my_profile_delete_failure, friendToDelete);

            return getConnection().deleteFriend(stateManager.getCurrentUser(), friendToDelete);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                Toast.makeText(MyProfileActivity.this, addingSuccessMsg, Toast.LENGTH_LONG).show();
                finish();
            }
            else
                Toast.makeText(MyProfileActivity.this, addingFailureMsg, Toast.LENGTH_SHORT).show();
        }
    }

    public int getId(){
        return R.id.menu_item_profile;
    }
}

