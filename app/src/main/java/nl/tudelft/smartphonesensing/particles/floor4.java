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
     * The dividers.
     */
    private List<ShapeDrawable> dividers;

    /**
     * The closed areas.
     */
    private List<ShapeDrawable> closed_areas;

    public List getClosedAreas(int width, int height){

        // remove top portion, subtract top from all top_input values
        int top = 7800;

        closed_areas = new ArrayList<>();

        //top area (we might chop this off the floorplan entirely
        //closed_areas.add(functionDimensionsToClosedArea(0,0,14400,7800));

        //rooms below area 2 & 3 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,7800-top,5200,17000-top));

        //room to the right of 7 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,23900-top,5200,26200-top));

        //room below 8  (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,26200-top,2100,29500-top));

        //floor below 9 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(0,29500-top,3600,33800-top));

        //floor above 9 (as seen on the official floorplan)
        closed_areas.add(functionDimensionsToClosedArea(5900,29500-top,9400,33800-top));

        //island between 16, 6, 11, 14 and 15
        closed_areas.add(functionDimensionsToClosedArea(7500,21600-top,13100,26000-top));
        closed_areas.add(functionDimensionsToClosedArea(7500,20400-top,11700,21600-top));

        //island from area 1, minus area 1
        closed_areas.add(functionDimensionsToClosedArea(9200,7800-top,13100,12200-top));
        closed_areas.add(functionDimensionsToClosedArea(7500,12200-top,11200,13600-top));
        closed_areas.add(functionDimensionsToClosedArea(7500,13600-top,11700,15800-top));

        //bottom right cell
        closed_areas.add(functionDimensionsToClosedArea(9200,27500-top,14400,33800-top));

        // remove bottom portion
        closed_areas.add(functionDimensionsToClosedArea(0,33800-top,14400,33800));

        return closed_areas;
    }

    /**
     * floor parameters
     */
    private int screenWidth;
    private int screenHeight;
    private int floorWidthInCm = 14400;
    private int floorHeightInCm = 26500;
    //private int floorHeightInCm = 33800;

    public floor4(int width, int height){
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public List getDividers(int width, int height){
        //initialize dividers
        dividers = new ArrayList<>();

        dividers.add(functionDimensionsToWall(5200,4400,2300,true));
        dividers.add(functionDimensionsToWall(5200,9200,2300,true));
        dividers.add(functionDimensionsToWall(5200,12600,2300,true));
        dividers.add(functionDimensionsToWall(5200,18200,2300,true));

        // doors of 5 and 7
        dividers.add(functionDimensionsToWall(5200,10000,1600,false));
        dividers.add(functionDimensionsToWall(5200, 13500,1500,false));

        // 8 and 10 verticle
        dividers.add(functionDimensionsToWall(5900,18200,3500,false));

        // 11 and 12
        dividers.add(functionDimensionsToWall(13100,18200,1300,false));

        // 12 and 13
        dividers.add(functionDimensionsToWall(13100,13800,1300,true));

        // 13 and 14
        dividers.add(functionDimensionsToWall(11700,10100,2700,true));

        // 14 and 15
        dividers.add(functionDimensionsToWall(11700,5800,2700,true));

        // middle area
        dividers.add(functionDimensionsToWall(7500,8000,2100,false));
        dividers.add(functionDimensionsToWall(11700,8000,2100,false));

        // door stairs
        dividers.add(functionDimensionsToWall(7500,10450,1500,false));

        // door cell 1
        dividers.add(functionDimensionsToWall(7500,1000,1500,false));

        // door cell 9
        dividers.add(functionDimensionsToWall(4000,21700,1500,true));

        return dividers;
    }

    public List getWalls(int width, int height){
        Log.d(TAG, "screen x:" + width + " y:" + height);



        //initialize walls
        walls = new ArrayList<>();

        /*
         * Door width and position variables to set
         */

        // distance between top right of cell 1 and door
        int wallUntilCell1Door = 1000;
        // door width at cell 1
        int cell1DoorWidth = 1500;

        // distance from top right corner of cell 5 (staircase) to door
        int wallUntilCell5Door = 10000;
        // door width at cell 5
        int cell5DoorWidth = 1500;

        // distance of wall between doors of cell 5 and cell 7
        int wallBetween5and7 = 2000;

        // door width at cell 7
        int cell7DoorWidth = 1500;

        // door width at cell 9
        int cell9DoorWidth = 1500;

        // distance between bottom left office's top horizontal wall and cell 9 door
        int cell9Distance = 4000;

        // distance between top left corner of cell 16 and door
        int cell16DoorFromTop = 350;
        int cell16DoorWidth = 1500;

        /*
         * Define walls of floor 4
         */

        //outlines
        walls.add(functionDimensionsToWall(0,0,14400,true));
        walls.add(functionDimensionsToWall(0,0,26000,false));
        walls.add(functionDimensionsToWall(14400,0,26000,false));
        walls.add(functionDimensionsToWall(0,26000,14400,true));

        // Island Office Part 1 - Cell 1
        walls.add(functionDimensionsToWall(7500,0,wallUntilCell1Door,false));
        walls.add(functionDimensionsToWall(7500,wallUntilCell1Door+cell1DoorWidth,8000-wallUntilCell1Door-cell1DoorWidth,false));
        walls.add(functionDimensionsToWall(9200,0,4400,false));
        walls.add(functionDimensionsToWall(7500,4400,5600,true));
        walls.add(functionDimensionsToWall(13100,0,4600,false));
        walls.add(functionDimensionsToWall(7500,5800,4200,true));
        walls.add(functionDimensionsToWall(11000,4400,1400,false));
        walls.add(functionDimensionsToWall(7500,8000,4200,true));
        walls.add(functionDimensionsToWall(11700,5800,2375,false));

        // Island Office Part 2
        walls.add(functionDimensionsToWall(7500,10100,4200,true));

        // Staircase - Cell 16
        walls.add(functionDimensionsToWall(7500,10100,cell16DoorFromTop,false));
        walls.add(functionDimensionsToWall(7500,10100+cell16DoorFromTop+cell16DoorWidth,8200-(cell16DoorFromTop+cell16DoorWidth),false));
        walls.add(functionDimensionsToWall(7500,18200,5600,true));
        walls.add(functionDimensionsToWall(7500,13800,5600,true));
        walls.add(functionDimensionsToWall(13100,13800,4600,false));
        walls.add(functionDimensionsToWall(11700,10100,3700,false));
        walls.add(functionDimensionsToWall(7500,12400,4200,true));

        // long vertical corridor left wall
        walls.add(functionDimensionsToWall(5200,0,wallUntilCell5Door,false));
        walls.add(functionDimensionsToWall(5200,cell5DoorWidth+wallUntilCell5Door,wallBetween5and7,false));
        walls.add(functionDimensionsToWall(5200,cell5DoorWidth+wallUntilCell5Door+wallBetween5and7+cell7DoorWidth,18500-(cell7DoorWidth+wallUntilCell5Door+wallBetween5and7+cell7DoorWidth),false));

        // rooms left side off left corridor wall
        walls.add(functionDimensionsToWall(0,9200,5200,true));
        walls.add(functionDimensionsToWall(0,12600,5200,true));
        walls.add(functionDimensionsToWall(0,16000,5200,true));
        walls.add(functionDimensionsToWall(0,18300,5200,true));
        walls.add(functionDimensionsToWall(2100,18300,3400,false));

        // bottom offices
        walls.add(functionDimensionsToWall(0,21700,cell9Distance,true));
        walls.add(functionDimensionsToWall(cell9Distance+cell9DoorWidth,21700,(9400-cell9Distance-cell9DoorWidth),true));
        walls.add(functionDimensionsToWall(3600,21700,4300,false));
        walls.add(functionDimensionsToWall(5900,21700,4300,false));
        walls.add(functionDimensionsToWall(9400,19500,6500,false));
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