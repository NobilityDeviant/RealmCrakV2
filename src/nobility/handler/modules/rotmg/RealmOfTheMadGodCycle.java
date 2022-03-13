package nobility.handler.modules.rotmg;

import com.google.common.base.Stopwatch;
import nobility.model.Model;

import java.util.concurrent.TimeUnit;

public class RealmOfTheMadGodCycle implements Runnable {

    private final Stopwatch elapsedTime = Stopwatch.createUnstarted();
    private final Stopwatch saveTime = Stopwatch.createUnstarted();
    private final Stopwatch cpmTime = Stopwatch.createUnstarted();
    private boolean running = false;
    private final Model model;

    public RealmOfTheMadGodCycle(Model model) {
        this.model = model;
    }

    public void start() {
        if (!elapsedTime.isRunning()) {
            elapsedTime.start();
        }
        if (!saveTime.isRunning()) {
            saveTime.start();
        }
        if (!cpmTime.isRunning()) {
            cpmTime.start();
        }
        running = true;
    }

    public void stop() {
        if (elapsedTime.isRunning()) {
            elapsedTime.stop();
        }
        if (saveTime.isRunning()) {
            saveTime.stop();
        }
        if (cpmTime.isRunning()) {
            cpmTime.stop();
        }
        running = false;
    }

    public void reset() {
        elapsedTime.reset();
        saveTime.reset();
        cpmTime.reset();
    }

    @Override
    public void run() {
        while (running) {
            if (model.module().settings().isComplete()) {
                model.module().stop();
            }
            model.updateProgress();
            updateTime();
            if (model.collects().getProxies().isEmpty()) {
                //Controller.instance().getModuleHandler().stop(true);
                model.collects().reloadProxies();
            }
            model.updateProxies();
            if (saveTime.elapsed(TimeUnit.SECONDS) > 30) {
                saveTime.reset();
                model.module().saveHits();
                model.module().saveCheckedHits();
                saveTime.start();
            }
            int cpm = model
                    .module().settings().getCpm();
            model.setRealtimeCpm(cpm);
            if (cpmTime.elapsed(TimeUnit.SECONDS) >= 60) {
                cpmTime.reset();
                model.setCpm(cpm);
                model.module().settings().setCpm(0);
                cpmTime.start();
            }
            try {
                Thread.sleep(250);
            } catch (Exception ignored) {}
        }
    }

    private void updateTime() {
        long milis = elapsedTime.elapsed(TimeUnit.MILLISECONDS);
        long hours = TimeUnit.MILLISECONDS.toHours(milis);
        milis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milis);
        milis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milis);
        model.setTime(hours + ":" + minutes + ":" + seconds);
    }
}
