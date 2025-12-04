// 图形界面的控制器，主要负责把游戏逻辑的结果显示出来。
// 包括绘制棋盘、当前方块、处理键盘输入、显示分数、以及控制游戏的开始/暂停/结束。
// 本类和 GameController 配合，一个负责显示，一个负责游戏逻辑。

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
import javafx.scene.control.Label;     // ★★★ 新增：用于绑定分数
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

    private static final int BRICK_SIZE = 32;
    private static final int BOARD_HIDDEN_ROWS = 2;
    private static final int FALL_INTERVAL_MS = 400;
    private static final int BRICK_CORNER_ARC = 9;

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

    // ★★★ 新增：分数 label，从 FXML 绑定 fx:id="scoreLabel"
    @FXML
    private Label scoreLabel;

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

                if (keyEvent.getCode() == KeyCode.P) {
                    pauseGame(null);
                    keyEvent.consume();
                }
            }
        });

        gameOverPanel.setVisible(false);
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {

        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = BOARD_HIDDEN_ROWS; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - BOARD_HIDDEN_ROWS);
            }
        }

        brickPanel.toFront();

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

        updateBrickPanelPosition(brick);

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

            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    // ★★★ 只修改了这里：绑定分数到 scoreLabel
    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreLabel != null && scoreProperty != null) {
            scoreLabel.textProperty().bind(scoreProperty.asString());
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

    public void pauseGame(ActionEvent actionEvent) {
        if (timeLine == null) return;

        if (isPause.get()) {
            isPause.set(false);
            timeLine.play();
            pauseButton.setText("⏸");
        } else {
            isPause.set(true);
            timeLine.pause();
            pauseButton.setText("▶");
        }

        gamePanel.requestFocus();
    }
}