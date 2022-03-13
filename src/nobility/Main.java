package nobility;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nobility.model.Model;
import nobility.tools.Alerter;

import java.lang.reflect.Constructor;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        model.setMainStage(primaryStage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fx/home.fxml"));
        loader.setControllerFactory((Class<?> controllerType) -> {
            try {
                for (Constructor<?> con : controllerType.getConstructors()) {
                    if (con.getParameterCount() == 1 && con.getParameterTypes()[0] == Model.class) {
                        return con.newInstance(model);
                    }
                }
                return controllerType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                Alerter.showError("Failed to load main controller. Error: " + e.getLocalizedMessage());
                e.printStackTrace(System.err);
                System.exit(-1);
                return null;
            }
        });
        Parent root = loader.load();
        int width = 900;
        int height = 700;
        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.setTitle(Model.APP_NAME);
        primaryStage.setResizable(true);
        primaryStage.getIcons().add(new Image(Model.ICON));
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((screenBounds.getWidth() - width) / 2);
        primaryStage.setY((screenBounds.getHeight() - height) / 2);
    }

    public static void main(String[] args) {
        launch(args);
    }


}
