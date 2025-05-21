package org.openjfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.openjfx.services.FileHandler;

import java.io.File;

public class FXMLControllerSettings {
    public static String PATH_SELECT;
    private String newSavePath = "";
    FileHandler fileHandler = new FileHandler();

    @FXML
    private Text afterPath, beforePath;

    @FXML
    private Button btnCancel, btnSelectPath, btnSave, btnReset;

    public void updateText(String newPath) {
        beforePath.setText(newPath);
        PATH_SELECT = newPath;
    }

    public String getSelectedPath() {
        return PATH_SELECT;
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSelectPath(String newPath) {
        afterPath.setText(newPath);
        newSavePath = newPath;
    }

    @FXML
    private void onSave() {
        PATH_SELECT = newSavePath;
        onCancel();
    }

    void initialize() {
        btnSave.setOnMouseClicked(event -> {
            onSave();
        });

        btnCancel.setOnMouseClicked(event -> {
            onCancel();
        });

        btnSelectPath.setOnMouseClicked(event -> {
            File databaseDirectory = fileHandler.chooseDatabaseDirectory("Выберите директорию для базы данных");

            if (databaseDirectory != null) {
                onSelectPath(databaseDirectory.getAbsolutePath());
            }
        });

        btnReset.setOnMouseClicked(event -> {
            File dbDirectory = fileHandler.getAppDataDirectory();
            newSavePath = dbDirectory.getAbsolutePath();
            onSelectPath(dbDirectory.getAbsolutePath());
            onSave();
        });
    }
}
