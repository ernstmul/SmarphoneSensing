package nl.tudelft.smartphonesensing.utils;


import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by ernstmulders on 21/02/2018.
 */

/*
 *  Class which allows manipulation of matrices containing PMF values of access points
 */

public class AccessPointMatrix {
    private static String TAG ="AccessPointMatrix";
    public Double[][] matrix;
    public String BSSID;

    public AccessPointMatrix(String BSSID){
        this.BSSID = BSSID;
    }

    public void createMatrix(Integer cellcount){
        matrix = new Double[cellcount][100]; // 100 bins for RSSI values
        Log.d(TAG, "Matrix created");
    }

    public void addValue(Integer cellIndex, Integer level, Integer timesTrained, Boolean useFrequency){
        // make the level absolute e.g. 30dBm = strong signal, 90dBm = weak signal
        // not negative values -30dBm or -90dBm
        level = Math.abs(level);
        // ignore values above 100dBm
        if(level > 100){return;}

        //go through the required row and change existing values, and add the current level
        Integer cellCount = 0;
        //Log.d(TAG, "Training cell " + cellIndex + " found level: "+ level + " training for the " + timesTrained + " time");
        for(Double cell : matrix[cellIndex]){

            if(cell == null){
                cell = 0.0;
            }

            Double newCellValue;

            // useFrequency:
            // true: log the frequency of each RSSI value to each matrix entry (no normalization)
            //          this is easier to process by MATLAB
            // false: log the relative frequency of each RSSI level value to each matrix entry
            //          this means dividing by the number of entries every time we add to the matrix
            if(useFrequency){
                newCellValue = cell;

                if (cellCount.equals(level)) {
                    newCellValue = cell + 1;
                }
            }
            else {
                newCellValue = (cell * (timesTrained - 1)) / timesTrained;

                if (!newCellValue.equals(0.0)) {
                    Log.d(TAG, "Found existing value for index " + cellCount + " which has become:" + newCellValue);
                }

                if (cellCount.equals(level)) {
                    Log.d(TAG, "before: " + newCellValue + "  1/times trained: " + (1.0 / timesTrained));
                    newCellValue += (1.0 / timesTrained);

                    Log.d(TAG, "Yes equals to " + level + ", update to value: " + newCellValue);
                }
            }

            // add new cell value to the matrix

            matrix[cellIndex][cellCount] = newCellValue;

            cellCount ++;

        }

    }

    public void saveToCsv(Context context){
        String csvText = "";

        //loop through all accesspoints
        for(Double[] rssiValues : matrix){
            //loop through all rssi values
            for(Double value : rssiValues){
                if(value == null){
                    value = 0.0;
                }
                csvText = csvText + value.toString() + ",";
            }
            //chop off last comma
            csvText = csvText.substring(0, csvText.length() - 1);

            //add brakeline
            csvText = csvText + "\n";
        }

        //create the file
        try {
            if(context == null){
                Log.d(TAG, "Context is null");
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(BSSID.replace(":","-")+".csv", context.MODE_PRIVATE));
            outputStreamWriter.write(csvText);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public Double[] getProbabilityForCellFromLevel(Integer level){
        //make the level absolute
        level = Math.abs(level);

        //create variable to store the values for each cell
        Double[] valuePerCell = new Double[matrix.length];
        Double sum = 0.0;

        //get the values and summation
        Integer cellCount = 0;
        for(Double[] values : matrix){
            sum += (values[level] == null) ? 0.0000001 : values[level];
            valuePerCell[cellCount] = (values[level] == null) ? 0.0000001 : values[level];
            cellCount++;
        }

        //normalize the values
        cellCount = 0;
        Double[] normalizedValues = new Double[matrix.length];
        for(Double cellValue : valuePerCell){
            normalizedValues[cellCount] = valuePerCell[cellCount] / sum;
            cellCount++;
        }

        return normalizedValues;

    }// convert access point matrix to string
    public String toString(){
        String output = "[";

        for(Double[] row : matrix){

            for(Double cell : row){
                output += cell + ",";
            }

            output += "\n";
        }

        output += "]";
        return output;
    }
}
