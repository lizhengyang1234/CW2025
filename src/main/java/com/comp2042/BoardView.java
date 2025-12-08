package com.comp2042;

import java.awt.Point;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * Only handles drawing:
 * - background board
 * - current 4x4 brick
 * - ghost piece
 * - "Next" preview
 *
 * No input / score / timeline here.
 */
public class BoardView {

    private final GridPane gamePanel;
    private final GridPane brickPanel;
    private final GridPane nextBrickPanel;

    private final int brickSize;
    private final int previewBrickSize;
    private final int hiddenRows;
    private final int brickCornerArc;
    private final Paint[] brickColors;

    private final GhostRenderer ghostRenderer;
    private final NextBrickRenderer nextBrickRenderer;

    // background cells (same size as board)
    private Rectangle[][] displayMatrix;
    // current 4x4 falling brick cells
    private Rectangle[][] rectangles;
    // background data (for ghost)
    private int[][] boardState;

    public BoardView(GridPane gamePanel,
                     GridPane brickPanel,
                     GridPane nextBrickPanel,
                     int brickSize,
                     int previewBrickSize,
                     int hiddenRows,
                     int brickCornerArc,
                     Paint[] brickColors) {

        this.gamePanel = gamePanel;
        this.brickPanel = brickPanel;
        this.nextBrickPanel = nextBrickPanel;

        this.brickSize = brickSize;
        this.previewBrickSize = previewBrickSize;
        this.hiddenRows = hiddenRows;
        this.brickCornerArc = brickCornerArc;
        this.brickColors = brickColors;

        this.ghostRenderer = new GhostRenderer(hiddenRows);
        this.nextBrickRenderer = new NextBrickRenderer(previewBrickSize, brickColors);
    }

    /* ================= init game view ================= */

    /**
     * Called once when game starts.
     * Creates background cells and the first 4x4 brick.
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {

        // copy board for ghost
        boardState = MatrixOperations.copy(boardMatrix);
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        // background cells (skip hidden rows)
        for (int i = hiddenRows; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {

                Rectangle rect = new Rectangle(brickSize, brickSize);
                rect.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rect;

                // row index in UI is (i - hiddenRows)
                gamePanel.add(rect, j, i - hiddenRows);
            }
        }

        // 4x4 brick on top
        brickPanel.toFront();

        int[][] brickData = brick.getBrickData();
        rectangles = new Rectangle[brickData.length][brickData[0].length];

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {

                Rectangle r = new Rectangle(brickSize, brickSize);
                r.setFill(getFillColor(brickData[i][j]));
                rectangles[i][j] = r;
                brickPanel.add(r, j, i);
            }
        }

        updateBrickPanelPosition(brick);
        renderNextBrick(brick);
        ghostRenderer.drawGhost(brick, boardState, displayMatrix);
    }

    /* ================= refresh current brick ================= */

