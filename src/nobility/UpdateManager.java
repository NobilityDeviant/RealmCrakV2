package nobility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import nobility.model.Model;
import nobility.save.Defaults;
import nobility.tools.Alerter;
import org.apache.commons.text.TextStringBuilder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;

public class UpdateManager {

    private final Model model;
    private final String version = "1.2.9";
    protected final String githubLatest = "https://api.github.com/repos/NobilityDeviant/RealmCrakV2/releases/latest";

    public UpdateManager(Model model) {
        this.model = model;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) {
                }
            }};
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println("Failed to trust all certifications. Program might not be able to " +
                    "check for updates.");
        }
    }

    private JsonObject apiSheet() {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(githubLatest).openConnection();
            urlConnection.setReadTimeout(20_000);
            urlConnection.setConnectTimeout(20_000);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestProperty("user-agent", model.module().getRandomUserAgent());
            urlConnection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            TextStringBuilder stringBuilder = new TextStringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine).append("\n");
            }
            in.close();
            urlConnection.disconnect();
            return new JsonParser().parse(stringBuilder.toString()).getAsJsonObject();
        } catch (Exception e) {
            System.out.println("Failed to get github api response.. Error: " + e.getLocalizedMessage());
        }
        return null;
    }

    public String latestVersion() {
        JsonObject json = apiSheet();
        if (json != null) {
            return json.get("tag_name").toString().replaceAll("\"", "");
        }
        return null;
    }

    //crappy overkill lol.. but idk im not really sure how to do this properly.
    public boolean isLatest(String latest) {
        if (latest == null || latest.equals(version)) {
            return true;
        }
        try {
            String[] latestSplit = latest.split("\\.");
            String[] current = version.split("\\.");
            if (Integer.parseInt(latestSplit[0]) > Integer.parseInt(current[0])) {
                return false;
            }
            if (Integer.parseInt(latestSplit[1]) > Integer.parseInt(current[1])) {
                return false;
            }
            if (Integer.parseInt(latestSplit[2]) > Integer.parseInt(current[2])) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public void checkUpdates(boolean denyEnabled) {
        String latest = latestVersion();
        if (!isLatest(latest)) {
            showUpdatePrompt("Update Available - v" + latest, "A new update is available. Would you like to " +
                            "download the latest version from github?",
                    "https://github.com/NobilityDeviant/RealmCrakV2/releases/latest", denyEnabled);
        }
    }

    public void showUpdatePrompt(String title, String message, String link, boolean denyEnabled) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.CANCEL, ButtonType.YES);
        alert.setTitle(title);
        alert.getDialogPane().getStylesheets().add(Model.DIALOG_CSS);
        alert.getDialogPane().getStyleClass().add("dialog");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Model.ICON));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.YES)) {
                openLink(link);
            } else {
                if (denyEnabled) {
                    System.out.println("Update has been denied. You will no longer receive a notification about it until the next update.");
                    model.save().setBoolean(Defaults.DENIEDUPDATE, true);
                    model.saveSettings();
                }
            }
        });
    }

    private void openLink(String link) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception e) {
                Alerter.showError("Unable to open " + link + " \nError: " + e.getLocalizedMessage());
            }
        } else {
            Alerter.showError("Desktop is not supported on this device.");
        }
    }

    public String getVersion() {
        return version;
    }
}
