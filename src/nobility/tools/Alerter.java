package nobility.tools;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nobility.model.Model;

import java.awt.*;
import java.net.URI;

public class Alerter {



    public static void showConfirm(String title, String message, Runnable runnable) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.CANCEL, ButtonType.YES);
        alert.setTitle(title);
        alert.getDialogPane().getStylesheets().add(Model.DIALOG_CSS);
        alert.getDialogPane().getStyleClass().add("dialog");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Model.ICON));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.YES)) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
    }

    public static void showConfirm(String message, Runnable runnable) {
        showConfirm("", message, runnable);
    }

    public static void showAlert(Alert.AlertType alertType, String header, String content) {
        Alert alert = new Alert(alertType, content, ButtonType.OK);
        alert.getDialogPane().getStylesheets().add(Model.DIALOG_CSS);
        System.out.println(Model.DIALOG_CSS);
        alert.getDialogPane().getStyleClass().add("dialog");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Model.ICON));
        alert.setHeaderText(header);
        alert.setTitle(Model.APP_NAME + " Alert");
        alert.show();
    }

    public static void showMessage(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.getDialogPane().getStylesheets().add(Model.DIALOG_CSS);
            alert.getDialogPane().getStyleClass().add("dialog");
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Model.ICON));
            alert.setHeaderText("");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showError(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.getDialogPane().getStylesheets().add(Model.DIALOG_CSS);
            alert.getDialogPane().getStyleClass().add("dialog");
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Model.ICON));
            alert.setHeaderText("");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

}
