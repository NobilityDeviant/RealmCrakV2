package nobility;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import nobility.model.Model;
import nobility.save.Defaults;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmController implements Initializable {

    @FXML private TextArea updateLog;
    @FXML private Button btnUpdate, btnCancel;
    private boolean required;
    private Stage stage;
    private final boolean upToDate;
    private final Model model;

    public ConfirmController(Model model) {
        this.model = model;
        upToDate = model.getUpdateManager().isLatestVersion()[0];
    }

    @FXML public void update() {
        model.getUpdateManager().openUpdateLink(required);
    }

    @FXML public void cancel() {
        if (required) {
            model.showError("You must update your client to continue. Shutting down...");
            System.exit(0);
        } else {
            stage.close();
            if (!upToDate) {
                System.out.println("Update has been denied. You will no longer receive a notification about it until the next update.");
                model.save().setBoolean(Defaults.DENIEDUPDATE, true);
                model.saveSettings();
            }
        }
    }

    public void close() {
        if (required) {
            model.showError("You must update your client to continue. Shutting down...");
            System.exit(0);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    void setStage(Stage stage, boolean required) {
        this.stage = stage;
        this.required = required;
        if (!required) {
            btnCancel.setText("Cancel");
        }
        StringBuilder sb = new StringBuilder();
        try {
            URL url1 = new URL("https://www.dropbox.com/s/c2riqp92yr3gkgy/updates.txt?dl=1");
            BufferedReader in = new BufferedReader(new InputStreamReader(url1.openStream()));
            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s).append("\n");
            }
            in.close();
            updateLog.setText(sb.toString());
        } catch (Exception e) {
            updateLog.setText("Failed to receive update log...");
        }
        if (upToDate) {
            btnUpdate.setText("Updated");
            btnUpdate.setDisable(true);
        }
    }

}