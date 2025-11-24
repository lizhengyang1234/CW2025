package com.comp2042.logic.bricks;

import com.comp2042.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/*
 * J 型方块
 * 这里把它所有 4 种旋转形态都用矩阵方式存起来。
 * 每个矩阵里用数字 2 来表示 J 方块占据的位置。
 * getShapeMatrix() 会返回一个深拷贝，避免外部把原数据改坏。
 *
 * J-shaped Tetris brick
 * Stores all 4 rotation states as 2D matrices.
 * Uses the number 2 to mark filled cells.
 * getShapeMatrix() returns a deep copy so the original matrices stay safe.
 */
final class JBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public JBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {2, 2, 2, 0},
                {0, 0, 2, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {0, 2, 2, 0},
                {0, 2, 0, 0},
                {0, 2, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {0, 2, 0, 0},
                {0, 2, 2, 2},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 0, 2, 0},
                {0, 0, 2, 0},
                {0, 2, 2, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}
