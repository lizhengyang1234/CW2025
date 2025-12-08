package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.NormalBrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * 重构后的棋盘核心：
 * - 背景状态交给 BoardState 管
 * - 旋转交给 BrickRotator
 * - 平移/碰撞交给 MovementController
 * - 不同模式（普通 / 炸弹）通过不同 BrickGenerator 实现
 *
 * 这里还负责记录“最近一次炸弹爆炸的格子”，给 GUI 做爆炸动画用。
 */
public class SimpleBoard implements Board {

    /** 新方块出生位置（列、行） */
    private static final int START_X = 4;
    private static final int START_Y = 10;

    /** 棋盘尺寸（行、列） */
    private final int rows;
    private final int cols;

    /** 背景棋盘状态（已经落下的方块） */
    private final BoardState boardState;

    /**
     * 为了兼容旧的 MatrixOperations 逻辑，这里缓存一份对
     * boardState 内部矩阵的引用（注意：不要自己 new）。
     */
    private int[][] currentGameMatrix;

    /** 当前方块的形状与旋转控制 */
    private final BrickRotator brickRotator;

    /** 方块生成器：普通模式 / 炸弹模式 使用不同实现 */
    private BrickGenerator brickGenerator;

    /** 专门处理平移 / 碰撞的逻辑 */
    private final MovementController movementController;

    /** 专门处理旋转 / 碰撞的逻辑 */
    private final RotationController rotationController;

    /** 当前活动方块左上角位置（列 = x, 行 = y） */
    private Point currentOffset;

    /** 分数模型 */
    private final Score score;

    /** 是否启用炸弹模式 */
    private final boolean bombMode;

    /** 炸弹方块在矩阵中的 id（写死为 8） */
    private static final int BOMB_ID = 8;

    /** 最近一次爆炸所影响到的格子列表（给 GUI 做动画用） */
    private final List<Point> lastExplosionCells = new ArrayList<>();

    /** 只读访问给 GUI 使用 */
    public List<Point> getLastExplosionCells() {
        return lastExplosionCells;
    }

    /* ======================= 构造函数 ======================= */

    /** 默认构造：普通模式（不含炸弹） */
    public SimpleBoard(int rows, int cols) {
        this(rows, cols, false);
    }

    /** 可选择是否启用炸弹模式的构造 */
    public SimpleBoard(int rows, int cols, boolean bombMode) {
        this.rows = rows;
        this.cols = cols;
        this.bombMode = bombMode;

        // 棋盘状态
        this.boardState = new BoardState(rows, cols);
        this.currentGameMatrix = boardState.getMatrix();

        // 控制组件
        this.brickRotator = new BrickRotator();
        this.movementController = new MovementController();
        this.rotationController = new RotationController();

        this.score = new Score();

        // 根据模式选择不同的方块生成器
        if (bombMode) {
            // 带炸弹的随机生成器（内部有 allowBombs=true）
            this.brickGenerator = new RandomBrickGenerator(true);
        } else {
            // 只有经典的 7 种普通砖块
            this.brickGenerator = new NormalBrickGenerator();
        }
    }

    /* ================== Board 接口：移动 ================== */

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

    /** 通用平移逻辑：尝试将当前方块移动 (dx, dy) */
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
            // 碰撞或越界，移动失败
            return false;
        }

        currentOffset = newOffset;
        return true;
    }

    /* ================== Board 接口：旋转 ================== */

    @Override
    public boolean rotateLeftBrick() {
        return rotationController.tryRotateLeft(boardState, brickRotator, currentOffset);
    }

    /* ============ Board 接口：生成新方块 / 视图数据 ============ */

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);

        // 出生位置
        currentOffset = new Point(START_X, START_Y);

        // 一出生就冲突 → 游戏结束
        boolean conflict = movementController.hasCollision(
                boardState.getMatrix(),
                brickRotator.getCurrentShape(),
                currentOffset
        );

        // 同步引用
        currentGameMatrix = boardState.getMatrix();
        // 每次生成新方块，把上一次爆炸记录清空
        lastExplosionCells.clear();

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
                // next brick 的第一个旋转形状（保持原来的行为）
                brickGenerator.getNextBrick().getShapeMatrix().get(0)
        );
    }

    /* ======= Board 接口：合并到背景 / 炸弹爆炸 / 消行 ======= */

    @Override
    public void mergeBrickToBackground() {

        int[][] shape = brickRotator.getCurrentShape();
        int x = currentOffset.x;
        int y = currentOffset.y;

        // 拿到最新背景矩阵引用
        currentGameMatrix = boardState.getMatrix();

        // 炸弹模式 + 当前形状里有炸弹 → 走爆炸逻辑
        if (bombMode && containsBomb(shape)) {
            lastExplosionCells.clear();
            explode(shape, x, y);
        } else {
            // 普通合并
            currentGameMatrix = MatrixOperations.merge(currentGameMatrix, shape, x, y);
            boardState.replaceMatrix(currentGameMatrix);

            // 非炸弹则清空爆炸记录
            lastExplosionCells.clear();
        }
    }

    /** 当前 4×4 形状里是否存在炸弹格子（id == BOMB_ID） */
    private boolean containsBomb(int[][] shape) {
        for (int[] row : shape) {
            for (int cell : row) {
                if (cell == BOMB_ID) return true;
            }
        }
        return false;
    }

    /**
     * 炸弹爆炸：
     * 以每个炸弹格子为中心，清除 3×3 区域（并记录所有被清除的格子坐标）。
     *
     * ⚠ 注意：
     *  - 这里已经把矩阵里的值改为 0（逻辑上已经“消失”）
     *  - 但 lastExplosionCells 里保存了这些格子的位置，
     *    GUI 可以用它做：先画黑色 → 再刷新背景 的动画效果。
     */
    private void explode(int[][] shape, int offsetX, int offsetY) {

        currentGameMatrix = boardState.getMatrix();

        int maxY = currentGameMatrix.length;
        int maxX = currentGameMatrix[0].length;

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {

                if (shape[i][j] == BOMB_ID) {

                    int cx = offsetX + j;
                    int cy = offsetY + i;

                    // 以 (cx, cy) 为中心，清除 3×3 区域
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {

                            int bx = cx + dx;
                            int by = cy + dy;

                            if (bx >= 0 && bx < maxX && by >= 0 && by < maxY) {

                                // 记录动画用的爆炸格子（给 GUI）
                                lastExplosionCells.add(new Point(bx, by));

                                // 逻辑上立刻清空
                                currentGameMatrix[by][bx] = 0;
                            }
                        }
                    }
                }
            }
        }

        // 将更新后的矩阵写回 BoardState
        boardState.replaceMatrix(currentGameMatrix);
    }

    @Override
    public ClearRow clearRows() {
        ClearRow result = boardState.clearFullRows();
        currentGameMatrix = boardState.getMatrix();
        return result;
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void newGame() {
        boardState.reset();
        currentGameMatrix = boardState.getMatrix();
        score.reset();
        lastExplosionCells.clear();
        createNewBrick();
    }
}