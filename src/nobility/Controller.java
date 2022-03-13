package nobility;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import nobility.model.Model;
import nobility.proxy.ProxyChecker;
import nobility.proxy.commands.ExportCommand;
import nobility.proxy.commands.ProxyCheckCommand;
import nobility.proxy.components.entities.Proxy;
import nobility.proxy.components.entities.ProxyAnonymity;
import nobility.proxy.components.entities.ProxyStatus;
import nobility.save.Defaults;
import nobility.tools.Alerter;
import nobility.tools.TextAreaAutoScroll;
import nobility.tools.TextOutput;
import nobility.tools.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML private TextAreaAutoScroll out, hitsOutput;
    @FXML private Button btnConsole, btnClear,
            btnAbout, btnChecker, btnSettings, btnProxyChecker; //menu and console
    @FXML private Button btnSaveSettings, btnResetSettings; //settings
    @FXML private Button btnStart, btnStop;
    @FXML private Button btnUpdates;
    @FXML private BorderPane pnlConsole, pnlChecker, pnlProxyChecker, pnlSettings, pnlAbout;
    @FXML private CheckBox s_autoremovecombodupes, s_autoremoveproxydupes, s_showhits, s_showdebug, s_emptycycle,
            s_consolealert, s_savechecked, s_savenames, s_checkRealmEye,
            s_namechosen, s_savegold, s_saveranks, s_showproxyerrors,
            s_closetotray, s_autoscroll, s_skiptooshort;
    @FXML private TextField tfRank, tfGold, tfFame,
            tfMaxCharacters, tfRealmeyeRetries;
    @FXML private TextField threads, timeout, retries;
    @FXML private Label cpu, ram, lblVersion;
    @FXML private Label lblRetries, lblCpm, lblRTCpm, lblErrors, lblProxies, lblHits, lblHQHits,
            lblComboName, lblComboProgress, lblChecked, lblTime, lblInvalid, lblThread;
    @FXML private ProgressBar progressBarChecker;
    @FXML private Slider sldComboLine;
    @FXML private ChoiceBox<String> chbxSeperator, chbxPetRarity, chbxProxyType;
    @FXML private MenuButton menuCombos, menuProxies, menuTools;
    @FXML public TableView<Proxy> table_proxy = new TableView<>();
    @FXML public TableColumn<Proxy, String> column_ip;
    @FXML public TableColumn<Proxy, Integer> column_port;
    @FXML public TableColumn<Proxy, String> column_status;
    @FXML public TableColumn<Proxy, String> column_country;
    @FXML public TableColumn<Proxy, String> column_anonymity;
    @FXML public TableColumn<Proxy, String> column_response_time;
    @FXML public TableColumn<Proxy, String> column_type;
    @FXML public Label label_loaded_proxies;
    @FXML public Label label_checked_proxies;
    @FXML public Label label_working_proxies;
    @FXML public Label label_ip_address;
    @FXML public ProgressBar progressBarProxy;
    @FXML public Button proxyStartButton;
    @FXML private VBox progressVbox;

    private TextOutput output;
    private boolean sliding = false;
    private ProxyChecker proxyChecker;
    private final Model model;
    private Page currentPage;

    public Controller(Model model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        output = new TextOutput(out, model);
        model.getFxModel().setOut(out);
        System.setOut(new PrintStream(output));
        setMenuItems();
        setModelComponents();
        initializeUi();

        File resourceFolder = new File("./resources/");
        if (!resourceFolder.exists()) {
            if (!resourceFolder.mkdir()) {
                System.out.println("The resources folder couldn't be created.");
            }
        }
        File dataFolder = new File("./data/");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdir()) {
                System.out.println("The data folder couldn't be created.");
            }
        }
        proxyChecker = new ProxyChecker(this);
        switchPages(Page.CONSOLE);
        lblVersion.setText("Version: " + model.getUpdateManager().getVersion());
        String version = model.getUpdateManager().latestVersion();
        if (!model.save().getString(Defaults.UPDATEVERSION).equalsIgnoreCase(version)) {
            model.save().setBoolean(Defaults.DENIEDUPDATE, false);
            model.saveSettings();
        }
        if (!model.save().getBoolean(Defaults.DENIEDUPDATE)) {
            model.save().setString(Defaults.UPDATEVERSION, version);
            model.saveSettings();
            model.getUpdateManager().checkUpdates(true);
        }
        model.getMainStage().show();
    }

    @FXML
    public void handleOptions(ActionEvent actionEvent) {
        if (actionEvent.getSource() == s_closetotray) {
            if (FXTrayIcon.isSupported()) {
                Platform.setImplicitExit(!s_closetotray.isSelected());
                model.save().setBoolean(Defaults.CLOSETOSYSTEMTRAY, s_closetotray.isSelected());
            } else {
                s_closetotray.setSelected(false);
                model.save().setBoolean(Defaults.CLOSETOSYSTEMTRAY, false);
                Alerter.showError("The system tray is not supported on your device.");
                Platform.setImplicitExit(true);
            }
            return;
        }
        if (actionEvent.getSource() == s_skiptooshort) {
            model.save().setBoolean(Defaults.SKIPTOOSHORT, s_skiptooshort.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_autoscroll) {
            model.save().setBoolean(Defaults.AUTOSCROLL, s_autoscroll.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_autoremovecombodupes) {
            model.save().setBoolean(Defaults.AUTOREMOVEDUPE_COMBOS, s_autoremovecombodupes.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_autoremoveproxydupes) {
            model.save().setBoolean(Defaults.AUTOREMOVEDUPE_PROXIES, s_autoremoveproxydupes.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_showhits) {
            model.save().setBoolean(Defaults.SHOWHITS, s_showhits.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_showdebug) {
            model.save().setBoolean(Defaults.SHOWDEBUG, s_showdebug.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_showproxyerrors) {
            model.save().setBoolean(Defaults.SHOWPROXYERRORS, s_showproxyerrors.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_emptycycle) {
            model.save().setBoolean(Defaults.EMPTYCONSOLECYCLE, s_emptycycle.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_consolealert) {
            model.save().setBoolean(Defaults.SHOWCONSOLEALERT, s_consolealert.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_savechecked) {
            model.save().setBoolean(Defaults.SAVECHECKED, s_savechecked.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_savenames) {
            model.save().setBoolean(Defaults.SAVENAMESINFILE, s_savenames.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_checkRealmEye) {
            model.save().setBoolean(Defaults.CHECKREALMEYE, s_checkRealmEye.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_namechosen) {
            model.save().setBoolean(Defaults.NAMECHOSEN, s_namechosen.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_savegold) {
            model.save().setBoolean(Defaults.SAVEGOLDINFILE, s_savegold.isSelected());
            return;
        }
        if (actionEvent.getSource() == s_saveranks) {
            model.save().setBoolean(Defaults.SAVERANKINFILE, s_saveranks.isSelected());
        }
    }

    @FXML
    public void handleClicks(ActionEvent actionEvent) {
        if (actionEvent.getSource() == proxyStartButton) {
            proxyChecker.start();
        } else if (actionEvent.getSource() == btnSaveSettings) {
            model.saveSettings();
            if (!model.save().getBoolean(Defaults.SAVECHECKED) && !model.collects().getCheckedList().isEmpty()) {
                model.collects().getCheckedList().clear();
                model.setChecked(0);
            } else if (model.save().getBoolean(Defaults.SAVECHECKED) && model.collects().getCheckedList().isEmpty()) {
                model.module().loadCheckedHits();
            }
            Toast.makeToast("Settings successfully saved.");
        } else if (actionEvent.getSource() == btnResetSettings) {
            model.resetSettings();
        } else if (actionEvent.getSource() == btnConsole) {
            model.stopConsoleButtonBlink();
            switchPages(Page.CONSOLE);
            pnlConsole.setStyle("-fx-background-color : " + Model.ACTIVE_PAGE_BACKGROUND_COLOR);
            pnlConsole.toFront();
        } else if (actionEvent.getSource() == btnChecker) {
            switchPages(Page.CHECKER);
            pnlChecker.setStyle("-fx-background-color : " + Model.ACTIVE_PAGE_BACKGROUND_COLOR);
            pnlChecker.toFront();
        } else if (actionEvent.getSource() == btnSettings) {
            switchPages(Page.SETTINGS);
            pnlSettings.setStyle("-fx-background-color : " + Model.ACTIVE_PAGE_BACKGROUND_COLOR);
            pnlSettings.toFront();
        } else if (actionEvent.getSource() == btnAbout) {
            switchPages(Page.ABOUT);
            pnlAbout.setStyle("-fx-background-color : " + Model.ACTIVE_PAGE_BACKGROUND_COLOR);
            pnlAbout.toFront();
        } else if (actionEvent.getSource() == btnProxyChecker) {
            switchPages(Page.PROXY);
            table_proxy.refresh();
            pnlProxyChecker.setStyle("-fx-background-color : " + Model.ACTIVE_PAGE_BACKGROUND_COLOR);
            pnlProxyChecker.toFront();
        } else if (actionEvent.getSource() == btnClear) {
            output.clear();
        } else if (actionEvent.getSource() == btnStart) {
            model.module().start();
        } else if (actionEvent.getSource() == btnStop) {
            model.module().stop();
        } else if (actionEvent.getSource() == btnUpdates) {
            model.getUpdateManager().checkUpdates(false);
        }
    }

    @FXML
    private void MenuItemHandler(ActionEvent e) {
        if (e.getSource() instanceof MenuItem) {
            MenuItem item = (MenuItem) e.getSource();
            String item_id = item.getId();
            switch (item_id) {
                case "open_file":
                    proxyChecker.loadFiles();
                    break;
                case "remove_dupes":
                    proxyChecker.removeDuplicates();
                    break;
                case "clear_proxies":
                    proxyChecker.clearProxies();
                    break;
                case "clear_table":
                    if (ProxyCheckCommand.isRunning()) {
                        Toast.makeToast("Please wait for the proxy checker to stop.");
                        return;
                    }
                    if (!table_proxy.getItems().isEmpty()) {
                        progressBarProxy.setProgress(0);
                        label_working_proxies.setText("Working Proxies: 0");
                        table_proxy.getItems().clear();
                    }
                    break;
                case "export_all":
                    proxyChecker.exportAll();
                    break;
                case "export_all_alive":
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, null);
                    break;
                case "export_alive_elite":
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, ProxyAnonymity.ELITE);
                    break;
                case "export_alive_anonymous":
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, ProxyAnonymity.ANONYMOUS);
                    break;
                case "export_alive_transparent":
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, ProxyAnonymity.TRANSPARENT);
                    break;
                case "export_all_dead":
                    ExportCommand.save(table_proxy, ProxyStatus.DEAD, null);
                    break;
                case "export_table":
                    ExportCommand.saveAsTable(table_proxy);
                    break;
                case "preferences":
                    nobility.proxy.Window.show(item_id.substring(0, 1).toUpperCase() + item_id.substring(1),
                            new FXMLLoader(Main.class.getResource("../fx/proxy-settings.fxml")));
                    break;
            }
        }
    }

    private void switchPages(Page page) {
        if (currentPage == page) {
            return;
        }
        String menuPath = String.valueOf(Main.class.getResource("/css/menu.css"));
        String menuSelectedPath = String.valueOf(Main.class.getResource("/css/menu-selected.css"));
        pnlChecker.setVisible(false);
        pnlSettings.setVisible(false);
        pnlConsole.setVisible(false);
        pnlAbout.setVisible(false);
        pnlProxyChecker.setVisible(false);
        btnChecker.getStylesheets().clear();
        btnChecker.getStylesheets().add(menuPath);
        btnSettings.getStylesheets().clear();
        btnSettings.getStylesheets().add(menuPath);
        btnAbout.getStylesheets().clear();
        btnAbout.getStylesheets().add(menuPath);
        btnConsole.getStylesheets().clear();
        btnConsole.getStylesheets().add(menuPath);
        btnProxyChecker.getStylesheets().clear();
        btnProxyChecker.getStylesheets().add(menuPath);
        currentPage = page;
        switch (page) {
            case CONSOLE:
                btnConsole.getStylesheets().clear();
                btnConsole.getStylesheets().add(menuSelectedPath);
                pnlConsole.setVisible(true);
                break;
            case CHECKER:
                btnChecker.getStylesheets().clear();
                btnChecker.getStylesheets().add(menuSelectedPath);
                pnlChecker.setVisible(true);
                break;
            case PROXY:
                btnProxyChecker.getStylesheets().clear();
                btnProxyChecker.getStylesheets().add(menuSelectedPath);
                pnlProxyChecker.setVisible(true);
                break;
            case SETTINGS:
                btnSettings.getStylesheets().clear();
                btnSettings.getStylesheets().add(menuSelectedPath);
                pnlSettings.setVisible(true);
                break;
            case ABOUT:
                btnAbout.getStylesheets().clear();
                btnAbout.getStylesheets().add(menuSelectedPath);
                pnlAbout.setVisible(true);
                break;
        }
    }

    private final Stage itemSearchStage = new Stage();

    @FXML
    private void editItemList() {
        if (!itemSearchStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("../fx/item-search.fxml"));
            Parent layout;
            try {
                layout = loader.load();
                ItemSearchController itemController = loader.getController();
                int width = 300;
                int height = 500;
                Scene scene = new Scene(layout, width, height);
                itemSearchStage.getIcons().add(new Image(Model.ICON));
                itemController.setDefault(model.getItemParser(), itemSearchStage);
                itemSearchStage.setTitle("Edit Item List");
                itemSearchStage.setResizable(true);
                itemSearchStage.setScene(scene);
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                itemSearchStage.setX((screenBounds.getWidth() - width) / 2);
                itemSearchStage.setY((screenBounds.getHeight() - height) / 2);
                itemSearchStage.show();
                itemSearchStage.setOnCloseRequest((event) -> model.getItemParser().saveSelected());
            } catch (IOException e) {
                Toast.makeToast("Failed to open item list. Error: " + e.getLocalizedMessage());
            }
        }
    }

    private void setMenuItems() {
        MenuItem loadCombos = new MenuItem("Load Combos");
        loadCombos.setOnAction(e -> model.module().loadComboList());
        MenuItem removeComboDuplicates = new MenuItem("Remove Combo Duplicates");
        removeComboDuplicates.setOnAction(e -> model.module().removeListDuplicates(0));

        menuCombos.getItems().clear();
        menuCombos.getItems().addAll(loadCombos, removeComboDuplicates);

        MenuItem loadProxies = new MenuItem("Load Proxies");
        loadProxies.setOnAction(e -> model.module().loadProxyList());
        MenuItem clearProxies = new MenuItem("Clear Proxies");
        clearProxies.setOnAction(e -> {
            model.collects().getProxies().clear();
            model.collects().getBackupProxies().clear();
            model.setProxies(0);
        });
        MenuItem removeProxyDuplicates = new MenuItem("Remove Proxy Duplicates");
        removeProxyDuplicates.setOnAction(e -> model.module().removeListDuplicates(1));

        menuProxies.getItems().clear();
        menuProxies.getItems().addAll(loadProxies, clearProxies, removeProxyDuplicates);

        MenuItem clearHits = new MenuItem("Clear Hits Console");
        clearHits.setOnAction(e -> hitsOutput.clear());
        MenuItem editItems = new MenuItem("Edit Item List");
        editItems.setOnAction(e -> editItemList());
        MenuItem showResults = new MenuItem("Show Results");
        showResults.setOnAction(e -> model.module().openResultsFolder());

        menuTools.getItems().clear();
        menuTools.getItems().addAll(clearHits, editItems, showResults);
    }

    private void setModelComponents() {
        model.getFxModel().setBtnConsole(btnConsole);
        model.getFxModel().setPnlConsole(pnlConsole);
        model.getFxModel().setLblRetries(lblRetries);
        model.getFxModel().setLblCpm(lblCpm);
        model.getFxModel().setLblRTCpm(lblRTCpm);
        model.getFxModel().setLblErrors(lblErrors);
        model.getFxModel().setLblProxies(lblProxies);
        model.getFxModel().setLblHits(lblHits);
        model.getFxModel().setLblHQHits(lblHQHits);
        model.getFxModel().setLblComboName(lblComboName);
        model.getFxModel().setLblComboProgress(lblComboProgress);
        model.getFxModel().setLblChecked(lblChecked);
        model.getFxModel().setLblTime(lblTime);
        model.getFxModel().setLblInvalid(lblInvalid);
        model.getFxModel().setLblThread(lblThread);
        model.getFxModel().setCpu(cpu);
        model.getFxModel().setRam(ram);

        model.getFxModel().setBtnStart(btnStart);
        model.getFxModel().setBtnStop(btnStop);

        model.getFxModel().setHitsOutput(hitsOutput);
        model.getFxModel().setProgressBarCombo(progressBarChecker);
        model.getFxModel().setSldComboLine(sldComboLine);

        model.getFxModel().setS_autoremovecombodupes(s_autoremovecombodupes);
        model.getFxModel().setS_autoremoveproxydupes(s_autoremoveproxydupes);
        model.getFxModel().setS_showhits(s_showhits);
        model.getFxModel().setS_showdebug(s_showdebug);
        model.getFxModel().setS_emptycycle(s_emptycycle);
        model.getFxModel().setS_consolealert(s_consolealert);
        model.getFxModel().setS_savegold(s_savegold);
        model.getFxModel().setS_saveranks(s_saveranks);
        model.getFxModel().setS_showproxyerrors(s_showproxyerrors);
        model.getFxModel().setS_closetotray(s_closetotray);
        model.getFxModel().setS_autoscroll(s_autoscroll);
        model.getFxModel().setS_skiptooshort(s_skiptooshort);

        model.getFxModel().setTfRank(tfRank);
        model.getFxModel().setTfGold(tfGold);
        model.getFxModel().setTfFame(tfFame);
        model.getFxModel().setTfMaxCharacters(tfMaxCharacters);
        model.getFxModel().setTfRealmeyeRetries(tfRealmeyeRetries);
        model.getFxModel().setThreads(threads);
        model.getFxModel().setTimeout(timeout);
        model.getFxModel().setRetries(retries);

        model.getFxModel().setChbxSeperator(chbxSeperator);
        model.getFxModel().setChbxPetRarity(chbxPetRarity);
        model.getFxModel().setChbxProxyType(chbxProxyType);

        model.getFxModel().setS_autoremovecombodupes(s_autoremovecombodupes);
        model.getFxModel().setS_autoremoveproxydupes(s_autoremoveproxydupes);
        model.getFxModel().setS_showhits(s_showhits);
        model.getFxModel().setS_showdebug(s_showdebug);
        model.getFxModel().setS_emptycycle(s_emptycycle);
        model.getFxModel().setS_consolealert(s_consolealert);
        model.getFxModel().setS_savechecked(s_savechecked);
        model.getFxModel().setS_savenames(s_savenames);
        model.getFxModel().setS_checkRealmEye(s_checkRealmEye);
        model.getFxModel().setS_namechosen(s_namechosen);
        model.getFxModel().setS_savegold(s_savegold);
        model.getFxModel().setS_saveranks(s_saveranks);
        model.getFxModel().setS_showproxyerrors(s_showproxyerrors);
        model.getFxModel().setS_closetotray(s_closetotray);
        model.getFxModel().setS_autoscroll(s_autoscroll);
        model.getFxModel().setS_skiptooshort(s_skiptooshort);
    }

    private void initializeUi() {
        setupProxyChecker();
        progressBarChecker.setMaxWidth(Double.MAX_VALUE);
        progressVbox.setFillWidth(true);
        btnConsole.getStylesheets().clear();
        btnConsole.getStylesheets().add(String.valueOf(Main.class.getResource("menu-selected.css")));
        btnStop.setDisable(true);
        model.setOptionBoxes();
        model.setTextFields();
        sldComboLine.setDisable(true);
        sldComboLine.setMax(0);
        sldComboLine.setOnMousePressed(event -> sliding = true);
        sldComboLine.setOnMouseReleased(event -> sliding = false);
        sldComboLine.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (sliding) {
                model.collects().setComboProgress(newValue.intValue());
                model.module().settings().setProgress(newValue.intValue());
                model.updateProgress();
            }
        });
        threads.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                threads.setText(newValue.replaceAll("[^\\d]", "200"));
            if (threads.getText().isEmpty()) {
                model.save().setInteger(Defaults.CHECKERTHREADS, 200);
            } else {
                if (Integer.parseInt(newValue) > 1000) {
                    model.save().setInteger(Defaults.CHECKERTHREADS, 1000);
                    threads.setText("1000");
                    return;
                }
                model.save().setInteger(Defaults.CHECKERTHREADS, Integer.parseInt(newValue));
            }
        });
        timeout.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                timeout.setText(newValue.replaceAll("[^\\d]", "10"));
            if (timeout.getText().isEmpty()) {
                model.save().setInteger(Defaults.CHECKERTIMEOUT, 10);
            } else {
                if (Integer.parseInt(newValue) > 60) {
                    model.save().setInteger(Defaults.CHECKERTIMEOUT, 60);
                    timeout.setText("60");
                    return;
                }
                model.save().setInteger(Defaults.CHECKERTIMEOUT, Integer.parseInt(newValue));
            }
        });
        retries.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                retries.setText(newValue.replaceAll("[^\\d]", "2"));
            if (retries.getText().isEmpty()) {
                model.save().setInteger(Defaults.CHECKERRETRIES, 2);
            } else {
                if (Integer.parseInt(newValue) > 10) {
                    model.save().setInteger(Defaults.CHECKERRETRIES, 10);
                    retries.setText("10");
                    return;
                }
                model.save().setInteger(Defaults.CHECKERRETRIES, Integer.parseInt(newValue));
            }
        });
        tfRank.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                tfRank.setText(newValue.replaceAll("[^\\d]", "30"));
            if (tfRank.getText().isEmpty()) {
                model.save().setInteger(Defaults.HQRANK, 30);
            } else {
                if (Integer.parseInt(newValue) > 80) {
                    model.save().setInteger(Defaults.HQRANK, 80);
                    tfRank.setText("80");
                    return;
                }
                model.save().setInteger(Defaults.HQRANK, Integer.parseInt(newValue));
            }
        });
        tfGold.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                tfGold.setText(newValue.replaceAll("[^\\d]", "1000"));
            if (tfGold.getText().isEmpty()) {
                model.save().setInteger(Defaults.HQGOLD, 1000);
            } else {
                if (Integer.parseInt(newValue) > 10_000) {
                    model.save().setInteger(Defaults.HQGOLD, 10_000);
                    tfGold.setText("10000");
                    return;
                }
                model.save().setInteger(Defaults.HQGOLD, Integer.parseInt(newValue));
            }
        });
        tfFame.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                tfFame.setText(newValue.replaceAll("[^\\d]", "10000"));
            if (tfFame.getText().isEmpty()) {
                model.save().setInteger(Defaults.HQFAME, 10_000);
            } else {
                if (Integer.parseInt(newValue) > 50_000) {
                    model.save().setInteger(Defaults.HQFAME, 50_000);
                    tfFame.setText("50000");
                    return;
                }
                model.save().setInteger(Defaults.HQFAME, Integer.parseInt(newValue));
            }
        });
        tfMaxCharacters.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                tfMaxCharacters.setText(newValue.replaceAll("[^\\d]", "0"));
            if (tfMaxCharacters.getText().isEmpty()) {
                model.save().setInteger(Defaults.HQCHARS, 0);
            } else {
                if (Integer.parseInt(newValue) > 15) {
                    model.save().setInteger(Defaults.HQCHARS, 15);
                    tfMaxCharacters.setText("15");
                    return;
                }
                model.save().setInteger(Defaults.HQCHARS, Integer.parseInt(newValue));
            }
        });
        tfRealmeyeRetries.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                tfRealmeyeRetries.setText(newValue.replaceAll("[^\\d]", "0"));
            if (tfRealmeyeRetries.getText().isEmpty()) {
                model.save().setInteger(Defaults.REALMEYERETRIES, 0);
            } else {
                if (Integer.parseInt(newValue) > 50) {
                    model.save().setInteger(Defaults.REALMEYERETRIES, 50);
                    tfRealmeyeRetries.setText("50");
                    return;
                }
                model.save().setInteger(Defaults.REALMEYERETRIES, Integer.parseInt(newValue));
            }
        });
        chbxPetRarity.getItems().addAll("None", "Rare", "Legendary", "Divine");
        chbxPetRarity.addEventHandler(ActionEvent.ACTION, event -> model.save().setString(Defaults.HQPETRARITY, chbxPetRarity.getValue()));
        chbxSeperator.getItems().addAll(":", ";", "-", "_", ",", ".");
        chbxSeperator.addEventHandler(ActionEvent.ACTION, event -> model.save().setString(Defaults.SEPERATOR, chbxSeperator.getValue()));
        chbxProxyType.getItems().addAll("HTTP(S)", "SOCKS");
        chbxProxyType.addEventHandler(ActionEvent.ACTION, event -> model.save().setBoolean(Defaults.SOCKS, chbxProxyType.getValue().equals("SOCKS")));
        model.setExtraOptions();
        model.setupMenuIcon();
    }

    private void setupProxyChecker() {
        table_proxy.setPlaceholder(new Label(""));
        model.getMainStage().widthProperty().addListener(((observable, oldValue, newValue) -> {
            if (currentPage == Page.PROXY) {
                table_proxy.refresh();
            }
        }));
        table_proxy.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        //must total 100
        column_ip.setMaxWidth(1f * Integer.MAX_VALUE * 17);
        column_port.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        column_status.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        column_country.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        column_anonymity.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        column_response_time.setMaxWidth(1f * Integer.MAX_VALUE * 13);
        column_type.setMaxWidth(1f * Integer.MAX_VALUE * 7);

        label_loaded_proxies.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        label_checked_proxies.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        label_working_proxies.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        label_ip_address.setMaxWidth(1f * Integer.MAX_VALUE * 20);
        progressBarProxy.setMaxWidth(1f * Integer.MAX_VALUE * 20);
    }

    public Model getModel() {
        return model;
    }

    public TextArea getOut() {
        return out;
    }

}
