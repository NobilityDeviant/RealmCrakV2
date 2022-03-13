package nobility.proxy;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import nobility.model.Model;
import nobility.tools.Alerter;

public class Window {

    /**
     * Displays a Window
     * @param title - Title of the Window
     * @param fxmlLoader - FXMLLoader content
     */
    public static void show(String title, FXMLLoader fxmlLoader) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle(title);
            stage.getIcons().add(new Image(Model.ICON));
            stage.setResizable(true);
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
        } catch (Exception e) {
            Alerter.showError("Failed to open window. Error: " + e.getLocalizedMessage());
        }
    }
}
