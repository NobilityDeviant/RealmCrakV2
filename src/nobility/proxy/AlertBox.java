package nobility.proxy;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nobility.Main;
import nobility.proxy.components.ProxySettings;

/**
 * Creates an AlertBox
 */
public class AlertBox {

    /**
     * Displays an AlertBox
     * @param alertType - javafx.scene.control.Alert.AlertType
     * @param header - Heading of the Alert
     * @param content - Content of the Alert
     */
    public static void show(Alert.AlertType alertType, String header, String content) {
        Alert alert = new Alert(alertType, content, ButtonType.OK);
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
                new Image(Main.class.getResourceAsStream("proxyicon.png"))
        );
        alert.setHeaderText(header);
        alert.setTitle(ProxySettings.getApplicationName());
        alert.show();
    }

    public static boolean showImportProxy(Alert.AlertType alertType, String header, String content) {
        Alert alert = new Alert(alertType, content, ButtonType.YES, ButtonType.NO);
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
                new Image(Main.class.getResourceAsStream("proxyicon.png"))
        );
        alert.setHeaderText(header);
        alert.setTitle(ProxySettings.getApplicationName());
        alert.showAndWait();
        return alert.getResult() == ButtonType.YES;
    }
}
