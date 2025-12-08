package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载主菜单
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("mainMenu.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Tetris");

        // ★ 设置主菜单窗口大小：900×650（更宽、更现代）
        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);

        // ★ 防止被缩到太小（可选）
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(600);

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}