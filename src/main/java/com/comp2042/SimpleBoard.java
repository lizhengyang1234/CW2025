// 简化版的棋盘实现类，负责游戏逻辑的核心部分。
// 包括：检测方块是否能移动、旋转、生成新方块、合并到背景、清除整行、更新分数等。
// 可以理解成整个 Tetris 的“数据层 + 规则层”。
// GameController 只是调用这里的方法，这里才是真正决定游戏行为的地方。
//
// Simple board implementation containing the core game logic.
// Handles checking movement, rotation, creating new bricks,
// merging bricks into the background, clearing full rows,
// and updating the score. This class represents the main
// rule/control layer of the Tetris board.
package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;

public class SimpleBoard implements Board {

    private static final int START_X = 4;   // 方块初始 X 坐标
    private static final int START_Y = 10;  // 方块初始 Y 坐标

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean moveBrickDown() {
        return tryMoveBrick(0, 1);
    }

    @Override
    public boolean moveBrickLeft() {
        return tryMoveBrick(-1, 0);
    }

    @Override
    public boolean moveBrickRight() {
        return tryMoveBrick(1, 0);
    }

    /**
     * 尝试按照给定方向移动当前方块（dx, dy）。
     * 返回 true 表示移动成功，false 表示发生碰撞或越界。
     */
    private boolean tryMoveBrick(int dx, int dy) {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point newOffset = new Point(currentOffset);
        newOffset.translate(dx, dy);
        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                brickRotator.getCurrentShape(),
                (int) newOffset.getX(),
                (int) newOffset.getY()
        );
        if (conflict) {
            return false;
        } else {
            currentOffset = newOffset;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = MatrixOperations.intersect(
                currentMatrix,
                nextShape.getShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        // 起始位置
        currentOffset = new Point(START_X, START_Y);
        return MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY(),
                brickGenerator.getNextBrick().getShapeMatrix().get(0)
        );
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        createNewBrick();
    }
}