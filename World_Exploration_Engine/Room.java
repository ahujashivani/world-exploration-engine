package byow.Core;

public class Room {

    private Coordinate upperLeft;
    private Coordinate lowerRight;

    Room (Coordinate upperLeft, Coordinate lowerRight) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
    }

    public Coordinate getUpperLeft() {
            return upperLeft;
        }

    public Coordinate getLowerRight() {
        return lowerRight;
    }

}
