package connectfour;

public class ConnectFourDemo {
    public static void main(String[] args) {
        Player red = new Player(DiscColor.RED, "Alice");
        Player blue = new Player(DiscColor.BLUE, "Bob");

        System.out.println("1. Invalid turns and columns are rejected");
        Game game = new Game(red, blue);
        require(!game.makeMove(blue, 0), "Out-of-turn move must fail");
        require(!game.makeMove(red, -1), "Negative column must fail");
        require(!game.makeMove(red, 7), "Column 7 must fail (columns are 0-6)");
        require(game.getCurrentPlayer() == red, "Rejected moves must preserve the turn");

        System.out.println("2. A full column is rejected without losing the turn");
        for (int i = 0; i < 6; i++) {
            move(game, 0);
        }
        require(!game.makeMove(red, 0), "Full column must fail");
        require(game.getCurrentPlayer() == red, "Full-column rejection must preserve the turn");
        move(game, 1);

        System.out.println("3. Horizontal win and rejection after game over");
        game = play(red, blue, 0, 0, 1, 1, 2, 2, 3);
        verifyWinner(game, red);
        require(!game.makeMove(blue, 4), "Moves after a win must fail");

        System.out.println("4. Vertical win");
        verifyWinner(play(red, blue, 0, 1, 0, 1, 0, 1, 0), red);

        System.out.println("5. Diagonal wins in both directions");
        int[] diagonal = {0, 1, 1, 2, 4, 2, 2, 3, 4, 3, 5, 3, 3};
        verifyWinner(play(red, blue, diagonal), red);
        int[] mirrored = new int[diagonal.length];
        for (int i = 0; i < diagonal.length; i++) {
            mirrored[i] = 6 - diagonal[i];
        }
        verifyWinner(play(red, blue, mirrored), red);

        System.out.println("6. Full board with no winner is a draw");
        game = new Game(red, blue);
        // Rows alternate RRBBRRB / BBRRBBR, avoiding four in any direction.
        for (int row = 0; row < 6; row++) {
            for (int col : new int[]{0, 2, 1, 3, 4, 6, 5}) {
                move(game, col);
            }
        }
        require(game.getGameState() == GameState.DRAW, "Expected a draw");
        require(game.getWinner() == null, "A draw must have no winner");
        require(!game.makeMove(red, 0), "Moves after a draw must fail");

        System.out.println("All Connect Four demo checks passed.");
    }

    private static Game play(Player red, Player blue, int... columns) {
        Game game = new Game(red, blue);
        for (int column : columns) {
            move(game, column);
        }
        return game;
    }

    private static void move(Game game, int column) {
        require(game.makeMove(game.getCurrentPlayer(), column),
                "Expected an accepted move in column " + column);
    }

    private static void verifyWinner(Game game, Player expected) {
        require(game.getGameState() == GameState.WON, "Expected a won game");
        require(game.getWinner() == expected, "Unexpected winner");
        System.out.println("Winner: " + expected.name());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
