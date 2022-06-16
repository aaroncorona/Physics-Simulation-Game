import java.awt.*;
import java.util.*;

import static java.lang.Math.abs;

public class Solution {

    // Add constants for particle types here.
    public static final int EMPTY = 0;
    public static final int METAL = 1;
    public static final int SAND = 2;
    public static final int SAND_LIGHT = -2;
    public static final int WATER = 3;
    public static final int CLOUD = 4;
    public static final int LAVA = 5;
    public static final int FIRE = 6;
    public static final String[] NAMES = {"Empty", "Metal", "Sand", "Water", "Cloud", "Lava", "Fire"};

    // Do not add any more fields as part of Lab 5.
    private int[][] grid;
    private SandDisplayInterface display;
    private RandomGenerator random;

    /**
     * Constructor.
     *
     * @param display The display to use for this run
     * @param random The random number generator to use to pick random points
     */
    public Solution(SandDisplayInterface display, RandomGenerator random) {
        this.display = display;
        this.random = random;
        // Fill color grid and grid tracker to start
        grid = new int[display.getNumRows()][display.getNumColumns()];
        for (int i = 0; i < grid.length; i++) {
            for (int a = 0; a < grid[i].length; a++) {
                grid[i][a] = 0;
            }
        }
    }

    /**
     * Called when the user clicks on a location.
     *
     * @param row
     * @param col
     * @param tool
     */
    private void locationClicked(int row, int col, int tool) {
        // If sand is selected, randomly pick a sand particle type (2 or -2)
        if(tool==2){
            if(new Random().nextInt(2) == 0){
                tool=-2;
            }
        }
        // Update the grid for every click event
        grid[row][col] = tool;
    }

    /** Copies each element of grid into the display. */
    public void updateDisplay() {
        // Redraw every grid position. Map tool values to colors
        for (int i = 0; i < grid.length; i++) {
            for (int a = 0; a < grid[i].length; a++) {
                if(grid[i][a] == 0){
                    display.setColor(i, a, Color.BLACK); // Empty
                } else if(grid[i][a] == 1){
                    display.setColor(i, a, Color.GRAY); // Metal
                } else if(grid[i][a] == 2){
                    display.setColor(i, a, Color.YELLOW); // Sand
                } else if(grid[i][a] == -2){
                display.setColor(i, a, Color.YELLOW.darker()); // Light sand
                } else if(grid[i][a] == 3){
                    display.setColor(i, a, Color.BLUE); // Water
                } else if(grid[i][a] == 4){
                    display.setColor(i, a, Color.WHITE); // Cloud
                } else if(grid[i][a] == 5){
                    display.setColor(i, a, Color.RED); // Lava
               }  else if(grid[i][a] == 6) {
                    display.setColor(i, a, Color.ORANGE); // Fire
               }
            }
        }
    }

