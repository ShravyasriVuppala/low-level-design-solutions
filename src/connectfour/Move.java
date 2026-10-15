package connectfour;

public class Move {
    private final int row, column;
    private final DiscColor color;

    public Move(int row, int column, DiscColor color) {
        this.row = row;
        this.column = column;
        this.color = color;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public DiscColor getColor() {
        return color;
    }
}
