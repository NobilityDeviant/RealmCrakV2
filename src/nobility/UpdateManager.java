package nobility;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nobility.model.Model;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;

public class UpdateManager {

    private final Model model;

    public UpdateManager(Model model) {
        this.model = model;
    }

    private final String version = "1.2.0.3";
    private final String updateLink = "https://www.dropbox.com/s/7922jic8i8pksab/RealmCrakV2.jar?dl=1";
    private final String versionLink = "https://www.dropbox.com/s/l8i65ssxntolxkv/version.txt?dl=1";

    public final String getLatestVersion() {
        try {
            removeValidation();
            URL fileSizeURL = new URL(versionLink);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileSizeURL.openStream()));
            String line = in.readLine();
            in.close();
            return line.substring(0, line.indexOf(":"));
        } catch (Exception e) {
            return "1.0";
        }
    }

    public final boolean[] isLatestVersion() {
        try {
            removeValidation();
            URL fileSizeURL = new URL(versionLink);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileSizeURL.openStream()));
            String line = in.readLine();
            in.close();
            String latestVersion = line.substring(0, line.indexOf(":"));
            boolean required = Boolean.parseBoolean(line.substring(line.indexOf(":") + 1));
            return new boolean[] {version.equalsIgnoreCase(latestVersion), required};
        } catch (Exception e) {
            return new boolean[] {true, false};
        }
    }

    private void removeValidation() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
            public java.security.cert.X509Certificate[] getAcceptedIssuers(){return null;}
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) {
            }
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) {
            }
        }};
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception ignored) {}
    }

    public void openUpdateLink(boolean required) {
        try {
            updateClientAndLaunch();
        } catch (Exception e) {
            e.printStackTrace();
            model.showError("Failed to download the client.\n"
                    + updateLink + " \nhas been copied to your clipboard. Please download it yourself.");
            StringSelection selection = new StringSelection(updateLink);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            if (required) {
                System.exit(0);
            }
        }
    }

    private void updateClientAndLaunch() {
        File downloadedClient = new File(System.getProperty("user.home") + "/RealmCrakV2.jar");
        try (InputStream in = new URL(updateLink).openStream()) {
            long size = Files.copy(in, downloadedClient.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Client downloaded successfully! Size: " + size + " Path: " + downloadedClient.getAbsolutePath());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(String.valueOf(Main.class.getResource("icon.png"))));
            alert.setTitle("Download Complete!");
            alert.setHeaderText("");
            alert.setContentText("The new client has been downloaded. It can be found in your User folder. \n" +
                    "Please copy it into the RealmCrak folder.\n" +
                    "Path: " + downloadedClient.getAbsolutePath() + "\n" +
                    "Close this window to shutdown...");
            alert.showAndWait();
            new Thread(() -> {
                if (Desktop.isDesktopSupported()){
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.open(downloadedClient.getParentFile());
                    } catch (IOException e) {
                        System.err.println("Failed to open folder. Message: " + e.getLocalizedMessage());
                    }
                }
                System.exit(0);
            }).start();
        } catch (Exception e) {
            model.showError("Failed to download the update. Contact Nobility for a link or try again.\n"
                    + "Close this window to shutdown...");
            System.exit(-1);
        }
    }

    public String getVersion() {
        return version;
    }
}
