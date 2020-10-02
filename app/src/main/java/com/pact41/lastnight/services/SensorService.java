package com.pact41.lastnight.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.pact41.lastnight.LastNight;
import com.pact41.lastnight.R;
import com.pact41.lastnight.activities.CurrentPartyActivity;
import com.pact41.lastnight.activities.PartyProfileActivity;
import com.pact41.lastnight.activities.audio.AccelTempo;
import com.pact41.lastnight.client_server.MyClient;
import com.pact41.lastnight.model.LastNightStateManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

/**
 * Service used to get sensor data and write it into a file in the background
 */
public class SensorService extends Service implements SensorEventListener{
    //Sensor data
    private SensorManager sensorManager;
    Sensor accelerometer;

    //Captures information attributes
    private long start, end;
    private long intervalInMs = 10000; //Tempo calculating interval
    private long origin; //Stores the starting time of the current tempo estimation
    ArrayList<Double> accelXArray, accelYArray, accelZArray, timeArray;
    private int nbOfTempoPoints;

    //Unique identifier for foreground service notification
    private final int PARTY_ONGOING_NOTIFICATION = 1;

    //File writing attributes
    private boolean external = true;
    String path;
    private File filePath;
    private DataOutputStream sensorDos;

    //Context attributes
    private String partyName;
    private String username;
    //We create a client for the service not to interfere with activities' client on the buffers
    private MyClient serviceClient;

