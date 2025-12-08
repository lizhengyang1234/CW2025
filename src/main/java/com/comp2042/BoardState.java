package com.comp2042;

/**
 * 负责维护“背景棋盘”的状态：
 * - 保存已经落下的方块
 * - 合并当前方块到背景
 * - 检查并清除整行
 */
public class BoardState {

    private final int rows;
    private final int cols;

    /** matrix[y][x]：0 表示空，其它表示砖块 id */
    private int[][] matrix;

    public BoardState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        reset();
    }

    /** 重置棋盘为全空。 */
    public void reset() {
        matrix = new int[rows][cols];
    }

    /** 返回当前棋盘矩阵。 */
    public int[][] getMatrix() {
        return matrix;
    }

    /** 把传入矩阵整体替换到 BoardState（用于兼容旧的 MatrixOperations.merge） */
    public void replaceMatrix(int[][] newMatrix) {
        if (newMatrix == null) return;
        if (newMatrix.length != rows || newMatrix[0].length != cols) return;
        this.matrix = newMatrix;
    }

    /** 合并当前方块到背景 */
    public void mergeBrick(int[][] shape, int offsetX, int offsetY) {
        matrix = MatrixOperations.merge(matrix, shape, offsetX, offsetY);
    }

    /** 检查并清除已满的行，更新 matrix，返回 ClearRow 结果。 */
    public ClearRow clearFullRows() {
        ClearRow result = MatrixOperations.checkRemoving(matrix);
        matrix = result.getNewMatrix();
        return result;
    }
}