package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import edu.princeton.cs.introcs.Stopwatch;

import java.awt.*;
import java.lang.Long;
import java.io.*;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 90;
    public static final int HEIGHT = 45;
    private TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
    private Long randomSeed;
    private ArrayList<String> readArray = new ArrayList<>();
    public static ArrayList<Long> seedList = new ArrayList<>();
    public static int obstacles = 0;
    public static int strikes = 0;
    public static int coins = 0;
    private boolean darkMode = false;
    private String level;
    private String mode;
    private Stopwatch sw = new Stopwatch();
    private int startingTime;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        startGame();
    }


    /* @source https://stackoverflow.com/questions/14974033/extract-digits-from-string-stringutils-java */
    /* @source https://www.tutorialspoint.com/java/lang/long_parselong.htm */
    public TETile[][] interactWithInputString(String input) {
        /** 
        * runs the engine using the input passed in as an argument, 
        * and return a 2D tile representation of the world that would have been drawn 
        * if the same inputs had been given to interactWithKeyboard().
        */
      
        char[] characterArray = input.toCharArray();

        if (input.charAt(0) == 'l' || input.charAt(0) == 'L') {
            load();

            for (int i = 0; i < characterArray.length; i++) {
                char move = Character.toUpperCase(characterArray[i]);
                if (move == 'W') {
                    moveUp(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'A') {
                    moveLeft(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'S') {
                    moveDown(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'D') {
                    moveRight(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == ':') {
                    char c = Character.toUpperCase(characterArray[i+1]);
                    if (c == 'Q') {
                        quit();
                    }
                }
            }
        }

        /** check that input has numbers in between 'N' and 'S' **/
        else if (input.replaceAll("[^0-9]", "").length() < 1) {
            System.out.println("Please input a valid string with number(s) in between 'N' and 'S'");
            return null;
        }

        /** check that input starts with 'N' **/
        else if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
            randomSeed = Long.parseLong(input.replaceAll("[^0-9]", ""));
            generateWorld();

            /** find the first s and replace it with a Z **/
            for (int i = 0; i < characterArray.length; i++) {
                if (characterArray[i] == 's' || characterArray[i] == 'S') {
                    characterArray[i] = 'Z';
                    break;
                }
            }

            for (int i = 0; i < characterArray.length; i++) {
                char move = Character.toUpperCase(characterArray[i]);
                if (move == 'W') {
                    moveUp(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'A') {
                    moveLeft(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'S') {
                    moveDown(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'D') {
                    moveRight(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == ':') {
                    char c = Character.toUpperCase(characterArray[i+1]);
                    if (c == 'Q') {
                        quit();
                    }
                }
            }
        }

        return finalWorldFrame;
    }

    public void createFrame() {
        /* frame width and height set equal to 50 */
        StdDraw.setCanvasSize(800, 800);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    private void drawMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "Quit (Q)");
        StdDraw.show();
        processInput();
    }

    private void processInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N') {
                    chooseMode();
                    //chooseLevel();
                    //rules();
                    collectSeed();
                    break;
                } else if (c == 'L') {
                    load();
                    processCommands();
                } else if (c == 'Q') {
                    quit();
                }
                else {
                    break;
                }
            }
        }
    }

    private void collectSeed() {
        StdDraw.clear(Color.GRAY);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.setPenColor(Color.BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Enter an integer to start the game");
        StdDraw.show();

        String input = "";
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'S') {
                    randomSeed = Long.parseLong(input); // the seed converted from a string to a long
                    break;
                }
                else if (c != 'N') {
                    input += c; // I only want integers
                }
            }
        }
    }

    private void chooseLevel() {
        StdDraw.clear(Color.GRAY);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.setPenColor(Color.BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Choose a level to start at.");
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 3, "Easy (A)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "Moderate (B)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 7, "Hard (C)");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'A') {
                    obstacles = 6;
                    startingTime = 200;
                    level = "A";
                    break;
                } else if (c == 'B') {
                    obstacles = 10;
                    startingTime = 150;
                    level = "B";
                    break;
                } else if (c == 'C') {
                    obstacles = 14;
                    startingTime = 60;
                    level = "C";
                    break;
                }
            }
        }
    }

    private void chooseMode() {
        StdDraw.clear(Color.GRAY);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.setPenColor(Color.BLUE);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Choose screen mode for the game.");
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 3, "Light (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 5, "Dark (D)");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
               if (c == 'D') {
                   mode = "dark";
                   darkMode = true;
                   break;
                } else if (c == 'L') {
                   mode = "light";
                   darkMode = false;
                   break;
               }
            }
        }
    }

    private void rules() {
        Stopwatch rw = new Stopwatch();
        while (rw.elapsedTime() <= 8) {
            StdDraw.clear(Color.GRAY);
            StdDraw.setPenColor(Color.WHITE);
            Font fontBig = new Font("Monaco", Font.BOLD, 40);
            StdDraw.setFont(fontBig);
            StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
            Font fontSmall = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(fontSmall);
            StdDraw.setPenColor(Color.BLUE);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 + 5, "Rules of the Game:");
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 + 1, "You are on level " + level + ".");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 1, "You are on " + mode + " mode.");
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 6, "To win the game, collect the three");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 8, "stars before the timer runs out.");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 10, "Every time you run into either a wall");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 12, "or an obstacle, you gain a strike.");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 14, "Reaching three strikes and/or running");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 16, "out of time means you lose the game.");
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 18, "Best of luck!");
            StdDraw.show();
        }
    }

    private void quit() {
        saveWorld();
        System.exit(0);
    }

    /* @source: https://www.geeksforgeeks.org/different-ways-reading-text-file-java/ */
    public void load()  {
        File worldFile = new File("/Users/olaal/Desktop/cs_courses/cs61b/fa20-s281/proj3/byow/Core/savedWorld.txt"); // specify path
        try {
            if (worldFile.exists()) {
                Scanner sc = new Scanner(worldFile);
                String savedSeed = sc.nextLine(); // read the seed
                readArray.add(savedSeed); // save the seed in the array

                String savedAvatarX = sc.nextLine(); // read the avatar's X position
                String savedAvatarY = sc.nextLine(); // read the avatar's Y position

                readArray.add(savedAvatarX); // save the avatar's X position in the array
                readArray.add(savedAvatarY); // save the avatar's Y position in the array

                String savedMode = sc.nextLine(); // read the display mode

                if (savedMode.equals("dark")) {
                    darkMode = true;
                } else {
                    darkMode = false;
                }

                mode = savedMode;
                randomSeed = Long.parseLong(readArray.get(0)); // update instance variable
                seedList.add(randomSeed); // add every seed we get/every world we generate

                WorldGeneration.avatarXPos = Integer.parseInt(String.valueOf(readArray.get(1)));
                WorldGeneration.avatarYPos = Integer.parseInt(String.valueOf(readArray.get(2)));

                for (int i = 0; i < 3; i++) { // delete items in list
                    readArray.remove(0);
                }
                generateWorld();
            }
        }
        catch (IOException e) {
            System.out.println("Exception occurred:" + e);
            e.printStackTrace();
        }
    }

    /* @source: https://www.tutorialspoint.com/javaexamples/file_append.htm */
    /* @source: https://stackoverflow.com/questions/6994518/how-to-delete-the-content-of-text-file-without-deleting-itself */
    /* @source: https://stackoverflow.com/questions/3844307/how-to-read-file-from-relative-path-in-java-project-java-io-file-cannot-find-th/43271117 */
    private void saveWorld() {
        String avatarX = Integer.toString(WorldGeneration.avatarXPos);
        String avatarY = Integer.toString(WorldGeneration.avatarYPos);
        String seed = String.valueOf(randomSeed);// randomSeed as a string
        String m = mode;
        File worldFile = new File("/Users/olaal/Desktop/cs_courses/cs61b/fa20-s281/proj3/byow/Core/savedWorld.txt"); // specify path

        try {
            if (!worldFile.exists()) {
                worldFile.createNewFile(); //create the text file if it does not exist
            } else {
                PrintWriter writer = new PrintWriter(worldFile);
                writer.print(""); // clears the text file
                writer.close();
            }
            FileWriter fw1 = new FileWriter(worldFile,true); // true to append string
            BufferedWriter bw = new BufferedWriter(fw1);
            bw.write(seed + "\n"); // add the seed
            bw.close();
            FileWriter fw2 = new FileWriter(worldFile,true); // true to append string
            bw = new BufferedWriter(fw2);
            bw.write(avatarX + "\n"); // add the avatar's X position
            bw.close();
            FileWriter fw3 = new FileWriter(worldFile,true); // true to append string
            bw = new BufferedWriter(fw3);
            bw.write(avatarY + "\n"); // add the avatar's Y position
            bw.close();
            FileWriter fw4 = new FileWriter(worldFile,true); // true to append string
            bw = new BufferedWriter(fw4);
            bw.write(m + "\n"); // add the display mode
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Exception occurred:" + e);
            e.printStackTrace();
        }
    }

    private void processCommands() {
        while (true) {
            HUD();
            StdDraw.enableDoubleBuffering();
            if (StdDraw.hasNextKeyTyped()) {
                char move = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (move == 'W') {
                    moveUp(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'A') {
                    moveLeft(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'S') {
                    moveDown(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == 'D') {
                    moveRight(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
                } else if (move == ':') {
                    while (true) {
                        HUD();
                        StdDraw.enableDoubleBuffering();
                        if (StdDraw.hasNextKeyTyped()) {
                            char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                            if (c == 'Q') {
                                quit(); // save and quit the world
                            }
                        }
                    }
                }
            }
        }
    }
    private void displayScores() {
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.setFont();
        StdDraw.text(WIDTH - 4, HEIGHT - 1, "Stars: " + coins);
        StdDraw.text(WIDTH - 4, HEIGHT - 2, "Strikes: " + strikes);
        StdDraw.text(WIDTH - 4, HEIGHT - 3, "Time left: " + (int) ((int) startingTime - sw.elapsedTime()));
        StdDraw.show();
        StdDraw.enableDoubleBuffering();

        if (coins == 3 && ((int) ((int) startingTime - sw.elapsedTime()) >= 0)) {
            winGame();
        } else if (((int) ((int) startingTime - sw.elapsedTime()) <= 0)) {
            //timesUp();
        } else if (strikes >= 3) {
            outOfStrikes();
        }
    }
    private boolean validMove(int x, int y) {
        return finalWorldFrame[x][y] != Tileset.WALL;
    }
    private void moveUp(int x, int y) {
        if (validMove(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos + 1)) {
            WorldGeneration.avatarYPos = WorldGeneration.avatarYPos + 1; // update position
            if (finalWorldFrame[WorldGeneration.avatarXPos][WorldGeneration.avatarYPos] == Tileset.SAND) {
                coins++;
            }
            move(x, y, WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
        } else {
            strikes++;
        }
    }
    private void moveLeft(int x, int y) {
        if (validMove(WorldGeneration.avatarXPos - 1, WorldGeneration.avatarYPos)) {
            WorldGeneration.avatarXPos = WorldGeneration.avatarXPos - 1; // update position
            if (finalWorldFrame[WorldGeneration.avatarXPos][WorldGeneration.avatarYPos] == Tileset.SAND) {
                coins++;
            }
            move(x, y, WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
        } else {
            strikes++;
        }
    }
    private void moveDown(int x, int y) {
        if (validMove(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos - 1)) {
            WorldGeneration.avatarYPos = WorldGeneration.avatarYPos - 1; // update position
            if (finalWorldFrame[WorldGeneration.avatarXPos][WorldGeneration.avatarYPos] == Tileset.SAND) {
                coins++;
            }
            move(x, y, WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
        } else {
            strikes++;
        }
    }
    private void moveRight(int x, int y) {
        if (validMove(WorldGeneration.avatarXPos + 1, WorldGeneration.avatarYPos)) {
            WorldGeneration.avatarXPos = WorldGeneration.avatarXPos + 1; // update position
            if (finalWorldFrame[WorldGeneration.avatarXPos][WorldGeneration.avatarYPos] == Tileset.SAND) {
                coins++;
            }
            move(x, y, WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);
        } else {
            strikes++;
        }
    }
    private void move(int startX, int startY, int endX, int endY) {
        finalWorldFrame[endX][endY] = Tileset.AVATAR;
        finalWorldFrame[startX][startY] = Tileset.FLOOR;
        ter.renderFrame(finalWorldFrame); // anytime we interact with the world frame, we need to re-render the display
        StdDraw.enableDoubleBuffering();

        if (darkMode == true) {
            saveWorld();
            load();// regenerates world
            darkMode();
        }
    }

    /* @source: https://www.javatpoint.com/java-get-current-date */
    private void HUD() {
        double mouseXPos = StdDraw.mouseX();
        double mouseYPos = StdDraw.mouseY();
        //displayScores();

        String description = finalWorldFrame[(int) mouseXPos][(int) mouseYPos].description();
        if (inBounds(mouseXPos, mouseYPos)) {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont();
            StdDraw.text(WIDTH / 2, HEIGHT - 1, "Tile: " + description);
            StdDraw.setPenColor(Color.MAGENTA);
            StdDraw.textLeft(2, HEIGHT - 1, "Date: " + java.time.LocalDate.now());
            StdDraw.textLeft(2, HEIGHT - 2, "Time: " + java.time.LocalTime.now());
            StdDraw.show();
        } else {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont();
            StdDraw.text(WIDTH / 2, HEIGHT - 1, "Tile: " + description);
            StdDraw.setPenColor(Color.MAGENTA);
            StdDraw.textLeft(2, HEIGHT - 1, "Date: " + java.time.LocalDate.now());
            StdDraw.textLeft(2, HEIGHT - 2, "Time: " + java.time.LocalTime.now());
            StdDraw.show();
        }
        ter.renderFrame(finalWorldFrame); // anytime we interact with the world frame, we need to re-render the display
        StdDraw.enableDoubleBuffering();
    }

    /* commented out for lab demo purposes (bc timer interferes with reloading the game) */
    private void timesUp() {
        Stopwatch rw = new Stopwatch();
        while (rw.elapsedTime() <= 6) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font fontBig = new Font("Monaco", Font.BOLD, 40);
            StdDraw.setFont(fontBig);
            StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
            Font fontSmall = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(fontSmall);
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(WIDTH / 2, HEIGHT / 2, "Times up!");
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "You have lost the game :(");
            StdDraw.show();
        }
        quit();
    }

    private void outOfStrikes() {
        Stopwatch rw = new Stopwatch();
        while (rw.elapsedTime() <= 6) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font fontBig = new Font("Monaco", Font.BOLD, 40);
            StdDraw.setFont(fontBig);
            StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
            Font fontSmall = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(fontSmall);
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(WIDTH / 2, HEIGHT / 2, "You have hit 3 strikes!");
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "You have lost the game :(");
            StdDraw.show();
        }
        quit();
    }

    private void winGame() {
        Stopwatch rw = new Stopwatch();
        while (rw.elapsedTime() <= 6) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            Font fontBig = new Font("Monaco", Font.BOLD, 40);
            StdDraw.setFont(fontBig);
            StdDraw.text(WIDTH / 2, HEIGHT - 10, "SPACE EXPLORATION");
            Font fontSmall = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(fontSmall);
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(WIDTH / 2, HEIGHT / 2, "Congrats!");
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "You have won the game :)");
            StdDraw.show();
        }
        quit();
    }

    private boolean inBounds(double x, double y) {
        return finalWorldFrame[(int) x][(int) y] != Tileset.NOTHING;
    }

    private void generateWorld() {
        WorldGeneration WG = new WorldGeneration(randomSeed, WIDTH, HEIGHT, finalWorldFrame);
        StdDraw.enableDoubleBuffering();
        if (darkMode == true) {
            darkMode();
        }
    }

    private void darkMode() {
        Coordinate upperLeft = new Coordinate(WorldGeneration.avatarXPos - 1, WorldGeneration.avatarYPos + 1);
        Coordinate upperMiddle = new Coordinate(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos + 1);
        Coordinate upperRight = new Coordinate(WorldGeneration.avatarXPos + 1, WorldGeneration.avatarYPos + 1);
        Coordinate MiddleLeft = new Coordinate(WorldGeneration.avatarXPos - 1, WorldGeneration.avatarYPos);
        Coordinate MiddleRight = new Coordinate(WorldGeneration.avatarXPos + 1, WorldGeneration.avatarYPos);
        Coordinate lowerLeft = new Coordinate(WorldGeneration.avatarXPos - 1, WorldGeneration.avatarYPos - 1);
        Coordinate lowerMiddle = new Coordinate(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos - 1);
        Coordinate lowerRight = new Coordinate(WorldGeneration.avatarXPos + 1, WorldGeneration.avatarYPos - 1);
        Coordinate avatar = new Coordinate(WorldGeneration.avatarXPos, WorldGeneration.avatarYPos);

        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if ((x != upperLeft.getX() || y != upperLeft.getY())
                        && (x != upperMiddle.getX() || y != upperMiddle.getY())
                        && (x != upperRight.getX() ||  y !=  upperRight.getY())
                        && (x != MiddleLeft.getX() ||  y != MiddleLeft.getY())
                        && (x != MiddleRight.getX() || y != MiddleRight.getY())
                        && (x != lowerLeft.getX() || y != lowerLeft.getY())
                        && (x != lowerMiddle.getX() || y !=  lowerMiddle.getY())
                        && (x != lowerRight.getX() || y !=  lowerRight.getY())
                        && (x != avatar.getX() || y!= avatar.getY())) {
                    finalWorldFrame[x][y] = Tileset.NOTHING;
                }
            }
        }
    }

    private void startGame() {
        createFrame();
        drawMenu();
        generateWorld();
        processCommands();
    }

    public static void main(String[] args) {
        Engine game = new Engine();
        game.startGame();
        //game.interactWithInputString("l");
    }
}