    //To keep the device awake while movements tracking is on
    PowerManager.WakeLock trackingWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        //Getting sensors information
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer == null) {
            //Shows a warning if accelerometer wasn't detected
            Toast.makeText(this, getResources().getString(R.string.sensor_service_warning_sensor_not_found), Toast.LENGTH_LONG).show();
            stopSelf();
        }
        else
            Toast.makeText(this, getResources().getString(R.string.sensor_service_started), Toast.LENGTH_LONG).show();

        //Initializing connection link
        LastNightStateManager stateManager = ((LastNight) getApplicationContext()).getAppStateManager();

        //The service has got its own connection client because it must work in the background
        serviceClient = new MyClient();
        serviceClient.init(SensorService.this);

        //Initializing file writing functionality
        //Determining the future file location
        if(external){ //Path to external files folder
            //If external storage isn't available
            if(!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))){
                Toast.makeText(SensorService.this, getResources().getString(R.string.sensor_service_external_storage_missing), Toast.LENGTH_LONG).show();
                stopSelf();
            }
            //If external storage is available
            path = getExternalFilesDir(null).toString();
            Log.d("testFileWriting", "External files path : " + path);
        }
        else
            path = getFilesDir().toString(); //Path to internal files folder

        this.partyName = stateManager.getCurrentPartyName();
        this.username = stateManager.getCurrentUser();

        try {
            //Initializing the file data output stream to write into it
            path +="/"+username+"_"+partyName+".txt"; //We put the user name as file name, followed by party name
            filePath = new File(path);
            sensorDos = new DataOutputStream(new FileOutputStream(filePath));
            sensorDos.writeLong(intervalInMs); //Used by the recap activity to know which time interval has been used
        } catch (FileNotFoundException e) {
            Log.d("testFileWriting", "Problem while initializing date output stream : File path not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("testFileWriting", "Problem while writing time interval into the file");
            e.printStackTrace();
        }

        //Initializing measuring objects
        accelXArray = new ArrayList<>();
        accelYArray = new ArrayList<>();
        accelZArray = new ArrayList<>();
        timeArray = new ArrayList<>();
        start = Calendar.getInstance().getTimeInMillis();
        origin = start;
        nbOfTempoPoints = 0;

        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(
                Context.POWER_SERVICE);

        //To prevent tracking from being disabled by the Android system when screen is off
        trackingWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        trackingWakeLock.acquire();

        //Starting measuring acceleration
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        //Generating permanent notification on the user's screen, will be displayed until the end of service
        Intent notificationIntent = new Intent(this, CurrentPartyActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.sensor_service_running))
                .setSmallIcon(R.drawable.notif_logo)
                .setContentIntent(pendingIntent).build();

        startForeground(PARTY_ONGOING_NOTIFICATION, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null) //intent value is null if system killed the service
        {
            boolean timeReached = intent.getBooleanExtra(PartyProfileActivity.TIME_REACHED_ACTION, false);
            if(timeReached) {
                ((LastNight) getApplicationContext()).getAppStateManager().notifyLeavingEvent(); //Party is finished, then we must refresh the app state
                stopSelf();
            }
            else
            {
                if(startId != 1) //Case of first launch of the service
                    // Case of launching error : service is launched a second time only if time is reached (see case above)
                    Log.d("fileWriting", "Service lancé anormalement alors qu'il était déjà en route");
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Gets acceleration values
        float aX = event.values[0];
        float aY = event.values[1];
        float aZ = event.values[2];
        long time = Calendar.getInstance().getTimeInMillis();

        if(time - origin < intervalInMs){ //Stores current acceleration and time into the lists
            timeArray.add(Double.valueOf(time));
            accelXArray.add(Double.valueOf(aX));
            accelYArray.add(Double.valueOf(aY));
            accelZArray.add(Double.valueOf(aZ));
        }
        else{ //Else we login calculating tempo on the current list and we clear values for the next calculation
            //Creating a copy to prevent erasing values before ASyncTask launch
            ArrayList<Double> timeResult = new ArrayList<>(timeArray);
            ArrayList<Double> accelXResult = new ArrayList<>(accelXArray);
            ArrayList<Double> accelYResult = new ArrayList<>(accelYArray);
            ArrayList<Double> accelZResult = new ArrayList<>(accelZArray);
            new AccelTempoTask().execute(timeResult, accelXResult, accelYResult, accelZResult);
            origin = time;
            timeArray.clear();
            accelXArray.clear();
            accelYArray.clear();
            accelZArray.clear();
            nbOfTempoPoints++; //We keep the nb of points which should be into the file
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Must implement it but nothing special to do there
        Log.d("testFileWriting", "Accuracy changed : " + accuracy + " s");
    }

    /** Calculates tempo from the acceleration and time data given into parameters */
    private class AccelTempoTask extends AsyncTask<ArrayList<Double>, Integer, Boolean>{
        @Override
        protected Boolean doInBackground(ArrayList<Double>... params) {
            ArrayList<Double> time = params[0];
            ArrayList<Double> accelX = params[1];
            ArrayList<Double> accelY = params[2];
            ArrayList<Double> accelZ = params[3];
            int size = time.size();

            //Displaying the average sample time for the used time interval
            float averageSampleTime = ((float)(intervalInMs)/ (size-1))/1000;
            Log.d("testFileWriting", "Calcul de tempo n°"+nbOfTempoPoints+". "+"Précision moyenne du capteur : "+averageSampleTime+" secondes");

            //Calculating tempo values on the lists data
            AccelTempo.score = 0;
            int tempoX = AccelTempo.tempo(accelX, time);
            int tempoY = AccelTempo.tempo(accelY, time);
            int tempoZ = AccelTempo.tempo(accelZ, time);
            try{
                sensorDos.writeInt(tempoX);
                sensorDos.writeInt(tempoY);
                sensorDos.writeInt(tempoZ);
                sensorDos.writeDouble(AccelTempo.score);
            }
            catch(IOException e){
                return false;
            }
            serviceClient.sendScoreToEvent(partyName, username, AccelTempo.score);
            serviceClient.init(SensorService.this); //If connection lost, will try to reset it
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(!success) Log.d("testFileWriting", "L'un des points n'a pas pu être écrit correctement.");
        }
    }

    @Override
    public void onDestroy()
    {
        sensorManager.unregisterListener(this); //Disables callback sensor methods
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 1;
                try {
                    serviceClient.quitEvent(partyName, username);
                    sensorDos.close();
                    end = Calendar.getInstance().getTimeInMillis();

                    DataInputStream sensorDis = new DataInputStream(new FileInputStream(filePath));
                    Log.d("testFileWriting", "Contenu du fichier \"+"+filePath+"\" :");
                    Log.d("testFileWriting", "Intervalle de capture lu dans le fichier = "+sensorDis.readLong()+" ms");

                    while (sensorDis.available() > 0) {
                        int tempoX = sensorDis.readInt();
                        int tempoY = sensorDis.readInt();
                        int tempoZ = sensorDis.readInt();
                        Log.d("testFileWriting","t=" + intervalInMs*i + ", " +
                                "tempo =" + "{" +tempoX+ "; " +tempoY+ "; " +tempoZ+ "}" + ", score = "+sensorDis.readDouble());
                        i++;
                    }
                    if(i-1 != nbOfTempoPoints)
                        Log.d("testFileWriting", "Erreur : le fichier comporte " + i + " calculs de tempo au lieu de " + nbOfTempoPoints + ".");
                    else
                        Log.d("testFileWriting", "Les " + nbOfTempoPoints + " points de tempo ont bien été écrits dans le fichier !");

                    sensorDis.close();
                } catch (FileNotFoundException e) {
                    Log.d("testFileWriting", "Exception while reading the file");
                } catch (IOException e) {
                    Log.d("testFileWriting", "Exception while closing DataInputStream");
                }
            }
        }).start();
        Toast.makeText(this, getResources().getString(R.string.sensor_service_stopped), Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Fichier de tempo enregistré dans " + filePath, Toast.LENGTH_SHORT).show();
        trackingWakeLock.release(); //Disables wakelock
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
}
