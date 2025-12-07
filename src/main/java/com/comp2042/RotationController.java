package com.comp2042;

import java.awt.Point;

/**
 * 专门处理“旋转相关”的逻辑：
 * - 询问 BrickRotator 下一个旋转形状
 * - 检查该形状在当前位置是否会碰撞
 * - 如果可以，就应用这次旋转
 */
public class RotationController {

    /**
     * 尝试逆时针旋转当前方块。
     *
     * @param boardState   背景棋盘
     * @param brickRotator 旋转器（维护当前形状 / 下一个旋转）
     * @param currentOffset 当前方块左上角坐标
     * @return true = 旋转成功；false = 旋转后会碰撞，保持原状
     */
    public boolean tryRotateLeft(BoardState boardState,
                                 BrickRotator brickRotator,
                                 Point currentOffset) {

        NextShapeInfo nextShape = brickRotator.getNextShape();

        boolean conflict = MatrixOperations.intersect(
                boardState.getMatrix(),
                nextShape.getShape(),
                currentOffset.x,
                currentOffset.y
        );

        if (conflict) {
            return false;
        }

        // 真正更新当前形状
        brickRotator.setCurrentShape(nextShape.getPosition());
        return true;
    }
}