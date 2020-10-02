package com.pact41.lastnight.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.pact41.lastnight.R;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class PartyRecapActivity extends AppCompatActivity {

    //Values from the file
    protected String filename;

    //Values from results, made at a regular time interval
    private double timeInterval;
    ArrayList<Integer> tempoXArray, tempoYArray, tempoZArray;
    ArrayList<Double> scoreArray;

    //Layout components
    private GraphView tempoGraph;
    private GraphView scoreGraph;
    private TextView progression;
    private Button album;

    public final static String PARTY_NAME_RECAP = "com.pact41.lastnight.intent.extra.PARTY_NAME_RECAP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //File path is stored into the sent intent
        filename = getIntent().getExtras().getString(MenuActivity.RECAP_FILE_DIRECTORY);

        setContentView(R.layout.party_recap);

        tempoGraph = (GraphView) findViewById(R.id.party_recap_tempo_graph);
        scoreGraph = (GraphView) findViewById(R.id.party_recap_score_graph);
        progression = (TextView) findViewById(R.id.party_recap_progression);

        tempoXArray = new ArrayList<>();
        tempoYArray = new ArrayList<>();
        tempoZArray = new ArrayList<>();
        scoreArray = new ArrayList<>();

        new ReadTempoValuesTask().execute(filename);

        album= (Button) findViewById(R.id.party_recap_album);
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent album=new Intent(PartyRecapActivity.this, AlbumActivity.class);
                album.putExtra(PARTY_NAME_RECAP,getIntent().getStringExtra(MenuActivity.PARTY_NAME));
                startActivity(album);
            }
        });
    }

    private class ReadTempoValuesTask extends AsyncTask<String, Integer, Integer>{

        protected Integer doInBackground(String... params){
            String fileName = params[0];
            File filePath = new File(getExternalFilesDir(null).toString() + "/" + fileName);
            int size = 0;
            try {
                DataInputStream sensorDis = new DataInputStream(new FileInputStream(filePath));
                timeInterval = sensorDis.readLong();
                while (sensorDis.available() > 0) {
                    int tempoX = sensorDis.readInt();
                    tempoXArray.add(tempoX);
                    int tempoY = sensorDis.readInt();
                    tempoYArray.add(tempoY);
                    int tempoZ = sensorDis.readInt();
                    tempoZArray.add(tempoZ);
                    Double score = sensorDis.readDouble();
                    scoreArray.add(score);
                    size++;
                }
                sensorDis.close();
            } catch (FileNotFoundException e) {
                Log.d("partyRecapTest", "Accelerometer file not found");
            } catch (IOException e) {
                Log.d("partyRecapTest", "Error while reading the file");
            }
            return size;
        }

        @Override
        protected void onPostExecute(Integer size) {
            new TracePointsTask().execute(size); //When reading is finished, we can start generating the graph
        }
    }

    private class TracePointsTask extends AsyncTask<Integer, Integer, Integer> {

        private DataPoint[] tempoXPoints, tempoYPoints, tempoZPoints, scorePoints;

        protected Integer doInBackground(Integer... params) {
            int size = params[0];
            tempoXPoints = new DataPoint[size];
            tempoYPoints = new DataPoint[size];
            tempoZPoints = new DataPoint[size];
            scorePoints = new DataPoint[size];

            for (int i = 1; i <= size; i++){ //We consider that the time interval is regular
                tempoXPoints[i - 1] = new DataPoint((float)timeInterval*(i) / 1000, tempoXArray.get(i - 1));
                tempoYPoints[i - 1] = new DataPoint((float)timeInterval*(i) / 1000, tempoYArray.get(i - 1));
                tempoZPoints[i - 1] = new DataPoint((float)timeInterval*(i) / 1000, tempoZArray.get(i - 1));
                scorePoints[i - 1] = new DataPoint((float)timeInterval*(i - 0.5) /1000, scoreArray.get(i - 1));
            }
            return size;
        }

        @Override
        protected void onPostExecute(Integer size) {
            LineGraphSeries<DataPoint> tempoXSeries = new LineGraphSeries<>(tempoXPoints);
            LineGraphSeries<DataPoint> tempoYSeries = new LineGraphSeries<>(tempoYPoints);
            LineGraphSeries<DataPoint> tempoZSeries = new LineGraphSeries<>(tempoZPoints);
            tempoXSeries.setColor(Color.RED);
            tempoYSeries.setColor(Color.BLUE);
            tempoXSeries.setColor(Color.GREEN);
            final BarGraphSeries<DataPoint> scoreSeries  = new BarGraphSeries<>(scorePoints);
            tempoGraph.getViewport().setScalable(true);
            tempoGraph.getViewport().setXAxisBoundsManual(true);
            scoreGraph.getViewport().setXAxisBoundsManual(true);
            tempoGraph.getViewport().setMinX((float)timeInterval/1000);
            tempoGraph.getViewport().setMaxX((float)timeInterval*size / 1000);
            scoreGraph.getViewport().setMinX(0);
            scoreGraph.getViewport().setMaxX((float)timeInterval*size / 1000);
            tempoXSeries.setTitle("X");
            tempoYSeries.setTitle("Y");
            tempoZSeries.setTitle("Z");
            scoreSeries.setTitle("Score");
            // styling
            scoreSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.rgb(
                            255 - (int) (((double)data.getY() / scoreSeries.getHighestValueY()) * 0),
                            255 - (int) (((double)data.getY() / scoreSeries.getHighestValueY()) * 255),
                            255 - (int) (((double)data.getY() / scoreSeries.getHighestValueY()) * 255));
                }
            });

            tempoGraph.addSeries(tempoXSeries);
            tempoGraph.addSeries(tempoYSeries);
            tempoGraph.addSeries(tempoZSeries);
            tempoGraph.getLegendRenderer().setVisible(true);
            scoreGraph.addSeries(scoreSeries);
        }
    }
}