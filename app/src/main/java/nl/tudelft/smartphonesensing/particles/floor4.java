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

public class floor4 {

    private static String TAG = "floor4";

    /**
     * The walls.
     */
    private List<ShapeDrawable> walls;

    /**
     * The closed areas.
     */
    private List<ShapeDrawable> closed_areas;

    public List getClosedAreas(int width, int height){
        closed_areas = new ArrayList<>();

        //top area (we might chop this off the floorplan entirely
        closed_areas.add(functionDimensionsToClosedArea(0,0,14400,7800));

        //rooms below area 2 & 3 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,7800,5200,17000));

        //room to the right of 7 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,23900,5200,26200));

        //room below 8  (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,26200,2100,29500));

        //floor below 9 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,29500,3600,33800));

        //floor above 9 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(5900,29500,9400,33800));

        //island between 16, 6, 11, 14 and 15
        closed_areas.add(functionDimensionsToClosedArea(7500,21600,13100,26000));
        closed_areas.add(functionDimensionsToClosedArea(7500,20400,11700,21600));

        //island from area 1, minus area 1
        closed_areas.add(functionDimensionsToClosedArea(9200,7800,13100,12200));
        closed_areas.add(functionDimensionsToClosedArea(7500,12200,11200,13600));
        closed_areas.add(functionDimensionsToClosedArea(7500,13600,11700,15800));

        return closed_areas;
    }

    /**
     * floor parameters
     */
    private int screenWidth;
    private int screenHeight;
    private int floorWidthInCm = 14400;
    private int floorHeightInCm = 33800;

    public floor4(int width, int height){
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public List getWalls(int width, int height){
        Log.d(TAG, "screen x:" + width + " y:" + height);
        //initialize walls
        walls = new ArrayList<>();

        //outlines
        walls.add(functionDimensionsToWall(0,0,14400,true));
        walls.add(functionDimensionsToWall(0,0,33800,false));
        walls.add(functionDimensionsToWall(14400,0,33800,false));
        walls.add(functionDimensionsToWall(0,33800,14400,true));

        //first horizontal cells
        walls.add(functionDimensionsToWall(0,4300,9200,true));
        walls.add(functionDimensionsToWall(9200,6500,5000,true));
        walls.add(functionDimensionsToWall(9200,4300,2200,false));

        //second layer horizontal cells
        walls.add(functionDimensionsToWall(2100,4300,3500,false));
        walls.add(functionDimensionsToWall(0,7800,5200,true));

        //island office parts 1
        walls.add(functionDimensionsToWall(7500,7800,5600,true));
        walls.add(functionDimensionsToWall(7500,7800,8000,false));
        walls.add(functionDimensionsToWall(9200,7800,4400,false));
        walls.add(functionDimensionsToWall(7500,12200,5600,true));
        walls.add(functionDimensionsToWall(13100,7800,4550,false));
        walls.add(functionDimensionsToWall(7500,13600,4200,true));
        walls.add(functionDimensionsToWall(11000,12200,1400,false));
        walls.add(functionDimensionsToWall(7500,15800,4200,true));
        walls.add(functionDimensionsToWall(11700,13600,2200,false));

        //island office parts 2
        walls.add(functionDimensionsToWall(7500,17900,4200,true));
        walls.add(functionDimensionsToWall(7500,17900,8100,false));
        walls.add(functionDimensionsToWall(7500,26000,5600,true));
        walls.add(functionDimensionsToWall(7500,21600,5600,true));
        walls.add(functionDimensionsToWall(13100,21600,4400,false));
        walls.add(functionDimensionsToWall(11700,17900,3700,false));
        walls.add(functionDimensionsToWall(7500,20200,4200,true));

        //long vertical corridor left wall
        walls.add(functionDimensionsToWall(5200,7800,18300,false));

        //rooms left side off left corridor wall
        walls.add(functionDimensionsToWall(0,17000,5200,true));
        walls.add(functionDimensionsToWall(0,20400,5200,true));
        walls.add(functionDimensionsToWall(0,23800,5200,true));
        walls.add(functionDimensionsToWall(0,26100,5200,true));
        walls.add(functionDimensionsToWall(2100,26100,3400,false));

        //bottom offices
        walls.add(functionDimensionsToWall(0,29500,9400,true));
        walls.add(functionDimensionsToWall(3600,29500,4300,false));
        walls.add(functionDimensionsToWall(5900,29500,4300,false));
        walls.add(functionDimensionsToWall(9400,27300,6500,false));
        walls.add(functionDimensionsToWall(9400,27300,5000,true));

        /*ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.setBounds(0, 0, width, height);
        //d.setBounds(width/2-200, height/2-90, width/2+200, height/2-80);
        ShapeDrawable d2 = new ShapeDrawable(new RectShape());
        d2.setBounds(width/2-200, height/2+60, width/2+200, height/2+80);
        ShapeDrawable d3 = new ShapeDrawable(new RectShape());
        d3.setBounds(width/2+200, height/2-90, width/2+210, height/2+70);
        walls.add(d);
        walls.add(d2);
        walls.add(d3);*/

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

       // Log.d(TAG, "partial:" + partial);
        //Log.d(TAG, "left:" + ((cmFromLeft/this.floorWidthInCm) * this.screenWidth));
       // Log.d(TAG, "Pixel draw left:" + left + " top:" + (cmFromTop/this.floorHeightInCm) * this.screenHeight + "right: " + ((isHorizontal) ? ((cmFromLeft+sizeInCm)/this.floorWidthInCm) * this.screenWidth : ((cmFromLeft/this.floorWidthInCm) * this.screenWidth + 20)) + " bottom:"+ ((!isHorizontal) ? ((cmFromTop+sizeInCm)/this.floorHeightInCm) * this.screenHeight : ((cmFromTop/this.floorHeightInCm) * this.screenHeight + 10)));
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

        Log.d(TAG, "left:" + left);
        Log.d(TAG, "top: " + top);
        Log.d(TAG, "right: " + right);
        Log.d(TAG, "bottom: " + bottom);

        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }
}