    /** Called repeatedly. Causes one random particle to maybe do something. */
    // A single invocation of step should just attempt to move one particle in one square
    public void step() {
        // Get random point on the grid
        int totalRows = display.getNumRows();
        int totalCol = display.getNumColumns();
        Point randomPoint = random.getRandomPoint();
        int randomRow = randomPoint.row;
        int randomCol = randomPoint.column;
        int maxCol = display.getNumColumns() - 1;
        int maxRow = display.getNumRows() - 1;
        // Bottom Row - Sand or Water or Metal next to Lava: Ignite
        if(randomRow == maxRow
           && grid[randomRow][randomCol] > 0 // Sand or Water or Metal
           && grid[randomRow][randomCol] < 5
           && randomCol < maxCol // oob check
           && randomCol > 0 // oob check
           && (grid[randomRow][randomCol + 1] == 5 // next to lava
               || grid[randomRow][randomCol - 1] == 5)) {
            // Ignite particle above
            grid[randomRow-1][randomCol] = 6;
            // Evaporate particle
            grid[randomRow][randomCol] = 0;
        }
        // Bottom Row - Fire extinguished (edge case)
        else if (grid[randomRow][randomCol] == 6
                && randomRow == maxRow){
            // Erase particle
            grid[randomRow][randomCol] = 0;
        }
        // Bottom Row - Out of bounds check (freeze particles at the very bottom)
        else if(randomRow == maxRow){}
        // Any particle next to Lava: Ignite
        else if (grid[randomRow][randomCol] < 5
                && grid[randomRow][randomCol] != 0 // don't ignite empty space
                && randomCol < maxCol // oob check
                && randomCol > 0 // oob check
                && (grid[randomRow + 1][randomCol] == 5 // next to lava
                    || grid[randomRow][randomCol + 1] == 5
                    || grid[randomRow][randomCol - 1] == 5)){
            // Evaporate particle
            grid[randomRow][randomCol] = 0;
            // Next, Ignite surrounding area
            // Lava check before igniting surrounding area (only ignite metal, sand, and water)
            if(grid[randomRow][randomCol+1] < 5){
                grid[randomRow][randomCol+1] = 6;// Turn to fire
            }
            else if(grid[randomRow][randomCol-1] < 5){
                grid[randomRow][randomCol-1] = 6;// Turn to fire
            }
        }
        // Sand down movement: If the particle is sand and below is empty, move it down 1
        else if (abs(grid[randomRow][randomCol]) == 2
                && grid[randomRow + 1][randomCol] == 0) {
            // Move down
            grid[randomRow + 1][randomCol] = grid[randomRow][randomCol];
            grid[randomRow][randomCol] = 0;

        }
        // Sand down movement in water: If the particle is sand and water is below, swap positions (so sand will sink)
        else if (abs(grid[randomRow][randomCol]) == 2
                && grid[randomRow + 1][randomCol] == 3) {
            // Move down and move water up
            grid[randomRow + 1][randomCol] = grid[randomRow][randomCol];
            grid[randomRow][randomCol] = 3;
        }
        // Sand anti-stacking: Fall to the side (through an empty or water) instead of making a single vertical column
        else if (abs(grid[randomRow][randomCol]) == 2 // Sand with sand the next 2 below (prevent stack)
                && abs(grid[randomRow + 1][randomCol]) == 2){
            // Get random direction (dont use same Random as water in order to use different seed)
            int randomDir = new Random().nextInt(3);
            // Move right and down if empty or water
            if (randomCol < maxCol // oob check
                    && randomDir == 1
                    && (grid[randomRow + 1][randomCol + 1] == 0
                    || grid[randomRow + 1][randomCol + 1] == 3)) {
                grid[randomRow + 1][randomCol + 1] = grid[randomRow][randomCol];
                grid[randomRow][randomCol] = 0;
            }
            // Move left and down if empty or water
            else if (randomCol > 0 // oob check
                    && randomDir == 2
                    && (grid[randomRow + 1][randomCol - 1] == 0
                    || grid[randomRow + 1][randomCol - 1] == 3)) {
                grid[randomRow + 1][randomCol - 1] = grid[randomRow][randomCol];
                grid[randomRow][randomCol] = 0;
            }
        }
        // Water random movement
        else if (grid[randomRow][randomCol] == 3) {
            // Get random direction (for water)
            int randomDir = random.getRandomDirection();
            // First, out of bounds check in all 3 directions
            if((randomDir == 0 && randomRow == maxRow)
               || (randomDir == 1 && randomCol == maxCol)
               || (randomDir == 2 && randomCol == 0)) {}
            // Move in the random dir if that space is open
            else if (randomDir == 0
                     && grid[randomRow + 1][randomCol] == 0) {
                grid[randomRow + 1][randomCol] = grid[randomRow][randomCol];
                grid[randomRow][randomCol] = 0;
            } else if (randomDir == 1
                       && grid[randomRow][randomCol + 1] == 0) {
                grid[randomRow][randomCol + 1] = grid[randomRow][randomCol];
                grid[randomRow][randomCol] = 0;
            } else if (randomDir == 2
                       && grid[randomRow][randomCol - 1] == 0) {
                grid[randomRow][randomCol - 1] = grid[randomRow][randomCol];
                grid[randomRow][randomCol] = 0;
            }
        }
        // Cloud disappears off screen
        else if (randomCol == maxCol // oob check
                && grid[randomRow][randomCol] == 4){
            // Disappear
            grid[randomRow][randomCol] = 0;
        }
        // Cloud floats right into empty space very slowly (light breeze)
        else if (grid[randomRow][randomCol] == 4
                 && grid[randomRow][randomCol+1] == 0) {
            // Get random num
            int randomNum = new Random().nextInt(100);
            // Move right 1/100 times
            if(randomNum == 1){
                grid[randomRow][randomCol+1] = grid[randomRow][randomCol];
                grid[randomRow][randomCol] = 0;
            }
        }
        // Lava down movement through empty space: Move down
        else if (grid[randomRow][randomCol] == 5
                && grid[randomRow + 1][randomCol] == 0) {
            // Move down
            grid[randomRow + 1][randomCol] = grid[randomRow][randomCol];// Turn to lava
            grid[randomRow][randomCol] = 0;
        }
        // Lava movement through other particle: Move down and ignite surrounding area
        else if (grid[randomRow][randomCol] == 5
                && grid[randomRow + 1][randomCol] < 5) {
            // Move lava down 1
            grid[randomRow + 1][randomCol] = grid[randomRow][randomCol];// Turn to lava
            grid[randomRow][randomCol] = 0;
            // Next, ignite surrounding area (add fire)
            // First, out of bounds check right and left before igniting
            if(randomCol == maxCol
               || randomCol == 0) {}
            // Lava check before igniting surrounding area (only ignite metal, sand, and water)
            else if(grid[randomRow][randomCol+1] < 5){
                grid[randomRow][randomCol+1] = 6;// Turn to fire
            }
            else if(grid[randomRow][randomCol-1] < 5){
                grid[randomRow][randomCol-1] = 6;// Turn to fire
            }
        }
        // Fire random movement: If the particle is fire, move in a random direction that is free, or be extinguished randomly
        else if (grid[randomRow][randomCol] == 6) {
            // Get random direction (dont use same Random as water in order to use different seed)
            int randomDir = new Random().nextInt(3);
            // 0 puts out fire (happens 1/3 rolls)
            if (randomDir == 0) {
                grid[randomRow][randomCol] = 0;
            }
            // Do not float
            else if (grid[randomRow+1][randomCol] == 0) {
                grid[randomRow][randomCol] = 0;
            }
            // First, out of bounds check right and left
            else if(randomCol == maxCol
                    || randomCol == 0) {}
            // Lava check before igniting surrounding area (only ignite metal, sand, and water)
            else if (randomDir == 1
                     && grid[randomRow][randomCol + 1] < 5){ // flame ignites everything except lava
                grid[randomRow][randomCol + 1] = 6;
            }
            else if (randomDir == 2
                     && grid[randomRow][randomCol - 1] < 5) { // flame ignites everything except lava
                grid[randomRow][randomCol - 1] = 6;
            }
        }
        // Lava anti-stacking: Fall to the side (through anything) instead of making a single vertical column
        else if (grid[randomRow][randomCol] == 5 // Sand with sand the next 2 below (prevent stack)
                && grid[randomRow + 1][randomCol] == 5){
            // Get random direction (dont use same Random as water in order to use different seed)
            int randomDir = new Random().nextInt(3);
            // Move right and down
            if (randomCol < maxCol // oob check
                 && randomDir == 1
                 && grid[randomRow + 1][randomCol + 1] < 5) {
                grid[randomRow + 1][randomCol + 1] = 5;
                grid[randomRow][randomCol] = 0;
            }
            // Move left and down
            else if (randomCol > 0 // oob check
                    && randomDir == 2
                    && grid[randomRow + 1][randomCol - 1] < 5) {
                grid[randomRow + 1][randomCol - 1] = 5;
                grid[randomRow][randomCol] = 0;
            }
        }
        // Fire extinguished by water
        else if (grid[randomRow][randomCol] == 6
                && randomCol < maxCol // oob check
                && randomCol > 0 // oob check
                && (grid[randomRow + 1][randomCol] == 3 // next to water
                || grid[randomRow][randomCol + 1] == 3
                || grid[randomRow][randomCol - 1] == 3)){
            // Erase particle
            grid[randomRow][randomCol] = 0;
        }
    }

