package nl.tudelft.smartphonesensing.utils.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by ernstmulders on 11/03/2018.
 */

public class Compass implements SensorEventListener{
    private static String TAG = "Compass";

    private Context context;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;

    private TextView wifi_status_text;
    private ImageView compass_needle;
    private float currentDegree = 0f;

    /**
     * Initialize the compass
     * @param c context
     */
    public Compass(Context c, TextView statusview, ImageView compasneedle){
        context = c;
        wifi_status_text = statusview;
        compass_needle = compasneedle;

        //create sensor manager
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        //check if we have the correct sensors
        if(hasCorrectSensors()){
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }


    /**
     * returns if the device has the correct sensors
     */
    public boolean hasCorrectSensors(){

        return ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) && (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //more interesting info: https://developer.android.com/guide/topics/sensors/sensors_position.html
        //float azimuth = Math.round( ( ( Math.toDegrees( event.values[0] ) + 360 ) % 360) * 10 ) / 10;

        if(Math.abs(currentDegree - event.values[0]) > 2){
            String direction;

            if(currentDegree - event.values[0] > 0){
                direction = " moving counter clockwise";
            }
            else{
                direction = " moving clockwise";
            }
            wifi_status_text.setText(Math.round(event.values[0]*10)/10 + " ° " + direction);
            currentDegree = event.values[0];

        }
        else{
            currentDegree = event.values[0];
            wifi_status_text.setText(Math.round(event.values[0]*10)/10 + " ° ");
        }


        compass_needle.setRotation(-event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
