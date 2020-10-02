package com.pact41.lastnight.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.R;

import java.util.ArrayList;

/**
 * Created by Robin on 26/01/2017.
 */

public class OrganizeActivity extends MenuActivity {

    private ImageView picture;
    private EditText name, date, place, hours_begin, hours_end, price;
    private Button validate;
    private ListView adminsList;
    private ArrayList<String> adminsListData;
    private ArrayAdapter<String> adminsListAdapter;
    private Button addAdmin;
    private TextView adminField;
    private AlertDialog deleteAdminDialog;

    private final int PHOTO_RESULT=2;
    private final int GALLERY_RESULT=3;

    @Override
    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.organize);

        picture=(ImageView) findViewById(R.id.organize_picture);

        name=(EditText) findViewById(R.id.organize_name_input);

        date=(EditText) findViewById(R.id.organize_date_input);

        place=(EditText) findViewById(R.id.organize_place_edit);

        hours_begin=(EditText) findViewById(R.id.organize_hours_begin);

        hours_end=(EditText) findViewById(R.id.organize_hours_end);

        price=(EditText) findViewById(R.id.organize_price_input);

        validate=(Button) findViewById(R.id.organize_validate);
        validate.setOnClickListener(new View.OnClickListener() {
            //création de la soirée
            @Override
            public void onClick(View view) {
                new CreateEventTask().execute(
                        name.getText().toString().trim(),
                        place.getText().toString().trim(),
                        price.getText().toString().trim(),
                        date.getText().toString().trim(),
                        hours_begin.getText().toString().trim(),
                        hours_end.getText().toString().trim(),
                        stateManager.getCurrentUser()); //Creator of the event is of course the current user
            }
        });

        //Admins adding editor
        adminsList = (ListView) findViewById(R.id.organize_admins_list);
        addAdmin = (Button) findViewById(R.id.organize_add_admin);
        adminField = (EditText) findViewById(R.id.organize_admin_username);
        adminsListData = new ArrayList<String>();
        adminsListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, adminsListData);
        adminsList.setAdapter(adminsListAdapter);

        addAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adminsListData.add(adminField.getText().toString().trim());
                adminsListAdapter.notifyDataSetChanged();
            }
        });

        //Useful items to delete an admin from the given admins list
        adminsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrganizeActivity.this);
                builder .setMessage(R.string.organize_admin_delete_confirmation)
                        .setTitle(R.string.organize_admin_delete_confirmation_title)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adminsListData.remove(position);
                                adminsListAdapter.notifyDataSetChanged();
                                deleteAdminDialog.dismiss();
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAdminDialog.dismiss();
                            }
                        });
                deleteAdminDialog = builder.create();
                deleteAdminDialog.show();
            }
        });
    }
    

    @Override
    protected int getId(){
        return R.id.menu_item_organize;
    }

    /* Sends a request to create an event into the database, with the provided event information given in parameters */
    private class CreateEventTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            for(String field : params){
                if(field == null || field.equals(""))
                    return false;
            }

            //Getting event's information
            String name = params[0];
            String place = params[1];
            int price = Integer.valueOf(params[2]);
            String day = params[3];
            String startHour = params[4];
            String endHour = params[5];
            String creator = params[6];

            //Concatenating date's input fields
            String fullDate = day+"-"+startHour+"-"+endHour;
            boolean success =  getConnection().createEvent(name, place, price, fullDate, creator);
            if(success){
                for(String admin : adminsListData){
                    getConnection().addAdminToEvent(admin, name);
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Toast message;
            if(success) {
                message = Toast.makeText(OrganizeActivity.this, R.string.organize_validated, Toast.LENGTH_LONG);
                startPartyProfileActivity(true, name.getText().toString());
            }
            else
                message = Toast.makeText(OrganizeActivity.this, R.string.organize_error, Toast.LENGTH_LONG);
            message.show();
        }
    }
}
