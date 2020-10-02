package com.pact41.lastnight.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.model.LastNightStateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Robin on 18/01/2017.
 */

public class FriendlistActivity extends MenuActivity{

    private ListView friendlist;
    private String[] friends;
    private Button add;

    private ArrayList<FriendItem> list_friends;

    public static String FRIEND_NAME_EXTRA = "com.pact41.lastnight.intent.extra.FRIEND_NAME_EXTRA";

    private class addTask extends AsyncTask<String,Integer, Boolean>{

        private String userToAdd;

        public Boolean doInBackground(String... params){
            String myId=params[0];
            userToAdd = params[1];
            return getConnection().addFriend(myId, userToAdd);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success)
            {
                String successMessage = getResources().getString(R.string.friendlist_add_success, userToAdd);
                Toast.makeText(FriendlistActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
            else
                Toast.makeText(FriendlistActivity.this, R.string.friendlist_add_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private class getTask extends AsyncTask<String, Integer, String[]>{
        public String[] doInBackground(String... params){
            String myId=params[0];
            friends = getConnection().getFriends(myId);
            return friends;
        }
        public void onPostExecute(String[] friends){
            new showTask().execute(friends);
        }
    }
    private class showTask extends AsyncTask<String,Integer, FriendAdapter>{
        public FriendAdapter doInBackground(String[] friends){
            list_friends=new ArrayList<>();
            FriendItem item;

            if(friends == null) return null;

            for (String friend: friends){
                item=new FriendItem(getConnection().getUserFstNme(friend) +" "+getConnection().getUserLstNme(friend), "avaibility", "current_party", getConnection().getProfilePhoto(friend) );
                list_friends.add(item);
            }

            FriendAdapter friendlistadapter = new FriendAdapter(FriendlistActivity.this, list_friends);
            
            return friendlistadapter;
        }
        public void onPostExecute(FriendAdapter friendlistadapter){
            if(friendlistadapter == null)
                new TextView(FriendlistActivity.this).setText("Ajoutez des amis!");
            friendlist.setAdapter(friendlistadapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendlist);

        friendlist=(ListView) findViewById(R.id.friendlist);
        add=(Button) findViewById(R.id.friendlist_add);

        LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();
        final String currentUser=stateManager.getCurrentUser();



        //Permet d'accéder au profil d'un ami
        friendlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent friendprofile=new Intent(FriendlistActivity.this,MyProfileActivity.class);
                String friendName = friends[i];
                friendprofile.putExtra(FRIEND_NAME_EXTRA, friendName);
                startActivity(friendprofile);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(FriendlistActivity.this);
                alert.setTitle(R.string.friendlist_add_title);
                final EditText friendId=new EditText(FriendlistActivity.this);
                alert.setView(friendId);
                alert.setNeutralButton(R.string.friendlist_add_validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //vérifier que l'ID existe bien
                        new addTask().execute(currentUser,friendId.getText().toString().trim());
                        dialogInterface.cancel();
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    protected int getId(){
        return R.id.menu_item_friends;
    }

    protected void onResume(){
        super.onResume();
        LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();
        final String currentUser=stateManager.getCurrentUser();
        new getTask().execute(currentUser);
    }

    private class FriendItem{
        private String name;
        private String avaibility;
        private String current_party;
        private Bitmap bmp;

        public FriendItem(String name, String avaibility, String current_party, Bitmap bmp){
            this.name=name;
            this.avaibility=avaibility;
            this.current_party=current_party;
            this.bmp=bmp;
        }
    }

    private class FriendAdapter extends ArrayAdapter<FriendItem>{
        private Context context;
        private List<FriendItem> friends;

        public FriendAdapter(Context context, List<FriendItem> friends){
            super(context,0,friends);
            this.context=context;
            this.friends=friends;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row=convertView;
            if (row==null){
                LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row=inflater.inflate(R.layout.friendprofile,parent, false);
            }
            final FriendItem thisItem=getItem(position);
            final TextView name= (TextView) row.findViewById(R.id.friend_name);
            final TextView avaibility = (TextView) row.findViewById(R.id.friend_online_unavailable);
            final TextView current_party = (TextView) row.findViewById(R.id.friend_current_party);
            final ImageView picture = (ImageView) row.findViewById(R.id.friend_pp);
            name.setText(thisItem.name);
            avaibility.setText(thisItem.avaibility);
            current_party.setText(thisItem.current_party);
            if(thisItem.bmp != null)
                picture.setImageBitmap(thisItem.bmp);
            return row;

        }
    }
}
