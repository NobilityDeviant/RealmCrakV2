package nobility;

import com.google.common.base.Stopwatch;
import javafx.application.Platform;
import nobility.model.Model;

import java.util.concurrent.TimeUnit;

public class Cycle implements Runnable {

    private final Stopwatch saveSettingsTimer = Stopwatch.createUnstarted();
    private final Model model;

    public Cycle(Model model) {
        this.model = model;
    }

    @Override
    public void run() {
        saveSettingsTimer.start();
        while (!Thread.interrupted()) {
            if (model.getIcon() != null) {
                if (model.module().settings().isRunning()) {
                    Platform.runLater(() -> model.getIcon().setTrayIconTooltip("Checking status: "
                            + model.module().settings().getProgress() + "/"
                            + model.module().settings().getComboSize()));
                } else {
                    Platform.runLater(() -> model.getIcon().setTrayIconTooltip("RealmCrak is sleeping.. Zzz"));
                }
            }
            if (saveSettingsTimer.isRunning()) {
                if (saveSettingsTimer.elapsed(TimeUnit.SECONDS) >= 60) {
                    model.saveSettings();
                    model.saveCombos();
                    saveSettingsTimer.reset();
                    saveSettingsTimer.start();
                }
            }
            try {
                Thread.sleep(1_250);
            } catch (Exception ignored) {}
        }
        model.saveSettings();
        model.saveCombos();
    }
}
