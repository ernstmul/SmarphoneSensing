package nl.tudelft.smartphonesensing.wifi;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import nl.tudelft.smartphonesensing.HomeActivity;
import nl.tudelft.smartphonesensing.R;
import nl.tudelft.smartphonesensing.utils.TrainedSet;
import nl.tudelft.smartphonesensing.utils.sensors.WifiSensor;

/**
 * Created by ernstmulders on 12/03/2018.
 */

public class LocateActivity  extends AppCompatActivity {
    private static String TAG ="LocateActivity";

    //wifi
    private WifiSensor wifi;

    //buttons
    private Button wifi_locate_done_button;
    private Button wifi_get_location;

    //textviews
    private TextView locate_area_status_text;
    private TextView locate_area_prediction_text;

    //hashmap with trained data
    HashMap<String, TrainedSet[]> trained = new HashMap<String, TrainedSet[]>();

    //set number of cells
    private Integer cellCount = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifilocate);

        //create the wifi
        wifi = new WifiSensor(getApplicationContext());

        //buttons
        wifi_locate_done_button = (Button) findViewById(R.id.wifi_locate_done_button);
        wifi_get_location = (Button) findViewById(R.id.wifi_get_location);

        //textviews
        locate_area_status_text = (TextView) findViewById(R.id.locate_area_status_text);
        locate_area_prediction_text = (TextView) findViewById(R.id.locate_area_prediction_text);

        //load the trained data
        loadTrainingData();

        //click listener for wifi train done button
        wifi_locate_done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //return home
                goBackToHome();
            }
        });

        //click listener for detect location
        wifi_get_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //calculate location
                calculateLocation();

                // disable button for 2 seconds
                disableGetLocationButton();
            }
        });
    }

    private void disableGetLocationButton(){
        wifi_get_location.setEnabled(false);
        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        wifi_get_location.setEnabled(true);
                    }
                });
            }
        }, 2000);
    }


    private void calculateLocation(){
        Log.d(TAG, "Determining current location ... ");

        // Define comparator for the scanresults according to RSSI value
        Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (lhs.level >rhs.level ? -1 : (lhs.level==rhs.level ? 0 : 1));
            }
        };

        // 1. Do a new scan of the Wifi Access Points
        List<ScanResult> currentScanResultList = wifi.getScanResults();

        // 2. sort the scanned results according to RSSI values
        Collections.sort(currentScanResultList, comparator);

        Log.d(TAG, currentScanResultList.toString());

        Double[] prior = new Double[cellCount];
        Log.d(TAG, "reset prior:" + prior.toString());

        // 3. Set the intial belief values into the prior
        for(Integer cellIndex = 0; cellIndex < cellCount; cellIndex++){
            prior[cellIndex] = 1.0/cellCount;
        }

        //loop through the list
        for(Integer checkCount = 0; checkCount < currentScanResultList.size(); checkCount++){
            ScanResult accesspoint = currentScanResultList.get(checkCount);
            //Log.d(TAG, accesspoint.toString());

            //make sure the accesspoint is known in the trained list
            if(trained.get(accesspoint.BSSID) != null) {
                Log.d(TAG, "accesspoint "+accesspoint.BSSID+ " is known");
                //get the correct matrix corresponding to the accesspoint
                        /* AccessPointMatrix matrix = matrices.get(accesspointList.indexOf(accesspoint.BSSID));
                        Double[] cellVector = getCellProbabilityValues(matrix, accesspoint.level);

                        //normalize the cellVector
                        Double[] normalized = normalize(cellVector); // change this, not correct*/

                Double[] cellVector = computeGaussionCellVector(trained.get(accesspoint.BSSID), Math.abs(accesspoint.level));

                // create bias sum
                Double priorCellVectorSum = computePriorCellVectorSum(prior, cellVector);

                Double[] posterior = computePosterior(priorCellVectorSum,cellVector,prior);
                Log.d(TAG, "posterior: "+ posterior);

                // ignore posterior if NaN, keep previous prior instead
                for (int i = 0; i < cellCount; i++){
                    if(!posterior[i].isNaN()) {
                        prior[i] = posterior[i];
                    }
                }

                // normalize the prior
                prior = normalize(prior);

                // add stopping criteria? TODO
                if(isMatchingStoppingCriteria(prior)){
                    //make it stop
                    checkCount = currentScanResultList.size();
                }
            }

        }

        for(Double probability : prior){
            Log.d(TAG, "prior: " + probability);
        }


        // Show Results

        // define prob vector of the probabilities of being in each cell
        Double highestProbability = 0.0;
        Integer cellChosenIndex = -2;
        String prob = "";

        // select cell of highest probability
        for(Integer cellIndex = 0; cellIndex < cellCount; cellIndex++){
            if(prior[cellIndex] > highestProbability){
                highestProbability = prior[cellIndex];
                cellChosenIndex = cellIndex;
            }
            prob = prob + "Area " + (cellIndex + 1) + ": " +prior[cellIndex].toString() + ",\n";
        }
        prob = prob + "";

        // write to where am I text box
        locate_area_prediction_text.setText("I'm in : " + (cellChosenIndex + 1) + "\n\n" + prob);
    }

    private boolean isMatchingStoppingCriteria(Double[] prior){
        Double maxVal = 0.0;

        for(Double val:prior){
            if(val > maxVal){maxVal = val;}
        }
        // threshold for cell probability
        return (maxVal > 0.5);
    }
    /*
     * compute the Gaussion for the cell vector
     */
    private Double[] computeGaussionCellVector(TrainedSet[] trainedData, Integer level){
        Double[] cellVector = new Double[cellCount];

        for(Integer cellIndex = 0; cellIndex < cellCount; cellIndex++){
            Log.d(TAG, "Looking for:" + cellIndex);
            cellVector[cellIndex] = trainedData[cellIndex].matrix[level];
        }

        return normalize(cellVector);
    }

    /*
     *  For each cell, determine the posterior
     */
    private Double[] computePosterior(Double priorCellVectorSum, Double[] cellVector, Double[] prior){
        Double[] result = new Double[cellCount];
        for(Integer cellIndex = 0; cellIndex < cellCount; cellIndex++){
            result[cellIndex] = prior[cellIndex]*cellVector[cellIndex]/priorCellVectorSum;
        }
        return result; // shouldn't we normalize here ? TODO
    }

    /*
     *  Determine the prior cell vector sum = P(1)*C(1) + P(2)*C(2) + .. + P(n)*C(n)
     *
     *  where P is the prior and C is the current probability
     *
     *  This is calculated for the denominator of the Bayes' inference formula
     */
    private Double computePriorCellVectorSum(Double[] prior, Double[] cellVector){
        Double sum = 0.0;

        for(Integer cellIndex = 0; cellIndex < cellCount; cellIndex++){
            sum += prior[cellIndex] * cellVector[cellIndex];
        }

        return sum;
    }

    /*
     *  Sum the indices of a vector
     */
    public static Double sum(Double...values) {
        Double result = 0.0;
        for (Double value:values)
            result += value;
        return result;
    }

    /*
     *  Normalize a vector to have entries sum to 1
     */
    public static Double[] normalize(Double[] vectorToNormalize){
        Double sumVectorToNormalize = sum(vectorToNormalize);
        Log.d(TAG, "Sum of vectorToNormalize:" + sumVectorToNormalize);

        Double[] output = new Double[vectorToNormalize.length];

        for(Integer cellIndex = 0; cellIndex < vectorToNormalize.length; cellIndex++){
            output[cellIndex] = vectorToNormalize[cellIndex] / sumVectorToNormalize;
        }

        return output;
    }

    /**
     * Load the training csv data
     */
    private void loadTrainingData(){
        String [] list;
        try {
            list = getAssets().list("");
            if (list.length > 0) {
                for (String file : list) {
                    if(file.toLowerCase().contains("csv")){
                        //its a file for training
                        InputStreamReader is = new InputStreamReader(getAssets()
                                .open(file));

                        BufferedReader reader = new BufferedReader(is);
                        String line;

                        Log.d(TAG, file);
                        String[] parts = file.split(Pattern.quote("."));
                        Log.d(TAG, parts[0]);
                        String temp = parts[0].replace("-", ":");
                        parts = temp.split("_");
                        String bssid = parts[1];
                        TrainedSet[] trainedValues = new TrainedSet[cellCount];

                        Integer checkedLine = 0;
                        while ((line = reader.readLine()) != null) {
                            Log.d(TAG, line);
                            if(checkedLine < cellCount) {
                                String[] cellValues = line.split(",");
                                Double[] matrixRow = new Double[100];

                                for(Integer cellCount = 0; cellCount < 100; cellCount++){
                                    matrixRow[cellCount] = Double.parseDouble(cellValues[cellCount]);
                                }

                                Log.d(TAG, "trained for cell" + checkedLine);
                                trainedValues[checkedLine] = new TrainedSet(matrixRow, bssid);
                            }
                            checkedLine++;

                        };
                        //add to the hashmap
                        trained.put(bssid, trainedValues);

                    }


                }
            }

            //output to user
            locate_area_status_text.setText("Loaded " + trained.size() + " trained accesspoints");

            //log
            Log.d(TAG, "Training data loaded");
            Log.d(TAG, trained.toString());
        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }
    }

    /**
     * Return to home activity
     */

    public void goBackToHome(){

        // start LocalizationActivity.class
        Intent myIntent = new Intent(LocateActivity.this,
                HomeActivity.class);
        startActivity(myIntent);
    }
}
