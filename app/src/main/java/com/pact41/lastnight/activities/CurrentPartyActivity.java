package com.pact41.lastnight.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.R;
import com.pact41.lastnight.services.SensorService;

import java.util.ArrayList;
import java.util.Arrays;

public class CurrentPartyActivity extends MenuActivity {
    private RelativeLayout currentPartyLayout;

    //Last message
    private ArrayList<String> messagesList;
    private TextView lastMessage;
    private Button newMessage;

    //Last poll
    private TextView lastPollTitleView;
    private LinearLayout lastPollBestOption;
    private TextView lastPollBestOptionText;
    private ProgressBar lastPollBestOptionNumber;
    private Button newPoll;
    private String lastPollTitle;
    private String[] lastPollOptions;
    private int[] lastPollPercentages;

    //Quit button
    private ImageButton quit;

    //Album button
    private ImageButton photo;

    //Sensor data capture service intent
    private Intent sensorService;

    public static int CREATE_POLL_REQUEST = 0;
    public static int ANSWER_POLL_REQUEST = 1;

    private final int PHOTO_RESULT=2;
    private final int GALLERY_RESULT=3;
    private Bitmap photo_taken;

    //Extra given to the activity "MessagesListActivity" to display messages
    public static String DISPLAY_MESSAGES_EXTRA = "com.pact41.lastnight.intent.extra.DISPLAY_MESSAGES_EXTRA";
    public static String POLL_TITLE_EXTRA = "com.pact41.lastnight.intent.extra.POLL_TITLE_EXTRA";
    public static String POLL_OPTIONS_EXTRA = "com.pact41.lastnight.intent.extra.POLL_CONTENT_EXTRA";
    public static String POLL_PERCENTAGES_EXTRA = "com.pact41.lastnight.intent.extra.POLL_PERCENTAGES_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPartyLayout = (RelativeLayout) RelativeLayout.inflate(this, R.layout.current_party, null);
        setTitle(stateManager.getCurrentPartyName());

