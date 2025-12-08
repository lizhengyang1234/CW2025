package com.comp2042.logic.bricks;

import java.util.ArrayList;
import java.util.List;

public class BombBrick implements Brick {

    @Override
    public List<int[][]> getShapeMatrix() {

        List<int[][]> shapes = new ArrayList<>();

        // 固定 4x4，只在中心放一个炸弹
        int[][] shape = {
                {0, 0, 0, 0},
                {0, 8, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };

        // 炸弹不旋转 → 4 个形状都一样
        shapes.add(shape);
        shapes.add(shape);
        shapes.add(shape);
        shapes.add(shape);

        return shapes;
    }
}