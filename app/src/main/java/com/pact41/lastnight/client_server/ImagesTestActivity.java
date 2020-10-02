package com.pact41.lastnight.client_server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.R;
import com.pact41.lastnight.activities.NetworkActivity;

import java.util.ArrayList;

public class ImagesTestActivity extends NetworkActivity {

    //Actualiser
    private ImageButton refreshButton;

    //Tests de la photo de profil
    private EditText usernameField;
    private ImageView profilePicReceived;
    private Button sendProfilePicButton, downloadProfilePicButton;
    private TextView sendProfilePicDetails;

    //Tests des photos de soirée
    private EditText partyNameField;
    private ImageView partyPicReceivedNb1, partyPicReceivedNb2, partyPicReceivedNb3;
    private Button sendPartyPicsButton, downloadPartyPicsButton;
    private TextView sendPartyPicDetails;

    private MyClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = getConnection();
        setContentView(R.layout.images_test);

        refreshButton = (ImageButton)findViewById(R.id.image_test_refresh_activity);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        //Profile picture widgets
        usernameField = (EditText)findViewById(R.id.image_test_profile_pic_username_field);
        profilePicReceived = (ImageView)findViewById(R.id.image_test_profile_pic_received);
        sendProfilePicButton = (Button)findViewById(R.id.image_test_send_profile_pic_button);
        downloadProfilePicButton = (Button)findViewById(R.id.image_test_download_profile_pic_button);
        sendProfilePicDetails = (TextView)findViewById(R.id.image_test_last_profile_pic_sent_details);

        sendProfilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString();
                if(username.matches(""))
                    return;
                new AddProfilePictureTask().execute(username);
            }
        });

        downloadProfilePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString();
                if(username.matches(""))
                    return;
                new GetProfilePictureTask().execute(username);
            }
        });

        //Party pictures widgets
        partyNameField = (EditText)findViewById(R.id.image_test_event_name_field);
        partyPicReceivedNb1 = (ImageView)findViewById(R.id.image_test_1st_image_received);
        partyPicReceivedNb2 = (ImageView)findViewById(R.id.image_test_2nd_image_received);
        partyPicReceivedNb3 = (ImageView)findViewById(R.id.image_test_3rd_image_received);
        sendPartyPicsButton = (Button)findViewById(R.id.image_test_send_party_pics_button);
        downloadPartyPicsButton = (Button)findViewById(R.id.image_test_download_party_pics_button);
        sendPartyPicDetails = (TextView)findViewById(R.id.image_test_party_photos_send_details);

        sendPartyPicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String partyName = partyNameField.getText().toString();
                if(partyName.matches(""))
                    return;

                new SendPartyPicsTask().execute(partyName);
            }
        });

        downloadPartyPicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String partyName = partyNameField.getText().toString();
                if(partyName.matches(""))
                    return;

                new GetPartyPicsTask().execute(partyName);
            }
        });
    }

    private String resultKey(boolean result){
        if(result)
            return "OK";
        else
            return "Echec";
    }

    private class AddProfilePictureTask extends AsyncTask<String, Integer, Boolean> {

        private String username;

        @Override
        protected Boolean doInBackground(String... params) {
            username = params[0];
            Bitmap profilePicture = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_picture);
            return client.addProfilePhoto(username, profilePicture);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            String result = resultKey(success);
            sendProfilePicDetails.setText("Ajout de la photo au compte \""+username+"\" : "+result);
        }
    }

    private class GetProfilePictureTask extends AsyncTask<String, Integer, Bitmap> {

        private String username;

        @Override
        protected Bitmap doInBackground(String... params) {
            username = params[0];
            return client.getProfilePhoto(username);
        }

        @Override
        protected void onPostExecute(Bitmap receivedPicture) {
            if(receivedPicture == null)
                sendProfilePicDetails.setText("Echec du téléchargement de la photo de profil du compte \""+
                        username+"\".");
            else {
                sendProfilePicDetails.setText("Photo de profil du compte \""+
                        username+"\" actuellement affichée sur l'écran.");
                profilePicReceived.setImageBitmap(receivedPicture);
            }
        }
    }

    private class SendPartyPicsTask extends AsyncTask<String, Integer, ArrayList<String>> {

        private String partyName;
        @Override
        protected ArrayList<String> doInBackground(String... params) {
            partyName = params[0];
            Bitmap image1 = BitmapFactory.decodeResource(getResources(), R.drawable.default_party_picture);
            Bitmap image2 = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo);
            Bitmap image3 = BitmapFactory.decodeResource(getResources(), R.drawable.quit_logo);
            String result1 = resultKey(client.addPartyPhoto(partyName, image1));
            String result2 = resultKey(client.addPartyPhoto(partyName, image2));
            String result3 = resultKey(client.addPartyPhoto(partyName, image3));
            ArrayList<String> results = new ArrayList<>();
            results.add(result1);
            results.add(result2);
            results.add(result3);
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<String> results) {
            String result1 = results.get(0);
            String result2 = results.get(1);
            String result3 = results.get(2);
            boolean globalResult = result1 == "OK" && result2 == "OK" && result3 == "OK";
            Toast.makeText(ImagesTestActivity.this, "Ajout des 3 photos à la soirée \""+partyName+"\" pris en compte. \n"+
                            "Image n°1 : "+result1+"\n"+
                            "Image n°2 : "+result2+"\n"+
                            "Image n°3 : "+result3+"\n",
                    Toast.LENGTH_LONG).show();
            sendPartyPicDetails.setText("Ajout des 3 photos à la soirée \""+partyName+"\" :"+resultKey(globalResult));
        }
    }

    private class GetPartyPicsTask extends AsyncTask<String, Integer, ArrayList<Bitmap>> {

        private String partyName;

        @Override
        protected ArrayList<Bitmap> doInBackground(String... params) {
            partyName = params[0];

            ArrayList<Bitmap> photos = new ArrayList<>();
            photos.add(client.getPartyPhoto(partyName, String.valueOf(1)));
            photos.add(client.getPartyPhoto(partyName, String.valueOf(2)));
            photos.add(client.getPartyPhoto(partyName, String.valueOf(3)));
            return photos;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> photos) {
            Bitmap image1 = photos.get(0);
            Bitmap image2 = photos.get(1);
            Bitmap image3 = photos.get(2);
            boolean success1 = image1 != null;
            boolean success2 = image2 != null;
            boolean success3 = image3 != null;

            if(success1)
                partyPicReceivedNb1.setImageBitmap(image1);
            else
                Toast.makeText(ImagesTestActivity.this, "Echec du téléchargement de la photo n°1.", Toast.LENGTH_SHORT).show();
            if(success2)
                partyPicReceivedNb2.setImageBitmap(image2);
            else
                Toast.makeText(ImagesTestActivity.this, "Echec du téléchargement de la photo n°2.", Toast.LENGTH_SHORT).show();
            if(success3)
                partyPicReceivedNb3.setImageBitmap(image3);
            else
                Toast.makeText(ImagesTestActivity.this, "Echec du téléchargement de la photo n°3.", Toast.LENGTH_SHORT).show();

            boolean globalResult = success1 && success2 && success3;

            sendPartyPicDetails.setText("Téléchargement des 3 premières photos de la soirée \""+partyName+"\" : "+
                    resultKey(globalResult));
        }
    }
}
