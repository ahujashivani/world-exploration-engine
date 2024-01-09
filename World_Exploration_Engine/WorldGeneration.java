package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Random;
import byow.TileEngine.TERenderer;

public class WorldGeneration {
    private TETile[][] finalWorldFrame;
    private int WIDTH;
    private int HEIGHT;
    private Random randomSeed;
    private long seed;
    private ArrayList<Room> roomList = new ArrayList<>();
    private ArrayList<Door> doorList = new ArrayList<>();
    public static int avatarXPos;
    public static int avatarYPos;

    WorldGeneration(long seed, int WIDTH, int HEIGHT, TETile[][] finalWorldFrame) {
        this.seed = seed;
        this.randomSeed = new Random(seed);
        this.WIDTH = WIDTH; // entire world's width
        this.HEIGHT = HEIGHT; // entire world's height
        this.finalWorldFrame = finalWorldFrame;

        /** initialize the tile rendering engine with a window of size WIDTH x HEIGHT **/
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        /** initialize tiles to "nothing" **/
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                finalWorldFrame[x][y] = Tileset.NOTHING;
            }
        }

        createRooms();
        createHallways();
        addwalls();

        if (!Engine.seedList.contains(seed)) {
            addAvatar(); // add an avatar if the world has never been generated before
            //addObstacle();
            //addCoins();
        } else {
            finalWorldFrame[avatarXPos][avatarYPos] = Tileset.AVATAR;
        }
        ter.renderFrame(finalWorldFrame);
    }

    public void createRooms() {
        for (int i = 0; i < 150; i++) {
            /** generate random startX and startY **/
            int startX = RandomUtils.uniform(randomSeed, 5, WIDTH - 7);
            int startY = RandomUtils.uniform(randomSeed, 5, HEIGHT - 7);

            /** generate random width and height **/
            int roomWidth = RandomUtils.uniform(randomSeed, 2, 7); // min width is 2 & max height is 7
            int roomHeight = RandomUtils.uniform(randomSeed, 2, 7); // min width is 2 & max height is 7

            Coordinate upperLeft = new Coordinate(startX - 1, startY + 1);
            Coordinate lowerRight = new Coordinate(startX + roomWidth, startY - roomHeight);
            Room theoreticalRoom = new Room(upperLeft, lowerRight);
            // this is technically floor width and floor height

            if (!isOverlapping(theoreticalRoom) && isInBounds(theoreticalRoom) && (startY > roomHeight)) { // added this
                constructFloor(startX, startY, roomWidth, roomHeight);
                addWall(theoreticalRoom);
                roomList.add(theoreticalRoom);
            }
        }
    }

    private void addwalls() {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (finalWorldFrame[x][y] == Tileset.FLOOR) {
                    if (finalWorldFrame[x + 1][y] == Tileset.NOTHING) {
                        finalWorldFrame[x + 1][y] = Tileset.WALL;
                    }

                    if (finalWorldFrame[x + 1][y + 1] == Tileset.NOTHING) {
                        finalWorldFrame[x + 1][y + 1] = Tileset.WALL;
                    }

                    if (finalWorldFrame[x - 1][y + 1] == Tileset.NOTHING) {
                        finalWorldFrame[x - 1][y + 1] = Tileset.WALL;
                    }

                    if (finalWorldFrame[x - 1][y - 1] == Tileset.NOTHING) {
                        finalWorldFrame[x - 1][y - 1] = Tileset.WALL;
                    }

                    if (finalWorldFrame[x + 1][y - 1] == Tileset.NOTHING) {
                        finalWorldFrame[x + 1][y - 1] = Tileset.WALL;
                    }

                    if (finalWorldFrame[x - 1][y] == Tileset.NOTHING) {
                        finalWorldFrame[x - 1][y] = Tileset.WALL;
                    }
                    if (finalWorldFrame[x][y - 1] == Tileset.NOTHING) {
                        finalWorldFrame[x][y - 1] = Tileset.WALL;
                    }
                    if (finalWorldFrame[x][y + 1] == Tileset.NOTHING) {
                        finalWorldFrame[x][y + 1] = Tileset.WALL;
                    }
                }
            }
        }
    }

    private void createHallways() {
        createDoor();
        findDoorConnections();
    }

    /* @source https://www.baeldung.com/java-distance-between-two-points */
    private double euclideanDistance(int x1, int y1, int x2, int y2) {
        int diffX = Math.abs(x2 - x1);
        int diffY = Math.abs(y2 - y1);
        return Math.sqrt((diffY * diffY) + (diffX * diffX));
    }

    public void findDoorConnections() {
        Door currDoor;
        Door bestDoor = null;
        while (doorList.size() > 1) {
            currDoor = doorList.get(0); // get and save first door
            doorList.remove(0); // remove first door
            double bestDistance = 0; // initial distance
            double currentDistance;

            for (Door comparisonDoor : doorList) {
                if (!comparisonDoor.getRoom().equals(currDoor.getRoom())) {
                    Coordinate t = currDoor.getCoordinate(); // tempDoor coordinate
                    Coordinate c = comparisonDoor.getCoordinate(); // comparisonDoor coordinate
                    currentDistance = euclideanDistance(t.getX(), t.getY(), c.getX(), c.getY());
                    if (currentDistance > bestDistance) {
                        bestDistance = currentDistance;
                        bestDoor = comparisonDoor;
                    }
                }
            }
            doorList.remove(bestDoor);
            if (bestDoor != null) {
                connectDoors(currDoor, bestDoor);
            }
        }
        if (doorList.size() == 1) {
            Door lastDoor = doorList.get(0);
            finalWorldFrame[lastDoor.getCoordinate().getX()][lastDoor.getCoordinate().getY()] = Tileset.FLOOR;
        }
    }

    public void createDoor() {
        for (Room room: roomList) {
            int maxDoors = RandomUtils.uniform(randomSeed, 1, 3); // random amount of rooms 1-2
            for (int i = 0; i < maxDoors; i++) {
                int wallBlock = RandomUtils.uniform(randomSeed, 1, 5); // random column or row
                createDoorHelper(wallBlock, room);
            }
        }
    }

    private void createDoorHelper(int wallBlock, Room room) {
        Coordinate upperLeft = room.getUpperLeft();
        Coordinate lowerRight = room.getLowerRight();
        boolean validTile = false;
        if (wallBlock == 1) { // first row
            while(!validTile) {
                int randomTileX = RandomUtils.uniform(randomSeed, upperLeft.getX() + 1, lowerRight.getX());
                TETile adjacentLeftTile = finalWorldFrame[randomTileX - 1][upperLeft.getY()];
                TETile adjacentRightTile = finalWorldFrame[randomTileX + 1][upperLeft.getY()];
                if ((adjacentLeftTile != Tileset.NOTHING && adjacentRightTile != Tileset.NOTHING)) {
                    finalWorldFrame[randomTileX][upperLeft.getY()] = Tileset.AVATAR;
                    Door door = new Door(room, new Coordinate(randomTileX, upperLeft.getY()));
                    doorList.add(door);
                    validTile = true;
                }
            }
        }
        else if (wallBlock == 2) { // last row
            while(!validTile) {
                int randomTileX = RandomUtils.uniform(randomSeed, upperLeft.getX() + 1, lowerRight.getX());
                TETile adjacentLeftTile = finalWorldFrame[randomTileX - 1][lowerRight.getY()];
                TETile adjacentRightTile = finalWorldFrame[randomTileX + 1][lowerRight.getY()];
                if ((adjacentLeftTile != Tileset.NOTHING && adjacentRightTile != Tileset.NOTHING)) {
                    finalWorldFrame[randomTileX][lowerRight.getY()] = Tileset.AVATAR;
                    Door door = new Door(room, new Coordinate(randomTileX, lowerRight.getY()));
                    doorList.add(door);
                    validTile = true;
                }
            }
        }
        else if (wallBlock == 3) { // first column
            while(!validTile) {
                int randomTileY = RandomUtils.uniform(randomSeed, lowerRight.getY() + 1, upperLeft.getY());
                TETile adjacentUpperTile = finalWorldFrame[upperLeft.getX()][randomTileY + 1];
                TETile adjacentLowerTile = finalWorldFrame[upperLeft.getX()][randomTileY - 1];
                if ((adjacentUpperTile != Tileset.NOTHING && adjacentLowerTile != Tileset.NOTHING)) {
                    finalWorldFrame[upperLeft.getX()][randomTileY] = Tileset.AVATAR;
                    Door door = new Door(room, new Coordinate(upperLeft.getX(), randomTileY));
                    doorList.add(door);
                    validTile = true;
                }
            }
        }
        else if (wallBlock == 4) { // last column
            while(!validTile) {
                int randomTileY = RandomUtils.uniform(randomSeed, lowerRight.getY() + 1, upperLeft.getY());
                TETile adjacentUpperTile = finalWorldFrame[lowerRight.getX()][randomTileY + 1];
                TETile adjacentLowerTile = finalWorldFrame[lowerRight.getX()][randomTileY - 1];
                if ((adjacentUpperTile != Tileset.NOTHING && adjacentLowerTile != Tileset.NOTHING)) {
                    finalWorldFrame[lowerRight.getX()][randomTileY] = Tileset.AVATAR;
                    Door door = new Door(room, new Coordinate(lowerRight.getX(), randomTileY));
                    doorList.add(door);
                    validTile = true;
                }
            }
        }
    }

    public boolean isInBounds(Room theoreticalRoom) {
        if (theoreticalRoom.getUpperLeft().getX() > 0 && theoreticalRoom.getLowerRight().getX() > 0
                && theoreticalRoom.getLowerRight().getX() < WIDTH && theoreticalRoom.getLowerRight().getX() < WIDTH) {
            return true;
        }
        if (theoreticalRoom.getUpperLeft().getY() > 0 && theoreticalRoom.getLowerRight().getY() > 0
                && theoreticalRoom.getUpperLeft().getY() < HEIGHT && theoreticalRoom.getLowerRight().getY() < HEIGHT) {
            if (theoreticalRoom.getUpperLeft().getX() < WIDTH && theoreticalRoom.getLowerRight().getY() > 0) { // >= 0
                return true;
            }
        }
        return false;
    }

    public boolean isOverlapping(Room theoreticalRoom) {
        boolean isOverlap = false;
        /** key:
         *  TR = "theoretical room"
         *  ER = "existing room"
         */
        int TR_UpperLeftX = theoreticalRoom.getUpperLeft().getX(); // top left corner X
        int TR_UpperLeftY = theoreticalRoom.getUpperLeft().getY(); // top left corner Y
        int TR_LowerRightX = theoreticalRoom.getLowerRight().getX(); // lower right corner X
        int TR_LowerRightY = theoreticalRoom.getLowerRight().getY(); // lower right corner Y
        int TR_UpperRightX = TR_LowerRightX; // upper right corner X
        int TR_UpperRightY = TR_UpperLeftY; // upper right corner Y
        int TR_LowerLeftX = TR_UpperLeftX; // lower left corner X
        int TR_LowerLeftY = TR_LowerRightY; // lower left corner Y

        if (roomList == null) {    // what happens when there is nothing in the room yet
            return false; // got rid of this and it still works
        }

        for (Room existingRoom : roomList) {
            int ER_UpperLeftX = existingRoom.getUpperLeft().getX();
            int ER_UpperLeftY = existingRoom.getUpperLeft().getY();
            int ER_LowerRightX = existingRoom.getLowerRight().getX();
            int ER_LowerRightY = existingRoom.getLowerRight().getY();

            /** case 1: does the lower right corner overlap? */
            if (ER_UpperLeftX <= TR_LowerRightX && TR_LowerRightX <= ER_LowerRightX) {
                if (ER_UpperLeftY >= TR_LowerRightY && TR_LowerRightY <= ER_LowerRightY) {
                    isOverlap = true;
                }
            }
            /** case 2: does the lower left corner overlap? */
            if (ER_UpperLeftX <= TR_LowerLeftX && TR_LowerLeftX <= ER_LowerRightX) {
                if (ER_UpperLeftY >= TR_LowerLeftY && TR_LowerLeftY >= ER_LowerRightY) {
                    isOverlap = true;
                }
            }
            /** case 3: does the upper right corner overlap? **/
            if (ER_UpperLeftX <= TR_UpperRightX && TR_UpperRightX <= ER_LowerRightX) {
                if (ER_UpperLeftY >= TR_UpperRightY && TR_UpperRightY >= ER_LowerRightY) {
                    isOverlap = true;
                }
            }
            /** case 4: does the upper left corner overlap? **/
            if (ER_UpperLeftX <= TR_UpperLeftX && TR_UpperLeftX <= ER_LowerRightX) {
                if (ER_UpperLeftY >= TR_UpperLeftY && TR_UpperLeftY >= ER_LowerRightY) {
                    isOverlap = true;
                }
            }
            /** case 5: does the theoretical room engulf the existing room on the right side? **/
            if (TR_UpperLeftX <= ER_LowerRightX && ER_LowerRightX <= TR_LowerRightX) {
                if (TR_UpperLeftY >= ER_LowerRightY && ER_LowerRightY >= TR_LowerRightY) {
                    isOverlap = true;
                }
            }
            /** case 6: does the theoretical room engulf the existing room on the left side? **/
            if (TR_UpperLeftX <= ER_UpperLeftX && ER_UpperLeftX <= TR_LowerRightX) {
                if (TR_UpperLeftY >= ER_UpperLeftY && ER_UpperLeftY >= TR_LowerRightY) {
                    isOverlap = true;
                }
            }

        }
        return isOverlap;
    }

    public void constructFloor(int startX, int startY, int roomWidth, int roomHeight) {
        for (int x = startX; x < startX + roomWidth; x += 1) {
            for (int y = startY; y > startY - roomHeight; y -= 1) {
                finalWorldFrame[x][y] = Tileset.FLOOR;
            }
        }
    }

    public void addWall(Room theoreticalRoom) {
        Coordinate upperLeft = theoreticalRoom.getUpperLeft();
        Coordinate lowerRight = theoreticalRoom.getLowerRight();
        for (int x = upperLeft.getX(); x <= lowerRight.getX(); x++) {
            for (int y = upperLeft.getY(); y >= lowerRight.getY(); y--) {
                if (finalWorldFrame[x][y] == Tileset.NOTHING) {
                    finalWorldFrame[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    private void connectDoors(Door currDoor, Door bestDoor) {
        /** pick a start door **/
        Door goalDoor;
        Door startDoor;
        /** we want to start with the door that has a larger y-value */
        if (currDoor.getCoordinate().getY() > bestDoor.getCoordinate().getY()) {
            startDoor = currDoor;
            goalDoor = bestDoor;
        } else {
            startDoor = bestDoor;
            goalDoor = currDoor;
        }

        int startX = startDoor.getCoordinate().getX();
        int startY = startDoor.getCoordinate().getY();
        int endX = goalDoor.getCoordinate().getX();
        int endY = goalDoor.getCoordinate().getY();

        /** start y is greater, move down **/
        if (startY > endY) {
            for (int y = startY; y >= endY; y--) { // stop once you hit the Y-value of your goal
                finalWorldFrame[startX][y] = Tileset.FLOOR;
            }
            /** change direction **/
            if (startX > endX) {
                /** move left **/
                for (int x = startX; x >= endX; x--) { // stop once you hit the X-value of your goal
                    finalWorldFrame[x][endY] = Tileset.FLOOR;
                }
            } else {
                /** move right **/
                for (int x = startX; x <= endX; x++) { // stop once you hit the X-value of your goal
                    finalWorldFrame[x][endY] = Tileset.FLOOR;
                }
            }
        }
        /** if the y-values are equal, move left or right **/
        else if (startY == endY) {
            if (startX > endX) {
                /** move left **/
                for (int x = startX; x >= endX; x--) { // stop once you hit the x-value of your goal
                    finalWorldFrame[x][startY] = Tileset.FLOOR;
                }
            } else {
                /** move right **/
                for (int x = startX; x <= endX; x++) { // stop once you hit the x-value of your goal
                    finalWorldFrame[x][startY] = Tileset.FLOOR;
                }
            }
        }
    }

    private void addObstacle() {
        for (int i = 0; i < Engine.obstacles; i++) {
            int randomX = RandomUtils.uniform(randomSeed, 0, WIDTH);
            int randomY = RandomUtils.uniform(randomSeed, 0, HEIGHT);
            if (finalWorldFrame[randomX][randomY] != Tileset.WALL && finalWorldFrame[randomX][randomY] != Tileset.NOTHING) {
                finalWorldFrame[randomX][randomY] = Tileset.FLOWER; //flower = obstacle
            } else {
                Engine.obstacles++;
            }
        }
    }

    private void addAvatar() {
        avatarXPos = roomList.get(0).getUpperLeft().getX();
        avatarYPos = roomList.get(0).getUpperLeft().getY();
        finalWorldFrame[avatarXPos][avatarYPos] = Tileset.AVATAR;
    }

    private void addCoins() {
        for (int i = 1; i <= 3; i++) {
            int x = roomList.get(roomList.size() - i).getUpperLeft().getX();
            int y = roomList.get(roomList.size() - i).getUpperLeft().getY();
            finalWorldFrame[x][y] = Tileset.SAND;
        }
    }
}
