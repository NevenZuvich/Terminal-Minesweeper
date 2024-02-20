// written and modified by Neven Zuvich, zuvic003

import java.util.Random;

public class Minefield {

    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001b[34m";
    public static final String ANSI_RED = "\u001b[31m";
    public static final String ANSI_DARK_GREEN = "\u001b[32m";
    public static final String ANSI_GREY_BG = "\u001b[0m";

    // extra custom colors gotten from: https://ss64.com/nt/syntax-ansi.html
    public static final String ANSI_CYAN = "\u001b[96m";
    public static final String ANSI_LIGHT_YELLOW = "\u001b[93m";
    public static final String ANSI_DARK_MAGENTA = "\u001b[95m";
    public static final String ANSI_LIGHT_GREEN = "\u001b[92m";
    public static final String ANSI_WHITE = "\u001b[97m";

    public boolean inBounds(int x, int y) { // returns if input coords are in bounds of the board
        return 0 <= x && x < field.length && 0 <= y && y < field.length;
    }

    public Minefield(int rows, int columns, int flags) {
        field = new Cell[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                field[row][col] = new Cell(false, "-");
            }
        }
        numFlags = flags;
    }

    public void evaluateField() {
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field.length; col++) { // loops through whole board
                if (!field[row][col].getStatus().equals("M")) { // if it's not a mine
                    String numMines = minesSurrounding(col, row); // get a string representation of the num of mines
                    field[row][col].setStatus(numMines); // set its status to that string
                }
            }
        }
    }

    public String minesSurrounding(int col, int row) {
        // returns a string of the number of mines in the 3x3 area around row and col
        int numMines = 0;
        for (int i = -1; i < 2; i++) { // counts row of 3 above row and col
            if (inBounds(col+i, row-1) && field[row-1][col+i].getStatus().equals("M"))
                numMines++;
        }
        for (int i = -1; i < 2; i++) { // counts row of 3 below row and col
            if (inBounds(col+i, row+1) && field[row+1][col+i].getStatus().equals("M"))
                numMines++;
        }
        for (int i = -1; i < 2; i+=2) { // checks left and right of row and col
            if (inBounds(col+i, row) && field[row][col+i].getStatus().equals("M"))
                numMines++;
        }
        return "" + numMines; // gives string of number of mines
    }

    public void createMines(int x, int y, int mines) {
        Random r = new Random();
        while (0 < mines) {
            int mineX = r.nextInt(field.length); // generates a random in-bounds x coord for a potential mine
            int mineY = r.nextInt(field.length); // generates a random in-bounds y coord for a potential mine
            Cell randCell = field[mineY][mineX];
            if ((mineX != x || mineY != y)  // not start coord
                && !randCell.getStatus().equals("M")  // and not already a mine
                && !randCell.getRevealed()) { // and not already revealed
                randCell.setStatus("M");
                mines--;
            }
        }
    }

    public boolean guess(int x, int y, boolean flag) {
        if (inBounds(x, y)) { // coord is in bounds
            if (flag && numFlags <= 0) { // if tried to place flags, but out of flags
                System.out.println("Sorry, you are out of flags.");
            } else if (flag && numFlags > 0) { // if tried to place flags and not out of flags
                field[y][x].setStatus("F");
                numFlags--;
            } else if (field[y][x].getStatus().equals("0")) // if guessed cell is a 0
                revealZeroes(x, y);
            else if (field[y][x].getStatus().equals("M")) // if guessed cell is a mine
                mineHit = true;
            field[y][x].setRevealed(true);
        }
        return true;
    }

    public boolean gameOver() {
        if (mineHit)
            return true;
        else {
            for (int row = 0; row < field.length; row++) {
                for (int col = 0; col < field.length; col++) {
                    if (field[col][row].getStatus() != "M" && !field[col][row].getRevealed())
                        // if there's a non-mine cell that is unrevealed
                        return false;
                }
            }
            return true;
        }
    }

    public void revealZeroes(int x, int y) {
        if (inBounds(x, y)) {
            Stack1Gen<int[]> fieldStack = new Stack1Gen<int[]>(); // stack of the coordinates of cells
            int[] currCoords = new int[] {x, y};
            fieldStack.push(currCoords);
            while (!fieldStack.isEmpty()) {
                currCoords = fieldStack.pop();
                x = currCoords[0]; y = currCoords[1];
                field[y][x].setRevealed(true);
                // top neighbor
                if (inBounds(x, y-1) && // in bounds
                        !field[y-1][x].getRevealed() &&  // not revealed
                        field[y-1][x].getStatus().equals("0")) // is a 0
                    fieldStack.push(new int[]{x, y - 1}); // add top neighbor coords to stack
                // right neighbor
                if (inBounds(x+1, y) && // in bounds
                        !field[y][x+1].getRevealed() &&  // not revealed
                        field[y][x+1].getStatus().equals("0")) // is a 0
                    fieldStack.push(new int[]{x + 1, y}); // add right neighbor coords to stack
                // below neighbor
                if (inBounds(x, y+1) && // in bounds
                        !field[y+1][x].getRevealed() &&  // not revealed
                        field[y+1][x].getStatus().equals("0"))  // is a 0
                    fieldStack.push(new int[]{x, y + 1}); // add below neighbor coords to stack
                // left neighbor
                if (inBounds(x-1, y) && // in bounds
                        !field[y][x-1].getRevealed() &&  // not revealed
                        field[y][x-1].getStatus().equals("0")) // is a 0
                    fieldStack.push(new int[]{x - 1, y}); // add left neighbor coords to stack
            }
        }
    }

    public void revealMines(int x, int y) {
        if (inBounds(x, y)) {
            Q1Gen<int[]> fieldQ = new Q1Gen<int[]>(); // queue of the coordinates of the cells
            int[] currCoords = new int[] {x, y};
            fieldQ.add(currCoords);
            boolean mineFound = false;
            boolean[][] visited = new boolean[field.length][field.length];
            while (fieldQ.length() != 0 && !mineFound) {
                currCoords = fieldQ.remove();
                x = currCoords[0]; y = currCoords[1];
                field[y][x].setRevealed(true);
                visited[y][x] = true;
                if (field[y][x].getStatus().equals("M")) {
                    mineFound = true;
                }
                // top neighbor
                if (inBounds(x, y-1) && // in bounds
                        !visited[y-1][x])  // not revealed
                    fieldQ.add(new int[] {x, y-1});
                // right
                if (inBounds(x+1, y) && // in bounds
                        !visited[y][x+1]) // not revealed
                    fieldQ.add(new int[] {x+1, y});
                // below
                if (inBounds(x, y+1) && // in bounds
                        !visited[y+1][x]) // not revealed
                    fieldQ.add(new int[] {x, y+1});
                // left
                if (inBounds(x-1, y) && // in bounds
                        !visited[y][x-1]) // not revealed
                    fieldQ.add(new int[] {x-1, y});
            }
        }
    }

    public void printMinefield() {
        String finalStr = "   ";
        for (int i = 0; i<field.length; i++){ // prints out column numbers
            finalStr += String.format("%2s", i) + " ";
        }
        finalStr+="\n";
        for (int row = 0; row < field.length; row++) {
            finalStr += String.format("%2s", row) + " "; // for row numbers
            for (int col = 0; col < field.length; col++) {
                // gets the right color and format for this cell
                String helpStr = formatPrint(field[row][col].getStatus()) + ANSI_GREY_BG;
                finalStr += helpStr + " ";
            }
            finalStr += "\n"; // new line between rows
        }

        System.out.println(finalStr);
    }

    public String toString() {
        String finalStr = "   ";
        for (int i = 0; i<field.length; i++){ // prints out column numbers
            finalStr += String.format("%2s", i) + " ";
        }
        finalStr += "\n";
        for (int row = 0; row < field.length; row++) {
            finalStr += String.format("%2s", row) + " "; // for row numbers
            for (int col = 0; col < field.length; col++) {
                if (field[row][col].getRevealed()) { // if cell is revealed
                    // gets the right format and color for this cell
                    String helpStr = formatPrint(field[row][col].getStatus()) + ANSI_GREY_BG;
                    finalStr += helpStr + " ";
                } else // if it's not revealed
                    finalStr += " - ";
            }
            finalStr += "\n"; // new line between rows
        }
        return finalStr;
    }

    public static String formatPrint(String chr) {
        // returns strings for the toString() and printMinefield() with the correct format and color
        // format: 2 characters wide, right justified
        switch (chr) {
            case ("F"):
                return String.format("%2s", "F");
            case ("M"):
                return String.format("%2s", "M");
            case ("-"):
                return String.format("%2s", "-");
            case ("0"):
                return ANSI_CYAN + String.format("%2s", "0");
            case ("1"):
                return ANSI_LIGHT_GREEN + String.format("%2s", "1");
            case ("2"):
                return ANSI_LIGHT_YELLOW + String.format("%2s", "2");
            case ("3"):
                return ANSI_RED + String.format("%2s", "3");
            case ("4"):
                return ANSI_BLUE + String.format("%2s", "4");
            case ("5"):
                return ANSI_YELLOW + String.format("%2s", "5");
            case ("6"):
                return ANSI_DARK_MAGENTA + String.format("%2s", "6");
            case ("7"):
                return ANSI_WHITE + String.format("%2s", "7");
            case ("8"):
                return ANSI_DARK_GREEN + String.format("%2s", "8"); // dark green isn't very distinct from light, but 8 is very rare
            // 9 or more is impossible
            default:
                return "\nERROR when printing board: invalid character " + chr + " passed.\n";
        }
    }

    public int getSideLength() {
        return field.length;
    }

    public boolean isMineHit() {
        return mineHit;
    }

    // member variables
    private Cell[][] field; // 2d array representing the minefield
    private int numFlags = 0;
    private boolean mineHit = false; // boolean variable to keep track of status of the game -- if ended or not

}
