package nl.tudelft.smartphonesensing.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
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

    //set number of measurements before results
    private Integer measurement_number = 10;
    private Integer measurement_count = 0;
    private Integer[] cells_found;

    private Double[] logs;

    private String previousScan = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifilocate);

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

                //delete app cache
                deleteCache(getApplicationContext());

                //reset
                measurement_count = 0;
                cells_found = new Integer[cellCount];
                previousScan = "";

                //create the wifi
                wifi = new WifiSensor(getApplicationContext());

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


        // Define comparator for the scanresults according to RSSI value
        Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                return (lhs.level >rhs.level ? -1 : (lhs.level==rhs.level ? 0 : 1));
            }
        };

        Double[] prior = new Double[cellCount];

        // 3. Initial prior is [0.05 0.05 0.05 ... 0.05] when cellCount is 20
        for(Integer cellIndex = 0; cellIndex < cellCount; cellIndex++){
            prior[cellIndex] = 1.0/cellCount;
        }

        // create initial probability vector containing sum of probabilities
        Double[] posteriorSumAP = new Double[cellCount];

        // initialize posteriorSum
        for (int i = 0; i < cellCount; i++){
            posteriorSumAP[i] = 0.0;
        }


        boolean thresholdReached = false;
        int whileLoopCounter = 0;
        //

        // if threshold is not reached and whileLoopCounter not overclocked
        while (!thresholdReached && whileLoopCounter < 2){

            // do scan
            // 1. Do a new scan of the Wifi Access Points
            List<ScanResult> currentScanResultList = wifi.getScanResults();

            //keep getting new results
            while(previousScan.equals(currentScanResultList.toString())){
                currentScanResultList = wifi.getScanResults();
            }
            //and remember for next time
            previousScan = currentScanResultList.toString();


            // 2. sort the scanned results according to RSSI values using comparator
            Collections.sort(currentScanResultList, comparator);


            for(Integer checkCount = 0; checkCount < currentScanResultList.size(); checkCount++) {

                // get the scan result of index checkCount
                ScanResult accesspoint = currentScanResultList.get(checkCount);

                /*
                 * Bayesian filter using the distribution defined by each AP and then (possibly weighted)
                 * summing them all together. If the resulting belief is not sure enough after scanning
                 * all the APs, re-scan and re-calculate. In this case, use the previously determined
                 * belief as the prior, instead of [0.05 0.05 ... 0.05] for cellCount of 20
                 */

                //make sure the accesspoint is known in the trained list
                if (trained.get(accesspoint.BSSID) != null) {

                    Double[] cellVector = computeGaussionCellVector(trained.get(accesspoint.BSSID), Math.abs(accesspoint.level));

                    // create bias sum
                    Double priorCellVectorSum = computePriorCellVectorSum(prior, cellVector);

                    Double[] currentAccessPointPosterior = computePosterior(priorCellVectorSum, cellVector, prior);
                    //Log.d(TAG, "posterior: " + posterior);

                    // ignore posterior if NaN, keep previous prior instead
                    for (int i = 0; i < cellCount; i++) {
                        if (!currentAccessPointPosterior[i].isNaN()) {
                            prior[i] = currentAccessPointPosterior[i];
                        } else {
                            prior[i] = 1E-40; // otherwise assign 0 - Could be possible problem* CHECK!
                        }
                    }

                    // current posterior is the previous
                    currentAccessPointPosterior = normalize(prior);

                    // add to overall posteriorSumAP - could add weight for certain SSIDs if necessary
                    Double weight = 1.0;
                    weight = 2.0 - 0.3*Math.log(Math.abs(accesspoint.level));
                    Log.d(TAG, "calc:" + weight +" ");


                    for (int j = 0; j < cellCount; j++) {

                        // only shows 0
                        posteriorSumAP[j] = posteriorSumAP[j] + weight*currentAccessPointPosterior[j]; // gets null pointer exception here
                    }


                }

            } // for checkCount (looping through each access point)

            if (isMatchingStoppingCriteria(normalize(posteriorSumAP))){
                thresholdReached = true;
            }

            // increase while loop counter
            whileLoopCounter++;


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

        if(measurement_count < measurement_number ){

            if(cells_found[cellChosenIndex] == null && measurement_count > 4){
                cells_found[cellChosenIndex] = 1;
            }
            else if(measurement_count > 4) {
                cells_found[cellChosenIndex]++; //increment the value
            }
            measurement_count++;

            Timer buttonTimer = new Timer();
            buttonTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            locate_area_prediction_text.setText("Measuring " + measurement_count + " out of " + measurement_number);
                            calculateLocation();
                        }
                    });
                }
            }, 10);


        }
        else{
            Integer biggestCount = 0;
            Integer mostOccuringIndex = -1;
            for(Integer checkCount = 0; checkCount < cellCount; checkCount++){
                if(cells_found[checkCount] != null && cells_found[checkCount] > biggestCount){
                    biggestCount = cells_found[checkCount];
                    mostOccuringIndex = checkCount;
                }
            }

            // write to 'where am I' text box
            Double prct = (biggestCount/((double)measurement_number - 5))*100;
            locate_area_prediction_text.setText("I'm in : " + (mostOccuringIndex + 1) + "("+prct + "%)\n\n" + prob);
        }

    }


    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
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
            //Log.d(TAG, "Looking for:" + cellIndex);
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
        return result; // shouldn't we normalize here ?
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
        //Log.d(TAG, "Sum of vectorToNormalize:" + sumVectorToNormalize);

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

                        String[] parts = file.split(Pattern.quote("."));
                        String temp = parts[0].replace("-", ":");
                        parts = temp.split("_");
                        String bssid = parts[1];
                        TrainedSet[] trainedValues = new TrainedSet[cellCount];

                        Integer checkedLine = 0;
                        while ((line = reader.readLine()) != null) {
                            if(checkedLine < cellCount) {
                                String[] cellValues = line.split(",");
                                Double[] matrixRow = new Double[100];

                                for(Integer cellCount = 0; cellCount < 100; cellCount++){
                                    matrixRow[cellCount] = Double.parseDouble(cellValues[cellCount]);
                                }

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