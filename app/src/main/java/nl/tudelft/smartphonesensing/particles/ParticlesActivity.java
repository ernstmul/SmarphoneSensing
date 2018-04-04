package nl.tudelft.smartphonesensing.particles;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import nl.tudelft.smartphonesensing.R;
import nl.tudelft.smartphonesensing.utils.sensors.Compass;
import nl.tudelft.smartphonesensing.utils.sensors.IMU;
import nl.tudelft.smartphonesensing.utils.sensors.Steps;



/**
 * Created by ernstmulders on 23/03/2018.
 */

public class ParticlesActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static String TAG = "ParticlesActivity";

    //configuration
    private static Integer particlesAmount = 1000;

    // number of particles to refactor:
    // must be less than particlesAmount
    private static int refactorParticlesAmount = 5;
    private static Boolean shouldDrawClosedAreas = false;

    private static Canvas canvas;
    private static List<ShapeDrawable> walls;
    private static List<ShapeDrawable> closed_areas;
    private static List<Particle> particlesList;
    //private List<Particle> particlesOriginalList;
    private static Particle currentLocation;

    //define sensors
    private static Compass compass;
    private static Steps steps;

    // define buttons
    private static Button up,left,right,down,reset,locateMe,sampleIMU,train,walking;

    public static String heading = "";

    // define textview for button pressed and reset status
    private static TextView status;
    private static TextView sampling;

    private static TextView calc_counter;
    private static TextView step_distance;
    private static TextView step_counter;
    private static TextView step_size;
    private static TextView current_floor;

    // manual location (Big red dot) original location on map
    private static int originalLocationX = 500;
    private static int originalLocationY = 220;

    // big red dot's current location
    private static int actualLocationX = originalLocationX;
    private static int actualLocationY = originalLocationY;

    private static int floor;

    //how much millimeters is one step
    private static int stepSize = 700;
    private static int walkedDistanceCm = 0;
    private static int stepCount = 0;

    // floor 3 dimensions in millimeters
    private int floor3Width = 14400;
    private int floor3Height = 26000;

    // floor 4 dimensions in millimeterse
    private int floor4Width = 14400;
    private int floor4Height = 26000;

    private SensorManager mSensorManager;

    private Sensor accelerometer, gyroscope;

    // accelerometer values
    private float aX = 0;
    private float aY = 0;
    private float aZ = 0;

    // gyroscope values
    private float gX = 0;
    private float gY = 0;
    private float gZ = 0;


    private static int screen_width = 0;
    private static int screen_height = 0;

    private static ArrayList<IMU> imuMeasurementsList = new ArrayList<IMU>();
    private static ArrayList<features> featuresList = new ArrayList<features>();

    // IMU sample
    private static boolean busySampling = false;
    private static boolean busyTraining = false;
    private static int normalWalking = 0;

    // walking on stairs boolean
    private static boolean walkingOnStairs = false;
    private static boolean previousWalkingOnStairs = false;
    private static long walkingOnStairsTime = 0;
    //long prevWalkingDetectedTime = 0;

    private static String gyroscopeName;
    private static String accelerometerName;

    private int trainingCount = 0;

    private boolean[] stairsDetectedArray = {false, false, false, false, false, false, false, false, false, false, false, false};

    //private String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particles);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;

        Bundle bundle = getIntent().getExtras();
        floor = bundle.getInt("floor");
        stepSize = bundle.getInt("stepsize");

        // define or set buttons
        up = (Button) findViewById(R.id.buttonUp);
        down = (Button) findViewById(R.id.buttonDown);
        left = (Button) findViewById(R.id.buttonLeft);
        right = (Button) findViewById(R.id.buttonRight);
        reset = (Button) findViewById(R.id.buttonReset);
        locateMe = (Button) findViewById(R.id.buttonLocateMe);
        sampleIMU = (Button) findViewById(R.id.buttonSampleIMU);
        train = (Button) findViewById(R.id.buttonTrain);
        walking = (Button) findViewById(R.id.buttonWalking);

        // set the text views
        calc_counter = (TextView) findViewById(R.id.calc_counter);
        step_distance = (TextView) findViewById(R.id.step_distance);
        step_counter = (TextView) findViewById(R.id.step_counter);
        status = (TextView) findViewById(R.id.textViewStatus);
        sampling = (TextView) findViewById(R.id.textSampling);
        step_size = (TextView) findViewById(R.id.step_size);
        current_floor = (TextView) findViewById(R.id.textCurrentFloor);

        step_size.setText("Size:" + stepSize);


        //intialialize sensors
        compass = new Compass(getApplicationContext(), null, (ImageView) findViewById(R.id.compass_needle), false);
        compass.setButtons(left, right, up, down);

        steps = new Steps(getApplicationContext(), null, false);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists

        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // get names of accelerometers
        String android_id = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);

        if (android_id.equals("9342638c3cbf3199")) {
            gyroscopeName = "3-axis Gyroscope";
            accelerometerName = "3-axis Accelerometer";
        } else {
            gyroscopeName = "L3GD20 Gyroscope";
            accelerometerName = "LIS3DH Accelerometer";
        }
        Log.d(TAG, "device ID: " + android_id);
        Log.d(TAG, "Gyroscope name: " + gyroscopeName);
        Log.d(TAG, "Accelerometer name: " + accelerometerName);

