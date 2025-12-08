package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import javafx.event.ActionEvent;

public class MainMenuController {

    @FXML
    private void onStartGame(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("gameLayout.fxml"));
        Parent root = loader.load();
        GuiController gui = loader.getController();

        // 普通模式：bombMode = false
        new GameController(gui, false);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void onStartBombMode(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("gameLayout.fxml"));
        Parent root = loader.load();
        GuiController gui = loader.getController();

        // 炸弹模式：bombMode = true
        new GameController(gui, true);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void onExit(ActionEvent event) {
        Platform.exit();
    }
}