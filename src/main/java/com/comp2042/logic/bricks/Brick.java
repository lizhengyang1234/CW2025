package com.comp2042.logic.bricks;

import java.util.List;
/**
 * 方块接口：定义所有俄罗斯方块必须提供的方法，主要用于获取其所有旋转状态的形状矩阵。
 * Each brick interface: defines the required method for all Tetris bricks,
 * mainly used to retrieve the shape matrices of all rotation states.
 */
public interface Brick {
    /**
     * 获取当前方块的所有旋转形状（通常为 0°, 90°, 180°, 270°）。
     * 每个形状使用一个二维数组（int[][]）表示，其中 1 表示方块填充位置，0 表示空白。
     *
     * Returns a list of all rotation states of this brick (usually 0°, 90°, 180°, 270°).
     * Each shape is represented by a 2D int array, where 1 indicates a filled cell
     * and 0 represents an empty cell.
     */
    List<int[][]> getShapeMatrix();
}
