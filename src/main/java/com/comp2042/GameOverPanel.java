// 游戏结束界面，用来显示“GAME OVER” 提示。
// 只是一个简单的面板，中心放了一个标签，并套用了对应的 CSS 样式。
// 主要用于在游戏无法继续时切换到这个界面。
//
// Simple game-over panel. Shows a “GAME OVER” label in the center,
// using the CSS style defined for it. Used when the game ends.

package com.comp2042;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;


public class GameOverPanel extends BorderPane {

    public GameOverPanel() {
        final Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("gameOverStyle");
        setCenter(gameOverLabel);
    }

}
