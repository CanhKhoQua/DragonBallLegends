package nro.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import network.SessionManager;

public class PerformanceMonitor {

    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();
    private static final String LOG_FILE = "log/performance-monitor.log";
    private static final long SAMPLE_SECONDS = 5;
    private static final long SUMMARY_SECONDS = 60;
    private static final double SPIKE_CPU_PERCENT = 45.0;
    private static final double SPIKE_JUMP_PERCENT = 25.0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final Map<Long, Long> lastThreadCpuNanos = new HashMap<>();

    private ScheduledExecutorService scheduler;
    private long sampleCount;
    private double lastCpu = -1.0;
    private long lastGcCount = -1L;
    private long lastGcTime = -1L;

    public static PerformanceMonitor gI() {
        return INSTANCE;
    }

    private PerformanceMonitor() {
    }

    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        enableThreadCpuTime();
        initGcSnapshot();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Performance-Monitor");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::safeSample, SAMPLE_SECONDS, SAMPLE_SECONDS, TimeUnit.SECONDS);
        writeLine("Performance monitor started. sample=" + SAMPLE_SECONDS + "s spikeCpu=" + SPIKE_CPU_PERCENT + "%");
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        writeLine("Performance monitor stopped.");
    }

    private void safeSample() {
        try {
            sample();
        } catch (Exception e) {
            writeLine("sample_error=" + e.getMessage());
        }
    }

    private void sample() {
        sampleCount++;
        double cpu = getProcessCpuPercent();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long usedMb = heap.getUsed() / 1024 / 1024;
        long maxMb = heap.getMax() / 1024 / 1024;
        int threads = Thread.activeCount();
        int liveThreads = threadBean.getThreadCount();
        int sessions = SessionManager.gI() != null ? SessionManager.gI().getSessions().size() : 0;
        int players = Client.gI() != null ? Client.gI().getPlayers().size() : 0;

        long[] gc = getGcSnapshot();
        long gcCountDelta = lastGcCount >= 0 ? gc[0] - lastGcCount : 0;
        long gcTimeDelta = lastGcTime >= 0 ? gc[1] - lastGcTime : 0;
        lastGcCount = gc[0];
        lastGcTime = gc[1];

        boolean intervalSummary = sampleCount % Math.max(1, SUMMARY_SECONDS / SAMPLE_SECONDS) == 0;
        boolean cpuSpike = cpu >= SPIKE_CPU_PERCENT || (lastCpu >= 0 && cpu - lastCpu >= SPIKE_JUMP_PERCENT);
        boolean gcSpike = gcTimeDelta >= 200;
        lastCpu = cpu;

        if (intervalSummary || cpuSpike || gcSpike) {
            String reason = intervalSummary ? "summary" : (cpuSpike ? "cpu_spike" : "gc_spike");
            writeLine(String.format(
                    "%s cpu=%.1f%% heap=%d/%dMB threads=%d liveThreads=%d sessions=%d players=%d gcCountDelta=%d gcTimeDeltaMs=%d topThreads=%s",
                    reason, cpu, usedMb, maxMb, threads, liveThreads, sessions, players,
                    gcCountDelta, gcTimeDelta, topThreadCpuDeltas()));
        }
    }

    private double getProcessCpuPercent() {
        try {
            java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            if (bean instanceof com.sun.management.OperatingSystemMXBean osBean) {
                double load = osBean.getProcessCpuLoad();
                if (!Double.isNaN(load) && load >= 0) {
                    return load * 100.0;
                }
            }
        } catch (Exception ignored) {
        }
        return 0.0;
    }

    private void enableThreadCpuTime() {
        try {
            if (threadBean.isThreadCpuTimeSupported() && !threadBean.isThreadCpuTimeEnabled()) {
                threadBean.setThreadCpuTimeEnabled(true);
            }
        } catch (Exception ignored) {
        }
    }

    private void initGcSnapshot() {
        long[] gc = getGcSnapshot();
        lastGcCount = gc[0];
        lastGcTime = gc[1];
    }

    private long[] getGcSnapshot() {
        long count = 0;
        long time = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long c = bean.getCollectionCount();
            long t = bean.getCollectionTime();
            if (c > 0) {
                count += c;
            }
            if (t > 0) {
                time += t;
            }
        }
        return new long[]{count, time};
    }

    private String topThreadCpuDeltas() {
        if (!threadBean.isThreadCpuTimeSupported() || !threadBean.isThreadCpuTimeEnabled()) {
            return "unavailable";
        }

        long[] ids = threadBean.getAllThreadIds();
        List<ThreadCpu> deltas = new ArrayList<>();
        Map<Long, Long> current = new HashMap<>();
        for (long id : ids) {
            long cpuNanos = threadBean.getThreadCpuTime(id);
            if (cpuNanos < 0) {
                continue;
            }
            current.put(id, cpuNanos);
            Long before = lastThreadCpuNanos.get(id);
            if (before == null) {
                continue;
            }
            long delta = cpuNanos - before;
            if (delta <= 0) {
                continue;
            }
            ThreadInfo info = threadBean.getThreadInfo(id);
            String name = info != null ? info.getThreadName() : ("thread-" + id);
            deltas.add(new ThreadCpu(name, delta / 1_000_000L));
        }
        lastThreadCpuNanos.clear();
        lastThreadCpuNanos.putAll(current);

        deltas.sort(Comparator.comparingLong((ThreadCpu t) -> t.deltaMs).reversed());
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(5, deltas.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            ThreadCpu t = deltas.get(i);
            sb.append(sanitize(t.name)).append("=").append(t.deltaMs).append("ms");
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    private String sanitize(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private synchronized void writeLine(String text) {
        try {
            File file = new File(LOG_FILE);
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write("[" + dateFormat.format(new Date()) + "] " + text);
                writer.newLine();
            }
        } catch (Exception ignored) {
        }
    }

    private static class ThreadCpu {
        private final String name;
        private final long deltaMs;

        private ThreadCpu(String name, long deltaMs) {
            this.name = name;
            this.deltaMs = deltaMs;
        }
    }
}
