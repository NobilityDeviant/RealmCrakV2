package nobility.handler.settings;

import javafx.application.Platform;
import nobility.model.Model;

public class Settings {

    private String loadedComboName;
    private String loadedComboPath;
    private int comboSize;
    private int cpm;
    private int errors;
    private int retries;
    private volatile int invalid;
    private volatile int threads;
    private boolean running;
    private boolean stopping;
    private volatile int progress;
    private volatile int lowQualityHits, highQualityHits;
    private final Model model;

    public Settings(Model model) {
        this.model = model;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getInvalid() {
        return invalid;
    }

    public synchronized void addInvalid() {
        progress++;
        invalid++;
        cpm++;
    }

    public void setInvalid(int invalid) {
        this.invalid =  invalid;
        model.setInvalid(invalid);
    }

    public void setLowQualityHits(int lowQualityHits) {
        this.lowQualityHits = lowQualityHits;

        Platform.runLater(() -> model.setHits(lowQualityHits));
    }

    public void setHighQualityHits(int highQualityHits) {
        this.highQualityHits = highQualityHits;
        Platform.runLater(() -> model.setHQHits(highQualityHits));
    }

    public synchronized void addLowQualityHit() {
        lowQualityHits++;
        progress++;
        cpm++;
        Platform.runLater(() -> model.setHits(lowQualityHits));
    }

    public synchronized void addHighQualityHit() {
        highQualityHits++;
        progress++;
        cpm++;
        Platform.runLater(() -> model.setHQHits(highQualityHits));
    }

    public boolean isComplete() {
        return progress >= comboSize;
    }

    public boolean isModuleComplete() {
        return isComplete() && threads == 0;
    }

    public void setThreads(int threads) {
        this.threads = threads;
        model.setThreads(threads);
    }

    public synchronized void addThread() {
        threads++;
        model.setThreads(threads);
    }

    public synchronized void removeThread() {
        threads--;
        model.setThreads(threads);
    }

    public void setRunning(boolean running) { this.running = running; }

    public boolean isRunning() {
        return running;
    }

    public boolean isStopping() {
        return stopping;
    }

    public void setStopping(boolean stopping) {
        this.stopping = stopping;
    }

    public void setComboSize() {
        comboSize = model.collects().getCombos().size();
    }

    public int getComboSize() {
        return comboSize;
    }

    public void setCpm(int cpm) {
        this.cpm = cpm;
    }

    public int getCpm() {
        return cpm;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * Adds a rety to the UI for the current checking session
     * Only used in this way to update the UI. Not used in any thread
     */
    public void addRetry() {
        retries++;
        model.setRetries(retries);
    }

    /**
     * Adds an error to the UI for the current checking session
     * Only used in this way to update the UI. Not used in any thread
     */
    public void addError() {
        errors++;
        model.setErrors(errors);
    }

    public String getLoadedComboName() {
        return loadedComboName;
    }

    public void setLoadedComboName(String name) {
        loadedComboName = name;
    }

    public String getLoadedComboPath() {
        return loadedComboPath;
    }

    public void setLoadedComboPath(String path) {
        loadedComboPath = path;
    }

}
