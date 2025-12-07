package com.comp2042;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * 专门负责绘制右侧“Next”预览方块的小工具类。
 * 不保存状态，传什么画什么。
 */
public class NextBrickRenderer {

    private final int previewBrickSize;
    private final Paint[] brickColors;

    public NextBrickRenderer(int previewBrickSize, Paint[] brickColors) {
        this.previewBrickSize = previewBrickSize;
        this.brickColors = brickColors;
    }

    /**
     * 在 nextBrickPanel 中绘制下一块方块。
     */
    public void renderNextBrick(ViewData viewData, GridPane nextBrickPanel) {

        if (viewData == null || nextBrickPanel == null) {
            return;
        }

        nextBrickPanel.getChildren().clear();

        int[][] data = viewData.getNextBrickData();
        if (data == null) {
            return;
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                Rectangle r = new Rectangle(previewBrickSize, previewBrickSize);
                r.setFill(getFillColor(data[i][j]));
                r.setArcWidth(5);
                r.setArcHeight(5);

                nextBrickPanel.add(r, j, i);
            }
        }
    }

    private Paint getFillColor(int id) {
        if (id >= 0 && id < brickColors.length) {
            return brickColors[id];
        }
        return Color.WHITE;
    }
}