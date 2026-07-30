package zalo.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledLogger {
    
    private static ScheduledLogger instance;
    private ScheduledExecutorService scheduler;

    private ScheduledLogger() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    public static ScheduledLogger gI() {
        if (instance == null) {
            instance = new ScheduledLogger();
        }
        return instance;
    }
    
    public void start() {
        System.out.println("[SCHEDULED LOGGER] started");
    }
    
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            System.out.println("[SCHEDULED LOGGER] stopped");
        }
    }
}

