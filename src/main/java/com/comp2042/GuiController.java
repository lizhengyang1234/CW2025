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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

/**
 * GUI controller: draw board, current brick, next-brick preview,
 * handle keyboard input, score display, pause / resume / new game.
 */
public class GuiController implements Initializable {

    // 主游戏区域方块尺寸
    private static final int BRICK_SIZE = 32;
    // 预览区域方块尺寸（比主区域小）
    private static final int PREVIEW_BRICK_SIZE = 20;
    // 顶部隐藏的行数（逻辑上存在，但不显示）
    private static final int BOARD_HIDDEN_ROWS = 2;
    // 自动下落间隔（毫秒）
    private static final int FALL_INTERVAL_MS = 400;
    // 方块圆角
    private static final int BRICK_CORNER_ARC = 9;

    // 砖块颜色映射表，下标对应砖块 id（0~7）
    private static final Paint[] BRICK_COLORS = {
            Color.TRANSPARENT,
            Color.AQUA,
            Color.BLUEVIOLET,
            Color.DARKGREEN,
            Color.YELLOW,
            Color.RED,
            Color.BEIGE,
            Color.BURLYWOOD
    };

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Button pauseButton;

    @FXML
    private Button newGameButton;

    // 分数标签
    @FXML
    private Label scoreLabel;

    // ★ 新增：显示总消除行数的标签（在 FXML 中 fx:id="linesLabel"）
    @FXML
    private Label linesLabel;

    // 预览“下一个方块”的小网格（在 FXML 中 fx:id="nextBrickPanel"）
    @FXML
    private GridPane nextBrickPanel;

    // 背景棋盘的格子
    private Rectangle[][] displayMatrix;
    // 当前下落方块 4x4 的格子
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
                if (!isPause.get() && !isGameOver.get()) {

                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(
                                new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }

                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(
                                new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }

                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(
                                new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }

                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }

                    // 空格键：硬降
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        hardDrop();
                        keyEvent.consume();
                    }
                }

                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }

                if (keyEvent.getCode() == KeyCode.P) {
                    pauseGame(null);
                    keyEvent.consume();
                }
            }
        });

        gameOverPanel.setVisible(false);
    }

    /**
     * 初始化游戏视图：创建背景格子、当前砖块 4x4、以及下一块预览。
     */
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

        // 确保当前方块层在最前面
        brickPanel.toFront();

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

        // 初始位置
        updateBrickPanelPosition(brick);

        // 初始时绘制一次“下一个方块”预览
        refreshNextBrick(brick);

        // 自动下落计时器
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(FALL_INTERVAL_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        if (pauseButton != null) {
            pauseButton.setText("⏸");
        }
    }

    private Paint getFillColor(int id) {
        if (id >= 0 && id < BRICK_COLORS.length) return BRICK_COLORS[id];
        return Color.WHITE;
    }

    /**
     * 根据逻辑坐标更新 4x4 当前砖块的面板位置。
     */
    private void updateBrickPanelPosition(ViewData brick) {
        double cellWidth = BRICK_SIZE + brickPanel.getHgap();
        double cellHeight = BRICK_SIZE + brickPanel.getVgap();

        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * cellWidth);
        brickPanel.setLayoutY(gamePanel.getLayoutY() + (brick.getyPosition() - BOARD_HIDDEN_ROWS) * cellHeight);
    }

    private void refreshBrick(ViewData brick) {
        if (!isPause.get()) {
            brickPanel.toFront();
            updateBrickPanelPosition(brick);

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
        if (!isPause.get()) {
            DownData downData = eventListener.onDownEvent(event);

            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel =
                        new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }

            // 更新当前方块在主棋盘上的位置
            refreshBrick(downData.getViewData());
            // 同时更新右侧“下一个方块”预览
            refreshNextBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    /**
     * 一键硬降：空格键调用。
     * 需要配合 EventType.HARD_DROP 和 GameController.onDownEvent 的逻辑。
     */
    private void hardDrop() {
        if (isPause.get() || isGameOver.get()) {
            return;
        }

        DownData downData =
                eventListener.onDownEvent(
                        new MoveEvent(EventType.HARD_DROP, EventSource.USER));

        if (downData.getClearRow() != null
                && downData.getClearRow().getLinesRemoved() > 0) {
            NotificationPanel notificationPanel =
                    new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
            groupNotification.getChildren().add(notificationPanel);
            notificationPanel.showScore(groupNotification.getChildren());
        }

        refreshBrick(downData.getViewData());
        refreshNextBrick(downData.getViewData());
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    // 分数绑定到右侧 scoreLabel
    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreLabel != null && scoreProperty != null) {
            scoreLabel.textProperty().bind(scoreProperty.asString());
        }
    }

    // ★ 新增：把“总消行数”绑定到右侧的 linesLabel
    public void bindLines(IntegerProperty linesProperty) {
        if (linesLabel != null && linesProperty != null) {
            linesLabel.textProperty().bind(linesProperty.asString());
        }
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.set(true);
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.set(false);
        isGameOver.set(false);

        if (pauseButton != null) pauseButton.setText("⏸");
    }

    // 暂停 / 继续 按钮 和 P 键 都会调用这个方法
    public void pauseGame(ActionEvent actionEvent) {
        if (timeLine == null) return;

        if (isPause.get()) {
            isPause.set(false);
            timeLine.play();
            if (pauseButton != null) {
                pauseButton.setText("⏸");
            }
        } else {
            isPause.set(true);
            timeLine.pause();
            if (pauseButton != null) {
                pauseButton.setText("▶");
            }
        }

        gamePanel.requestFocus();
    }

    /**
     * 绘制“下一个方块”的预览小图。
     * 使用 ViewData 里的 nextBrickData，在 nextBrickPanel 里用小方块画出来。
     */
    public void refreshNextBrick(ViewData viewData) {
        if (nextBrickPanel == null || viewData == null) {
            return;
        }

        nextBrickPanel.getChildren().clear();

        int[][] data = viewData.getNextBrickData();
        if (data == null) {
            return;
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                Rectangle r = new Rectangle(PREVIEW_BRICK_SIZE, PREVIEW_BRICK_SIZE);
                r.setFill(getFillColor(data[i][j]));
                r.setArcWidth(5);
                r.setArcHeight(5);
                nextBrickPanel.add(r, j, i);
            }
        }
    }
}