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
import javafx.util.Duration;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

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
            Color.LIMEGREEN,   // 更亮的绿色
            Color.YELLOW,
            Color.RED,
            Color.BEIGE,
            Color.BURLYWOOD
    };

    @FXML private GridPane gamePanel;
    @FXML private Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;

    @FXML private Button pauseButton;
    @FXML private Button newGameButton;

    @FXML private Label scoreLabel;
    @FXML private Label linesLabel;
    @FXML private Label highScoreLabel;

    @FXML private GridPane nextBrickPanel;

    private InputEventListener eventListener;
    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    // 画面专用类：负责棋盘 / 幽灵影子 / Next 预览
    private BoardView boardView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 加载数码字体
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);

        // 构造 BoardView（此时 FXML 已经注入完毕）
        boardView = new BoardView(
                gamePanel,
                brickPanel,
                nextBrickPanel,
                BRICK_SIZE,
                PREVIEW_BRICK_SIZE,
                BOARD_HIDDEN_ROWS,
                BRICK_CORNER_ARC,
                BRICK_COLORS
        );

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        // 键盘输入统一在这里处理
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
     * GameController 初始化时调用：
     * - 初始化棋盘视图
     * - 创建下落时间线
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {

        // 委托给 BoardView 画棋盘、当前块、next、幽灵影子
        boardView.initGameView(boardMatrix, brick);

        // 自动下落计时器
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(FALL_INTERVAL_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        pauseButton.setText("Pause");
    }

    /* =================== 和 BoardView 的交互 =================== */

    private void refreshBrick(ViewData brick) {
        if (!isPause.get()) {
            boardView.refreshBrick(brick);
        }
    }

    public void refreshGameBackground(int[][] board) {
        boardView.refreshBackground(board);
    }

    public void refreshNextBrick(ViewData viewData) {
        boardView.renderNextBrick(viewData);
    }

    /* =================== 下落 / 硬降 / 通知 =================== */

    private void moveDown(MoveEvent event) {

        if (!isPause.get()) {

            DownData downData = eventListener.onDownEvent(event);

            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {

                NotificationPanel np =
                        new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(np);
                np.showScore(groupNotification.getChildren());
            }

            refreshBrick(downData.getViewData());
            refreshNextBrick(downData.getViewData());
        }

        gamePanel.requestFocus();
    }

    /**
     * 一键硬降：空格键调用。
     */
    private void hardDrop() {

        if (isPause.get() || isGameOver.get()) return;

        DownData downData =
                eventListener.onDownEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));

        if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {

            NotificationPanel np =
                    new NotificationPanel("+" + downData.getClearRow().getScoreBonus());

            groupNotification.getChildren().add(np);
            np.showScore(groupNotification.getChildren());
        }

        refreshBrick(downData.getViewData());
        refreshNextBrick(downData.getViewData());
        gamePanel.requestFocus();
    }

    /* =================== 事件监听 / 绑定 HUD =================== */

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreLabel != null && scoreProperty != null) {
            scoreLabel.textProperty().bind(scoreProperty.asString());
        }
    }

    // 把“总消行数”显示到右侧 linesLabel，并根据行数自动加速下落（每 5 行升一级）
    public void bindLines(IntegerProperty linesProperty) {
        if (linesLabel != null && linesProperty != null) {

            linesLabel.textProperty().bind(linesProperty.asString());

            linesProperty.addListener((obs, oldVal, newVal) -> {
                if (timeLine != null) {
                    int lines = newVal.intValue();
                    int level = lines / 5;
                    double rate = 1.0 + level * 0.2; // 每级提高 20% 下落速度
                    timeLine.setRate(rate);
                }
            });
        }
    }

    // 历史最高分绑定
    public void bindHighScore(IntegerProperty highScoreProperty) {
        if (highScoreLabel != null && highScoreProperty != null) {
            highScoreLabel.textProperty().bind(highScoreProperty.asString());
        }
    }

    /* =================== 游戏状态控制 =================== */

    public void gameOver() {
        if (timeLine != null) {
            timeLine.stop();
        }
        gameOverPanel.setVisible(true);
        isGameOver.set(true);
    }

    public void newGame(ActionEvent e) {
        if (timeLine != null) {
            timeLine.stop();
        }
        gameOverPanel.setVisible(false);

        eventListener.createNewGame();

        gamePanel.requestFocus();
        if (timeLine != null) {
            timeLine.play();
        }
        isPause.set(false);
        isGameOver.set(false);

        pauseButton.setText("Pause");
    }

    public void pauseGame(ActionEvent e) {

        if (timeLine == null) return;

        if (isPause.get()) {
            isPause.set(false);
            timeLine.play();
            pauseButton.setText("Pause");
        } else {
            isPause.set(true);
            timeLine.pause();
            pauseButton.setText("Resume");
        }

        gamePanel.requestFocus();
    }
}