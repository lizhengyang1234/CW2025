package com.comp2042.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 纯“普通模式”的方块生成器：
 * 只会生成经典的 7 种俄罗斯方块，不包含炸弹。
 *
 * 用一个小队列 nextBricks，让 UI 可以预览“下一块”。
 * 逻辑基本和你最早的 RandomBrickGenerator 一样，只是没有炸弹。
 */
public class NormalBrickGenerator implements BrickGenerator {

    // 普通 7 种俄罗斯方块
    private final List<Brick> bricks = new ArrayList<>();

    // 当前 + 下一块 的队列
    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    public NormalBrickGenerator() {
        // 把 7 种标准方块加入列表
        bricks.add(new IBrick());
        bricks.add(new JBrick());
        bricks.add(new LBrick());
        bricks.add(new OBrick());
        bricks.add(new SBrick());
        bricks.add(new TBrick());
        bricks.add(new ZBrick());

        // 初始化队列：先塞两个
        nextBricks.add(randomBrick());
        nextBricks.add(randomBrick());
    }

    /** 从 7 种普通砖块里随机选一个 */
    private Brick randomBrick() {
        int idx = ThreadLocalRandom.current().nextInt(bricks.size());
        return bricks.get(idx);
    }

    /**
     * 取出“当前方块”，同时保证队列里至少还有一个“下一块”。
     */
    @Override
    public Brick getBrick() {
        if (nextBricks.size() <= 1) {
            nextBricks.add(randomBrick());
        }
        return nextBricks.poll();
    }

    /**
     * 返回“下一块”，给右侧预览用。
     */
    @Override
    public Brick getNextBrick() {
        if (nextBricks.isEmpty()) {
            nextBricks.add(randomBrick());
        }
        return nextBricks.peek();
    }
}