package nobility.model;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import nobility.tools.TextAreaAutoScroll;

public class FXModel {

    private Button btnConsole;

    public void setBtnConsole(Button btnConsole) {
        this.btnConsole = btnConsole;
    }

    public Button getBtnConsole() {
        return btnConsole;
    }

    private Pane pnlConsole;

    public void setPnlConsole(Pane pnlConsole) {
        this.pnlConsole = pnlConsole;
    }

    public Pane getPnlConsole() {
        return pnlConsole;
    }

    private Label lblRetries, lblCpm, lblRTCpm, lblErrors, lblProxies, lblHits, lblHQHits,
            lblComboName, lblComboProgress, lblChecked, lblTime, lblInvalid, lblThread, cpu, ram;

    public void setLblRetries(Label lblRetries) {
        this.lblRetries = lblRetries;
    }

    public Label getLblRetries() {
        return lblRetries;
    }

    public void setLblCpm(Label lblCpm) {
        this.lblCpm = lblCpm;
    }

    public Label getLblCpm() {
        return lblCpm;
    }

    public void setLblRTCpm(Label lblRTCpm) {
        this.lblRTCpm = lblRTCpm;
    }

    public Label getLblRTCpm() {
        return lblRTCpm;
    }

    public void setLblErrors(Label lblErrors) {
        this.lblErrors = lblErrors;
    }

    public Label getLblErrors() {
        return lblErrors;
    }

    public void setLblProxies(Label lblProxies) {
        this.lblProxies = lblProxies;
    }

    public Label getLblProxies() {
        return lblProxies;
    }

    public void setLblHits(Label lblHits) {
        this.lblHits = lblHits;
    }

    public Label getLblHits() {
        return lblHits;
    }

    public void setLblHQHits(Label lblHQHits) {
        this.lblHQHits = lblHQHits;
    }

    public Label getLblHQHits() {
        return lblHQHits;
    }

    public void setLblComboName(Label lblComboName) {
        this.lblComboName = lblComboName;
    }

    public Label getLblComboName() {
        return lblComboName;
    }

    public void setLblComboProgress(Label lblComboProgress) {
        this.lblComboProgress = lblComboProgress;
    }

    public Label getLblComboProgress() {
        return lblComboProgress;
    }

    public void setLblChecked(Label lblChecked) {
        this.lblChecked = lblChecked;
    }

    public Label getLblChecked() {
        return lblChecked;
    }

    public Label getLblTime() {
        return lblTime;
    }

    public void setLblTime(Label lblTime) {
        this.lblTime = lblTime;
    }

    public void setLblInvalid(Label lblInvalid) {
        this.lblInvalid = lblInvalid;
    }

    public Label getLblInvalid() {
        return lblInvalid;
    }

    public void setLblThread(Label lblThread) {
        this.lblThread = lblThread;
    }

    public Label getLblThread() {
        return lblThread;
    }

    public void setCpu(Label cpu) {
        this.cpu = cpu;
    }

    public Label getCpu() {
        return cpu;
    }

    public void setRam(Label ram) {
        this.ram = ram;
    }

    public Label getRam() {
        return ram;
    }

    private Button btnStart, btnStop;

    public void setBtnStart(Button btnStart) {
        this.btnStart = btnStart;
    }

    public Button getBtnStart() {
        return btnStart;
    }

    public void setBtnStop(Button btnStop) {
        this.btnStop = btnStop;
    }

    public Button getBtnStop() {
        return btnStop;
    }

    public void setStartButtons() {
        Platform.runLater(() -> {
            threads.setDisable(true);
            btnStart.setDisable(true);
            btnStop.setDisable(false);
        });
    }

    public void setStopButtons() {
        Platform.runLater(() -> {
            threads.setDisable(false);
            btnStart.setDisable(false);
            btnStop.setDisable(true);
        });
    }

    private TextAreaAutoScroll hitsOutput, out;

    public void setHitsOutput(TextAreaAutoScroll hitsOutput) {
        this.hitsOutput = hitsOutput;
    }

    public TextAreaAutoScroll getHitsOutput() {
        return hitsOutput;
    }

    public void setOut(TextAreaAutoScroll out) {
        this.out = out;
    }

    public TextAreaAutoScroll getOut() {
        return out;
    }

    private ProgressBar progressBarCombo;

    public void setProgressBarCombo(ProgressBar progressBarCombo) {
        this.progressBarCombo = progressBarCombo;
    }

    public ProgressBar getProgressBarCombo() {
        return progressBarCombo;
    }

    private Slider sldComboLine;

    public void setSldComboLine(Slider sldComboLine) {
        this.sldComboLine = sldComboLine;
    }

    public Slider getSldComboLine() {
        return sldComboLine;
    }

    private ChoiceBox<String> chbxSeperator, chbxPetRarity, chbxProxyType;

    public void setChbxSeperator(ChoiceBox<String> chbxSeperator) {
        this.chbxSeperator = chbxSeperator;
    }

    public ChoiceBox<String> getChbxSeperator() {
        return chbxSeperator;
    }

    public void setChbxPetRarity(ChoiceBox<String> chbxPetRarity) {
        this.chbxPetRarity = chbxPetRarity;
    }

    public ChoiceBox<String> getChbxPetRarity() {
        return chbxPetRarity;
    }

