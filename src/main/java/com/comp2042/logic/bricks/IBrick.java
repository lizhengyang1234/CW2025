package com.comp2042.logic.bricks;

import com.comp2042.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/*
 * I 型方块（就是长条那种）。
 * 这里把它的两个主要旋转状态（水平和垂直）用矩阵的方式保存下来。
 * getShapeMatrix() 会返回一个深拷贝，避免外部修改原始数据。
 *
 * I-shaped Tetris brick (the long straight piece).
 * Stores both horizontal and vertical rotation states as 2D matrices.
 * getShapeMatrix() returns a deep copy so the original data stays unchanged.
 */
final class IBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public IBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }

}
