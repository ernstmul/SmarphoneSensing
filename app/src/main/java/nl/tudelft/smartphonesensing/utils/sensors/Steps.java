package nl.tudelft.smartphonesensing.utils.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import nl.tudelft.smartphonesensing.particles.ParticlesActivity;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by ernstmulders on 12/03/2018.
 */

public class Steps extends ParticlesActivity implements SensorEventListener {

    private Context context;
    private TextView steps_status_view;
    private SensorManager mSensorManager;
    private boolean is_home; // determines if called from the home activity or the particleActivity

    private int stepCount;

    /**
     * Initialize barometer
     * @param c context
     * @param statusview homepage output text
     */
    public Steps(Context c, TextView statusview, boolean ishome){
        context = c;
        steps_status_view = statusview;
        stepCount = 0;
        is_home = ishome;

        //create sensor manager
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        //and listen to changes
        Sensor pS = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(this, pS, SensorManager.SENSOR_DELAY_GAME);

    }

    /**
     * set number of steps back to 0
     */
    public void resetSteps(){
        stepCount = 0;
        steps_status_view.setText("Steps: " + stepCount);
    }

    /**
     * Unregisters the listerener event
     */
    public void unregisterListener(){
        mSensorManager.unregisterListener(this);
    }

    /**
     * returns if the device has the correct sensors
     */
    public boolean hasCorrectSensors(){

        return ((mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 1.0f) {
            stepCount++; //
        }
        if(is_home){steps_status_view.setText("Steps: " + stepCount + " stepsize:" + (19400 / stepCount));} //
        else{
            Log.d("Steps", "Walking!");
            super.walkingDetected(heading);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
