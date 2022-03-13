package nobility.model;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.util.Duration;
import nobility.Cycle;
import nobility.Main;
import nobility.SystemCycle;
import nobility.UpdateManager;
import nobility.handler.ModuleHandler;
import nobility.handler.modules.hits.Hit;
import nobility.handler.modules.rotmg.ItemParser;
import nobility.handler.settings.Collects;
import nobility.save.Combos;
import nobility.save.Defaults;
import nobility.save.Save;
import nobility.save.SerializableManager;
import nobility.tools.Alerter;
import nobility.tools.Toast;
import nobility.tools.Tools;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Model {

    public static final String APP_NAME = "RealmCrak";
    public static final String ACTIVE_PAGE_BACKGROUND_COLOR = "#02030A";
    public static final String ICON = String.valueOf(Main.class.getResource("/images/icon.png"));
    public static final String DIALOG_CSS = String.valueOf(Main.class.getResource("/css/dialog.css"));
    public static final String FX_PATH = "/fx/";

    private Stage mainStage;
    private final ModuleHandler moduleHandler;
    private Save save;
    private Combos combos;
    private final ExecutorService cycleService;
    private final ExecutorService systemCycleService;
    private final Collects collects;
    private FXTrayIcon icon;
    public MenuItem exit, show, start, stop, openResults;
    private final FXModel fxModel = new FXModel();
    private final FadeTransition fadeTransition;
    private final ItemParser itemParser;
    private final UpdateManager updateManager;

    public Model() {
        save = SerializableManager.loadSettings();
        if (save == null) {
            save = new Save();
            save.loadDefaultSettings();
        }
        save.checkForNewSettings();
        saveSettings();
        combos = SerializableManager.loadCombos();
        if (combos == null) {
            combos = new Combos();
            saveCombos();
        }
        cycleService = Executors.newSingleThreadExecutor();
        cycleService.submit(new Cycle(this));
        systemCycleService = Executors.newSingleThreadExecutor();
        systemCycleService.submit(new SystemCycle(this));
        fadeTransition = new FadeTransition(Duration.seconds(2), fxModel.getBtnConsole());
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.3);
        fadeTransition.setCycleCount(Animation.INDEFINITE);
        itemParser = new ItemParser();
        updateManager = new UpdateManager(this);
        collects = new Collects(this);
        moduleHandler = new ModuleHandler(this);
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    public ItemParser getItemParser() {
        return itemParser;
    }

    public void setComboSlider(int posistion, int max) {
        fxModel.getSldComboLine().setDisable(false);
        fxModel.getSldComboLine().setMax(max);
        fxModel.getSldComboLine().setValue(posistion);
    }

    public void setOptionBoxes() {
        if (FXTrayIcon.isSupported()) {
            Platform.setImplicitExit(!save.getBoolean(Defaults.CLOSETOSYSTEMTRAY));
            fxModel.getS_closetotray().setSelected(save.getBoolean(Defaults.CLOSETOSYSTEMTRAY));
        }
        fxModel.getS_skiptooshort().setSelected(save.getBoolean(Defaults.SKIPTOOSHORT));
        fxModel.getS_autoremovecombodupes().setSelected(save.getBoolean(Defaults.AUTOREMOVEDUPE_COMBOS));
        fxModel.getS_autoremoveproxydupes().setSelected(save.getBoolean(Defaults.AUTOREMOVEDUPE_PROXIES));
        fxModel.getS_showhits().setSelected(save.getBoolean(Defaults.SHOWHITS));
        fxModel.getS_showdebug().setSelected(save.getBoolean(Defaults.SHOWDEBUG));
        fxModel.getS_showproxyerrors().setSelected(save.getBoolean(Defaults.SHOWPROXYERRORS));
        fxModel.getS_emptycycle().setSelected(save.getBoolean(Defaults.EMPTYCONSOLECYCLE));
        fxModel.getS_consolealert().setSelected(save.getBoolean(Defaults.SHOWCONSOLEALERT));
        fxModel.getS_savechecked().setSelected(save.getBoolean(Defaults.SAVECHECKED));
        fxModel.getS_savenames().setSelected(save.getBoolean(Defaults.SAVENAMESINFILE));
        fxModel.getS_checkRealmEye().setSelected(save.getBoolean(Defaults.CHECKREALMEYE));
        fxModel.getS_namechosen().setSelected(save.getBoolean(Defaults.NAMECHOSEN));
        fxModel.getS_savegold().setSelected(save.getBoolean(Defaults.SAVEGOLDINFILE));
        fxModel.getS_saveranks().setSelected(save.getBoolean(Defaults.SAVERANKINFILE));
        fxModel.getS_autoscroll().setSelected(save.getBoolean(Defaults.AUTOSCROLL));
    }

    public void setTextFields() {
        fxModel.getThreads().setText(String.valueOf(save.getInteger(Defaults.CHECKERTHREADS)));
        fxModel.getTimeout().setText(String.valueOf(save.getInteger(Defaults.CHECKERTIMEOUT)));
        fxModel.getRetries().setText(String.valueOf(save.getInteger(Defaults.CHECKERRETRIES)));
        fxModel.getTfRank().setText(String.valueOf(save.getInteger(Defaults.HQRANK)));
        fxModel.getTfGold().setText(String.valueOf(save.getInteger(Defaults.HQGOLD)));
        fxModel.getTfFame().setText(String.valueOf(save.getInteger(Defaults.HQFAME)));
        fxModel.getTfMaxCharacters().setText(String.valueOf(save.getInteger(Defaults.HQCHARS)));
        fxModel.getTfRealmeyeRetries().setText(String.valueOf(save.getInteger(Defaults.REALMEYERETRIES)));
    }

    public void setExtraOptions() {
        fxModel.getChbxPetRarity().setValue(save.getString(Defaults.HQPETRARITY));
        fxModel.getChbxSeperator().setValue(save.getString(Defaults.SEPERATOR));
        fxModel.getChbxProxyType().setValue(save.getBoolean(Defaults.SOCKS) ? "SOCKS" : "HTTP(S)");
    }

    public void setComboProgressText(int progress, int total) {
        if (progress > total) {
            progress = total;
        }
        fxModel.getLblComboProgress().setText(progress + "/" + total
                + " Completed (" + Tools.percentFormat.format(progress / (double) total) + ")");
        fxModel.getSldComboLine().setValue(progress);
    }

    public void updateProgress() {
        Platform.runLater(() -> {
            int progress = moduleHandler.settings().getProgress();
            int size = moduleHandler.settings().getComboSize();
            setInvalid(moduleHandler.settings().getInvalid());
            setComboProgressText(progress, size);
            fxModel.getProgressBarCombo().setProgress((double) Math.round(100.0 / size * progress) / 100);
        });
    }

    public void updateCpu(String s) {
        Platform.runLater(() -> fxModel.getCpu().setText("CPU: " + s));
    }

    public void updateRam(String s) {
        Platform.runLater(() -> fxModel.getRam().setText("RAM: " + s));
    }

    public void startConsoleAlert() {
        if (!save.getBoolean(Defaults.SHOWCONSOLEALERT)) {
            return;
        }
        if (fxModel.getPnlConsole().isVisible()) {
            stopConsoleButtonBlink();
            return;
        }
        fadeTransition.play();
    }

    public void stopConsoleButtonBlink() {
        fadeTransition.jumpTo(Duration.ZERO);
        fadeTransition.stop();
    }

    public void setRetries(int retries) {
        Platform.runLater(() -> fxModel.getLblRetries().setText("Retries: " + retries));
    }

    public void setCpm(int cpm) {
        Platform.runLater(() -> fxModel.getLblCpm().setText("CPM: " + cpm));
    }

    public void setRealtimeCpm(int cpm) {
        Platform.runLater(() -> fxModel.getLblRTCpm().setText("Realtime CPM: " + cpm));
    }

    public void setErrors(int errors) {
        Platform.runLater(() -> fxModel.getLblErrors().setText("Errors: " + errors));
    }

    public void setProxies(int proxies) {
        Platform.runLater(() -> fxModel.getLblProxies().setText("Proxies: " + proxies));
    }

    public void updateProxies() {
        Platform.runLater(() -> fxModel.getLblProxies().setText("Proxies: " + collects.getProxies().size()));
    }

    public void setHits(int hits) {
        Platform.runLater(() -> fxModel.getLblHits().setText("LQ Hits: " + hits));
    }

    public void setHQHits(int hits) {
        Platform.runLater(() -> fxModel.getLblHQHits().setText("HQ Hits: " + hits));
    }

    public void setComboName(String name) {
        Platform.runLater(() -> fxModel.getLblComboName().setText(name));
    }

    public void setChecked(int size) {
        Platform.runLater(() -> fxModel.getLblChecked().setText("Checked Hits: " + size));
    }

    public void setTime(String time) {
        Platform.runLater(() -> fxModel.getLblTime().setText("Elapsed Time: " + time));
    }

    public void setInvalid(int invalid) {
        Platform.runLater(() -> fxModel.getLblInvalid().setText("Invalid: " + invalid));
    }

    public void setThreads(int threads) {
        Platform.runLater(() -> fxModel.getLblThread().setText("Threads: " + threads));
    }

    public void setupMenuIcon() {
        if (FXTrayIcon.isSupported()) {
            icon = new FXTrayIcon(mainStage, Main.class.getResource("/images/icon.png"));
            icon.setApplicationTitle(Model.APP_NAME);
            icon.addMenuItem(show = new MenuItem("Show/Hide " + APP_NAME));
            show.setOnAction(event -> {
                if (!mainStage.isShowing()) {
                    Platform.setImplicitExit(!save.getBoolean(Defaults.CLOSETOSYSTEMTRAY));
                    mainStage.show();
                } else {
                    Platform.setImplicitExit(false);
                    mainStage.close();
                }
            });
            icon.addSeparator();
            icon.addMenuItem(start = new MenuItem("Start Checker"));
            start.setOnAction(event -> {
                if (!moduleHandler.settings().isRunning() && !moduleHandler.settings().isStopping()) {
                    moduleHandler.start();
                }
            });
            icon.addMenuItem(stop = new MenuItem("Stop Checker"));
            stop.setOnAction(event -> moduleHandler.stop());
            icon.addMenuItem(openResults = new MenuItem("Open Results"));
            openResults.setOnAction(event -> moduleHandler.openResultsFolder());
            icon.addSeparator();
            icon.addMenuItem(exit = new MenuItem("Exit"));
            exit.setOnAction(event -> shutdown());
            icon.show();
        } else {
            Platform.setImplicitExit(true);
            System.out.println("Your device doesn't support the menu icon.");
        }
    }

    public FXTrayIcon getIcon() {
        return icon;
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        this.mainStage.setOnCloseRequest(e -> {
            if (FXTrayIcon.isSupported()) {
                if (save.getBoolean(Defaults.CLOSETOSYSTEMTRAY)) {
                    this.mainStage.close();
                } else {
                    shutdown();
                }
            } else {
                shutdown();
            }
        });
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public Save save() {
        return save;
    }

    public Combos combos() {
        return combos;
    }

    public void resetSettings() {
        Alerter.showConfirm("Reset Settings", "This will set all your settings to default. \n" +
                "Do you want to continue?", () -> {
            SerializableManager.resetSettings();
            save = new Save();
            save.loadDefaultSettings();
            SerializableManager.saveSettings(save);
            Toast.makeToast("Successfully reset your settings.");
            setOptionBoxes();
            setTextFields();
            setExtraOptions();
        });
    }

    public int getThreads() {
        return save.getInteger(Defaults.CHECKERTHREADS);
    }

    public int getTimeouts() {
        return save.getInteger(Defaults.CHECKERTIMEOUT);
    }

    /**
     * Gets the retries setting
     * @return model.getSave().getIntegers(retries)
     */
    public int getCheckerRetries() {
        return save.getInteger(Defaults.CHECKERRETRIES);
    }

    public ModuleHandler module() {
        return moduleHandler;
    }

    public Collects collects() {
        return collects;
    }

    public void saveSettings() {
        SerializableManager.saveSettings(save);
    }

    public void saveCombos() { SerializableManager.saveCombos(combos); }

    private String time() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int am = c.get(Calendar.AM_PM);
        return "[" + hour + ":" + (String.valueOf(minute).length() == 1 ? "0" : "")
                + minute + (am == 0 ? "AM" : "PM") + "]";
    }

    public void writeToHits(Hit hit) {
        collects.addToCheckedList(hit.getCombo());
        collects.getHitList().add(hit);
        if (!hit.isHighQuality()) {
            moduleHandler.settings().addLowQualityHit();
        } else {
            moduleHandler.settings().addHighQualityHit();
        }
        Platform.runLater(() -> fxModel.getHitsOutput().appendText(time() + " Found " + (!hit.isHighQuality() ? "LQ" : "HQ")
                + " Hit: " + hit.getCombo() + "\n", save.getBoolean(Defaults.AUTOSCROLL)));
    }

    public void printDebug(String s) {
        if (save.getBoolean(Defaults.SHOWDEBUG)) {
            System.out.println(s);
        }
    }

    public void printProxyErrors(String s) {
        if (save.getBoolean(Defaults.SHOWPROXYERRORS)) {
            System.out.println(s);
        }
    }

    public void shutdown() {
        if (moduleHandler.settings().isRunning() || moduleHandler.settings().isStopping()) {
            Alerter.showConfirm("Shutdown", APP_NAME + " is currently running.\n" +
                    "Exiting will force stop the threads and might mess up results.\n" +
                    "Do you still want to exit?", () -> {
                if (icon != null) {
                    icon.hide();
                }
                moduleHandler.stopForShutdown();
                moduleHandler.stop();
                saveSettings();
                saveCombos();
                cycleService.shutdownNow();
                systemCycleService.shutdownNow();
                System.exit(0);
            });
        } else {
            if (icon != null) {
                icon.hide();
            }
            moduleHandler.stopForShutdown();
            moduleHandler.stop();
            saveSettings();
            saveCombos();
            systemCycleService.shutdownNow();
            cycleService.shutdownNow();
            System.exit(0);
        }
    }

    public FXModel getFxModel() {
        return fxModel;
    }

}
