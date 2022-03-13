package nobility.proxy.components;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Manages Application settings and configuration files and directory
 */
public class ProxySettings {

    private static final String saveFile =  "proxychecker.json";

    /**
     * Gets the configuration file and parses it using Gson
     * @return UserSettings
     */
    public static UserSettings getConfig() {
        try {
            return new Gson().fromJson(new JsonReader(new FileReader(getConfigFile())), UserSettings.class);
        } catch (IOException e) {
            UserSettings userSettings = new UserSettings();
            saveConfig(userSettings);
            return userSettings;
        }
    }

    /**
     * Saves an updated UserSettings  to disk
     * @param userSettings - UserSettings
     * @return boolean - Whether or not the save was successful
     */
    public static boolean saveConfig(UserSettings userSettings) {
        if (userSettings == null) {
            userSettings = getConfig();
        }
        File file = new File(getSettingsFolder().getAbsolutePath() + File.separator + saveFile);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(new Gson().toJson(userSettings));
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the configuration File object from the settings folder
     * @return File
     */
    public static File getConfigFile() {
        File file = new File(getSettingsFolder().getAbsolutePath() + File.separator + saveFile);
        if(!file.exists()) {
            if(!saveConfig(new UserSettings())) {
                throw new RuntimeException("Unable to save default config.json!");
            }
        }
        return file;
    }


    /**
     * Gets the settings folder and creates it if it doesn't exist in the home directory
     * @return File
     */
    public static File getSettingsFolder()  {
        File file = new File("." + File.separator + "resources" + File.separator);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                throw new RuntimeException("Unable to create application directory!");
            }
        }
        return file;
    }

}
