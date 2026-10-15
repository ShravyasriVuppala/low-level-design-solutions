package connectfour;

public class Board {
    private final int rows, cols;
    private final DiscColor[][] grid;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new DiscColor[rows][cols];
    }

    // check if column has place for new disc
    boolean canPlace(int column){
        if(column < 0 || column >= cols){
            return false;
        }
       return grid[0][column] == null;
    }

    //place disc in the column and return row
    int placeDisc(int column, DiscColor color){
        if(!canPlace(column)){
            return -1;
        }

        for(int row = rows - 1; row >= 0; row--){
            if(grid[row][column] == null){
                grid[row][column] = color;
                return row;
            }
        }
        return -1;
    }

    //check if there's a win
    boolean checkWin(DiscColor color, int row, int column){
        //check in horizontal, vertical and diagonal directions
        //if any of it has more than or equals 4 cells of given color, return true
        int[][] dir = {
                {0, 1}, //horizontal
                {1, 0}, //vertical
                {1, 1}, //diagonal down-right
                {-1, 1} //diagonal up-right
        };
        for(int[] d : dir){
            int count = 1; //current cell
            //check in this direction and opposite direction and count no. of cells with this color
            count += countInDirection(row, column, d[0], d[1], color);
            count += countInDirection(row, column, -d[0], -d[1], color); //opposite direction
            if(count >= 4){
                return true;
            }
        }
        return false;
    }

    private int countInDirection(int row, int col, int dr, int dc, DiscColor color) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        while(inBounds(r, c) && grid[r][c] == color){
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && c >= 0 && r < rows && c < cols;
    }

    //check if board is full
    boolean isBoardFull(){
        for(int col = 0; col < cols; col++){
            if(grid[0][col] == null){
                return false;
            }
        }
        return true;
    }

}
