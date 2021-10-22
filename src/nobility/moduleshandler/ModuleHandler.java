package nobility.moduleshandler;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import nobility.model.Model;
import nobility.moduleshandler.modules.Module;
import nobility.moduleshandler.modules.hits.HitsExporter;
import nobility.moduleshandler.modules.rotmg.RealmOfTheMadGodCore;
import nobility.moduleshandler.modules.rotmg.RealmOfTheMadGodCycle;
import nobility.moduleshandler.modules.rotmg.RealmOfTheMadGodModule;
import nobility.moduleshandler.settings.ComboContainer;
import nobility.moduleshandler.settings.ProxyContainer;
import nobility.moduleshandler.settings.Settings;
import nobility.save.Defaults;
import nobility.tools.Tools;
import org.apache.commons.lang3.SystemUtils;

import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toMap;

public class ModuleHandler {

    private final Settings settings;
    private final Module module = new RealmOfTheMadGodModule();
    private final RealmOfTheMadGodCycle cycle;
    private final File dailySaveFolder;
    private File resultNames;
    private File currentSaveFolder;
    private List<String> userAgents;
    private final List<Future<String>> taskList = new ArrayList<>();
    private ExecutorService moduleCycle;
    private ExecutorService moduleThreads;
    private final Model model;

    public ModuleHandler(Model model) {

        this.model = model;
        cycle = new RealmOfTheMadGodCycle(model);
        settings = new Settings(model);

        File resultsFolder = new File("./data/results/");
        if (!resultsFolder.exists()) {
            if (!resultsFolder.mkdir()) {
                model.showError("Couldn't create /data/results/ folder. Please create it manually before starting.");
            }
        }
        String dataFolder = Tools.getDateForFolder();
        dailySaveFolder = new File(resultsFolder.getAbsolutePath() + "/" + dataFolder);
        if (!dailySaveFolder.exists()) {
            if (!dailySaveFolder.mkdir()) {
                model.showError("Couldn't create daily save folder. Please create a folder called "
                        + dataFolder + " like /data/results/" + dataFolder);
            }
        }
        loadCheckedHits();
        try {
            File ua = new File("./resources/useragents.txt");
            if (!ua.exists()) {
                FileOutputStream fos = null;
                try {
                    URL website = new URL("https://www.dropbox.com/s/a55ixf3wnmp5liy/useragents.txt?dl=1");
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    fos = new FileOutputStream(ua);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    System.out.println("Successfully downloaded useragents.txt");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Failed to download useragents.txt Error: " + e.getLocalizedMessage());
                    }
                }
            }
            userAgents = Files.readAllLines(ua.toPath());
        } catch (Exception e) {
            System.out.println("Failed to read /resources/useragents.txt. Error: " + e.getLocalizedMessage());
        }
    }

    public void start() {
        if (settings.isRunning()) {
            return;
        }
        if (settings.isStopping()) {
            model.showError("Please wait for the checker to fully stop.");
            return;
        }
        if (model.isFreetrialMode() && model.getDatabase().freeTrialEnded(model.getSavedKey())) {
            model.showError("Your free trial has ended.");
            return;
        }
        new Thread(() -> {
            if (model.collects().getCombos().isEmpty()) {
                model.showError("Please load a new combo file first.");
                return;
            }
            if (!module.proxyless()) {
                if (model.collects().getProxies().isEmpty()
                        && model.collects().getBackupProxies().isEmpty()) {
                    model.showError("Please load a new proxy file first.");
                    return;
                }
            }
            int threads = model.getThreads();
            if (threads < 5) {
                model.showError("Threads must be set to 5 and up.");
                return;
            }
            int timeout = model.getTimeouts();
            if (timeout < 1) {
                model.showError("Timeout must be set to 1 and up.");
                return;
            }
            if (settings.isComplete()) {
                model.showError("This combo is complete. Please load a new one.");
                return;
            }
            if (currentSaveFolder == null) {
                currentSaveFolder = new File(dailySaveFolder.getAbsolutePath() + "/"
                        + settings.getLoadedComboName().replace(".txt", "")
                        + "-" + Tools.getCurrentTimeForFolder() + "/");
                if (!currentSaveFolder.exists()) {
                    if (!currentSaveFolder.mkdir()) {
                        model.showError("Couldn't create save folder. There is something wrong with your " +
                                "folder permissions. Please fix that before using this program.");
                        return;
                    } else {
                        model.printDebug("Created save folder: " + currentSaveFolder.getAbsolutePath());
                    }
                }
                resultNames = new File(currentSaveFolder.getAbsolutePath() + "/" + "names.txt");
            }
            model.getFxModel().setStartButtons();
            if (model.isLoggedIn()) {
                return;
            }
            settings.setRunning(true);
            //if (moduleCycle == null) {
            cycle.start();
            moduleCycle = Executors.newSingleThreadExecutor();
            moduleCycle.submit(cycle);
            //}
            if (settings.getComboSize() < threads) {
                threads = settings.getComboSize();
            }
            if (!taskList.isEmpty()) {
                taskList.clear();
            }
            moduleThreads = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; i++) {
                taskList.add(moduleThreads.submit(new RealmOfTheMadGodCore(model)));
            }
        }).start();
    }

    public void stop() {
        if (!settings.isRunning()) {
            return;
        }
        if (settings.isStopping()) {
            return;
        }
        new Thread(() -> {
            settings.setStopping(true);
            settings.setRunning(false);
            try {
                moduleThreads.shutdown();
                if (moduleThreads.awaitTermination(5, TimeUnit.MINUTES)) {
                    for (Future<String> stringFuture : taskList) {
                        try {
                            String combo = stringFuture.get();
                            if (combo != null) {
                                model.collects().getCombos().add(new ComboContainer(combo,
                                        model.save().getString(Defaults.SEPERATOR)));
                                model.collects().removeComboProgress();
                            }
                        } catch (ExecutionException ignored) {}
                    }
                    settings.setThreads(0);
                }
            } catch (InterruptedException e) {
                System.out.println("Stop interrupted. Error: " + e.getLocalizedMessage());
                model.startConsoleAlert();
            } finally {
                saveCheckedHits();
                saveHits();
                if (currentSaveFolder.listFiles() == null
                        || Objects.requireNonNull(currentSaveFolder.listFiles()).length == 0) {
                    currentSaveFolder.delete();
                }
                model.collects().setComboProgress(settings.getProgress()); //TODO remove?
                model.updateProgress();
                model.getFxModel().setStopButtons();
                System.out.println("Successfully stopped checker with "
                        + settings.getProgress()
                        + "/" + settings.getComboSize() + " progress done.");
                model.saveSettings();
                cycle.stop();
                moduleCycle.shutdown();
                settings.setStopping(false);
                //System.out.println(settingsHandler.getComboProgress());
            }
        }).start();
    }


    public void loadComboList() {
        if (settings.isRunning()) {
            model.showError("Please stop the checker before loading a new combo.");
            return;
        }
        if (settings.isStopping()) {
            model.showError("Please wait for the checker to fully stop before loading a new combo.");
            return;
        }
        File lastPath = new File(model.save().getString(Defaults.LASTCOMBOFOLDER));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Combo File");
        if (lastPath.exists()) {
            fileChooser.setInitialDirectory(lastPath);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            model.save().setString(Defaults.LASTCOMBOFOLDER, System.getProperty("user.home"));
            model.saveSettings();
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            return;
        }
        model.save().setString(Defaults.LASTCOMBOFOLDER, selectedFile.getParent());
        model.saveSettings();
        if (!model.collects().getCombos().isEmpty()) {
            model.collects().getCombos().clear();
        }
        settings.setLoadedComboName(selectedFile.getName());
        settings.setLoadedComboPath(selectedFile.getAbsolutePath());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            String s;
            while ((s = reader.readLine()) != null) {
                try {
                    model.collects().getCombos().add(new ComboContainer(s,
                            model.save().getString(Defaults.SEPERATOR)));
                } catch (Exception ignored) {}
            }
            reader.close();
        } catch (IOException e) {
            model.showError("Failed to load combo file. Error: " + e.getLocalizedMessage());
            return;
        }
        if (model.collects().getCombos().isEmpty()) {
            model.showError("This combo file is empty. Please load a new one.");
            return;
        }
        if (model.save().getBoolean(Defaults.AUTOREMOVEDUPE_COMBOS)) {
            Set<ComboContainer> dedupe = new LinkedHashSet<>(model.collects().getCombos());
            if (dedupe.size() == model.collects().getCombos().size()) {
                System.out.println("Loaded combo file " + settings.getLoadedComboName() + " with "
                        + dedupe.size() + " combos and no duplicates found.");
            } else {
                int dupesFound = model.collects().getCombos().size() - dedupe.size();
                model.collects().getCombos().clear();
                model.collects().getCombos().addAll(dedupe);
                System.out.println("Loaded combo file " + settings.getLoadedComboName() + " with "
                        + dedupe.size() + " combos and " + dupesFound + " duplicates removed.");
            }
        } else {
            System.out.println("Loaded combo file " + settings.getLoadedComboName() + " with "
                    + model.collects().getCombos().size() + " combos.");
        }
        settings.setComboSize();
        model.setComboName(selectedFile.getName());
        resetStatusForNewCombo();
        model.collects().setComboMD5(selectedFile);
        int posistion = 0;
        if (model.collects().getComboMD5() != null) {
            posistion = model.combos().getPosistionForMD5(model.collects().getComboMD5());
        }
        model.setComboSlider(posistion, model.collects().getCombos().size());
        settings.setProgress(posistion);
        model.collects().setComboProgress(posistion);
        int size = settings.getComboSize();
        double percentage = Math.round(100.0 / size * posistion);
        model.setComboProgressText(posistion, settings.getComboSize(), percentage);
        model.updateProgress();
        //model.collects().updateComboIterator(posistion);
        currentSaveFolder = null;
    }

    public void loadProxyList() {
        if (settings.isRunning()) {
            model.showError("Please stop the checker before loading a new proxy list.");
            return;
        }
        if (settings.isStopping()) {
            model.showError("Please wait for the checker to fully stop before loading a new proxy list.");
            return;
        }
        File lastPath = new File(model.save().getString(Defaults.LASTCHECKERPROXYFOLDER));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Proxy File(s)");
        if (lastPath.exists()) {
            fileChooser.setInitialDirectory(lastPath);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            model.save().setString(Defaults.LASTCHECKERPROXYFOLDER, System.getProperty("user.home"));
            model.saveSettings();
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        model.save().setString(Defaults.LASTCHECKERPROXYFOLDER, selectedFiles.get(0).getParent());
        model.saveSettings();
        for (File file : selectedFiles) {
            try (BufferedReader r = new BufferedReader(new FileReader(file))) {
                String s;
                while ((s = r.readLine()) != null) {
                    if (s.contains(":")) {
                        model.collects().getProxies().add(new ProxyContainer(s));
                    }
                }
            } catch (IOException e) {
                model.printDebug("Failed to load proxy file: " + file.getName() + ". Error: " + e.getLocalizedMessage());
            }
        }
        if (model.collects().getProxies().isEmpty()) {
            model.showError((selectedFiles.size() > 1 ? "These proxy lists are empty. Please load new ones."
                    : "This proxy list is empty. Please load a new one."));
            return;
        }
        if (model.save().getBoolean(Defaults.AUTOREMOVEDUPE_PROXIES)) {
            Set<String> dedupe = new LinkedHashSet<>();
            model.collects().getProxies().forEach(c -> dedupe.add(c.toString()));
            if (dedupe.size() == model.collects().getProxies().size()) {
                System.out.println("Loaded proxy file(s) with "
                        + dedupe.size() + " proxies and no duplicates found.");
            } else {
                int dupesFound = model.collects().getProxies().size() - dedupe.size();
                model.collects().getProxies().clear();
                dedupe.forEach(c -> model.collects().getProxies().add(new ProxyContainer(c)));
                System.out.println("Loaded proxy file(s) with "
                        + dedupe.size() + " proxies and " + dupesFound + " duplicates removed.");
            }
        } else {
            System.out.println("Loaded proxy file(s) with " + model.collects().getProxies().size() + " proxies.");
        }
        model.collects().updateBackupProxies();
        model.setProxies(model.collects().getProxies().size());
    }

    /**
     * Removes duplicate lines from a list
     * @param list The id of the list. 0 = combo, 1 = proxies
     */

    public void removeListDuplicates(int list) {
        if (settings.isRunning()) {
            model.showError("Please stop the checker before removing duplicates.");
            return;
        }
        if (settings.isStopping()) {
            model.showError("Please wait for the checker to fully stop before removing duplicates.");
            return;
        }
        switch (list) {
            case 0:
                if (model.collects().getCombos().isEmpty()) {
                    model.showError("No combos have been loaded.");
                    return;
                }
                Set<ComboContainer> dedupe = new LinkedHashSet<>(model.collects().getCombos());
                if (dedupe.size() == model.collects().getCombos().size()) {
                    model.showMessage("Clean Combos", "No combo duplicates found.");
                } else {
                    int dupesFound = model.collects().getCombos().size() - dedupe.size();
                    model.collects().getCombos().clear();
                    model.collects().getCombos().addAll(dedupe);

                    model.setComboSlider(model.combos().getPosistionForMD5(model.collects().getComboMD5()),
                            model.collects().getCombos().size());
                    settings.setComboSize();
                    byte[] hash = null;
                    int posistion = 0;
                    try {
                        File loadedCombo = new File(settings.getLoadedComboPath());
                        if (loadedCombo.exists()) {
                            hash = MessageDigest.getInstance("MD5").digest(Files
                                    .readAllBytes(loadedCombo.toPath()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (hash != null) {
                        String md5 = DatatypeConverter.printHexBinary(hash);
                        posistion = model.combos().getPosistionForMD5(md5);
                    }
                    settings.setProgress(posistion);
                    model.collects().setComboProgress(posistion);
                    int percentage = (int) Math.round(100.0 / settings.getComboSize() * settings.getProgress());
                    model.setComboProgressText(posistion, settings.getComboSize(), percentage);
                    model.showMessage("Cleaned", "Successfully removed " + dupesFound + " duplicate combos.");
                }
                break;
            case 1:
                if (model.collects().getProxies().isEmpty()) {
                    model.showError("No proxies have been loaded.");
                    return;
                }
                Set<String> dedupe1 = new LinkedHashSet<>();
                model.collects().getProxies().forEach(c -> dedupe1.add(c.toString()));
                if (dedupe1.size() == model.collects().getProxies().size()) {
                    model.showMessage("Clean Proxies", "No proxy duplicates found.");
                } else {
                    int dupesFound = model.collects().getProxies().size() - dedupe1.size();
                    model.collects().getProxies().clear();
                    dedupe1.forEach(c -> model.collects().getProxies().add(new ProxyContainer(c)));
                    model.setProxies(model.collects().getProxies().size());
                    model.showMessage("Cleaned", "Successfully removed " + dupesFound + " duplicate proxies.");
                }
                break;
            default:
                break;
        }
    }

    public void stopForShutdown() {
        if (moduleCycle != null) {
            moduleCycle.shutdownNow();
        }
    }

    public void loadCheckedHits() {
        if (!model.save().getBoolean(Defaults.SAVECHECKED)) {
            return;
        }
        File file = new File("./data/" + module.tag() + "Checked.txt");
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println("Failed to create " + module.tag() + "Checked.txt in the data folder. Please create one or disable " +
                            "the Save Hits To Checked List option.");
                    model.startConsoleAlert();
                }
                return;
            }
            model.collects().getCheckedList().addAll(Files.readAllLines(file.toPath()));
            model.setChecked(model.collects().getCheckedList().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCheckedHits() {
        if (!model.save().getBoolean(Defaults.SAVECHECKED)) {
            return;
        }
        File file = new File("./data/" + module.tag() + "Checked.txt");
        if (!file.exists()) {
            return;
        }
        try {
            Files.write(file.toPath(), model.collects().getCheckedList(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveHits() {
        try {
            HitsExporter.export(model.collects().getHitList(), currentSaveFolder);
        } catch (IOException e) {
            System.out.println("Failed to save hits. Error: " + e.getMessage());
        }
        if (model.save().getBoolean(Defaults.SAVENAMESINFILE)) {
            if (!model.collects().getNameList().isEmpty()) {
                try {
                    Files.write(resultNames.toPath(), model.collects().getNameList(), Charset.defaultCharset());
                } catch (Exception e) {
                    System.out.println("Failed to write names. Error: " + e.getLocalizedMessage());
                }
            }
        }
        if (!model.collects().getRankMap().isEmpty()) {
            File rank = new File(currentSaveFolder.getAbsolutePath() + "/" + "ranks.txt");
            try {
                Map<String, Integer> sorted = model.collects().getRankMap().entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                        LinkedHashMap::new));
                BufferedWriter w = new BufferedWriter(new FileWriter(rank));
                sorted.forEach((key, value) -> {
                    try {
                        w.write("Rank: " + value + " - " + key);
                        w.newLine();
                    } catch (IOException ignored) {}
                });
                w.flush();
                w.close();
            } catch (Exception ignored) {}
        }

        if (!model.collects().getGoldMap().isEmpty()) {
            File gold = new File(currentSaveFolder.getAbsolutePath() + "/" + "gold.txt");
            try {
                Map<String, Integer> sorted = model.collects().getGoldMap().entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                        LinkedHashMap::new));
                BufferedWriter w = new BufferedWriter(new FileWriter(gold));
                sorted.forEach((key, value) -> {
                    try {
                        w.write("Gold: " + value + " - " + key);
                        w.newLine();
                    } catch (IOException ignored) {}
                });
                w.flush();
                w.close();
            } catch (Exception ignored) {}
        }
    }

    public void openResultsFolder() {
        new Thread(() -> {
            if (!Desktop.isDesktopSupported()){
                Platform.runLater(() -> model.showError("Desktop is not supported. Unable to open folders on this device."));
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            try {
                File file = (currentSaveFolder == null || !currentSaveFolder.exists()) ? dailySaveFolder : currentSaveFolder;
                if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
                    System.out.println("Opening result folder: " + file.getAbsolutePath());
                    System.out.println("If the file doesn't open, please report it to Nobility.");
                    model.startConsoleAlert();
                }
                desktop.open((currentSaveFolder == null || !currentSaveFolder.exists()) ? dailySaveFolder : currentSaveFolder);
            } catch (IOException e) {
                System.out.println("Failed to open folder. Error: " + e.getLocalizedMessage());
            }
        }).start();
    }

    private void resetStatusForNewCombo() {
        //settingsHandler.setInvalid(0);
        model.collects().setComboMD5(null);
        cycle.reset();
        model.setTime("0:0:0");
        //settings.setProgress(0);
        settings.setInvalid(0);
        settings.setCpm(0);
        model.setCpm(0);
        model.setRealtimeCpm(0);
        settings.setErrors(0);
        model.setErrors(0);
        model.collects().getHitList().clear();
        settings.setLowQualityHits(0);
        settings.setHighQualityHits(0);
        settings.setRetries(0);
        model.setRetries(0);
        model.collects().getGoldMap().clear();
        model.collects().getRankMap().clear();
    }

    public synchronized ProxyContainer getNextProxy() {
        ProxyContainer proxy = model.collects().getProxies().get(0);
        model.collects().getProxies().remove(proxy);
        return proxy;
    }

    public synchronized ProxyContainer getRandomProxy() {
        if (model.collects().getProxies().isEmpty()) {
            return null;
        }
        return model.collects().getProxies().get(ThreadLocalRandom.current()
                .nextInt(model.collects().getProxies().size()));
    }

    public String getRandomUserAgent() {
        if (!userAgents.isEmpty()) {
            return userAgents.get(ThreadLocalRandom.current().nextInt(userAgents.size()));
        } else {
            return "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20130406 Firefox/23.0";
        }
    }

    public Settings settings() {
        return settings;
    }
}
