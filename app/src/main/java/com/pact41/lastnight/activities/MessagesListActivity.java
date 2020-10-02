package com.pact41.lastnight.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.pact41.lastnight.R;

import java.util.ArrayList;

public class MessagesListActivity extends AppCompatActivity {

    //Messages list components
    private ListView messages;
    private ArrayList<String> messagesList;
    private ArrayAdapter<String> messagesListAdapter;

    private Button close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_list);

        //Getting messages list widgets ID
        messages = (ListView)findViewById(R.id.messages_list);
        messages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//To be completed
            }
        });

        //Filling messages list with the messages given by the parent activity
        messagesList = getIntent().getStringArrayListExtra(CurrentPartyActivity.DISPLAY_MESSAGES_EXTRA);

        //Creating adapter and setting to the messages ListView
        messagesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messagesList);
        messages.setAdapter(messagesListAdapter);

        close = (Button)findViewById(R.id.messages_list_close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
