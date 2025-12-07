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

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 32;
    private static final int PREVIEW_BRICK_SIZE = 20;
    private static final int BOARD_HIDDEN_ROWS = 2;
    private static final int FALL_INTERVAL_MS = 400;
    private static final int BRICK_CORNER_ARC = 9;

    // 砖块颜色表（保持你现在的颜色）
    private static final Paint[] BRICK_COLORS = {
            Color.TRANSPARENT,
            Color.AQUA,
            Color.BLUEVIOLET,
            Color.LIMEGREEN,
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

    // Score 显示
    @FXML private Label scoreLabel;

    // 总消除行数显示
    @FXML private Label linesLabel;

    // ⭐ 新增：等级显示（在 FXML 里加 fx:id="levelLabel"）
    @FXML private Label levelLabel;

    // 预览“下一个方块”的小网格
    @FXML private GridPane nextBrickPanel;

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

        // 初始化等级文字，防止一开始是空的
        if (levelLabel != null) {
            levelLabel.setText("1");
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {

        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int i = BOARD_HIDDEN_ROWS; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {

                Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rect.setFill(Color.TRANSPARENT);

                displayMatrix[i][j] = rect;
                gamePanel.add(rect, j, i - BOARD_HIDDEN_ROWS);
            }
        }

        brickPanel.toFront();

        int[][] brickData = brick.getBrickData();
        rectangles = new Rectangle[brickData.length][brickData[0].length];

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {

                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(getFillColor(brickData[i][j]));

                rectangles[i][j] = r;
                brickPanel.add(r, j, i);
            }
        }

        updateBrickPanelPosition(brick);

        refreshNextBrick(brick);

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(FALL_INTERVAL_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        pauseButton.setText("Pause");
    }

    private Paint getFillColor(int id) {
        if (id >= 0 && id < BRICK_COLORS.length) return BRICK_COLORS[id];
        return Color.WHITE;
    }

    private void updateBrickPanelPosition(ViewData brick) {
        double w = BRICK_SIZE + brickPanel.getHgap();
        double h = BRICK_SIZE + brickPanel.getVgap();

        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * w);
        brickPanel.setLayoutY(gamePanel.getLayoutY() + (brick.getyPosition() - BOARD_HIDDEN_ROWS) * h);
    }

    private void refreshBrick(ViewData brick) {
        if (!isPause.get()) {

            brickPanel.toFront();
            updateBrickPanelPosition(brick);

            int[][] data = brick.getBrickData();

            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    setRectangleData(data[i][j], rectangles[i][j]);
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

    private void setRectangleData(int color, Rectangle r) {
        r.setFill(getFillColor(color));
        r.setArcHeight(BRICK_CORNER_ARC);
        r.setArcWidth(BRICK_CORNER_ARC);
    }

    private void moveDown(MoveEvent event) {

        if (!isPause.get()) {

            DownData downData = eventListener.onDownEvent(event);

            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {

                NotificationPanel np = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(np);
                np.showScore(groupNotification.getChildren());
            }

            refreshBrick(downData.getViewData());
            refreshNextBrick(downData.getViewData());
        }

        gamePanel.requestFocus();
    }

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

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty scoreProperty) {
        scoreLabel.textProperty().bind(scoreProperty.asString());
    }

    /**
     * 显示“总消行数”，并且：
     *  - 每 5 行提升一级
     *  - 同时更新等级 Label 和 下落速度
     */
    public void bindLines(IntegerProperty linesProperty) {
        if (linesLabel != null && linesProperty != null) {

            // 显示行数
            linesLabel.textProperty().bind(linesProperty.asString());

            // 监听行数 → 更新等级 和 下落速度
            linesProperty.addListener((obs, oldVal, newVal) -> {
                if (timeLine != null) {
                    int lines = newVal.intValue();

                    // ★ 每 5 行提升一级；最少是 1 级
                    int level = lines / 5 + 1;

                    // 更新等级文字
                    if (levelLabel != null) {
                        levelLabel.setText(String.valueOf(level));
                    }

                    // 每级快 20%（你可以自己改系数）
                    double rate = 1.0 + (level - 1) * 0.2;
                    timeLine.setRate(rate);
                }
            });
        }
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.set(true);
    }

    public void newGame(ActionEvent e) {
        timeLine.stop();
        gameOverPanel.setVisible(false);

        eventListener.createNewGame();

        gamePanel.requestFocus();
        timeLine.play();
        isPause.set(false);
        isGameOver.set(false);

        pauseButton.setText("Pause");

        // 新游戏时等级恢复成 1
        if (levelLabel != null) {
            levelLabel.setText("1");
        }
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

    public void refreshNextBrick(ViewData viewData) {

        if (nextBrickPanel == null || viewData == null) return;

        nextBrickPanel.getChildren().clear();

        int[][] data = viewData.getNextBrickData();
        if (data == null) return;

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