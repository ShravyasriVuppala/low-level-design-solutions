package connectfour;

import java.util.Stack;

public class Game {
    private final Board board;
    private final Player player1, player2;
    private GameState status;
    private Player winner, currentPlayer;
    private final Stack<Move> moves;

    public Game(Player player1, Player player2) {
        this.moves = new Stack<>();
        this.board = new Board(6, 7); //can inject into game constructor
        this.player1 = player1;
        this.player2 = player2;
        this.status = GameState.IN_PROGRESS;
        this.currentPlayer = player1;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getGameState() {
        return status;
    }

    public Player getWinner() {
        return winner;
    }

    public boolean makeMove(Player player, int column){
        //validate game state
        if(status != GameState.IN_PROGRESS){
            return false;
        }
        //validate current player
        if(currentPlayer != player){
            return false;
        }
        //check if disc can be placed in this column and get the row
        int row = board.placeDisc(column, player.discColor());
        if(row == -1){
            return false;
        }
        //Create move and add to stack
        Move move = new Move(row, column, player.discColor());
        moves.push(move);

        //check win
        if(board.checkWin(player.discColor(), row, column)){
            status = GameState.WON;
            winner = player;
        }
        // check draw
        else if (board.isBoardFull()){
            status = GameState.DRAW;
        }
        //set current player
        else {
            currentPlayer = currentPlayer == player1 ? player2 : player1;
        }
        return true;
    }

    public void undoMove(){
        //pop from stack, clear cell on board and revert current player
        Move move = moves.pop();
        board.clearCell(move.getRow(), move.getColumn());
        currentPlayer = currentPlayer == player1 ? player2 : player1;
    }
}
