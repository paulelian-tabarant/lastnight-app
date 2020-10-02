package com.pact41.lastnight.client_server;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.activities.NetworkActivity;
import com.pact41.lastnight.services.SensorService;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class MyClient implements AndroidClientServerInterface {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
    private NetworkActivity boundActivity;
    private boolean connected = false;

    private int connect(){
        try {
            socket = new Socket("79.137.85.43",1999);
            this.in = new ObjectInputStream(socket.getInputStream());
            Log.d("ConnectionTest", "connecté au port : 1999");
            int port = (int) in.readObject();
            Log.d("ConnectionTest", "trouvé un port libre : "+Integer.toString(port) +"   (-1 => no available port)");
            in.close();
            socket.close();
            return port;
        }catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("ConnectionTest", "failed to connect to port : 1999");
        }
        return -1;
    }

    public void init(NetworkActivity activity){
        this.boundActivity = activity;

        //Handling connection availability
        ConnectivityManager cm = (ConnectivityManager)boundActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork == null || !activeNetwork.isConnectedOrConnecting()){
            boundActivity.notifyNetworkUnavailibility();
            connected = false;
            return;
        }

        //Establishing socket connection
        if(!connected) {
            boundActivity.notifyConnectionAttempt();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int n = -1;
                    int attempts = 0;
                    while (n == -1) {
                        n = connect();
                        attempts++;
                        if(attempts > 100){
                            boundActivity.notifyConnectionResult(false);
                            connected = false;
                            return;
                        }
                    }
                    try {
                        socket = new Socket("79.137.85.43", n);
                        out = new ObjectOutputStream(socket.getOutputStream());
                        in = new ObjectInputStream(socket.getInputStream());
                        Log.d("ConnectionTest", "connecté au port : " + n);
                        connected = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("ConnectionTest", "failed to connect to port : " + n);
                        connected = false;
                        boundActivity.notifyConnectionResult(false);
                    }
                    boundActivity.notifyConnectionResult(true);
                }
            }).start();
        }
    }

    //Init method for sensor services (specific client as not linked to the app context)
    public void init(final SensorService sensorService){
        if(!connected) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int n = -1;
                    int attempts = 0;
                    while (n == -1) {
                        n = connect();
                        attempts++;
                        if (attempts > 100) {
                            return;
                        }
                    }
                    try {
                        socket = new Socket("79.137.85.43", n);
                        out = new ObjectOutputStream(socket.getOutputStream());
                        in = new ObjectInputStream(socket.getInputStream());
                        Log.d("ConnectionTest", "Service connecté au port : " + n);
                        connected = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("ConnectionTest", "Service failed to connect to port : " + n);
                        connected = false;
                    }
                }
            }).start();
        }
    }

    public boolean quit(){
        try {
            out.flush();
            out.writeObject("quit");
            this.socket.close();
            connected = false;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login(String myId, String myPassWord) {
        try {
            out.flush();
            out.writeObject("login "+myId+" "+myPassWord);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during login : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean addUtilisateur(String myId, String firstName, String lastName,
                                  String email, String myPassWord) {
        try {
            out.flush();
            out.writeObject("addUtilisateur "+myId+" "+firstName+" "+lastName+" "+email+" "+myPassWord);
            return (boolean) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during addUtilisateur : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean changeUtilisateurInfo(String myId, String firstName, String lastName,
                                         String email, String myPassWord) {
        try {
            out.flush();
            out.writeObject("changeUtilisateur "+myId+" "+firstName+" "+lastName+" "+email+" "+myPassWord);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during changeUtilisateurInfo : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean addProfilePhoto(String myId, Bitmap bmp) {
        try {
            out.flush();
            out.writeObject("addProfilePhoto "+myId);
            out.flush();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            out.writeObject(byteArray);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
            return false;
        }
    }

    public boolean createEvent(String eventName, String eventPlace,
                               int eventPrice, String eventDate, String creator) {
        try {
            out.flush();
            out.writeObject("createEvent "+eventName+" "+eventPlace+" "+eventPrice+" "
                    +eventDate+" "+creator);
            return (boolean)in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during createEvent : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean joinEvent(String myId, String eventName) {
        try {
            out.flush();
            out.writeObject("joinEvent "+myId+" "+eventName);
            return (boolean) in.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            Log.d("connectionTest", "Exception during joinEvent : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean quitEvent(String eventName, String myId) {
        try {
            out.flush();
            out.writeObject("quitEvent "+eventName+" "+myId);
            return (boolean) in.readObject();
        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean addFriend(String myId, String friendName) {
        try {
            out.flush();
            out.writeObject("addFriend "+myId+" "+friendName);
            return (boolean) in.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    public boolean deleteFriend(String myId, String friendName) {
        try {
            out.flush();
            out.writeObject("deleteFriend "+myId+" "+friendName);
            return (boolean) in.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    public String[] getFriends(String myId) {
        try {
            out.flush();
            out.writeObject("getFriends "+myId);
            return (String[]) in.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            connected = false;
        }
        return null;
    }

    public boolean isFriend(String myId, String friendId){
        try {
            out.flush();
            out.writeObject("isFriend "+myId+" "+friendId);
            return (boolean) (in.readObject());
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    public boolean changeEvent(String eventName, String eventPlace,
                               int eventPrice, String eventHour, String eventId) {
        try {
            out.flush();
            out.writeObject("changeEvent "+eventName+" "+eventPlace+" "+eventPrice+" "+eventHour);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during changeEvent : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean isRegistered(String myId) {
        try {
            out.flush();
            out.writeObject("isRegistered "+myId+" "+"utilisateur");
            return (boolean)in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during isRegistered : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public boolean addAdminToEvent(String idAdmin, String eventId) {
        try {
            out.flush();
            out.writeObject("addAdminToEvent "+idAdmin+" "+eventId);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during addAdminToEvent : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    @Override
    public boolean createSondage(String[] sondage) {
        try {
            out.flush();
            String str = "createSondage "+sondage[0];
            for (int i =1 ; i<sondage.length ; i++){
                str += " "+sondage[i];
            }
            out.writeObject(str);
            return (boolean) in.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    @Override
    public boolean voteSondage(String eventName, String choix) {
        try {
            out.flush();
            out.writeObject("voteSondage "+eventName+" "+choix);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    @Override
    public String[] getSondageChoix(String eventId) {
        try {
            out.flush();
            out.writeObject("getSondageChoix "+eventId);
            return (String[]) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return null;
    }

    @Override
    public int[] getSondageVote(String eventId) {
        try {
            out.flush();
            out.writeObject("getSondageVote "+eventId);
            return (int[]) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return null;
    }

    public boolean addMessage(String eventName, String message) {
        try{
            out.flush();
            out.writeObject("addMessage "+eventName);
            out.flush();
            out.writeObject(message);
            return (boolean) in.readObject();
        }catch (IOException | ClassNotFoundException | NullPointerException e){
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    public String[] getAdminMessages(String eventId) {
        try {
            out.flush();
            out.writeObject("getAdminMessage "+eventId);
            return (String[]) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getAdminMessages : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    public boolean isAdmin(String adminId, String eventId) {
        try {
            out.flush();
            out.writeObject("isAdmin "+adminId+" "+eventId);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during isAdmin : " + e.getMessage());
            connected = false;
        }
        return false;
    }

    public String getUserPssWd(String userId) {
        try {
            out.flush();
            out.writeObject("getUserPssWd "+userId);
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getUserPssWd : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    public String getUserFstNme(String userId) {
        try {
            out.flush();
            out.writeObject("getUserFstNme "+userId);
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getUserFstNme : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    public String getUserLstNme(String userId) {
        try {
            out.flush();
            out.writeObject("getUserLstNme "+userId);
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getUserLstNme : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    public String getUserEmail(String userId) {
        try {
            out.flush();
            out.writeObject("getUserEmail "+userId);
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getUserEmail : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    public String getEventPlace(String eventID) {
        try {
            out.flush();
            out.writeObject("getEventPlace "+eventID);
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getEventPlace : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    @Override
    public int getEventPrice(String eventID) {
        try {
            out.flush();
            out.writeObject("getEventPrice "+eventID);
            return (int) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getEventPrice : " + e.getMessage());
            connected = false;
        }
        return -1;
    }

    public String getEventHour(String eventID) {
        try {
            out.flush();
            out.writeObject("getEventHour "+eventID);
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            Log.d("connectionTest", "Exception during getEventHour : " + e.getMessage());
            connected = false;
        }
        return null;
    }

    public boolean addPartyPhoto(String partyId,Bitmap bmp) {
        try {
            out.flush();
            out.writeObject("addPartyPhoto "+partyId);
            out.flush();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            out.writeObject(byteArray);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

    public Bitmap getProfilePhoto(String myId) {
        try {
            out.flush();
            out.writeObject("getProfilePhoto " + myId);
            byte[] byteArray = (byte[]) in.readObject();
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return null;
    }

    public Bitmap getPartyPhoto(String partyId,String photoNumber) {
        try {
            out.flush();
            out.writeObject("getPartyPhoto " + partyId +" " + photoNumber);
            boolean b = (boolean) in.readObject();
            if (b){
                byte[] byteArray = (byte[]) in.readObject();
                return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public ArrayList<Bitmap> getAlbum(String partyId){
        ArrayList<Bitmap> photos = new ArrayList<Bitmap>();
        for(int i = 1; i <= albumLength(partyId); i++){
            Bitmap bmp = getPartyPhoto(partyId, String.valueOf(i));
            if (bmp!=null){
                photos.add(bmp);
            }
        }
        return photos;
    }


    public int albumLength(String partyId){
        try{
            out.flush();
            out.writeObject("albumLength "+partyId);
            return (int) in.readObject();
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String[] getScoreRanking(String eventName){
        try {
            out.flush();
            out.writeObject("getRanking "+eventName);
            return (String[]) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return null;
    }

    public double getUserScore(String eventName, String idUser){
        try {
            out.flush();
            out.writeObject("getScore "+eventName+" "+idUser);
            return (double) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return -1;
    }

    public boolean sendScoreToEvent(String eventName, String idUser, double score){
        if(!connected) return false;
        try {
            out.flush();
            out.writeObject("sendScore "+eventName+" "+idUser+" "+score);
            return (boolean) in.readObject();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
            connected = false;
        }
        return false;
    }

}