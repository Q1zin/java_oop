package org.openjfx.services;

import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;

public class NotificationManager {
    private final VBox messageBlock;
    private static final Logger logger = LogManager.getLogger(NotificationManager.class);

    public NotificationManager(VBox messageBlock) {
        this.messageBlock = messageBlock;
    }

    public void showError(String message) {
        showMessage("error.fxml", "#errorBlockText", message, 7);
    }

    public void showWarning(String message) {
        showMessage("warning.fxml", "#warningBlockText", message, 5);
    }

    public void showNotify(String message) {
        showMessage("notify.fxml", "#notifyBlockText", message, 3);
    }

    private void showMessage(String fxml, String textId, String message, int duration) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            AnchorPane newBlock = loader.load();
            ((Text) newBlock.lookup(textId)).setText(message);
            messageBlock.getChildren().add(newBlock);

            PauseTransition delay = new PauseTransition(Duration.seconds(duration));
            delay.setOnFinished(event -> messageBlock.getChildren().remove(newBlock));
            delay.play();
        } catch (Exception e) {
            logger.error("Произошла ошибка при выводе окна с ошибкой, ошибка из окна: {}. Ошибка вывода: {}. StackTrace: {}", message, e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }
}