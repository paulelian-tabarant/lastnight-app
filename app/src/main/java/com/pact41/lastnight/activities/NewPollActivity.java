package com.pact41.lastnight.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.R;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Robin on 20/02/2017.
 */

public class NewPollActivity extends AppCompatActivity {
    private TextView title;
    private ListView pollOptionsList;
    private Button addOption;
    private ArrayList<HashMap<String, String>> option_list;
    private SimpleAdapter pollAdapter;
    private Button validate;

    public final static String POLL_OPTIONS_EXTRA = "com.pact41.lastnight.intent.extra.POLL_OPTIONS_EXTRA";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_poll);

        option_list= new ArrayList<HashMap<String,String>>();

        title=(TextView) findViewById(R.id.new_poll_title);
        pollOptionsList=(ListView)findViewById(R.id.new_poll_options);
        pollOptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder alert= new AlertDialog.Builder(NewPollActivity.this);
                alert.setTitle(R.string.new_poll_delete);
                alert.setNegativeButton(R.string.new_poll_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        dialogInterface.cancel();
                    }
                });
                alert.setPositiveButton(R.string.new_poll_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        option_list.remove(i);
                        pollAdapter=new SimpleAdapter(NewPollActivity.this, option_list,R.layout.poll_option, new String[] {"option_text"},new int[]{R.id.poll_option_text} );
                        pollOptionsList.setAdapter(pollAdapter);
                        dialogInterface.cancel();
                    }
                });
                alert.show();

                }
        });

        addOption=(Button) findViewById(R.id.new_poll_add);
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert= new AlertDialog.Builder(NewPollActivity.this);
                alert.setTitle(R.string.new_poll_new_option_title);
                final EditText optionText=new EditText(NewPollActivity.this);
                alert.setView(optionText);
                alert.setNeutralButton(R.string.new_poll_new_option_validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap<String,String> option = new HashMap<String, String>();
                        option.put("option_text",optionText.getText().toString());
                        option_list.add(option);
                        pollAdapter=new SimpleAdapter(NewPollActivity.this, option_list,R.layout.poll_option, new String[] {"option_text"},new int[]{R.id.poll_option_text} );
                        pollOptionsList.setAdapter(pollAdapter);
                        dialogInterface.cancel();
                    }
                });
                alert.show();
            }
        });

        validate=(Button) findViewById(R.id.new_poll_validate_button);
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If the poll doesn't have a title or has no option, we can't validate it
                if(title.getText().toString().equals("") || option_list.isEmpty())
                    Toast.makeText(NewPollActivity.this, R.string.new_poll_error, Toast.LENGTH_SHORT).show();
                else{
                    Intent pollOptionsResult = new Intent();
                    //Returns poll options given by the user, with the result intent
                    String[] pollOptions = new String[option_list.size() + 2];
                    //First we add the party name given by the parent activity
                    pollOptions[0] = getIntent().getStringExtra(CurrentPartyActivity.PARTY_NAME);
                    //Second we store the poll title
                    pollOptions[1] = title.getText().toString().replace(" ", "_");
                    //Then we add the edited options
                    for (int i = 0; i < option_list.size(); i++){
                        String optionText = option_list.get(i).get("option_text");
                        pollOptions[i+2] = optionText.replace(" ", "_");
                    }
                    pollOptionsResult.putExtra(POLL_OPTIONS_EXTRA, pollOptions);
                    setResult(RESULT_OK, pollOptionsResult);
                    finish();
                }
            }
        });
    }

}
