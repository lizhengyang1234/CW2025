package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.Point;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 32;
    private static final int PREVIEW_BRICK_SIZE = 20;
    private static final int BOARD_HIDDEN_ROWS = 2;
    private static final int FALL_INTERVAL_MS = 400;
    private static final int BRICK_CORNER_ARC = 9;

    private static final Paint[] BRICK_COLORS = {
            Color.TRANSPARENT,
            Color.AQUA,
            Color.BLUEVIOLET,
            Color.LIMEGREEN,
            Color.YELLOW,
            Color.RED,
            Color.BEIGE,
            Color.BURLYWOOD,
            Color.BLACK // bomb
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
    @FXML private Label levelLabel;   // 对应 FXML 里的 fx:id="levelLabel"

    @FXML private GridPane nextBrickPanel;

    private InputEventListener eventListener;
    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);

    private BoardView boardView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Font.loadFont(
                getClass().getClassLoader().getResource("digital.ttf").toExternalForm(),
                38
        );

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

        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {

                if (!isPause.get() && !isGameOver.get()) {

                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        handleDownResult(new DownData(null,
                                eventListener.onLeftEvent(
                                        new MoveEvent(EventType.LEFT, EventSource.USER))));
                        keyEvent.consume();
                    }

                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        handleDownResult(new DownData(null,
                                eventListener.onRightEvent(
                                        new MoveEvent(EventType.RIGHT, EventSource.USER))));
                        keyEvent.consume();
                    }

                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        handleDownResult(new DownData(null,
                                eventListener.onRotateEvent(
                                        new MoveEvent(EventType.ROTATE, EventSource.USER))));
                        keyEvent.consume();
                    }

                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }

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

    public void initGameView(int[][] boardMatrix, ViewData brick) {

        boardView.initGameView(boardMatrix, brick);

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(FALL_INTERVAL_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        pauseButton.setText("Pause");
    }

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

    public void playExplosionAnimation(List<Point> cells) {
        if (boardView != null && cells != null && !cells.isEmpty()) {
            boardView.playExplosionAnimation(cells);
        }
    }

    // ====================== 下落 & 硬降 ======================

    private void moveDown(MoveEvent event) {
        if (!isPause.get()) {
            DownData downData = eventListener.onDownEvent(event);
            handleDownResult(downData);
        }
        gamePanel.requestFocus();
    }

    private void hardDrop() {
        if (isPause.get() || isGameOver.get()) return;

        DownData downData =
                eventListener.onDownEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
        handleDownResult(downData);
        gamePanel.requestFocus();
    }

    /**
     * 统一处理一次“下落/硬降”的结果：
     *  - 如果有消行，显示加分动画
     *  - 刷新当前方块和 next 预览
     */
    private void handleDownResult(DownData downData) {
        if (downData == null) return;

        if (downData.getClearRow() != null &&
                downData.getClearRow().getLinesRemoved() > 0) {

            NotificationPanel np =
                    new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
            groupNotification.getChildren().add(np);
            np.showScore(groupNotification.getChildren());
        }

        refreshBrick(downData.getViewData());
        refreshNextBrick(downData.getViewData());
    }

    // ====================== 绑定 HUD ======================

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreLabel != null && scoreProperty != null) {
            scoreLabel.textProperty().bind(scoreProperty.asString());
        }
    }

    public void bindLines(IntegerProperty linesProperty) {
        if (linesLabel != null && linesProperty != null) {

            linesLabel.textProperty().bind(linesProperty.asString());

            linesProperty.addListener((obs, oldVal, newVal) -> {
                if (timeLine != null) {
                    int lines = newVal.intValue();
                    int levelIndex = lines / 5;
                    double rate = 1.0 + levelIndex * 0.2;
                    timeLine.setRate(rate);

                    // 更新 LEVEL 显示：从 1 开始
                    if (levelLabel != null) {
                        int level = levelIndex + 1;
                        levelLabel.setText(String.valueOf(level));
                    }
                }
            });
        }
    }

    public void bindHighScore(IntegerProperty highScoreProperty) {
        if (highScoreLabel != null && highScoreProperty != null) {
            highScoreLabel.textProperty().bind(highScoreProperty.asString());
        }
    }

    // ====================== 游戏状态 ======================

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

    /**
     * 返回主菜单按钮
     */
    public void onReturnToMenu(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    GuiController.class.getResource("/mainMenu.fxml")
            );

            Parent root = loader.load();
            Stage stage = (Stage) gamePanel.getScene().getWindow();

            Scene scene = new Scene(root, 900, 650);
            stage.setScene(scene);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}