    /**
     * Redraw current falling brick + ghost piece.
     * GuiController already checks pause.
     */
    public void refreshBrick(ViewData brick) {

        brickPanel.toFront();

        // redraw background from boardState (clear old ghost)
        for (int i = hiddenRows; i < boardState.length; i++) {
            for (int j = 0; j < boardState[i].length; j++) {
                setRectangleData(boardState[i][j], displayMatrix[i][j]);
            }
        }

        // then draw ghost again
        ghostRenderer.drawGhost(brick, boardState, displayMatrix);

        // update 4x4 brick position
        updateBrickPanelPosition(brick);

        // update colors inside 4x4 brick
        int[][] data = brick.getBrickData();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                setRectangleData(data[i][j], rectangles[i][j]);
            }
        }
    }

    /* ================= refresh background ================= */

    /**
     * Redraw background (after merge / clear rows).
     * Also updates boardState used by ghost.
     */
    public void refreshBackground(int[][] board) {
        boardState = MatrixOperations.copy(board);

        for (int i = hiddenRows; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    /* ================= Next preview ================= */

    public void renderNextBrick(ViewData viewData) {
        nextBrickRenderer.renderNextBrick(viewData, nextBrickPanel);
    }

    /* ================= helpers ================= */

    private void updateBrickPanelPosition(ViewData brick) {
        double cw = brickSize + brickPanel.getHgap();
        double ch = brickSize + brickPanel.getVgap();

        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * cw);
        brickPanel.setLayoutY(gamePanel.getLayoutY() + (brick.getyPosition() - hiddenRows) * ch);
    }

    private void setRectangleData(int color, Rectangle r) {
        r.setFill(getFillColor(color));
        r.setArcHeight(brickCornerArc);
        r.setArcWidth(brickCornerArc);
    }

    private Paint getFillColor(int id) {
        if (id >= 0 && id < brickColors.length) {
            return brickColors[id];
        }
        return Color.WHITE;
    }

    /* ================= explosion animation ================= */

    /**
     * Simple explosion animation:
     * - explosion cells turn black
     * - board shakes a bit
     * - black cells fade out
     */
    public void playExplosionAnimation(List<Point> cells) {

        if (cells == null || cells.isEmpty() || displayMatrix == null) return;

        final double shakeAmount = 4;

        // remember original position
        final double originalX = gamePanel.getTranslateX();
        final double originalY = gamePanel.getTranslateY();

        Timeline timeline = new Timeline(

                // 1) paint explosion cells black + small shake
                new KeyFrame(Duration.ZERO, e -> {

                    for (Point p : cells) {
                        int x = p.x;
                        int y = p.y;

                        // skip hidden rows
                        if (y < hiddenRows) continue;

                        if (y >= 0 && y < displayMatrix.length &&
                                x >= 0 && x < displayMatrix[0].length) {

                            Rectangle r = displayMatrix[y][x];
                            if (r != null) {
                                r.setFill(Color.BLACK);
                                r.setOpacity(1.0);
                            }
                        }
                    }

                    // shake background board only
                    gamePanel.setTranslateX(originalX - shakeAmount);
                    gamePanel.setTranslateY(originalY);
                }),

                // 2) move to the other side
                new KeyFrame(Duration.millis(60), e -> {
                    gamePanel.setTranslateX(originalX + shakeAmount);
                }),

                // 3) back to center (stop shaking)
                new KeyFrame(Duration.millis(120), e -> {
                    gamePanel.setTranslateX(originalX);
                    gamePanel.setTranslateY(originalY);
                }),

                // 4) start fade out
                new KeyFrame(Duration.millis(200), e -> {
                    for (Point p : cells) {
                        int x = p.x;
                        int y = p.y;

                        if (y < hiddenRows) continue;

                        if (y >= 0 && y < displayMatrix.length &&
                                x >= 0 && x < displayMatrix[0].length) {

                            Rectangle r = displayMatrix[y][x];
                            if (r != null) {
                                r.setOpacity(0.5);
                            }
                        }
                    }
                }),

                // 5) fully fade out
                new KeyFrame(Duration.millis(350), e -> {
                    for (Point p : cells) {
                        int x = p.x;
                        int y = p.y;

                        if (y < hiddenRows) continue;

                        if (y >= 0 && y < displayMatrix.length &&
                                x >= 0 && x < displayMatrix[0].length) {

                            Rectangle r = displayMatrix[y][x];
                            if (r != null) {
                                r.setOpacity(0.0);
                            }
                        }
                    }
                }),

                // 6) cleanup: reset cells and board position
                new KeyFrame(Duration.millis(500), e -> {
                    for (Point p : cells) {
                        int x = p.x;
                        int y = p.y;

                        if (y < hiddenRows) continue;

                        if (y >= 0 && y < displayMatrix.length &&
                                x >= 0 && x < displayMatrix[0].length) {

                            Rectangle r = displayMatrix[y][x];
                            if (r != null) {
                                r.setOpacity(1.0);
                                r.setFill(Color.TRANSPARENT);
                            }
                        }
                    }

                    gamePanel.setTranslateX(originalX);
                    gamePanel.setTranslateY(originalY);
                })
        );

        timeline.setCycleCount(1);
        timeline.play();
    }
}