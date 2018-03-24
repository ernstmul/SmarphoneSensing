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

import nl.tudelft.smartphonesensing.R;


/**
 * Created by ernstmulders on 23/03/2018.
 */

public class ParticlesActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "ParticlesActivity";

    //configuration
    private static Integer particlesAmount = 1000;
    private static Boolean shouldDrawClosedAreas = false;

    private Canvas canvas;
    private List<ShapeDrawable> walls;
    private List<ShapeDrawable> closed_areas;
    private List<Particle> particles;
    private Particle currentLocation;

    // define buttons
    private Button up,left,right,down,reset;
    // define textview
    private TextView textStatus;

    private int actualX = 500;
    private int actualY = 220;


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

        // set buttons
        up = (Button) findViewById(R.id.buttonUp);
        down = (Button) findViewById(R.id.buttonDown);
        left = (Button) findViewById(R.id.buttonLeft);
        right = (Button) findViewById(R.id.buttonRight);
        reset = (Button) findViewById(R.id.buttonReset);

        // set listeners
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

        //draw the closed areas if needed
        if(shouldDrawClosedAreas) {
            for (ShapeDrawable closedArea : closed_areas)
                closedArea.draw(canvas);
        }


        //generate all particles, and place them on the map
        particles = new ArrayList<>();
        for(Integer particleCount = 0; particleCount < particlesAmount; particleCount++){
            //generate a particle
            Particle particle = new Particle(canvas, width, height);

            //place random
            while(isCollision(particle) || isInClosedArea(particle)){
                particle.drawRandomPosition();
            }

            //add to our particles list
            particles.add(particle);
        }

        currentLocation = new Particle(canvas, width, height);
        while(isCollision(currentLocation) || isInClosedArea(currentLocation)){
            currentLocation.drawSpecifedPosition(actualX,actualY);
        }

        particles.add(currentLocation);

        //and redraw everything
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

        for(Particle particle : particles){
            particle.redraw();
        }

        //redraw the closed areas if needed
        if(shouldDrawClosedAreas) {
            for (ShapeDrawable closedArea : closed_areas)
                closedArea.draw(canvas);
        }
    }

    /**
     * determines if particle is in a closed area
     */
    private boolean isInClosedArea(Particle p){
        //if particles == null, it is not yet drawn, and so its in a closed area
        if(p.particle == null){return true;}

        for(ShapeDrawable closedArea : closed_areas) {
            Log.d(TAG, "bounds:" + closedArea.getBounds());
            if(isCollision(closedArea,p.particle)){
                Log.d(TAG, "restricted area collision");
                return true;
            }

        }

        return false;
    }

    @Override
    public void onClick(View v){
        Log.d(TAG, "Button pressed");

        // move current location (big red particle)
        switch (v.getId()) {
            case R.id.buttonUp:{
                textStatus.setText("Up");
                actualY = actualY - 10;
                currentLocation.drawSpecifedPosition(actualX,actualY);
                break;
            }
            case R.id.buttonDown:{
                textStatus.setText("Down");
                actualY = actualY + 10;
                currentLocation.drawSpecifedPosition(actualX,actualY);
                break;
            }
            case R.id.buttonLeft:{
                textStatus.setText("Left");
                actualX = actualX - 10;
                currentLocation.drawSpecifedPosition(actualX,actualY);
                break;
            }
            case R.id.buttonRight:{
                textStatus.setText("Right");
                actualX = actualX + 10;
                currentLocation.drawSpecifedPosition(actualX,actualY);
                break;
            }
        }

        // apply motion model to all particles



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
            if(isCollision(wall,p.particle)){
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
    private boolean isCollision(ShapeDrawable first, ShapeDrawable second) {
        Rect firstRect = new Rect(first.getBounds());
        return firstRect.intersect(second.getBounds());
    }
}
