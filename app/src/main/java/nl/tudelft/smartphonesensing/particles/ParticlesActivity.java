package nl.tudelft.smartphonesensing.particles;

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
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.smartphonesensing.R;

/**
 * Created by ernstmulders on 23/03/2018.
 */

public class ParticlesActivity  extends AppCompatActivity {

    private static String TAG = "ParticlesActivity";

    //configuration
    private static Integer particlesAmount = 1000;
    private static Boolean shouldDrawClosedAreas = false;

    private Canvas canvas;
    private List<ShapeDrawable> walls;
    private List<ShapeDrawable> closed_areas;
    private List<Particle> particles;



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

        //and redraw everything
        redraw();


        Log.d(TAG, "load floor:" + floor);

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
