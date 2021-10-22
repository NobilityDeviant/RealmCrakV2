package nobility.save;

import java.io.*;
import java.util.ConcurrentModificationException;

public class SerializableManager {

    private static final String settingsFileName = "save.nobi";
    private static final String combosFileName = "combos.nobi";
    private static final String savePath = "." + File.separator + "resources" + File.separator;

    public static Save loadSettings() {
        File file = new File(savePath + settingsFileName);
        return (Save) loadSerializedFile(file);
    }

    public static Combos loadCombos() {
        File file = new File(savePath + combosFileName);
        return (Combos) loadSerializedFile(file);
    }

    public static void saveSettings(Save items) {
        try {
            if (items == null) {
                return;
            }
            storeSerializableClass(items, new File(savePath + settingsFileName));
        } catch (ConcurrentModificationException e) {
            System.out.println("Error saving settings.");
        } catch (Throwable e) {
            File path = new File(savePath);
            if (!path.exists()) {
                if (path.mkdir()) {
                    System.out.println("Creating new settings file.");
                }
            }
        }
    }

    public static void saveCombos(Combos items) {
        try {
            if (items == null) {
                return;
            }
            storeSerializableClass(items, new File(savePath + combosFileName));
        } catch (ConcurrentModificationException e) {
            System.out.println("Error saving combos.");
        } catch (Throwable e) {
            File path = new File(savePath);
            if (!path.exists()) {
                if (path.mkdir()) {
                    System.out.println("Creating new combos save file.");
                }
            }
        }
    }

    public static void resetSettings() {
        File file = new File(savePath + settingsFileName);
        if (!file.delete()) {
            System.out.println("Failed to delete settings file.");
        }
    }

    private static Object loadSerializedFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            if (!f.exists()) {
                return null;
            }
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
            Object object = in.readObject();
            in.close();
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    private static void storeSerializableClass(Serializable items, File f) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(items);
        out.close();
    }

}
