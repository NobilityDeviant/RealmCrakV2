package nobility.proxy.commands;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import nobility.proxy.components.ProxySettings;
import nobility.proxy.components.RequestAPI;
import nobility.proxy.components.UserSettings;
import nobility.proxy.components.entities.Proxy;
import nobility.proxy.components.entities.ProxyStatus;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Takes each proxy value in the ListView, performs checks and updates TableView accordingly.
 */
public class ProxyCheckCommand {

    private static final int thCount_start = Thread.activeCount();
    private static final UserSettings settings = ProxySettings.getConfig();

    /**
     * Setups the thread pool and launches asynchronous checks on the list of proxies
     * @param list - The List containing the proxies that will be checked
     * @param tableView - The TableView that will be updated with the status of the proxies
     */
    public static void startCheck(List<String> list, TableView<Proxy> tableView) {
        ExecutorService executorService = Executors.newFixedThreadPool(settings.getThreads());
        for (String proxy : list) {
            executorService.submit(new Checker(new Proxy(proxy), tableView));
        }
        executorService.shutdown();
    }


    /**
     * @return Boolean - Whether or not if proxies are still being checked
     */
    public static boolean isRunning() {
        return (Thread.activeCount() - thCount_start) != 0;
    }

    /**
     * Task that asynchronous checks each proxy and updates the TableView
     */
    private static class Checker implements Runnable {

        private final Proxy proxy;
        private final TableView<Proxy> tableView;

        /**
         *
         * @param proxy - The Proxy object to check.
         * @param tableView tv
         */
        public Checker(Proxy proxy, TableView<Proxy> tableView) {
            this.proxy = proxy;
            this.tableView = tableView;
        }

        @Override
        public void run() {
            java.net.Proxy.Type type;
            if (settings.isAutoCheckType()) {
                try {
                    type = detectProxyType(proxy.getIp(), proxy.getPort());
                } catch (IOException ignored) {
                    type = settings.getProxyType();
                }
            } else {
                type = settings.getProxyType();
            }
            RequestAPI requestAPI = new RequestAPI(settings);
            java.net.Proxy proxy = new java.net.Proxy(type,
                    new InetSocketAddress(
                            this.proxy.getIp(),
                            this.proxy.getPort()
                    )
            );

            Pair<HttpURLConnection, Long> pair = requestAPI.connect(proxy);
            if(pair != null) {
                try {
                    this.proxy.setProxyStatus(ProxyStatus.ALIVE);
                    RequestAPI.Response response = requestAPI.getResponse(pair.getKey());
                    this.proxy.setProxyAnonymity(response.anonymity);
                    this.proxy.setCountry(response.country);
                    this.proxy.setResponseTime(pair.getValue() + " (ms)");
                    this.proxy.setProxyTypeValue(type);

                } catch (Exception e) {
                    this.proxy.setProxyStatus(ProxyStatus.DEAD);
                    this.proxy.setProxyAnonymity(null);
                }
            } else {
                this.proxy.setProxyStatus(ProxyStatus.DEAD);
                this.proxy.setProxyAnonymity(null);
            }

            // this has to be done on another thread
            Platform.runLater(()-> tableView.getItems().add(this.proxy));
        }

        private java.net.Proxy.Type detectProxyType(String ip, int port) throws IOException {
            URL url = new URL("http://www.google.com");
            List<java.net.Proxy.Type> proxyTypesToTry = Arrays.asList(java.net.Proxy.Type.SOCKS,
                    java.net.Proxy.Type.HTTP);

            for (java.net.Proxy.Type proxyType : proxyTypesToTry) {
                java.net.Proxy proxy = new java.net.Proxy(proxyType, new InetSocketAddress(ip, port));
                URLConnection connection;
                try {

                    connection = url.openConnection(proxy);

                    //Can modify timeouts if default timeout is taking too long
                    connection.setConnectTimeout(settings.getTimeout());
                    connection.setReadTimeout(settings.getTimeout());

                    connection.getContent();
                    return(proxyType);
                } catch (SocketException ignored) {}
            }

            //No proxies worked if we get here
            return(java.net.Proxy.Type.HTTP);
        }
    }
}
