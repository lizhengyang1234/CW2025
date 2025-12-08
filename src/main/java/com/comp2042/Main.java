package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // 正确加载主菜单 FXML，确保路径来自 src/main/resources
        URL fxml = getClass().getClassLoader().getResource("mainMenu.fxml");
        if (fxml == null) {
            throw new IllegalStateException("❌ mainMenu.fxml not found in src/main/resources/");
        }

        Parent root = FXMLLoader.load(fxml);

        primaryStage.setTitle("Tetris");

        // 主菜单窗口大小：900 × 650
        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);

        // 防止窗口被缩得太小（可选）
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(600);

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}