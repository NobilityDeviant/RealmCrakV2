package nobility;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import nobility.model.Model;
import nobility.proxy.AlertBox;
import nobility.proxy.ProxyChecker;
import nobility.proxy.commands.ExportCommand;
import nobility.proxy.commands.ProxyCheckCommand;
import nobility.proxy.components.entities.Proxy;
import nobility.proxy.components.entities.ProxyAnonymity;
import nobility.proxy.components.entities.ProxyStatus;
import nobility.save.Defaults;
import nobility.tools.TextAreaAutoScroll;
import nobility.tools.TextOutput;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML private TextAreaAutoScroll out, hitsOutput;
    @FXML private Button btnConsole, btnClear, btnClearHits,
            btnAbout, btnChecker, btnSettings, btnProxyChecker; //menu and console
    @FXML private Button btnSaveSettings, btnResetSettings; //settings
    @FXML private Button btnLoadCombo, btnRemoveComboDupes, btnLoadProxies, btnRemoveProxyDupes,
                btnStart, btnStop, btnEditItems, btnResults, btnClearProxies; //checker
    @FXML private Button btnUpdates;
    @FXML private Pane pnlChecker, pnlSettings, pnlConsole, pnlAbout, pnlProxyChecker;
    //@FXML private ImageView profile_image;
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
    @FXML private ProgressBar progressBarCombo;
    @FXML private Slider sldComboLine;
    @FXML private ChoiceBox<String> chbxSeperator, chbxPetRarity, chbxProxyType;

    private TextOutput output;
    private boolean sliding = false;

    private ProxyChecker proxyChecker;
    @FXML public TableView<Proxy> table_proxy = new TableView<>();
    //@FXML private ListView<String> view_loaded_proxies = new ListView<>();

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

    @FXML public ProgressBar progressBar;

    @FXML public Button button_check;

    private final Stage loginStage = new Stage();
    private final Stage confirmStage = new Stage();
    private final Model model;

    //-fx-font-family:'serif';

    public Controller(Model model) {
        this.model = model;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        output = new TextOutput(out, model);
        model.getFxModel().setOut(out);
        System.setOut(new PrintStream(output));

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
        model.getFxModel().setProgressBarCombo(progressBarCombo);
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
        pnlAbout.setVisible(false);
        pnlChecker.setVisible(false);
        pnlSettings.setVisible(false);
        pnlProxyChecker.setVisible(false);
        pnlConsole.setVisible(true);
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
                //System.out.println("Progress: " + newValue.intValue());
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
        lblVersion.setText("Version: " + model.getUpdateManager().getVersion());
        boolean[] latest = model.getUpdateManager().isLatestVersion();
        String version = model.getUpdateManager().getLatestVersion();
        if (!model.save().getString(Defaults.UPDATEVERSION).equalsIgnoreCase(version)) {
            model.save().setBoolean(Defaults.DENIEDUPDATE, false);
            model.saveSettings();
        }
        if (!latest[0]) {
            if (latest[1]) {
                model.save().setString(Defaults.UPDATEVERSION, version);
                model.saveSettings();
                showUpdateConfirm("Update Available - v" + version + " - Required", true);
            } else {
                if (!model.save().getBoolean(Defaults.DENIEDUPDATE)) {
                    model.save().setString(Defaults.UPDATEVERSION, version);
                    model.saveSettings();
                    showUpdateConfirm("Update Available - v" + version,false);
                }
            }
        }
        if (!latest[0] && latest[1]) {
            model.showError("You must update your client to continue. Shutting down...");
            System.exit(0);
        }
        model.getMainStage().show();
        //openLogin();
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
                model.showError("The system tray is not supported on your device.");
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
        if (actionEvent.getSource() == button_check) {
            if (model.isFreetrialMode() && model.getDatabase().freeTrialEnded(model.getSavedKey())) {
                model.showError("Your free trial has ended.");
                return;
            }
            proxyChecker.start();
            return;
        }
        if (actionEvent.getSource() == btnSaveSettings) {
            model.saveSettings();
            if (!model.save().getBoolean(Defaults.SAVECHECKED) && !model.collects().getCheckedList().isEmpty()) {
                model.collects().getCheckedList().clear();
                model.setChecked(0);
            } else if (model.save().getBoolean(Defaults.SAVECHECKED) && model.collects().getCheckedList().isEmpty()) {
                model.module().loadCheckedHits();
            }
            System.out.println("Settings successfully saved.");
            return;
        }
        if (actionEvent.getSource() == btnResetSettings) {
            model.resetSettings();
            return;
        }
        if (actionEvent.getSource() == btnConsole) {
            model.stopConsoleButtonBlink();
            switchPages(0);
            pnlConsole.setStyle("-fx-background-color : #02030A");
            pnlConsole.toFront();
            return;
        }
        if (actionEvent.getSource()== btnChecker) {
            switchPages(1);
            pnlChecker.setStyle("-fx-background-color : #02030A");
            pnlChecker.toFront();
            return;
        }
        if (actionEvent.getSource()== btnSettings) {
            switchPages(2);
            pnlSettings.setStyle("-fx-background-color : #02030A");
            pnlSettings.toFront();
            return;
        }
        if (actionEvent.getSource() == btnAbout) {
            switchPages(3);
            pnlAbout.setStyle("-fx-background-color : #02030A");
            pnlAbout.toFront();
            return;
        }
        if (actionEvent.getSource() == btnProxyChecker) {
            switchPages(4);
            pnlProxyChecker.setStyle("-fx-background-color : #02030A");
            pnlProxyChecker.toFront();
            return;
        }
        if (actionEvent.getSource() == btnClear) {
            output.clear();
            return;
        }
        if (actionEvent.getSource() == btnClearHits) {
            hitsOutput.clear();
            return;
        }
        if (actionEvent.getSource() == btnLoadCombo) {
            model.module().loadComboList();
            return;
        }
        if (actionEvent.getSource() == btnRemoveComboDupes) {
            model.module().removeListDuplicates(0);
            return;
        }
        if (actionEvent.getSource() == btnLoadProxies) {
            model.module().loadProxyList();
            return;
        }
        if (actionEvent.getSource() == btnRemoveProxyDupes) {
            model.module().removeListDuplicates(1);
            return;
        }
        if (actionEvent.getSource() == btnStart) {
            model.module().start();
            return;
        }
        if (actionEvent.getSource() == btnStop) {
            model.module().stop();
            return;
        }
        if (actionEvent.getSource() == btnEditItems) {
            editItemList();
            return;
        }
        if (actionEvent.getSource() == btnUpdates) {
            showUpdateConfirm("Check For Updates", false);
            return;
        }
        if (actionEvent.getSource() == btnResults) {
            model.module().openResultsFolder();
            return;
        }
        if (actionEvent.getSource() == btnClearProxies) {
            model.collects().getProxies().clear();
            model.collects().getBackupProxies().clear();
            model.setProxies(0);
        }
    }

    @FXML
    private void MenuItemHandler(ActionEvent e) {
        if(e.getSource() instanceof MenuItem) {
            MenuItem item = (MenuItem) e.getSource();
            String item_id = item.getId();
            switch (item_id) {
                case "open_file":
                    proxyChecker.loadFiles();
                    break;
                case "remove_dupes":
                    proxyChecker.removeDupes();
                    break;
                case "clear_proxies":
                    proxyChecker.clearProxies();
                    break;
                case "clear_table":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    if (!table_proxy.getItems().isEmpty()) {
                        progressBar.setProgress(0);
                        label_working_proxies.setText("Working Proxies: 0"); // reset working proxy count
                        table_proxy.getItems().clear();
                    }
                    break;
                case "export_all":
                    proxyChecker.exportAll();
                    break;
                case "export_all_alive":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, null);
                    break;
                case "export_alive_elite":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, ProxyAnonymity.ELITE);
                    break;
                case "export_alive_anonymous":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, ProxyAnonymity.ANONYMOUS);
                    break;
                case "export_alive_transparent":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    ExportCommand.save(table_proxy, ProxyStatus.ALIVE, ProxyAnonymity.TRANSPARENT);
                    break;
                case "export_all_dead":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    ExportCommand.save(table_proxy, ProxyStatus.DEAD, null);
                    break;
                case "export_table":
                    if (ProxyCheckCommand.isRunning()) {
                        AlertBox.show(Alert.AlertType.ERROR, "Checker is running!", "Please wait for the checker to stop.");
                        return;
                    }
                    ExportCommand.saveAsTable(table_proxy);
                    break;
                case "preferences":
                    nobility.proxy.Window.show(item_id.substring(0, 1).toUpperCase() + item_id.substring(1),
                            new FXMLLoader(Main.class.getResource("Settings.fxml")));
                    break;
            }
        }
    }

    private void switchPages(int page) {
        pnlChecker.setVisible(false);
        pnlSettings.setVisible(false);
        pnlConsole.setVisible(false);
        pnlAbout.setVisible(false);
        pnlProxyChecker.setVisible(false);
        btnChecker.getStylesheets().clear();
        btnChecker.getStylesheets().add(String.valueOf(Main.class.getResource("menu.css")));
        btnSettings.getStylesheets().clear();
        btnSettings.getStylesheets().add(String.valueOf(Main.class.getResource("menu.css")));
        btnAbout.getStylesheets().clear();
        btnAbout.getStylesheets().add(String.valueOf(Main.class.getResource("menu.css")));
        btnConsole.getStylesheets().clear();
        btnConsole.getStylesheets().add(String.valueOf(Main.class.getResource("menu.css")));
        btnProxyChecker.getStylesheets().clear();
        btnProxyChecker.getStylesheets().add(String.valueOf(Main.class.getResource("menu.css")));
        switch (page) {
            case 0:
                btnConsole.getStylesheets().clear();
                btnConsole.getStylesheets().add(String.valueOf(Main.class.getResource("menu-selected.css")));
                pnlConsole.setVisible(true);
            break;
            case 1:
                btnChecker.getStylesheets().clear();
                btnChecker.getStylesheets().add(String.valueOf(Main.class.getResource("menu-selected.css")));
                pnlChecker.setVisible(true);
            break;
            case 2:
                btnSettings.getStylesheets().clear();
                btnSettings.getStylesheets().add(String.valueOf(Main.class.getResource("menu-selected.css")));
                pnlSettings.setVisible(true);
            break;
            case 3:
                btnAbout.getStylesheets().clear();
                btnAbout.getStylesheets().add(String.valueOf(Main.class.getResource("menu-selected.css")));
                pnlAbout.setVisible(true);
            break;
            case 4:
                btnProxyChecker.getStylesheets().clear();
                btnProxyChecker.getStylesheets().add(String.valueOf(Main.class.getResource("menu-selected.css")));
                pnlProxyChecker.setVisible(true);
            break;
        }
    }

    private final Stage ItemSearchStage = new Stage();

    @FXML
    private void editItemList() {
        if (!ItemSearchStage.isShowing()) {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("itemsearch.fxml"));
            Parent layout;
            try {
                layout = loader.load();
                ItemSearchController itemController = loader.getController();
                Scene scene = new Scene(layout);
                ItemSearchStage.getIcons().add(new Image(String.valueOf(Main.class.getResource("icon.png"))));
                itemController.setDefault(model.getItemParser());
                ItemSearchStage.setTitle("Edit Item List");
                ItemSearchStage.setResizable(false);
                ItemSearchStage.setScene(scene);
                ItemSearchStage.sizeToScene();
                ItemSearchStage.show();
                ItemSearchStage.setOnCloseRequest((event) -> model.getItemParser().saveSelected());
            } catch (IOException var5) {
                System.out.println("Item Search Error: " + var5.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    public void openLogin() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("login.fxml"));
        loader.setControllerFactory((Class<?> controllerType) -> {
            try {
                for (Constructor<?> con : controllerType.getConstructors()) {
                    if (con.getParameterCount() == 1 && con.getParameterTypes()[0] == Model.class) {
                        return con.newInstance(model);
                    }
                }
                return controllerType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(-1);
                return null;
            }
        });
        Parent layout;
        try {
            layout = loader.load();
            final LoginController windowController = loader.getController();
            Scene scene = new Scene(layout);
            windowController.setStage(loginStage);
            loginStage.toFront();
            loginStage.initModality(Modality.APPLICATION_MODAL);
            loginStage.initOwner(model.getMainStage());
            loginStage.setTitle("RealmCrakV2 Login");
            loginStage.getIcons().add(new Image(String.valueOf(Main.class.getResource("icon.png"))));
            loginStage.setResizable(false);
            loginStage.setScene(scene);
            loginStage.sizeToScene();
            loginStage.initStyle(StageStyle.DECORATED);
            loginStage.setOnCloseRequest(e -> System.exit(0));
            loginStage.showAndWait();
        }
        catch (IOException e2) {
            System.out.println("Validation Error: " + e2.getMessage());
            System.exit(-1);
        }
    }

    @FXML
    private void showUpdateConfirm(String title, boolean required) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("confirm.fxml"));
        loader.setControllerFactory((Class<?> controllerType) -> {
            try {
                for (Constructor<?> con : controllerType.getConstructors()) {
                    if (con.getParameterCount() == 1 && con.getParameterTypes()[0] == Model.class) {
                        return con.newInstance(model);
                    }
                }
                return controllerType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(-1);
                return null;
            }
        });
        try {
            Parent root = loader.load();
            ConfirmController confirmController = loader.getController();
            Scene scene = new Scene(root);
            confirmController.setStage(confirmStage, required);
            confirmStage.getIcons().add(new Image(String.valueOf(Main.class.getResource("icon.png"))));
            confirmStage.sizeToScene();
            confirmStage.setTitle(title);
            confirmStage.setResizable(false);
            confirmStage.setScene(scene);
            confirmStage.setOnCloseRequest(event -> confirmController.close());
            confirmStage.showAndWait();
        } catch (IOException var5) {
            System.out.println("Message Error: " + var5.getMessage());
        }
    }

    public TextArea getOut() {
        return out;
    }

}
