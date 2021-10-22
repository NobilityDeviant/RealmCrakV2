package nobility.proxy.commands;

import javafx.stage.FileChooser;
import nobility.proxy.components.ProxySettings;

import java.io.File;
import java.util.List;

/**
 * Contains static methods that allow selecting a file to save or files to open in a dialog.
 */
public class FileCommand {

    private static FileChooser fileChooser;

    /**
     * Show a dialog and get a File to save
     * @param table - Whether or not the file dialog is to save a table (csv).
     * @return File - The selected File object.
     */
    public static File getFileToSave(boolean table, String name) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        if(table) {
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV File", "*.csv")
            );
            if (!name.isEmpty()) {
                fileChooser.setInitialFileName(name + ".csv");
            }
        } else {
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("TXT Files", "*.txt")
            );
            if (!name.isEmpty()) {
                fileChooser.setInitialFileName(name + ".txt");
            }
        }
        return fileChooser.showSaveDialog(null);
    }



    /**
     * Shows a dialog and gets one or more files to open
     * @return List - A list of files selected
     */
    public static List<File> getFilesToOpen() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Proxy File(s)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT Files", "*.txt")
        );
        String path = ProxySettings.getConfig().getLatestOpenPath();
        if (path != null && new File(path).exists()) {
            fileChooser.setInitialDirectory(new File(path));
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        return fileChooser.showOpenMultipleDialog(null);
    }
}
