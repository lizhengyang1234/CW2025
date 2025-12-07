package com.comp2042;

import java.awt.Point;

/**
 * 负责和“移动相关”的规则：
 * - 尝试把当前方块平移 (dx, dy)
 * - 判断是否会碰撞或越界
 *
 * 不保存任何状态，只根据传入的棋盘 / 形状 / 位置做计算。
 */
public class MovementController {

    /**
     * 检查给定方块在指定 offset 下是否与背景棋盘发生碰撞或越界。
     */
    public boolean hasCollision(int[][] boardMatrix, int[][] shape, Point offset) {
        return MatrixOperations.intersect(
                boardMatrix,
                shape,
                offset.x,
                offset.y
        );
    }

    /**
     * 尝试把当前方块从 currentOffset 平移 (dx, dy)。
     *
     * @param boardState   背景棋盘状态
     * @param shape        当前方块形状矩阵
     * @param currentOffset 当前坐标
     * @param dx           X 方向增量
     * @param dy           Y 方向增量
     * @return 新的坐标（可移动）; 若发生碰撞则返回 null
     */
    public Point tryTranslate(BoardState boardState,
                              int[][] shape,
                              Point currentOffset,
                              int dx,
                              int dy) {

        Point newOffset = new Point(currentOffset);
        newOffset.translate(dx, dy);

        boolean conflict = MatrixOperations.intersect(
                boardState.getMatrix(),
                shape,
                newOffset.x,
                newOffset.y
        );

        return conflict ? null : newOffset;
    }
}