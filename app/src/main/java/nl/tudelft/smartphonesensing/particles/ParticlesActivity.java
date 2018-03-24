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
            floor3 floor3 = new floor3(width,height);
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
        int width = size.x;
        int height = size.y;

        // check if reset is performed, if so, do not use motion model
        boolean reset = false;

        Log.d(TAG, "height = " + String.valueOf(height));
        Log.d(TAG, "width = " + String.valueOf(width));

        // distance walked in pixel distances
        int distanceWalked = 25; // TODO need to convert to meter distance using height and width - remember to account for orientation angle with different aspect ratio

        // orientation in degrees clockwise of North i.e. 45 = NE movement, 90 = E movement
        // Note -  NORTH points DOWNWARDS on map - TODO need to account for actual NORTH when implementing with compass
        int orientationWalked = 0;

        // variance of orientation and distance
        int distanceVariance = 5; // 5 pixel variance
        int orientationVariance = 10; // 45 degrees orientation variance

        // move current location (big red particle)
        switch (v.getId()) {
            case R.id.buttonUp:{
                textStatus.setText("Up");
                actualLocationY = actualLocationY - distanceWalked;
                orientationWalked = 180;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonDown:{
                textStatus.setText("Down");
                actualLocationY = actualLocationY + distanceWalked;
                orientationWalked = 0;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonLeft:{
                textStatus.setText("Left");
                actualLocationX = actualLocationX - distanceWalked;
                orientationWalked = 90;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonRight:{
                textStatus.setText("Right");
                actualLocationX = actualLocationX + distanceWalked;
                orientationWalked = 270;
                currentLocation.defineParticlePosition(actualLocationX,actualLocationY,true);
                break;
            }
            case R.id.buttonReset:{
                reset = true;
                textStatus.setText("Reset");
                actualLocationX = originalLocationX;
                actualLocationY = originalLocationY;
                orientationWalked = 0;
                currentLocation.defineParticlePosition(actualLocationX, actualLocationY,true);
                break;
            }
        }

        /*
         * Apply motion model to all particles
         */

        // make sure we haven't pressed reset - otherwise we do not apply motion model
        if (!reset){

            // 1 = x, 2 = y position of original particles
            // this matrix contains the original x and y positions of the particles
            //
            // this is necessary because the for loop below updates particle locations, meaning we
            // cannot access their original locations as they have been changed
            int[][] originalParticleLocationMatrix = new int[2][particlesAmount];
            for (int particleIdx = 0; particleIdx < particlesAmount; particleIdx++){
                Particle currentParticle = particlesList.get(particleIdx);
                originalParticleLocationMatrix[0][particleIdx] = currentParticle.getX();
                originalParticleLocationMatrix[1][particleIdx] = currentParticle.getY();
            }

            // init distance and orientation variables which include noise
            int noisyDistanceWalked = 0;
            double noisyOrientationWalked = 0;

            // loop through particle list to apply motion model
            for (int particleIdx = 0; particleIdx < particlesAmount; particleIdx++){

                //Log.d(TAG, "--------------------------");
                //Log.d(TAG, "current particle index: " + String.valueOf(particleIdx));

                Particle currentParticle = particlesList.get(particleIdx);
                int initX = currentParticle.getX();
                int initY = currentParticle.getY();
                //Log.d(TAG, "current particle x and y: " + String.valueOf(initX) + ", " + String.valueOf(initY));

                // create random variables
                Random distanceRandom = new Random();
                Random orientationRandom = new Random();

                // add noise to orientation and distance walked variance (Gaussian distribution)
                noisyDistanceWalked = distanceWalked + (int) Math.round(distanceRandom.nextGaussian()*distanceVariance);
                noisyOrientationWalked = (double) orientationWalked + orientationRandom.nextGaussian()*orientationVariance;

                Particle newCurrentParticle = new Particle(canvas,width,height);

                // first try current particle and apply motion model
                int randomParticleIdx = particleIdx;

                int newX = 0;
                int newY = 0;

                // stop stuck while loop
                int counter = 0;

                while((isCollision(newCurrentParticle) || isInClosedArea(newCurrentParticle)) && counter<5){

                    counter++;
                    //Log.d(TAG, "counter of while: " + String.valueOf(counter));

                    newCurrentParticle.defineParticlePosition(initX, initY, false);

                    //Log.d(TAG, "Init values: " + String.valueOf(initX) + ", " + String.valueOf(initY));
                    //Log.d(TAG, String.valueOf(counter));

                    // apply motion model
                    newX = initX - (int) Math.round(noisyDistanceWalked*Math.sin(Math.toRadians(noisyOrientationWalked)));
                    newY = initY + (int) Math.round(noisyDistanceWalked*Math.cos(Math.toRadians(noisyOrientationWalked)));

                    newCurrentParticle.defineParticlePosition(newX, newY,false);

                    // create new randomParticleIdx and apply motion model
                    randomParticleIdx = ThreadLocalRandom.current().nextInt(0, particlesAmount-1);
                    //Log.d(TAG, "randomParticleIdx: " + String.valueOf(randomParticleIdx));

                    // get x and y coordinates of this new randomParticleIdx
                    initX = originalParticleLocationMatrix[0][randomParticleIdx];
                    initY = originalParticleLocationMatrix[1][randomParticleIdx];

                    //Log.d(TAG, "newCurrentParticle x and y: " + String.valueOf(newCurrentParticle.getCurrentX()) + ", " + String.valueOf(newCurrentParticle.getCurrentY()));
                    //Particle fromParticleList = particlesList.get(particleIdx);
                    //Log.d(TAG, "particleList x and y: " + String.valueOf(fromParticleList.getCurrentX()) + ", " + String.valueOf(fromParticleList.getCurrentY()));

                    //Log.d(TAG, "newCurrentParticle: ");

                    //Log.d(TAG, "isCollision: " + Boolean.valueOf(isCollision(newCurrentParticle)));
                    //Log.d(TAG, "isInClosedArea: " + Boolean.valueOf(isInClosedArea(newCurrentParticle)));

                    //Log.d(TAG, "CurrentParticle: ");

                    //Log.d(TAG, "isCollision: " + Boolean.valueOf(isCollision(currentParticle)));
                    //Log.d(TAG, "isInClosedArea: " + Boolean.valueOf(isInClosedArea(currentParticle)));

                    //Log.d(TAG,"** end of while loop iteration **");
                }

                // redefine particle with particle parameters that are within bounds
                // as determined within the while-loop
                currentParticle.defineParticlePosition(newX,newY,false);

                // update particleList
                particlesList.set(particleIdx,currentParticle);



            } // end of loop going through particles

            // refactor particles
            // this means re-assigning random particles to random map positions
            // the idea is to allow for alternative particle positions in case all particles converge
            // to the wrong location

            for (int refactorIdx = 0; refactorIdx < refactorParticlesAmount; refactorIdx++){

                // get random index within particle amount
                int randomIdx = ThreadLocalRandom.current().nextInt(0, particlesAmount-1);

                // update new particles
                Particle newRandomParticle = new Particle(canvas,width,height);

                while(isCollision(newRandomParticle) || isInClosedArea(newRandomParticle)){
                    newRandomParticle.assignRandomPosition();
                }

                //newRandomParticle.drawRandomPosition();

                // add this new particle to the particlesList
                particlesList.set(randomIdx,newRandomParticle);

                // update array of previous positions
                originalParticleLocationMatrix[0][randomIdx] = newRandomParticle.getX();
                originalParticleLocationMatrix[1][randomIdx] = newRandomParticle.getY();

            }

        } // end if reset statement

        // TODO - add functionality for reset button if deemed necessary

        redraw();

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
