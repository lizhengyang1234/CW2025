// 方块旋转工具：负责管理当前方块的旋转状态。
// 每次旋转其实就是从方块的形状列表里切换到下一个矩阵。
// 这里不直接做矩阵旋转，旋转的形状已经在各个砖块类里定义好了。
//
// Brick rotation helper. It keeps track of which rotation state
// the brick is currently using and moves to the next one.
// It doesn’t compute rotation itself — each brick already stores
// its own rotated shapes.

package com.comp2042;

import com.comp2042.logic.bricks.Brick;

public class BrickRotator {

    private Brick brick;
    private int currentShape = 0;

    public NextShapeInfo getNextShape() {
        int nextShape = currentShape;
        nextShape = (++nextShape) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }


}
