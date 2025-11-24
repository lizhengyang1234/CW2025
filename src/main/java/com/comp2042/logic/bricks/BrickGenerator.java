package com.comp2042.logic.bricks;

/*
 * 用来生成俄罗斯方块的接口。
 * 主要负责给游戏提供当前方块和下一个方块。
 * 具体的生成方式会在实现类（例如 RandomBrickGenerator）里完成。
 *
 * Interface for generating Tetris bricks.
 * Provides the current brick and the next brick for the game.
 * The actual generation logic is implemented in classes like RandomBrickGenerator.
 */
public interface BrickGenerator {

    // 返回当前要使用的方块
    Brick getBrick();

    // 返回下一块方块（用于界面预览）
    Brick getNextBrick();
}