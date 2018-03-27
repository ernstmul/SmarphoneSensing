package nl.tudelft.smartphonesensing.particles;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import nl.tudelft.smartphonesensing.R;
import nl.tudelft.smartphonesensing.utils.sensors.Compass;
import nl.tudelft.smartphonesensing.utils.sensors.Steps;


/**
 * Created by ernstmulders on 23/03/2018.
 */

public class ParticlesActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "ParticlesActivity";

    //configuration
    private static Integer particlesAmount = 1000;

    // number of particles to refactor:
    // must be less than particlesAmount
    private static int refactorParticlesAmount = 5;
    private static Boolean shouldDrawClosedAreas = false;

    private Canvas canvas;
    private List<ShapeDrawable> walls;
    private List<ShapeDrawable> closed_areas;
    private List<Particle> particlesList;
    //private List<Particle> particlesOriginalList;
    private Particle currentLocation;

    //define sensors
    private Compass compass;
    private Steps steps;

    // define buttons
    private Button up,left,right,down,reset;

    // define textview for button pressed and reset status
    private TextView textStatus;

    // manual location (Big red dot) original location on map
    private int originalLocationX = 500;
    private int originalLocationY = 220;

    // big red dot's current location
    private int actualLocationX = originalLocationX;
    private int actualLocationY = originalLocationY;


    //how much centimeter is one step
    private int stepSize = 74;

    // floor 3 dimensions in millimeters
    private int floor3Width = 14400;
    private int floor3Height = 26000;

    // floor 4 dimensions in millimeterse
    private int floor4Width = 14400;
    private int floor4Height = 26000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particles);

        // get the screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // define or set buttons
        up = (Button) findViewById(R.id.buttonUp);
        down = (Button) findViewById(R.id.buttonDown);
        left = (Button) findViewById(R.id.buttonLeft);
        right = (Button) findViewById(R.id.buttonRight);
        reset = (Button) findViewById(R.id.buttonReset);


        //intialialize sensors
        compass = new Compass(getApplicationContext(), null, (ImageView) findViewById(R.id.compass_needle), false);
        compass.setButtons(left, right, up, down);

        steps = new Steps(getApplicationContext(), null, false);


        // set listeners on buttons
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        reset.setOnClickListener(this);

        // set the text view
        textStatus = (TextView) findViewById(R.id.textViewStatus);

        // create a canvas
        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        //determine the floor
        Bundle bundle = getIntent().getExtras();
        Integer floor = bundle.getInt("floor");

        if(floor == 3){
            floor3 floor3 = new floor3(width,height,floor3Width,floor3Height);
            walls = floor3.getWalls(width, height);
            closed_areas = floor3.getClosedAreas(width, height);
        }
        else{
            floor4 floor4 = new floor4(width,height);
            walls = floor4.getWalls(width, height);
            closed_areas = floor4.getClosedAreas(width, height);
        }

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
        for(Integer particleCount = 0; particleCount < particlesAmount; particleCount++){
            // generate a new particle
            Particle particle = new Particle(canvas, width, height);

            // place particle at random position within bounds
            while(isCollision(particle) || isInClosedArea(particle)){
                particle.assignRandomPosition();
            }

            // add to our list of particles
            particlesList.add(particle);
        }

        // Create particle showing current location
        currentLocation = new Particle(canvas, width, height);
        textStatus.setText("Start");
        actualLocationX = originalLocationX;
        actualLocationY = originalLocationY;
        currentLocation.defineParticlePosition(actualLocationX, actualLocationY,true);
        // add current location to list of particles so it gets redrawn every time
        particlesList.add(currentLocation);

        // and redraw everything
        redraw();

        Log.d(TAG, "load floor: " + floor);

    }

    /**
     * redraw, regenerate the canvas
     */
    private void redraw(){

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
        Log.d(TAG, "button pressed: red dot moved");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenwidth = size.x;
        int screenheight = size.y;

        // check if reset is performed, if so, do not use motion model
        boolean reset = false;

        Log.d(TAG, "height = " + String.valueOf(screenheight));
        Log.d(TAG, "width = " + String.valueOf(screenwidth));


        // distance walked in millimeters
        int distanceWalkedMillimeters = 500;

        //int distanceWalked = distanceWalkedMillimeters*screenwidth/floor4Width;

        int orientationWalked = 0;

        // variance of orientation and distance
        int distanceVariance = 5; // 5 pixel variance
        int orientationVariance = 5; // 45 degrees orientation variance

        int orientationWalkedDegrees = 0;

        // move current location (big red particle)
        switch (v.getId()) {
            case R.id.buttonUp:{
                textStatus.setText("Up");

                actualLocationY = actualLocationY - distanceWalkedMillimeters*screenheight/floor3Height;
                orientationWalkedDegrees = 180;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonDown:{
                textStatus.setText("Down");
                actualLocationY = actualLocationY + distanceWalkedMillimeters*screenheight/floor3Height;
                orientationWalkedDegrees = 0;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonLeft:{
                textStatus.setText("Left");
                actualLocationX = actualLocationX - distanceWalkedMillimeters*screenwidth/floor3Width;
                orientationWalkedDegrees = 270;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonRight:{
                textStatus.setText("Right");
                actualLocationX = actualLocationX + distanceWalkedMillimeters*screenwidth/floor3Width;
                orientationWalkedDegrees = 90;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonReset:{
                reset = true;
                textStatus.setText("Reset");
                //actualLocationX = originalLocationX;
                //actualLocationY = originalLocationY;
                //orientationWalked = 0;
                //currentLocation.defineParticlePosition(actualLocationX, actualLocationY,true);
                break;
            }
        }

        // input from compass and IMU
        //distanceWalkedMillimeters = 1000;
        //orientationWalkedDegrees = 0;

        // orientation in degrees clockwise of North i.e. 45 = NE movement, 90 = E movement
        // Note -  NORTH points DOWNWARDS on map - TODO need to account for actual NORTH ito MAP when implementing with compass

        int distanceWalkedMillimetersX = (int) Math.round(distanceWalkedMillimeters*Math.cos(Math.toRadians((double) orientationWalkedDegrees)));
        int distanceWalkedMillimetersY = (int) Math.round(distanceWalkedMillimeters*Math.sin(Math.toRadians((double) orientationWalkedDegrees)));

        int distanceWalkedPixelsX;
        int distanceWalkedPixelsY;

        Bundle bundle = getIntent().getExtras();
        Integer floor = bundle.getInt("floor");
        if(floor == 3){
            distanceWalkedPixelsX = distanceWalkedMillimetersX*screenwidth/floor3Width;
            distanceWalkedPixelsY = distanceWalkedMillimetersY*screenheight/floor3Height;
        }
        else{
            distanceWalkedPixelsX = distanceWalkedMillimetersX*screenwidth/floor4Width;
            distanceWalkedPixelsY = distanceWalkedMillimetersY*screenheight/floor4Height;
        }

        // distance walked in pixel distances
        //int distanceWalkedPixels = distanceWalkedMillimeters*pixelcount/height;
        // TODO need to convert to meter distance using height and width - remember to account for orientation angle with different aspect ratio

        //distanceWalked = (int) Math.round(Math.sqrt(distanceWalkedPixelsX^2 + distanceWalkedPixelsY^2));

        // make sure we haven't pressed reset - otherwise we do not apply motion model
        if (!reset) {


            // init distance and orientation variables which include noise
            int noisyDistanceWalkedMillimeters;
            double noisyOrientationWalkedDegrees;

            int noisyDistanceWalkedPixelsX;
            int noisyDistanceWalkedPixelsY;

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

                if(floor == 3){
                    noisyDistanceWalkedPixelsX = noisyDistanceWalkedMillimetersX*screenwidth/floor3Width;
                    noisyDistanceWalkedPixelsY = noisyDistanceWalkedMillimetersY*screenheight/floor3Height;
                }
                else{
                    noisyDistanceWalkedPixelsX = noisyDistanceWalkedMillimetersX*screenwidth/floor4Width;
                    noisyDistanceWalkedPixelsY = noisyDistanceWalkedMillimetersY*screenheight/floor4Height;
                }

                        //int noisyDistanceWalkedPixlesY = distanceWalkedPixelsY + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);

                //noisyDistanceWalked = distanceWalked + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);

                // noisyOrientationWalkedDegrees = (double) orientationWalkedDegrees + orientationRandom.nextGaussian()*orientationVariance;

                // create new Particle which represents moved particle position
                Particle movedParticle = new Particle(canvas,screenwidth,screenheight);

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
                int counter = 0;

                while((isCollision(movedParticle) || isInClosedArea(movedParticle) || isCollisionTrajectory(movedParticle, currentParticle)) && counter<50){

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
                }

                // update particleList
                particlesList.set(particleIdx,movedParticle);

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
                Particle newRandomParticle = new Particle(canvas,screenwidth,screenheight);

                while(isCollision(newRandomParticle) || isInClosedArea(newRandomParticle)){
                    newRandomParticle.assignRandomPosition();
                }

                // replace current particle at randomIdx with this new, random, refactored particle
                particlesList.set(randomIdx,newRandomParticle);
            }

        }

        // TODO - add functionality for reset button if deemed necessary

        redraw();


    }

    private void calculateParticlesPosition(){


    }

    /**
     * detected walking
     */
    public void walkingDetected(){
        Log.d(TAG, "I've made a step");

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
            // TODO - possibly not necessary?
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
            // TODO - possibly not necessary?
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
     * Determines if two shapes intersect.
     * @param first The first shape.
     * @param second The second shape.
     * @return True if they intersect, false otherwise.
     */
    private boolean isShapeCollision(ShapeDrawable first, ShapeDrawable second) {
        Rect firstRect = new Rect(first.getBounds());
        return firstRect.intersect(second.getBounds());
    }
}
