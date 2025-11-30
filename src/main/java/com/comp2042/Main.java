// 程序的入口类。主要负责加载 FXML 布局，创建 GUI 控制器，
// 然后启动主窗口并把控制权交给 GameController。
// 基本上就是把界面搭好，然后让游戏逻辑接管运行。
//
// Main entry point of the application. Loads the FXML layout,
// creates the GUI controller, sets up the window, and hands
// control over to the GameController to run the game.

package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();
        GuiController c = fxmlLoader.getController();

        primaryStage.setTitle("TetrisJFX");
// 宽 420，高 780
        Scene scene = new Scene(root, 420, 780);
        primaryStage.setScene(scene);
        primaryStage.show();
        new GameController(c);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
