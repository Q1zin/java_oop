package org.openjfx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLControllerEditStruct {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private CheckBox checkboxNotNull;

    @FXML
    private CheckBox checkboxUnique;

    @FXML
    private TextField fieldName;

    @FXML
    private ComboBox<String> fieldType;

    private TableRowData currentRowData;
    private Runnable onSaveCallback;

    public void setData(TableRowData data) {
        this.currentRowData = data;
        fieldName.setText(data.getName());
        fieldType.setValue(data.getType());
        checkboxNotNull.setSelected(data.isNotNull());
        checkboxUnique.setSelected(data.isUnique());
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    void initialize() {
        fieldType.getItems().addAll("integer", "string", "date", "boolean", "float", "[]strings");

        btnSave.setOnAction(e -> {
            currentRowData.setName(fieldName.getText());
            currentRowData.setType(fieldType.getValue());
            currentRowData.setNotNull(checkboxNotNull.isSelected());
            currentRowData.setUnique(checkboxUnique.isSelected());

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            ((Stage) btnSave.getScene().getWindow()).close();
        });

        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());
    }
}