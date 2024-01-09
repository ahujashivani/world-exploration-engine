package byow.Core;

public class Door {

    private Coordinate coordinate;
    private Room room;

    public Door(Room room, Coordinate coordinate) {
        this.coordinate = coordinate;
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
