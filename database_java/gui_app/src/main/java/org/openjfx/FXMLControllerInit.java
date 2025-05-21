package org.openjfx;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.openjfx.services.FileHandler;

public class FXMLControllerInit {
    public static String PATH_SELECT;
    FileHandler fileHandler = new FileHandler();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnGo, btnSelectPath;

    @FXML
    private Text textPathDefault, textSelectPath;

    @FXML
    void initialize() {
        File dbDirectory = fileHandler.getAppDataDirectory();
        PATH_SELECT = dbDirectory.getAbsolutePath();
        textPathDefault.setText(PATH_SELECT);

        btnGo.setOnMouseClicked(event -> {
            File settingsFile = new File(fileHandler.getAppDataDirectory(), "settings.properties");
            fileHandler.saveDbPath(settingsFile, PATH_SELECT);

            try {
                onStartButtonClick();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        btnSelectPath.setOnMouseClicked(event -> {
            File databaseDirectory = fileHandler.chooseDatabaseDirectory("Выберите директорию для базы данных");

            if (databaseDirectory != null) {
                PATH_SELECT = databaseDirectory.getAbsolutePath();
                textSelectPath.setText(PATH_SELECT);
            }
        });
    }

    private void onStartButtonClick() throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("start_scene.fxml")));

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root, 1280, 720));
        newStage.setResizable(false);
        newStage.setTitle("VovixBD - База данных нового поколения! =)");
        newStage.show();

        Stage currentStage = (Stage) btnGo.getScene().getWindow();
        currentStage.close();
    }

}
