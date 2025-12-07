// 重构后的 SimpleBoard：只负责“协调”各个组件，自己不再写大段逻辑。
// - 背景棋盘 → BoardState
// - 移动 / 碰撞 → MovementController
// - 旋转       → RotationController
// - 方块生成   → BrickGenerator / RandomBrickGenerator
// - 分数       → Score
//
// 这样每个类职责都很单一，以后加功能（幽灵块、墙踢、不同模式）就会轻松很多。

package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.Point;

public class SimpleBoard implements Board {

    /** 新方块出生位置 */
    private static final int START_X = 4;
    private static final int START_Y = 10;

    /** 棋盘尺寸（行、列） */
    private final int rows;
    private final int cols;

    /** 背景棋盘状态（已经落下的方块） */
    private final BoardState boardState;

    /** 负责当前方块的形状与旋转 */
    private final BrickRotator brickRotator;

    /** 方块生成器（负责随机出下一个 Brick） */
    private final BrickGenerator brickGenerator;

    /** 专门处理平移 / 碰撞的逻辑 */
    private final MovementController movementController;

    /** 专门处理旋转 / 碰撞的逻辑 */
    private final RotationController rotationController;

    /** 当前活动方块左上角位置（列 = x, 行 = y） */
    private Point currentOffset;

    /** 分数模型 */
    private final Score score;

    public SimpleBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        this.boardState = new BoardState(rows, cols);
        this.brickRotator = new BrickRotator();
        this.brickGenerator = new RandomBrickGenerator();
        this.movementController = new MovementController();
        this.rotationController = new RotationController();

        this.score = new Score();
    }

    // ============= Board 接口实现：移动相关 =============

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
     * 通用移动逻辑：尝试把当前方块平移 (dx, dy)。
     *
     * @return true = 移动成功；false = 会碰撞，位置保持不变。
     */
    private boolean tryMoveBrick(int dx, int dy) {
        int[][] shape = brickRotator.getCurrentShape();

        Point newOffset = movementController.tryTranslate(
                boardState,
                shape,
                currentOffset,
                dx,
                dy
        );

        if (newOffset == null) {
            // 碰撞或越界
            return false;
        }

        currentOffset = newOffset;
        return true;
    }

    // ============= Board 接口实现：旋转相关 =============

    @Override
    public boolean rotateLeftBrick() {
        return rotationController.tryRotateLeft(boardState, brickRotator, currentOffset);
    }

    // ============= Board 接口实现：新方块 / 棋盘数据 =============

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);

        // 出生位置
        currentOffset = new Point(START_X, START_Y);

        // 一出生就冲突 → 游戏结束信号（返回 true）
        boolean conflict = movementController.hasCollision(
                boardState.getMatrix(),
                brickRotator.getCurrentShape(),
                currentOffset
        );
        return conflict;
    }

    @Override
    public int[][] getBoardMatrix() {
        return boardState.getMatrix();
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y,
                // next brick 的第一个旋转形状，保持原来行为
                brickGenerator.getNextBrick().getShapeMatrix().get(0)
        );
    }

    // ============= Board 接口实现：合并 / 消行 / 分数 / 新游戏 =============

    @Override
    public void mergeBrickToBackground() {
        boardState.mergeBrick(
                brickRotator.getCurrentShape(),
                currentOffset.x,
                currentOffset.y
        );
    }

    @Override
    public ClearRow clearRows() {
        // 清行本身不加分，加分由 GameController 根据 ClearRow 决定
        return boardState.clearFullRows();
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void newGame() {
        boardState.reset();
        score.reset();
        createNewBrick();
    }
}