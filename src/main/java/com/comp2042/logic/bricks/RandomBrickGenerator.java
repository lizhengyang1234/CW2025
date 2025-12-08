package com.comp2042.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机生成方块的类。
 * ⭐ 现在支持炸弹模式（allowBombs = true 时才会出现炸弹）。
 * 使用 nextBricks 队列支持“下一块预览”。
 */
public class RandomBrickGenerator implements BrickGenerator {

    private final List<Brick> normalBricks = new ArrayList<>();
    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    /** 是否启用炸弹模式（由主菜单决定） */
    private final boolean allowBombs;

    /** 炸弹出现概率 */
    private static final double BOMB_PROBABILITY = 0.10; // 10%

    /** 默认构造 = 不允许炸弹（保持原来普通模式） */
    public RandomBrickGenerator() {
        this(false);
    }

    /** 新构造：可以选择是否允许炸弹 */
    public RandomBrickGenerator(boolean allowBombs) {
        this.allowBombs = allowBombs;

        // 普通 7 种砖块
        normalBricks.add(new IBrick());
        normalBricks.add(new JBrick());
        normalBricks.add(new LBrick());
        normalBricks.add(new OBrick());
        normalBricks.add(new SBrick());
        normalBricks.add(new TBrick());
        normalBricks.add(new ZBrick());

        // 初始化 next 队列
        nextBricks.add(generate());
        nextBricks.add(generate());
    }

    /** 根据模式生成随机方块 */
    private Brick generate() {

        // ★ 炸弹模式：10% 生成 BombBrick
        if (allowBombs && ThreadLocalRandom.current().nextDouble() < BOMB_PROBABILITY) {
            return new BombBrick();
        }

        // 普通模式（或未触发炸弹概率）
        return normalBricks.get(ThreadLocalRandom.current().nextInt(normalBricks.size()));
    }

    /** 取出当前方块，并补一个新的到队尾 */
    @Override
    public Brick getBrick() {
        if (nextBricks.size() <= 1) {
            nextBricks.add(generate());
        }
        return nextBricks.poll();
    }

    /** 返回下一块（用于 UI） */
    @Override
    public Brick getNextBrick() {
        if (nextBricks.isEmpty()) {
            nextBricks.add(generate());
        }
        return nextBricks.peek();
    }
}