    public void setChbxProxyType(ChoiceBox<String> chbxProxyType) {
        this.chbxProxyType = chbxProxyType;
    }

    public ChoiceBox<String> getChbxProxyType() {
        return chbxProxyType;
    }

    private TextField tfRank, tfGold, tfFame,
            tfMaxCharacters, tfRealmeyeRetries;
    private TextField threads, timeout, retries;

    public void setThreads(TextField threads) {
        this.threads = threads;
    }

    public TextField getThreads() {
        return threads;
    }

    public void setTimeout(TextField timeout) {
        this.timeout = timeout;
    }

    public TextField getTimeout() {
        return timeout;
    }

    public void setRetries(TextField retries) {
        this.retries = retries;
    }

    public TextField getRetries() {
        return retries;
    }

    public void setTfRank(TextField tfRank) {
        this.tfRank = tfRank;
    }

    public TextField getTfRank() {
        return tfRank;
    }

    public void setTfGold(TextField tfGold) {
        this.tfGold = tfGold;
    }

    public TextField getTfGold() {
        return tfGold;
    }

    public void setTfFame(TextField tfFame) {
        this.tfFame = tfFame;
    }

    public TextField getTfFame() {
        return tfFame;
    }

    public void setTfMaxCharacters(TextField tfMaxCharacters) {
        this.tfMaxCharacters = tfMaxCharacters;
    }

    public TextField getTfMaxCharacters() {
        return tfMaxCharacters;
    }

    public void setTfRealmeyeRetries(TextField tfRealmeyeRetries) {
        this.tfRealmeyeRetries = tfRealmeyeRetries;
    }

    public TextField getTfRealmeyeRetries() {
        return tfRealmeyeRetries;
    }

    @FXML private CheckBox s_autoremovecombodupes, s_autoremoveproxydupes, s_showhits, s_showdebug, s_emptycycle,
            s_consolealert, s_savechecked, s_savenames, s_checkRealmEye,
            s_namechosen, s_savegold, s_saveranks, s_showproxyerrors,
            s_closetotray, s_autoscroll, s_skiptooshort;

    public void setS_autoremovecombodupes(CheckBox s_autoremovecombodupes) {
        this.s_autoremovecombodupes = s_autoremovecombodupes;
    }

    public CheckBox getS_autoremovecombodupes() {
        return s_autoremovecombodupes;
    }

    public void setS_autoremoveproxydupes(CheckBox s_autoremoveproxydupes) {
        this.s_autoremoveproxydupes = s_autoremoveproxydupes;
    }

    public CheckBox getS_autoremoveproxydupes() {
        return s_autoremoveproxydupes;
    }

    public void setS_showhits(CheckBox s_showhits) {
        this.s_showhits = s_showhits;
    }

    public CheckBox getS_showhits() {
        return s_showhits;
    }

    public void setS_showdebug(CheckBox s_showdebug) {
        this.s_showdebug = s_showdebug;
    }

    public CheckBox getS_showdebug() {
        return s_showdebug;
    }

    public void setS_emptycycle(CheckBox s_emptycycle) {
        this.s_emptycycle = s_emptycycle;
    }

    public CheckBox getS_emptycycle() {
        return s_emptycycle;
    }

    public void setS_consolealert(CheckBox s_consolealert) {
        this.s_consolealert = s_consolealert;
    }

    public CheckBox getS_consolealert() {
        return s_consolealert;
    }

    public void setS_savechecked(CheckBox s_savechecked) {
        this.s_savechecked = s_savechecked;
    }

    public CheckBox getS_savechecked() {
        return s_savechecked;
    }

    public void setS_savenames(CheckBox s_savenames) {
        this.s_savenames = s_savenames;
    }

    public CheckBox getS_savenames() {
        return s_savenames;
    }

    public void setS_namechosen(CheckBox s_namechosen) {
        this.s_namechosen = s_namechosen;
    }

    public CheckBox getS_namechosen() {
        return s_namechosen;
    }

    public void setS_savegold(CheckBox s_savegold) {
        this.s_savegold = s_savegold;
    }

    public CheckBox getS_savegold() {
        return s_savegold;
    }

    public void setS_saveranks(CheckBox s_saveranks) {
        this.s_saveranks = s_saveranks;
    }

    public CheckBox getS_saveranks() {
        return s_saveranks;
    }

    public void setS_showproxyerrors(CheckBox s_showproxyerrors) {
        this.s_showproxyerrors = s_showproxyerrors;
    }

    public CheckBox getS_showproxyerrors() {
        return s_showproxyerrors;
    }

    public void setS_checkRealmEye(CheckBox s_checkRealmEye) {
        this.s_checkRealmEye = s_checkRealmEye;
    }

    public CheckBox getS_checkRealmEye() {
        return s_checkRealmEye;
    }

    public void setS_closetotray(CheckBox s_closetotray) {
        this.s_closetotray = s_closetotray;
    }

    public CheckBox getS_closetotray() {
        return s_closetotray;
    }

    public void setS_autoscroll(CheckBox s_autoscroll) {
        this.s_autoscroll = s_autoscroll;
    }

    public CheckBox getS_autoscroll() {
        return s_autoscroll;
    }

    public void setS_skiptooshort(CheckBox s_skiptooshort) {
        this.s_skiptooshort = s_skiptooshort;
    }

    public CheckBox getS_skiptooshort() {
        return s_skiptooshort;
    }
}