    /********************************************************************/
    /********************************************************************/
    /**
     * DO NOT MODIFY
     *
     * <p>The rest of this file is UI and testing infrastructure. Do not modify as part of pre-GDA Lab
     * 5.
     */
    /********************************************************************/
    /********************************************************************/

    private static class Point {
        private int row;
        private int column;

        public Point(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
    }

    /**
     * Special random number generating class to help get consistent results for testing.
     *
     * <p>Please use getRandomPoint to get an arbitrary point on the grid to evaluate.
     *
     * <p>When dealing with water, please use getRandomDirection.
     */
    public static class RandomGenerator {
        private static Random randomNumberGeneratorForPoints;
        private static Random randomNumberGeneratorForDirections;
        private int numRows;
        private int numCols;

        public RandomGenerator(int seed, int numRows, int numCols) {
            randomNumberGeneratorForPoints = new Random(seed);
            randomNumberGeneratorForDirections = new Random(seed);
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public RandomGenerator(int numRows, int numCols) {
            randomNumberGeneratorForPoints = new Random();
            randomNumberGeneratorForDirections = new Random();
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public Point getRandomPoint() {
            return new Point(
                    randomNumberGeneratorForPoints.nextInt(numRows),
                    randomNumberGeneratorForPoints.nextInt(numCols));
        }

        /**
         * Method that returns a random direction.
         *
         * @return an int indicating the direction of movement: 0: Indicating the water should attempt
         *     to move down 1: Indicating the water should attempt to move right 2: Indicating the water
         *     should attempt to move left
         */
        public int getRandomDirection() {
            return randomNumberGeneratorForDirections.nextInt(3);
        }
    }

    /**
     * Read a grid set up from the input scanner.
     *
     * @param in
     */
    private void readGridValues(Scanner in) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = in.nextInt();
            }
        }
    }

