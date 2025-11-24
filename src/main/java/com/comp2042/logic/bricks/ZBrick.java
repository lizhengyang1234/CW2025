package com.comp2042.logic.bricks;

import com.comp2042.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/*
 * Z 型方块（形状像一条反向的 Z）。
 * 这个方块只有两个独特的旋转方向，所以这里只存两个矩阵。
 * 用数字 7 标记 Z 方块占据的格子。
 * getShapeMatrix() 返回深拷贝，避免外部随便改动原始数据。
 *
 * Z-shaped Tetris brick (the mirrored version of the S piece).
 * Only two unique rotation states are needed.
 * Uses number 7 for the filled cells.
 * getShapeMatrix() returns a deep copy to protect the original matrices.
 */
final class ZBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public ZBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {7, 7, 0, 0},
                {0, 7, 7, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 7, 0, 0},
                {7, 7, 0, 0},
                {7, 0, 0, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}
