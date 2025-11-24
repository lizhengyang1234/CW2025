// 用来保存“下一个旋转形状”的简单数据类。
// 在旋转方块之前，GameController 或 Board 会先通过 BrickRotator
// 获取下一种旋转后的形状和对应的位置编号。
// 这个类只是把这两个信息打包在一起，不包含任何逻辑。
//
// Simple data holder for the next rotation state of a brick.
// BrickRotator uses this to return the rotated matrix and the index
// of that rotation. The class only stores data and has no logic.

package com.comp2042;

public final class NextShapeInfo {

    private final int[][] shape;
    private final int position;

    public NextShapeInfo(final int[][] shape, final int position) {
        this.shape = shape;
        this.position = position;
    }

    public int[][] getShape() {
        return MatrixOperations.copy(shape);
    }

    public int getPosition() {
        return position;
    }
}
