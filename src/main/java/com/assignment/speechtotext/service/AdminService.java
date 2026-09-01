package com.assignment.speechtotext.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import com.assignment.speechtotext.model.UptimeResponse;

@Service
public class AdminService {

    private final Instant serverStartTime;
    private final ConfigurableApplicationContext applicationContext;
    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

    public AdminService(ConfigurableApplicationContext applicationContext) {
        this.serverStartTime = Instant.now();
        this.applicationContext = applicationContext;
    }

    public UptimeResponse getUptime() {

        Instant now = Instant.now();

        double uptimeSeconds =
                Duration.between(serverStartTime, now).toNanos()
                / 1_000_000_000.0;

        return new UptimeResponse(
                serverStartTime,
                now,
                uptimeSeconds
        );
    }

    public boolean requestShutdown() {

        if (!shutdownInProgress.compareAndSet(false, true)) {
            return false;
        }

        Thread shutdownThread = new Thread(() -> {

            try {
                // Allow the HTTP 202 response to be returned before
                // shutting down the Spring application context.
                Thread.sleep(500);

                applicationContext.close();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }
        });

        shutdownThread.setName("graceful-shutdown");
        shutdownThread.start();

        return true;
    }
}