package com.comp2042;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 主菜单控制器：
 * - 点击 Start Game → 切换到游戏界面 gameLayout.fxml 并创建 GameController
 * - 点击 Exit       → 退出程序
 */
public class MainMenuController {

    @FXML
    private void onStartGame(ActionEvent event) {
        try {
            // 1. 加载游戏界面 FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getClassLoader().getResource("gameLayout.fxml"));
            Parent gameRoot = loader.load();

            // 2. 拿到 GUI 控制器
            GuiController guiController = loader.getController();

            // 3. 创建 GameController，把 guiController 传进去
            new GameController(guiController);

            // 4. 用当前窗口切换 Scene 到游戏界面
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(gameRoot));
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onExit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();
        stage.close();
    }
}