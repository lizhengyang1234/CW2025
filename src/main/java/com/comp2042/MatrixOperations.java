// 矩阵工具类：封装了对棋盘和方块矩阵的一些常用操作。
// 包括碰撞检测、复制矩阵、把方块合并到背景，以及检查并清除已满的行等。
// 这些方法通常由 Board 或 GameController 调用，本类本身不包含游戏规则，只提供底层计算。
//
// Matrix utility class: contains common operations on board and brick matrices,
// such as collision checks, copying, merging a brick into the board background,
// and detecting/removing full rows. Used by the board/game logic as a helper.

package com.comp2042;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOperations {

    // 工具类不需要被实例化
    // We don't want to instantiate this utility class
    private MatrixOperations() {

    }

    /**
     * 检查给定的砖块在指定偏移位置 (offsetX, offsetY) 下，
     * 是否会和棋盘发生碰撞或越界。
     *
     * Checks if the given brick placed at (offsetX, offsetY)
     * would collide with the board or go out of bounds.
     */
    public static boolean intersect(final int[][] board, final int[][] brick, int offsetX, int offsetY) {
        for (int brickRow = 0; brickRow < brick.length; brickRow++) {
            for (int brickCol = 0; brickCol < brick[brickRow].length; brickCol++) {

                int targetX = offsetX + brickRow;
                int targetY = offsetY + brickCol;

                // 注意：保持和原先逻辑一致，这里仍然使用“转置”的索引 brick[brickCol][brickRow]
                int brickCell = brick[brickCol][brickRow];

                if (brickCell != 0) {
                    // 越界就算碰撞
                    if (checkOutOfBound(board, targetX, targetY)) {
                        return true;
                    }
                    // 目标格子已被占用也算碰撞
                    if (board[targetY][targetX] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkOutOfBound(int[][] matrix, int targetX, int targetY) {
        boolean returnValue = true;
        if (targetX >= 0 && targetY < matrix.length && targetX < matrix[targetY].length) {
            returnValue = false;
        }
        return returnValue;
    }

    public static int[][] copy(int[][] original) {
        int[][] myInt = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            int[] aMatrix = original[i];
            int aLength = aMatrix.length;
            myInt[i] = new int[aLength];
            System.arraycopy(aMatrix, 0, myInt[i], 0, aLength);
        }
        return myInt;
    }

    public static int[][] merge(int[][] filledFields, int[][] brick, int x, int y) {
        int[][] copy = copy(filledFields);
        for (int i = 0; i < brick.length; i++) {
            for (int j = 0; j < brick[i].length; j++) {
                int targetX = x + i;
                int targetY = y + j;
                if (brick[j][i] != 0) {
                    copy[targetY][targetX] = brick[j][i];
                }
            }
        }
        return copy;
    }

    public static ClearRow checkRemoving(final int[][] matrix) {
        int[][] tmp = new int[matrix.length][matrix[0].length];
        Deque<int[]> newRows = new ArrayDeque<>();
        List<Integer> clearedRows = new ArrayList<>();

        for (int i = 0; i < matrix.length; i++) {
            int[] tmpRow = new int[matrix[i].length];
            boolean rowToClear = true;
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 0) {
                    rowToClear = false;
                }
                tmpRow[j] = matrix[i][j];
            }
            if (rowToClear) {
                clearedRows.add(i);
            } else {
                newRows.add(tmpRow);
            }
        }
        for (int i = matrix.length - 1; i >= 0; i--) {
            int[] row = newRows.pollLast();
            if (row != null) {
                tmp[i] = row;
            } else {
                break;
            }
        }
        int scoreBonus = 50 * clearedRows.size() * clearedRows.size();
        return new ClearRow(clearedRows.size(), tmp, scoreBonus);
    }

    public static List<int[][]> deepCopyList(List<int[][]> list) {
        return list.stream().map(MatrixOperations::copy).collect(Collectors.toList());
    }

}