//        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
//            // set accelerometer
//            accelerometer = mSensorManager
//                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            // register 'this' as a listener that updates values. Each time a sensor value changes,
//            // the method 'onSensorChanged()' is called.
//            mSensorManager.registerListener(this, accelerometer,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        } else {
//            // No accelerometer!
//        }
//
//        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
//            // set accelerometer
//            gyroscope = mSensorManager
//                    .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//            // register 'this' as a listener that updates values. Each time a sensor value changes,
//            // the method 'onSensorChanged()' is called.
//            mSensorManager.registerListener(this, gyroscope,
//                    SensorManager.SENSOR_DELAY_NORMAL);
//        } else {
//            // No gyroscope!
//        }

        //listen to steps
       /* mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        //and listen to changes
        Sensor pS = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mSensorManager.registerListener(this, pS, SensorManager.SENSOR_DELAY_GAME);*/

        // set listeners on buttons
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        reset.setOnClickListener(this);
        locateMe.setOnClickListener(this);
        sampleIMU.setOnClickListener(this);
        train.setOnClickListener(this);
        walking.setOnClickListener(this);

        // create a canvas
        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(screen_width,screen_height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        //determine the floor

        if(floor == 3){
            floor3 floor3 = new floor3(screen_width,screen_height,floor3Width,floor3Height);
            walls = floor3.getWalls(screen_width, screen_height);
            closed_areas = floor3.getClosedAreas(screen_width, screen_height);
        }
        else{
            floor4 floor4 = new floor4(screen_width,screen_height);
            walls = floor4.getWalls(screen_width, screen_height);
            closed_areas = floor4.getClosedAreas(screen_width, screen_height);
        }

        current_floor.setText(Integer.toString(floor));



        Log.d(TAG, "closed area count: " + closed_areas.size());

        // draw the objects
        for(ShapeDrawable wall : walls)
            wall.draw(canvas);

        // draw the closed areas if needed
        if(shouldDrawClosedAreas) {
            for (ShapeDrawable closedArea : closed_areas)
                closedArea.draw(canvas);
        }


        //generate all particles and place them on the map
        particlesList = new ArrayList<>();
        Integer particleCount = 0;
        for(particleCount = 0; particleCount < particlesAmount; particleCount++){
            // generate a new particle
            Particle particle = new Particle(canvas, screen_width, screen_height, particleCount);

            // place particle at random position within bounds
            while(isCollision(particle) || isInClosedArea(particle)){
                particle.assignRandomPosition();
            }

            // add to our list of particles
            particlesList.add(particle);
        }

        // Create particle showing current location
        currentLocation = new Particle(canvas, screen_width, screen_height, particleCount + 1);
        status.setText("Start");
        actualLocationX = originalLocationX;
        actualLocationY = originalLocationY;
        currentLocation.defineParticlePosition(actualLocationX, actualLocationY,false);
        // add current location to list of particles so it gets redrawn every time
        particlesList.add(currentLocation);

        // and redraw everything
        //redraw();
        new redraw().execute("");

        Log.d(TAG, "load floor: " + floor);

    }

    /**
     * determines if particle is in a closed area (within an obstacle)
     */
    private boolean isInClosedArea(Particle p){
        //if particles == null, it is not yet drawn, and so its in a closed area
        if(p.particle == null){return true;}

        for(ShapeDrawable closedArea : closed_areas) {
            //Log.d(TAG, "bounds:" + closedArea.getBounds());
            if(isShapeCollision(closedArea,p.particle)){
                //Log.d(TAG, "restricted area collision");
                return true;
            }

        }

        return false;
    }

    @Override
    public void onClick(View v){
        Log.d(TAG, "a button was pressed");

        // check if reset is performed, if so, do not use motion model
        boolean resetPressed = false;
        boolean manualMovePressed = false;
        boolean sampleButtonPressed = false;
        boolean trainButtonPressed = false;

        //Log.d(TAG, "height = " + String.valueOf(screen_height));
        //Log.d(TAG, "width = " + String.valueOf(screen_width));

        //int distanceWalked = distanceWalkedMillimeters*screenwidth/floor4Width;
        int distanceWalkedMillimeters = 500;
        int orientationWalkedDegrees = 0;

        currentLocation.defineParticlePosition(actualLocationX,actualLocationY,false);
        currentLocation.redraw();
        //new redraw().execute("");

        /**
         * Check which button is pressed
         */
        switch (v.getId()) {
            case R.id.buttonUp:{
                status.setText("Up");

                actualLocationY = actualLocationY - distanceWalkedMillimeters*screen_height/floor3Height;
                orientationWalkedDegrees = 180;
                //currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                manualMovePressed = true;
                break;
            }
            case R.id.buttonDown:{
                status.setText("Down");
                actualLocationY = actualLocationY + distanceWalkedMillimeters*screen_height/floor3Height;
                orientationWalkedDegrees = 0;
                //currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                manualMovePressed = true;
                break;
            }
            case R.id.buttonLeft:{
                status.setText("Left");
                actualLocationX = actualLocationX - distanceWalkedMillimeters*screen_width/floor3Width;
                orientationWalkedDegrees = 270;
                //currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                manualMovePressed = true;
                break;
            }
            case R.id.buttonRight:{
                status.setText("Right");
                actualLocationX = actualLocationX + distanceWalkedMillimeters*screen_width/floor3Width;
                orientationWalkedDegrees = 90;
                //currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                manualMovePressed = true;
                break;
            }
            case R.id.buttonReset:{
                resetPressed = true;
                status.setText("Reset");
                //actualLocationX = originalLocationX;
                //actualLocationY = originalLocationY;
                //orientationWalked = 0;
                //currentLocation.defineParticlePosition(actualLocationX, actualLocationY,true);
                break;
            }
            case R.id.buttonLocateMe:{
                applyRANSACLocalization();
                status.setText("Located!");
                break;
            }
            case R.id.buttonSampleIMU:{
                //status.setText("Sampling");
                sampleButtonPressed = true;
                break;
            }
            case R.id.buttonTrain:{
                trainButtonPressed = true;
                break;
            }
            case R.id.buttonWalking:{
                Log.d(TAG, "walking:" + normalWalking);
                if (normalWalking == 0){
                    normalWalking = 1;
                    walking.setText("Stairs UP");
                }
                else if (normalWalking == 1){
                    normalWalking = 2;
                    walking.setText("Stairs DOWN");
                }
                else if (normalWalking == 2){
                    normalWalking = 0;
                    walking.setText("Walking");
                }

//                normalWalking = !normalWalking;
//                if (normalWalking){
//                    walking.setText("normal");
//                } else {
//                    walking.setText("stairs");
//                }
                Log.d(TAG, "we are training for normal walking?: " + Integer.toString(normalWalking));
                break;
            }
        }

        /**
         * if UP, DOWN, LEFT, RIGHT is pressed
         */
        if (manualMovePressed) {
            // apply movement as specified by manual input
            calculateParticlesPosition(distanceWalkedMillimeters,orientationWalkedDegrees );
        }

        /**
         * if RESET is pressed
         */
        if (resetPressed){
            // if reset IS pressed, put particles on map (probability)
            // do reset of particles
            // generate all particles and place them on the map
            for(Integer particleCount = 0; particleCount < particlesAmount; particleCount++){
                // generate a new particle
                Particle particle = new Particle(canvas, screen_width, screen_height, particleCount);

                // place particle at random position within bounds
                while(isCollision(particle) || isInClosedArea(particle)){
                    particle.assignRandomPosition();
                }

                // update our list of particles
                particlesList.set(particleCount, particle);
            }

            // reset distance walked and step count
            walkedDistanceCm = 0;
            stepCount = 0;

            // update text views
            step_distance.setText("0cm");
            step_counter.setText("0 steps");

            // redraw on a separate thread
            new redraw().execute("");
        }

        /**
         * If SAMPLE is pressed - used to
         */
        if (sampleButtonPressed){

            // toggle boolean
            busySampling = !busySampling;

            if (busySampling){
                // what to do when sampling
                sampling.setText("Now Sampling");
            } else {
                // what to do when not sampling
                sampling.setText("Finished sampling");
                Log.d(TAG, "Measurement list size: " + Integer.toString(imuMeasurementsList.size()));
                if (imuMeasurementsList.size() > 0){
                    writeIMUDataToCsv(getApplicationContext());
                }
            }
        }

        /**
         * If TRAIN is pressed
         */
        if (trainButtonPressed){
            busyTraining = !busyTraining;
            if (busyTraining) {
                trainingCount++;
                Log.d(TAG, "busy training");
                sampling.setText("Training started");
            } else {
                // stop training and write CSV files
                Log.d(TAG, "training has been stopped");
                Log.d(TAG, "features list size: " + Integer.toString(featuresList.size()));
                if (featuresList.size() > 0){
                    //Log.d(TAG, "writing CSV of feature lists");
                    writeFeatureDataToCsv(getApplicationContext());
                }
                sampling.setText("Training stopped");
            }
        }

        //Log.d(TAG, "get here");
    }

    @Override
    public void onBackPressed(){
        //reset stepcount
        stepCount = 0;

        //unregister sensors
        Log.d(TAG, "unregister sensors");
        steps.unregisterListener();
        mSensorManager.unregisterListener(this);

        super.onBackPressed();
    }

    /**
     * uses a RANSAC algorithm to determine the most probable current location based on the particle
     * spread utilizing a circular location model
     */
    private void applyRANSACLocalization(){
        int[][] particleLocations = new int[2][particlesAmount];

        currentLocation.defineParticlePosition(currentLocation.getX(),currentLocation.getY(),false);
        currentLocation.redraw();

        // fill array with particle locations
        for (int idx = 0;idx < particlesAmount;idx++){
            particleLocations[0][idx] = particlesList.get(idx).getX();
            particleLocations[1][idx] = particlesList.get(idx).getY();
        }

        // RANSAC
        int rounds = 52;
        int NinliersMax = 0;
        int idxMax = 0;
        int distThreshold = 100; // pixels
        distThreshold = distThreshold*distThreshold;

        // try find the perfect particle 'round' times
        for (int iter = 0; iter < rounds; iter++){

            // choose random index
            int randomIdx = ThreadLocalRandom.current().nextInt(0,particlesAmount-1);

            int currentParticleX = particlesList.get(randomIdx).getX();
            int currentParticleY = particlesList.get(randomIdx).getY();

            particlesList.get(randomIdx).changeColor();

            int Ninliers = 0;



            for (int i = 0; i < particlesAmount; i++){

                //Log.d(TAG, "partLoc[0][i]: " + Integer.toString(particleLocations[0][i]) + "    partLoc[1][i]: " + Integer.toString(particleLocations[1][i]));
                //Log.d(TAG, "current X : " + Integer.toString(currentParticleX) + "    current Y : " + Integer.toString(currentParticleY));

                //Log.d(TAG, "X : " + Double.toString(Math.pow( (double) (particleLocations[0][i] - currentParticleX),2.0)) + "    Y : " + Double.toString(Math.pow( (double)(particleLocations[1][i] - currentParticleY),2)));

                int dist =  (int) Math.round(Math.pow( (particleLocations[0][i] - currentParticleX),2.0) + Math.pow((particleLocations[1][i] - currentParticleY),2));
                //Log.d(TAG, "dist: " + Integer.toString(dist));
                if (dist<distThreshold){

                    Ninliers++;
                }
            }

            //Log.d(TAG, "Ninliers: " + Integer.toString(Ninliers) + "   NinliersMax: " +  Integer.toString(NinliersMax) + "    RandomIdx: " + Integer.toString(randomIdx) + "    idxMax: " + Integer.toString(idxMax));


            if (Ninliers > NinliersMax){
                NinliersMax = Ninliers;
                idxMax = randomIdx;
            }
        }

        // Determine centroid using particles around best fitting particle

        int maxIdxParticleX = particlesList.get(idxMax).getX();
        int maxIdxParticleY = particlesList.get(idxMax).getY();

        // log all inliers to an array
        ArrayList<Integer> inliersX = new ArrayList<>();
        ArrayList<Integer> inliersY = new ArrayList<>();

        for (int i = 0; i < particlesAmount; i++) {
            int dist =  (int) Math.round(Math.pow( (particleLocations[0][i] - maxIdxParticleX),2.0) + Math.pow((particleLocations[1][i] - maxIdxParticleY),2));
            //Log.d(TAG, "dist: " + Integer.toString(dist));

            // check if distance is within a specified threshold
            if (dist<distThreshold){

                // if so, add this particle to list of inliers
                inliersX.add(particleLocations[0][i]);
                inliersY.add(particleLocations[1][i]);
            }
        }

        int centroidX = 0;
        int centroidY = 0;

        for(int i = 0; i < inliersX.size(); i++){
            centroidX = centroidX + inliersX.get(i);
            centroidY = centroidY + inliersY.get(i);
        }

        centroidX = centroidX/NinliersMax;
        centroidY = centroidY/NinliersMax;

        currentLocation.defineParticlePosition(centroidX,centroidY,true);

        //new redraw().execute("");
        // make sure red dot is on top
        currentLocation.redraw();
    }

    /**
     * apply noisy motion model to particles given a distance and orientation input
     * @param distanceWalkedMillimeters
     * @param orientationWalkedDegrees
     */
    private void calculateParticlesPosition(int distanceWalkedMillimeters, int orientationWalkedDegrees){
        long starttime = System.currentTimeMillis();
        // variance of orientation and distance
        int distanceVariance = 300; // 5 pixel variance
        int orientationVariance = 10; // 45 degrees orientation variance

        // init distance and orientation variables which include noise
        int noisyDistanceWalkedMillimeters;
        double noisyOrientationWalkedDegrees;

        int noisyDistanceWalkedPixelsX;
        int noisyDistanceWalkedPixelsY;

        ArrayList<Particle> survived = new ArrayList<>();
        ArrayList<Particle> dead = new ArrayList<>();

        // loop through particle list to apply motion model
        for (int particleIdx = 0; particleIdx < particlesAmount; particleIdx++) {

            // currentParticle is the particle at its current location (no motion applied yet)
            Particle currentParticle = particlesList.get(particleIdx);
            int initX = currentParticle.getX();
            int initY = currentParticle.getY();

            // create random variables and define (Gaussian) noisy distance and orientation (based on variances defined at OnClick)
            Random distanceRandom = new Random();
            Random orientationRandom = new Random();

            // create distance
            noisyDistanceWalkedMillimeters = distanceWalkedMillimeters + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);
            noisyOrientationWalkedDegrees = (double) orientationWalkedDegrees + orientationRandom.nextGaussian()*orientationVariance;

            int noisyDistanceWalkedMillimetersX = (int) Math.round(noisyDistanceWalkedMillimeters*Math.sin(Math.toRadians(noisyOrientationWalkedDegrees)));
            int noisyDistanceWalkedMillimetersY = (int) Math.round(noisyDistanceWalkedMillimeters*Math.cos(Math.toRadians(noisyOrientationWalkedDegrees)));

            //Log.d(TAG, "noisyX" + noisyDistanceWalkedMillimetersX + " noisyY:" + noisyDistanceWalkedMillimetersY);

            if(floor == 3){
                noisyDistanceWalkedPixelsX = noisyDistanceWalkedMillimetersX*screen_width/floor3Width;
                noisyDistanceWalkedPixelsY = noisyDistanceWalkedMillimetersY*screen_height/floor3Height;
            }
            else{
                noisyDistanceWalkedPixelsX = noisyDistanceWalkedMillimetersX*screen_width/floor4Width;
                noisyDistanceWalkedPixelsY = noisyDistanceWalkedMillimetersY*screen_height/floor4Height;
            }

            //int noisyDistanceWalkedPixlesY = distanceWalkedPixelsY + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);

            //noisyDistanceWalked = distanceWalked + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);

            // noisyOrientationWalkedDegrees = (double) orientationWalkedDegrees + orientationRandom.nextGaussian()*orientationVariance;

            // create new Particle which represents moved particle position
            Particle movedParticle = new Particle(canvas,screen_width,screen_height, particleIdx);

            // find new x and y coordinates of moved particle and define the movedParticle
            //int moveX = - (int) Math.round(noisyDistanceWalked*Math.sin(Math.toRadians(noisyOrientationWalked)));
            //int moveY = (int) Math.round(noisyDistanceWalked*Math.cos(Math.toRadians(noisyOrientationWalked)));
            int newX = initX + noisyDistanceWalkedPixelsX;
            int newY = initY + noisyDistanceWalkedPixelsY;

            movedParticle.defineParticlePosition(newX, newY, false);

            /**
             *  Summary - we have:
             *  currentParticle - represents original particle position
             *  movedParticle - represents original particle moved with motion model
             */

            // if movedParticle and trajectory from currentParticle violates obstacle boundaries,
            // define new currentParticle based on random other particle in particlesList

            // first try current particle and apply motion model
            int randomParticleIdx = particleIdx;

            // stop stuck while loop in case it gets stuck (typical when particleCount is small)
           /* int counter = 0;
//isCollision(movedParticle) || isInClosedArea(movedParticle) ||isCollisionTrajectory(movedParticle, currentParticle)
            while(( isCollisionTrajectory(movedParticle, currentParticle)) && counter<50){
                Log.d(TAG, "Particle" + particleIdx + " is in CollisionTrajectory");
                // redefine current particle and moved particle from random index in particlesList
                randomParticleIdx = ThreadLocalRandom.current().nextInt(0, particlesAmount-1);
                currentParticle = particlesList.get(randomParticleIdx);

                // apply motion model to this particle
                initX = currentParticle.getX();
                initY = currentParticle.getY();
                newX = initX + noisyDistanceWalkedPixelsX;
                newY = initY + noisyDistanceWalkedPixelsY;
                movedParticle.defineParticlePosition(newX, newY,false);

                // increase counter
                counter++;
            }*/



            //check if new particle is dead
            if(isCollisionTrajectory(movedParticle, currentParticle)){
                dead.add(movedParticle);
            }
            else{
                //its not, so add to the alive list
                survived.add(movedParticle);
            }


            // update particleList
            particlesList.set(particleIdx,movedParticle);


        }



        //reassign the dead particles to an alive particle
        int randomParticleIdx;
        for(Particle deadParticle : dead){
            //get a random particle that survived
            randomParticleIdx = ThreadLocalRandom.current().nextInt(0, survived.size() - 1);
            Particle survivedParticle = survived.get(randomParticleIdx);

            //copy its location to the dead particle
            deadParticle.defineParticlePosition(survivedParticle.getX(), survivedParticle.getY(), false);

            //and put it in the correct place in the list
            particlesList.set(deadParticle.getIndex(), deadParticle);
        }

        /***
         * Refactor particles
         *
         * This means re-assigning random particles to random map positions
         *
         * The idea is to allow for alternative particle positions in case all particles converge
         * to the wrong location
         */


        for (int refactorIdx = 0; refactorIdx < refactorParticlesAmount; refactorIdx++){

            // get random index within particle amount
            int randomIdx = ThreadLocalRandom.current().nextInt(0, particlesAmount-1);

            // update new particles
            Particle newRandomParticle = new Particle(canvas,screen_width,screen_height, randomIdx);

            while(isCollision(newRandomParticle) || isInClosedArea(newRandomParticle)){
                newRandomParticle.assignRandomPosition();
            }

            // replace current particle at randomIdx with this new, random, refactored particle
            particlesList.set(randomIdx,newRandomParticle);
        }

       // Log.d(TAG, "redraw call 2");
        new redraw().execute("");

        long taken = System.currentTimeMillis() - starttime;
        //Log.d(TAG, "it took me:" + taken + "ms - list size:" + particlesList.size());

        calc_counter.setText(taken + "ms");
    }

    /**
     * Updates states when walking is detected
     */
    public void walkingDetected(){
        Log.d(TAG, "I've made a step");
        int distanceWalkedMillimeters = 1 * stepSize;   //1 = because the function is called for every step

        walkedDistanceCm = walkedDistanceCm + (distanceWalkedMillimeters / 10);
        stepCount++;

        step_distance.setText(walkedDistanceCm + "cm");
        step_counter.setText(stepCount + "steps");

        // do not create red dot for estimated current location
        currentLocation.defineParticlePosition(currentLocation.getX(),currentLocation.getY(),false);

        String direction = heading;
        // Log.d(TAG, "heading:" + heading + ";");
        int directionInt = 0;

        switch(direction){
            case "up":
                directionInt = 180;
                break;
            case "right":
                directionInt = 90;
                break;
            case "down":
                directionInt = 0;
                break;
            case "left":
                directionInt = 270;
        }

        //make the calculations
        if (!busyTraining) {
            calculateParticlesPosition(distanceWalkedMillimeters, directionInt);
        }



        // if we are training, add to features list
        // if not busy training, just analyze data
        if (busyTraining) {
            //Log.d(TAG, "--------------------------------------------");
            //Log.d(TAG, "busy training here! (within walking detected)");
            // get features
            if (stepCount <= 1) {
                // first step, clear list
                featuresList.clear();
                imuMeasurementsList.clear();
                //Log.d(TAG,"step count <= 1: clearing lists");
            } else {
                //Log.d(TAG, "step count >= 1: writing features to list");
               // Log.d(TAG, "length of imuMeasurementsList before clear: " + Integer.toString(imuMeasurementsList.size()));
                featuresList.add(getSVMFeatures(imuMeasurementsList));
               // Log.d(TAG, "length of featuresList: " + Integer.toString(featuresList.size()));
                imuMeasurementsList.clear();
               // Log.d(TAG, "length of imuMeasurementsList after clear: " + Integer.toString(imuMeasurementsList.size()));
            }
            // add to featureslist

        } else {
            // determine if we are walking normally or on up/down stairs
            //Log.d(TAG, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
           // Log.d(TAG, "determine if we are walking or stairs! (within walking detected)");

            if (stepCount <= 1){
                //featuresList.clear();
                imuMeasurementsList.clear();
               // Log.d(TAG,"step count <= 1: clearing lists");
            } else {
               // Log.d(TAG, "step count >= 1: writing features to list");
               // Log.d(TAG, "length of imuMeasurementsList before clear: " + Integer.toString(imuMeasurementsList.size()));

                features currentFeatures = getSVMFeatures(imuMeasurementsList);

                // get 2 PCA components
                PCA twoPCAComponents = applyPCAreduction(currentFeatures);

                // am I walking on stairs?
                walkingOnStairs = amIWalkingOnStairs(twoPCAComponents);
                if (walkingOnStairs) {
                    sampling.setText("Walking on stairs!");
                } else {
                    sampling.setText("Walking normally!");
                }

                // array of previous stairs detections
                int lengthStairsArray = stairsDetectedArray.length;

                // shift values up
                for (int i = 0; i < lengthStairsArray-1; i++ ){
                    stairsDetectedArray[i] = stairsDetectedArray[i+1];
                }
                // update last entry of array
                stairsDetectedArray[lengthStairsArray-1] = walkingOnStairs;

                // count number of true values in array
                int numberOfStairsDetected = 0;
                for (int i = 0; i<lengthStairsArray;i++){
                    if (stairsDetectedArray[i]) {
                        numberOfStairsDetected++;
                    }
                }
                // if 8 out of 12 (length of stairsDetectedArray) is true, we switch floors
                if((numberOfStairsDetected > 4) && System.currentTimeMillis() > walkingOnStairsTime + 30000){
                    //switch floor
                    walkingOnStairsTime = System.currentTimeMillis();

                    if(floor == 3){
                        floor4 floor4 = new floor4(screen_width,screen_height);
                        walls = floor4.getWalls(screen_width, screen_height);
                        closed_areas = floor4.getClosedAreas(screen_width, screen_height);
                        floor = 4;
                    }
                    else{
                        floor3 floor3 = new floor3(screen_width,screen_height,floor3Width,floor3Height);
                        walls = floor3.getWalls(screen_width, screen_height);
                        closed_areas = floor3.getClosedAreas(screen_width, screen_height);
                        floor = 3;
                    }

                    current_floor.setText(Integer.toString(floor));
                }

                previousWalkingOnStairs = walkingOnStairs;

               // Log.d(TAG, " am I walking on stairs??????: " + Boolean.toString(walkingOnStairs));

               // Log.d(TAG, "length of featuresList: " + Integer.toString(featuresList.size()));
                imuMeasurementsList.clear();
               // Log.d(TAG, "length of imuMeasurementsList after clear: " + Integer.toString(imuMeasurementsList.size()));
            }
        }
    }

    /**
     * Determines if the drawable dot intersects with any of the walls.
     * @return True if that's true, false otherwise.
     */
    private boolean isCollision(Particle p) {
        //if particles == null, it is not yet drawn, and so its a collisions (since it needs a redraw)
        if(p.particle == null){return true;}

        for(ShapeDrawable wall : walls) {
            if(isShapeCollision(wall,p.particle)){
                return true;
            }

        }
        return false;
    }

    /**
     * Determines if the trajectory between a particle's old and new position intersects with a wall
     * or obstacle area
     *
     * @param newPosition - position of particle at new position
     * @param oldPosition - position of particle at original position
     * @return True if the rectangle spanning the old and new particle position intersect with a wall
     */
    private boolean isCollisionTrajectory(Particle newPosition, Particle oldPosition){

        int newX = newPosition.getX();
        int newY = newPosition.getY();

        int oldX = oldPosition.getX();
        int oldY = oldPosition.getY();

        int left, right, top, bottom;

        // check x values
        if (newX <= oldX){
            left = newX;
            right = oldX;
        } else { //if (newX > oldX)
            left = oldX;
            right = newX;
        } /*else {
            // if equal, add extra width to ensure rect does not have 0 area
            //  - possibly not necessary?
            left = newX;
            right = oldX + 1;
        }*/

        // check y values
        if (newY <= oldY){
            top = newY;
            bottom = oldY;
        } else { // if (newY > oldY)
            top = oldY;
            bottom = newY;
        } /*else {
            // if equal, add extra height to ensure rect does not have 0 area
            //  - possibly not necessary?
            top = newY;
            bottom = oldY + 1;
        }*/

        // create trajectory rectangle between two particles
        Rect trajectoryRect = new Rect(left, top, right, bottom);

        // check if this trajectory crosses any walls
        for(ShapeDrawable wall : walls) {
            if (trajectoryRect.intersect(wall.getBounds())){
                // trajectory rectangle does intersect with a wall - collision = true
                return true;
            }
        }
        return false;
    }

    /**
     * read IMU sensors. Also specify sampling rate (200 Hz)
     */
    protected void onResume() {
        super.onResume();
        // SENSOR_DELAY_NORMAL  - 100ms (  10 Hz) sampling
        // SENSOR_DELAY_UI      -  60ms (  16 Hz) sampling
        // SENSOR_DELAY_GAME    -  20ms (  50 Hz) sampling
        // SENSOR_DELAY_FASTEST -   5ms ( 200 Hz) sampling
        mSensorManager.registerListener(this, accelerometer, mSensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /**
     * Determines if two shapes intersect.
     * @param first The first shape.
     * @param second The second shape.
     * @return True if they intersect, false otherwise.
     */
    private boolean isShapeCollision(ShapeDrawable first, ShapeDrawable second) {
        Rect firstRect = new Rect(first.getBounds());
        return firstRect.intersect(second.getBounds());
    }

    /**
     * Writes the IMU data to a CSV file so that it can be analyzed
     * @param context
     */
    private void writeIMUDataToCsv(Context context) {
        StringBuffer csvText = new StringBuffer("");

        for (int i = 0; i < imuMeasurementsList.size(); i++) {
            csvText.append(Float.toString(imuMeasurementsList.get(i).getAccX()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getAccY()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getAccZ()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getGyroX()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getGyroY()));
            csvText.append(",");
            csvText.append(Float.toString(imuMeasurementsList.get(i).getGyroZ()));
            csvText.append(",");
            csvText.append(Long.toString(imuMeasurementsList.get(i).getTimeStamp()));
            csvText.append("\n");
        }

        // remove last \n - necessary?
        csvText.setLength(csvText.length()-1);

        //create the file
        try {
            if(context == null){
                Log.d(TAG, "Context is null");
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("dataIMUData08.csv", context.MODE_PRIVATE));
            outputStreamWriter.write(csvText.toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Write Features to CSV file
     * @param context - application context
     */
    private void writeFeatureDataToCsv(Context context){
        StringBuffer csvProcessed = new StringBuffer("");
        Log.d(TAG, "writing features CSV file");

        String csvFeaturesFileName = "dataFeatures" + Integer.toString(trainingCount) + ".csv";
        Log.d(TAG, "csv Features file name: " + csvFeaturesFileName);

        //features currentFeatures = getSVMFeatures(imuMeasurementsList);

        for (int idx = 0; idx < featuresList.size(); idx++) {
            csvProcessed.append(Double.toString(featuresList.get(idx).getX1()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX2()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX3()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX4()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX5()));
            csvProcessed.append(",");
            csvProcessed.append(Double.toString(featuresList.get(idx).getX6()));
            csvProcessed.append(",");
            csvProcessed.append(Integer.toString(featuresList.get(idx).walkingOnStairs()));
            csvProcessed.append("\n");
        }

        //create the file
        try {
            if(context == null){
                Log.d(TAG, "Context is null");
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(csvFeaturesFileName, context.MODE_PRIVATE));
            outputStreamWriter.write(csvProcessed.toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * read IMU (accelerometer and gyroscope) values to imuMeasurementsList
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG, "sensor change called!!!");
          //  walkingDetected();
        String sensorName = event.sensor.getName();
        long timeInMillis = 0;

        //Log.d(TAG, sensorName);
        if (sensorName.equals(gyroscopeName)){ //3-axis Gyroscope
            //Log.d(TAG, "gyro!!!!");
            gX = event.values[0];
            gY = event.values[1];
            gZ = event.values[2];
        }

        if (sensorName.equals(accelerometerName)){ //3-axis Accelerometer
            //Log.d(TAG, "accelerometer!");
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];

            // only add to list once, otherwise get same measurement twice

            timeInMillis = (new Date()).getTime()
                    + (event.timestamp - System.nanoTime()) / 1000000L;


            // if sampling at this point, add to arraylist
            if (stepCount>0 || busySampling){     // && (timeInMillis<prevWalkingDetectedTime+5000)){
                IMU newMeasurement = new IMU(aX,aY,aZ,gX,gY,gZ,timeInMillis);
                imuMeasurementsList.add(newMeasurement);
                //Log.d(TAG, "imuMeasurementList onSensorChange: " + Integer.toString(imuMeasurementsList.size()));
            }
            //Log.d(TAG, "aX: " + Float.toString(aX) + "  aY: " + Float.toString(aY) + "  aZ: " + Float.toString(aZ) + "  gX: " + Float.toString(gX) + "  gY: " + Float.toString(gY) + "  gZ: " + Float.toString(gZ) + "  time: " + Long.toString(timeInMillis));
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * redraw, regenerate the canvas
     */
    private class redraw extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //Log.d(TAG, "I'm redrawing");

            // redrawing of the object
            canvas.drawColor(Color.WHITE);

            for(ShapeDrawable wall : walls)
                wall.draw(canvas);

            for(Particle particle : particlesList){
                particle.redraw();
            }

            //redraw the closed areas if needed
            if(shouldDrawClosedAreas) {
                for (ShapeDrawable closedArea : closed_areas)
                    closedArea.draw(canvas);
            }

            return "drawn";
        }

        @Override
        protected void onPostExecute(String result) {
            //TextView txt = (TextView) findViewById(R.id.output);
            //txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    /**
     * Extracts the SVM features from an ArrayList of IMU measurements
     *
     * @param imuMeasurement
     * @return features obtained from the imuMeasurement list
     */
    private features getSVMFeatures(ArrayList<IMU> imuMeasurement){
        //double feature1,feature2,feature3,feature4,feature5,feature6;

        // init first value of imuMeasurement
        double valueAccX = 0; // = imuMeasurement.get(0).getAccX();
        double valueAccY = 0; //imuMeasurement.get(0).getAccY();
        double valueAccZ = 0; //imuMeasurement.get(0).getAccZ();
        double valueGyroY = 0; //imuMeasurement.get(0).getGyroY();

        double meanEnergyXZ = 0.0;
        double meanAccY = 0.0;
        double meanAccZ = 0.0;

        double maxGyroY = 0;

        double varianceAccY = 0.0;
        double varianceEnergyXZ = 0.0;
        double varianceAccZ = 0.0;

        double smoothingFactor = 1; // represents LPF of 20 Hz

        ArrayList<Double> filteredAccY = new ArrayList<>();
        ArrayList<Double> filteredAccZ = new ArrayList<>();
        ArrayList<Double> energyXZ = new ArrayList<>();

        // loop through all parameters to apply LPF and get MEAN

        int measurementCount = imuMeasurement.size();

        for (int idx = 0; idx<measurementCount;idx++){

            // only apply to gyroY and accX,Y,Z

            // apply LPF
            valueAccX = valueAccX + (imuMeasurement.get(idx).getAccX()-valueAccX)/smoothingFactor;
            valueAccY = valueAccY + (imuMeasurement.get(idx).getAccY()-valueAccY)/smoothingFactor;
            valueAccZ = valueAccZ + (imuMeasurement.get(idx).getAccZ()-valueAccZ)/smoothingFactor;
            valueGyroY = valueGyroY + (imuMeasurement.get(idx).getGyroY()-valueGyroY)/smoothingFactor;

            // create new list
            filteredAccY.add(valueAccY);
            filteredAccZ.add(valueAccZ);
            energyXZ.add(Math.pow(valueAccX,2) + Math.pow(valueAccZ,2));

            if (Math.abs(valueGyroY) > Math.abs(maxGyroY)){
                maxGyroY = valueGyroY;
            }

            // determine mean so long

            meanEnergyXZ = meanEnergyXZ + Math.pow(valueAccX,2) + Math.pow(valueAccZ,2);
            meanAccY = meanAccY + valueAccY;
            meanAccZ = meanAccZ + valueAccZ;
        }

        meanAccZ = meanAccZ/measurementCount;
        meanAccY = meanAccY/measurementCount;
        meanEnergyXZ = meanEnergyXZ/measurementCount;

        // determine variance

        for (int idx = 0; idx<measurementCount;idx++) {
            varianceAccY = varianceAccY + Math.pow((filteredAccY.get(idx) - meanAccY),2);
            varianceAccZ = varianceAccZ + Math.pow((filteredAccZ.get(idx) - meanAccZ),2);
            varianceEnergyXZ = varianceEnergyXZ + Math.pow((energyXZ.get(idx) - meanEnergyXZ),2);
        }

        // final calculations

        //meanAccZ = meanAccZ/measurementCount;
        varianceAccY = varianceAccY/measurementCount;
        varianceAccZ = varianceAccZ/measurementCount;
        varianceEnergyXZ = varianceEnergyXZ/measurementCount;

        /**
         * Assign variables to features
         */
        // check if we are training "normal walking" or "stairs walking" from button

        // return features object
        features featureVals = new features(maxGyroY,varianceAccY,meanEnergyXZ,varianceEnergyXZ,varianceAccZ,meanAccZ,normalWalking);
        //Log.d(TAG, "mean Acc Z: " + Double.toString(meanAccZ)); // YES
        //Log.d(TAG, "measurement Count: " + Double.toString(measurementCount)); // YES
        //Log.d(TAG,"mean Acc Y: " + Double.toString(meanAccY)); // YES
        //Log.d(TAG, "energy mean: " + Double.toString(meanEnergyXZ));
        //Log.d(TAG,"mean Acc X: " + Double.toString(meanAccX));

        return featureVals;
    }

    /**
     * Apply a Principal Component Analysis (PCA) to the feature set to obtain the two main components
     * @param currentFeatures containing the features of the current measurement
     * @return PCA object containing two main PCA components
     */
    private PCA applyPCAreduction(features currentFeatures){

        // define mean
        double[] meanVec = {-0.0547,0.5001,101.388,1933.3,3.9856,9.8310};

        // subtract mean from features
        double X1_no_mean = currentFeatures.getX1() - meanVec[0];
        double X2_no_mean = currentFeatures.getX2() - meanVec[1];
        double X3_no_mean = currentFeatures.getX3() - meanVec[2];
        double X4_no_mean = currentFeatures.getX4() - meanVec[3];
        double X5_no_mean = currentFeatures.getX5() - meanVec[4];
        double X6_no_mean = currentFeatures.getX6() - meanVec[5];

        // define W matrix
        double[][]W_transpose =  {{0.0001,    0.0000 ,   0.0031 ,   1.0000,    0.0019,    0.0001},
                                  {0.0208,   -0.0013,   -0.9967 ,   0.0030,    0.0565,   -0.0546}};

        // muliply by W matrix
        double PCA1 = X1_no_mean*W_transpose[0][0] + X2_no_mean*W_transpose[0][1] + X3_no_mean*W_transpose[0][2] + X4_no_mean*W_transpose[0][3] + X5_no_mean*W_transpose[0][4] + X6_no_mean*W_transpose[0][5];
        double PCA2 = X1_no_mean*W_transpose[1][0] + X2_no_mean*W_transpose[1][1] + X3_no_mean*W_transpose[1][2] + X4_no_mean*W_transpose[1][3] + X5_no_mean*W_transpose[1][4] + X6_no_mean*W_transpose[1][5];

        PCA currentPCAcomponents = new PCA(PCA1,PCA2);
        return currentPCAcomponents;
    }

    /**
     * Determine decision value of a PCA component object (containing 2 PCA components)
     * @param currentPCAcomponent
     * @return
     */
    private boolean amIWalkingOnStairs(PCA currentPCAcomponent){
        // SVM equation parameters
        double w1 = 0.0032;
        double w2 = 0.1021;
        double bias = 0;//5.1149;

        double decision = w1*currentPCAcomponent.getPCA1() + w2*currentPCAcomponent.getPCA2() + bias;
        Log.d(TAG, "Decision value is: " + Double.toString(decision));
        return (decision > 0);
    }

    /**
     * feature object of the IMU data
     * contains:    X1 to X6 which are the 6 features used for walking classification a
     *              stairs boolean - if feature is for normal walking or climbing stairs
     */
    private class features{
        private double X1,X2,X3,X4,X5,X6;

        private int stairs;

        features(double X1,double X2,double X3,double X4,double X5,double X6, int stairs){
            this.X1 = X1;
            this.X2 = X2;
            this.X3 = X3;
            this.X4 = X4;
            this.X5 = X5;
            this.X6 = X6;
            this.stairs = stairs;
        }

        public double getX1(){
            return this.X1;
        }

        public double getX2(){
            return this.X2;
        }

        public double getX3(){
            return this.X3;
        }

        public double getX4(){
            return this.X4;
        }

        public double getX5(){
            return this.X5;
        }

        public double getX6(){
            return this.X6;
        }

        public int walkingOnStairs() {return this.stairs; }
    }

    /**
     * class for first 2 PCA components
     */
    private class PCA{
        private double PCA1, PCA2;

        PCA(double PCA1, double PCA2){
            this.PCA1 = PCA1;
            this.PCA2 = PCA2;
        }

        public double getPCA1() { return this.PCA1;}

        public double getPCA2() { return this.PCA2;}

    }

}