# Connect Four

## Clarifying questions & answers
- How do users interact with the board? - enter column number and the disc drops
- What happens when board is full and no winner? - Draw
- What happens when user makes invalid move(column thats already full)? - Reject move
-  What happens when user makes invalid turn? - Reject the turn
- Concurrency? Are we supporting multiple boards at a time? - single board for now
- Do we need to track move history or support undo? - No
- What about board size—does it need to be configurable, or always 7x6? - Always 7x6

## Requirements
1. Two players take turns dropping discs into a 7-column, 6-row board
2. A disc falls to the lowest available row in the chosen column
3. The game ends when:
    - A player gets four discs in a row (vertical, horizontal, or diagonal). They win.
    - The board is full. It's a draw.
4. Invalid moves should be rejected clearly:
    - Dropping in a full column.
    - Moving out of turn.
    - Moving after the game is over.

## Out of SCope
- UI support
- Concurrent games
- Move history
- Undo
- Board size configuration

## Entities & relationships
- Game - orchestrator and has state of game - winner, players, gameState , Stack of moves(if history/undo is needed)
- Board - has state of board - grid of DiscColor
- Player - has a color
- DiscColor - Enum
- GameState - Enum
- Move - not needed for initial design, can be added if undo is needed
## Class design

## APIs
- makeMove(player, column) -> boolean true: success, false: invalid move
- getCurrentPlayer()
- getGameState()
- getWinner() - above 3 can be used to display in UI. 
Only makeMove() mutates the game, rest all read-only

## Design patterns used (and why)
Command pattern - if move is included

## Trade-offs / what I'd do with more time
