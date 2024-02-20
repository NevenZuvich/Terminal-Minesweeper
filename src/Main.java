// written and created by Neven Zuvich, zuvic003

import java.util.Scanner;

public class Main {
    public static boolean debugInput(Scanner stdin) {
        System.out.println("Would you like to run the game in debug mode or not?");
        System.out.println("Enter Yes or No exactly: ");
        String input = stdin.nextLine();
        while (!input.equals("Yes") &&  !input.equals("No")) { // ensures users gives a valid input
            System.out.print("You entered something wrong. ");
            System.out.println("Remember it must be Yes or No exactly, capitals included");
            input = stdin.nextLine(); // get new input
        }
        if (input.equals("Yes")) {
            System.out.println("Ok. Debug mode is on.");
            return true;
        } else {
            System.out.println("Ok. Debug mode is off.");
            return false;
        }
    }

    public static char difficultyInput(Scanner stdin) {
        // returns a car for difficulty 'E' for Easy, 'M' for medium, 'H' for hard
        String input = stdin.nextLine();
        while (!input.equals("Easy") && !input.equals("Medium") && !input.equals("Hard")) { // while input is invalid
            System.out.print("You entered the difficulty in wrong. ");
            System.out.println("Remember, it must be exactly Easy, Medium or Hard, capitals included.");
            System.out.println("Please reenter: ");
            input = stdin.nextLine();
        }
        switch(input) {
            case "Easy":
                return 'E';
            case "Medium":
                return 'M';
            case "Hard":
                return 'H';
            default:
                return 'E'; // if something goes wrong, default to easy
        }
    }


    public static int[] startingCoordInput(Scanner stdin, Minefield minefield) {
        // returns int array, 0 index is x coord input, 1 index is y
        int x, y;
        String xStr, yStr;
        try {
            // parseInt() throws a NumberFormatException if the user just passes a string
            xStr = stdin.next(); x = Integer.parseInt(xStr);
            yStr = stdin.next(); y = Integer.parseInt(yStr);
            while (!minefield.inBounds(x, y)) { // makes sure coordinates are inbounds
                System.out.println("The coordinates you entered were out of bounds.");
                System.out.println("Please reenter with both x and y between 0 and " + minefield.getSideLength() + " inclusive.");
                xStr = stdin.next(); x = Integer.parseInt(xStr);
                yStr = stdin.next(); y = Integer.parseInt(yStr);
                // parseInt() throws a NumberFormatException if the user just passes a string
            }
        }
        catch (NumberFormatException e) { // catches if user doesn't input integers at any point
            /* I found out parseInt() threw a NumberFormatException when not given an integer String from:
             https://www.javatpoint.com/java-integer-parseint-method */
            stdin.nextLine(); // clears out incorrect previous input
            System.out.println("You input a letter or a string of characters. You must enter integers.");
            System.out.print("Those integers must also be between 0 and " + minefield.getSideLength() + " inclusive for x and y, ");
            System.out.println("Remember, there is also no way to remove a placed flag, so be careful when placing one.");
            System.out.println("Please reenter: [x] [y]:");
            return startingCoordInput(stdin, minefield);
        }
        return new int[] {x, y};
    }

