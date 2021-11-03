package nobility;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nobility.model.Model;

import java.lang.reflect.Constructor;

public class Main extends Application {

    private double x, y;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model();
        model.setMainStage(primaryStage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
        loader.setControllerFactory((Class<?> controllerType) -> {
            try {
                for (Constructor<?> con : controllerType.getConstructors()) {
                    if (con.getParameterCount() == 1 && con.getParameterTypes()[0] == Model.class) {
                        return con.newInstance(model);
                    }
                }
                return controllerType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("Failed to load main controller. Error: " + e.getMessage());
                e.printStackTrace(System.err);
                System.exit(-1);
                return null;
            }
        });
        Parent root = loader.load();
        primaryStage.setY(0);
        primaryStage.setX(0);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(Model.APP_NAME);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(String.valueOf(getClass().getResource("icon.png"))));
        primaryStage.sizeToScene();
        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - x);
            primaryStage.setY(event.getScreenY() - y);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }


}
