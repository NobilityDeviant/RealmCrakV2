package nobility.save;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Save implements Serializable {

    private final Map<String, Integer> integers = new HashMap<>();
    private final Map<String, Boolean> booleans = new HashMap<>();
    private final Map<String, String> strings = new HashMap<>();

    public void clearSettings() {
        integers.clear();
        booleans.clear();
        strings.clear();
    }

    public void loadDefaultSettings() {
        for (Defaults setting : Defaults.values()) {
            switch (setting.getType()) {
                case 0:
                    setBoolean(setting, (Boolean) setting.getValue());
                    break;
                case 1:
                    setInteger(setting, (Integer) setting.getValue());
                    break;
                case 2:
                    setString(setting, (String) setting.getValue());
                    break;
            }
        }
    }

    public void checkForNewSettings() {
        for (Defaults setting : Defaults.values()) {
            switch (setting.getType()) {
                case 0:
                    if (!booleans.containsKey(setting.getKey())) {
                        booleans.put(setting.getKey(), (Boolean) setting.getValue());
                    }
                    break;
                case 1:
                    if (!integers.containsKey(setting.getKey())) {
                        integers.put(setting.getKey(), (Integer) setting.getValue());
                    }
                    break;
                case 2:
                    if (!strings.containsKey(setting.getKey())) {
                        strings.put(setting.getKey(), (String) setting.getValue());
                    }
                    break;
            }
        }
    }

    public boolean getBoolean(Defaults setting) {
        return booleans.getOrDefault(setting.getKey(), false);
    }

    public int getInteger(Defaults setting) {
        return integers.getOrDefault(setting.getKey(), 0);
    }

    public String getString(Defaults setting) {
        return strings.getOrDefault(setting.getKey(), "null");
    }

    public void setBoolean(Defaults setting, boolean value) {
        if (booleans.containsKey(setting.getKey())) {
            booleans.replace(setting.getKey(), value);
        } else {
            booleans.put(setting.getKey(), value);
        }
    }

    public void setInteger(Defaults setting, int value) {
        if (integers.containsKey(setting.getKey())) {
            integers.replace(setting.getKey(), value);
        } else {
            integers.put(setting.getKey(), value);
        }
    }

    public void setString(Defaults setting, String value) {
        if (strings.containsKey(setting.getKey())) {
            strings.replace(setting.getKey(), value);
        } else {
            strings.put(setting.getKey(), value);
        }
    }

}

