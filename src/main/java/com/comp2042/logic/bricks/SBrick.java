package com.comp2042.logic.bricks;

import com.comp2042.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/*
 * S 型方块（形状像一条倾斜的 S）。
 * 这个方块只有两个不同的旋转方向，所以只需要保存两个矩阵。
 * 用数字 5 表示 S 方块的位置。
 * getShapeMatrix() 会返回深拷贝，避免外面修改原始数据。
 *
 * S-shaped Tetris brick.
 * Only has two unique rotation states, so two matrices are stored.
 * Uses number 5 to mark the filled cells.
 * getShapeMatrix() returns a deep copy to keep the original safe.
 */
final class SBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public SBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {0, 5, 5, 0},
                {5, 5, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {5, 0, 0, 0},
                {5, 5, 0, 0},
                {0, 5, 0, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}
