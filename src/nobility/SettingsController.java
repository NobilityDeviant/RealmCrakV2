package nobility;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import nobility.proxy.AlertBox;
import nobility.proxy.components.ProxySettings;
import nobility.proxy.components.UserSettings;
import nobility.proxy.components.entities.ProxyAnonymity;

import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Settings.fxml
 */
@SuppressWarnings("all")
public class SettingsController implements Initializable {

    @FXML
    private Button button_save;

    @FXML
    private TextField field_threads;

    @FXML
    private TextField field_timeout;

    @FXML
    private ComboBox<String> combo_type;

    @FXML
    private ColorPicker color_elite;

    @FXML
    private ColorPicker color_anonymous;

    @FXML
    private ColorPicker color_transparent;

    @FXML
    private CheckBox auto_remove_check, auto_check_type;

    private final UserSettings settings = ProxySettings.getConfig();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //populate
        combo_type.getItems().add("HTTP");
        combo_type.getItems().add("SOCKS");

        // get current settings
        if (settings != null) {
            combo_type.getSelectionModel().select(settings.getProxyType().toString());

            field_timeout.setText(String.valueOf(settings.getTimeout()));
            field_threads.setText(String.valueOf(settings.getThreads()));
            for (Pair<ProxyAnonymity, String> p : settings.getColorScheme()) {
                switch (p.getKey()) {
                    case ELITE:
                        color_elite.setValue(Color.web(p.getValue()));
                        break;
                    case ANONYMOUS:
                        color_anonymous.setValue(Color.web(p.getValue()));
                        break;
                    case TRANSPARENT:
                        color_transparent.setValue(Color.web(p.getValue()));
                        break;
                }
            }

            combo_type.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                button_save.setDisable(!settingsChanged());
            });

            field_timeout.textProperty().addListener((observable, oldValue, newValue) -> integerTextField(field_timeout, oldValue, newValue));
            field_threads.textProperty().addListener((observable, oldValue, newValue) -> integerTextField(field_threads, oldValue, newValue));

            button_save.setDisable(true); // disable save button by default
            auto_remove_check.setSelected(settings.getAutoRemoveDuplicates());
            auto_remove_check.selectedProperty().addListener((observable, oldValue, newValue) -> {
                button_save.setDisable(!settingsChanged());
            });
            auto_check_type.setSelected(settings.isAutoCheckType());
            auto_check_type.selectedProperty().addListener((observable, oldValue, newValue) -> {
                button_save.setDisable(!settingsChanged());
            });
        } else {
            AlertBox.show(Alert.AlertType.INFORMATION, "Settings are corrupted!",
                    "Restart the application before using it.");
        }
    }

    /**
     * Main button save handle
     */
    @FXML
    private void button_save_action() {
        Proxy.Type type = Proxy.Type.valueOf(combo_type.getSelectionModel().getSelectedItem());
        int threads = Integer.parseInt(field_threads.getText());
        int timeout = Integer.parseInt(field_timeout.getText());
        boolean autoCheck = auto_remove_check.isSelected();
        boolean autoCheckType = auto_check_type.isSelected();

        if (timeout < 500) { // restriction due to timeout value being too low will result
            // in null value for UserSettings.getIp() which will prevent application from working
            AlertBox.show(Alert.AlertType.ERROR, "Timeout Too Low",
                    "The minimum accepted timeout value for requests is 500 milliseconds.");
        } else {
            if (settings != null) {
                settings.setThreads(threads)
                        .setTimeout(timeout)
                        .setProxyType(type)
                        .setAutoCheckType(autoCheckType)
                        .setAutoRemoveDuplicates(autoCheck);
                if (ProxySettings.saveConfig(settings)) {
                    AlertBox.show(Alert.AlertType.INFORMATION, "Changes Saved",
                            "The changes made have been saved to the disk!");
                    button_save.setDisable(true);
                } else {
                    AlertBox.show(Alert.AlertType.ERROR, "Save Failed",
                            "Unable to save the configuration file onto the disk!");
                }
            } else {
                AlertBox.show(Alert.AlertType.INFORMATION, "Settings are corrupted",
                        "Restart the application before using it.");
            }
        }
    }

    @FXML
    private void button_save_color_action() {
        List<Pair<ProxyAnonymity, String>> newScheme = new ArrayList<>();
        newScheme.add(new Pair<>(ProxyAnonymity.ELITE,
                "#" + Integer.toHexString(color_elite.getValue().hashCode()).substring(0,6)));
        newScheme.add(new Pair<>(ProxyAnonymity.ANONYMOUS,
                "#" + Integer.toHexString(color_anonymous.getValue().hashCode()).substring(0,6)));
        newScheme.add(new Pair<>(ProxyAnonymity.TRANSPARENT,
                "#" + Integer.toHexString(color_transparent.getValue().hashCode()).substring(0,6)));
        if (settings != null) {
            settings.setColorScheme(newScheme);
            if (ProxySettings.saveConfig(settings)) {
                AlertBox.show(Alert.AlertType.INFORMATION, "Changes Saved",
                        "The changes made have been saved to the disk!");
                button_save.setDisable(true);
            } else {
                AlertBox.show(Alert.AlertType.ERROR, "Save Failed",
                        "Unable to save the configuration file onto the disk!");
            }
        } else {
            AlertBox.show(Alert.AlertType.INFORMATION, "Settings are corrupted",
                    "Restart the application before using it.");
        }
    }

    /**
     * Determines whether or not if any proxy setting value has been changed
     * @return Boolean - Settings have been changed
     */
    private boolean settingsChanged() {
        return (
                (!(settings.getProxyType().toString().equals(combo_type.getSelectionModel().getSelectedItem()))) ||
                        (!(String.valueOf(settings.getTimeout()).equals(field_timeout.getText()))) ||
                        (!(String.valueOf(settings.getThreads()).equals(field_threads.getText()))) ||
                        (settings.getAutoRemoveDuplicates() != auto_remove_check.isSelected()) ||
                        (settings.isAutoCheckType() != auto_check_type.isSelected())
        );
    }

    /**
     * Makes sure the user is only able enter in a number on the textfield
     * @param field - TextField
     * @param oldValue - String the value before a new character is entered
     * @param newValue - String the value after the new character is entered
     */
    private void integerTextField(TextField field, String oldValue, String newValue) {
        try {
            if(field.getText().length() > 0 ) {
                int value = Integer.parseInt(newValue);
                if(field.getText().length() == 1) { // first numbered entered
                    if(value == 0) { // prevent from being a zero
                        field.setText(oldValue);
                    }
                }
            }
        } catch (NumberFormatException e) {
            field.setText(oldValue);
        }
        button_save.setDisable(!settingsChanged()); // only enable save button if values have changed
    }
}
