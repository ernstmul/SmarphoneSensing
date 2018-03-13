package nl.tudelft.smartphonesensing.wifi;

import android.net.wifi.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.smartphonesensing.HomeActivity;
import nl.tudelft.smartphonesensing.R;
import nl.tudelft.smartphonesensing.utils.AccessPointMatrix;
import nl.tudelft.smartphonesensing.utils.TrainedSet;
import nl.tudelft.smartphonesensing.utils.WifiData;
import nl.tudelft.smartphonesensing.utils.sensors.WifiSensor;

/**
 * Created by ernstmulders on 12/03/2018.
 */

public class TrainActivity extends AppCompatActivity {
    private static String TAG ="TrainActivity";

    //wifi
    private WifiSensor wifi;

    //buttons
    private Button wifi_train_done_button;
    private Button start_wifi_train;
    private Button wifi_get_measurement;
    private Button wifi_end_area_measurement;

    //views
    private View training_setup_view;
    private View training_progress_view;

    //textviews
    private TextView training_area_header_text;
    private TextView training_area_status_text;

    //spinner
    private Spinner area_spinner;

    //accesspoint info
    private List<String> accesspointList;
    private List<AccessPointMatrix> matrices;

    //set number of cells
    Integer cellCount = 20;
    Integer[] timesTrained;

    //hashmap with trained data
    HashMap<String, TrainedSet[]> trained = new HashMap<String, TrainedSet[]>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifitrain);

        //create the wifi
        wifi = new WifiSensor(getApplicationContext());

       //create the area spinner
        createAreaSpinner();

        //set the buttons
        wifi_train_done_button = (Button) findViewById(R.id.wifi_train_done_button);
        start_wifi_train = (Button) findViewById(R.id.start_wifi_train);
        wifi_get_measurement = (Button) findViewById(R.id.wifi_get_measurement);
        wifi_end_area_measurement = (Button) findViewById(R.id.wifi_end_area_measurement);

        //set the views
        training_setup_view = (ScrollView) findViewById(R.id.training_setup_view);
        training_progress_view = (ScrollView) findViewById(R.id.training_progress_view);

        //set the textviews
        training_area_header_text = (TextView) findViewById(R.id.training_area_header_text);
        training_area_status_text = (TextView) findViewById(R.id.training_area_status_text);

        // Init list with known accesspoints
        accesspointList = new ArrayList<String>();

        // Init matrix of each accesspoint's PMF values
        matrices = new ArrayList<AccessPointMatrix>();

        // Init times trained for each cell
        timesTrained = new Integer[cellCount];

        //click listener for start training
        start_wifi_train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ready for training
                getReadyForTraining();
            }
        });

        //click listener for wifi end measurement button
        wifi_end_area_measurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ready for training
                endAreaTrainingTraining();
            }
        });



        //click listener for wifi get measurement button
        wifi_get_measurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ready for training
                getMeasurement();
            }
        });

        //click listener for wifi train done button
        wifi_train_done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Creating csv");
                wifi_train_done_button.setText("Creating CSV ");

                //create the csv files
                //generate the csv's
                for(AccessPointMatrix matrix : matrices){

                    Log.d(TAG, "Createing csv for:" + matrix.BSSID);
                    matrix.saveToCsv(getApplicationContext());
                }

                //now return home
               goBackToHome();
            }
        });

    }

    /**
     * get a wifi measurement, and process it
     */
    private void getMeasurement(){
        //first make a new scan
        List<ScanResult> resultList = wifi.getScanResults();

        //let the user know a measurement is performed
        training_area_status_text.setText("Measurement performed, " + resultList.size() + " accesspoints found.\n Turn 90° clockwise for next measurement.");

        //temporary disable the button, to prevent obtaining double results
        disableMeasurementButton();

        //get the chosen cell
        Integer cellIndex = area_spinner.getSelectedItemPosition();

        //set the trained count
        //add the trained value
        if(timesTrained[cellIndex] == null){
            timesTrained[cellIndex] = 1;
        }else{
            timesTrained[cellIndex]++; //so we know we did an extra training here
        }

        //loop through the accesspoints we found in the scan
        for(ScanResult accesspoint : resultList){
            if(!accesspointList.contains(accesspoint.BSSID)){
                accesspointList.add(accesspoint.BSSID);
                //Log.d(TAG, "Didn't know: " + accesspoint.BSSID + "so create and add empty matrix");

                AccessPointMatrix newmatrix = new AccessPointMatrix(accesspoint.BSSID);
                newmatrix.createMatrix(cellCount);
                matrices.add(newmatrix);
            }


            //save the BSSID value in the table for the correct cell
            AccessPointMatrix storeMatrix = matrices.get(accesspointList.indexOf(accesspoint.BSSID));

            // useFrequency:
            storeMatrix.addValue(cellIndex, accesspoint.level, timesTrained[cellIndex], true);

        }

        //get the selected area
        String area_name = area_spinner.getSelectedItem().toString();

        //display the amount of trained information
        training_area_header_text.setText("Training area " + area_name + " ("+timesTrained[cellIndex]+" measurements)");



    }

    /**
     * disables the measurement button for 2 seconds to prevent doubles
     */
    private void disableMeasurementButton(){
        wifi_get_measurement.setEnabled(false);
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        wifi_get_measurement.setEnabled(true);
                    }
                });
            }
        }, 2000);
    }

    /**
     * prepares the interface for training for a specific area
     */
    private void getReadyForTraining(){

        //get the selected area
        String area_name = area_spinner.getSelectedItem().toString();

        //get selected area index
        Integer area_index = area_spinner.getSelectedItemPosition();

        //get times trained
        String times_trained = (timesTrained[area_index] == null) ? "0" : timesTrained[area_index].toString();

        //display
        training_area_header_text.setText("Training area " + area_name + " ("+times_trained+" measurements)");

        //and finaly set the visibility
        training_setup_view.setVisibility(View.GONE);
        training_progress_view.setVisibility(View.VISIBLE);
        wifi_train_done_button.setVisibility(View.GONE); //we cant end training whilst in measurement
    }

    /**
     * end the training for an area
     */
    private void endAreaTrainingTraining(){
        //set the visibility
        training_setup_view.setVisibility(View.VISIBLE);
        training_progress_view.setVisibility(View.GONE);
        wifi_train_done_button.setVisibility(View.VISIBLE);
    }

    /**
     * Return to home activity
     */

    public void goBackToHome(){

        // start LocalizationActivity.class
        Intent myIntent = new Intent(TrainActivity.this,
                HomeActivity.class);
        startActivity(myIntent);
    }

    /**
     * create the selection spinner for the area we want to train
     */
    private void createAreaSpinner(){
        //set the spinner to the area values
        area_spinner = (Spinner) findViewById(R.id.area_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.area_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        area_spinner.setAdapter(adapter);
    }
}
