package com.comp2042.logic.bricks;

import com.comp2042.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/*
 * T 型方块（形状就是一个 T 字）。
 * T 方块有四个方向，所以这里把四种旋转状态都存成矩阵。
 * 数字 6 表示 T 方块占据的位置。
 * getShapeMatrix() 返回深拷贝，避免外部修改原始数据。
 *
 * T-shaped Tetris brick.
 * Has four rotation states, so four matrices are stored here.
 * Uses number 6 for the filled cells.
 * getShapeMatrix() returns a deep copy to protect the original.
 */
final class TBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public TBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {6, 6, 6, 0},
                {0, 6, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 6, 0, 0},
                {0, 6, 6, 0},
                {0, 6, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 6, 0, 0},
                {6, 6, 6, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 6, 0, 0},
                {6, 6, 0, 0},
                {0, 6, 0, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}
