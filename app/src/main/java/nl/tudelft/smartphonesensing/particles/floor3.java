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
     * The closed areas.
     */
    private List<ShapeDrawable> closed_areas;

    /**
     * floor parameters
     */
    private int screenWidth;
    private int screenHeight;
    private int floorWidthInCm = 14400;
    private int floorHeightInCm = 33800;

    public floor3(int width, int height){
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public List getClosedAreas(int width, int height){
        closed_areas = new ArrayList<>();

        //toto add closed areas 3rd floor

        return closed_areas;
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
        walls.add(functionDimensionsToWall(7500,7800,6900,true));
        walls.add(functionDimensionsToWall(7500,9600,3300,true));
        walls.add(functionDimensionsToWall(7500,7800,8000,false));
        walls.add(functionDimensionsToWall(10800,7800,4400,false));
        walls.add(functionDimensionsToWall(7500,12200,6900,true));
        walls.add(functionDimensionsToWall(7500,13600,4200,true));
        walls.add(functionDimensionsToWall(9300,12200,1400,false));
        walls.add(functionDimensionsToWall(7500,15800,4200,true));
        walls.add(functionDimensionsToWall(11700,13600,2200,false));

        //island office parts 2
        walls.add(functionDimensionsToWall(7500,17900,4200,true));
        walls.add(functionDimensionsToWall(7500,17900,8100,false));
        walls.add(functionDimensionsToWall(7500,26000,5600,true));
        walls.add(functionDimensionsToWall(7500,21600,5600,true));
        walls.add(functionDimensionsToWall(9800,21600,4400,false));
        walls.add(functionDimensionsToWall(13100,21600,4400,false));
        walls.add(functionDimensionsToWall(11700,17900,2300,false));
        walls.add(functionDimensionsToWall(9200,20200,1400,false));

        walls.add(functionDimensionsToWall(7500,20200,4200,true));

        //long vertical corridor left wall
        walls.add(functionDimensionsToWall(5200,7800,18300,false));

        //rooms left side off left corridor wall
        walls.add(functionDimensionsToWall(0,26100,5200,true));
        walls.add(functionDimensionsToWall(2100,26100,3400,false));

        //bottom offices
        walls.add(functionDimensionsToWall(0,29500,9400,true));
        walls.add(functionDimensionsToWall(9400,27300,6500,false));
        walls.add(functionDimensionsToWall(9400,27300,5000,true));


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
     * @param cmFromLeft distance from left floor bound in cm
     * @param cmFromTop distance from top floor bound in cm
     * @param sizeInCm length of the wall in cm
     * @param isHorizontal if its a vertical wall or horizontal line
     * IMPORTANT: the floorplan is 90degrees turned to fit the landscape plan on a portrait screen
     * @return
     */


    private ShapeDrawable functionDimensionsToClosedArea(int cmFromLeft, int cmFromTop, int sizeInCm, boolean isHorizontal){
        ShapeDrawable d = new ShapeDrawable(new RectShape());
        d.getPaint().setColor(Color.RED);

        //correct cmFromLeft and cmFromTop for line thinkness
        int cmFromLeftPixelWallThinknessCorrection = (cmFromLeft/this.floorWidthInCm)*10;
        int cmFromTopPixelWallThinknessCorrection = (cmFromTop/this.floorHeightInCm)*10;

        double partial = ((double)cmFromLeft/(double)this.floorWidthInCm);

        int left = (int)(((double)cmFromLeft/(double)this.floorWidthInCm) * (double)this.screenWidth - (double)cmFromLeftPixelWallThinknessCorrection);
        int top = (int) (((double)cmFromTop/(double)this.floorHeightInCm) * (double)this.screenHeight - (double)cmFromTopPixelWallThinknessCorrection);
        int right = (int) ((isHorizontal) ? (((double)cmFromLeft+sizeInCm)/(double)this.floorWidthInCm) * (double)this.screenWidth : (((double)cmFromLeft/(double)this.floorWidthInCm) * (double)this.screenWidth + 10.0));
        int bottom = (int)((!isHorizontal) ? (((double)cmFromTop+sizeInCm)/(double)this.floorHeightInCm) * (double)this.screenHeight : (((double)cmFromTop/(double)this.floorHeightInCm) * (double)this.screenHeight + 10.0));

        Log.d(TAG, "partial:" + partial);
        Log.d(TAG, "left:" + ((cmFromLeft/this.floorWidthInCm) * this.screenWidth));
        Log.d(TAG, "Pixel draw left:" + left + " top:" + (cmFromTop/this.floorHeightInCm) * this.screenHeight + "right: " + ((isHorizontal) ? ((cmFromLeft+sizeInCm)/this.floorWidthInCm) * this.screenWidth : ((cmFromLeft/this.floorWidthInCm) * this.screenWidth + 20)) + " bottom:"+ ((!isHorizontal) ? ((cmFromTop+sizeInCm)/this.floorHeightInCm) * this.screenHeight : ((cmFromTop/this.floorHeightInCm) * this.screenHeight + 10)));
        d.setBounds(
                left,
                top,
                right,
                bottom);


        return d;
    }
}