package com.comp2042;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * 统一管理界面相关的常量和颜色配置。
 
 */
public final class ViewConfig {

    // 主游戏区域方块尺寸
    public static final int BRICK_SIZE = 32;
    // 预览区域方块尺寸（比主区域小）
    public static final int PREVIEW_BRICK_SIZE = 20;
    // 顶部隐藏的行数（逻辑上存在，但不显示）
    public static final int BOARD_HIDDEN_ROWS = 2;
    // 初始自动下落间隔（毫秒）
    public static final int FALL_INTERVAL_MS = 400;
    // 方块圆角
    public static final int BRICK_CORNER_ARC = 9;

    /**
     * 砖块颜色映射表：
     * 0 = 空
     * 1~7 = 不同形状方块
     * 在这里把绿、蓝这些颜色稍微提亮一点
     */
    private static final Paint[] BRICK_COLORS = {
            Color.TRANSPARENT,                 // 0
            Color.AQUA.brighter(),             // 1
            Color.BLUEVIOLET.brighter(),       // 2
            Color.LIMEGREEN.brighter(),        // 3 更亮的绿色
            Color.YELLOW.brighter(),           // 4
            Color.RED.brighter(),              // 5
            Color.BEIGE,                       // 6
            Color.BURLYWOOD.brighter()         // 7
    };

    private ViewConfig() {
        // 工具类，不需要实例化
    }

    /**
     * 根据方块 id 返回颜色，如果超出范围则返回白色。
     */
    public static Paint getBrickColor(int id) {
        if (id >= 0 && id < BRICK_COLORS.length) {
            return BRICK_COLORS[id];
        }
        return Color.WHITE;
    }
}