package nl.tudelft.smartphonesensing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.tudelft.smartphonesensing.utils.sensors.Barometer;
import nl.tudelft.smartphonesensing.utils.sensors.Compass;
import nl.tudelft.smartphonesensing.utils.sensors.Steps;
import nl.tudelft.smartphonesensing.utils.sensors.WifiSensor;
import nl.tudelft.smartphonesensing.wifi.LocateActivity;
import nl.tudelft.smartphonesensing.wifi.TrainActivity;

public class HomeActivity extends AppCompatActivity {
    private static String TAG ="HomeActivity";

    private ScrollView home_view;
    private ScrollView wifi_view;
    private ScrollView particles_view;

    private Button refresh_button;
    private Button wifi_train_button;
    private Button wifi_locate_button;

    private WifiSensor wifi;
    private Compass compass;
    private Barometer barometer;
    private Steps steps;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //set them all to invisible
            home_view.setVisibility(View.GONE);
            wifi_view.setVisibility(View.GONE);
            particles_view.setVisibility(View.GONE);

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    home_view.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_wifi:
                    wifi_view.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_particles:
                    particles_view.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //get the views
        home_view = (ScrollView) findViewById(R.id.home_view);
        wifi_view = (ScrollView) findViewById(R.id.wifi_view);
        particles_view = (ScrollView) findViewById(R.id.particles_view);

        // button load
        refresh_button = (Button) findViewById(R.id.refresh_button);
        wifi_train_button = (Button) findViewById(R.id.wifi_train_button);
        wifi_locate_button = (Button) findViewById(R.id.wifi_locate_button);

        //click listener for refresh button
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //refresh the information
                updateInformation();
            }
        });

        //click listener for train button
        wifi_train_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refresh the information
                startTrain();
            }
        });

        //click listener for locate me wifi button
        wifi_locate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //refresh the information
                startWifiLocate();
            }
        });


        //create the sensors
        wifi = new WifiSensor(getApplicationContext());
        compass = new Compass(getApplicationContext(), (TextView) findViewById(R.id.compass_status_text), (ImageView) findViewById(R.id.compass_needle));
        barometer = new Barometer(getApplicationContext(), (TextView) findViewById(R.id.barometer_status_text));
        steps = new Steps(getApplicationContext(), (TextView) findViewById(R.id.steps_status_text));

        //update the status page (home)
        updateInformation();


        //get the navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /**
     * go to start wifi train activity
     */
    public void startTrain(){
        // start LocalizationActivity.class
        Intent myIntent = new Intent(HomeActivity.this,
                TrainActivity.class);
        startActivity(myIntent);
    }

    /**
     * go to start wifi locate activity
     */
    public void startWifiLocate(){
        // start LocalizationActivity.class
        Intent myIntent = new Intent(HomeActivity.this,
                LocateActivity.class);
        startActivity(myIntent);
    }

    /**
     * Updates the information from the sensors and shows it in the home tab
     */
    public void updateInformation(){
        //first check wifi
        TextView wifi_status_label = (TextView) findViewById(R.id.wifi_status_label);
        if(wifi.hasPermission()){
            Log.d(TAG, "Permissions are good");
            wifi_status_label.setBackgroundColor(getResources().getColor(R.color.okBackground));
            wifi_status_label.setText("OK");

            //continue with showing the current stats
            TextView wifi_status_text = (TextView) findViewById(R.id.wifi_status_text);
            wifi_status_text.setText("Number of accesspoints visible: " + wifi.getNumberOfVisibleAccessPoints());


        }
        else{
            Log.d(TAG, "not all permissions");
            //wifi doesn't has all permissions, so show the error
            wifi_status_label.setBackgroundColor(getResources().getColor(R.color.warningBackground));
            wifi_status_label.setText("Not all permissions");
        }

        //compass check for all sensors present
        TextView compass_status_label = (TextView) findViewById(R.id.compass_status_label);
        if(compass.hasCorrectSensors()){
            Log.d(TAG, "Compass is good");
            compass_status_label.setBackgroundColor(getResources().getColor(R.color.okBackground));
            compass_status_label.setText("OK");
        }
        else{
            wifi_status_label.setBackgroundColor(getResources().getColor(R.color.warningBackground));
            wifi_status_label.setText("Not present");
        }

        //barometer check for all sensors present
        TextView barometer_status_label = (TextView) findViewById(R.id.barometer_status_label);
        if(barometer.hasCorrectSensors()){
            Log.d(TAG, "Barometer is good");
            barometer_status_label.setBackgroundColor(getResources().getColor(R.color.okBackground));
            barometer_status_label.setText("OK");
        }
        else{
            barometer_status_label.setBackgroundColor(getResources().getColor(R.color.warningBackground));
            barometer_status_label.setText("Not present");
        }

        //steps check for all sensors present
        TextView steps_status_label = (TextView) findViewById(R.id.steps_status_label);
        if(steps.hasCorrectSensors()){
            Log.d(TAG, "Step counter is good");
            steps_status_label.setBackgroundColor(getResources().getColor(R.color.okBackground));
            steps_status_label.setText("OK");

            //set step count to 0
            steps.resetSteps();
        }
        else{
            steps_status_label.setBackgroundColor(getResources().getColor(R.color.warningBackground));
            steps_status_label.setText("Not present");
        }
    }



}
