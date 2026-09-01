package com.assignment.speechtotext.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.assignment.speechtotext.model.UptimeResponse;

@Service
public class AdminService {

    private final Instant serverStartTime;

    public AdminService() {
        this.serverStartTime = Instant.now();
    }

    public UptimeResponse getUptime() {
        Instant now = Instant.now();

        double uptimeSeconds =
                Duration.between(serverStartTime, now).toNanos() / 1_000_000_000.0;

        return new UptimeResponse(
                serverStartTime,
                now,
                uptimeSeconds
        );
    }
}