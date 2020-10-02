package com.pact41.lastnight.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LastPollActivity extends AppCompatActivity {

    //Last poll components, filled by server's information
    private TextView pollTitle;
    private ListView pollOptionsList;
    private ArrayList<HashMap<String, String>> pollOptions;
    private SimpleAdapter pollAdapter;
    private TextView userChoice;

    private Button validateButton;
    private String choice;
    private int lastItemSelected = -1;
    private int chosenOption = -1;

    //Choice confirmation dialog builder
    AlertDialog.Builder builder;

    public static String POLL_CHOICE_EXTRA = "com.pact41.lastnight.intent.extra.POLL_CHOICE_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_poll);

        //Getting last poll widgets ID
        pollTitle = (TextView)findViewById(R.id.poll_display_last_poll_title);
        pollOptionsList = (ListView)findViewById(R.id.poll_display_last_poll_options);
        pollOptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout itemLayout = (LinearLayout) view;
                lastItemSelected = position;

                //Gets text from the selected choice to build the dialog box
                TextView SelectedItemTextView = (TextView) itemLayout.findViewById(R.id.poll_option_text);
                choice = SelectedItemTextView.getText().toString();
                //Inserts the user's choice into a sentence using "%s" tag
                String message = getResources().getString(R.string.last_poll_dialog_message, "<b>" + choice + "</b>");
                builder.setMessage(Html.fromHtml(message));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //Creating adapter and setting to the poll ListView
        //Poll title
        String title = getIntent().getStringExtra(CurrentPartyActivity.POLL_TITLE_EXTRA);
        pollTitle.setText(Html.fromHtml("<b>"+title+"</b>"));

        //Poll options
        pollOptions = new ArrayList<HashMap<String, String>>();
        String[] pollOptionsText = getIntent().getStringArrayExtra(CurrentPartyActivity.POLL_OPTIONS_EXTRA);
        int[] pollOptionsPercentage = getIntent().getIntArrayExtra(CurrentPartyActivity.POLL_PERCENTAGES_EXTRA);
        for(int i=0; i < pollOptionsText.length; i++) { //Last poll options filling loop...
            HashMap<String, String> option = new HashMap<String, String>();
            option.put("option_text", pollOptionsText[i]);
            option.put("option_percentage", String.valueOf(pollOptionsPercentage[i])); //Random percentage generation, to be replaced with real values
            pollOptions.add(option);
        }
        pollAdapter = new SimpleAdapter(this, pollOptions, R.layout.poll_option, new String[] {"option_text", "option_percentage"},
                new int[]{R.id.poll_option_text, R.id.poll_option_percentage_bar});

        //Indicates the adapter how to set the progress bar into an element of the pollOptions ListView
        pollAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view.getId() == R.id.poll_option_percentage_bar){
                    int percentage = Integer.parseInt(textRepresentation);
                    ProgressBar optionProgressBar = (ProgressBar)view;
                    optionProgressBar.setProgress(percentage);
                    return true;
                }
                return false; //Poll titles are handled normally
            }
        });
        pollOptionsList.setAdapter(pollAdapter);

        //User's choice text view
        userChoice = (TextView)findViewById(R.id.poll_display_user_choice);

        //Validate button, comes back to CurrentPartyActivity
        validateButton = (Button)findViewById(R.id.poll_display_validate_button);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chosenOption == -1)
                    Toast.makeText(LastPollActivity.this, R.string.last_poll_no_option_selected, Toast.LENGTH_SHORT).show();
                else {
                    Intent pollResult = new Intent();
                    pollResult.putExtra(POLL_CHOICE_EXTRA, chosenOption);
                    setResult(RESULT_OK, pollResult);
                    finish();
                }
            }
        });

        //Initializing the dialog builder used to make the user validate his choice
        builder = new AlertDialog.Builder(LastPollActivity.this);
        builder.setTitle(getResources().getString(R.string.last_poll_dialog_title));
        // Add the buttons
        builder.setPositiveButton(R.string.last_poll_dialog_validate_choice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button : send response to server
                String text = getResources().getString(R.string.last_poll_choice, "<b>"+ choice + "</b>");
                userChoice.setText(Html.fromHtml(text));
                chosenOption = lastItemSelected;
            }
        });
        builder.setNegativeButton(R.string.last_poll_dialog_cancel_choice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog : nothing to do
            }
        });
    }
}
