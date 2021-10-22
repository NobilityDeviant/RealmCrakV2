package nobility.proxy.commands;

import javafx.scene.control.Alert;
import nobility.proxy.AlertBox;
import nobility.proxy.components.ProxySettings;
import nobility.proxy.components.UserSettings;
import nobility.proxy.components.entities.Proxy;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads a file or string onto a ListView in the form ip:port
 */
public class LoadCommand {

    /**
     * Manages the addition of one or more files onto the ListView
     * @param list - List of files to add onto the view (null to show dialog)
     */
    public static List<String> loadFile(List<File> list) {
        List<String> proxies = new ArrayList<>();
        list = list == null ? FileCommand.getFilesToOpen() : list;
        if (list != null) {
            if (!list.isEmpty()) {
                final UserSettings settings = ProxySettings.getConfig();
                settings.setLatestOpenPath(list.get(0).getParent());
                ProxySettings.saveConfig(settings);
            }
            for (File file : list) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        proxies.add(line);
                    }
                } catch (FileNotFoundException e) {
                    AlertBox.show(Alert.AlertType.INFORMATION, "File Not Found",
                            "The file you selected: " + file.getName() + ", doesn't seem to exist!");
                    return null;
                } catch (IOException e) {
                    AlertBox.show(Alert.AlertType.ERROR, "File Exception",
                            "Unable to read the file " + file.getName() + ". Error: " + e.getMessage());
                    return null;
                }
            }
        }
        if (list != null && !list.isEmpty()) {
            if (proxies.isEmpty()) {
                AlertBox.show(Alert.AlertType.ERROR, "Empty File(s)",
                        "The file(s) you selected are empty.");
                return null;
            }
            if (ProxySettings.getConfig().getAutoRemoveDuplicates()) {
                Set<String> dedupe = new LinkedHashSet<>(proxies);
                if (dedupe.size() == proxies.size()) {
                    System.out.println("Loaded proxy file(s) with " + proxies.size() + " proxies and no duplicates found.");
                } else {
                    int found = proxies.size() - dedupe.size();
                    proxies.clear();
                    proxies.addAll(dedupe);
                    System.out.println("Loaded proxy file(s) with " + dedupe.size()
                            + " proxies and " + found + " duplicates removed.");
                }
            } else {
                System.out.println("Loaded proxy file(s) with " + proxies.size());
            }
        }
        return proxies;
    }

    /**
     * Adds a string onto the listview provided its in a valid format
     * @param string - The String to add to the ListView
     * @param view - The ListView to add the string onto.
     * @return Boolean - Whether addition of item onto the listview was successful
     */
    private static boolean addItem(String string, List<String> view) {
        if ((!ProxySettings.getConfig().isAutoCheckType() && !Proxy.isValidFormat(string))) {
            return false;
        }
        if ((ProxySettings.getConfig().getAutoRemoveDuplicates() && view.contains(string))) {
            return false;
        }
        return view.add(string);
    }
}