        //Getting last message widget's ID
        messagesList = new ArrayList<>();
        lastMessage = (TextView) currentPartyLayout.findViewById(R.id.current_party_last_message);
        lastMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messagesListActivity = new Intent(CurrentPartyActivity.this, MessagesListActivity.class);
                //Gives the messages list to the activity which has to display them
                messagesListActivity.putStringArrayListExtra(DISPLAY_MESSAGES_EXTRA, messagesList);
                startActivity(messagesListActivity);
            }
        });

        newMessage = (Button)currentPartyLayout.findViewById(R.id.current_party_admin_send_msg);

        //Getting last poll widget's ID
        lastPollTitleView = (TextView) currentPartyLayout.findViewById(R.id.current_party_last_poll_title);
        lastPollBestOption = (LinearLayout) currentPartyLayout.findViewById(R.id.current_party_last_poll_best_option);
        lastPollBestOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Occurs when connection problem or no poll on the event
                if(lastPollOptions == null || lastPollPercentages == null)
                    return;

                Intent lastPollActivity = new Intent(CurrentPartyActivity.this, LastPollActivity.class);
                lastPollActivity.putExtra(POLL_TITLE_EXTRA, lastPollTitle);
                lastPollActivity.putExtra(POLL_OPTIONS_EXTRA, lastPollOptions);
                lastPollActivity.putExtra(POLL_PERCENTAGES_EXTRA, lastPollPercentages);
                startActivityForResult(lastPollActivity, ANSWER_POLL_REQUEST);
            }
        });
        lastPollBestOptionNumber = (ProgressBar)lastPollBestOption.findViewById(R.id.current_party_last_poll_best_option_percentage_bar);
        lastPollBestOptionText = (TextView)lastPollBestOption.findViewById(R.id.current_party_last_poll_best_option_text);

        newPoll = (Button) currentPartyLayout.findViewById(R.id.current_party_admin_send_poll);

        //Button which must be used to leave the current event
        quit = (ImageButton) currentPartyLayout.findViewById(R.id.current_party_quit_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateManager.notifyLeavingEvent();
                openCurrentPartyMenu();
                sensorService = new Intent(CurrentPartyActivity.this, SensorService.class);
                stopService(sensorService);
                sensorService.putExtra(PartyProfileActivity.TIME_REACHED_ACTION, true);
                AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                manager.cancel(PendingIntent.getService(CurrentPartyActivity.this, 0, sensorService, 0));
            }
        });

        final CharSequence[] picture_choice = {"Camera","Galerie"};

        //Button which must be used to access to the photo album
        photo = (ImageButton) currentPartyLayout.findViewById(R.id.current_party_photo);
        photo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                AlertDialog.Builder alert = new AlertDialog.Builder(CurrentPartyActivity.this);
                alert.setTitle("Comment choisir la photo?");
                alert.setSingleChoiceItems(picture_choice,-1, new
                        DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                if(picture_choice[which]=="Camera") {
                                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(cameraIntent,PHOTO_RESULT);
                                    dialog.dismiss();

                                }

                                else if (picture_choice[which]=="Galerie") {
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galleryIntent,GALLERY_RESULT);
                                    dialog.dismiss();
                                }
                            }
                        });
                alert.show();
            }


        });

        //Removes admin options from the layout if the user is not an admin on the event
        new CheckAdminStatusTask().execute();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(!stateManager.isPartyInProgress())
        {
            finish();
            openCurrentPartyMenu();
        }

        new GetMessagesTask().execute();
        new GetPollResultTask().execute();
    }

    @Override
    public int getId()
    {
        return R.id.menu_item_current_party;
    }

    /** Sets the layout content from the current user's status on the current event */
    private class CheckAdminStatusTask extends AsyncTask<String, Integer, Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            return getConnection().isAdmin(stateManager.getCurrentUser(), stateManager.getCurrentPartyName());
        }

        @Override
        protected void onPostExecute(Boolean isAdmin) {
            if(!isAdmin){
                //The user has no access to these options if he is not an admin
                ((LinearLayout) newMessage.getParent()).removeView(newMessage);
                ((LinearLayout) newPoll.getParent()).removeView(newPoll);
            }
            else {
                newPoll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent newPollActivity=new Intent(CurrentPartyActivity.this, NewPollActivity.class);
                        newPollActivity.putExtra(PARTY_NAME, stateManager.getCurrentPartyName());
                        startActivityForResult(newPollActivity, CREATE_POLL_REQUEST);
                    }
                });
                newMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert= new AlertDialog.Builder(CurrentPartyActivity.this);
                        alert.setTitle(R.string.current_party_new_message_title);
                        final EditText message = new EditText(CurrentPartyActivity.this);
                        message.setHeight(300);
                        message.setGravity(0);
                        alert.setView(message);
                        alert.setNeutralButton("OK", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog , int i){
                                new SendMessageTask().execute(message.getText().toString().trim());
                                dialog.cancel();
                            }
                        });
                        alert.show();
                    }
                });
            }
            setContentView(currentPartyLayout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CREATE_POLL_REQUEST){
            if(resultCode == RESULT_OK){
                String[] pollOptions = data.getStringArrayExtra(NewPollActivity.POLL_OPTIONS_EXTRA);
                new SendPollTask().execute(pollOptions);
            }
        }
        if(requestCode == ANSWER_POLL_REQUEST) {
            if(resultCode == RESULT_OK) {
                int choice = data.getIntExtra(LastPollActivity.POLL_CHOICE_EXTRA, -1);
                if(choice != -1)
                    new AnswerPollTask().execute(choice);
            }
        }
        if (requestCode == PHOTO_RESULT){
            if(resultCode == RESULT_OK){
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,GALLERY_RESULT);
            }
        }
        if (requestCode == GALLERY_RESULT){
            if(resultCode == RESULT_OK){
                Uri imageUri = data.getData();
                String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                Cursor cur = managedQuery(imageUri, orientationColumn, null, null, null);
                int orientation = -1;
                if (cur != null && cur.moveToFirst()) {
                    orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    photo_taken=rotatedBitmap;
                    new SendPhotoTask().execute(photo_taken);
                }
                catch (Exception e){
                    Toast toast=Toast.makeText(CurrentPartyActivity.this, R.string.current_party_photo_fail, Toast.LENGTH_LONG);
                    toast.show();
                }

            }

        }
    }

    /** Creates a poll into the event table in the database, thanks to the String table given as parameter */
    private class SendPollTask extends AsyncTask<String[], Integer, Boolean>{
        @Override
        protected Boolean doInBackground(String[]... params) {
            String[] pollOptions = params[0];
            return getConnection().createSondage(pollOptions);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success)
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_poll_created_confirmation, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_poll_not_created_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Loads poll title, options and vote percentages from the current event, if it exists */
    private class GetPollResultTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            String[] lastPollInfo = getConnection().getSondageChoix(stateManager.getCurrentPartyName());
            //A poll must contain at least a title + 1 option.
            if(lastPollInfo == null || lastPollInfo.length < 2)
                return null;

            //Spaces are stored as "_" on the server
            for(int i = 0; i < lastPollInfo.length; i++) {
                lastPollInfo[i] = lastPollInfo[i].replace("_", " ");
            }

            //Title is at index 0, options follow next
            lastPollTitle = lastPollInfo[0];
            lastPollOptions = Arrays.copyOfRange(lastPollInfo, 1, lastPollInfo.length);

            int[] pollVotes = getConnection().getSondageVote(stateManager.getCurrentPartyName());
            //pollVotes must contain the exact same number of fields because there's one vote number per option
            if ( (pollVotes == null) /*|| (pollVotes.length != lastPollOptions.length)*/ )
                return null;

            //Calculating total number of votes
            int sum = 0;
            for(int votes : pollVotes)
                sum+=votes;
            //Calculating votes percentage for each option and determining the most successful option
            lastPollPercentages = new int[pollVotes.length];
            Integer bestOptionIndex = 0;
            for(int i = 0; i < lastPollPercentages.length; i++) {
                lastPollPercentages[i] = (int) ((float) pollVotes[i] / sum * 100);
                if(lastPollPercentages[i] > lastPollPercentages[bestOptionIndex])
                    bestOptionIndex = i;
            }

            return bestOptionIndex;
        }

        @Override
        protected void onPostExecute(Integer bestOptionIndex) {
            if(bestOptionIndex == null)
                return;

            lastPollTitleView.setText(lastPollTitle);
            //Displaying the most successful option on the last poll
            lastPollBestOptionText.setText(lastPollOptions[bestOptionIndex]);
            lastPollBestOptionNumber.setProgress(lastPollPercentages[bestOptionIndex]);
        }
    }

    /** Sends a vote for an option of the current party poll from a given index */
    private class AnswerPollTask extends AsyncTask<Integer, Integer, Boolean> {

        private String optionText;

        @Override
        protected Boolean doInBackground(Integer... params) {
            int optionIndex = params[0];
            //Spaces are stored as "_" on the server
            optionText = lastPollOptions[optionIndex];
            return getConnection().voteSondage(stateManager.getCurrentPartyName(), optionText.replace(" ", "_"));
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                String successMsg = getResources().getString(R.string.current_party_vote_sent_confirmation, optionText);
                Toast.makeText(CurrentPartyActivity.this, successMsg, Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_vote_not_sent_error, Toast.LENGTH_SHORT).show();
        }
    }


    /** Gets the current event messages list, stores it into activity's attributes and refreshes the view */
    private class GetMessagesTask extends AsyncTask<String, Integer, String[]>{
        @Override
        protected String[] doInBackground(String... params) {
            String[] messagesList = null;
            messagesList = getConnection().getAdminMessages(stateManager.getCurrentPartyName());
            return messagesList;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result == null || result.length == 0)
                return;
            messagesList.clear();
            for(String message : result)
                messagesList.add(message);
            //Displaying the last message
            lastMessage.setText(messagesList.get(messagesList.size() - 1));
        }
    }

    /** Sends the message specified as first parameter to the current event.
     * Reserved to admin rights.
     */
    private class SendMessageTask extends AsyncTask<String, Integer, Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            String message = params[0];
            return getConnection().addMessage(stateManager.getCurrentPartyName(), message);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_message_sent_confirmation, Toast.LENGTH_SHORT).show();
                refresh();
            }
            else
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_message_not_sent_error, Toast.LENGTH_LONG).show();
        }
    }

    //Sends the photo to the data basis
    private class SendPhotoTask extends AsyncTask<Bitmap, Integer, Boolean>{
        @Override
        protected Boolean doInBackground(Bitmap... params){
            Bitmap bmp = params[0];
            return getConnection().addPartyPhoto(stateManager.getCurrentPartyName(), bmp);
        }

        @Override
        protected void onPostExecute (Boolean success){
            if(success){
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_photo_sent, Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(CurrentPartyActivity.this, R.string.current_party_photo_not_sent, Toast.LENGTH_SHORT).show();
        }
    }
}
