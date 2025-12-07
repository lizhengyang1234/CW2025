package com.comp2042;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * 负责纯“画面相关”的工作：
 * - 画背景棋盘
 * - 画当前 4x4 方块
 * - 画幽灵影子块
 * - 画右侧 Next 预览
 *
 * 不处理输入、不处理分数，也不碰 Timeline。
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

    // 背景棋盘的格子（对应 boardMatrix 的每一格）
    private Rectangle[][] displayMatrix;
    // 当前下落方块 4x4 的格子
    private Rectangle[][] rectangles;
    // 当前背景的数值状态，用于幽灵影子计算
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

    /**
     * 初次启动游戏时调用：
     *  - 创建背景网格 Rectangle
     *  - 创建当前 4x4 方块的 Rectangle
     *  - 设置初始位置
     *  - 画一次 next 预览 + 幽灵影子
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {

        // 保存背景数据给幽灵影子用
        boardState = MatrixOperations.copy(boardMatrix);

        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        // 背景格子：跳过隐藏行
        for (int i = hiddenRows; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {

                Rectangle rect = new Rectangle(brickSize, brickSize);
                rect.setFill(Color.TRANSPARENT);

                displayMatrix[i][j] = rect;
                gamePanel.add(rect, j, i - hiddenRows);
            }
        }

        brickPanel.toFront();

        // 当前 4x4 方块
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

        // 初始位置
        updateBrickPanelPosition(brick);

        // Next 预览
        renderNextBrick(brick);

        // 幽灵影子
        ghostRenderer.drawGhost(brick, boardState, displayMatrix);
    }

    /**
     * 刷新当前方块 + 幽灵影子。
     * GuiController 已经保证未暂停时才调用。
     */
    public void refreshBrick(ViewData brick) {

        brickPanel.toFront();

        // 先用当前 boardState 重画背景，清掉旧的幽灵影子
        if (boardState != null && displayMatrix != null) {
            for (int i = hiddenRows; i < boardState.length; i++) {
                for (int j = 0; j < boardState[i].length; j++) {
                    setRectangleData(boardState[i][j], displayMatrix[i][j]);
                }
            }
        }

        // 重画幽灵影子
        ghostRenderer.drawGhost(brick, boardState, displayMatrix);

        // 更新当前方块位置
        updateBrickPanelPosition(brick);

        // 更新 4x4 方块内的颜色
        int[][] data = brick.getBrickData();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                setRectangleData(data[i][j], rectangles[i][j]);
            }
        }
    }

    /**
     * 刷新背景棋盘（消行、新方块合并之后等）。
     * 同时更新 boardState，给幽灵影子用。
     */
    public void refreshBackground(int[][] board) {

        boardState = MatrixOperations.copy(board);

        if (displayMatrix == null) {
            return;
        }

        for (int i = hiddenRows; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    /**
     * 右侧 Next 预览。
     */
    public void renderNextBrick(ViewData viewData) {
        nextBrickRenderer.renderNextBrick(viewData, nextBrickPanel);
    }

    /* =================== 内部小工具方法 =================== */

    private void updateBrickPanelPosition(ViewData brick) {
        double cellWidth = brickSize + brickPanel.getHgap();
        double cellHeight = brickSize + brickPanel.getVgap();

        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * cellWidth);
        brickPanel.setLayoutY(gamePanel.getLayoutY() + (brick.getyPosition() - hiddenRows) * cellHeight);
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
}