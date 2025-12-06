package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

// 分数系统：现在除了分数，还额外记录“总共消掉了多少行”。
// Scoring system: now also tracks how many lines have been cleared in total.
public final class Score {

    // 当前分数
    private final IntegerProperty score = new SimpleIntegerProperty(0);

    // 新增：总共消除的行数
    private final IntegerProperty lines = new SimpleIntegerProperty(0);

    public IntegerProperty scoreProperty() {
        return score;
    }

    // 新增：让 GUI 绑定总消行数
    public IntegerProperty linesProperty() {
        return lines;
    }

    // 加分
    public void add(int i) {
        score.set(score.get() + i);
    }

    // 新增：增加消行统计
    public void addLines(int count) {
        lines.set(lines.get() + count);
    }

    // 重置分数和总消行数
    public void reset() {
        score.set(0);
        lines.set(0);
    }
}