package nobility;

import nobility.model.Model;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;

public class SystemCycle implements Runnable {

    private final Model model;
    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();
    private final CentralProcessor cpu = hal.getProcessor();
    private final GlobalMemory ram = hal.getMemory();
    private long[] prevTicks = cpu.getSystemCpuLoadTicks();

    public SystemCycle(Model model) {
        this.model = model;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            model.updateCpu(String.format("%.1f%%",
                    cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100));
            prevTicks = cpu.getSystemCpuLoadTicks();
            model.updateRam(FormatUtil.formatBytes(ram.getTotal() - ram.getAvailable()));
            try {
                Thread.sleep(1_000);
            } catch (Exception ignored) {}
        }
    }
}
