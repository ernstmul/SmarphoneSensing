package nl.tudelft.smartphonesensing.utils.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by ernstmulders on 12/03/2018.
 */

public class Steps implements SensorEventListener {

    private Context context;
    private TextView steps_status_view;
    private SensorManager mSensorManager;

    private int stepCount;

    /**
     * Initialize barometer
     * @param c context
     * @param statusview homepage output text
     */
    public Steps(Context c, TextView statusview){
        context = c;
        steps_status_view = statusview;
        stepCount = 0;

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
     * returns if the device has the correct sensors
     */
    public boolean hasCorrectSensors(){

        return ((mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 1.0f) {
            stepCount++;
        }
        steps_status_view.setText("Steps: " + stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
