package com.comp2042;

/**
 * 负责维护“背景棋盘”的状态：
 * - 保存已经落下的方块
 * - 合并当前方块到背景
 * - 检查并清除整行
 *
 * 不关心“当前方块”的位置，也不关心分数。
 * 只是一个带工具方法的 2D 数组包装类。
 */
public class BoardState {

    /** 行数（纵向，高度） */
    private final int rows;
    /** 列数（横向，宽度） */
    private final int cols;

    /** 真实棋盘矩阵，matrix[y][x]：0 表示空，其它表示砖块 id */
    private int[][] matrix;

    public BoardState(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        reset();
    }

    /**
     * 重置棋盘为全空。
     */
    public void reset() {
        matrix = new int[rows][cols];
    }

    /**
     * 返回当前棋盘矩阵（给 GUI 画背景用）。
     */
    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * 把当前活动方块合并到背景里。
     *
     * @param shape   当前方块的 2D 矩阵
     * @param offsetX 左上角 X（列）
     * @param offsetY 左上角 Y（行）
     */
    public void mergeBrick(int[][] shape, int offsetX, int offsetY) {
        matrix = MatrixOperations.merge(matrix, shape, offsetX, offsetY);
    }

    /**
     * 检查并清除已满的行，返回 ClearRow 结果（包含新棋盘 + 消行数 + 分数加成）。
     * 内部也会更新 matrix 为消行后的状态。
     */
    public ClearRow clearFullRows() {
        ClearRow result = MatrixOperations.checkRemoving(matrix);
        matrix = result.getNewMatrix();
        return result;
    }
}