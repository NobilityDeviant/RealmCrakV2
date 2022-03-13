package nobility.proxy.commands;

import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import nobility.tools.Alerter;
import nobility.proxy.components.ProxySettings;
import nobility.proxy.components.entities.Proxy;
import nobility.proxy.components.entities.ProxyAnonymity;
import nobility.proxy.components.entities.ProxyStatus;
import nobility.tools.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExportCommand {

    /**
     * Gets a user selected destination file to save all loaded proxies onto, in the format ip:port
     * @param list - collection component
     */
    public static void saveNoFilter(List<String> list) {
        if (list.size() != 0) {
            File file = FileCommand.getFileToSave(false,
                    list.size() + "_" + ProxySettings.getConfig().getProxyType().name() + "_proxies");
            if (file != null) {
                try {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(file));
                    for (String ip_port : list) {
                        printWriter.write(ip_port + "\n");
                    }
                    printWriter.close();
                    Toast.makeToast("All proxies have been successfully exported to disk!");
                } catch (IOException e) {
                    Alerter.showAlert(Alert.AlertType.ERROR, "Export Failed",
                            "Unable to export the data. Error: " + e.getMessage());
                }
            }
        } else {
            Toast.makeToast("There's currently no proxies loaded to export!");
        }
    }

    /**
     * Gets a user selected destination file to export the proxy table in a comma separated value file.
     * @param tableView - collection component
     */
    public static void saveAsTable(TableView<Proxy> tableView) {
        if (ProxyCheckCommand.isRunning()) {
            Toast.makeToast("Please wait for the proxy checker to stop.");
            return;
        }
        if (tableView.getItems().size() != 0) {
            File file = FileCommand.getFileToSave(true,
                    tableView.getItems().size() + "_" + ProxySettings.getConfig().getProxyType().name() + "_proxies");
            if (file != null) {
                try {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(file));
                    for (Proxy proxy : tableView.getItems()) {
                        printWriter.write(getCSV(proxy));
                    }
                    printWriter.close();
                    Toast.makeToast("The table has been successfully exported to disk!");
                } catch (IOException e) {
                    Alerter.showAlert(Alert.AlertType.ERROR, "Export Failed",
                            "Unable to export the table. Error: " + e.getMessage());
                }
            }
        } else {
            Toast.makeToast("There's currently no data to export!");
        }
    }

    /**
     * Gets a user selected destination file to export the proxy table based on the given ProxyStatus and
     * ProxyAnonymity values, in the format ip:port
     * @param tableView - collection component
     * @param proxyStatus - The ProxyStatus
     * @param proxyAnonymity - The ProxyAnonymity (null for all proxies belonging to ProxyStatus)
     */
    public static void save(TableView<Proxy> tableView, ProxyStatus proxyStatus, ProxyAnonymity proxyAnonymity) {
        if (ProxyCheckCommand.isRunning()) {
            Toast.makeToast("Please wait for the proxy checker to stop.");
            return;
        }
        if (tableView.getItems().size() != 0) {
            File file = FileCommand.getFileToSave(false,
                    getFilteredListSize(tableView, proxyStatus, proxyAnonymity)
                            + "_" + ProxySettings.getConfig().getProxyType().name()
                            + "_" + (proxyAnonymity == null ? "" : proxyAnonymity + "_") + "proxies");
            if (file != null) {
                try {
                    PrintWriter printWriter = new PrintWriter(new FileWriter(file));
                    for (Proxy proxy : tableView.getItems()) {

                        String line = getLine(proxy, proxyStatus, proxyAnonymity);
                        if (line != null) { // a valid line was given
                            printWriter.write(line);
                        }
                    }
                    printWriter.close();
                    Toast.makeToast("The data has been successfully exported to disk!");
                } catch (IOException e) {
                    Alerter.showAlert(Alert.AlertType.ERROR, "Export Failed",
                            "Unable to export the data. Error: " + e.getMessage());
                }
            }
        } else {
            Toast.makeToast("There's currently no data to export!");
        }
    }

    /**
     * Generates comma separated values (csv) for a given Proxy
     * @param proxy - The Proxy object
     * @return String - csv
     */
    private static String getCSV(Proxy proxy) {
        return proxy.getIp() + "," +
                proxy.getPort() + "," +
                proxy.getProxyStatus() + "," +
                proxy.getProxyAnonymity() + "," +
                proxy.getCountry() + "," +
                proxy.getResponseTime() + "\n";
    }

    /**
     * Generates a line in the form ip:port for a given proxy confining to ProxyStatus and ProxyAnonymity
     * @param proxy - The Proxy object
     * @param proxyStatus - The ProxyStatus
     * @param proxyAnonymity - The ProxyAnonymity (null for all proxies belonging to ProxyStatus)
     * @return String - in the form ip:port,
     *         null if the proxy does not confine to the given proxyStatus and/or proxyAnonymity
     */
    private static String getLine(Proxy proxy, ProxyStatus proxyStatus, ProxyAnonymity proxyAnonymity) {
        if (proxy.getProxyStatus() == proxyStatus) {
            if (proxyAnonymity != null) {
                if (proxyAnonymity == proxy.getProxyAnonymity()) {
                    return proxy.getIp() + ":" + proxy.getPort() + "\n";
                } else {
                    return null;
                }
            } else {
                return proxy.getIp() + ":" + proxy.getPort() + "\n";
            }
        }
        return null;
    }

    private static int getFilteredListSize(TableView<Proxy> tableView, ProxyStatus proxyStatus, ProxyAnonymity proxyAnonymity) {
        int amount = 0;
        for (Proxy proxy : tableView.getItems()) {
            String line = getLine(proxy, proxyStatus, proxyAnonymity);
            if (line != null) {
                amount++;
            }
        }
        return amount;
    }
}
