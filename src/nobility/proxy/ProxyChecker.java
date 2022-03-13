package nobility.proxy;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.util.Pair;
import nobility.Controller;
import nobility.proxy.commands.ExportCommand;
import nobility.proxy.commands.LoadCommand;
import nobility.proxy.commands.ProxyCheckCommand;
import nobility.proxy.components.ProxySettings;
import nobility.proxy.components.UserSettings;
import nobility.proxy.components.entities.Proxy;
import nobility.proxy.components.entities.ProxyAnonymity;
import nobility.proxy.components.entities.ProxyStatus;
import nobility.proxy.events.ProxyCheckerKeyEvent;
import nobility.tools.Alerter;
import nobility.tools.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProxyChecker {

    private final Controller controller;
    private Boolean notifyCompleted;
    private final List<String> proxies = new ArrayList<>();

    public UserSettings getSettings() {
        return ProxySettings.getConfig();
    }

    public ProxyChecker(Controller controller) {
        this.controller = controller;

        new Thread(() -> {
            String ip = "0.0.0.0";
            try {
                ip = getIp();
            } catch (IOException ignored) {}
            if (ip != null && !getSettings().getIp().equals(ip)) {
                ProxySettings.saveConfig(getSettings().setIp(ip));
            }
            String finalIp = ip;
            Platform.runLater(() -> controller.label_ip_address.setText(finalIp));
        }).start();

        notifyCompleted = false;

        controller.table_proxy.setOnKeyPressed(new ProxyCheckerKeyEvent());

        // setup table factory
        controller.column_ip.setCellValueFactory(new PropertyValueFactory<>("Ip"));
        controller.column_port.setCellValueFactory(new PropertyValueFactory<>("Port"));
        controller.column_status.setCellValueFactory(new PropertyValueFactory<>("ProxyStatus"));
        controller.column_anonymity.setCellValueFactory(new PropertyValueFactory<>("ProxyAnonymity"));
        controller.column_country.setCellValueFactory(new PropertyValueFactory<>("Country"));
        controller.column_response_time.setCellValueFactory(new PropertyValueFactory<>("ResponseTime"));
        controller.column_type.setCellValueFactory(new PropertyValueFactory<>("ProxyType"));

        controller.table_proxy.getSortOrder().add(controller.column_status);

        controller.table_proxy.setRowFactory(tp -> new TableRow<Proxy>() {
            @Override
            protected void updateItem(Proxy proxy, boolean empty) {
                super.updateItem(proxy, empty);
                if ((proxy == null) || (proxy.getProxyStatus() == ProxyStatus.DEAD)) {
                    setStyle("");
                } else {
                    ProxyAnonymity anonymity = proxy.getProxyAnonymity();
                    for (Pair<ProxyAnonymity, String> p : ProxySettings.getConfig().getColorScheme()) {
                        if (p.getKey() == anonymity) {
                            setStyle("-fx-background-color: " + p.getValue() + ";");
                        }
                    }
                }
            }
        });

        // manage progress bar and count for working proxies and checked proxies
        controller.table_proxy.getItems().addListener((ListChangeListener<Proxy>) c -> {
            if (!c.getList().isEmpty()) {
                Proxy proxy = c.getList().get(c.getList().size() - 1); // newest added proxy
                controller.label_checked_proxies.setText("Checked Proxies: " + c.getList().size());
                if ((c.getList().size() == proxies.size())) {
                    controller.progressBarProxy.setProgress(0f);
                    controller.proxyStartButton.setDisable(false); // disable check button until all proxies are checked
                    if (!notifyCompleted) {
                        Alerter.showAlert(Alert.AlertType.INFORMATION, "Task Completed",
                                "Proxy checker has finished checking your proxies!");
                        notifyCompleted = true;
                    }
                } else {
                    controller.progressBarProxy.setProgress((float) c.getList().size() / proxies.size());
                }
                if (proxy.getProxyStatus() == ProxyStatus.ALIVE) {
                    controller.table_proxy.getSortOrder().clear();
                    controller.table_proxy.getSortOrder().add(controller.column_status);
                    controller.table_proxy.sort();
                    int current_working = Integer.parseInt(controller.label_working_proxies.getText().split(":")[1].trim());
                    controller.label_working_proxies.setText("Working Proxies: " + (current_working + 1));
                }
            }
        });

        // allow drag and drop of files into the loaded proxies view
        controller.table_proxy.setOnDragOver(event -> {
            if (!ProxyCheckCommand.isRunning()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
        });

        // route the files dropped to the proper load command
        controller.table_proxy.setOnDragDropped(event -> {
            if (event.getDragboard().hasFiles()) {
                List<String> temp = LoadCommand.loadFile(event.getDragboard().getFiles());
                if (temp != null) {
                    proxies.addAll(temp);
                    controller.label_loaded_proxies.setText("Loaded Proxies: " + proxies.size());
                }
            }
        });
    }

    public void loadFiles() {
        if (!ProxyCheckCommand.isRunning()) {
            List<String> temp = LoadCommand.loadFile(null);
            if (temp != null) {
                proxies.addAll(temp);
                controller.label_loaded_proxies.setText("Loaded Proxies: " + proxies.size());
                Toast.makeToast("Loaded " + proxies.size() + " proxies.");
            }
        } else {
            Toast.makeToast("Please wait for the proxy checker to stop.");
        }
    }

    public void removeDuplicates() {
        if (!ProxyCheckCommand.isRunning()) {
            if (proxies.isEmpty()) {
                Toast.makeToast("Please load a new proxy file first.");
                return;
            }
            Set<String> dedupe = new LinkedHashSet<>(proxies);
            if (dedupe.size() == proxies.size()) {
                Toast.makeToast("No proxy duplicates found!");
            } else {
                int found = proxies.size() - dedupe.size();
                proxies.clear();
                proxies.addAll(dedupe);
                controller.label_loaded_proxies.setText("Loaded Proxies: " + proxies.size());
                Toast.makeToast("Successfully removed " + found + " duplicate proxies.");
            }
        } else {
            Toast.makeToast("Please wait for the proxy checker to stop.");
        }
    }

    public void exportAll() {
        if (ProxyCheckCommand.isRunning()) {
            Toast.makeToast("Please wait for the proxy checker to stop.");
            return;
        }
        ExportCommand.saveNoFilter(proxies);
    }

    public void clearProxies() {
        proxies.clear();
        controller.label_loaded_proxies.setText("Loaded Proxies: 0");
    }

    public void start() {
        if (getSettings().getIp().equals("0.0.0.0")) {
            Alerter.showConfirm("Your IP couldn't be resolved. You will be unable to use the proxy checker without this. Would you " +
                    "like to try to receive it again?", () -> {
                try {
                    String ip = getIp();
                    ProxySettings.saveConfig(getSettings().setIp(ip));
                    Platform.runLater(() -> controller.label_ip_address.setText(ip));
                    Alerter.showMessage("IP Resolved", "Successfully received your IP address. You can now use the proxy checker.");
                } catch (IOException e) {
                    Alerter.showAlert(Alert.AlertType.ERROR, "Failed", "Failed to resolve your IP address. Please make sure you don't block " +
                            "https://icanhazip.com/ and try again.");
                }
            });
            return;
        }
        if (!ProxyCheckCommand.isRunning()) {
            if (!proxies.isEmpty()) {
                controller.progressBarProxy.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                controller.label_working_proxies.setText("Working Proxies: 0"); // reset working proxy count
                controller.table_proxy.getItems().clear(); // reset table
                controller.proxyStartButton.setDisable(true);
                ProxyCheckCommand.startCheck(proxies, controller.table_proxy);
                notifyCompleted = false;
            } else {
                Toast.makeToast("Please load a new proxy file first.");
            }
        }
    }

    private String getIp() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new URL("https://icanhazip.com/").openStream(),
                StandardCharsets.UTF_8));
        return r.readLine();
    }

    public List<String> getProxies() {
        return proxies;
    }
}
