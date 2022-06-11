import java.awt.*;
import java.util.*;

public class Solution {

    // Add constants for particle types here.
    public static final int EMPTY = 0;
    public static final int METAL = 1;
    public static final int SAND = 2;
    public static final int WATER = 3;

    public static final String[] NAMES = {"Empty", "Metal", "Sand", "Water"};

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
        // Update the grid for every click event
        grid[row][col] = tool;
    }

    /** Copies each element of grid into the display. */
    public void updateDisplay() {
        // Redraw every grid position. Map tool values to colors
        for (int i = 0; i < grid.length; i++) {
            for (int a = 0; a < grid[i].length; a++) {
                if(grid[i][a] == 0){
                    display.setColor(i, a, Color.BLACK);
                } else if(grid[i][a] == 1){
                    display.setColor(i, a, Color.GRAY);
                } else if(grid[i][a] == 2){
                    display.setColor(i, a, Color.YELLOW);
                } else if(grid[i][a] == 3){
                    display.setColor(i, a, Color.BLUE);
                }
            }
        }
    }

    /** Called repeatedly. Causes one random particle to maybe do something. */
    // A single invocation of step should just attempt to move one particle in one square
    public void step() {
        // TODO: Populate this method in step 6 and beyond.
        // Get random point on the grid
        int totalRows = display.getNumRows();
        int totalCol = display.getNumColumns();
        Point randomPoint = random.getRandomPoint();
        int randomRow = randomPoint.row;
        int randomCol = randomPoint.column;
        int maxCol = display.getNumColumns() - 1;
        int maxRow = display.getNumRows() - 1;
        // Sand down movement: If the particle is sand and below is empty, move it down 1
        if (grid[randomRow][randomCol] == 2
                && randomRow < maxRow // out of bounds check
                && grid[randomRow + 1][randomCol] == 0) {
            // Update grid
            grid[randomRow][randomCol] = 0;
            grid[randomRow + 1][randomCol] = 2;
        }
        // Sand down movement in water: If the particle is sand and water is below, swap positions (so sand will sink)
        else if (grid[randomRow][randomCol] == 2
                && randomRow < maxRow
                && grid[randomRow + 1][randomCol] == 3) {
            // Update grid
            grid[randomRow][randomCol] = 3;
            grid[randomRow + 1][randomCol] = 2;
        }
        // If the particle is water, move in a random direction that is free
        else if (grid[randomRow][randomCol] == 3) {
            // Get random direction (for water)
            int randomDir = random.getRandomDirection();
            // First, out of bounds check in all 3 directions
            if((randomRow == maxRow && randomDir == 0)
               || (randomCol == maxCol && randomDir == 1)
               || (randomCol == 0 && randomDir == 2)) {}
            // Move in the random dir if that space is open
            else if (randomDir == 0
                     && grid[randomRow + 1][randomCol] == 0) {
                grid[randomRow][randomCol] = 0;
                grid[randomRow + 1][randomCol] = 3;
            } else if (randomDir == 1
                       && grid[randomRow][randomCol + 1] == 0) {
                grid[randomRow][randomCol] = 0;
                grid[randomRow][randomCol + 1] = 3;
            } else if (randomDir == 2
                    && grid[randomRow][randomCol - 1] == 0) {
                grid[randomRow][randomCol] = 0;
                grid[randomRow][randomCol - 1] = 3;
            }
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
