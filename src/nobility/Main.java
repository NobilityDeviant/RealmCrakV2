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
        /*try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://www.realmeye.com/player/"
                    + "ArtwoBR").openConnection();
            con.setRequestMethod("GET");
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
            con.setConnectTimeout(10_000);
            con.setReadTimeout(10_000);
            con.setInstanceFollowRedirects(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            TextStringBuilder stringBuilder = new TextStringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine).append("\n");
            }
            in.close();
            if (stringBuilder.contains("Sorry, but we either:")) {
                System.out.println("Invalid user");
                System.exit(-1);
                return;
            }
            String s = stringBuilder.toString();
            TextStringBuilder results = new TextStringBuilder();
            String firstSeenKey = "<td>First seen</td><td>";
            if (s.contains(firstSeenKey)) {
                results.append("First Seen: ").append(s.substring(s.indexOf(firstSeenKey) + firstSeenKey.length(), s.indexOf("</td></tr><tr><td>Last seen")));
            }
            String createdKey = "Created</td><td>";
            if (s.contains(createdKey)) {
                results.append("Created: ").append(s.substring(s.indexOf(createdKey) + createdKey.length(), s.indexOf("</td></tr><tr><td>Last seen")));
            }
            String lastSeenKey = "Last seen</td><td><span class=\"timeago\" title=\"";
            if (s.contains(lastSeenKey)) {
                String lastSeenResult = s.substring(s.indexOf(lastSeenKey) + lastSeenKey.length(),
                        s.indexOf("</td></tr></table></div><div class=\"col-md-7\"><div class=\"well description\""));
                results.appendNewLine();
                String spanEnd = "</span>";
                results.append("Last Seen: ").append(lastSeenResult.substring(lastSeenResult.indexOf("\">") + 2,
                        lastSeenResult.indexOf(spanEnd))).append(" ").append(lastSeenResult
                        .substring(lastSeenResult.indexOf(spanEnd) + spanEnd.length()).trim());
            }
            System.out.println(stringBuilder);
            System.out.println(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);*/
        launch(args);
    }


}
