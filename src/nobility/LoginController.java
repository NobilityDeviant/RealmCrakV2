package nobility;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import nobility.model.Model;
import nobility.save.DatabaseMessages;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    //@FXML public Button loginButton, resetButton;
    @FXML private TextField input;
    private Stage stage;
    private int retries = 0, resets = 0;
    private final File keyFile = new File( "./key.txt");
    private final Model model;

    public LoginController(Model model) {
        this.model = model;
    }

    private void saveKey(String key) {
        try {
            Files.write(keyFile.toPath(), Collections.singletonList(key));
        } catch (Exception e) {
            System.out.println("Error saving your key. Error: " + e.getMessage());
        }
    }

    private String getKey() {
        String s = null;
        try {
            s = Files.readAllLines(keyFile.toPath()).get(0);
        } catch (Exception ignored) {}
        return s;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String key = getKey();
        if (key != null && !key.isEmpty()) {
            input.setText(key);
        }
        /*input.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (input.getText().isEmpty()) {
                loginButton.setText("Start Free Trial");
                resetButton.setText("Purchase A Key");
            } else {
                if (!loginButton.getText().equals("Login")) {
                    loginButton.setText("Login");
                }
                if (!resetButton.getText().equals("Reset Key UUID")) {
                    resetButton.setText("Reset Key UUID");
                }
            }
        }));*/
    }

    @FXML
    public void login() {
        if (retries >= 5) {
            model.showError("Too many login attempts. Please close & relaunch this program to try again.");
            return;
        }
        if (input.getText().isEmpty()) {
            model.showError("Please enter your key.");
            return;
        }
        String key = input.getText().toUpperCase();
        if (!key.matches("^[A-Za-z0-9]*$")) {
            model.showError("Invalid key type.");
            return;
        }
        if (key.length() >= 12) {
            DatabaseMessages trialId = model.getDatabase().freeTrialInit(key);
            switch (trialId) {
                case SUCCESS:
                    saveKey(key);
                    model.setFreetrialMode(true);
                    model.setSavedKey(key);
                    model.showMessage("Success", "Welcome to RealmCrak! Thank you for trying it!\n" +
                            "Time Left: " + model.getDays() + ":" + model.getHours() + ":" + model.getMinutes() + " (days:hours:minutes).\n"
                            + "When your time is up, you will need to purchase a key to continue using it.\n"
                            + "Please close this window to continue.");
                    model.setLoggedIn(true);
                    model.setupMenuIcon();
                    model.getMainStage().show();
                    System.out.println("Successfully logged in with your free trial. Thank you for trying RealmCrak.");
                    stage.close();
                break;
                case BANNED:
                    retries++;
                    model.showError("This key has been banned. Contact Nobility to learn more.");
                break;
                case TRIALSOVER:
                    retries++;
                    model.showError("Your free trial is up. You can purchase a key at http://nobility.fun");
                break;
                case BADCONNECTION:
                    model.showError("Connection not established. Check your internet and try again.");
                    break;
                case UPDATEFAILED:
                    model.showError("Unknown error. You can view the error in your console. Please try again.");
                    break;
                case INVALIDKEY:
                    retries++;
                    model.showError("Invalid key. Please check for any errors and try again.");
                break;
                case NOUUID:
                    model.showError("Couldn't find your UUID. Read the console for an error.");
                    break;
                case MISMATCHUUID:
                    retries++;
                    model.showError("This key has been registered to a different PC. You can only use it there.");
                break;
                case UUIDONOTHERKEY:
                    retries++;
                    model.showError("Sneaky sneaky... You have already used a trial key before. Share this one with someone. ;)");
                break;
            }
        } else if (key.length() >= 4 && key.length() <= 8) {
            byte id = model.getDatabase().login(key);
            switch (id) {
                case 0:
                    model.setLoggedIn(true);
                    saveKey(key);
                    model.setupMenuIcon();
                    model.getMainStage().show();
                    System.out.println("Successfully logged in with key: " + key);
                    stage.close();
                break;
                case 1:
                    retries++;
                    model.showError("This key has been banned. Contact Nobility to learn more.");
                break;
                case 2:
                    retries++;
                    model.showError("This key has been registered to a different PC. \n" +
                            "There is an option to reset your key at login.");
                break;
                case 3:
                    retries++;
                    model.showError("Invalid key. Please check for any errors and try again.");
                break;
                case 4:
                    model.showError("Connection not established. Check your internet and try again.");
                    break;
                case 5:
                    model.showError("Unknown error. You can view the error in your console. Please try again.");
                    break;
                case 8:
                    model.showError("Couldn't find UUID. Contact Nobility.");
                    break;
            }
        } else {
            model.showError("Invalid key type.");
        }
    }

    @FXML
    public void resetKey() {
        if (input.getText().isEmpty()) {
            model.showError("Please enter your key.");
            return;
        }
        if (resets >= 5) {
            model.showError("Too many reset attempts. Please close & relaunch this program to try again.");
            return;
        }
        String key = input.getText().toUpperCase();
        if (!key.matches("^[A-Za-z0-9]*$")) {
            model.showError("Invalid key type.");
            return;
        }
        if (key.length() >= 12) {
            model.showError("You can't reset trial keys.");
            return;
        }
        if (key.length() >= 4 && key.length() <= 8) {
            byte id = model.getDatabase().resetUUID(key);
            switch (id) {
                case 0:
                    model.showMessage("Success", "Successfully reset the key UUID. You can now log into a new PC.");
                    break;
                case 1:
                    resets++;
                    model.showError("This key is banned. You can't reset it.");
                break;
                case 2:
                    resets++;
                    model.showMessage("No reset needed", "This key has no UUID. You can log in to register one.");
                break;
                case 3:
                    resets++;
                    model.showError("Invalid key. Please check for any errors and try again.");
                break;
                case 4:
                    model.showError("Connection not established. Check your internet and try again.");
                    break;
                case 5:
                    resets++;
                    model.showError("Valid attempt, but failed to update database. Please try again.");
                break;
                case 6:
                    model.showError("You have to wait " + model.getDays() + ":" + model.getHours() + ":" + model.getMinutes()
                        + " (days:hours:minutes) before you can reset this key.");
                    break;
                case 7:
                    resets++;
                    model.showError("Error reading last date from DB.");
                break;
            }
        } else {
            model.showError("Invalid key type.");
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

}
