package com.pact41.lastnight.client_server;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.activities.NetworkActivity;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class ConnectionTestActivity extends NetworkActivity {

    private EditText request;
    private Button send, disconnect;
    private ImageView loadedImage;

    private MyClient client;

    //Test constants, fill values with what you want to put
    final String goodUsername = "testUsername";
    final String goodPassword = "testPassword";
    final String validLastName = "testLastName";
    final String validFirstName = "testFirstName";
    final String validEmail = "test@pact.fr";

    final String newPassword = "testNewPassword";
    final String newFirstName = "testNewFirstName";
    final String newLastName = "testNewLastName";
    final String newEmail = "test.new.email@pact.fr";

    final String eventName = "testEventName";
    final String eventName2 = "FUPS";
    final String eventPlace = "testEventPlace";
    final int eventPrice = 0;
    final String eventDate = "01/01/2020-00:00-00:00";

    final String newEventName = "testNewEvent3";
    final String newEventPlace = "testNewEventPlace";
    final int newEventPrice = 1;
    final String newEventHour = "testNewEventHour";
    final String newEventCreator = "testNewEventCreator";

    final String friendUserName = "testFriendUserName";
    final String friendUserName2 = "testFriendUserName2";

    final String[] pollOptions = {"testNewEventName", "testPollTitle", "OptionEx1", "OptionEx2", "OptionEx3"};

    final String message1ToEvent = "I sent a first message.";
    final String message2ToEvent = "I sent a second message.";
    final String message3ToEvent = "I sent a third message";

    final String rankingUser1 = "testRankingUser1";
    final String rankingUser2 = "testRankingUser2";
    final String rankingUser3 = "testRankingUser3";

    Bitmap testProfilePicture, testEventPicture1, testEventPicture2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_test);

        testProfilePicture = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo);
        testEventPicture1 = BitmapFactory.decodeResource(getResources(), R.drawable.default_party_picture);
        testEventPicture2 = BitmapFactory.decodeResource(getResources(), android.R.drawable.button_onoff_indicator_off);

        request = (EditText)findViewById(R.id.connection_test_request);
        send = (Button)findViewById(R.id.connection_test_send);
        disconnect = (Button)findViewById(R.id.connection_test_disconnect);
        loadedImage = (ImageView)findViewById(R.id.connection_test_image_input);


        //Client is stored into the app state manager defined into application context
        client = getConnection();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch(request.getText().toString()) {
                            case "login":
                                testLogin();
                                break;
                            case "addUtilisateur":
                                testSubscription();
                                break;
                            case "changeUtilisateurInfo":
                                testChangeUtilisateurInfo();
                                break;
                            case "createEvent":
                                testCreateEvent();
                                break;
                            case "changeEvent":
                                testChangeEvent();
                                break;
                            case "isRegistered":
                                testIsRegistered();
                                break;
                            case "addAdminToEvent":
                                testAddAdminToEvent();
                                break;
                            case "isAdmin":
                                testIsAdmin();
                                break;
                            case "joinEvent":
                                testJoinEvent();
                                break;
                            case "addFriend" :
                                testAddFriend();
                                break;
                            case "deleteFriend" :
                                testDeleteFriend();
                                break;
                            case "sendSondage" :
                                testNewPoll();
                                break;
                            case "sendMessage" :
                                testSendMessageToEvent();
                                break;
                            case "sendProfilePhoto" :
                                testProfilePhoto();
                                break;
                            case "sendPartyPhoto" :
                                testPartyPhotos();
                                break;
                            case "sendScore":
                                testSendScoreToEvent();
                                break;
                            case "quitEvent" :
                                testQuitEvent();
                                break;
                        }
                    }
                }).start();
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.quit();
            }
        });
    }

    private String status(boolean result)
    {
        if(result)
            return "Success";
        return "Failure";
    }

    /** Testing login client method */
    private void testLogin(){
        String userName = "testWrongUsername";
        String passwd = goodPassword;
        Log.d("ConnectionTest", "Starting connection test\n\n");

        Log.d("ConnectionTest", "Tested account : \n" +
                "Username : " + userName + "\n" +
                "Password : " + passwd);
        Log.d("ConnectionTest", "First attempt of connection with username \"" + userName + "\", " +
                "password \"" + passwd + "\" : " + status(client.login(userName, passwd)));

        userName = goodUsername;
        passwd = "testWrongPassword";
        Log.d("ConnectionTest", "Third attempt of connection with username \"" + userName + "\", " +
                "password \"" + passwd + "\" : " + status(client.login(userName, passwd)));
        userName = "testWrongUsername";
        passwd = "testWrongPassword";
        Log.d("ConnectionTest", "Fourth attempt of connection with username \"" + userName + "\", " +
                "password \"" + passwd + "\" : " + status(client.login(userName, passwd)));
        userName = goodUsername;
        passwd = goodPassword;
        Log.d("ConnectionTest", "Fourth attempt of connection with username \"" + userName + "\", " +
                "password \"" + passwd + "\" : " + status(client.login(userName, passwd)));
        Log.d("ConnectionTest", "Connection test finished. Evaluate the result.");
    }

    /** Testing adding a user to the LastNight database */
    private void testSubscription(){

        String myId, firstName, lastName, email, myPassWord = null;
        Log.d("ConnectionTest", "Starting subscription test\n\n");

        myId = goodUsername;
        myPassWord = goodPassword;
        firstName = validFirstName;
        lastName = validLastName;
        email = validEmail;
        Log.d("ConnectionTest", "First attempt : " + "\n" +
                "Username : " + myId + "\n" +
                "Password : " + myPassWord + "\n" +
                "First name : " + firstName + "\n" +
                "Last name : " + lastName + "\n" +
                "e-mail : " + email + "\n" +
        "Result : " + status(client.addUtilisateur(myId, firstName, lastName, email, myPassWord)));

        Log.d("ConnectionTest", "User " + myId + " created on the database with : " + "\n" +
                "Password : " + client.getUserPssWd(myId) + "\n" +
                "First name : " + client.getUserFstNme(myId) + "\n" +
                "Last name : " + client.getUserLstNme(myId) + "\n" +
                "e-mail : " + client.getUserEmail(myId) + "\n");
        Log.d("ConnectionTest", "Registering test finished. Evaluate the result.");
    }

    /** Testing user registration checking in the database */
    private void testIsRegistered()
    {
        String userName = "unidentifiedUserName";
        Log.d("ConnectionTest", "Starting subscription verification test \n\n");

        Log.d("ConnectionTest", "First attempt of identifying user \"" + userName + "\" : " + "\n" +
                status(client.isRegistered(userName)));

        userName = this.goodUsername;
        Log.d("ConnectionTest", "Second attempt of identifying user \"" + userName + "\" : " + "\n" +
                status(client.isRegistered(userName)));

        Log.d("ConnectionTest", "Subscription verification test finished. Evaluate the result.");
    }

    /** Testing changing a user profile in the database */
    private void testChangeUtilisateurInfo()
    {
        String myId = goodUsername;
        String newFirstName = this.newFirstName;
        String newLastName = this.newLastName;
        String newPassword = this.newPassword;
        String newEmail = this.newEmail;

        Log.d("ConnectionTest", "Starting user account modification test\n\n");

        Log.d("ConnectionTest", "First attempt of modifying the account \"" + myId + "\" : " + "\n" +
                "Password : " + newPassword + "\n" +
                "First name : " + newFirstName + "\n" +
                "Last name : " + newLastName + "\n" +
                "e-mail : " + newEmail + "\n" +
                "Result : " + status(client.changeUtilisateurInfo(myId, newFirstName, newLastName, newEmail, newPassword)));

        Log.d("ConnectionTest", "User " + myId + " now identified on the database as : " + "\n" +
                "Password : " + client.getUserPssWd(myId) + "\n" +
                "First name : " + client.getUserFstNme(myId) + "\n" +
                "Last name : " + client.getUserLstNme(myId) + "\n" +
                "e-mail : " + client.getUserEmail(myId) + "\n");

        Log.d("ConnectionTest", "Account modification test finished. Evaluate the result.");
    }

    /** Testing creating an event in the database */
    private void testCreateEvent()
    {
        String eventName = this.newEventName;
        String eventPlace = this.eventPlace;
        int eventPrice = this.eventPrice;
        String eventDate = this.eventDate;
        String creator = this.goodUsername;

        Log.d("ConnectionTest", "Starting creating event test \n\n");

        Log.d("ConnectionTest", "First attempt of creating the event \"" + eventName + "\" : " + "\n" +
                "Place : " + eventPlace + "\n" +
                "Price : " + eventPrice + "\n" +
                "Hour : " + eventDate + "\n" +
                "Creator : " + creator + "\n"+
                "Result : " + status(client.createEvent(eventName, eventPlace, eventPrice, eventDate, creator)));

        showEvent(eventName);

        Log.d("ConnectionTest", "Event creation test finished. Evaluate the result.");
    }

    /** Prints the event information as stored in the database */
    private void showEvent(String eventName){
        Log.d("ConnectionTest", "Event \"" + eventName + "\" now identified on the database as : " + "\n" +
                "Place : " + client.getEventPlace(eventName) + "\n" +
                "Price : " + client.getEventPrice(eventName) + "\n" +
                "Hour : " + client.getEventHour(eventName) + "\n");
    }

    /** Testing to change information of an event in the database */
    private void testChangeEvent()
    {
        String newEventName = this.newEventName;
        String newEventPlace = this.newEventPlace;
        int newEventPrice = this.newEventPrice;
        String newEventHour = this.newEventHour;
        String newEventCreator = this.newEventCreator;

        Log.d("ConnectionTest", "Starting modifying event test \n\n");

        Log.d("ConnectionTest", "First attempt of modifying the event \"" + eventName + "\" : " + "\n" +
                "New place : " + newEventPlace + "\n" +
                "New Price : " + newEventPrice + "\n" +
                "New Hour : " + newEventHour + "\n" +
                "New creator : " + newEventCreator + "\n" +
                "Result : " + status(client.changeEvent(newEventName, newEventPlace, newEventPrice, newEventHour, newEventCreator)));

        showEvent(newEventName);

        Log.d("ConnectionTest", "Event modification test finished. Evaluate the result.");
    }

    /** Testing adding an admin to an event in the database */
    private void testAddAdminToEvent()
    {
        String adminToAdd = goodUsername;
        String event = eventName;
        Log.d("ConnectionTest", "Starting verification test of adding admin to an event \n\n");

        Log.d("ConnectionTest", "First attempt of adding user \"" + adminToAdd + "\" to the party " +
                "named " + "\"" + event + "\" :\n" + status(client.addAdminToEvent(adminToAdd, event)));
        Log.d("ConnectionTest", "Testing user \"" + adminToAdd + "\" status in the event \""+event+"\" : " +
                client.isAdmin(adminToAdd, event));
        Log.d("ConnectionTest", "Adding admin to event verification test finished. Evaluate the result.");
    }

    /** Testing the admin status checking method on an event in the database */
    private void testIsAdmin()
    {
        String badAdmin = "iAmNotAnAdmin";
        String goodAdmin = goodUsername;
        String testedEvent = eventName;
        Log.d("ConnectionTest", "Starting verification test of checking if the user is admin on an event \n\n");

        Log.d("ConnectionTest", "Checking if user \"" + badAdmin + "\" is registered as an admin on the event " +
        "\"" + testedEvent + "\" :\n" + status(client.isAdmin(badAdmin, testedEvent)));
        Log.d("ConnectionTest", "Checking if user \"" + goodAdmin + "\" is registered as an admin on the event " +
                "\"" + testedEvent + "\" :\n" + status(client.isAdmin(goodAdmin, testedEvent)));
        Log.d("ConnectionTest", "Admin status verification on an event test finished. Evaluate the result.");
    }

    /** Testing a user request to sign in into a created event in the database */
    private void testJoinEvent()
    {
        String connectedUser = goodUsername;
        String eventToJoin = eventName;
        String nonExistingEvent = "falseEvent";
        Log.d("ConnectionTest", "Starting verification test of signing into an event \n\n");

        Log.d("ConnectionTest", "Trying to join the event \"" + nonExistingEvent + "\" as user " +
                "\"" + connectedUser + "\" :\n" + status(client.joinEvent(connectedUser, nonExistingEvent)));
        Log.d("ConnectionTest", "Trying to join the event \"" + eventToJoin + "\" as user " +
                "\"" + connectedUser + "\" :\n" + status(client.joinEvent(connectedUser, eventToJoin)));
        Log.d("ConnectionTest", "Displaying event ranking : ");
        for(String user : client.getScoreRanking(eventToJoin))
            Log.d("ConnectionTest", user);
        Log.d("ConnectionTest", "User sign in into a given event test finished. Evaluate the result.");
    }

    /** Testing a user request to leave the current event table */
    private void testQuitEvent()
    {
        String connectedUser = goodUsername;
        String eventToQuit = eventName;
        String nonExistingEvent = "falseEvent";
        Log.d("ConnectionTest", "Starting verification test of leaving an event \n\n");

        Log.d("ConnectionTest", "Trying to quit the event \"" + nonExistingEvent + "\" as user " +
                "\"" + connectedUser + "\" :\n" + status(client.quitEvent(connectedUser, nonExistingEvent)));
        Log.d("ConnectionTest", "Trying to quit the event \"" + eventToQuit + "\" as user " +
                "\"" + connectedUser + "\" :\n" + status(client.quitEvent(connectedUser, eventToQuit)));
        Log.d("ConnectionTest", "Displaying event ranking : ");
        for(String user : client.getScoreRanking(eventToQuit))
            Log.d("ConnectionTest", user);
        Log.d("ConnectionTest", "User leaving a given event test finished. Evaluate the result.");
    }

    /** Testing the "add friend" request from a given account */
    private void testAddFriend()
    {
        String connectedUser = goodUsername;
        String friendToAdd1 = friendUserName;
        String friendToAdd2 = friendUserName2;

        Log.d("ConnectionTest", "Starting verification test of adding friends to the account" +
                "\""+connectedUser+"\" \n\n");

        Log.d("ConnectionTest", "Trying to add friend \""+friendToAdd1+"\" " +
                "on the account "+"\""+connectedUser+"\" :"+
                status(client.addFriend(connectedUser, friendToAdd1))+"\n");
        Log.d("ConnectionTest", "Result of the isFriend method launched for "+
                "\""+friendToAdd1+"\" :"+
                status(client.isFriend(connectedUser, friendToAdd1)));

        Log.d("ConnectionTest", "Trying to add friend \""+friendToAdd2+"\" " +
                "on the account "+"\""+connectedUser+"\" :"+
                status(client.addFriend(connectedUser, friendToAdd2))+"\n");
        Log.d("ConnectionTest", "Result of the isFriend method launched for "+
                "\""+friendToAdd2+"\" :"+
                status(client.isFriend(connectedUser, friendToAdd2)));
        Log.d("ConnectionTest", "Listing friends on the account \""+connectedUser+"\" :");
        String[] friends = client.getFriends(connectedUser);
        for(String friend : friends)
            Log.d("ConnectionTest", friend);
        Log.d("ConnectionTest", "Adding friends to an account : test finished. Evaluate the result.");
    }

    /** Testing the "deleteFriend" method for a given account */
    private void testDeleteFriend()
    {
        String connectedUser = goodUsername;
        String friendToDelete = friendUserName;
        String friendToDelete2 = friendUserName2;

        Log.d("ConnectionTest", "Starting verification test of deleting friends from the account" +
                "\""+connectedUser+"\" \n\n");

        Log.d("ConnectionTest", "Trying to delete friend \""+friendToDelete+"\" " +
                "on the account "+"\""+connectedUser+"\" :"+
                status(client.deleteFriend(connectedUser, friendToDelete))+"\n");
        Log.d("ConnectionTest", "Result of the isFriend method launched for "+
                "\""+friendToDelete+"\" :"+
                status(client.isFriend(connectedUser, friendToDelete)));

        Log.d("ConnectionTest", "Trying to delete friend \""+friendToDelete2+"\" " +
                "on the account "+"\""+connectedUser+"\" :"+
                status(client.deleteFriend(connectedUser, friendToDelete2))+"\n");
        Log.d("ConnectionTest", "Result of the isFriend method launched for "+
                "\""+friendToDelete2+"\" :"+
                status(client.isFriend(connectedUser, friendToDelete2)));
        Log.d("ConnectionTest", "Listing friends on the account \""+connectedUser+"\" :");
        String[] friends = client.getFriends(connectedUser);
        for(String friend : friends)
            Log.d("ConnectionTest", friend);
        Log.d("ConnectionTest", "Deleting friends from the account" + "\""+connectedUser+"\""+
                " : test finished. Evaluate the result.");
    }

    /** Test of creating a poll from the user current account, on a defined event */
    private void testNewPoll()
    {
        String[] pollOptionsToSend = pollOptions;
        String testedEvent = pollOptionsToSend[0];
        int vote1Index = 0;
        int vote2Index = 2;

        Log.d("ConnectionTest", "Starting verification test of creating a poll into an event \n\n");
        Log.d("ConnectionTest", "Trying to add following options to the event "+"\""+pollOptionsToSend[0]+"\" :");
        for(String option : pollOptionsToSend)
            Log.d("ConnectionTest", "\""+option+"\"");
        Log.d("ConnectionTest", "Result : "+status(client.createSondage(pollOptionsToSend)));
        Log.d("ConnectionTest", "Displaying tested poll options : ");
        String[] sondageInfo = client.getSondageChoix(testedEvent);
        int[] votes = client.getSondageVote(testedEvent);
        Log.d("ConnectionTest", "Title : " + sondageInfo[0]);
        for(int i=1; i < sondageInfo.length; i++)
            Log.d("ConnectionTest", "Option "+i+": "+sondageInfo[i]+" ("+votes[i-1]+" votes)");

        Log.d("ConnectionTest", "Voting for option number "+vote1Index+" ("+sondageInfo[vote1Index+1]+ ") : "+
                status(client.voteSondage(testedEvent, sondageInfo[vote1Index+1])));
        Log.d("ConnectionTest", "Displaying tested poll options : ");
        votes = client.getSondageVote(testedEvent);
        Log.d("ConnectionTest", "Title : " + sondageInfo[0]);
        for(int i=1; i < sondageInfo.length; i++)
            Log.d("ConnectionTest", "Option "+i+": "+sondageInfo[i]+" ("+votes[i-1]+" votes)");

        Log.d("ConnectionTest", "Voting for option number "+vote2Index+" ("+sondageInfo[vote2Index+1]+ ": "+
                status(client.voteSondage(testedEvent, sondageInfo[vote2Index+1])));
        Log.d("ConnectionTest", "Displaying tested poll options : ");
        votes = client.getSondageVote(testedEvent);
        Log.d("ConnectionTest", "Title : " + sondageInfo[0]);
        for(int i=1; i < sondageInfo.length; i++)
            Log.d("ConnectionTest", "Option "+i+": "+sondageInfo[i]+" ("+votes[i-1]+" votes)");

        Log.d("ConnectionTest", "Creating a poll into an event : test finished. Evaluate the result.");
    }

    private void testSendMessageToEvent() {
        String testedEvent = newEventName;
        String message1 = message1ToEvent;
        String message2 = message2ToEvent;
        String message3 = message3ToEvent;

        Log.d("ConnectionTest", "Starting verification test of sending 3 messages into the event \""+testedEvent+"\" \n\n");
        Log.d("ConnectionTest", "First message : "+message1+". Result : "+status(getConnection().addMessage(testedEvent, message1)));
        Log.d("ConnectionTest", "First message : "+message2+". Result : "+status(getConnection().addMessage(testedEvent, message2)));
        Log.d("ConnectionTest", "First message : "+message3+". Result : "+status(getConnection().addMessage(testedEvent, message3)));
        Log.d("ConnectionTest", "Listing messages of the event \""+testedEvent+"\" :");
        String[] eventMessages = getConnection().getAdminMessages(testedEvent);
        for(String message : eventMessages)
            Log.d("ConnectionTest", message);
        Log.d("ConnectionTest", "Test of adding messages to the event \""+testedEvent+"\" finished. Evaluate the result.");
    }

    private void testProfilePhoto() {
        String testedAccount = goodUsername;
        Bitmap testedPicture = testProfilePicture;

        Log.d("ConnectionTest", "Starting verification test of adding a profile picture to the account \""+testedAccount+"\" \n\n");
        Log.d("ConnectionTest", status(client.addProfilePhoto(testedAccount, testedPicture)));
        Bitmap result = client.getProfilePhoto(testedAccount);
        loadedImage.setImageBitmap(result);
        Log.d("ConnectionTest", "Test of adding profile picture to the account \""+testedAccount+"\" finished. Evaluate the result : " +
                "image must be displayed on the mobile screen.");
    }

    private void testPartyPhotos() {
        String testedEvent = eventName;
        Bitmap photo1 = testEventPicture1;
        Bitmap photo2 = testEventPicture2;

        Log.d("ConnectionTest", "Starting verification test of adding 2 photos on the event \""+testedEvent+"\" \n\n");
        Log.d("ConnectionTest", "1st photo : "+status(client.addPartyPhoto(testedEvent, photo1)));
        Log.d("ConnectionTest", "2nd photo : "+status(client.addPartyPhoto(testedEvent, photo2)));
        Bitmap photo1Result = client.getPartyPhoto(testedEvent, String.valueOf(0));
        Bitmap photo2Result = client.getPartyPhoto(testedEvent, String.valueOf(1));
        loadedImage.setImageBitmap(photo1Result);
        Log.d("ConnectionTest", "1st image into the event table should currently be displayed on the screen.");
        try {sleep(5000);}
        catch (InterruptedException e){e.printStackTrace();}
        loadedImage.setImageBitmap(photo2Result);
        Log.d("ConnectionTest", "2nd image into the event table should currently be displayed on the screen.");
        Log.d("ConnectionTest", "Test of sending pictures to the event \""+testedEvent+"\" finished. Evaluate the result.");
    }

    private void testSendScoreToEvent() {
        String user1 = rankingUser1;
        String user2 = rankingUser2;
        String user3 = rankingUser3;
        String testedEvent = eventName;

        Log.d("ConnectionTest", "User \""+user1+"\" joins the event : "+status(client.joinEvent(user1, testedEvent)));
        Log.d("ConnectionTest", "User \""+user2+"\" joins the event : "+status(client.joinEvent(user2, testedEvent)));
        Log.d("ConnectionTest", "User \""+user3+"\" joins the event : "+status(client.joinEvent(user3, testedEvent)));

        Log.d("ConnectionTest", "Starting verification test of sending users " +
                "\""+user1+"\""+
                "\""+user2+"\""+
                "\""+user3+"\""+
                " scores on the event \""+testedEvent+"\" \n\n");

        double score1 = 5;
        Log.d("ConnectionTest", user1+"'s score : " + score1);
        Log.d("ConnectionTest", "Sending request result : "+status(client.sendScoreToEvent(testedEvent, user1, score1)));

        double score2 = 15;
        Log.d("ConnectionTest", user2+"'s score : " + score2);
        Log.d("ConnectionTest", "Sending request result : "+status(client.sendScoreToEvent(testedEvent, user2, score2)));

        double score3 = 10;
        Log.d("ConnectionTest", user3+"'s score : " + score3);
        Log.d("ConnectionTest", "Sending request result : "+status(client.sendScoreToEvent(testedEvent, user3, score3)));

        Log.d("ConnectionTest", "Event "+testedEvent+" current user scores ranking :");
        String[] ranking = client.getScoreRanking(testedEvent);
        for(String user : ranking)
            Log.d("ConnectionTest", user+": "+client.getUserScore(testedEvent, user));

        Log.d("ConnectionTest", "Test of score ranking on the event \""+testedEvent+"\" finished. Evaluate the result.");
    }
}
