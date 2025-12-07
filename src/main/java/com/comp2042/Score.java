package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 负责分数、消行数、历史最高分的统计和持久化。
 *
 * Handles:
 * - current score
 * - total cleared lines
 * - best (high) score (saved to a small text file)
 */
public class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty lines = new SimpleIntegerProperty(0);

    // ★ 新增：历史最高分
    private final IntegerProperty highScore = new SimpleIntegerProperty(0);

    // 存历史最高分的小文件名（和程序同一目录）
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    public Score() {
        // 启动时尝试加载历史最高分
        loadHighScore();
    }

    // 当前分数相关
    public int getScore() {
        return score.get();
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    // 消除总行数相关
    public int getLines() {
        return lines.get();
    }

    public IntegerProperty linesProperty() {
        return lines;
    }

    // 历史最高分相关
    public int getHighScore() {
        return highScore.get();
    }

    public IntegerProperty highScoreProperty() {
        return highScore;
    }

    /**
     * 当前分数增加 value，并自动更新历史最高分。
     */
    public void add(int value) {
        if (value <= 0) return;

        int newScore = score.get() + value;
        score.set(newScore);

        if (newScore > highScore.get()) {
            highScore.set(newScore);
        }
    }

    /**
     * 累加消除的行数（用于等级/加速等）
     */
    public void addLines(int value) {
        if (value <= 0) return;
        lines.set(lines.get() + value);
    }

    /**
     * 新游戏时重置当前分数和行数（历史最高分不清零）
     */
    public void reset() {
        score.set(0);
        lines.set(0);
    }

    /**
     * 从本地文件读取历史最高分。
     */
    public void loadHighScore() {
        try {
            Path path = Path.of(HIGH_SCORE_FILE);
            if (Files.exists(path)) {
                String txt = Files.readString(path).trim();
                int stored = Integer.parseInt(txt);
                if (stored >= 0) {
                    highScore.set(stored);
                }
            }
        } catch (Exception ignored) {
            // 读不出来就算了，当成 0
        }
    }

    /**
     * 把当前 highScore 保存到本地文件。
     * 一般在游戏结束时调用。
     */
    public void saveHighScore() {
        try {
            Path path = Path.of(HIGH_SCORE_FILE);
            Files.writeString(
                    path,
                    Integer.toString(highScore.get()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException ignored) {
            // 不影响游戏运行，可以忽略写文件错误
        }
    }
}