    public static int[] coordInput(Scanner stdin, Minefield minefield) {
        // returns int array, 0 index is x coord input, 1 index is y, 2 is if flag is placed (0 if not, 1 if so)
        int x, y;
        int flagNum; // 0 if no flag 1 if user wants to place a flag
        String xStr, yStr, flagStr; // temporary variables
        try {
            xStr = stdin.next(); x = Integer.parseInt(xStr);
            yStr = stdin.next(); y = Integer.parseInt(yStr);
            while (!minefield.inBounds(x, y)) { // ensures coordinate inputs are valid
                System.out.println("The coordinates you entered were out of bounds.");
                System.out.println("Please reenter with both x and y between 0 and " + minefield.getSideLength() + " inclusive.");
                xStr = stdin.next(); x = Integer.parseInt(xStr);
                yStr = stdin.next(); y = Integer.parseInt(yStr);
            }
            flagStr = stdin.next(); flagNum = Integer.parseInt(flagStr);
            while (flagNum != 0 && flagNum != 1) { // ensures flag inputs are valid
                    System.out.println("You entered the flag number wrong. It must be either 0 for no flag or 1 for flag.");
                    System.out.println("Reenter the flag number: ");
                    flagStr = stdin.next(); flagNum = Integer.parseInt(flagStr);
            }
        }
        catch (NumberFormatException e) { // catches if user doesn't input integers, I'm not allowed to import java.util
            /* I found out parseInt() threw a NumberFormatException when not given an integer String from:
             https://www.javatpoint.com/java-integer-parseint-method */
            stdin.nextLine(); // clears out incorrect previous input
            System.out.println("You input a letter or a string of characters. You must enter integers.");
            System.out.print("Those integers must also be between 0 and " + minefield.getSideLength() + " inclusive for x and y, ");
            System.out.println("and 1 or 0 for if you want a flag or not (1 for yes, 0 for no).");
            System.out.println("Remember, there is also no way to remove a placed flag, so be careful when placing one.");
            System.out.println("Please reenter: [x] [y] [f] (1 for yes flag 0 for no flag)");
            return coordInput(stdin, minefield);
        }
        return new int[] {x, y, flagNum};
    }

    public static Minefield makeMinefield(Scanner stdin) {
        // handles getting difficulty
        System.out.println("What difficulty would you like, Easy, Medium, or Hard?");
        char difficulty = difficultyInput(stdin); // returns 'E' for easy, 'M' for medium, 'H' for Hard
        int mines, sideLen;
        if (difficulty == 'E') {
            mines = 5; // also the number of flags
            sideLen = 5;
        } else if (difficulty == 'M') {
            mines = 12; // also the number of flags
            sideLen = 9;
        } else {
            mines = 40; // also the number of flags
            sideLen = 20;
        }
        Minefield minefield = new Minefield(sideLen,sideLen, mines);
        System.out.println("Enter starting coordinates, both between 0 and " + minefield.getSideLength() + " inclusive: [x] [y]");
        int[] coordArr = startingCoordInput(stdin, minefield);
        minefield.createMines(coordArr[0], coordArr[1], mines);
        minefield.evaluateField();
        minefield.revealMines(coordArr[0], coordArr[1]);
        return minefield;
    }

    public static void main(String[] args) {
        Scanner stdin = new Scanner(System.in);
        System.out.println("Welcome to minesweeper.");
        boolean debugOn = debugInput(stdin); // deals with welcoming user, getting debug input
        Minefield minefield = makeMinefield(stdin); // deals with difficulty input, starting coords, and making the minefield accordingly
        int x = 0, y = 0, placeFlag; // for placeFlag, 1 if yes, 0 if no
        int[] coordArr;
        while (!minefield.gameOver()) { // gameloop
            if (debugOn)
                minefield.printMinefield();
            System.out.println(minefield);
            System.out.print("Enter a coordinate and if you wish to place a flag there. Format: [x] [y] [f], ");
            System.out.println("for f, 1 if you wish to place a flag, 0 if not. There is no way to remove a placed flag, so be careful when placing one.");
            coordArr = coordInput(stdin, minefield); // gets correct input from user
            x = coordArr[0]; y = coordArr[1]; placeFlag = coordArr[2];
            minefield.guess(x, y, placeFlag==1);
        }
        // prints out final state of the field
        if (debugOn)
            minefield.printMinefield();
        System.out.println(minefield);
        if (minefield.isMineHit()) { // user hit a mine (lost)
            System.out.print("Sorry, you lost the game! You hit the mine at " + x + ", " + y + ". ");
        } else
            System.out.print("You won the game! Good job! ");
        System.out.println("Thanks for playing. To play again, rerun the program.");
        stdin.close();
    }
}