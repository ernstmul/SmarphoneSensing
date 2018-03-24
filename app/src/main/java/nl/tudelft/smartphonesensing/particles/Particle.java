package nl.tudelft.smartphonesensing.particles;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by ernstmulders on 23/03/2018.
 */

public class Particle {

    private String TAG = "Particle class: ";

    private Canvas canvas;
    private Integer screen_width;
    private Integer screen_height;

    public ShapeDrawable particle;

    private Integer particle_size = 5;

    public Particle(Canvas c, Integer width, Integer height){
        //store the canvas, width and height
        canvas = c;
        screen_width = width;
        screen_height = height;
    }


    public void drawSpecifedPosition(int posX, int posY){

        //int posX = ThreadLocalRandom.current().nextInt(0, screen_width - (particle_size/2));
        //int posY = ThreadLocalRandom.current().nextInt(0, screen_height - (particle_size / 2));

        particle = new ShapeDrawable(new OvalShape());
        particle.getPaint().setColor(Color.RED);
        particle.setBounds(posX-5*particle_size,posY-5*particle_size,posX+5*particle_size,posY+5*particle_size);
        particle.draw(canvas);
        Log.d(TAG, "Specified particle created: ");
        Log.d(TAG, String.valueOf(posX));
        Log.d(TAG, String.valueOf(posY));
    }

//    public void applyMotionModel(double orientation, double distance, double variance){
//
//    }

    /**
     * draw particle on a random position
     */
    public void drawRandomPosition(){

        //calculate width
        int randX = ThreadLocalRandom.current().nextInt(0, screen_width - (particle_size/2));
        int randY = ThreadLocalRandom.current().nextInt(0, screen_height - (particle_size / 2));

        particle = new ShapeDrawable(new OvalShape());
        particle.getPaint().setColor(Color.BLUE);
        particle.setBounds(randX-particle_size, randY-particle_size, randX+particle_size, randY+particle_size);
        particle.draw(canvas);
    }

    /**
     * redraw the particle
     */
    public void redraw(){
        particle.draw(canvas);
    }
}
