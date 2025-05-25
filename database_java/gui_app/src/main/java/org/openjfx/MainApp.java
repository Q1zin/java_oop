package org.openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.openjfx.services.FileHandler;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FileHandler fileHandler = new FileHandler();
        String dbPath = fileHandler.getDbPath();

        if (dbPath != null) {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("start_scene.fxml")));
            stage.setScene(new Scene(root, 1280, 720));
        } else {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("init_scene.fxml")));
            stage.setScene(new Scene(root));
        }

        stage.setTitle("VovixBD - База данных нового поколения! =)");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
