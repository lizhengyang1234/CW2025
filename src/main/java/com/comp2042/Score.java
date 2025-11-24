// 分数类：用来记录玩家当前的得分，并提供更新和重置的方法。
// 使用 JavaFX 的 IntegerProperty，可以让 GUI 自动监听分数变化并实时更新显示。
// 本类本身不负责计算分数，只是一个简单的计分器。
//
// Score class: stores the player's score using an IntegerProperty,
// so the GUI can automatically react to score changes. Provides
// methods for adding points and resetting the score. Does not
// contain scoring logic itself.

package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);

    public IntegerProperty scoreProperty() {
        return score;
    }

    public void add(int i){
        score.setValue(score.getValue() + i);
    }

    public void reset() {
        score.setValue(0);
    }
}
