package com.comp2042;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 负责绘制“幽灵影子块”的小工具类。
 * 不保存状态，只根据传入的棋盘、当前方块数据来算影子落点并画出来。
 */
public class GhostRenderer {

    /** 顶部隐藏的行数（小于这个行号的不要画出来） */
    private final int hiddenRows;

    public GhostRenderer(int hiddenRows) {
        this.hiddenRows = hiddenRows;
    }

    /**
     * 计算并绘制“幽灵影子块”：
     * 根据当前棋盘 boardState 和正在下落的方块，算出它到底会落到哪一行，
     * 然后在对应位置画一层半透明白色。
     *
     * @param brick         当前正在下落的方块数据
     * @param boardState    当前背景棋盘（数值矩阵）
     * @param displayMatrix 背景对应的 Rectangle 矩阵（用于实际画影子）
     */
    public void drawGhost(ViewData brick,
                          int[][] boardState,
                          Rectangle[][] displayMatrix) {

        if (boardState == null || brick == null || displayMatrix == null) {
            return;
        }

        int[][] brickData = brick.getBrickData();
        int rows = boardState.length;
        int cols = boardState[0].length;

        int x = brick.getxPosition();
        int y = brick.getyPosition();

        // 从当前 y 一直往下试探，直到不能再放
        int ghostY = y;
        while (canPlace(brickData, x, ghostY + 1, boardState)) {
            ghostY++;
        }

        // 把幽灵影子画在 displayMatrix 上（只画在可见区域）
        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                if (brickData[i][j] != 0) {
                    int gx = x + j;
                    int gy = ghostY + i;

                    if (gx < 0 || gx >= cols || gy < 0 || gy >= rows) {
                        continue;
                    }

                    // 只在可见区域（跳过隐藏行）
                    if (gy >= hiddenRows) {
                        Rectangle cell = displayMatrix[gy][gx];
                        if (cell != null) {
                            // 半透明白色作为影子，不改变原方块颜色
                            cell.setFill(Color.color(1.0, 1.0, 1.0, 0.28));
                        }
                    }
                }
            }
        }
    }

    /**
     * 简单碰撞检测，检查 brickData 放在 (x, y) 是否会出界或撞到 board。
     * 仅用于计算幽灵影子，不影响真实逻辑层。
     */
    private boolean canPlace(int[][] brickData, int x, int y, int[][] board) {

        int rows = board.length;
        int cols = board[0].length;

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                if (brickData[i][j] != 0) {

                    int bx = x + j;
                    int by = y + i;

                    if (bx < 0 || bx >= cols || by < 0 || by >= rows) {
                        return false;
                    }

                    if (board[by][bx] != 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}