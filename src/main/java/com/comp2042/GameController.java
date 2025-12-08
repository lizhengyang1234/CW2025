package com.comp2042;

import java.awt.Point;
import java.util.List;

// GameController: handles all core game logic and talks to the GUI.
public class GameController implements InputEventListener {

    private final Board board;
    private final GuiController viewGuiController;

    // Default constructor: normal mode (no bombs)
    public GameController(GuiController c) {
        this(c, false);
    }

    // Constructor with bomb mode support
    public GameController(GuiController c, boolean bombMode) {
        this.viewGuiController = c;

        // 25 rows × 10 columns, third argument: enable bomb mode or not
        this.board = new SimpleBoard(25, 10, bombMode);

        // Load previous high score
        board.getScore().loadHighScore();

        // Create the first falling brick
        board.createNewBrick();

        // Connect to GUI
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());

        // Bind HUD labels
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindLines(board.getScore().linesProperty());
        viewGuiController.bindHighScore(board.getScore().highScoreProperty());
    }

    /**
     * Handles DOWN and HARD_DROP events.
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {

        ClearRow clearRow = null;

        // ===== HARD DROP (space key) =====
        if (event.getEventType() == EventType.HARD_DROP) {
            int steps = 0;

            // Move down until we hit something
            while (board.moveBrickDown()) {
                steps++;
            }

            // Brick has landed → handle merge, clear rows, new brick, etc.
            clearRow = handleBrickLanded();

            // Extra score for hard drop distance
            if (steps > 0) {
                board.getScore().add(steps);
            }

            return new DownData(clearRow, board.getViewData());
        }

        // ===== Normal one-step DOWN =====
        boolean canMove = board.moveBrickDown();
        if (!canMove) {
            // Brick landed, handle it
            clearRow = handleBrickLanded();
        } else {
            // Small score for manual soft drop
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
            }
        }

        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Called whenever the current brick can no longer move down.
     *
     * Steps:
     *  1) Merge brick into the background (may also trigger bomb explosion)
     *  2) Clear full rows and update score / lines
     *  3) If needed, play bomb explosion animation
     *  4) Spawn a new brick; if that fails, the game is over
     *  5) Refresh the background board in the GUI
     *
     * Returns info about cleared rows (can be null or 0 lines).
     */
    private ClearRow handleBrickLanded() {

        // Merge brick into the background.
        // In bomb mode this may also explode and update the matrix.
        board.mergeBrickToBackground();

        // Clear full rows and update score
        ClearRow clearRow = board.clearRows();
        if (clearRow.getLinesRemoved() > 0) {
            Score score = board.getScore();
            score.add(clearRow.getScoreBonus());
            score.addLines(clearRow.getLinesRemoved());
        }

        // ➤ If we are in bomb mode and there was an explosion,
        //    SimpleBoard stored the affected cells in lastExplosionCells.
        if (board instanceof SimpleBoard) {
            List<Point> explosionCells = ((SimpleBoard) board).getLastExplosionCells();
            if (explosionCells != null && !explosionCells.isEmpty()) {
                // Ask GUI to play the explosion animation (black + shake)
                viewGuiController.playExplosionAnimation(explosionCells);
            }
        }

        // Try to create a new brick; if it collides immediately, game over
        if (board.createNewBrick()) {
            board.getScore().saveHighScore();
            viewGuiController.gameOver();
        }

        // Finally refresh the background grid
        viewGuiController.refreshGameBackground(board.getBoardMatrix());

        return clearRow;
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        // Do NOT reset high score here
    }
}