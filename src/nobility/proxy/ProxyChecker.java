package nobility.proxy;

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
import nobility.proxy.components.RequestAPI;
import nobility.proxy.components.UserSettings;
import nobility.proxy.components.entities.Proxy;
import nobility.proxy.components.entities.ProxyAnonymity;
import nobility.proxy.components.entities.ProxyStatus;
import nobility.proxy.events.ProxyCheckerKeyEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
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

    private String getIp() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new URL("https://icanhazip.com/").openStream(),
                StandardCharsets.UTF_8));
        return r.readLine();
    }

    public ProxyChecker(Controller controller) {
        this.controller = controller;

        UserSettings settings = ProxySettings.getConfig();

        String ip = null;
        try {
            ip = getIp();
        } catch (IOException e) {
            System.out.println("Unable to grab IP for proxy checker. Error: " + e.getMessage());
            //e.printStackTrace();
        }

        if (ip != null && !settings.getIp().equals(ip)) {
            ProxySettings.saveConfig(settings.setIp(ip));
        }

        notifyCompleted = false;
        // set users IP address on label
        if (ip != null) {
            controller.label_ip_address.setText(ip);
        } else {
            controller.label_ip_address.setText("Unable to get IP");
        }

        controller.table_proxy.setOnKeyPressed(new ProxyCheckerKeyEvent());

        // setup table factory
        controller.column_ip.setCellValueFactory(new PropertyValueFactory<>("Ip"));
        controller.column_port.setCellValueFactory(new PropertyValueFactory<>("Port"));
        controller.column_status.setCellValueFactory(new PropertyValueFactory<>("ProxyStatus"));
        controller.column_anonymity.setCellValueFactory(new PropertyValueFactory<>("ProxyAnonymity"));
        controller.column_country.setCellValueFactory(new PropertyValueFactory<>("Country"));
        controller.column_response_time.setCellValueFactory(new PropertyValueFactory<>("ResponseTime"));
        controller.column_type.setCellValueFactory(new PropertyValueFactory<>("ProxyType"));

        controller.table_proxy.setRowFactory(tp -> new TableRow<Proxy>() {
            @Override
            protected void updateItem(Proxy proxy, boolean empty) {
                super.updateItem(proxy, empty);
                if ((proxy == null) || (proxy.getProxyStatus() == ProxyStatus.DEAD))  {
                    setStyle("");
                } else {
                    ProxyAnonymity anonymity = proxy.getProxyAnonymity();
                    for (Pair<ProxyAnonymity, String> p : ProxySettings.getConfig().getColorScheme()) {
                        if (p.getKey() == anonymity) {
                            setStyle("-fx-background-color: "+ p.getValue() + ";");
                        }
                    }
                }
            }
        });

        // manage progress bar and count for working proxies and checked proxies
        controller.table_proxy.getItems().addListener((ListChangeListener<Proxy>) c -> {
            if (!c.getList().isEmpty()) {
                Proxy proxy = c.getList().get(c.getList().size()-1); // newest added proxy
                controller.label_checked_proxies.setText("Checked Proxies: " + c.getList().size());
                if ((c.getList().size() == proxies.size())) {
                    controller.progressBar.setProgress(0f);
                    controller.button_check.setDisable(false); // disable check button until all proxies are checked
                    if(!notifyCompleted) {
                        AlertBox.show(Alert.AlertType.INFORMATION, "Task Completed",
                                "Proxy Checker has finished checking your proxies!");
                        notifyCompleted = true;
                    }
                } else {
                    controller.progressBar.setProgress((float) c.getList().size() / proxies.size());
                }
                if (proxy.getProxyStatus() == ProxyStatus.ALIVE) {
                    int current_working = Integer.parseInt(controller.label_working_proxies.getText().split(":")[1].trim());
                    controller.label_working_proxies.setText("Working Proxies: " + (current_working + 1));
                }
            }
        });

        // allow drag and drop of files into the loaded proxies view
        controller.table_proxy.setOnDragOver(event -> {
            if(!ProxyCheckCommand.isRunning()) {
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
        if(!ProxyCheckCommand.isRunning()) {
            List<String> temp = LoadCommand.loadFile(null);
            if (temp != null) {
                proxies.addAll(temp);
                controller.label_loaded_proxies.setText("Loaded Proxies: " + proxies.size());
            }
        } else {
            AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "You can't load proxies while the checker is running.");
        }
    }

    public void removeDupes() {
        if (!ProxyCheckCommand.isRunning()) {
            if (proxies.isEmpty()) {
                AlertBox.show(Alert.AlertType.ERROR, "No Proxies Loaded", "There are no proxies to check!");
                return;
            }
            Set<String> dedupe = new LinkedHashSet<>(proxies);
            if (dedupe.size() == proxies.size()) {
                AlertBox.show(Alert.AlertType.INFORMATION, "Clean Proxies", "No proxy duplicates found.");
            } else {
                int found = proxies.size() - dedupe.size();
                proxies.clear();
                proxies.addAll(dedupe);
                controller.label_loaded_proxies.setText("Loaded Proxies: " + proxies.size());
                AlertBox.show(Alert.AlertType.INFORMATION, "Cleaned", "Successfully removed " + found + " duplicate proxies.");
            }
        } else {
            AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "You can't remove duplicate proxies while the checker is running.");
        }
    }

    public void exportAll() {
        if (ProxyCheckCommand.isRunning()) {
            AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
            return;
        }
        ExportCommand.saveNoFilter(proxies);
    }

    public void clearProxies() {
        proxies.clear();
        controller.label_loaded_proxies.setText("Loaded Proxies: " + proxies.size());
    }

    public void start() {
        if (!ProxyCheckCommand.isRunning()) {
            if (!proxies.isEmpty()) {
                controller.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                controller.label_working_proxies.setText("Working Proxies: 0"); // reset working proxy count
                controller.table_proxy.getItems().clear(); // reset table
                controller.button_check.setDisable(true);
                ProxyCheckCommand.startCheck(proxies, controller.table_proxy);
                notifyCompleted = false;
            } else {
                AlertBox.show(Alert.AlertType.ERROR, "No Loaded Proxies", "There are no proxies to check!");
            }
                /* else {
                boolean choice = AlertBox.showImportProxy(Alert.AlertType.INFORMATION, "No Loaded Proxies",
                        "There are no proxies to check! To check a proxy you must load at least one first.\n\n"
                                + "Would you like to import proxies from the web? Press No or close this window to decline.");
                if (choice) {
                    loadLinks();
                }
            }*/
        }
    }

    public List<String> getProxies() {
        return proxies;
    }
}
