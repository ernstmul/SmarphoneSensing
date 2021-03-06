package nl.tudelft.smartphonesensing.particles;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ernstmulders on 09/03/2018.
 */

public class floor3 {

    private static String TAG = "floor3";

    /**
     * The walls.
     */
    private List<ShapeDrawable> walls;

    /**
     * The dividers.
     */
    private List<ShapeDrawable> dividers;

    /**
     * The closed areas.
     */
    private List<ShapeDrawable> closed_areas;

    /**
     * floor parameters
     */
    private int screenWidth;
    private int screenHeight;
    // default values, can be changed
    private int floorWidthInCm = 14400;
    private int floorHeightInCm = 26000;

    public floor3(int width, int height, int floorWidthInCm, int floorHeightInCm){
        this.screenWidth = width;
        this.screenHeight = height;
        this.floorWidthInCm = floorWidthInCm;
        this.floorHeightInCm = floorHeightInCm;
    }

    // alternative constructor
    public floor3(int width, int height){
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public List getClosedAreas(int width, int height){
        closed_areas = new ArrayList<>();

        closed_areas.add(functionDimensionsToClosedArea(0,0,5200,18200));

        closed_areas.add(functionDimensionsToClosedArea(0,18200,2100,18200+3400));

        closed_areas.add(functionDimensionsToClosedArea(0,18200+3400,9400,26000));

        // island part 2
        closed_areas.add(functionDimensionsToClosedArea(7500,13800,13100,18200));
        closed_areas.add(functionDimensionsToClosedArea(7500,12400,11700,13800));

        // island part 1
        closed_areas.add(functionDimensionsToClosedArea(10800,0,14400,4400));
        closed_areas.add(functionDimensionsToClosedArea(7500,1800,10800,4400));
        closed_areas.add(functionDimensionsToClosedArea(7500,4400,9300,5800));
        closed_areas.add(functionDimensionsToClosedArea(7500,5800,11700,8000));



        return closed_areas;
    }

    public List getDividers(int width, int height){
        //initialize dividers
        dividers = new ArrayList<>();

        //dividers.add(functionDimensionsToWall(0,200,14400,true));

        // door cell 18
        dividers.add(functionDimensionsToWall(7500,300,1500,false));

        // door stairs
        dividers.add(functionDimensionsToWall(7500,10450,1500,false));

        // door cell 20
        dividers.add(functionDimensionsToWall(9400,19700,1600,false));

        return dividers;
    }

    public List getWalls(int width, int height){
        Log.d(TAG, "screen x:" + width + " y:" + height);

        //initialize walls
        walls = new ArrayList<>();

        /*
         * Door width and position variables to set
         */

        // distance from top left corner of cell 17 (staircase) to door
        int cell17DoorFromTop = 350;
        // door width at cell 17 staircase
        int cell17DoorWidth = 1500;

        // height from top left corner of cell 18 to door
        int cell18DoorFromTop = 300;
        // door at cell 18 width
        int cell18DoorWidth = 1500;

        // distance from top left corner of cell 20 to door
        int cell20DoorFromTop = 250;
        // door width at cell 20
        int cell20DoorWidth = 1500;

        /*
         * Define walls of floor 3
         */

        //outlines
        walls.add(functionDimensionsToWall(0,0,14400,true));
        walls.add(functionDimensionsToWall(0,0,26000,false));
        walls.add(functionDimensionsToWall(14400,0,26000,false));
        walls.add(functionDimensionsToWall(0,26000,14400,true));

        //island office parts 1
        walls.add(functionDimensionsToWall(7500,0,6900,true));
        walls.add(functionDimensionsToWall(7500,1800,3300,true));
        walls.add(functionDimensionsToWall(7500,0,cell18DoorFromTop,false));
        walls.add(functionDimensionsToWall(7500,cell18DoorFromTop+cell18DoorWidth,5100-cell18DoorFromTop+cell18DoorWidth,false));

        walls.add(functionDimensionsToWall(10800,0,4400,false));
        walls.add(functionDimensionsToWall(7500,4400,6900,true));
        walls.add(functionDimensionsToWall(7500,5800,4200,true));
        walls.add(functionDimensionsToWall(9300,4400,1400,false));
        walls.add(functionDimensionsToWall(7500,8000,4200,true));


        walls.add(functionDimensionsToWall(11700,5800,2400,false));


        //island office parts 2
        walls.add(functionDimensionsToWall(7500,10100,4200,true));
        // staircase
        walls.add(functionDimensionsToWall(7500,10100,cell17DoorFromTop,false));
        walls.add(functionDimensionsToWall(7500,10100+cell17DoorFromTop+cell17DoorWidth,8200-(cell17DoorFromTop+cell17DoorWidth),false));
        walls.add(functionDimensionsToWall(7500,18200,5600,true));
        walls.add(functionDimensionsToWall(7500,13800,5600,true));
        walls.add(functionDimensionsToWall(13100,13800,4600,false));
        walls.add(functionDimensionsToWall(11700,10100,3700,false));
        walls.add(functionDimensionsToWall(7500,12400,4200,true));

        //long vertical corridor left wall
        walls.add(functionDimensionsToWall(5200,0,18450,false));

        //rooms left side off left corridor wall
        walls.add(functionDimensionsToWall(0,9200,5200,true));
        walls.add(functionDimensionsToWall(0,12600,5200,true));
        walls.add(functionDimensionsToWall(0,16000,5200,true));
        walls.add(functionDimensionsToWall(0,18300,5200,true));
        walls.add(functionDimensionsToWall(2100,18300,3400,false));

        //bottom offices
        walls.add(functionDimensionsToWall(0,21700,9400,true));
        walls.add(functionDimensionsToWall(3600,21700,4300,false));
        walls.add(functionDimensionsToWall(5900,21700,4300,false));

        // cell 20
        walls.add(functionDimensionsToWall(9400,19500,cell20DoorFromTop,false));
        walls.add(functionDimensionsToWall(9400,19500+cell20DoorFromTop+cell20DoorWidth,6500-cell20DoorFromTop-cell20DoorWidth,false));
        walls.add(functionDimensionsToWall(9400,19500,5000,true));

        return walls;

    }


    /**
     *
     * @param cmFromLeft distance from left floor bound in cm
     * @param cmFromTop distance from top floor bound in cm
     * @param sizeInCm length of the wall in cm
     * @param isHorizontal if its a vertical wall or horizontal line
     * IMPORTANT: the floorplan is 90degrees turned to fit the landscape plan on a portrait screen
     * @return
     */


    private ShapeDrawable functionDimensionsToWall(int cmFromLeft, int cmFromTop, int sizeInCm, boolean isHorizontal){
        ShapeDrawable d = new ShapeDrawable(new RectShape());

        //correct cmFromLeft and cmFromTop for line thinkness
        int cmFromLeftPixelWallThinknessCorrection = (cmFromLeft/this.floorWidthInCm)*10;
        int cmFromTopPixelWallThinknessCorrection = (cmFromTop/this.floorHeightInCm)*10;

        double partial = ((double)cmFromLeft/(double)this.floorWidthInCm);

        int left = (int)(((double)cmFromLeft/(double)this.floorWidthInCm) * (double)this.screenWidth - (double)cmFromLeftPixelWallThinknessCorrection);
        int top = (int) (((double)cmFromTop/(double)this.floorHeightInCm) * (double)this.screenHeight - (double)cmFromTopPixelWallThinknessCorrection);
        int right = (int) ((isHorizontal) ? (((double)cmFromLeft+sizeInCm)/(double)this.floorWidthInCm) * (double)this.screenWidth : (((double)cmFromLeft/(double)this.floorWidthInCm) * (double)this.screenWidth + 10.0));
        int bottom = (int)((!isHorizontal) ? (((double)cmFromTop+sizeInCm)/(double)this.floorHeightInCm) * (double)this.screenHeight : (((double)cmFromTop/(double)this.floorHeightInCm) * (double)this.screenHeight + 10.0));

        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }

    /**
     *
     * @param left_input distance from left floor bound in cm
     * @param top_input distance from top floor bound in cm
     * @param right_input distance from left floor bound in cm
     * @param bottom_input distance from top floor bound in cm
     * IMPORTANT: the floorplan is 90degrees turned to fit the landscape plan on a portrait screen
     * @return
     */

    private ShapeDrawable functionDimensionsToClosedArea(int left_input, int top_input, int right_input, int bottom_input){
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setColor(Color.RED);

        //correct cmFromLeft and cmFromTop for line thinkness

        int left = (int)(((double)left_input/(double)this.floorWidthInCm) * (double)this.screenWidth);
        int top = (int) (((double)top_input/(double)this.floorHeightInCm) * (double)this.screenHeight);
        int right = (int)(((double)right_input/(double)this.floorWidthInCm) * (double)this.screenWidth);
        int bottom = (int) (((double)bottom_input/(double)this.floorHeightInCm) * (double)this.screenHeight);

        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }


}
