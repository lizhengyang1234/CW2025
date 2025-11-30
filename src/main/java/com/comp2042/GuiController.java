// 图形界面的控制器，主要负责把游戏逻辑的结果显示出来。
// 包括绘制棋盘、当前方块、处理键盘输入、显示分数、以及控制游戏的开始/暂停/结束。
// 本类和 GameController 配合，一个负责显示，一个负责游戏逻辑。
//
// GUI controller for the game. Handles drawing the board, updating the current
// falling brick, processing keyboard input, showing score, and managing game
// states such as start/pause/game-over. Works together with GameController to
// separate logic from presentation.

package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    // 方块尺寸：改大让画面更清晰
    private static final int BRICK_SIZE = 32;
    // 顶部隐藏的行数（逻辑上存在，但不显示）
    private static final int BOARD_HIDDEN_ROWS = 2;
    // 自动下落间隔（毫秒）
    private static final int FALL_INTERVAL_MS = 400;
    // 方块圆角
    private static final int BRICK_CORNER_ARC = 9;

    // 砖块颜色映射表，下标对应砖块 id（0~7）
    private static final Paint[] BRICK_COLORS = {
            Color.TRANSPARENT,  // 0
            Color.AQUA,         // 1
            Color.BLUEVIOLET,   // 2
            Color.DARKGREEN,    // 3
            Color.YELLOW,       // 4
            Color.RED,          // 5
            Color.BEIGE,        // 6
            Color.BURLYWOOD     // 7
    };

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;

    private InputEventListener eventListener;
    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
            }
        });

        gameOverPanel.setVisible(false);

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        // ======= 背景棋盘（固定格子） =======
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = BOARD_HIDDEN_ROWS; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - BOARD_HIDDEN_ROWS);
            }
        }

        // ======= 当前下落中的 4x4 方块 =======
        int[][] brickData = brick.getBrickData();
        rectangles = new Rectangle[brickData.length][brickData[0].length];

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brickData[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }

        // 初始位置：根据棋盘坐标→像素坐标（不再用 -42 魔法数）
        updateBrickPanelPosition(brick);

        // 自动下落计时器
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(FALL_INTERVAL_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    /**
     * 根据砖块 id 返回对应的填充颜色。
     * 如果 id 超出范围，返回白色作为默认值。
     */
    private Paint getFillColor(int id) {
        if (id >= 0 && id < BRICK_COLORS.length) {
            return BRICK_COLORS[id];
        }
        return Color.WHITE;
    }

    /**
     * 把 ViewData 中的 x / y（逻辑坐标）转换为像素坐标，更新 brickPanel 的位置。
     * 公式：
     *   像素X = gamePanel.X + x * (BRICK_SIZE + hgap)
     *   像素Y = gamePanel.Y + (y - 隐藏行数) * (BRICK_SIZE + vgap)
     */
    private void updateBrickPanelPosition(ViewData brick) {
        double cellWidth = BRICK_SIZE + brickPanel.getHgap();
        double cellHeight = BRICK_SIZE + brickPanel.getVgap();

        double x = gamePanel.getLayoutX() + brick.getxPosition() * cellWidth;
        double y = gamePanel.getLayoutY() + (brick.getyPosition() - BOARD_HIDDEN_ROWS) * cellHeight;

        brickPanel.setLayoutX(x);
        brickPanel.setLayoutY(y);
    }

    private void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE) {
            // 更新位置
            updateBrickPanelPosition(brick);

            // 更新 4x4 方块内每一格的颜色
            int[][] brickData = brick.getBrickData();
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    setRectangleData(brickData[i][j], rectangles[i][j]);
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int i = BOARD_HIDDEN_ROWS; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(BRICK_CORNER_ARC);
        rectangle.setArcWidth(BRICK_CORNER_ARC);
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel =
                        new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty integerProperty) {
        // 这里可以绑定一个 Label 来显示分数，目前留空即可
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }
}