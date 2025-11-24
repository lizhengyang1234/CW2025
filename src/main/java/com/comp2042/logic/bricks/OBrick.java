package com.comp2042.logic.bricks;

import com.comp2042.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/*
 * O 型方块（正方形那块）。
 * 这个方块的特点是旋转后形状完全不变，所以只需要保存一个矩阵。
 * 数字 4 表示 O 方块的填充位置。
 * getShapeMatrix() 返回深拷贝，保护原始数据不被外部修改。
 *
 * O-shaped Tetris brick (the square piece).
 * Since it looks the same in all rotations, only one matrix is stored.
 * Uses number 4 to mark the filled cells.
 * getShapeMatrix() returns a deep copy to keep the original safe.
 */
final class OBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    public OBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }

}