    /** Output the current status of the grid for testing purposes. */
    private void printGrid() {
        for (int i = 0; i < grid.length; i++) {
            System.out.println(Arrays.toString(grid[i]));
        }
    }

    /** Runner that advances the display a determinate number of times. */
    private void runNTimes(int times) {
        for (int i = 0; i < times; i++) {
            runOneTime();
        }
    }

    /** Runner that controls the window until it is closed. */
    public void run() {
        while (true) {
            runOneTime();
        }
    }

    /**
     * Runs one iteration of the display. Note that one iteration may call step repeatedly depending
     * on the speed of the UI.
     */
    private void runOneTime() {
        for (int i = 0; i < display.getSpeed(); i++) {
            step();
        }
        updateDisplay();
        display.repaint();
        display.pause(1); // Wait for redrawing and for mouse
        int[] mouseLoc = display.getMouseLocation();
        if (mouseLoc != null) { // Test if mouse clicked
            int rowClicked = mouseLoc[0];
            int colClicked = mouseLoc[1];
            int toolSelected = display.getTool();
            locationClicked(rowClicked, colClicked, toolSelected);
        }
    }

    /**
     * An implementation of the SandDisplayInterface that doesn't display anything. Used for testing.
     */
    static class NullDisplay implements SandDisplayInterface {
        private int numRows;
        private int numCols;

        public NullDisplay(int numRows, int numCols) {
            this.numRows = numRows;
            this.numCols = numCols;
        }

        public void pause(int milliseconds) {}

        public int getNumRows() {
            return numRows;
        }

        public int getNumColumns() {
            return numCols;
        }

        public int[] getMouseLocation() {
            return null;
        }

        public int getTool() {
            return 0;
        }

        public void setColor(int row, int col, Color color) {}

        public int getSpeed() {
            return 1;
        }

        public void repaint() {}
    }

    /** Interface for the UI of the SandLab. */
    public interface SandDisplayInterface {
        public void repaint();

        public void pause(int milliseconds);

        public int[] getMouseLocation();

        public int getNumRows();

        public int getNumColumns();

        public void setColor(int row, int col, Color color);

        public int getSpeed();

        public int getTool();
    }

    public static void main(String[] args) {
        // Test mode, read the input, run the simulation and print the result
        Scanner in = new Scanner(System.in);
        int numRows = in.nextInt();
        int numCols = in.nextInt();
        int iterations = in.nextInt();
        Solution lab =
                new Solution(new NullDisplay(numRows, numCols), new RandomGenerator(0, numRows, numCols));
        lab.readGridValues(in);
        lab.runNTimes(iterations);
        lab.printGrid();
    }
}

// Note: Project 6 features added
// 1) Added new particle: Cloud. It's white and drifts right until it hits something or disappears
// 2) Added new particle: Lava. It's red, falls, and burns through other particles
// 3) Added new particle: Fire. It's orange and moves randomly very briefly until burning out
// 4) Added new modeling feature: Fire is ignited when Lava contacts other particles
// 5) Added new modeling feature: Sand and Lava fall down rather than stacking up like a vertical column
// 6) Added new modeling feature: Sand particles can have 2 different colors, which are picked randomly